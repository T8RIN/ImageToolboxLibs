#[path = "../../interruption.rs"]
mod interruption;

use interruption::Interruption;
use std::borrow::Cow;
use std::io::{self, Write};
use std::panic::{AssertUnwindSafe, catch_unwind};
use std::sync::Arc;

use gif::{DisposalMethod, Encoder, Frame, Repeat};
use jni::JNIEnv;
use jni::objects::{JIntArray, JObject};
use jni::sys::{jbyteArray, jint, jlong};
use palette::Srgb;
use quantette::kmeans::KmeansOptions;
use quantette::{ImageRef, PaletteSize, Pipeline, QuantizeMethod};
use weezl::{BitOrder, LzwStatus, encode::Encoder as LzwEncoder};

#[derive(Default)]
struct ChunkWriter {
    bytes: Vec<u8>,
}

impl Write for ChunkWriter {
    fn write(&mut self, buffer: &[u8]) -> io::Result<usize> {
        self.bytes.extend_from_slice(buffer);
        Ok(buffer.len())
    }

    fn flush(&mut self) -> io::Result<()> {
        Ok(())
    }
}

struct NativeEncoder {
    encoder: Option<Encoder<ChunkWriter>>,
}

struct FrameOptions {
    width: jint,
    height: jint,
    x: jint,
    y: jint,
    delay: jint,
    dispose: jint,
    transparent: jint,
    quality: jint,
}

impl NativeEncoder {
    fn new(width: jint, height: jint, repeat: jint) -> Option<Self> {
        let width = u16::try_from(width).ok()?;
        let height = u16::try_from(height).ok()?;
        if width == 0 || height == 0 {
            return None;
        }

        let mut encoder = Encoder::new(ChunkWriter::default(), width, height, &[]).ok()?;
        if repeat >= 0 {
            let repeat = if repeat == 0 {
                Repeat::Infinite
            } else {
                Repeat::Finite(repeat as u16)
            };
            encoder.set_repeat(repeat).ok()?;
        }

        Some(Self {
            encoder: Some(encoder),
        })
    }

    fn add_frame(
        &mut self,
        argb: &[jint],
        options: FrameOptions,
        interruption: Option<Arc<Interruption>>,
    ) -> Option<Vec<u8>> {
        let width_u16 = u16::try_from(options.width).ok()?;
        let height_u16 = u16::try_from(options.height).ok()?;
        let expected_len = usize::from(width_u16).checked_mul(usize::from(height_u16))?;
        if argb.len() != expected_len {
            return None;
        }

        let cancelled = || interruption.as_ref().is_some_and(|check| check.is_requested());
        let mut pixels = Vec::with_capacity(argb.len());
        for chunk in argb.chunks(4096) {
            if cancelled() {
                return None;
            }
            pixels.extend(chunk.iter().map(|&color| {
                let [_, red, green, blue] = color.to_be_bytes();
                Srgb::new(red, green, blue)
            }));
        }
        let image = ImageRef::new(
            options.width as u32,
            options.height as u32,
            pixels.as_slice(),
        )
        .ok()?;

        let alpha_transparency =
            options.transparent != -1 && (options.transparent as u32 >> 24) == 0;
        let (mut palette_size, quantize_method) = quantization_settings(options.quality);
        if alpha_transparency {
            palette_size = PaletteSize::from_u16_clamped(palette_size.as_u16().min(255));
        }
        let quantize = || {
            Pipeline::new()
                .parallel(true)
                .palette_size(palette_size)
                .quantize_method(quantize_method)
                .input_image(image)
                .output_srgb8_indexed_image()
        };
        let indexed = if let Some(interruption) = &interruption {
            let check = Arc::clone(interruption);
            quantette::with_cancel_check(move || check.is_requested(), quantize)?
        } else {
            quantize()
        };
        let (mut palette, mut indices) = indexed.into_parts();

        let transparent_index = if options.transparent == -1 {
            None
        } else if alpha_transparency {
            let transparent_index = palette.len() as u8;
            let color = options.transparent as u32;
            palette.push(Srgb::new(
                ((color >> 16) & 0xff) as u8,
                ((color >> 8) & 0xff) as u8,
                (color & 0xff) as u8,
            ));
            for (indices, colors) in indices.chunks_mut(4096).zip(argb.chunks(4096)) {
                if cancelled() {
                    return None;
                }
                for (pixel_index, color) in indices.iter_mut().zip(colors) {
                    if (*color as u32 >> 24) == 0 {
                        *pixel_index = transparent_index;
                    }
                }
            }
            Some(transparent_index)
        } else {
            let color = options.transparent as u32;
            let target = [
                ((color >> 16) & 0xff) as i32,
                ((color >> 8) & 0xff) as i32,
                (color & 0xff) as i32,
            ];
            palette
                .iter()
                .enumerate()
                .min_by_key(|(_, color)| {
                    let red = target[0] - i32::from(color.red);
                    let green = target[1] - i32::from(color.green);
                    let blue = target[2] - i32::from(color.blue);
                    red * red + green * green + blue * blue
                })
                .map(|(index, _)| index as u8)
        };
        let palette: Vec<u8> = palette
            .into_iter()
            .flat_map(|color| [color.red, color.green, color.blue])
            .collect();

        if cancelled() {
            return None;
        }
        let mut frame =
            Frame::from_palette_pixels(width_u16, height_u16, indices, palette, transparent_index);
        frame.left = options.x as u16;
        frame.top = options.y as u16;
        frame.delay = options.delay as u16;
        frame.dispose = disposal_method(options.dispose, transparent_index.is_some());
        let encoder = self.encoder.as_mut()?;
        if interruption.is_some() {
            frame.buffer = Cow::Owned(encode_lzw(&frame.buffer, cancelled)?);
            encoder.write_lzw_pre_encoded_frame(&frame).ok()?;
        } else {
            encoder.write_frame(&frame).ok()?;
        }
        Some(std::mem::take(&mut encoder.get_mut().bytes))
    }

    fn finish(mut self) -> Option<Vec<u8>> {
        let output = self.encoder.take()?.into_inner().ok()?;
        Some(output.bytes)
    }
}

fn encode_lzw(indices: &[u8], cancelled: impl Fn() -> bool) -> Option<Vec<u8>> {
    const CHUNK_SIZE: usize = 16 * 1024;
    let mut max_index = 0;
    for chunk in indices.chunks(CHUNK_SIZE) {
        if cancelled() {
            return None;
        }
        max_index = max_index.max(chunk.iter().copied().max().unwrap_or(0));
    }

    // GIF requires a minimum code size of two bits, even for two-color frames.
    let code_size = (u32::from(max_index) + 1).max(4).next_power_of_two().trailing_zeros() as u8;
    let mut output = vec![code_size];
    let mut encoder = LzwEncoder::new(BitOrder::Lsb, code_size);
    let mut buffer = [0; CHUNK_SIZE];
    for mut chunk in indices.chunks(CHUNK_SIZE) {
        while !chunk.is_empty() {
            if cancelled() {
                return None;
            }
            let result = encoder.encode_bytes(chunk, &mut buffer);
            result.status.ok()?;
            if result.consumed_in == 0 && result.consumed_out == 0 {
                return None;
            }
            chunk = &chunk[result.consumed_in..];
            output.extend_from_slice(&buffer[..result.consumed_out]);
        }
    }

    encoder.finish();
    loop {
        if cancelled() {
            return None;
        }
        let result = encoder.encode_bytes(&[], &mut buffer);
        output.extend_from_slice(&buffer[..result.consumed_out]);
        match result.status.ok()? {
            LzwStatus::Done => break,
            LzwStatus::NoProgress => return None,
            LzwStatus::Ok => {}
        }
    }
    Some(output)
}

fn quantization_settings(quality: jint) -> (PaletteSize, QuantizeMethod) {
    let quality = quality.clamp(0, 100) as u16;
    let palette_size = PaletteSize::from_u16_clamped(2 + quality * 254 / 100);
    let quantize_method = if quality < 50 {
        QuantizeMethod::Wu
    } else {
        let sampling_factor = 0.1 + f32::from(quality - 50) / 50.0 * 0.9;
        QuantizeMethod::from(KmeansOptions::new().sampling_factor(sampling_factor))
    };
    (palette_size, quantize_method)
}

fn disposal_method(dispose: jint, has_transparency: bool) -> DisposalMethod {
    let code = if dispose >= 0 {
        dispose & 7
    } else if has_transparency {
        2
    } else {
        0
    };
    match code {
        1 => DisposalMethod::Keep,
        2 => DisposalMethod::Background,
        3 => DisposalMethod::Previous,
        _ => DisposalMethod::Any,
    }
}

fn null_byte_array() -> jbyteArray {
    std::ptr::null_mut()
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_com_t8rin_gif_1converter_GifEncoder_nativeStart(
    _env: JNIEnv,
    _object: JObject,
    width: jint,
    height: jint,
    repeat: jint,
) -> jlong {
    catch_unwind(AssertUnwindSafe(|| {
        NativeEncoder::new(width, height, repeat)
            .map(|encoder| Box::into_raw(Box::new(encoder)) as jlong)
            .unwrap_or_default()
    }))
    .unwrap_or_default()
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_com_t8rin_gif_1converter_GifEncoder_nativeAddFrame(
    mut env: JNIEnv,
    _object: JObject,
    handle: jlong,
    pixels: JIntArray,
    width: jint,
    height: jint,
    x: jint,
    y: jint,
    delay: jint,
    dispose: jint,
    transparent: jint,
    quality: jint,
) -> jbyteArray {
    catch_unwind(AssertUnwindSafe(|| {
        if handle == 0 {
            return null_byte_array();
        }
        let interruption = match Interruption::new(&mut env) {
            Ok(check) => Arc::new(check),
            Err(_) => return null_byte_array(),
        };
        if interruption.throw(&mut env) {
            return null_byte_array();
        }
        let length = match env.get_array_length(&pixels) {
            Ok(length) => length as usize,
            Err(_) => return null_byte_array(),
        };
        let mut argb = vec![0; length];
        if env.get_int_array_region(&pixels, 0, &mut argb).is_err() {
            return null_byte_array();
        }
        let encoder = unsafe { &mut *(handle as *mut NativeEncoder) };
        let Some(chunk) = encoder.add_frame(
            &argb,
            FrameOptions {
                width,
                height,
                x,
                y,
                delay,
                dispose,
                transparent,
                quality,
            },
            Some(Arc::clone(&interruption)),
        ) else {
            interruption.throw(&mut env);
            return null_byte_array();
        };
        if interruption.throw(&mut env) {
            return null_byte_array();
        }
        env.byte_array_from_slice(&chunk)
            .map(|array| array.into_raw())
            .unwrap_or_else(|_| null_byte_array())
    }))
    .unwrap_or_else(|_| null_byte_array())
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_com_t8rin_gif_1converter_GifEncoder_nativeFinish(
    env: JNIEnv,
    _object: JObject,
    handle: jlong,
) -> jbyteArray {
    catch_unwind(AssertUnwindSafe(|| {
        if handle == 0 {
            return null_byte_array();
        }
        let encoder = unsafe { Box::from_raw(handle as *mut NativeEncoder) };
        let Some(chunk) = encoder.finish() else {
            return null_byte_array();
        };
        env.byte_array_from_slice(&chunk)
            .map(|array| array.into_raw())
            .unwrap_or_else(|_| null_byte_array())
    }))
    .unwrap_or_else(|_| null_byte_array())
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_com_t8rin_gif_1converter_GifEncoder_nativeAbort(
    _env: JNIEnv,
    _object: JObject,
    handle: jlong,
) {
    if handle != 0 {
        unsafe { drop(Box::from_raw(handle as *mut NativeEncoder)) };
    }
}

#[cfg(test)]
mod tests {
    use std::io::Cursor;

    use gif::{ColorOutput, DecodeOptions};

    use super::*;

    #[test]
    fn maps_quality_to_direct_zero_to_hundred_scale() {
        let (minimum_palette, minimum_method) = quantization_settings(-1);
        assert_eq!(minimum_palette.as_u16(), 2);
        assert_eq!(minimum_method, QuantizeMethod::Wu);

        let (middle_palette, middle_method) = quantization_settings(50);
        assert_eq!(middle_palette.as_u16(), 129);
        let QuantizeMethod::Kmeans(middle_options) = middle_method else {
            panic!("quality 50 must use k-means");
        };
        assert_eq!(middle_options.get_sampling_factor(), 0.1);

        let (maximum_palette, maximum_method) = quantization_settings(101);
        assert_eq!(maximum_palette.as_u16(), 256);
        let QuantizeMethod::Kmeans(maximum_options) = maximum_method else {
            panic!("quality 100 must use k-means");
        };
        assert_eq!(maximum_options.get_sampling_factor(), 1.0);
    }

    #[test]
    fn encodes_streamed_frame_metadata() {
        let mut encoder = NativeEncoder::new(2, 2, 0).unwrap();
        let mut encoded = encoder
            .add_frame(
                &[
                    0xffff0000u32 as i32,
                    0xff00ff00u32 as i32,
                    0xff0000ffu32 as i32,
                    0xffffffffu32 as i32,
                ],
                FrameOptions {
                    width: 2,
                    height: 2,
                    x: 0,
                    y: 0,
                    delay: 12,
                    dispose: -1,
                    transparent: 0xffff0000u32 as i32,
                    quality: 20,
                },
                None,
            )
            .unwrap();
        encoded.extend(encoder.finish().unwrap());

        assert_eq!(&encoded[..6], b"GIF89a");
        assert_eq!(encoded.last(), Some(&0x3b));

        let mut options = DecodeOptions::new();
        options.set_color_output(ColorOutput::Indexed);
        let mut decoder = options.read_info(Cursor::new(encoded)).unwrap();
        assert_eq!(decoder.width(), 2);
        assert_eq!(decoder.height(), 2);
        assert_eq!(decoder.repeat(), Repeat::Infinite);

        let frame = decoder.read_next_frame().unwrap().unwrap();
        assert_eq!(frame.delay, 12);
        assert_eq!(frame.dispose, DisposalMethod::Background);
        assert!(frame.transparent.is_some());
        assert!(decoder.read_next_frame().unwrap().is_none());
    }

    #[test]
    fn transparent_argb_does_not_make_opaque_black_transparent() {
        let mut encoder = NativeEncoder::new(2, 1, -1).unwrap();
        let mut encoded = encoder
            .add_frame(
                &[0x00000000, 0xff000000u32 as i32],
                FrameOptions {
                    width: 2,
                    height: 1,
                    x: 0,
                    y: 0,
                    delay: 0,
                    dispose: -1,
                    transparent: 0x00000000,
                    quality: 100,
                },
                None,
            )
            .unwrap();
        encoded.extend(encoder.finish().unwrap());

        let mut options = DecodeOptions::new();
        options.set_color_output(ColorOutput::RGBA);
        let mut decoder = options.read_info(Cursor::new(encoded)).unwrap();
        let frame = decoder.read_next_frame().unwrap().unwrap();

        assert_eq!(&frame.buffer[..4], &[0, 0, 0, 0]);
        assert_eq!(&frame.buffer[4..], &[0, 0, 0, 255]);
    }
}
