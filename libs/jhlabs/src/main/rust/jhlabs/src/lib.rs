use std::ffi::c_void;
use std::panic::{AssertUnwindSafe, catch_unwind};
use std::ptr;
use std::sync::OnceLock;

use jni::JNIEnv;
use jni::objects::{JFloatArray, JIntArray, JObject, JObjectArray, JString, JValue};
use jni::sys::{jint, jobject};

const ANDROID_BITMAP_FORMAT_RGBA_8888: i32 = 1;

#[repr(C)]
struct AndroidBitmapInfo {
    width: u32,
    height: u32,
    stride: u32,
    format: i32,
    flags: u32,
}

#[link(name = "jnigraphics")]
unsafe extern "C" {
    fn AndroidBitmap_getInfo(
        env: *mut jni::sys::JNIEnv,
        bitmap: jobject,
        info: *mut AndroidBitmapInfo,
    ) -> i32;
    fn AndroidBitmap_lockPixels(
        env: *mut jni::sys::JNIEnv,
        bitmap: jobject,
        address: *mut *mut c_void,
    ) -> i32;
    fn AndroidBitmap_unlockPixels(env: *mut jni::sys::JNIEnv, bitmap: jobject) -> i32;
}

struct BitmapPixels {
    pixels: Vec<u32>,
    width: usize,
    height: usize,
}

fn read_rgba_bitmap(env: &JNIEnv, bitmap: &JObject) -> Option<BitmapPixels> {
    let mut info = AndroidBitmapInfo {
        width: 0,
        height: 0,
        stride: 0,
        format: 0,
        flags: 0,
    };
    let result = unsafe { AndroidBitmap_getInfo(env.get_raw(), bitmap.as_raw(), &mut info) };
    if result != 0 || info.format != ANDROID_BITMAP_FORMAT_RGBA_8888 {
        return None;
    }
    let width = info.width as usize;
    let height = info.height as usize;
    let pixel_count = width.checked_mul(height)?;
    let mut address = ptr::null_mut();
    if unsafe { AndroidBitmap_lockPixels(env.get_raw(), bitmap.as_raw(), &mut address) } != 0 {
        return None;
    }
    let mut pixels = Vec::with_capacity(pixel_count);
    for y in 0..height {
        let row = unsafe { (address as *const u8).add(y * info.stride as usize) };
        for x in 0..width {
            let rgba = unsafe { std::slice::from_raw_parts(row.add(x * 4), 4) };
            pixels.push(argb(rgba[3], rgba[0], rgba[1], rgba[2]));
        }
    }
    unsafe { AndroidBitmap_unlockPixels(env.get_raw(), bitmap.as_raw()) };
    Some(BitmapPixels {
        pixels,
        width,
        height,
    })
}

fn read_bitmap(env: &mut JNIEnv, bitmap: &JObject) -> Option<BitmapPixels> {
    if let Some(pixels) = read_rgba_bitmap(env, bitmap) {
        return Some(pixels);
    }
    let config_class = env.find_class("android/graphics/Bitmap$Config").ok()?;
    let config = env
        .get_static_field(
            config_class,
            "ARGB_8888",
            "Landroid/graphics/Bitmap$Config;",
        )
        .ok()?
        .l()
        .ok()?;
    let copy = env
        .call_method(
            bitmap,
            "copy",
            "(Landroid/graphics/Bitmap$Config;Z)Landroid/graphics/Bitmap;",
            &[JValue::Object(&config), JValue::Bool(0)],
        )
        .ok()?
        .l()
        .ok()?;
    read_rgba_bitmap(env, &copy)
}

fn create_bitmap<'local>(
    env: &mut JNIEnv<'local>,
    pixels: &[u32],
    width: usize,
    height: usize,
) -> Option<JObject<'local>> {
    let config_class = env.find_class("android/graphics/Bitmap$Config").ok()?;
    let config = env
        .get_static_field(
            config_class,
            "ARGB_8888",
            "Landroid/graphics/Bitmap$Config;",
        )
        .ok()?
        .l()
        .ok()?;
    let bitmap_class = env.find_class("android/graphics/Bitmap").ok()?;
    let bitmap = env
        .call_static_method(
            bitmap_class,
            "createBitmap",
            "(IILandroid/graphics/Bitmap$Config;)Landroid/graphics/Bitmap;",
            &[
                JValue::Int(width as jint),
                JValue::Int(height as jint),
                JValue::Object(&config),
            ],
        )
        .ok()?
        .l()
        .ok()?;
    let mut info = AndroidBitmapInfo {
        width: 0,
        height: 0,
        stride: 0,
        format: 0,
        flags: 0,
    };
    if unsafe { AndroidBitmap_getInfo(env.get_raw(), bitmap.as_raw(), &mut info) } != 0 {
        return None;
    }
    let mut address = ptr::null_mut();
    if unsafe { AndroidBitmap_lockPixels(env.get_raw(), bitmap.as_raw(), &mut address) } != 0 {
        return None;
    }
    for y in 0..height {
        let row = unsafe { (address as *mut u8).add(y * info.stride as usize) };
        for x in 0..width {
            let color = pixels[y * width + x];
            let rgba = [red(color), green(color), blue(color), alpha(color)];
            unsafe { ptr::copy_nonoverlapping(rgba.as_ptr(), row.add(x * 4), 4) };
        }
    }
    unsafe { AndroidBitmap_unlockPixels(env.get_raw(), bitmap.as_raw()) };
    Some(bitmap)
}

fn class_name(env: &mut JNIEnv, filter: &JObject) -> Option<String> {
    let class = env
        .call_method(filter, "getClass", "()Ljava/lang/Class;", &[])
        .ok()?
        .l()
        .ok()?;
    let name = env
        .call_method(class, "getSimpleName", "()Ljava/lang/String;", &[])
        .ok()?
        .l()
        .ok()?;
    let name = JString::from(name);
    Some(env.get_string(&name).ok()?.into())
}

fn object_class_name(env: &mut JNIEnv, object: &JObject) -> Option<String> {
    class_name(env, object)
}

fn binary_mask(env: &mut JNIEnv, filter: &JObject, pixels: &[u32]) -> Vec<bool> {
    let function = env
        .call_method(
            filter,
            "getBlackFunction",
            "()Lcom/jhlabs/math/BinaryFunction;",
            &[],
        )
        .ok()
        .and_then(|value| value.l().ok());
    let Some(function) = function else {
        let _ = env.exception_clear();
        return pixels.iter().map(|color| *color == 0xff000000).collect();
    };
    if object_class_name(env, &function).as_deref() == Some("BlackFunction") {
        return pixels.iter().map(|color| *color == 0xff000000).collect();
    }
    pixels
        .iter()
        .map(|color| {
            env.call_method(&function, "isBlack", "(I)Z", &[JValue::Int(*color as i32)])
                .ok()
                .and_then(|value| value.z().ok())
                .unwrap_or(false)
        })
        .collect()
}

fn colormap_color(env: &mut JNIEnv, filter: &JObject, position: f32, fallback: u32) -> u32 {
    let colormap = env
        .call_method(filter, "getColormap", "()Lcom/jhlabs/Colormap;", &[])
        .ok()
        .and_then(|value| value.l().ok());
    let Some(colormap) = colormap.filter(|value| !value.is_null()) else {
        let _ = env.exception_clear();
        return fallback;
    };
    env.call_method(colormap, "getColor", "(F)I", &[JValue::Float(position)])
        .ok()
        .and_then(|value| value.i().ok())
        .map(|color| color as u32)
        .unwrap_or(fallback)
}

fn int_param(env: &mut JNIEnv, filter: &JObject, getter: &str, default: i32) -> i32 {
    match env
        .call_method(filter, getter, "()I", &[])
        .and_then(|value| value.i())
    {
        Ok(value) => value,
        Err(_) => {
            let _ = env.exception_clear();
            default
        }
    }
}

fn float_param(env: &mut JNIEnv, filter: &JObject, getter: &str, default: f32) -> f32 {
    match env
        .call_method(filter, getter, "()F", &[])
        .and_then(|value| value.f())
    {
        Ok(value) => value,
        Err(_) => {
            let _ = env.exception_clear();
            default
        }
    }
}

fn bool_param(env: &mut JNIEnv, filter: &JObject, getter: &str, default: bool) -> bool {
    match env
        .call_method(filter, getter, "()Z", &[])
        .and_then(|value| value.z())
    {
        Ok(value) => value,
        Err(_) => {
            let _ = env.exception_clear();
            default
        }
    }
}

fn int_field(env: &mut JNIEnv, filter: &JObject, field: &str, default: i32) -> i32 {
    match env
        .get_field(filter, field, "I")
        .and_then(|value| value.i())
    {
        Ok(value) => value,
        Err(_) => {
            let _ = env.exception_clear();
            default
        }
    }
}

fn float_field(env: &mut JNIEnv, filter: &JObject, field: &str, default: f32) -> f32 {
    match env
        .get_field(filter, field, "F")
        .and_then(|value| value.f())
    {
        Ok(value) => value,
        Err(_) => {
            let _ = env.exception_clear();
            default
        }
    }
}

fn bool_field(env: &mut JNIEnv, filter: &JObject, field: &str, default: bool) -> bool {
    match env
        .get_field(filter, field, "Z")
        .and_then(|value| value.z())
    {
        Ok(value) => value,
        Err(_) => {
            let _ = env.exception_clear();
            default
        }
    }
}

fn long_field(env: &mut JNIEnv, filter: &JObject, field: &str, default: i64) -> i64 {
    match env
        .get_field(filter, field, "J")
        .and_then(|value| value.j())
    {
        Ok(value) => value,
        Err(_) => {
            let _ = env.exception_clear();
            default
        }
    }
}

fn int_array_from_object(env: &mut JNIEnv, value: JObject) -> Option<Vec<i32>> {
    if value.is_null() {
        return None;
    }
    let array = JIntArray::from(value);
    let length = env.get_array_length(&array).ok()? as usize;
    let mut values = vec![0; length];
    env.get_int_array_region(&array, 0, &mut values).ok()?;
    Some(values)
}

fn int_array_param(env: &mut JNIEnv, filter: &JObject, getter: &str) -> Option<Vec<i32>> {
    let value = match env.call_method(filter, getter, "()[I", &[]) {
        Ok(value) => value.l().ok()?,
        Err(_) => {
            let _ = env.exception_clear();
            return None;
        }
    };
    int_array_from_object(env, value)
}

fn int_2d_array_param(env: &mut JNIEnv, filter: &JObject, getter: &str) -> Option<Vec<Vec<i32>>> {
    let value = match env.call_method(filter, getter, "()[[I", &[]) {
        Ok(value) => value.l().ok()?,
        Err(_) => {
            let _ = env.exception_clear();
            return None;
        }
    };
    if value.is_null() {
        return None;
    }
    let array = JObjectArray::from(value);
    let length = env.get_array_length(&array).ok()?;
    let mut result = Vec::with_capacity(length as usize);
    for index in 0..length {
        let row = env.get_object_array_element(&array, index).ok()?;
        result.push(int_array_from_object(env, row)?);
    }
    Some(result)
}

fn float_array_field(env: &mut JNIEnv, object: &JObject, field: &str) -> Option<Vec<f32>> {
    let value = match env.get_field(object, field, "[F") {
        Ok(value) => value.l().ok()?,
        Err(_) => {
            let _ = env.exception_clear();
            return None;
        }
    };
    if value.is_null() {
        return None;
    }
    let array = JFloatArray::from(value);
    let length = env.get_array_length(&array).ok()? as usize;
    let mut values = vec![0.0; length];
    env.get_float_array_region(&array, 0, &mut values).ok()?;
    Some(values)
}

struct JavaRandom {
    seed: u64,
    next_gaussian: Option<f64>,
}

impl JavaRandom {
    fn new(seed: i64) -> Self {
        Self {
            seed: (seed as u64 ^ 0x5deece66d) & ((1_u64 << 48) - 1),
            next_gaussian: None,
        }
    }

    fn next(&mut self, bits: u32) -> i32 {
        self.seed = (self.seed.wrapping_mul(0x5deece66d).wrapping_add(0xb)) & ((1_u64 << 48) - 1);
        (self.seed >> (48 - bits)) as i32
    }

    fn next_float(&mut self) -> f32 {
        self.next(24) as f32 / (1_u32 << 24) as f32
    }

    fn next_int(&mut self) -> i32 {
        self.next(32)
    }

    fn next_double(&mut self) -> f64 {
        let high = self.next(26) as i64;
        let low = self.next(27) as i64;
        ((high << 27) + low) as f64 / (1_u64 << 53) as f64
    }

    fn next_gaussian(&mut self) -> f64 {
        if let Some(value) = self.next_gaussian.take() {
            return value;
        }
        loop {
            let first = 2.0 * self.next_double() - 1.0;
            let second = 2.0 * self.next_double() - 1.0;
            let square = first * first + second * second;
            if square < 1.0 && square != 0.0 {
                let multiplier = (-2.0 * square.ln() / square).sqrt();
                self.next_gaussian = Some(second * multiplier);
                return first * multiplier;
            }
        }
    }
}

struct PerlinNoise {
    permutation: [usize; 514],
    gradient1: [f32; 514],
    gradient2: [[f32; 2]; 514],
    gradient3: [[f32; 3]; 514],
}

impl PerlinNoise {
    fn new(seed: i64) -> Self {
        let mut random = JavaRandom::new(seed);
        let mut permutation = [0_usize; 514];
        let mut gradient1 = [0.0_f32; 514];
        let mut gradient2 = [[0.0_f32; 2]; 514];
        let mut gradient3 = [[0.0_f32; 3]; 514];
        let positive = |random: &mut JavaRandom| random.next_int() & 0x7fff_ffff;
        for index in 0..256 {
            permutation[index] = index;
            gradient1[index] = ((positive(&mut random) % 512) - 256) as f32 / 256.0;
            for coordinate in 0..2 {
                gradient2[index][coordinate] = ((positive(&mut random) % 512) - 256) as f32 / 256.0;
            }
            let length = (gradient2[index][0] * gradient2[index][0]
                + gradient2[index][1] * gradient2[index][1])
                .sqrt();
            gradient2[index][0] /= length;
            gradient2[index][1] /= length;
            for coordinate in 0..3 {
                gradient3[index][coordinate] = ((positive(&mut random) % 512) - 256) as f32 / 256.0;
            }
            let length = (gradient3[index][0] * gradient3[index][0]
                + gradient3[index][1] * gradient3[index][1]
                + gradient3[index][2] * gradient3[index][2])
                .sqrt();
            for coordinate in 0..3 {
                gradient3[index][coordinate] /= length;
            }
        }
        for index in (0..256).rev() {
            let target = (positive(&mut random) % 256) as usize;
            permutation.swap(index, target);
        }
        for index in 0..258 {
            permutation[256 + index] = permutation[index];
            gradient1[256 + index] = gradient1[index];
            gradient2[256 + index] = gradient2[index];
            gradient3[256 + index] = gradient3[index];
        }
        Self {
            permutation,
            gradient1,
            gradient2,
            gradient3,
        }
    }

    fn noise1(&self, value: f32) -> f32 {
        let shifted = value + 4096.0;
        let first = shifted as i32 as usize & 255;
        let second = (first + 1) & 255;
        let remainder = shifted - shifted as i32 as f32;
        let next_remainder = remainder - 1.0;
        let curve = remainder * remainder * (3.0 - 2.0 * remainder);
        let first_value = remainder * self.gradient1[self.permutation[first]];
        let second_value = next_remainder * self.gradient1[self.permutation[second]];
        2.3 * (first_value + curve * (second_value - first_value))
    }

    fn noise2(&self, x: f32, y: f32) -> f32 {
        let shifted_x = x + 4096.0;
        let x0 = shifted_x as i32 as usize & 255;
        let x1 = (x0 + 1) & 255;
        let remainder_x = shifted_x - shifted_x as i32 as f32;
        let next_x = remainder_x - 1.0;
        let shifted_y = y + 4096.0;
        let y0 = shifted_y as i32 as usize & 255;
        let y1 = (y0 + 1) & 255;
        let remainder_y = shifted_y - shifted_y as i32 as f32;
        let next_y = remainder_y - 1.0;
        let first = self.permutation[x0];
        let second = self.permutation[x1];
        let corners = [
            self.permutation[first + y0],
            self.permutation[second + y0],
            self.permutation[first + y1],
            self.permutation[second + y1],
        ];
        let curve_x = remainder_x * remainder_x * (3.0 - 2.0 * remainder_x);
        let curve_y = remainder_y * remainder_y * (3.0 - 2.0 * remainder_y);
        let dot = |gradient: [f32; 2], dx: f32, dy: f32| dx * gradient[0] + dy * gradient[1];
        let top_left = dot(self.gradient2[corners[0]], remainder_x, remainder_y);
        let top_right = dot(self.gradient2[corners[1]], next_x, remainder_y);
        let bottom_left = dot(self.gradient2[corners[2]], remainder_x, next_y);
        let bottom_right = dot(self.gradient2[corners[3]], next_x, next_y);
        let top = top_left + curve_x * (top_right - top_left);
        let bottom = bottom_left + curve_x * (bottom_right - bottom_left);
        1.5 * (top + curve_y * (bottom - top))
    }

    fn noise3(&self, x: f32, y: f32, z: f32) -> f32 {
        let shifted_x = x + 4096.0;
        let x0 = shifted_x as i32 as usize & 255;
        let x1 = (x0 + 1) & 255;
        let rx0 = shifted_x - shifted_x as i32 as f32;
        let rx1 = rx0 - 1.0;
        let shifted_y = y + 4096.0;
        let y0 = shifted_y as i32 as usize & 255;
        let y1 = (y0 + 1) & 255;
        let ry0 = shifted_y - shifted_y as i32 as f32;
        let ry1 = ry0 - 1.0;
        let shifted_z = z + 4096.0;
        let z0 = shifted_z as i32 as usize & 255;
        let z1 = (z0 + 1) & 255;
        let rz0 = shifted_z - shifted_z as i32 as f32;
        let rz1 = rz0 - 1.0;
        let first = self.permutation[x0];
        let second = self.permutation[x1];
        let base = [
            self.permutation[first + y0],
            self.permutation[second + y0],
            self.permutation[first + y1],
            self.permutation[second + y1],
        ];
        let curve = |value: f32| value * value * (3.0 - 2.0 * value);
        let sx = curve(rx0);
        let sy = curve(ry0);
        let sz = curve(rz0);
        let dot = |gradient: [f32; 3], dx: f32, dy: f32, dz: f32| {
            dx * gradient[0] + dy * gradient[1] + dz * gradient[2]
        };
        let plane = |z_index: usize, dz: f32| {
            let top_left = dot(self.gradient3[base[0] + z_index], rx0, ry0, dz);
            let top_right = dot(self.gradient3[base[1] + z_index], rx1, ry0, dz);
            let bottom_left = dot(self.gradient3[base[2] + z_index], rx0, ry1, dz);
            let bottom_right = dot(self.gradient3[base[3] + z_index], rx1, ry1, dz);
            let top = top_left + sx * (top_right - top_left);
            let bottom = bottom_left + sx * (bottom_right - bottom_left);
            top + sy * (bottom - top)
        };
        let front = plane(z0, rz0);
        let back = plane(z1, rz1);
        1.5 * (front + sz * (back - front))
    }

    fn turbulence3(&self, x: f32, y: f32, z: f32, octaves: f32) -> f32 {
        let mut result = 0.0;
        let mut frequency = 1.0;
        while frequency <= octaves {
            result += self
                .noise3(frequency * x, frequency * y, frequency * z)
                .abs()
                / frequency;
            frequency *= 2.0;
        }
        result
    }
}

fn perlin_noise() -> &'static PerlinNoise {
    static NOISE: OnceLock<PerlinNoise> = OnceLock::new();
    NOISE.get_or_init(|| PerlinNoise::new(0x4a484c616273))
}

#[derive(Clone, Copy)]
struct CellPoint {
    distance: f32,
    dx: f32,
    dy: f32,
    x: f32,
    y: f32,
}

struct CellularConfig {
    randomness: f32,
    grid_type: i32,
    distance_power: f32,
    coefficients: [f32; 4],
    angle_coefficient: f32,
    gradient_coefficient: f32,
}

fn cellular_probabilities() -> &'static [u8; 8192] {
    static PROBABILITIES: OnceLock<[u8; 8192]> = OnceLock::new();
    PROBABILITIES.get_or_init(|| {
        let mut values = [0_u8; 8192];
        let mut factorial = 1.0_f32;
        let mut total = 0.0_f32;
        for index in 0..10 {
            if index > 1 {
                factorial *= index as f32;
            }
            let probability = 2.5_f32.powi(index) * (-2.5_f32).exp() / factorial;
            let start = (total * 8192.0) as usize;
            total += probability;
            let end = (total * 8192.0) as usize;
            for value in &mut values[start.min(8192)..end.min(8192)] {
                *value = index as u8;
            }
        }
        values
    })
}

fn cellular_check_cube(
    x: f32,
    y: f32,
    cube_x: i32,
    cube_y: i32,
    config: &CellularConfig,
    results: &mut [CellPoint; 3],
) -> f32 {
    let mut random = JavaRandom::new(571_i64 * cube_x as i64 + 23_i64 * cube_y as i64);
    let points = match config.grid_type {
        1 | 2 => 1,
        3 | 4 => 2,
        _ => cellular_probabilities()[(random.next_int() as usize) & 0x1fff] as usize,
    };
    for index in 0..points {
        let (mut point_x, mut point_y, weight) = match config.grid_type {
            1 => (0.5, 0.5, 1.0),
            2 => (0.75, if cube_x & 1 == 0 { 0.0 } else { 0.5 }, 1.0),
            3 if index == 0 => (0.207, 0.207, 1.0),
            3 => (0.707, 0.707, 1.6),
            4 if cube_y & 1 == 0 && index == 0 => (0.25, 0.35, 1.0),
            4 if cube_y & 1 == 0 => (0.75, 0.65, 1.0),
            4 if index == 0 => (0.75, 0.35, 1.0),
            4 => (0.25, 0.65, 1.0),
            _ => (random.next_float(), random.next_float(), 1.0),
        };
        if config.randomness != 0.0 {
            if config.grid_type == 1 {
                point_x += config.randomness * (random.next_float() - 0.5);
                point_y += config.randomness * (random.next_float() - 0.5);
            } else if config.grid_type != 0 {
                point_x += config.randomness
                    * perlin_noise().noise2(
                        271.0 * (cube_x as f32 + point_x),
                        271.0 * (cube_y as f32 + point_y),
                    );
                point_y += config.randomness
                    * perlin_noise().noise2(
                        271.0 * (cube_x as f32 + point_x) + 89.0,
                        271.0 * (cube_y as f32 + point_y) + 137.0,
                    );
            }
        }
        let dx = (x - point_x).abs() * weight;
        let dy = (y - point_y).abs() * weight;
        let distance = if config.distance_power == 1.0 {
            dx + dy
        } else if config.distance_power == 2.0 {
            (dx * dx + dy * dy).sqrt()
        } else {
            (dx.powf(config.distance_power) + dy.powf(config.distance_power))
                .powf(1.0 / config.distance_power)
        };
        let point = CellPoint {
            distance,
            dx,
            dy,
            x: cube_x as f32 + point_x,
            y: cube_y as f32 + point_y,
        };
        if distance < results[0].distance {
            results[2] = results[1];
            results[1] = results[0];
            results[0] = point;
        } else if distance < results[1].distance {
            results[2] = results[1];
            results[1] = point;
        } else if distance < results[2].distance {
            results[2] = point;
        }
    }
    results[2].distance
}

fn cellular_evaluate(x: f32, y: f32, config: &CellularConfig) -> (f32, [CellPoint; 3]) {
    let empty = CellPoint {
        distance: f32::INFINITY,
        dx: 0.0,
        dy: 0.0,
        x: 0.0,
        y: 0.0,
    };
    let mut results = [empty; 3];
    let integer_x = x as i32;
    let integer_y = y as i32;
    let fraction_x = x - integer_x as f32;
    let fraction_y = y - integer_y as f32;
    let mut distance = cellular_check_cube(
        fraction_x,
        fraction_y,
        integer_x,
        integer_y,
        config,
        &mut results,
    );
    if distance > fraction_y {
        distance = cellular_check_cube(
            fraction_x,
            fraction_y + 1.0,
            integer_x,
            integer_y - 1,
            config,
            &mut results,
        );
    }
    if distance > 1.0 - fraction_y {
        distance = cellular_check_cube(
            fraction_x,
            fraction_y - 1.0,
            integer_x,
            integer_y + 1,
            config,
            &mut results,
        );
    }
    if distance > fraction_x {
        cellular_check_cube(
            fraction_x + 1.0,
            fraction_y,
            integer_x - 1,
            integer_y,
            config,
            &mut results,
        );
        if distance > fraction_y {
            distance = cellular_check_cube(
                fraction_x + 1.0,
                fraction_y + 1.0,
                integer_x - 1,
                integer_y - 1,
                config,
                &mut results,
            );
        }
        if distance > 1.0 - fraction_y {
            distance = cellular_check_cube(
                fraction_x + 1.0,
                fraction_y - 1.0,
                integer_x - 1,
                integer_y + 1,
                config,
                &mut results,
            );
        }
    }
    if distance > 1.0 - fraction_x {
        distance = cellular_check_cube(
            fraction_x - 1.0,
            fraction_y,
            integer_x + 1,
            integer_y,
            config,
            &mut results,
        );
        if distance > fraction_y {
            distance = cellular_check_cube(
                fraction_x - 1.0,
                fraction_y + 1.0,
                integer_x + 1,
                integer_y - 1,
                config,
                &mut results,
            );
        }
        if distance > 1.0 - fraction_y {
            cellular_check_cube(
                fraction_x - 1.0,
                fraction_y - 1.0,
                integer_x + 1,
                integer_y + 1,
                config,
                &mut results,
            );
        }
    }
    let mut value = (0..3)
        .map(|index| config.coefficients[index] * results[index].distance)
        .sum::<f32>();
    if config.angle_coefficient != 0.0 {
        let angle = (y - results[0].y)
            .atan2(x - results[0].x)
            .rem_euclid(std::f32::consts::TAU)
            / (4.0 * std::f32::consts::PI);
        value += config.angle_coefficient * angle;
    }
    if config.gradient_coefficient != 0.0 {
        value += config.gradient_coefficient / (results[0].dy + results[0].dx);
    }
    (value, results)
}

fn clamp(value: i32) -> u8 {
    value.clamp(0, 255) as u8
}

fn argb(a: u8, r: u8, g: u8, b: u8) -> u32 {
    ((a as u32) << 24) | ((r as u32) << 16) | ((g as u32) << 8) | b as u32
}

fn alpha(color: u32) -> u8 {
    (color >> 24) as u8
}

fn red(color: u32) -> u8 {
    (color >> 16) as u8
}

fn green(color: u32) -> u8 {
    (color >> 8) as u8
}

fn blue(color: u32) -> u8 {
    color as u8
}

fn brightness(color: u32) -> i32 {
    (red(color) as i32 + green(color) as i32 + blue(color) as i32) / 3
}

fn mix_colors(amount: f32, first: u32, second: u32) -> u32 {
    let amount = amount.clamp(0.0, 1.0);
    let inverse = 1.0 - amount;
    argb(
        clamp((alpha(first) as f32 * inverse + alpha(second) as f32 * amount) as i32),
        clamp((red(first) as f32 * inverse + red(second) as f32 * amount) as i32),
        clamp((green(first) as f32 * inverse + green(second) as f32 * amount) as i32),
        clamp((blue(first) as f32 * inverse + blue(second) as f32 * amount) as i32),
    )
}

fn smooth_step(first: f32, second: f32, value: f32) -> f32 {
    if value < first {
        0.0
    } else if value >= second {
        1.0
    } else {
        let value = (value - first) / (second - first);
        value * value * (3.0 - 2.0 * value)
    }
}

fn triangle(value: f32) -> f32 {
    let remainder = value.rem_euclid(1.0);
    2.0 * if remainder < 0.5 {
        remainder
    } else {
        1.0 - remainder
    }
}

fn spline(value: f32, knots: &[f32]) -> f32 {
    let spans = knots.len() - 3;
    let scaled = value.clamp(0.0, 1.0) * spans as f32;
    let span = (scaled as usize).min(knots.len() - 4);
    let x = scaled - span as f32;
    let [k0, k1, k2, k3] = [
        knots[span],
        knots[span + 1],
        knots[span + 2],
        knots[span + 3],
    ];
    let c3 = -0.5 * k0 + 1.5 * k1 - 1.5 * k2 + 0.5 * k3;
    let c2 = k0 - 2.5 * k1 + 2.0 * k2 - 0.5 * k3;
    let c1 = -0.5 * k0 + 0.5 * k2;
    ((c3 * x + c2) * x + c1) * x + k1
}

fn curve_table(env: &mut JNIEnv, curve: &JObject) -> Option<Vec<u8>> {
    let x = float_array_field(env, curve, "x")?;
    let y = float_array_field(env, curve, "y")?;
    if x.len() != y.len() || x.len() < 2 {
        return None;
    }
    let mut x_knots = Vec::with_capacity(x.len() + 2);
    let mut y_knots = Vec::with_capacity(y.len() + 2);
    x_knots.push(x[0]);
    y_knots.push(y[0]);
    x_knots.extend_from_slice(&x);
    y_knots.extend_from_slice(&y);
    x_knots.push(*x.last()?);
    y_knots.push(*y.last()?);
    let mut table = vec![0_u8; 256];
    for index in 0..1024 {
        let position = index as f32 / 1024.0;
        let table_x = clamp((255.0 * spline(position, &x_knots) + 0.5) as i32) as usize;
        table[table_x] = clamp((255.0 * spline(position, &y_knots) + 0.5) as i32);
    }
    Some(table)
}

fn rgb_to_hsv(color: u32) -> (f32, f32, f32) {
    let r = red(color) as f32 / 255.0;
    let g = green(color) as f32 / 255.0;
    let b = blue(color) as f32 / 255.0;
    let max = r.max(g).max(b);
    let min = r.min(g).min(b);
    let delta = max - min;
    let mut hue = if delta == 0.0 {
        0.0
    } else if max == r {
        60.0 * ((g - b) / delta).rem_euclid(6.0)
    } else if max == g {
        60.0 * ((b - r) / delta + 2.0)
    } else {
        60.0 * ((r - g) / delta + 4.0)
    };
    if hue < 0.0 {
        hue += 360.0;
    }
    let saturation = if max == 0.0 { 0.0 } else { delta / max };
    (hue, saturation, max)
}

fn hsv_to_rgb(hue: f32, saturation: f32, value: f32) -> (u8, u8, u8) {
    let chroma = value * saturation;
    let h = hue.rem_euclid(360.0) / 60.0;
    let x = chroma * (1.0 - (h.rem_euclid(2.0) - 1.0).abs());
    let (r, g, b) = match h as i32 {
        0 => (chroma, x, 0.0),
        1 => (x, chroma, 0.0),
        2 => (0.0, chroma, x),
        3 => (0.0, x, chroma),
        4 => (x, 0.0, chroma),
        _ => (chroma, 0.0, x),
    };
    let m = value - chroma;
    (
        clamp(((r + m) * 255.0 + 0.5) as i32),
        clamp(((g + m) * 255.0 + 0.5) as i32),
        clamp(((b + m) * 255.0 + 0.5) as i32),
    )
}

fn transfer(pixels: &mut [u32], function: impl Fn(f32) -> f32) {
    let table: Vec<u8> = (0..256)
        .map(|value| clamp((255.0 * function(value as f32 / 255.0)) as i32))
        .collect();
    for color in pixels {
        *color = argb(
            alpha(*color),
            table[red(*color) as usize],
            table[green(*color) as usize],
            table[blue(*color) as usize],
        );
    }
}

fn point_filter(env: &mut JNIEnv, filter: &JObject, name: &str, pixels: &mut [u32]) -> bool {
    match name {
        "InvertFilter" => {
            for color in pixels {
                *color = (*color & 0xff00_0000) | (!*color & 0x00ff_ffff);
            }
        }
        "InvertAlphaFilter" => {
            for color in pixels {
                *color = (*color & 0x00ff_ffff) | ((255 - alpha(*color) as u32) << 24);
            }
        }
        "FillFilter" => {
            let color = int_param(env, filter, "getFillColor", 0xff000000u32 as i32) as u32;
            pixels.fill(color);
        }
        "OpacityFilter" => {
            let opacity = int_param(env, filter, "getOpacity", 0x88) as u8;
            for color in pixels {
                if alpha(*color) != 0 {
                    *color = argb(opacity, red(*color), green(*color), blue(*color));
                }
            }
        }
        "GrayFilter" => {
            for color in pixels {
                *color = argb(
                    alpha(*color),
                    ((red(*color) as u16 + 255) / 2) as u8,
                    ((green(*color) as u16 + 255) / 2) as u8,
                    ((blue(*color) as u16 + 255) / 2) as u8,
                );
            }
        }
        "GrayscaleFilter" => {
            for color in pixels {
                let gray = ((red(*color) as i32 * 77
                    + green(*color) as i32 * 151
                    + blue(*color) as i32 * 28)
                    >> 8) as u8;
                *color = argb(alpha(*color), gray, gray, gray);
            }
        }
        "ExposureFilter" => {
            let exposure = float_param(env, filter, "getExposure", 1.0);
            transfer(pixels, |value| 1.0 - (-value * exposure).exp());
        }
        "ContrastFilter" => {
            let brightness = float_param(env, filter, "getBrightness", 1.0);
            let contrast = float_param(env, filter, "getContrast", 0.5);
            transfer(pixels, |value| (value * brightness - 0.5) * contrast + 0.5);
        }
        "TemperatureFilter" => {
            let kelvin = float_param(env, filter, "getTemperature", 6650.0).clamp(1000.0, 10000.0);
            let temperature = kelvin / 100.0;
            let black_body = if temperature <= 66.0 {
                (
                    255.0,
                    99.470_8 * temperature.ln() - 161.119_57,
                    if temperature <= 19.0 {
                        0.0
                    } else {
                        138.517_73 * (temperature - 10.0).ln() - 305.044_8
                    },
                )
            } else {
                (
                    329.698_73 * (temperature - 60.0).powf(-0.133_204_76),
                    288.122_16 * (temperature - 60.0).powf(-0.075_514_846),
                    255.0,
                )
            };
            let factors = [
                255.0 / black_body.0.clamp(1.0, 255.0),
                255.0 / black_body.1.clamp(1.0, 255.0),
                255.0 / black_body.2.clamp(1.0, 255.0),
            ];
            let maximum = factors[0].max(factors[1]).max(factors[2]);
            for color in pixels {
                *color = argb(
                    alpha(*color),
                    clamp((red(*color) as f32 * factors[0] / maximum) as i32),
                    clamp((green(*color) as f32 * factors[1] / maximum) as i32),
                    clamp((blue(*color) as f32 * factors[2] / maximum) as i32),
                );
            }
        }
        "RescaleFilter" => {
            let scale = float_param(env, filter, "getScale", 1.0);
            transfer(pixels, |value| value * scale);
        }
        "SolarizeFilter" => transfer(pixels, |value| {
            if value > 0.5 {
                2.0 * (value - 0.5)
            } else {
                2.0 * (0.5 - value)
            }
        }),
        "GammaFilter" => {
            let red_gamma = float_field(env, filter, "rGamma", 1.0);
            let green_gamma = float_field(env, filter, "gGamma", red_gamma);
            let blue_gamma = float_field(env, filter, "bGamma", red_gamma);
            let table = |gamma: f32| {
                (0..256)
                    .map(|value| {
                        clamp(
                            (255.0 * (value as f64 / 255.0).powf(1.0 / gamma as f64) + 0.5) as i32,
                        )
                    })
                    .collect::<Vec<_>>()
            };
            let red_table = table(red_gamma);
            let green_table = if green_gamma == red_gamma {
                red_table.clone()
            } else {
                table(green_gamma)
            };
            let blue_table = if blue_gamma == red_gamma {
                red_table.clone()
            } else if blue_gamma == green_gamma {
                green_table.clone()
            } else {
                table(blue_gamma)
            };
            for color in pixels {
                *color = argb(
                    alpha(*color),
                    red_table[red(*color) as usize],
                    green_table[green(*color) as usize],
                    blue_table[blue(*color) as usize],
                );
            }
        }
        "GainFilter" => {
            let gain = float_param(env, filter, "getGain", 0.5);
            let bias = float_param(env, filter, "getBias", 0.5);
            transfer(pixels, |value| {
                let c = (1.0 / gain - 2.0) * (1.0 - 2.0 * value);
                let gained = if value < 0.5 {
                    value / (c + 1.0)
                } else {
                    (c - value) / (c - 1.0)
                };
                gained / (((1.0 / bias - 2.0) * (1.0 - gained)) + 1.0)
            });
        }
        "PosterizeFilter" | "ReduceFilter" => {
            let getter = if name == "PosterizeFilter" {
                "getNumLevels"
            } else {
                "getNumLevels"
            };
            let levels = int_param(env, filter, getter, 6).max(2);
            transfer(pixels, |value| {
                (((value * 255.0) as i32 * levels / 256) * 255 / (levels - 1)) as f32 / 255.0
            });
        }
        "RGBAdjustFilter" => {
            let rf = 1.0 + float_param(env, filter, "getRFactor", 0.0);
            let gf = 1.0 + float_param(env, filter, "getGFactor", 0.0);
            let bf = 1.0 + float_param(env, filter, "getBFactor", 0.0);
            for color in pixels {
                *color = argb(
                    alpha(*color),
                    clamp((red(*color) as f32 * rf) as i32),
                    clamp((green(*color) as f32 * gf) as i32),
                    clamp((blue(*color) as f32 * bf) as i32),
                );
            }
        }
        "HSBAdjustFilter" => {
            let hf = float_param(env, filter, "getHFactor", 0.0);
            let sf = float_param(env, filter, "getSFactor", 0.0);
            let bf = float_param(env, filter, "getBFactor", 0.0);
            for color in pixels {
                let (h, s, v) = rgb_to_hsv(*color);
                let (r, g, b) =
                    hsv_to_rgb(h + hf, (s + sf).clamp(0.0, 1.0), (v + bf).clamp(0.0, 1.0));
                *color = argb(alpha(*color), r, g, b);
            }
        }
        "ThresholdFilter" => {
            let lower = int_param(env, filter, "getLowerThreshold", 127) as f32;
            let upper = int_param(env, filter, "getUpperThreshold", 127) as f32;
            let white = int_param(env, filter, "getWhite", 0x00ffffff) as u32;
            let black = int_param(env, filter, "getBlack", 0) as u32;
            for color in pixels {
                let mixed = mix_colors(
                    smooth_step(lower, upper, brightness(*color) as f32),
                    black,
                    white,
                );
                *color = (*color & 0xff00_0000) | (mixed & 0x00ff_ffff);
            }
        }
        "TritoneFilter" => {
            let shadow = int_param(env, filter, "getShadowColor", 0xff000000u32 as i32) as u32;
            let middle = int_param(env, filter, "getMidColor", 0xff888888u32 as i32) as u32;
            let high = int_param(env, filter, "getHighColor", 0xffffffffu32 as i32) as u32;
            for color in pixels {
                let value = brightness(*color);
                *color = if value < 128 {
                    mix_colors(value as f32 / 127.0, shadow, middle)
                } else {
                    mix_colors((value - 127) as f32 / 128.0, middle, high)
                };
            }
        }
        "ChannelMixFilter" => {
            let bg = int_param(env, filter, "getBlueGreen", 0);
            let rb = int_param(env, filter, "getRedBlue", 0);
            let gr = int_param(env, filter, "getGreenRed", 0);
            let into_r = int_param(env, filter, "getIntoR", 0);
            let into_g = int_param(env, filter, "getIntoG", 0);
            let into_b = int_param(env, filter, "getIntoB", 0);
            for color in pixels {
                let r = red(*color) as i32;
                let g = green(*color) as i32;
                let b = blue(*color) as i32;
                let nr = (into_r * (bg * g + (255 - bg) * b) / 255 + (255 - into_r) * r) / 255;
                let ng = (into_g * (rb * b + (255 - rb) * r) / 255 + (255 - into_g) * g) / 255;
                let nb = (into_b * (gr * r + (255 - gr) * g) / 255 + (255 - into_b) * b) / 255;
                *color = argb(alpha(*color), clamp(nr), clamp(ng), clamp(nb));
            }
        }
        "MaskFilter" => {
            let mask = int_param(env, filter, "getMask", 0xff00ffffu32 as i32) as u32;
            for color in pixels {
                *color &= mask;
            }
        }
        "MapColorsFilter" => {
            let old = int_field(env, filter, "oldColor", 0) as u32;
            let new = int_field(env, filter, "newColor", 0) as u32;
            for color in pixels {
                if *color == old {
                    *color = new;
                }
            }
        }
        _ => return false,
    }
    true
}

fn sample(pixels: &[u32], width: usize, height: usize, x: isize, y: isize, edge: i32) -> u32 {
    if x >= 0 && y >= 0 && x < width as isize && y < height as isize {
        return pixels[y as usize * width + x as usize];
    }
    match edge {
        2 => {
            let x = x.rem_euclid(width as isize) as usize;
            let y = y.rem_euclid(height as isize) as usize;
            pixels[y * width + x]
        }
        1 | 3 => {
            let x = x.clamp(0, width as isize - 1) as usize;
            let y = y.clamp(0, height as isize - 1) as usize;
            let color = pixels[y * width + x];
            if edge == 3 {
                color & 0x00ff_ffff
            } else {
                color
            }
        }
        _ => 0,
    }
}

fn bilinear(pixels: &[u32], width: usize, height: usize, x: f32, y: f32, edge: i32) -> u32 {
    let x0 = x.floor() as isize;
    let y0 = y.floor() as isize;
    let x_weight = x - x.floor();
    let y_weight = y - y.floor();
    let colors = [
        sample(pixels, width, height, x0, y0, edge),
        sample(pixels, width, height, x0 + 1, y0, edge),
        sample(pixels, width, height, x0, y0 + 1, edge),
        sample(pixels, width, height, x0 + 1, y0 + 1, edge),
    ];
    let channel = |get: fn(u32) -> u8| {
        let north = get(colors[0]) as f32 * (1.0 - x_weight) + get(colors[1]) as f32 * x_weight;
        let south = get(colors[2]) as f32 * (1.0 - x_weight) + get(colors[3]) as f32 * x_weight;
        clamp((north * (1.0 - y_weight) + south * y_weight) as i32)
    };
    argb(channel(alpha), channel(red), channel(green), channel(blue))
}

fn transform_filter(
    env: &mut JNIEnv,
    filter: &JObject,
    name: &str,
    source: &[u32],
    width: usize,
    height: usize,
) -> Option<Vec<u32>> {
    if !matches!(
        name,
        "OffsetFilter"
            | "TwirlFilter"
            | "WaterFilter"
            | "PinchFilter"
            | "RippleFilter"
            | "RotateFilter"
            | "DiffuseFilter"
            | "KaleidoscopeFilter"
            | "PolarFilter"
            | "SphereLensDistortionFilter"
            | "SphereFilter"
            | "ShearFilter"
            | "ArcFilter"
            | "PerspectiveFilter"
            | "MarbleFilter"
            | "SwimFilter"
    ) {
        return None;
    }
    let edge = int_param(env, filter, "getEdgeAction", 3);
    let interpolation = int_param(env, filter, "getInterpolation", 1);
    let mut output = vec![0; source.len()];
    let center_x = float_param(env, filter, "getCentreX", 0.5) * width as f32;
    let center_y = float_param(env, filter, "getCentreY", 0.5) * height as f32;
    let default_radius = center_x.min(center_y);
    for y in 0..height {
        for x in 0..width {
            let xf = x as f32;
            let yf = y as f32;
            let (sx, sy) = match name {
                "OffsetFilter" => {
                    let dx = int_param(env, filter, "getXOffset", 0) as f32;
                    let dy = int_param(env, filter, "getYOffset", 0) as f32;
                    if bool_param(env, filter, "getWrap", true) {
                        (
                            (xf + width as f32 - dx).rem_euclid(width as f32),
                            (yf + height as f32 - dy).rem_euclid(height as f32),
                        )
                    } else {
                        (xf - dx, yf - dy)
                    }
                }
                "TwirlFilter" => {
                    let dx = xf - center_x;
                    let dy = yf - center_y;
                    let distance = (dx * dx + dy * dy).sqrt();
                    let radius = float_param(env, filter, "getRadius", 0.0);
                    let radius = if radius == 0.0 {
                        default_radius
                    } else {
                        radius
                    };
                    if distance > radius {
                        (xf, yf)
                    } else {
                        let angle = float_param(env, filter, "getAngle", 0.0) * (radius - distance)
                            / radius;
                        let sine = angle.sin();
                        let cosine = angle.cos();
                        (
                            center_x + cosine * dx - sine * dy,
                            center_y + sine * dx + cosine * dy,
                        )
                    }
                }
                "WaterFilter" => {
                    let dx = xf - center_x;
                    let dy = yf - center_y;
                    let distance = (dx * dx + dy * dy).sqrt();
                    let radius = float_param(env, filter, "getRadius", 0.0);
                    let radius = if radius == 0.0 {
                        default_radius
                    } else {
                        radius
                    };
                    if distance > radius || distance == 0.0 {
                        (xf, yf)
                    } else {
                        let wavelength = float_param(env, filter, "getWavelength", 16.0);
                        let amplitude = float_param(env, filter, "getAmplitude", 10.0);
                        let phase = float_param(env, filter, "getPhase", 0.0);
                        let amount = amplitude
                            * (distance / wavelength * std::f32::consts::TAU - phase).sin()
                            * (radius - distance)
                            / radius
                            / distance;
                        (xf + dx * amount * wavelength, yf + dy * amount * wavelength)
                    }
                }
                "PinchFilter" => {
                    let dx = xf - center_x;
                    let dy = yf - center_y;
                    let distance2 = dx * dx + dy * dy;
                    let radius = float_param(env, filter, "getRadius", 0.0);
                    let radius = if radius == 0.0 {
                        default_radius
                    } else {
                        radius
                    };
                    if distance2 > radius * radius || distance2 == 0.0 {
                        (xf, yf)
                    } else {
                        let distance = distance2.sqrt();
                        let amount = float_param(env, filter, "getAmount", 0.5);
                        let pinch = (std::f32::consts::FRAC_PI_2 * distance / radius)
                            .sin()
                            .powf(-amount);
                        let angle = float_param(env, filter, "getAngle", 0.0)
                            * (1.0 - distance / radius).powi(2);
                        let sine = angle.sin();
                        let cosine = angle.cos();
                        (
                            center_x + pinch * (cosine * dx - sine * dy),
                            center_y + pinch * (sine * dx + cosine * dy),
                        )
                    }
                }
                "RippleFilter" => {
                    let xa = float_param(env, filter, "getXAmplitude", 5.0);
                    let xw = float_param(env, filter, "getXWavelength", 16.0);
                    let ya = float_param(env, filter, "getYAmplitude", 0.0);
                    let yw = float_param(env, filter, "getYWavelength", 16.0);
                    let wave_type = int_param(env, filter, "getWaveType", 0);
                    let wave = |value: f32| match wave_type {
                        1 => value.rem_euclid(1.0),
                        2 => triangle(value),
                        3 => perlin_noise().noise1(value),
                        _ => value.sin(),
                    };
                    let output_x = if edge == 0 { xf - xa as i32 as f32 } else { xf };
                    let output_y = if edge == 0 { yf - ya as i32 as f32 } else { yf };
                    (
                        output_x + wave(output_y / xw) * xa,
                        output_y + wave(output_x / yw) * ya,
                    )
                }
                "RotateFilter" => {
                    let angle = float_param(env, filter, "getAngle", 0.0);
                    let sine = angle.sin();
                    let cosine = angle.cos();
                    let corners = [
                        (0.0_f32, 0.0_f32),
                        (width as f32, 0.0_f32),
                        (0.0_f32, height as f32),
                        (width as f32, height as f32),
                    ];
                    let resize = bool_field(env, filter, "resize", true);
                    let minimum_x = if resize {
                        corners
                            .iter()
                            .map(|(x, y)| (x * cosine + y * sine) as i32)
                            .min()
                            .unwrap_or(0) as f32
                    } else {
                        0.0
                    };
                    let minimum_y = if resize {
                        corners
                            .iter()
                            .map(|(x, y)| (y * cosine - x * sine) as i32)
                            .min()
                            .unwrap_or(0) as f32
                    } else {
                        0.0
                    };
                    let output_x = xf + minimum_x;
                    let output_y = yf + minimum_y;
                    (
                        output_x * cosine - output_y * sine,
                        output_y * cosine + output_x * sine,
                    )
                }
                "DiffuseFilter" => {
                    let scale = float_param(env, filter, "getScale", 4.0);
                    let hash = ((x as u64).wrapping_mul(0x9e3779b1)
                        ^ (y as u64).wrapping_mul(0x85ebca77))
                        as u32;
                    let angle = (hash & 255) as f32 / 256.0 * std::f32::consts::TAU;
                    let distance = ((hash >> 8) & 0xffff) as f32 / 65535.0 * scale;
                    (xf + distance * angle.sin(), yf + distance * angle.cos())
                }
                "KaleidoscopeFilter" => {
                    let dx = (xf - center_x) as f64;
                    let dy = (yf - center_y) as f64;
                    let mut radius = (dx * dx + dy * dy).sqrt();
                    let angle = float_param(env, filter, "getAngle", 0.0) as f64;
                    let angle2 = float_param(env, filter, "getAngle2", 0.0) as f64;
                    let sides = int_param(env, filter, "getSides", 3) as f64;
                    let mut theta = triangle(
                        ((dy.atan2(dx) - angle - angle2) / std::f64::consts::PI * sides * 0.5)
                            as f32,
                    ) as f64;
                    let effect_radius = float_param(env, filter, "getRadius", 0.0) as f64;
                    if effect_radius != 0.0 {
                        let radius_cosine = effect_radius / theta.cos();
                        radius = radius_cosine * triangle((radius / radius_cosine) as f32) as f64;
                    }
                    theta += angle;
                    (
                        center_x + (radius * theta.cos()) as f32,
                        center_y + (radius * theta.sin()) as f32,
                    )
                }
                "PolarFilter" => {
                    let kind = int_param(env, filter, "getType", 0);
                    let dx = xf - width as f32 / 2.0;
                    let dy = yf - height as f32 / 2.0;
                    let radius = width.max(height) as f32 / 2.0;
                    match kind {
                        0 => {
                            let theta = dx.atan2(-dy).rem_euclid(std::f32::consts::TAU);
                            (
                                width as f32
                                    - 1.0
                                    - (width as f32 - 1.0) * theta / std::f32::consts::TAU,
                                height as f32 * (dx * dx + dy * dy).sqrt() / radius,
                            )
                        }
                        1 => {
                            let theta = xf / width as f32 * std::f32::consts::TAU;
                            let distance = radius * yf / height as f32;
                            (
                                width as f32 / 2.0 - distance * theta.sin(),
                                height as f32 / 2.0 - distance * theta.cos(),
                            )
                        }
                        _ => {
                            let distance2 = (dx * dx + dy * dy).max(0.0001);
                            (
                                width as f32 / 2.0
                                    + width as f32 * width as f32 / 4.0 * dx / distance2,
                                height as f32 / 2.0
                                    + height as f32 * height as f32 / 4.0 * dy / distance2,
                            )
                        }
                    }
                }
                "SphereLensDistortionFilter" | "SphereFilter" => {
                    let radius = float_param(env, filter, "getRadius", 100.0).max(0.001);
                    let dx = xf - center_x;
                    let dy = yf - center_y;
                    let x2 = dx * dx;
                    let y2 = dy * dy;
                    if y2 >= radius * radius - x2 {
                        (xf, yf)
                    } else {
                        let refraction = float_param(env, filter, "getRefractionIndex", 1.5);
                        let z = ((1.0 - x2 / (radius * radius) - y2 / (radius * radius))
                            * radius
                            * radius)
                            .sqrt();
                        let x_angle = (dx / (x2 + z * z).sqrt()).acos();
                        let first_x = std::f32::consts::FRAC_PI_2 - x_angle;
                        let second_x = std::f32::consts::FRAC_PI_2
                            - x_angle
                            - (first_x.sin() / refraction).asin();
                        let y_angle = (dy / (y2 + z * z).sqrt()).acos();
                        let first_y = std::f32::consts::FRAC_PI_2 - y_angle;
                        let second_y = std::f32::consts::FRAC_PI_2
                            - y_angle
                            - (first_y.sin() / refraction).asin();
                        (xf - second_x.tan() * z, yf - second_y.tan() * z)
                    }
                }
                "ShearFilter" => {
                    let x_angle = float_param(env, filter, "getXAngle", 0.0);
                    let y_angle = float_param(env, filter, "getYAngle", 0.0);
                    let x_offset = -(height as f32) * x_angle.tan();
                    let transformed_width = height as f32 * x_angle.tan().abs() + width as f32;
                    let y_offset = -transformed_width * y_angle.tan();
                    (
                        xf + x_offset + yf * x_angle.sin(),
                        yf + y_offset + xf * y_angle.sin(),
                    )
                }
                "ArcFilter" => {
                    let dx = xf - center_x;
                    let dy = yf - center_y;
                    let angle = float_param(env, filter, "getAngle", 0.0);
                    let spread = float_param(env, filter, "getSpreadAngle", std::f32::consts::PI);
                    let radius = float_param(env, filter, "getRadius", 10.0);
                    let arc_height = float_param(env, filter, "getHeight", 20.0);
                    let theta = ((-dy).atan2(-dx) + angle).rem_euclid(std::f32::consts::TAU);
                    let distance = (dx * dx + dy * dy).sqrt();
                    (
                        (width as f32 - 1.0) * theta / (spread + 0.00001),
                        height as f32 * (1.0 - (distance - radius) / (arc_height + 0.00001)),
                    )
                }
                "PerspectiveFilter" => {
                    let a11 = float_field(env, filter, "a11", 1.0);
                    let a12 = float_field(env, filter, "a12", 0.0);
                    let a13 = float_field(env, filter, "a13", 0.0);
                    let a21 = float_field(env, filter, "a21", 0.0);
                    let a22 = float_field(env, filter, "a22", 1.0);
                    let a23 = float_field(env, filter, "a23", 0.0);
                    let a31 = float_field(env, filter, "a31", 0.0);
                    let a32 = float_field(env, filter, "a32", 0.0);
                    let a33 = float_field(env, filter, "a33", 1.0);
                    let mut a = a22 * a33 - a32 * a23;
                    let mut b = a31 * a23 - a21 * a33;
                    let c = a21 * a32 - a31 * a22;
                    let mut d = a32 * a13 - a12 * a33;
                    let mut e = a11 * a33 - a31 * a13;
                    let f = a31 * a12 - a11 * a32;
                    let mut g = a12 * a23 - a22 * a13;
                    let mut h = a21 * a13 - a11 * a23;
                    let i = a11 * a22 - a21 * a12;
                    if !bool_field(env, filter, "scaled", false) {
                        a /= width as f32;
                        d /= width as f32;
                        g /= width as f32;
                        b /= height as f32;
                        e /= height as f32;
                        h /= height as f32;
                    }
                    let scaled = bool_field(env, filter, "scaled", false);
                    let (offset_x, offset_y) = if scaled {
                        let xs = [
                            float_field(env, filter, "x0", 0.0),
                            float_field(env, filter, "x1", width as f32),
                            float_field(env, filter, "x2", width as f32),
                            float_field(env, filter, "x3", 0.0),
                        ];
                        let ys = [
                            float_field(env, filter, "y0", 0.0),
                            float_field(env, filter, "y1", 0.0),
                            float_field(env, filter, "y2", height as f32),
                            float_field(env, filter, "y3", height as f32),
                        ];
                        (
                            xs.into_iter().fold(f32::INFINITY, f32::min) as i32 as f32,
                            ys.into_iter().fold(f32::INFINITY, f32::min) as i32 as f32,
                        )
                    } else if !bool_param(env, filter, "getClip", false) {
                        (a31 / a33, a32 / a33)
                    } else {
                        (0.0, 0.0)
                    };
                    let output_x = xf + offset_x as i32 as f32;
                    let output_y = yf + offset_y as i32 as f32;
                    let denominator = g * output_x + h * output_y + i;
                    (
                        width as f32 * (a * output_x + b * output_y + c) / denominator,
                        height as f32 * (d * output_x + e * output_y + f) / denominator,
                    )
                }
                "MarbleFilter" => {
                    let x_scale = float_param(env, filter, "getXScale", 4.0);
                    let y_scale = float_param(env, filter, "getYScale", 4.0);
                    let turbulence = float_param(env, filter, "getTurbulence", 1.0);
                    let displacement = clamp(
                        (127.0 * (1.0 + perlin_noise().noise2(xf / x_scale, yf / x_scale))) as i32,
                    ) as f32;
                    let angle = std::f32::consts::TAU * displacement / 256.0 * turbulence;
                    (xf - y_scale * angle.sin(), yf + y_scale * angle.cos())
                }
                "SwimFilter" => {
                    let scale = float_param(env, filter, "getScale", 32.0);
                    let stretch = float_param(env, filter, "getStretch", 1.0);
                    let angle = float_param(env, filter, "getAngle", 0.0);
                    let amount = float_param(env, filter, "getAmount", 1.0);
                    let turbulence = float_param(env, filter, "getTurbulence", 1.0);
                    let time = float_param(env, filter, "getTime", 0.0);
                    let nx = (angle.cos() * xf + angle.sin() * yf) / scale;
                    let ny = (-angle.sin() * xf + angle.cos() * yf) / (scale * stretch);
                    let noise = perlin_noise();
                    if turbulence == 1.0 {
                        (
                            xf + amount * noise.noise3(nx + 0.5, ny, time),
                            yf + amount * noise.noise3(nx, ny + 0.5, time),
                        )
                    } else {
                        (
                            xf + amount * noise.turbulence3(nx + 0.5, ny, turbulence, time),
                            yf + amount * noise.turbulence3(nx, ny + 0.5, turbulence, time),
                        )
                    }
                }
                _ => return None,
            };
            output[y * width + x] = if interpolation == 0 {
                sample(source, width, height, sx as isize, sy as isize, edge)
            } else {
                bilinear(source, width, height, sx, sy, edge)
            };
        }
    }
    Some(output)
}

fn premultiply(pixels: &mut [u32]) {
    for color in pixels {
        let a = alpha(*color);
        if a != 255 {
            let factor = a as f32 / 255.0;
            *color = argb(
                a,
                (red(*color) as f32 * factor) as u8,
                (green(*color) as f32 * factor) as u8,
                (blue(*color) as f32 * factor) as u8,
            );
        }
    }
}

fn unpremultiply(pixels: &mut [u32]) {
    for color in pixels {
        let a = alpha(*color);
        if a != 0 && a != 255 {
            let factor = 255.0 / a as f32;
            *color = argb(
                a,
                clamp((red(*color) as f32 * factor) as i32),
                clamp((green(*color) as f32 * factor) as i32),
                clamp((blue(*color) as f32 * factor) as i32),
            );
        }
    }
}

struct KernelData {
    width: usize,
    height: usize,
    values: Vec<f32>,
}

fn kernel_param(env: &mut JNIEnv, filter: &JObject) -> Option<KernelData> {
    let kernel = env
        .call_method(filter, "getKernel", "()Lcom/jhlabs/Kernel;", &[])
        .ok()?
        .l()
        .ok()?;
    if kernel.is_null() {
        return None;
    }
    let width = env
        .call_method(&kernel, "getWidth", "()I", &[])
        .ok()?
        .i()
        .ok()? as usize;
    let height = env
        .call_method(&kernel, "getHeight", "()I", &[])
        .ok()?
        .i()
        .ok()? as usize;
    let values = float_array_field(env, &kernel, "mMatrix")?;
    Some(KernelData {
        width,
        height,
        values,
    })
}

fn convolve_exact(
    source: &[u32],
    width: usize,
    height: usize,
    kernel: &KernelData,
    use_alpha: bool,
    edge_action: i32,
) -> Vec<u32> {
    let mut output = vec![0; source.len()];
    let half_width = kernel.width / 2;
    let half_height = kernel.height / 2;
    for y in 0..height {
        for x in 0..width {
            let mut a = 0.0_f32;
            let mut r = 0.0_f32;
            let mut g = 0.0_f32;
            let mut b = 0.0_f32;
            for kernel_y in 0..kernel.height {
                let offset_y = kernel_y as isize - half_height as isize;
                let source_y = y as isize + offset_y;
                let row = if source_y >= 0 && source_y < height as isize {
                    source_y as usize
                } else if edge_action == 1 {
                    if kernel.width == 1 {
                        source_y.clamp(0, height as isize - 1) as usize
                    } else {
                        y
                    }
                } else if edge_action == 2 {
                    source_y.rem_euclid(height as isize) as usize
                } else {
                    continue;
                };
                for kernel_x in 0..kernel.width {
                    let weight = kernel.values[kernel_y * kernel.width + kernel_x];
                    if weight == 0.0 {
                        continue;
                    }
                    let offset_x = kernel_x as isize - half_width as isize;
                    let source_x = x as isize + offset_x;
                    let column = if source_x >= 0 && source_x < width as isize {
                        source_x as usize
                    } else if edge_action == 1 {
                        if kernel.height == 1 {
                            source_x.clamp(0, width as isize - 1) as usize
                        } else {
                            x
                        }
                    } else if edge_action == 2 {
                        source_x.rem_euclid(width as isize) as usize
                    } else {
                        continue;
                    };
                    let color = source[row * width + column];
                    a += weight * alpha(color) as f32;
                    r += weight * red(color) as f32;
                    g += weight * green(color) as f32;
                    b += weight * blue(color) as f32;
                }
            }
            output[y * width + x] = argb(
                if use_alpha {
                    clamp((a + 0.5) as i32)
                } else {
                    255
                },
                clamp((r + 0.5) as i32),
                clamp((g + 0.5) as i32),
                clamp((b + 0.5) as i32),
            );
        }
    }
    output
}

fn gaussian_kernel_exact(radius: f32) -> KernelData {
    let half = radius.ceil() as i32;
    let sigma = radius / 3.0;
    let sigma22 = 2.0 * sigma * sigma;
    let sqrt_sigma_pi2 = (2.0 * std::f32::consts::PI * sigma).sqrt();
    let radius2 = radius * radius;
    let mut values = Vec::with_capacity((half * 2 + 1) as usize);
    let mut total = 0.0;
    for row in -half..=half {
        let distance = (row * row) as f32;
        let value = if distance > radius2 {
            0.0
        } else {
            (-distance / sigma22).exp() / sqrt_sigma_pi2
        };
        values.push(value);
        total += value;
    }
    for value in &mut values {
        *value /= total;
    }
    KernelData {
        width: values.len(),
        height: 1,
        values,
    }
}

fn convolve_and_transpose_exact(
    source: &[u32],
    width: usize,
    height: usize,
    kernel: &KernelData,
    use_alpha: bool,
    premultiply_alpha: bool,
    unpremultiply_alpha: bool,
) -> Vec<u32> {
    let half = kernel.width / 2;
    let mut output = vec![0; source.len()];
    for y in 0..height {
        let row = y * width;
        for x in 0..width {
            let mut a = 0.0_f32;
            let mut r = 0.0_f32;
            let mut g = 0.0_f32;
            let mut b = 0.0_f32;
            for kernel_x in 0..kernel.width {
                let source_x = (x as isize + kernel_x as isize - half as isize)
                    .clamp(0, width as isize - 1) as usize;
                let color = source[row + source_x];
                let weight = kernel.values[kernel_x];
                let pa = alpha(color) as f32;
                let mut pr = red(color) as f32;
                let mut pg = green(color) as f32;
                let mut pb = blue(color) as f32;
                if premultiply_alpha {
                    let factor = pa / 255.0;
                    pr = (pr * factor) as i32 as f32;
                    pg = (pg * factor) as i32 as f32;
                    pb = (pb * factor) as i32 as f32;
                }
                a += weight * pa;
                r += weight * pr;
                g += weight * pg;
                b += weight * pb;
            }
            if unpremultiply_alpha && a != 0.0 && a != 255.0 {
                let factor = 255.0 / a;
                r *= factor;
                g *= factor;
                b *= factor;
            }
            output[x * height + y] = argb(
                if use_alpha {
                    clamp((a + 0.5) as i32)
                } else {
                    255
                },
                clamp((r + 0.5) as i32),
                clamp((g + 0.5) as i32),
                clamp((b + 0.5) as i32),
            );
        }
    }
    output
}

fn gaussian_blur_exact(
    source: &[u32],
    width: usize,
    height: usize,
    radius: f32,
    use_alpha: bool,
    premultiply_alpha: bool,
) -> Vec<u32> {
    if radius <= 0.0 {
        return source.to_vec();
    }
    let kernel = gaussian_kernel_exact(radius);
    let first = convolve_and_transpose_exact(
        source,
        width,
        height,
        &kernel,
        use_alpha,
        use_alpha && premultiply_alpha,
        false,
    );
    convolve_and_transpose_exact(
        &first,
        height,
        width,
        &kernel,
        use_alpha,
        false,
        use_alpha && premultiply_alpha,
    )
}

fn box_blur_transpose_exact(input: &[u32], width: usize, height: usize, radius: f32) -> Vec<u32> {
    let radius = radius as usize;
    let table_size = 2 * radius + 1;
    let divide = (0..256 * table_size)
        .map(|value| value / table_size)
        .collect::<Vec<_>>();
    let mut output = vec![0; input.len()];
    let mut input_index = 0;
    for y in 0..height {
        let mut output_index = y;
        let mut totals = [0_usize; 4];
        for offset in -(radius as isize)..=radius as isize {
            let color = input[input_index + offset.clamp(0, width as isize - 1) as usize];
            totals[0] += alpha(color) as usize;
            totals[1] += red(color) as usize;
            totals[2] += green(color) as usize;
            totals[3] += blue(color) as usize;
        }
        for x in 0..width {
            output[output_index] = argb(
                divide[totals[0]] as u8,
                divide[totals[1]] as u8,
                divide[totals[2]] as u8,
                divide[totals[3]] as u8,
            );
            let add = (x + radius + 1).min(width - 1);
            let remove = x.saturating_sub(radius);
            let added = input[input_index + add];
            let removed = input[input_index + remove];
            totals[0] = (totals[0] as i32 + alpha(added) as i32 - alpha(removed) as i32) as usize;
            totals[1] = (totals[1] as i32 + red(added) as i32 - red(removed) as i32) as usize;
            totals[2] = (totals[2] as i32 + green(added) as i32 - green(removed) as i32) as usize;
            totals[3] = (totals[3] as i32 + blue(added) as i32 - blue(removed) as i32) as usize;
            output_index += height;
        }
        input_index += width;
    }
    output
}

fn box_blur_fractional_transpose_exact(
    input: &[u32],
    width: usize,
    height: usize,
    radius: f32,
) -> Vec<u32> {
    let fraction = radius - radius as i32 as f32;
    let factor = 1.0 / (1.0 + 2.0 * fraction);
    let mut output = vec![0; input.len()];
    let mut input_index = 0;
    for y in 0..height {
        let mut output_index = y;
        output[output_index] = input[0];
        output_index += height;
        for x in 1..width.saturating_sub(1) {
            let first = input[input_index + x - 1];
            let second = input[input_index + x];
            let third = input[input_index + x + 1];
            let channel = |get: fn(u32) -> u8| {
                let value = get(second) as i32
                    + ((get(first) as i32 + get(third) as i32) as f32 * fraction) as i32;
                (value as f32 * factor) as u8
            };
            output[output_index] =
                argb(channel(alpha), channel(red), channel(green), channel(blue));
            output_index += height;
        }
        if width > 1 {
            output[output_index] = input[width - 1];
        }
        input_index += width;
    }
    output
}

fn box_blur_exact(
    source: &[u32],
    width: usize,
    height: usize,
    horizontal_radius: f32,
    vertical_radius: f32,
    iterations: i32,
    premultiply_alpha: bool,
) -> Vec<u32> {
    let mut input = source.to_vec();
    if premultiply_alpha {
        premultiply(&mut input);
    }
    for _ in 0..iterations {
        let output = box_blur_transpose_exact(&input, width, height, horizontal_radius);
        input = box_blur_transpose_exact(&output, height, width, vertical_radius);
    }
    let output = box_blur_fractional_transpose_exact(&input, width, height, horizontal_radius);
    input = box_blur_fractional_transpose_exact(&output, height, width, vertical_radius);
    if premultiply_alpha {
        unpremultiply(&mut input);
    }
    input
}

fn threshold_blur_transpose_exact(
    input: &[u32],
    width: usize,
    height: usize,
    kernel: &KernelData,
    threshold: i32,
) -> Vec<u32> {
    let mut output = vec![0; input.len()];
    let half = kernel.width / 2;
    for y in 0..height {
        let row = y * width;
        for x in 0..width {
            let center = input[row + x];
            let centers = [alpha(center), red(center), green(center), blue(center)];
            let mut totals = [0.0_f32; 4];
            let mut weights = [0.0_f32; 4];
            for kernel_x in 0..kernel.width {
                let offset = kernel_x as isize - half as isize;
                let source_x = x as isize + offset;
                let source_x = if source_x < 0 || source_x >= width as isize {
                    x
                } else {
                    source_x as usize
                };
                let color = input[row + source_x];
                let channels = [alpha(color), red(color), green(color), blue(color)];
                let weight = kernel.values[kernel_x];
                for channel in 0..4 {
                    let difference = centers[channel] as i32 - channels[channel] as i32;
                    if difference >= -threshold && difference <= threshold {
                        totals[channel] += weight * channels[channel] as f32;
                        weights[channel] += weight;
                    }
                }
            }
            let result = |channel: usize| {
                if weights[channel] == 0.0 {
                    centers[channel]
                } else {
                    clamp((totals[channel] / weights[channel] + 0.5) as i32)
                }
            };
            output[x * height + y] = argb(result(0), result(1), result(2), result(3));
        }
    }
    output
}

fn edge_filter_exact(
    source: &[u32],
    width: usize,
    height: usize,
    horizontal: &[f32],
    vertical: &[f32],
) -> Vec<u32> {
    let mut output = vec![0; source.len()];
    for y in 0..height {
        for x in 0..width {
            let mut horizontal_channels = [0_i32; 3];
            let mut vertical_channels = [0_i32; 3];
            for row in -1_isize..=1 {
                let source_y = y as isize + row;
                let source_y = if source_y < 0 || source_y >= height as isize {
                    y
                } else {
                    source_y as usize
                };
                for column in -1_isize..=1 {
                    let source_x = x as isize + column;
                    let source_x = if source_x < 0 || source_x >= width as isize {
                        x
                    } else {
                        source_x as usize
                    };
                    let color = source[source_y * width + source_x];
                    let index = ((row + 1) * 3 + column + 1) as usize;
                    for (channel, value) in [red(color), green(color), blue(color)]
                        .into_iter()
                        .enumerate()
                    {
                        horizontal_channels[channel] += (horizontal[index] * value as f32) as i32;
                        vertical_channels[channel] += (vertical[index] * value as f32) as i32;
                    }
                }
            }
            let magnitude = |channel: usize| {
                clamp(
                    (((horizontal_channels[channel] * horizontal_channels[channel]
                        + vertical_channels[channel] * vertical_channels[channel])
                        as f64)
                        .sqrt()
                        / 1.8) as i32,
                )
            };
            output[y * width + x] = argb(
                alpha(source[y * width + x]),
                magnitude(0),
                magnitude(1),
                magnitude(2),
            );
        }
    }
    output
}

fn laplace_filter_exact(source: &[u32], width: usize, height: usize) -> Vec<u32> {
    let luminance = source
        .iter()
        .map(|color| brightness(*color))
        .collect::<Vec<_>>();
    let mut first_pass = vec![0_u32; source.len()];
    for y in 0..height {
        first_pass[y * width] = 0xff000000;
        first_pass[y * width + width - 1] = 0xff000000;
        for x in 1..width.saturating_sub(1) {
            let top_y = y.saturating_sub(1);
            let bottom_y = (y + 1).min(height - 1);
            let center = luminance[y * width + x];
            let neighbors = [
                luminance[y * width + x - 1],
                luminance[top_y * width + x],
                luminance[bottom_y * width + x],
                luminance[y * width + x + 1],
            ];
            let maximum = *neighbors.iter().max().unwrap();
            let minimum = *neighbors.iter().min().unwrap();
            let gradient = (0.5 * (maximum - center).max(center - minimum) as f32) as i32;
            let laplace = luminance[top_y * width + x - 1]
                + luminance[top_y * width + x]
                + luminance[top_y * width + x + 1]
                + luminance[y * width + x - 1]
                - 8 * center
                + luminance[y * width + x + 1]
                + luminance[bottom_y * width + x - 1]
                + luminance[bottom_y * width + x]
                + luminance[bottom_y * width + x + 1];
            first_pass[y * width + x] = if laplace > 0 {
                gradient as u32
            } else {
                (128 + gradient) as u32
            };
        }
    }
    let mut output = first_pass.clone();
    for y in 0..height {
        output[y * width] = 0xff000000;
        output[y * width + width - 1] = 0xff000000;
        for x in 1..width.saturating_sub(1) {
            let top_y = y.saturating_sub(1);
            let bottom_y = (y + 1).min(height - 1);
            let value = first_pass[y * width + x] as i32;
            let has_positive_neighbor = [
                first_pass[top_y * width + x - 1],
                first_pass[top_y * width + x],
                first_pass[top_y * width + x + 1],
                first_pass[y * width + x - 1],
                first_pass[y * width + x + 1],
                first_pass[bottom_y * width + x - 1],
                first_pass[bottom_y * width + x],
                first_pass[bottom_y * width + x + 1],
            ]
            .into_iter()
            .any(|neighbor| neighbor as i32 > 128);
            let result = if value <= 128 && has_positive_neighbor {
                if value >= 128 { value - 128 } else { value }
            } else {
                0
            } as u8;
            output[y * width + x] = argb(255, result, result, result);
        }
    }
    output
}

fn binary_morphology_exact(
    env: &mut JNIEnv,
    filter: &JObject,
    source: &[u32],
    width: usize,
    height: usize,
    erode: bool,
) -> Vec<u32> {
    let iterations = int_param(env, filter, "getIterations", 1);
    let threshold = int_param(env, filter, "getThreshold", 2);
    let new_color = int_param(
        env,
        filter,
        "getNewColor",
        if erode {
            0xffffffff_u32 as i32
        } else {
            0xff000000_u32 as i32
        },
    ) as u32;
    let mut input = source.to_vec();
    let mut output = vec![0; source.len()];
    for iteration in 0..iterations {
        let black = binary_mask(env, filter, &input);
        let replacement =
            colormap_color(env, filter, iteration as f32 / iterations as f32, new_color);
        for y in 0..height {
            for x in 0..width {
                let index = y * width + x;
                let selected = if erode { black[index] } else { !black[index] };
                if !selected {
                    output[index] = input[index];
                    continue;
                }
                let mut neighbors = 0;
                for dy in -1_isize..=1 {
                    let source_y = y as isize + dy;
                    if source_y < 0 || source_y >= height as isize {
                        continue;
                    }
                    for dx in -1_isize..=1 {
                        let source_x = x as isize + dx;
                        if (dx == 0 && dy == 0) || source_x < 0 || source_x >= width as isize {
                            continue;
                        }
                        let neighbor_black = black[source_y as usize * width + source_x as usize];
                        if (erode && !neighbor_black) || (!erode && neighbor_black) {
                            neighbors += 1;
                        }
                    }
                }
                output[index] = if neighbors >= threshold {
                    replacement
                } else {
                    input[index]
                };
            }
        }
        if iteration + 1 < iterations {
            std::mem::swap(&mut input, &mut output);
        }
    }
    output
}

fn outline_filter_exact(
    env: &mut JNIEnv,
    filter: &JObject,
    source: &[u32],
    width: usize,
    height: usize,
) -> Vec<u32> {
    let black = binary_mask(env, filter, source);
    let new_color = int_param(env, filter, "getNewColor", 0xffffffff_u32 as i32) as u32;
    let mut output = source.to_vec();
    for y in 0..height {
        for x in 0..width {
            let index = y * width + x;
            if !black[index] {
                continue;
            }
            let mut neighbors = 0;
            for dy in -1_isize..=1 {
                let source_y = y as isize + dy;
                if source_y < 0 || source_y >= height as isize {
                    continue;
                }
                for dx in -1_isize..=1 {
                    let source_x = x as isize + dx;
                    if (dx == 0 && dy == 0) || source_x < 0 || source_x >= width as isize {
                        neighbors += 1;
                    } else if black[source_y as usize * width + source_x as usize] {
                        neighbors += 1;
                    }
                }
            }
            if neighbors == 9 {
                output[index] = new_color;
            }
        }
    }
    output
}

fn reduce_noise_exact(source: &[u32], width: usize, height: usize) -> Vec<u32> {
    let mut output = vec![0; source.len()];
    for y in 0..height {
        for x in 0..width {
            let center = source[y * width + x];
            let mut channels = [[0_u8; 9]; 3];
            let mut index = 0;
            for dy in -1_isize..=1 {
                for dx in -1_isize..=1 {
                    let source_x = x as isize + dx;
                    let source_y = y as isize + dy;
                    let color = if source_x >= 0
                        && source_x < width as isize
                        && source_y >= 0
                        && source_y < height as isize
                    {
                        source[source_y as usize * width + source_x as usize]
                    } else {
                        center
                    };
                    channels[0][index] = red(color);
                    channels[1][index] = green(color);
                    channels[2][index] = blue(color);
                    index += 1;
                }
            }
            let smooth = |values: &[u8; 9]| {
                let minimum = values
                    .iter()
                    .enumerate()
                    .filter(|(index, _)| *index != 4)
                    .min_by_key(|(_, value)| *value)
                    .map(|(_, value)| *value)
                    .unwrap();
                let maximum = values
                    .iter()
                    .enumerate()
                    .filter(|(index, _)| *index != 4)
                    .max_by_key(|(_, value)| *value)
                    .map(|(_, value)| *value)
                    .unwrap();
                values[4].clamp(minimum, maximum)
            };
            output[y * width + x] = argb(
                alpha(center),
                smooth(&channels[0]),
                smooth(&channels[1]),
                smooth(&channels[2]),
            );
        }
    }
    output
}

fn despeckle_exact(source: &[u32], width: usize, height: usize) -> Vec<u32> {
    let adjust = |mut center: i16, first: i16, second: i16| {
        if center < first {
            center += 1;
        }
        if center < second {
            center += 1;
        }
        if center > first {
            center -= 1;
        }
        if center > second {
            center -= 1;
        }
        center
    };
    let mut output = source.to_vec();
    for y in 0..height {
        for x in 0..width {
            let center = source[y * width + x];
            let mut result = [
                red(center) as i16,
                green(center) as i16,
                blue(center) as i16,
            ];
            let inside_x = x > 0 && x + 1 < width;
            let inside_y = y > 0 && y + 1 < height;
            let pair = |first_x: usize, first_y: usize, second_x: usize, second_y: usize| {
                [
                    (
                        red(source[first_y * width + first_x]) as i16,
                        red(source[second_y * width + second_x]) as i16,
                    ),
                    (
                        green(source[first_y * width + first_x]) as i16,
                        green(source[second_y * width + second_x]) as i16,
                    ),
                    (
                        blue(source[first_y * width + first_x]) as i16,
                        blue(source[second_y * width + second_x]) as i16,
                    ),
                ]
            };
            if inside_y {
                for (channel, values) in pair(x, y - 1, x, y + 1).into_iter().enumerate() {
                    result[channel] = adjust(result[channel], values.0, values.1);
                }
            }
            if inside_x {
                for (channel, values) in pair(x - 1, y, x + 1, y).into_iter().enumerate() {
                    result[channel] = adjust(result[channel], values.0, values.1);
                }
            }
            if inside_x && inside_y {
                for (channel, values) in pair(x - 1, y - 1, x + 1, y + 1).into_iter().enumerate() {
                    result[channel] = adjust(result[channel], values.0, values.1);
                }
                for (channel, values) in pair(x - 1, y + 1, x + 1, y - 1).into_iter().enumerate() {
                    result[channel] = adjust(result[channel], values.0, values.1);
                }
            }
            output[y * width + x] = argb(
                alpha(center),
                result[0] as u8,
                result[1] as u8,
                result[2] as u8,
            );
        }
    }
    output
}

fn composite_normal(source: u32, destination: u32, extra_alpha: i32) -> u32 {
    let source_alpha = alpha(source) as i32 * extra_alpha / 255;
    let destination_alpha = alpha(destination) as i32;
    let remaining_alpha = (255 - source_alpha) * destination_alpha / 255;
    let channel = |get: fn(u32) -> u8| {
        clamp((get(source) as i32 * source_alpha + get(destination) as i32 * remaining_alpha) / 255)
    };
    let final_alpha = clamp(source_alpha + remaining_alpha);
    argb(final_alpha, channel(red), channel(green), channel(blue))
}

fn contour_exact(
    source: &[u32],
    width: usize,
    height: usize,
    levels: f32,
    scale: f32,
    offset: f32,
    contour_color: u32,
) -> Vec<u32> {
    let offset_level = (offset * 256.0 / levels) as i32;
    let table = (0..256)
        .map(|value| {
            clamp(
                (255.0 * (levels * (value + offset_level) as f32 / 256.0).floor() / (levels - 1.0)
                    - offset_level as f32) as i32,
            )
        })
        .collect::<Vec<_>>();
    let luminance = source
        .iter()
        .map(|color| brightness(*color) as u8)
        .collect::<Vec<_>>();
    let mut output = source.to_vec();
    for y in 1..height.saturating_sub(1) {
        for x in 1..width.saturating_sub(1) {
            let northwest = luminance[(y - 1) * width + x - 1];
            let northeast = luminance[(y - 1) * width + x];
            let southwest = luminance[y * width + x - 1];
            let southeast = luminance[y * width + x];
            if table[northwest as usize] != table[northeast as usize]
                || table[northwest as usize] != table[southwest as usize]
                || table[northeast as usize] != table[southeast as usize]
                || table[southwest as usize] != table[southeast as usize]
            {
                let strength = (scale
                    * ((northwest as i32 - northeast as i32).abs()
                        + (northwest as i32 - southwest as i32).abs()
                        + (northeast as i32 - southeast as i32).abs()
                        + (southwest as i32 - southeast as i32).abs()) as f32)
                    as i32;
                if strength != 0 {
                    let index = y * width + x;
                    output[index] =
                        composite_normal(source[index], contour_color, strength.min(255));
                }
            }
        }
    }
    output
}

fn equalize(pixels: &mut [u32]) {
    let mut histograms = [[0usize; 256]; 3];
    for color in pixels.iter() {
        histograms[0][red(*color) as usize] += 1;
        histograms[1][green(*color) as usize] += 1;
        histograms[2][blue(*color) as usize] += 1;
    }
    let mut maps = [[0u8; 256]; 3];
    for channel in 0..3 {
        let mut sum = 0usize;
        for value in 0..256 {
            sum += histograms[channel][value];
            maps[channel][value] = (sum as f32 * 255.0 / pixels.len().max(1) as f32).round() as u8;
        }
    }
    for color in pixels {
        *color = argb(
            alpha(*color),
            maps[0][red(*color) as usize],
            maps[1][green(*color) as usize],
            maps[2][blue(*color) as usize],
        );
    }
}

fn oil_filter(
    source: &[u32],
    width: usize,
    height: usize,
    range: usize,
    levels: usize,
) -> Vec<u32> {
    let levels = levels.max(2);
    let mut output = vec![0; source.len()];
    for y in 0..height {
        for x in 0..width {
            let mut counts = [
                vec![0usize; levels],
                vec![0usize; levels],
                vec![0usize; levels],
            ];
            let mut totals = [
                vec![0usize; levels],
                vec![0usize; levels],
                vec![0usize; levels],
            ];
            for yy in y.saturating_sub(range)..=(y + range).min(height - 1) {
                for xx in x.saturating_sub(range)..=(x + range).min(width - 1) {
                    let color = source[yy * width + xx];
                    for (channel, value) in [red(color), green(color), blue(color)]
                        .into_iter()
                        .enumerate()
                    {
                        let level = value as usize * levels / 256;
                        counts[channel][level] += 1;
                        totals[channel][level] += value as usize;
                    }
                }
            }
            let channel = |channel: usize| {
                let mut selected = 0;
                for level in 1..levels {
                    if counts[channel][level] > counts[channel][selected] {
                        selected = level;
                    }
                }
                (totals[channel][selected] / counts[channel][selected]) as u8
            };
            output[y * width + x] = argb(
                alpha(source[y * width + x]),
                channel(0),
                channel(1),
                channel(2),
            );
        }
    }
    output
}

fn resize(
    source: &[u32],
    width: usize,
    height: usize,
    new_width: usize,
    new_height: usize,
) -> Vec<u32> {
    let mut output = vec![0; new_width * new_height];
    let x_scale = width as f32 / new_width as f32;
    let y_scale = height as f32 / new_height as f32;
    for y in 0..new_height {
        for x in 0..new_width {
            let source_x = (x as f32 * x_scale) as usize;
            let source_y = (y as f32 * y_scale) as usize;
            output[y * new_width + x] = source[source_y * width + source_x];
        }
    }
    output
}

fn bicubic_resize(
    source: &[u32],
    width: usize,
    height: usize,
    new_width: usize,
    new_height: usize,
) -> Vec<u32> {
    let cubic = |value: f32| {
        let value = value.abs();
        if value <= 1.0 {
            1.5 * value * value * value - 2.5 * value * value + 1.0
        } else if value < 2.0 {
            -0.5 * value * value * value + 2.5 * value * value - 4.0 * value + 2.0
        } else {
            0.0
        }
    };
    let mut output = vec![0; new_width * new_height];
    for y in 0..new_height {
        let source_y = (y as f32 + 0.5) * height as f32 / new_height as f32 - 0.5;
        let base_y = source_y.floor() as isize;
        for x in 0..new_width {
            let source_x = (x as f32 + 0.5) * width as f32 / new_width as f32 - 0.5;
            let base_x = source_x.floor() as isize;
            let mut totals = [0.0_f32; 4];
            let mut total_weight = 0.0;
            for offset_y in -1..=2 {
                let weight_y = cubic(source_y - (base_y + offset_y) as f32);
                for offset_x in -1..=2 {
                    let weight = weight_y * cubic(source_x - (base_x + offset_x) as f32);
                    let color = sample(
                        source,
                        width,
                        height,
                        base_x + offset_x,
                        base_y + offset_y,
                        1,
                    );
                    totals[0] += alpha(color) as f32 * weight;
                    totals[1] += red(color) as f32 * weight;
                    totals[2] += green(color) as f32 * weight;
                    totals[3] += blue(color) as f32 * weight;
                    total_weight += weight;
                }
            }
            output[y * new_width + x] = argb(
                clamp((totals[0] / total_weight) as i32),
                clamp((totals[1] / total_weight) as i32),
                clamp((totals[2] / total_weight) as i32),
                clamp((totals[3] / total_weight) as i32),
            );
        }
    }
    output
}

fn neighborhood_filter(
    name: &str,
    source: &[u32],
    width: usize,
    height: usize,
) -> Option<Vec<u32>> {
    if !matches!(name, "MinimumFilter" | "MaximumFilter" | "MedianFilter") {
        return None;
    }
    let mut output = vec![0; source.len()];
    for y in 0..height {
        for x in 0..width {
            let mut values = Vec::with_capacity(9);
            for dy in -1..=1 {
                for dx in -1..=1 {
                    let source_x = x as isize + dx;
                    let source_y = y as isize + dy;
                    if source_x >= 0
                        && source_x < width as isize
                        && source_y >= 0
                        && source_y < height as isize
                    {
                        values.push(source[source_y as usize * width + source_x as usize]);
                    }
                }
            }
            output[y * width + x] = match name {
                "MinimumFilter" => argb(
                    255,
                    values.iter().map(|color| red(*color)).min().unwrap_or(255),
                    values
                        .iter()
                        .map(|color| green(*color))
                        .min()
                        .unwrap_or(255),
                    values.iter().map(|color| blue(*color)).min().unwrap_or(255),
                ),
                "MaximumFilter" => argb(
                    255,
                    values.iter().map(|color| red(*color)).max().unwrap_or(0),
                    values.iter().map(|color| green(*color)).max().unwrap_or(0),
                    values.iter().map(|color| blue(*color)).max().unwrap_or(0),
                ),
                "MedianFilter" => {
                    values.resize(9, 0xff000000);
                    let mut best_index = 0;
                    let mut best_distance = i32::MAX;
                    for first in 0..9 {
                        let mut distance = 0;
                        for second in 0..9 {
                            distance +=
                                (red(values[first]) as i32 - red(values[second]) as i32).abs();
                            distance +=
                                (green(values[first]) as i32 - green(values[second]) as i32).abs();
                            distance +=
                                (blue(values[first]) as i32 - blue(values[second]) as i32).abs();
                        }
                        if distance < best_distance {
                            best_distance = distance;
                            best_index = first;
                        }
                    }
                    values[best_index]
                }
                _ => return None,
            };
        }
    }
    Some(output)
}

const SKELETON_TABLE: [u8; 256] = [
    0, 0, 0, 1, 0, 0, 1, 3, 0, 0, 3, 1, 1, 0, 1, 3, 0, 0, 0, 0, 0, 0, 0, 0, 2, 0, 2, 0, 3, 0, 3, 3,
    0, 0, 0, 0, 0, 0, 0, 0, 3, 0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 0, 3, 0, 2, 2,
    0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
    2, 0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 0, 2, 0, 0, 0, 3, 0, 0, 0, 0, 0, 0, 0, 3, 0, 0, 0, 3, 0, 2, 0,
    0, 1, 3, 1, 0, 0, 1, 3, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1,
    3, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
    2, 3, 1, 3, 0, 0, 1, 3, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
    2, 3, 0, 1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 3, 3, 0, 1, 0, 0, 0, 0, 2, 2, 0, 0, 2, 0, 0, 0,
];

fn skeleton_filter_exact(
    env: &mut JNIEnv,
    filter: &JObject,
    source: &[u32],
    width: usize,
    height: usize,
) -> Vec<u32> {
    let iterations = int_param(env, filter, "getIterations", 1).max(0) as usize;
    let new_color = int_param(env, filter, "getNewColor", 0xffffffff_u32 as i32) as u32;
    let has_colormap = env
        .call_method(filter, "getColormap", "()Lcom/jhlabs/Colormap;", &[])
        .ok()
        .and_then(|value| value.l().ok())
        .is_some_and(|value| !value.is_null());
    let mut input = source.to_vec();
    let mut output = vec![0; input.len()];
    for iteration in 0..iterations {
        let mut count = 0;
        for pass in 0..2 {
            if width >= 3 && height >= 3 {
                for y in 1..height - 1 {
                    for x in 1..width - 1 {
                        let index = y * width + x;
                        let mut pixel = input[index];
                        if pixel == 0xff000000 {
                            let mut table_index = 0;
                            for (bit, dx, dy) in [
                                (1, -1, -1),
                                (2, 0, -1),
                                (4, 1, -1),
                                (8, 1, 0),
                                (16, 1, 1),
                                (32, 0, 1),
                                (64, -1, 1),
                                (128, -1, 0),
                            ] {
                                let neighbor =
                                    (y as isize + dy) as usize * width + (x as isize + dx) as usize;
                                if input[neighbor] == 0xff000000 {
                                    table_index |= bit;
                                }
                            }
                            let code = SKELETON_TABLE[table_index];
                            if (pass == 0 && (code == 1 || code == 3))
                                || (pass == 1 && (code == 2 || code == 3))
                            {
                                pixel = if has_colormap {
                                    colormap_color(
                                        env,
                                        filter,
                                        iteration as f32 / iterations as f32,
                                        new_color,
                                    )
                                } else {
                                    new_color
                                };
                                count += 1;
                            }
                        }
                        output[index] = pixel;
                    }
                }
            }
            if pass == 0 {
                input = output;
                output = vec![0; input.len()];
            }
        }
        if count == 0 {
            break;
        }
    }
    output
}

fn shape_filter_exact(
    env: &mut JNIEnv,
    filter: &JObject,
    source: &[u32],
    width: usize,
    height: usize,
) -> Vec<u32> {
    const ONE: i32 = 41;
    const SQRT_2: i32 = 57;
    const SQRT_5: i32 = 91;
    let use_alpha = bool_param(env, filter, "getUseAlpha", true);
    let mut map = source
        .iter()
        .map(|color| {
            (if use_alpha {
                alpha(*color) as i32
            } else {
                brightness(*color)
            }) * ONE
        })
        .collect::<Vec<_>>();
    let mut maximum = 0;
    let passes = [
        (0..height).collect::<Vec<_>>(),
        (0..height).rev().collect::<Vec<_>>(),
    ];
    for ys in passes {
        let reverse = ys.first().copied().unwrap_or(0) != 0;
        for y in ys {
            let xs: Box<dyn Iterator<Item = usize>> = if reverse {
                Box::new((0..width).rev())
            } else {
                Box::new(0..width)
            };
            for x in xs {
                let index = y * width + x;
                if map[index] <= 0 {
                    continue;
                }
                let value = if x == 0 || y == 0 || x + 1 == width || y + 1 == height {
                    ONE
                } else {
                    let mut minimum = i32::MAX;
                    for (dx, dy, cost) in [
                        (0, -1, ONE),
                        (-1, 0, ONE),
                        (1, 0, ONE),
                        (0, 1, ONE),
                        (-1, -1, SQRT_2),
                        (1, -1, SQRT_2),
                        (-1, 1, SQRT_2),
                        (1, 1, SQRT_2),
                    ] {
                        let target =
                            (y as isize + dy) as usize * width + (x as isize + dx) as usize;
                        minimum = minimum.min(map[target] + cost);
                    }
                    if x > 1 && y > 1 && x + 2 < width && y + 2 < height {
                        for (dx, dy) in [
                            (-1, -2),
                            (1, -2),
                            (2, -1),
                            (2, 1),
                            (1, 2),
                            (-1, 2),
                            (-2, 1),
                            (-2, -1),
                        ] {
                            let target =
                                (y as isize + dy) as usize * width + (x as isize + dx) as usize;
                            minimum = minimum.min(map[target] + SQRT_5);
                        }
                    }
                    minimum
                };
                map[index] = value;
                maximum = maximum.max(value);
            }
        }
    }
    let maximum = maximum.max(1) as f32;
    let factor = float_param(env, filter, "getFactor", 1.0);
    let shape_type = int_param(env, filter, "getType", 0);
    let invert = bool_param(env, filter, "getInvert", false);
    let merge = bool_param(env, filter, "getMerge", false);
    let has_colormap = env
        .call_method(filter, "getColormap", "()Lcom/jhlabs/Colormap;", &[])
        .ok()
        .and_then(|value| value.l().ok())
        .is_some_and(|value| !value.is_null());
    source
        .iter()
        .enumerate()
        .map(|(index, source_color)| {
            let (sa, mut sr, mut sg, mut sb) = if map[index] == 0 {
                (0, 0, 0, 0)
            } else {
                let mut value = (factor * map[index] as f32 / maximum).clamp(0.0, 1.0);
                value = match shape_type {
                    1 => (1.0 - (value - 1.0) * (value - 1.0)).sqrt(),
                    2 => 1.0 - (1.0 - value * value).sqrt(),
                    3 => value * value * (3.0 - 2.0 * value),
                    _ => value,
                };
                let color = if has_colormap {
                    colormap_color(env, filter, value, 0)
                } else {
                    let gray = (value * 255.0) as u8;
                    argb(255, gray, gray, gray)
                };
                let mut r = red(color);
                let mut g = green(color);
                let mut b = blue(color);
                if invert {
                    r = 255 - r;
                    g = 255 - g;
                    b = 255 - b;
                }
                (
                    if use_alpha {
                        alpha(*source_color)
                    } else {
                        brightness(*source_color) as u8
                    },
                    r,
                    g,
                    b,
                )
            };
            if merge {
                sr = (sr as i32 * red(*source_color) as i32 / 255) as u8;
                sg = (sg as i32 * green(*source_color) as i32 / 255) as u8;
                sb = (sb as i32 * blue(*source_color) as i32 / 255) as u8;
                argb(alpha(*source_color), sr, sg, sb)
            } else {
                argb(sa, sr, sg, sb)
            }
        })
        .collect()
}

#[derive(Clone, Copy)]
struct FieldLine {
    x1: i32,
    y1: i32,
    x2: i32,
    y2: i32,
}

fn field_lines(env: &mut JNIEnv, filter: &JObject, getter: &str) -> Option<Vec<FieldLine>> {
    let signature = "()[Lcom/jhlabs/FieldWarpFilter$Line;";
    let value = env
        .call_method(filter, getter, signature, &[])
        .ok()?
        .l()
        .ok()?;
    if value.is_null() {
        return None;
    }
    let array = JObjectArray::from(value);
    let length = env.get_array_length(&array).ok()?;
    let mut lines = Vec::with_capacity(length as usize);
    for index in 0..length {
        let line = env.get_object_array_element(&array, index).ok()?;
        let read = |env: &mut JNIEnv, field| env.get_field(&line, field, "I").ok()?.i().ok();
        lines.push(FieldLine {
            x1: read(env, "x1")?,
            y1: read(env, "y1")?,
            x2: read(env, "x2")?,
            y2: read(env, "y2")?,
        });
    }
    Some(lines)
}

fn field_warp_exact(
    env: &mut JNIEnv,
    filter: &JObject,
    source: &[u32],
    width: usize,
    height: usize,
) -> Vec<u32> {
    let Some(input_lines) = field_lines(env, filter, "getInLines") else {
        return source.to_vec();
    };
    let Some(output_lines) = field_lines(env, filter, "getOutLines") else {
        return source.to_vec();
    };
    if input_lines.is_empty() || input_lines.len() != output_lines.len() {
        return source.to_vec();
    }
    let amount = float_param(env, filter, "getAmount", 1.0);
    let power = float_param(env, filter, "getPower", 1.0);
    let strength = float_param(env, filter, "getStrength", 2.0);
    let intermediate = input_lines
        .iter()
        .zip(&output_lines)
        .map(|(input, output)| FieldLine {
            x1: (input.x1 as f32 + amount * (output.x1 - input.x1) as f32) as i32,
            y1: (input.y1 as f32 + amount * (output.y1 - input.y1) as f32) as i32,
            x2: (input.x2 as f32 + amount * (output.x2 - input.x2) as f32) as i32,
            y2: (input.y2 as f32 + amount * (output.y2 - input.y2) as f32) as i32,
        })
        .collect::<Vec<_>>();
    let edge = int_param(env, filter, "getEdgeAction", 3);
    let interpolation = int_param(env, filter, "getInterpolation", 1);
    let exponent = 1.5 * strength + 0.5;
    let mut output = vec![0; source.len()];
    for y in 0..height {
        for x in 0..width {
            let mut total_weight = 0.0;
            let mut sum_x = 0.0;
            let mut sum_y = 0.0;
            for (input, line) in input_lines.iter().zip(&intermediate) {
                let line_dx = (line.x2 - line.x1) as f32;
                let line_dy = (line.y2 - line.y1) as f32;
                let line_length_squared = line_dx * line_dx + line_dy * line_dy;
                let line_length = line_length_squared.sqrt();
                let dx = x as f32 - line.x1 as f32;
                let dy = y as f32 - line.y1 as f32;
                let fraction = (dx * line_dx + dy * line_dy) / line_length_squared;
                let perpendicular = (dy * line_dx - dx * line_dy) / line_length;
                let distance = if fraction <= 0.0 {
                    (dx * dx + dy * dy).sqrt()
                } else if fraction >= 1.0 {
                    let dx = x as f32 - line.x2 as f32;
                    let dy = y as f32 - line.y2 as f32;
                    (dx * dx + dy * dy).sqrt()
                } else {
                    perpendicular.abs()
                };
                let input_dx = (input.x2 - input.x1) as f32;
                let input_dy = (input.y2 - input.y1) as f32;
                let input_length = (input_dx * input_dx + input_dy * input_dy).sqrt();
                let u =
                    input.x1 as f32 + fraction * input_dx - perpendicular * input_dy / input_length;
                let v =
                    input.y1 as f32 + fraction * input_dy + perpendicular * input_dx / input_length;
                let weight = (line_length.powf(power) / (0.001 + distance)).powf(exponent);
                sum_x += (u - x as f32) * weight;
                sum_y += (v - y as f32) * weight;
                total_weight += weight;
            }
            let sx = x as f32 + sum_x / total_weight + 0.5;
            let sy = y as f32 + sum_y / total_weight + 0.5;
            output[y * width + x] = if interpolation == 0 {
                sample(source, width, height, sx as isize, sy as isize, edge)
            } else {
                bilinear(source, width, height, sx, sy, edge)
            };
        }
    }
    output
}

#[derive(Clone)]
struct OctreeNode {
    children: [Option<usize>; 8],
    child_count: usize,
    is_leaf: bool,
    count: i32,
    total_red: i32,
    total_green: i32,
    total_blue: i32,
    index: usize,
}

struct OctreeQuantizer {
    nodes: Vec<OctreeNode>,
    color_lists: Vec<Vec<usize>>,
    colors: usize,
    maximum_colors: usize,
    reduce_colors: usize,
}

impl OctreeQuantizer {
    fn new(maximum_colors: usize) -> Self {
        Self {
            nodes: vec![OctreeNode {
                children: [None; 8],
                child_count: 0,
                is_leaf: false,
                count: 0,
                total_red: 0,
                total_green: 0,
                total_blue: 0,
                index: 0,
            }],
            color_lists: vec![Vec::new(); 6],
            colors: 0,
            maximum_colors,
            reduce_colors: 512.max(maximum_colors * 2),
        }
    }

    fn insert(&mut self, color: u32) {
        let channels = [red(color), green(color), blue(color)];
        let mut node = 0;
        for level in 0..=5 {
            let bit = 0x80 >> level;
            let child_index = ((channels[0] & bit != 0) as usize) * 4
                + ((channels[1] & bit != 0) as usize) * 2
                + (channels[2] & bit != 0) as usize;
            if let Some(child) = self.nodes[node].children[child_index] {
                if self.nodes[child].is_leaf {
                    self.nodes[child].count += 1;
                    self.nodes[child].total_red += channels[0] as i32;
                    self.nodes[child].total_green += channels[1] as i32;
                    self.nodes[child].total_blue += channels[2] as i32;
                    return;
                }
                node = child;
            } else {
                let child = self.nodes.len();
                self.nodes.push(OctreeNode {
                    children: [None; 8],
                    child_count: 0,
                    is_leaf: level == 5,
                    count: if level == 5 { 1 } else { 0 },
                    total_red: if level == 5 { channels[0] as i32 } else { 0 },
                    total_green: if level == 5 { channels[1] as i32 } else { 0 },
                    total_blue: if level == 5 { channels[2] as i32 } else { 0 },
                    index: 0,
                });
                self.nodes[node].children[child_index] = Some(child);
                self.nodes[node].child_count += 1;
                self.nodes[node].is_leaf = false;
                self.color_lists[level].push(child);
                if level == 5 {
                    self.colors += 1;
                    return;
                }
                node = child;
            }
        }
    }

    fn reduce(&mut self, target: usize) {
        for level in (0..=4).rev() {
            let nodes = self.color_lists[level].clone();
            for node in nodes {
                if self.nodes[node].child_count == 0 {
                    continue;
                }
                let children = self.nodes[node].children;
                for (slot, child) in children.into_iter().enumerate() {
                    if let Some(child) = child {
                        self.nodes[node].count += self.nodes[child].count;
                        self.nodes[node].total_red += self.nodes[child].total_red;
                        self.nodes[node].total_green += self.nodes[child].total_green;
                        self.nodes[node].total_blue += self.nodes[child].total_blue;
                        self.nodes[node].children[slot] = None;
                        self.nodes[node].child_count -= 1;
                        self.colors -= 1;
                        self.color_lists[level + 1].retain(|entry| *entry != child);
                    }
                }
                self.nodes[node].is_leaf = true;
                self.colors += 1;
                if self.colors <= target {
                    return;
                }
            }
        }
    }

    fn add_pixels(&mut self, pixels: &[u32]) {
        for color in pixels {
            self.insert(*color);
            if self.colors > self.reduce_colors {
                self.reduce(self.reduce_colors);
            }
        }
    }

    fn build_table_node(&mut self, node: usize, table: &mut Vec<u32>) {
        if self.nodes[node].is_leaf {
            let count = self.nodes[node].count;
            self.nodes[node].index = table.len();
            table.push(argb(
                255,
                (self.nodes[node].total_red / count) as u8,
                (self.nodes[node].total_green / count) as u8,
                (self.nodes[node].total_blue / count) as u8,
            ));
        } else {
            for child in self.nodes[node].children {
                if let Some(child) = child {
                    self.nodes[node].index = table.len();
                    self.build_table_node(child, table);
                }
            }
        }
    }

    fn build_table(&mut self) -> Vec<u32> {
        if self.colors > self.maximum_colors {
            self.reduce(self.maximum_colors);
        }
        let mut table = Vec::with_capacity(self.colors);
        self.build_table_node(0, &mut table);
        table
    }

    fn color_index(&self, color: u32) -> usize {
        let channels = [red(color), green(color), blue(color)];
        let mut node = 0;
        for level in 0..=5 {
            let bit = 0x80 >> level;
            let child_index = ((channels[0] & bit != 0) as usize) * 4
                + ((channels[1] & bit != 0) as usize) * 2
                + (channels[2] & bit != 0) as usize;
            let Some(child) = self.nodes[node].children[child_index] else {
                return self.nodes[node].index;
            };
            if self.nodes[child].is_leaf {
                return self.nodes[child].index;
            }
            node = child;
        }
        0
    }
}

fn quantize_exact(
    source: &[u32],
    width: usize,
    height: usize,
    colors: usize,
    dither: bool,
    serpentine: bool,
) -> Vec<u32> {
    let mut quantizer = OctreeQuantizer::new(colors);
    quantizer.add_pixels(source);
    let table = quantizer.build_table();
    let mut input = source.to_vec();
    let mut output = vec![0; source.len()];
    if !dither {
        for (index, color) in source.iter().enumerate() {
            output[index] = table[quantizer.color_index(*color)];
        }
        return output;
    }
    for y in 0..height {
        let reverse = serpentine && y & 1 == 1;
        let mut index = if reverse {
            y * width + width - 1
        } else {
            y * width
        };
        for x in 0..width {
            let color = input[index];
            let result = table[quantizer.color_index(color)];
            output[index] = result;
            let errors = [
                red(color) as i32 - red(result) as i32,
                green(color) as i32 - green(result) as i32,
                blue(color) as i32 - blue(result) as i32,
            ];
            for dy in -1_isize..=1 {
                let target_y = y as isize + dy;
                if target_y < 0 || target_y >= height as isize {
                    continue;
                }
                for dx in -1_isize..=1 {
                    let target_x = x as isize + dx;
                    if target_x < 0 || target_x >= width as isize {
                        continue;
                    }
                    let matrix_x = if reverse { -dx } else { dx };
                    let weight =
                        [0, 0, 0, 0, 0, 7, 3, 5, 1][((dy + 1) * 3 + matrix_x + 1) as usize];
                    if weight == 0 {
                        continue;
                    }
                    let target = if reverse {
                        (index as isize - dx) as usize
                    } else {
                        (index as isize + dx) as usize
                    };
                    let target_color = input[target];
                    input[target] = argb(
                        0,
                        clamp(red(target_color) as i32 + errors[0] * weight / 16),
                        clamp(green(target_color) as i32 + errors[1] * weight / 16),
                        clamp(blue(target_color) as i32 + errors[2] * weight / 16),
                    );
                }
            }
            if x + 1 < width {
                if reverse {
                    index -= 1;
                } else {
                    index += 1;
                }
            }
        }
    }
    output
}

fn apply_filter(
    env: &mut JNIEnv,
    filter: &JObject,
    name: &str,
    mut image: BitmapPixels,
) -> BitmapPixels {
    if point_filter(env, filter, name, &mut image.pixels) {
        return image;
    }
    if let Some(output) =
        transform_filter(env, filter, name, &image.pixels, image.width, image.height)
    {
        image.pixels = output;
        return image;
    }
    if let Some(output) = neighborhood_filter(name, &image.pixels, image.width, image.height) {
        image.pixels = output;
        return image;
    }
    match name {
        "BicubicScaleFilter" => {
            let width = int_param(env, filter, "getWidth", 32).max(1) as usize;
            let height = int_param(env, filter, "getHeight", 32).max(1) as usize;
            image.pixels = bicubic_resize(&image.pixels, image.width, image.height, width, height);
            image.width = width;
            image.height = height;
        }
        "BlurFilter" => {
            image.pixels = convolve_exact(
                &image.pixels,
                image.width,
                image.height,
                &KernelData {
                    width: 3,
                    height: 3,
                    values: vec![
                        1.0 / 14.0,
                        2.0 / 14.0,
                        1.0 / 14.0,
                        2.0 / 14.0,
                        2.0 / 14.0,
                        2.0 / 14.0,
                        1.0 / 14.0,
                        2.0 / 14.0,
                        1.0 / 14.0,
                    ],
                },
                true,
                1,
            );
        }
        "BorderFilter" => {
            let left = int_param(env, filter, "getLeftBorder", 0).max(0) as usize;
            let right = int_param(env, filter, "getRightBorder", 0).max(0) as usize;
            let top = int_param(env, filter, "getTopBorder", 0).max(0) as usize;
            let bottom = int_param(env, filter, "getBottomBorder", 0).max(0) as usize;
            let color = int_param(env, filter, "getBorderColor", 0) as u32;
            let width = image.width + left + right;
            let height = image.height + top + bottom;
            let mut output = vec![color; width * height];
            for y in 0..image.height {
                let source = y * image.width;
                let destination = (y + top) * width + left;
                output[destination..destination + image.width]
                    .copy_from_slice(&image.pixels[source..source + image.width]);
            }
            image.pixels = output;
            image.width = width;
            image.height = height;
        }
        "CheckFilter" => {
            let foreground = int_param(env, filter, "getForeground", -1) as u32;
            let background = int_param(env, filter, "getBackground", 0xff000000u32 as i32) as u32;
            let x_scale = int_param(env, filter, "getXScale", 8).max(1) as f32;
            let y_scale = int_param(env, filter, "getYScale", 8).max(1) as f32;
            let fuzziness = int_param(env, filter, "getFuzziness", 0) as f32 / 100.0;
            let angle = float_param(env, filter, "getAngle", 0.0);
            let sine = angle.sin();
            let cosine = angle.cos();
            for y in 0..image.height {
                for x in 0..image.width {
                    let nx = (cosine * x as f32 + sine * y as f32) / x_scale;
                    let ny = (-sine * x as f32 + cosine * y as f32) / y_scale;
                    let parity = (nx.floor() as i32 + ny.floor() as i32) & 1;
                    let edge = nx
                        .fract()
                        .abs()
                        .min(ny.fract().abs())
                        .min(1.0 - nx.fract().abs())
                        .min(1.0 - ny.fract().abs());
                    let amount = if fuzziness == 0.0 {
                        parity as f32
                    } else {
                        smooth_step(0.0, fuzziness * 0.5, edge) * parity as f32
                            + (1.0 - smooth_step(0.0, fuzziness * 0.5, edge)) * 0.5
                    };
                    image.pixels[y * image.width + x] = mix_colors(amount, background, foreground);
                }
            }
        }
        "BrushedMetalFilter" => {
            let base = int_param(env, filter, "getColor", 0xff888888u32 as i32) as u32;
            let amount = float_param(env, filter, "getAmount", 0.1) * 255.0;
            let shine = float_param(env, filter, "getShine", 0.1);
            let monochrome = bool_param(env, filter, "getMonochrome", true);
            let radius = int_param(env, filter, "getRadius", 10).max(0) as usize;
            let mut random = JavaRandom::new(0);
            for y in 0..image.height {
                for x in 0..image.width {
                    let highlight = 255.0
                        * shine
                        * (2.0 * std::f32::consts::PI * x as f32 / image.width as f32).sin();
                    let n1 = (random.next_float() * 2.0 - 1.0) * amount;
                    let n2 = if monochrome {
                        n1
                    } else {
                        (random.next_float() * 2.0 - 1.0) * amount
                    };
                    let n3 = if monochrome {
                        n1
                    } else {
                        (random.next_float() * 2.0 - 1.0) * amount
                    };
                    image.pixels[y * image.width + x] = argb(
                        255,
                        clamp((red(base) as f32 + highlight + n1) as i32),
                        clamp((green(base) as f32 + highlight + n2) as i32),
                        clamp((blue(base) as f32 + highlight + n3) as i32),
                    );
                }
            }
            if radius > 0 {
                image.pixels = box_blur_exact(
                    &image.pixels,
                    image.width,
                    image.height,
                    radius as f32,
                    0.0,
                    1,
                    false,
                );
            }
        }
        "CausticsFilter" => {
            let scale = float_param(env, filter, "getScale", 32.0).max(0.01);
            let amount = float_param(env, filter, "getAmount", 1.0);
            let turbulence = float_param(env, filter, "getTurbulence", 1.0);
            let time = float_param(env, filter, "getTime", 0.0);
            let brightness = int_param(env, filter, "getBrightness", 10) as f32 / 10.0;
            let background = int_param(env, filter, "getBgColor", 0xff799fffu32 as i32) as u32;
            for y in 0..image.height {
                for x in 0..image.width {
                    let nx = x as f32 / scale;
                    let ny = y as f32 / scale;
                    let n = perlin_noise().turbulence3(nx, ny, time, turbulence.max(1.0));
                    let ridge = (1.0 - n.abs()).powi(8) * amount * brightness;
                    image.pixels[y * image.width + x] = mix_colors(ridge, background, 0xffffffff);
                }
            }
        }
        "ChromeFilter" => {
            let source = image.pixels.clone();
            let amount = float_param(env, filter, "getAmount", 0.5);
            let exposure = float_param(env, filter, "getExposure", 1.0);
            for y in 0..image.height {
                for x in 0..image.width {
                    let left = brightness(sample(
                        &source,
                        image.width,
                        image.height,
                        x as isize - 1,
                        y as isize,
                        1,
                    ));
                    let right = brightness(sample(
                        &source,
                        image.width,
                        image.height,
                        x as isize + 1,
                        y as isize,
                        1,
                    ));
                    let top = brightness(sample(
                        &source,
                        image.width,
                        image.height,
                        x as isize,
                        y as isize - 1,
                        1,
                    ));
                    let bottom = brightness(sample(
                        &source,
                        image.width,
                        image.height,
                        x as isize,
                        y as isize + 1,
                        1,
                    ));
                    let bump = ((left - right + top - bottom) as f32 / 255.0 * amount + 0.5)
                        .clamp(0.0, 1.0);
                    let value = (1.0 - (-(bump + bump.sin()) * exposure).exp()).clamp(0.0, 1.0);
                    let channel = clamp((value * 255.0) as i32);
                    image.pixels[y * image.width + x] = argb(
                        alpha(source[y * image.width + x]),
                        channel,
                        channel,
                        channel,
                    );
                }
            }
        }
        "FBMFilter" | "TextureFilter" | "MarbleTexFilter" | "WoodFilter" => {
            let scale = float_param(env, filter, "getScale", 32.0).max(0.01);
            let stretch = float_param(env, filter, "getStretch", 1.0).max(0.01);
            let angle = float_param(env, filter, "getAngle", 0.0);
            let turbulence = float_param(
                env,
                filter,
                "getTurbulence",
                if name == "FBMFilter" { 4.0 } else { 1.0 },
            );
            let sine = angle.sin();
            let cosine = angle.cos();
            let source = image.pixels.clone();
            for y in 0..image.height {
                for x in 0..image.width {
                    let nx = (cosine * x as f32 + sine * y as f32) / scale;
                    let ny = (-sine * x as f32 + cosine * y as f32) / (scale * stretch);
                    let noise = perlin_noise().turbulence3(nx, ny, 0.0, turbulence.max(1.0));
                    let value = match name {
                        "MarbleTexFilter" => triangle(
                            nx + float_param(env, filter, "getTurbulenceFactor", 0.4) * noise,
                        ),
                        "WoodFilter" => triangle(
                            (nx * nx + ny * ny).sqrt()
                                + float_param(env, filter, "getRings", 0.5) * noise,
                        ),
                        _ => (noise + 1.0) * 0.5,
                    }
                    .clamp(0.0, 1.0);
                    let generated = colormap_color(
                        env,
                        filter,
                        value,
                        argb(
                            255,
                            clamp((255.0 * value) as i32),
                            clamp((255.0 * value) as i32),
                            clamp((255.0 * value) as i32),
                        ),
                    );
                    let index = y * image.width + x;
                    image.pixels[index] = if name == "TextureFilter" || name == "FBMFilter" {
                        mix_colors(
                            float_param(env, filter, "getAmount", 1.0),
                            source[index],
                            generated,
                        )
                    } else {
                        generated
                    };
                }
            }
        }
        "FeedbackFilter" => {
            let source = image.pixels.clone();
            let iterations = int_param(env, filter, "getIterations", 3).max(0);
            let center_x = float_param(env, filter, "getCentreX", 0.5) * image.width as f32;
            let center_y = float_param(env, filter, "getCentreY", 0.5) * image.height as f32;
            let angle = float_param(env, filter, "getAngle", 0.0);
            let distance = float_param(env, filter, "getDistance", 0.0);
            let rotation = float_param(env, filter, "getRotation", 0.0);
            let zoom = float_param(env, filter, "getZoom", 0.0);
            let dx = distance * angle.cos();
            let dy = -distance * angle.sin();
            for iteration in 1..=iterations {
                let scale = 1.0 + zoom * iteration as f32;
                let theta = rotation * iteration as f32;
                let sine = theta.sin();
                let cosine = theta.cos();
                for y in 0..image.height {
                    for x in 0..image.width {
                        let px = (x as f32 - center_x - dx * iteration as f32) / scale;
                        let py = (y as f32 - center_y - dy * iteration as f32) / scale;
                        let sx = center_x + cosine * px + sine * py;
                        let sy = center_y - sine * px + cosine * py;
                        let reflected = bilinear(&source, image.width, image.height, sx, sy, 0);
                        let index = y * image.width + x;
                        image.pixels[index] =
                            composite_normal(reflected, image.pixels[index], 255 / (iteration + 1));
                    }
                }
            }
        }
        "GradientFilter" => {
            let point = |env: &mut JNIEnv, getter: &str, default_x: i32, default_y: i32| {
                let object = env
                    .call_method(filter, getter, "()Landroid/graphics/Point;", &[])
                    .ok()
                    .and_then(|v| v.l().ok());
                object
                    .map(|p| {
                        (
                            int_field(env, &p, "x", default_x),
                            int_field(env, &p, "y", default_y),
                        )
                    })
                    .unwrap_or((default_x, default_y))
            };
            let (x1, y1) = point(env, "getPoint1", 0, 0);
            let (x2, y2) = point(env, "getPoint2", image.width as i32, image.height as i32);
            let dx = (x2 - x1) as f32;
            let dy = (y2 - y1) as f32;
            let length = (dx * dx + dy * dy).max(0.001);
            let kind = int_param(env, filter, "getType", 0);
            let interpolation = int_param(env, filter, "getInterpolation", 0);
            for y in 0..image.height {
                for x in 0..image.width {
                    let px = x as f32 - x1 as f32;
                    let py = y as f32 - y1 as f32;
                    let mut value = match kind {
                        1 => (px * dx / length).abs() + (py * dy / length).abs(),
                        2 => (px * px + py * py).sqrt() / length.sqrt(),
                        3 | 4 => {
                            (py.atan2(px) - dy.atan2(dx)).rem_euclid(std::f32::consts::TAU)
                                / std::f32::consts::TAU
                        }
                        5 => px.abs().max(py.abs()) / length.sqrt(),
                        _ => (px * dx + py * dy) / length,
                    }
                    .clamp(0.0, 1.0);
                    value = match interpolation {
                        1 => 1.0 - (value * std::f32::consts::FRAC_PI_2).cos(),
                        2 => value.acos() / std::f32::consts::FRAC_PI_2,
                        3 => value * value * (3.0 - 2.0 * value),
                        _ => value,
                    };
                    image.pixels[y * image.width + x] = colormap_color(
                        env,
                        filter,
                        value,
                        argb(
                            255,
                            clamp((255.0 * value) as i32),
                            clamp((255.0 * value) as i32),
                            clamp((255.0 * value) as i32),
                        ),
                    );
                }
            }
        }
        "LensBlurFilter" => {
            let radius = float_param(env, filter, "getRadius", 10.0).max(0.0);
            let threshold = float_param(env, filter, "getBloomThreshold", 255.0);
            let bloom = float_param(env, filter, "getBloom", 2.0);
            let mut source = image.pixels.clone();
            for color in &mut source {
                if brightness(*color) as f32 > threshold {
                    *color = argb(
                        alpha(*color),
                        clamp((red(*color) as f32 * bloom) as i32),
                        clamp((green(*color) as f32 * bloom) as i32),
                        clamp((blue(*color) as f32 * bloom) as i32),
                    );
                }
            }
            image.pixels =
                gaussian_blur_exact(&source, image.width, image.height, radius, true, true);
        }
        "LightFilter" => {
            let source = gaussian_blur_exact(
                &image.pixels,
                image.width,
                image.height,
                float_param(env, filter, "getBumpSoftness", 5.0),
                true,
                true,
            );
            let bump_height = float_param(env, filter, "getBumpHeight", 1.0);
            let constant_color = int_param(env, filter, "getDiffuseColor", -1) as u32;
            let color_source = int_param(env, filter, "getColorSource", 0);
            let light = env
                .call_method(filter, "getLight", "()Lcom/jhlabs/LightFilter$Light;", &[])
                .ok()
                .and_then(|v| v.l().ok());
            let (azimuth, elevation, intensity, light_color) = light
                .as_ref()
                .map(|value| {
                    (
                        float_param(env, value, "getAzimuth", 4.712389),
                        float_param(env, value, "getElevation", 0.523599),
                        float_param(env, value, "getIntensity", 1.0),
                        int_param(env, value, "getColor", -1) as u32,
                    )
                })
                .unwrap_or((4.712389, 0.523599, 1.0, 0xffffffff));
            let lx = azimuth.cos() * elevation.cos();
            let ly = azimuth.sin() * elevation.cos();
            let lz = elevation.sin();
            for y in 0..image.height {
                for x in 0..image.width {
                    let gx = brightness(sample(
                        &source,
                        image.width,
                        image.height,
                        x as isize + 1,
                        y as isize,
                        1,
                    )) - brightness(sample(
                        &source,
                        image.width,
                        image.height,
                        x as isize - 1,
                        y as isize,
                        1,
                    ));
                    let gy = brightness(sample(
                        &source,
                        image.width,
                        image.height,
                        x as isize,
                        y as isize + 1,
                        1,
                    )) - brightness(sample(
                        &source,
                        image.width,
                        image.height,
                        x as isize,
                        y as isize - 1,
                        1,
                    ));
                    let nx = -gx as f32 * bump_height / 255.0;
                    let ny = -gy as f32 * bump_height / 255.0;
                    let length = (nx * nx + ny * ny + 1.0).sqrt();
                    let shade = ((nx * lx + ny * ly + lz) / length * intensity).max(0.0);
                    let base = if color_source == 0 {
                        image.pixels[y * image.width + x]
                    } else {
                        constant_color
                    };
                    image.pixels[y * image.width + x] = argb(
                        alpha(base),
                        clamp((red(base) as f32 * shade * red(light_color) as f32 / 255.0) as i32),
                        clamp(
                            (green(base) as f32 * shade * green(light_color) as f32 / 255.0) as i32,
                        ),
                        clamp(
                            (blue(base) as f32 * shade * blue(light_color) as f32 / 255.0) as i32,
                        ),
                    );
                }
            }
        }
        "MirrorFilter" => {
            let source = image.pixels.clone();
            let center =
                (float_param(env, filter, "getCentreY", 0.5) * image.height as f32) as isize;
            let gap = (float_param(env, filter, "getGap", 0.0) * image.height as f32) as isize;
            let opacity =
                (float_param(env, filter, "getOpacity", 1.0).clamp(0.0, 1.0) * 255.0) as i32;
            for y in center.max(0) as usize..image.height {
                let sy = center * 2 - y as isize - gap;
                if sy >= 0 && sy < image.height as isize {
                    for x in 0..image.width {
                        let index = y * image.width + x;
                        image.pixels[index] = composite_normal(
                            source[sy as usize * image.width + x],
                            image.pixels[index],
                            opacity,
                        );
                    }
                }
            }
        }
        "PlasmaFilter" => {
            let turbulence = float_param(env, filter, "getTurbulence", 1.0).max(1.0);
            let scaling = float_param(env, filter, "getScaling", 0.0);
            let phase = long_field(env, filter, "seed", 567) as f32 * 0.0001;
            let original = image.pixels.clone();
            for y in 0..image.height {
                for x in 0..image.width {
                    let nx = x as f32 / image.width as f32 * 4.0;
                    let ny = y as f32 / image.height as f32 * 4.0;
                    let value = ((perlin_noise().turbulence3(nx + phase, ny, phase, turbulence)
                        + 1.0)
                        * 0.5
                        + scaling)
                        .clamp(0.0, 1.0);
                    let color = colormap_color(
                        env,
                        filter,
                        value,
                        argb(
                            255,
                            clamp((255.0 * value) as i32),
                            clamp((255.0 * value) as i32),
                            clamp((255.0 * value) as i32),
                        ),
                    );
                    let index = y * image.width + x;
                    image.pixels[index] = if bool_param(env, filter, "getUseImageColors", false) {
                        mix_colors(value, original[index], color)
                    } else {
                        color
                    };
                }
            }
        }
        "RaysFilter" => {
            let source = image.pixels.clone();
            let threshold = float_param(env, filter, "getThreshold", 0.0) * 255.0;
            let strength = float_param(env, filter, "getStrength", 0.5);
            let opacity =
                (float_param(env, filter, "getOpacity", 1.0).clamp(0.0, 1.0) * 255.0) as i32;
            let center_x = image.width as f32 * 0.5;
            let center_y = image.height as f32 * 0.5;
            let mut rays = vec![0; source.len()];
            for y in 0..image.height {
                for x in 0..image.width {
                    let mut total = [0_i32; 3];
                    let mut count = 0;
                    for step in 0..16 {
                        let t = step as f32 / 15.0 * strength;
                        let sx = x as f32 + (center_x - x as f32) * t;
                        let sy = y as f32 + (center_y - y as f32) * t;
                        let color = bilinear(&source, image.width, image.height, sx, sy, 1);
                        if brightness(color) as f32 >= threshold {
                            total[0] += red(color) as i32;
                            total[1] += green(color) as i32;
                            total[2] += blue(color) as i32;
                            count += 1;
                        }
                    }
                    if count > 0 {
                        rays[y * image.width + x] = argb(
                            255,
                            clamp(total[0] / count),
                            clamp(total[1] / count),
                            clamp(total[2] / count),
                        );
                    }
                }
            }
            image.pixels = if bool_param(env, filter, "getRaysOnly", false) {
                rays
            } else {
                rays.into_iter()
                    .zip(source)
                    .map(|(ray, base)| composite_normal(ray, base, opacity))
                    .collect()
            };
        }
        "ShadowFilter" => {
            let radius = float_param(env, filter, "getRadius", 5.0).max(0.0);
            let angle = float_param(env, filter, "getAngle", 4.712389);
            let distance = float_param(env, filter, "getDistance", 5.0);
            let opacity =
                (float_param(env, filter, "getOpacity", 0.5).clamp(0.0, 1.0) * 255.0) as i32;
            let shadow_color =
                int_param(env, filter, "getShadowColor", 0xff000000u32 as i32) as u32;
            let source = image.pixels.clone();
            let dx = (distance * angle.cos()).round() as isize;
            let dy = (-distance * angle.sin()).round() as isize;
            let add_margins = bool_param(env, filter, "getAddMargins", false);
            let margin = radius.ceil() as isize;
            let left = if add_margins { (margin - dx).max(0) } else { 0 } as usize;
            let right = if add_margins { (margin + dx).max(0) } else { 0 } as usize;
            let top = if add_margins { (margin - dy).max(0) } else { 0 } as usize;
            let bottom = if add_margins { (margin + dy).max(0) } else { 0 } as usize;
            let width = image.width + left + right;
            let height = image.height + top + bottom;
            let mut mask = vec![0; width * height];
            for y in 0..image.height {
                for x in 0..image.width {
                    let target_x = left as isize + x as isize + dx;
                    let target_y = top as isize + y as isize + dy;
                    if target_x >= 0
                        && target_y >= 0
                        && target_x < width as isize
                        && target_y < height as isize
                    {
                        let source_color = source[y * image.width + x];
                        mask[target_y as usize * width + target_x as usize] = argb(
                            clamp(alpha(source_color) as i32 * opacity / 255),
                            red(shadow_color),
                            green(shadow_color),
                            blue(shadow_color),
                        );
                    }
                }
            }
            let mut output = gaussian_blur_exact(&mask, width, height, radius, true, true);
            if !bool_param(env, filter, "getShadowOnly", false) {
                for y in 0..image.height {
                    for x in 0..image.width {
                        let destination = (y + top) * width + x + left;
                        output[destination] =
                            composite_normal(source[y * image.width + x], output[destination], 255);
                    }
                }
            }
            image.pixels = output;
            image.width = width;
            image.height = height;
        }
        "TileImageFilter" => {
            let width = int_param(env, filter, "getWidth", 64).max(1) as usize;
            let height = int_param(env, filter, "getHeight", 64).max(1) as usize;
            let symmetry = int_2d_array_param(env, filter, "getSymmetryMatrix")
                .filter(|matrix| !matrix.is_empty() && matrix.iter().all(|row| !row.is_empty()));
            let source = image.pixels.clone();
            let mut output = vec![0; width * height];
            for y in 0..height {
                for x in 0..width {
                    let tile_x = x / image.width;
                    let tile_y = y / image.height;
                    let mut sx = x % image.width;
                    let mut sy = y % image.height;
                    let operation = symmetry
                        .as_ref()
                        .map(|matrix| {
                            let row = &matrix[tile_y % matrix.len()];
                            row[tile_x % row.len()]
                        })
                        .unwrap_or((tile_x & 1) as i32 | (((tile_y & 1) as i32) << 1));
                    if operation == 1 || operation == 3 || operation == 4 {
                        sx = image.width - sx - 1;
                    }
                    if operation == 2 || operation == 3 || operation == 4 {
                        sy = image.height - sy - 1;
                    }
                    output[y * width + x] = source[sy * image.width + sx];
                }
            }
            image.pixels = output;
            image.width = width;
            image.height = height;
        }
        "VariableBlurFilter" => {
            let horizontal = int_param(env, filter, "getHRadius", 5).max(0) as f32;
            let vertical = int_param(env, filter, "getVRadius", 5).max(0) as f32;
            let iterations = int_param(env, filter, "getIterations", 1).max(1);
            let blurred = box_blur_exact(
                &image.pixels,
                image.width,
                image.height,
                horizontal,
                vertical,
                iterations,
                true,
            );
            let mask = env
                .call_method(filter, "getBlurMask", "()Landroid/graphics/Bitmap;", &[])
                .ok()
                .and_then(|v| v.l().ok())
                .filter(|v| !v.is_null())
                .and_then(|v| read_bitmap(env, &v));
            image.pixels = if let Some(mask) = mask {
                image
                    .pixels
                    .iter()
                    .zip(blurred)
                    .enumerate()
                    .map(|(index, (source, blur))| {
                        let x = index % image.width;
                        let y = index / image.width;
                        let mx = x * mask.width / image.width;
                        let my = y * mask.height / image.height;
                        mix_colors(
                            brightness(mask.pixels[my * mask.width + mx]) as f32 / 255.0,
                            *source,
                            blur,
                        )
                    })
                    .collect()
            } else {
                blurred
            };
        }
        "WarpFilter" => {
            let source_grid = env
                .call_method(filter, "getSourceGrid", "()Lcom/jhlabs/WarpGrid;", &[])
                .ok()
                .and_then(|v| v.l().ok())
                .filter(|v| !v.is_null());
            let destination_grid = env
                .call_method(filter, "getDestGrid", "()Lcom/jhlabs/WarpGrid;", &[])
                .ok()
                .and_then(|v| v.l().ok())
                .filter(|v| !v.is_null());
            if let (Some(source_grid), Some(destination_grid)) = (source_grid, destination_grid) {
                let rows = int_field(env, &source_grid, "rows", 0).max(0) as usize;
                let columns = int_field(env, &source_grid, "cols", 0).max(0) as usize;
                let source_x = float_array_field(env, &source_grid, "xGrid");
                let source_y = float_array_field(env, &source_grid, "yGrid");
                let destination_x = float_array_field(env, &destination_grid, "xGrid");
                let destination_y = float_array_field(env, &destination_grid, "yGrid");
                if rows >= 2
                    && columns >= 2
                    && source_x.as_ref().is_some_and(|v| v.len() >= rows * columns)
                    && source_y.as_ref().is_some_and(|v| v.len() >= rows * columns)
                    && destination_x
                        .as_ref()
                        .is_some_and(|v| v.len() >= rows * columns)
                    && destination_y
                        .as_ref()
                        .is_some_and(|v| v.len() >= rows * columns)
                {
                    let source_x = source_x.unwrap();
                    let source_y = source_y.unwrap();
                    let destination_x = destination_x.unwrap();
                    let destination_y = destination_y.unwrap();
                    let source = image.pixels.clone();
                    for y in 0..image.height {
                        for x in 0..image.width {
                            let gx =
                                x as f32 / (image.width - 1).max(1) as f32 * (columns - 1) as f32;
                            let gy =
                                y as f32 / (image.height - 1).max(1) as f32 * (rows - 1) as f32;
                            let cx = (gx.floor() as usize).min(columns - 2);
                            let cy = (gy.floor() as usize).min(rows - 2);
                            let tx = gx - cx as f32;
                            let ty = gy - cy as f32;
                            let interpolate = |values: &[f32]| {
                                let north = values[cy * columns + cx] * (1.0 - tx)
                                    + values[cy * columns + cx + 1] * tx;
                                let south = values[(cy + 1) * columns + cx] * (1.0 - tx)
                                    + values[(cy + 1) * columns + cx + 1] * tx;
                                north * (1.0 - ty) + south * ty
                            };
                            let sx =
                                x as f32 + interpolate(&source_x) - interpolate(&destination_x);
                            let sy =
                                y as f32 + interpolate(&source_y) - interpolate(&destination_y);
                            image.pixels[y * image.width + x] =
                                bilinear(&source, image.width, image.height, sx, sy, 3);
                        }
                    }
                }
            } else {
                let _ = env.exception_clear();
            }
        }
        "ScaleFilter" => {
            let width = int_field(env, filter, "width", 32).max(1) as usize;
            let height = int_field(env, filter, "height", 32).max(1) as usize;
            image.pixels = resize(&image.pixels, image.width, image.height, width, height);
            image.width = width;
            image.height = height;
        }
        "CropFilter" => {
            let left = int_param(env, filter, "getX", 0).clamp(0, image.width as i32 - 1) as usize;
            let top = int_param(env, filter, "getY", 0).clamp(0, image.height as i32 - 1) as usize;
            let width = int_param(env, filter, "getWidth", 32).max(1) as usize;
            let height = int_param(env, filter, "getHeight", 32).max(1) as usize;
            let source_width = image.width - left;
            let source_height = image.height - top;
            let mut cropped = vec![0; source_width * source_height];
            for y in 0..source_height {
                let start = (top + y) * image.width + left;
                cropped[y * source_width..(y + 1) * source_width]
                    .copy_from_slice(&image.pixels[start..start + source_width]);
            }
            image.pixels = resize(&cropped, source_width, source_height, width, height);
            image.width = width;
            image.height = height;
        }
        "FourColorFilter" => {
            let nw = int_param(env, filter, "getColorNW", 0xffff0000u32 as i32) as u32;
            let ne = int_param(env, filter, "getColorNE", 0xffff00ffu32 as i32) as u32;
            let sw = int_param(env, filter, "getColorSW", 0xff0000ffu32 as i32) as u32;
            let se = int_param(env, filter, "getColorSE", 0xff00ffffu32 as i32) as u32;
            for y in 0..image.height {
                let fy = y as f32 / image.height as f32;
                for x in 0..image.width {
                    let fx = x as f32 / image.width as f32;
                    let channel = |get: fn(u32) -> u8| {
                        let north = get(nw) as f32 + (get(ne) as f32 - get(nw) as f32) * fx;
                        let south = get(sw) as f32 + (get(se) as f32 - get(sw) as f32) * fx;
                        clamp((north + (south - north) * fy + 0.5) as i32)
                    };
                    image.pixels[y * image.width + x] =
                        argb(255, channel(red), channel(green), channel(blue));
                }
            }
        }
        "NoiseFilter" => {
            let amount = int_param(env, filter, "getAmount", 25);
            let density = float_param(env, filter, "getDensity", 1.0);
            let monochrome = bool_param(env, filter, "getMonochrome", false);
            let distribution = int_param(env, filter, "getDistribution", 1);
            let mut random = JavaRandom::new(
                std::time::SystemTime::now()
                    .duration_since(std::time::UNIX_EPOCH)
                    .map(|duration| duration.as_nanos() as i64)
                    .unwrap_or(0),
            );
            for color in &mut image.pixels {
                if random.next_float() <= density {
                    let noise = |state: &mut JavaRandom| {
                        let value = if distribution == 0 {
                            state.next_gaussian() as f32
                        } else {
                            2.0 * state.next_float() - 1.0
                        };
                        (value * amount as f32) as i32
                    };
                    let first = noise(&mut random);
                    let second = if monochrome {
                        first
                    } else {
                        noise(&mut random)
                    };
                    let third = if monochrome {
                        first
                    } else {
                        noise(&mut random)
                    };
                    *color = argb(
                        alpha(*color),
                        clamp(red(*color) as i32 + first),
                        clamp(green(*color) as i32 + second),
                        clamp(blue(*color) as i32 + third),
                    );
                }
            }
        }
        "LevelsFilter" => {
            let low = float_param(env, filter, "getLowLevel", 0.0);
            let high = float_param(env, filter, "getHighLevel", 1.0);
            let low_output = float_param(env, filter, "getLowOutputLevel", 0.0);
            let high_output = float_param(env, filter, "getHighOutputLevel", 1.0);
            let high = if low == high {
                high + 1.0 / 255.0
            } else {
                high
            };
            transfer(&mut image.pixels, |value| {
                low_output + (high_output - low_output) * (value - low) / (high - low)
            });
        }
        "LookupFilter" => {
            let colormap = env
                .call_method(filter, "getColormap", "()Lcom/jhlabs/Colormap;", &[])
                .ok()
                .and_then(|value| value.l().ok());
            if let Some(colormap) = colormap {
                let mut table = [0u32; 256];
                for (value, color) in table.iter_mut().enumerate() {
                    *color = env
                        .call_method(
                            &colormap,
                            "getColor",
                            "(F)I",
                            &[JValue::Float(value as f32 / 255.0)],
                        )
                        .ok()
                        .and_then(|value| value.i().ok())
                        .unwrap_or(0) as u32;
                }
                for color in &mut image.pixels {
                    *color = table[brightness(*color) as usize];
                }
            } else {
                let _ = env.exception_clear();
            }
        }
        "CurvesFilter" => {
            let curves = env
                .call_method(filter, "getCurves", "()[Lcom/jhlabs/Curve;", &[])
                .ok()
                .and_then(|value| value.l().ok())
                .filter(|value| !value.is_null())
                .map(JObjectArray::from);
            if let Some(curves) = curves {
                let length = env.get_array_length(&curves).unwrap_or(0);
                if length == 1 || length == 3 {
                    let red_curve = env.get_object_array_element(&curves, 0).ok();
                    let green_curve = env
                        .get_object_array_element(&curves, if length == 1 { 0 } else { 1 })
                        .ok();
                    let blue_curve = env
                        .get_object_array_element(&curves, if length == 1 { 0 } else { 2 })
                        .ok();
                    if let (Some(red_curve), Some(green_curve), Some(blue_curve)) =
                        (red_curve, green_curve, blue_curve)
                        && let (Some(red_table), Some(green_table), Some(blue_table)) = (
                            curve_table(env, &red_curve),
                            curve_table(env, &green_curve),
                            curve_table(env, &blue_curve),
                        )
                    {
                        for color in &mut image.pixels {
                            *color = argb(
                                alpha(*color),
                                red_table[red(*color) as usize],
                                green_table[green(*color) as usize],
                                blue_table[blue(*color) as usize],
                            );
                        }
                    }
                }
            } else {
                let _ = env.exception_clear();
            }
        }
        "JavaLnFFilter" => {
            for y in 0..image.height {
                for x in 0..image.width {
                    if (x & 1) != (y & 1) {
                        let index = y * image.width + x;
                        image.pixels[index] = mix_colors(0.25, 0xff999999, image.pixels[index]);
                    }
                }
            }
        }
        "OvalFilter" => {
            let center_x = image.width as f32 / 2.0;
            let center_y = image.height as f32 / 2.0;
            for y in 0..image.height {
                for x in 0..image.width {
                    let dx = (x as f32 - center_x) / center_x;
                    let dy = (y as f32 - center_y) / center_y;
                    if dx * dx + dy * dy >= 1.0 {
                        image.pixels[y * image.width + x] = 0;
                    }
                }
            }
        }
        "FadeFilter" => {
            let angle = float_param(env, filter, "getAngle", 0.0);
            let start = float_param(env, filter, "getFadeStart", 1.0);
            let fade_width = float_param(env, filter, "getFadeWidth", 10.0);
            let sides = int_param(env, filter, "getSides", 0);
            let invert = bool_param(env, filter, "getInvert", false);
            for y in 0..image.height {
                for x in 0..image.width {
                    let nx = angle.cos() * x as f32 + angle.sin() * y as f32;
                    let ny = -angle.sin() * x as f32 + angle.cos() * y as f32;
                    let nx = match sides {
                        2 => (nx * nx + ny * ny).sqrt(),
                        3 => nx.rem_euclid(16.0),
                        4 => {
                            let value = nx.rem_euclid(32.0);
                            if value > 16.0 { 32.0 - value } else { value }
                        }
                        _ => nx,
                    };
                    let mut a = clamp((smooth_step(start, start + fade_width, nx) * 255.0) as i32);
                    if invert {
                        a = 255 - a;
                    }
                    let index = y * image.width + x;
                    image.pixels[index] = argb(
                        a,
                        red(image.pixels[index]),
                        green(image.pixels[index]),
                        blue(image.pixels[index]),
                    );
                }
            }
        }
        "Flush3DFilter" => {
            let source = image.pixels.clone();
            for y in 1..image.height {
                for x in 1..image.width {
                    let index = y * image.width + x;
                    if source[index] != 0xff000000 {
                        let count = [
                            source[index - 1],
                            source[index - image.width],
                            source[index - image.width - 1],
                        ]
                        .iter()
                        .filter(|color| **color == 0xff000000)
                        .count();
                        if count >= 2 {
                            image.pixels[index] = 0xffffffff;
                        }
                    }
                }
            }
        }
        "DissolveFilter" => {
            let density = float_param(env, filter, "getDensity", 1.0);
            let softness = float_param(env, filter, "getSoftness", 0.0);
            let mut random = JavaRandom::new(0);
            let maximum = (1.0 - density) * (1.0 + softness);
            let minimum = maximum - softness;
            for color in &mut image.pixels {
                let factor = smooth_step(minimum, maximum, random.next_float());
                *color = argb(
                    clamp((alpha(*color) as f32 * factor) as i32),
                    red(*color),
                    green(*color),
                    blue(*color),
                );
            }
        }
        "AverageFilter" | "SharpenFilter" | "BumpFilter" | "ConvolveFilter" => {
            if let Some(kernel) = kernel_param(env, filter) {
                let use_alpha = bool_param(env, filter, "getUseAlpha", true);
                let premultiply_alpha = bool_param(env, filter, "getPremultiplyAlpha", true);
                let edge_action = int_param(env, filter, "getEdgeAction", 1);
                let mut source = image.pixels.clone();
                if premultiply_alpha {
                    premultiply(&mut source);
                }
                image.pixels = convolve_exact(
                    &source,
                    image.width,
                    image.height,
                    &kernel,
                    use_alpha,
                    edge_action,
                );
                if premultiply_alpha {
                    unpremultiply(&mut image.pixels);
                }
            }
        }
        "EdgeFilter" => {
            let horizontal = float_array_field(env, filter, "hEdgeMatrix")
                .unwrap_or_else(|| vec![-1.0, -2.0, -1.0, 0.0, 0.0, 0.0, 1.0, 2.0, 1.0]);
            let vertical = float_array_field(env, filter, "vEdgeMatrix")
                .unwrap_or_else(|| vec![-1.0, 0.0, 1.0, -2.0, 0.0, 2.0, -1.0, 0.0, 1.0]);
            image.pixels = edge_filter_exact(
                &image.pixels,
                image.width,
                image.height,
                &horizontal,
                &vertical,
            );
        }
        "LaplaceFilter" => {
            image.pixels = laplace_filter_exact(&image.pixels, image.width, image.height);
        }
        "ReduceNoiseFilter" => {
            image.pixels = reduce_noise_exact(&image.pixels, image.width, image.height);
        }
        "DespeckleFilter" => {
            image.pixels = despeckle_exact(&image.pixels, image.width, image.height);
        }
        "EmbossFilter" => {
            let azimuth = float_param(env, filter, "getAzimuth", 135.0_f32.to_radians());
            let elevation = float_param(env, filter, "getElevation", 30.0_f32.to_radians());
            let bump_height = float_param(env, filter, "getBumpHeight", 1.0);
            let use_source_color = bool_param(env, filter, "getEmboss", false);
            let luminance = image
                .pixels
                .iter()
                .map(|color| brightness(*color))
                .collect::<Vec<_>>();
            let light_x = (azimuth.cos() * elevation.cos() * 255.9) as i32;
            let light_y = (azimuth.sin() * elevation.cos() * 255.9) as i32;
            let light_z = (elevation.sin() * 255.9) as i32;
            let normal_z = (6.0 * 255.0 / (3.0 * bump_height)) as i32;
            let normal_z_squared = normal_z * normal_z;
            let normal_z_light_z = normal_z * light_z;
            let mut output = vec![0; image.pixels.len()];
            for y in 0..image.height {
                for x in 0..image.width {
                    let index = y * image.width + x;
                    let shade = if y != 0
                        && y < image.height.saturating_sub(2)
                        && x != 0
                        && x < image.width.saturating_sub(2)
                    {
                        let top = (y - 1) * image.width;
                        let middle = y * image.width;
                        let bottom = (y + 1) * image.width;
                        let normal_x = luminance[top + x - 1]
                            + luminance[middle + x - 1]
                            + luminance[bottom + x - 1]
                            - luminance[top + x + 1]
                            - luminance[middle + x + 1]
                            - luminance[bottom + x + 1];
                        let normal_y = luminance[bottom + x - 1]
                            + luminance[bottom + x]
                            + luminance[bottom + x + 1]
                            - luminance[top + x - 1]
                            - luminance[top + x]
                            - luminance[top + x + 1];
                        if normal_x == 0 && normal_y == 0 {
                            light_z
                        } else {
                            let dot = normal_x * light_x + normal_y * light_y + normal_z_light_z;
                            if dot < 0 {
                                0
                            } else {
                                (dot as f64
                                    / ((normal_x * normal_x
                                        + normal_y * normal_y
                                        + normal_z_squared)
                                        as f64)
                                        .sqrt()) as i32
                            }
                        }
                    } else {
                        light_z
                    };
                    output[index] = if use_source_color {
                        let source = image.pixels[index];
                        argb(
                            alpha(source),
                            ((red(source) as i32 * shade) >> 8) as u8,
                            ((green(source) as i32 * shade) >> 8) as u8,
                            ((blue(source) as i32 * shade) >> 8) as u8,
                        )
                    } else {
                        argb(255, shade as u8, shade as u8, shade as u8)
                    };
                }
            }
            image.pixels = output;
        }
        "GaussianFilter" => {
            let radius = float_param(env, filter, "getRadius", 2.0);
            let use_alpha = bool_param(env, filter, "getUseAlpha", true);
            let premultiply_alpha = bool_param(env, filter, "getPremultiplyAlpha", true);
            image.pixels = gaussian_blur_exact(
                &image.pixels,
                image.width,
                image.height,
                radius,
                use_alpha,
                premultiply_alpha,
            );
        }
        "GlowFilter" => {
            let radius = float_param(env, filter, "getRadius", 2.0);
            let amount = float_param(env, filter, "getAmount", 0.5);
            let blurred = gaussian_blur_exact(
                &image.pixels,
                image.width,
                image.height,
                radius,
                bool_param(env, filter, "getUseAlpha", true),
                bool_param(env, filter, "getPremultiplyAlpha", true),
            );
            for (color, glow) in image.pixels.iter_mut().zip(blurred) {
                *color = argb(
                    alpha(*color),
                    clamp(red(*color) as i32 + (red(glow) as f32 * 4.0 * amount) as i32),
                    clamp(green(*color) as i32 + (green(glow) as f32 * 4.0 * amount) as i32),
                    clamp(blue(*color) as i32 + (blue(glow) as f32 * 4.0 * amount) as i32),
                );
            }
        }
        "UnsharpFilter" => {
            let radius = float_param(env, filter, "getRadius", 2.0);
            let amount = float_param(env, filter, "getAmount", 0.5);
            let threshold = int_param(env, filter, "getThreshold", 1);
            let blurred = gaussian_blur_exact(
                &image.pixels,
                image.width,
                image.height,
                radius,
                bool_param(env, filter, "getUseAlpha", true),
                bool_param(env, filter, "getPremultiplyAlpha", true),
            );
            for (color, blur) in image.pixels.iter_mut().zip(blurred) {
                let sharpen = |value: u8, blurred: u8| {
                    let difference = value as i32 - blurred as i32;
                    if difference.abs() >= threshold {
                        clamp(((4.0 * amount + 1.0) * difference as f32 + blurred as f32) as i32)
                    } else {
                        value
                    }
                };
                *color = argb(
                    alpha(*color),
                    sharpen(red(*color), red(blur)),
                    sharpen(green(*color), green(blur)),
                    sharpen(blue(*color), blue(blur)),
                );
            }
        }
        "HighPassFilter" => {
            let radius = float_param(env, filter, "getRadius", 10.0);
            let blurred = gaussian_blur_exact(
                &image.pixels,
                image.width,
                image.height,
                radius,
                bool_param(env, filter, "getUseAlpha", true),
                bool_param(env, filter, "getPremultiplyAlpha", true),
            );
            for (color, blur) in image.pixels.iter_mut().zip(blurred) {
                *color = argb(
                    alpha(*color),
                    ((red(*color) as i32 + 255 - red(blur) as i32) / 2) as u8,
                    ((green(*color) as i32 + 255 - green(blur) as i32) / 2) as u8,
                    ((blue(*color) as i32 + 255 - blue(blur) as i32) / 2) as u8,
                );
            }
        }
        "DoGFilter" => {
            let radius1 = float_param(env, filter, "getRadius1", 1.0);
            let radius2 = float_param(env, filter, "getRadius2", 2.0);
            let first = box_blur_exact(
                &image.pixels,
                image.width,
                image.height,
                radius1,
                radius1,
                3,
                true,
            );
            let second = box_blur_exact(
                &image.pixels,
                image.width,
                image.height,
                radius2,
                radius2,
                3,
                true,
            );
            let mut difference = Vec::with_capacity(image.pixels.len());
            for (source, destination) in first.into_iter().zip(second) {
                let source_alpha = alpha(source) as f32;
                let destination_alpha = alpha(destination) as f32;
                let amount = source_alpha / 255.0;
                let inverse = 1.0 - amount;
                let channel = |get: fn(u32) -> u8| {
                    let destination_channel = get(destination) as i32;
                    let delta = (destination_channel - get(source) as i32).max(0);
                    (amount * delta as f32 + inverse * destination_channel as f32) as u8
                };
                difference.push(argb(
                    (source_alpha + destination_alpha * inverse) as u8,
                    channel(red),
                    channel(green),
                    channel(blue),
                ));
            }
            if bool_param(env, filter, "getNormalize", true) && radius1 != radius2 {
                let maximum = difference
                    .iter()
                    .flat_map(|color| [red(*color), green(*color), blue(*color)])
                    .max()
                    .unwrap_or(0) as i32;
                if maximum != 0 {
                    for color in &mut difference {
                        *color = argb(
                            alpha(*color),
                            (red(*color) as i32 * 255 / maximum) as u8,
                            (green(*color) as i32 * 255 / maximum) as u8,
                            (blue(*color) as i32 * 255 / maximum) as u8,
                        );
                    }
                }
            }
            if bool_param(env, filter, "getInvert", false) {
                for color in &mut difference {
                    *color = (*color & 0xff00_0000) | (!*color & 0x00ff_ffff);
                }
            }
            image.pixels = difference;
        }
        "EqualizeFilter" => equalize(&mut image.pixels),
        "QuantizeFilter" => {
            image.pixels = quantize_exact(
                &image.pixels,
                image.width,
                image.height,
                int_param(env, filter, "getNumColors", 256).clamp(8, 256) as usize,
                bool_param(env, filter, "getDither", false),
                bool_param(env, filter, "getSerpentine", true),
            );
        }
        "DitherFilter" => {
            let levels = int_param(env, filter, "getLevels", 6).max(2) as usize;
            let matrix = int_array_param(env, filter, "getMatrix")
                .unwrap_or_else(|| vec![0, 14, 3, 13, 11, 5, 8, 6, 12, 2, 15, 1, 7, 9, 4, 10]);
            let rows = (matrix.len() as f64).sqrt() as usize;
            let columns = rows;
            let map = (0..levels)
                .map(|index| (255 * index / (levels - 1)) as u8)
                .collect::<Vec<_>>();
            let divisor = (0..256)
                .map(|value| (levels - 1) * value / 256)
                .collect::<Vec<_>>();
            let modulo = (0..256)
                .map(|value| value * (rows * columns + 1) / 256)
                .collect::<Vec<_>>();
            let color_dither = bool_field(env, filter, "colorDither", true);
            for y in 0..image.height {
                for x in 0..image.width {
                    let color = image.pixels[y * image.width + x];
                    let threshold = matrix[(y % rows) * columns + x % columns] as usize;
                    let quantize = |value: u8| -> u8 {
                        let value = value as usize;
                        map[if modulo[value] > threshold {
                            divisor[value] + 1
                        } else {
                            divisor[value]
                        }]
                    };
                    let (r, g, b) = if color_dither {
                        (
                            quantize(red(color)),
                            quantize(green(color)),
                            quantize(blue(color)),
                        )
                    } else {
                        let value = quantize(brightness(color) as u8);
                        (value, value, value)
                    };
                    image.pixels[y * image.width + x] = argb(alpha(color), r, g, b);
                }
            }
        }
        "DiffusionFilter" => {
            let levels = int_param(env, filter, "getLevels", 6).max(2) as usize;
            let matrix = int_array_param(env, filter, "getMatrix")
                .unwrap_or_else(|| vec![0, 0, 0, 0, 0, 7, 3, 5, 1]);
            let sum = matrix.iter().sum::<i32>();
            let serpentine = bool_param(env, filter, "getSerpentine", true);
            let color_dither = bool_param(env, filter, "getColorDither", true);
            let map = (0..levels)
                .map(|index| (255 * index / (levels - 1)) as u8)
                .collect::<Vec<_>>();
            let divisor = (0..256)
                .map(|value| levels * value / 256)
                .collect::<Vec<_>>();
            let mut input = image.pixels.clone();
            let mut output = vec![0; input.len()];
            for y in 0..image.height {
                let reverse = serpentine && y & 1 == 1;
                let mut index = if reverse {
                    y * image.width + image.width - 1
                } else {
                    y * image.width
                };
                for x in 0..image.width {
                    let color = input[index];
                    let mut r1 = red(color) as i32;
                    let mut g1 = green(color) as i32;
                    let mut b1 = blue(color) as i32;
                    if !color_dither {
                        let value = (r1 + g1 + b1) / 3;
                        r1 = value;
                        g1 = value;
                        b1 = value;
                    }
                    let r2 = map[divisor[r1 as usize]] as i32;
                    let g2 = map[divisor[g1 as usize]] as i32;
                    let b2 = map[divisor[b1 as usize]] as i32;
                    output[index] = argb(alpha(color), r2 as u8, g2 as u8, b2 as u8);
                    let errors = [r1 - r2, g1 - g2, b1 - b2];
                    for dy in -1_isize..=1 {
                        let target_y = y as isize + dy;
                        if target_y < 0 || target_y >= image.height as isize {
                            continue;
                        }
                        for dx in -1_isize..=1 {
                            let target_x = x as isize + dx;
                            if target_x < 0 || target_x >= image.width as isize {
                                continue;
                            }
                            let matrix_x = if reverse { -dx } else { dx };
                            let weight = matrix[((dy + 1) * 3 + matrix_x + 1) as usize];
                            if weight == 0 {
                                continue;
                            }
                            let target = if reverse {
                                (index as isize - dx) as usize
                            } else {
                                (index as isize + dx) as usize
                            };
                            let target_color = input[target];
                            input[target] = argb(
                                alpha(target_color),
                                clamp(red(target_color) as i32 + errors[0] * weight / sum),
                                clamp(green(target_color) as i32 + errors[1] * weight / sum),
                                clamp(blue(target_color) as i32 + errors[2] * weight / sum),
                            );
                        }
                    }
                    if x + 1 < image.width {
                        if reverse {
                            index -= 1;
                        } else {
                            index += 1;
                        }
                    }
                }
            }
            image.pixels = output;
        }
        "ErodeFilter" => {
            image.pixels = binary_morphology_exact(
                env,
                filter,
                &image.pixels,
                image.width,
                image.height,
                true,
            );
        }
        "DilateFilter" => {
            image.pixels = binary_morphology_exact(
                env,
                filter,
                &image.pixels,
                image.width,
                image.height,
                false,
            );
        }
        "OutlineFilter" => {
            image.pixels =
                outline_filter_exact(env, filter, &image.pixels, image.width, image.height);
        }
        "ContourFilter" => {
            image.pixels = contour_exact(
                &image.pixels,
                image.width,
                image.height,
                float_param(env, filter, "getLevels", 5.0),
                float_param(env, filter, "getScale", 1.0),
                float_param(env, filter, "getOffset", 0.0),
                int_param(env, filter, "getContourColor", 0xff000000_u32 as i32) as u32,
            );
        }
        "SkeletonFilter" => {
            image.pixels =
                skeleton_filter_exact(env, filter, &image.pixels, image.width, image.height);
        }
        "OilFilter" => {
            let range = int_param(env, filter, "getRange", 3).max(0) as usize;
            let levels = int_param(env, filter, "getLevels", 256).max(2) as usize;
            image.pixels = oil_filter(&image.pixels, image.width, image.height, range, levels);
        }
        "StampFilter" => {
            let radius = float_param(env, filter, "getRadius", 5.0) as i32 as f32;
            let threshold = float_param(env, filter, "getThreshold", 0.5) * 255.0;
            let softness = float_param(env, filter, "getSoftness", 0.0) * 255.0 * 0.5;
            let white = int_param(env, filter, "getWhite", 0xffffffffu32 as i32) as u32;
            let black = int_param(env, filter, "getBlack", 0xff000000u32 as i32) as u32;
            let blurred =
                gaussian_blur_exact(&image.pixels, image.width, image.height, radius, true, true);
            for (color, blur) in image.pixels.iter_mut().zip(blurred) {
                *color = mix_colors(
                    smooth_step(
                        threshold - softness,
                        threshold + softness,
                        brightness(blur) as f32,
                    ),
                    black,
                    white,
                );
            }
        }
        "HalftoneFilter" => {
            let Some(mask) = int_array_param(env, filter, "getMask") else {
                image.pixels.fill(0);
                return image;
            };
            let mask_width = int_field(env, filter, "maskWidth", 0) as usize;
            let mask_height = int_field(env, filter, "maskHeight", 0) as usize;
            let softness = float_param(env, filter, "getSoftness", 0.1) * 255.0;
            let invert = bool_param(env, filter, "getInvert", false);
            let monochrome = bool_param(env, filter, "getMonochrome", false);
            for y in 0..image.height {
                for x in 0..image.width {
                    let index = y * image.width + x;
                    let source = image.pixels[index];
                    let mut mask_color =
                        mask[(y % mask_height) * mask_width + x % mask_width] as u32;
                    if invert {
                        mask_color ^= 0x00ff_ffff;
                    }
                    image.pixels[index] = if monochrome {
                        let value = (255.0
                            * (1.0
                                - smooth_step(
                                    brightness(source) as f32 - softness,
                                    brightness(source) as f32 + softness,
                                    brightness(mask_color) as f32,
                                ))) as u8;
                        argb(alpha(source), value, value, value)
                    } else {
                        let channel = |get: fn(u32) -> u8| {
                            let source_value = get(source) as f32;
                            (255.0
                                * (1.0
                                    - smooth_step(
                                        source_value - softness,
                                        source_value + softness,
                                        get(mask_color) as f32,
                                    ))) as u8
                        };
                        argb(alpha(source), channel(red), channel(green), channel(blue))
                    };
                }
            }
        }
        "ColorHalftoneFilter" => {
            let dot_radius = float_param(env, filter, "getdotRadius", 2.0);
            let grid_size = 2.0 * dot_radius * 1.414;
            let half_grid = grid_size / 2.0;
            let angles = [
                float_param(env, filter, "getCyanScreenAngle", 108.0_f32.to_radians()),
                float_param(env, filter, "getMagentaScreenAngle", 162.0_f32.to_radians()),
                float_param(env, filter, "getYellowScreenAngle", 90.0_f32.to_radians()),
            ];
            let source = image.pixels.clone();
            for y in 0..image.height {
                for x in 0..image.width {
                    let index = y * image.width + x;
                    let mut output = (source[index] & 0xff00_0000) | 0x00ff_ffff;
                    for (channel, angle) in angles.into_iter().enumerate() {
                        let shift = 16 - 8 * channel;
                        let mask = 0xff_u32 << shift;
                        let sine = angle.sin();
                        let cosine = angle.cos();
                        let transformed_x = x as f32 * cosine + y as f32 * sine;
                        let transformed_y = -(x as f32) * sine + y as f32 * cosine;
                        let grid_x = transformed_x
                            - (transformed_x - half_grid).rem_euclid(grid_size)
                            + half_grid;
                        let grid_y = transformed_y
                            - (transformed_y - half_grid).rem_euclid(grid_size)
                            + half_grid;
                        let mut factor = 1.0_f32;
                        for (offset_x, offset_y) in
                            [(0.0, 0.0), (-1.0, 0.0), (1.0, 0.0), (0.0, -1.0), (0.0, 1.0)]
                        {
                            let neighbor_x = grid_x + offset_x * grid_size;
                            let neighbor_y = grid_y + offset_y * grid_size;
                            let image_x = neighbor_x * cosine - neighbor_y * sine;
                            let image_y = neighbor_x * sine + neighbor_y * cosine;
                            let sample_x =
                                (image_x as i32).clamp(0, image.width as i32 - 1) as usize;
                            let sample_y =
                                (image_y as i32).clamp(0, image.height as i32 - 1) as usize;
                            let value = ((source[sample_y * image.width + sample_x] >> shift)
                                & 0xff) as f32
                                / 255.0;
                            let dot = (1.0 - value * value) * half_grid * 1.414;
                            let distance = ((x as f32 - image_x).powi(2)
                                + (y as f32 - image_y).powi(2))
                            .sqrt();
                            factor = factor.min(1.0 - smooth_step(distance, distance + 1.0, dot));
                        }
                        let value = (255.0 * factor) as u32;
                        let screen = ((value << shift) ^ !mask) | 0xff00_0000;
                        output &= screen;
                    }
                    image.pixels[index] = output;
                }
            }
        }
        "MotionBlurFilter" => {
            let distance = float_param(env, filter, "getDistance", 1.0);
            let angle = float_param(env, filter, "getAngle", 0.0);
            let rotation = float_param(env, filter, "getRotation", 0.0);
            let zoom = float_param(env, filter, "getZoom", 0.0);
            let wrap = bool_param(env, filter, "getWrapEdges", false);
            let premultiplied = bool_param(env, filter, "getPremultiplyAlpha", true);
            let center_x = image.width as i32 / 2;
            let center_y = image.height as i32 / 2;
            let image_radius = ((center_x * center_x + center_y * center_y) as f32).sqrt();
            let translate_x = distance * angle.cos();
            let translate_y = -distance * angle.sin();
            let repetitions =
                (distance + (rotation * image_radius).abs() + zoom * image_radius) as i32;
            let mut source = image.pixels.clone();
            if premultiplied {
                premultiply(&mut source);
            }
            let mut output = vec![0; source.len()];
            for y in 0..image.height {
                for x in 0..image.width {
                    let mut totals = [0_i32; 4];
                    let mut count = 0;
                    for iteration in 0..repetitions {
                        let factor = iteration as f32 / repetitions as f32;
                        let scale = 1.0 - zoom * factor;
                        let rotation_radians = (-rotation * factor).to_radians();
                        let translated_x = x as f32 + center_x as f32 + factor * translate_x;
                        let translated_y = y as f32 + center_y as f32 + factor * translate_y;
                        let scaled_x = translated_x * scale;
                        let scaled_y = translated_y * scale;
                        let mut source_x = (scaled_x * rotation_radians.cos()
                            - scaled_y * rotation_radians.sin()
                            - center_x as f32) as i32;
                        let mut source_y = (scaled_x * rotation_radians.sin()
                            + scaled_y * rotation_radians.cos()
                            - center_y as f32) as i32;
                        if source_x < 0 || source_x >= image.width as i32 {
                            if wrap {
                                source_x = source_x.rem_euclid(image.width as i32);
                            } else {
                                break;
                            }
                        }
                        if source_y < 0 || source_y >= image.height as i32 {
                            if wrap {
                                source_y = source_y.rem_euclid(image.height as i32);
                            } else {
                                break;
                            }
                        }
                        let color = source[source_y as usize * image.width + source_x as usize];
                        totals[0] += alpha(color) as i32;
                        totals[1] += red(color) as i32;
                        totals[2] += green(color) as i32;
                        totals[3] += blue(color) as i32;
                        count += 1;
                    }
                    let index = y * image.width + x;
                    output[index] = if count == 0 {
                        source[index]
                    } else {
                        argb(
                            clamp(totals[0] / count),
                            clamp(totals[1] / count),
                            clamp(totals[2] / count),
                            clamp(totals[3] / count),
                        )
                    };
                }
            }
            if premultiplied {
                unpremultiply(&mut output);
            }
            image.pixels = output;
        }
        "MotionBlurOp" => {
            let distance = float_param(env, filter, "getDistance", 0.0);
            let angle = float_param(env, filter, "getAngle", 0.0);
            let rotation = float_param(env, filter, "getRotation", 0.0);
            let zoom = float_param(env, filter, "getZoom", 0.0);
            let center_x = image.width as f32 * float_param(env, filter, "getCentreX", 0.5);
            let center_y = image.height as f32 * float_param(env, filter, "getCentreY", 0.5);
            let radius = (center_x * center_x + center_y * center_y).sqrt();
            let maximum = distance + (rotation * radius).abs() + zoom * radius;
            let mut magnitude = 1;
            let mut steps = 0;
            while magnitude < maximum as i32 {
                magnitude *= 2;
                steps += 1;
            }
            if steps != 0 {
                let mut translate_x = distance * angle.cos() / maximum;
                let mut translate_y = -distance * angle.sin() / maximum;
                let mut scale = zoom / maximum;
                let mut rotate = rotation / maximum;
                let mut buffers = [image.pixels.clone(), image.pixels.clone()];
                for iteration in 0..steps {
                    let buffer = iteration & 1;
                    let source = buffers[buffer].clone();
                    let scale_factor = 1.0001 + scale;
                    let sine = rotate.to_radians().sin();
                    let cosine = rotate.to_radians().cos();
                    for y in 0..image.height {
                        for x in 0..image.width {
                            let mut px = x as f32 - center_x - translate_x;
                            let mut py = y as f32 - center_y - translate_y;
                            let rotated_x = cosine * px + sine * py;
                            let rotated_y = -sine * px + cosine * py;
                            px = rotated_x / (scale_factor * scale_factor) + center_x;
                            py = rotated_y / (scale_factor * scale_factor) + center_y;
                            let overlay = bilinear(&source, image.width, image.height, px, py, 0);
                            let index = y * image.width + x;
                            buffers[buffer][index] = mix_colors(0.5, source[index], overlay);
                        }
                    }
                    translate_x *= 2.0;
                    translate_y *= 2.0;
                    scale *= 2.0;
                    rotate *= 2.0;
                }
                image.pixels = buffers[steps & 1].clone();
            }
        }
        "LifeFilter" => {
            let source = image.pixels.clone();
            let black = binary_mask(env, filter, &source);
            for y in 0..image.height {
                for x in 0..image.width {
                    let mut neighbors = 0;
                    for dy in -1_isize..=1 {
                        let source_y = y as isize + dy;
                        if source_y < 0 || source_y >= image.height as isize {
                            continue;
                        }
                        for dx in -1_isize..=1 {
                            let source_x = x as isize + dx;
                            if (dx != 0 || dy != 0)
                                && source_x >= 0
                                && source_x < image.width as isize
                                && black[source_y as usize * image.width + source_x as usize]
                            {
                                neighbors += 1;
                            }
                        }
                    }
                    let index = y * image.width + x;
                    image.pixels[index] = if black[index] {
                        if neighbors == 2 || neighbors == 3 {
                            source[index]
                        } else {
                            0xffffffff
                        }
                    } else if neighbors == 3 {
                        0xff000000
                    } else {
                        source[index]
                    };
                }
            }
        }
        "CellularFilter" | "CrystallizeFilter" | "PointillizeFilter" => {
            let scale = float_param(env, filter, "getScale", 32.0);
            let stretch = float_param(env, filter, "getStretch", 1.0);
            let angle = float_param(env, filter, "getAngle", 0.0);
            let turbulence = float_param(env, filter, "getTurbulence", 1.0);
            let amount = float_param(env, filter, "getAmount", 1.0);
            let coefficient_values = float_array_field(env, filter, "coefficients")
                .unwrap_or_else(|| vec![1.0, 0.0, 0.0, 0.0]);
            let config = CellularConfig {
                randomness: float_param(env, filter, "getRandomness", 0.0),
                grid_type: int_param(env, filter, "getGridType", 2),
                distance_power: float_param(env, filter, "getDistancePower", 2.0),
                coefficients: [
                    *coefficient_values.first().unwrap_or(&1.0),
                    *coefficient_values.get(1).unwrap_or(&0.0),
                    *coefficient_values.get(2).unwrap_or(&0.0),
                    *coefficient_values.get(3).unwrap_or(&0.0),
                ],
                angle_coefficient: float_param(env, filter, "getAngleCoefficient", 0.0),
                gradient_coefficient: float_param(env, filter, "getGradientCoefficient", 0.0),
            };
            let source = image.pixels.clone();
            let edge_color = int_param(env, filter, "getEdgeColor", 0xff000000u32 as i32) as u32;
            let edge = float_param(env, filter, "getEdgeThickness", 0.4);
            let fade_edges = bool_param(env, filter, "getFadeEdges", false);
            let fuzziness = float_param(env, filter, "getFuzziness", 0.1);
            let use_color = bool_field(env, filter, "useColor", false);
            for y in 0..image.height {
                for x in 0..image.width {
                    let nx = (angle.cos() * x as f32 + angle.sin() * y as f32) / scale + 1000.0;
                    let ny = (-angle.sin() * x as f32 + angle.cos() * y as f32) / (scale * stretch)
                        + 1000.0;
                    let (mut value, mut results) = cellular_evaluate(nx, ny, &config);
                    if turbulence != 1.0 {
                        value = 0.0;
                        let mut frequency = 1.0;
                        while frequency <= turbulence {
                            let evaluated =
                                cellular_evaluate(frequency * nx, frequency * ny, &config);
                            value += evaluated.0 / frequency;
                            results = evaluated.1;
                            frequency *= 2.0;
                        }
                    }
                    let sample = |point: CellPoint| {
                        let source_x = (((point.x - 1000.0) * scale) as i32)
                            .clamp(0, image.width as i32 - 1)
                            as usize;
                        let source_y = (((point.y - 1000.0) * scale) as i32)
                            .clamp(0, image.height as i32 - 1)
                            as usize;
                        source[source_y * image.width + source_x]
                    };
                    image.pixels[y * image.width + x] = match name {
                        "CrystallizeFilter" => {
                            let first = results[0].distance;
                            let second = results[1].distance;
                            let color = sample(results[0]);
                            let factor = smooth_step(0.0, edge, (second - first) / edge);
                            if fade_edges {
                                let mixed = mix_colors(0.5, sample(results[1]), color);
                                mix_colors(factor, mixed, color)
                            } else {
                                mix_colors(factor, edge_color, color)
                            }
                        }
                        "PointillizeFilter" => {
                            let first = results[0].distance;
                            let mut color = sample(results[0]);
                            if fade_edges {
                                color = mix_colors(
                                    0.5 * first / results[1].distance,
                                    color,
                                    sample(results[1]),
                                );
                            } else {
                                color = mix_colors(
                                    1.0 - smooth_step(edge, edge + fuzziness, first),
                                    edge_color,
                                    color,
                                );
                            }
                            color
                        }
                        _ => {
                            value *= 2.0 * amount;
                            let mut color = colormap_color(
                                env,
                                filter,
                                value,
                                argb(
                                    255,
                                    clamp((value * 255.0) as i32),
                                    clamp((value * 255.0) as i32),
                                    clamp((value * 255.0) as i32),
                                ),
                            );
                            if use_color {
                                color = sample(results[0]);
                                let factor = (results[1].distance - results[0].distance)
                                    / (results[1].distance + results[0].distance);
                                color = mix_colors(
                                    smooth_step(
                                        config.coefficients[1],
                                        config.coefficients[0],
                                        factor,
                                    ),
                                    0xff000000,
                                    color,
                                );
                            }
                            color
                        }
                    };
                }
            }
        }
        "QuiltFilter" => {
            let iterations = int_param(env, filter, "getIterations", 25000).max(0);
            let a = float_param(env, filter, "getA", -0.59);
            let b = float_param(env, filter, "getB", 0.2);
            let c = float_param(env, filter, "getC", 0.1);
            let d = float_param(env, filter, "getD", 0.0);
            let k = int_param(env, filter, "getK", 0) as f32;
            let mut x = 0.1_f32;
            let mut y = 0.3_f32;
            let advance = |x: f32, y: f32| {
                let mx = std::f32::consts::PI * x;
                let my = std::f32::consts::PI * y;
                let next_x = a * (2.0 * mx).sin()
                    + b * (2.0 * mx).sin() * (2.0 * my).cos()
                    + c * (4.0 * mx).sin()
                    + d * (6.0 * mx).sin() * (4.0 * my).cos()
                    + k * x;
                let next_y = a * (2.0 * my).sin()
                    + b * (2.0 * my).sin() * (2.0 * mx).cos()
                    + c * (4.0 * my).sin()
                    + d * (6.0 * my).sin() * (4.0 * mx).cos()
                    + k * y;
                let wrap = |value: f32| {
                    if value >= 0.0 {
                        value - value.trunc()
                    } else {
                        value - value.trunc() + 1.0
                    }
                };
                (wrap(next_x), wrap(next_y))
            };
            for _ in 0..20 {
                (x, y) = advance(x, y);
            }
            image.pixels.fill(0);
            let mut maximum = 0;
            for _ in 0..iterations {
                (x, y) = advance(x, y);
                let ix = (image.width as f32 * x) as i32;
                let iy = (image.height as f32 * y) as i32;
                if ix >= 0 && ix < image.width as i32 && iy >= 0 && iy < image.height as i32 {
                    let index = iy as usize * image.width + ix as usize;
                    let previous = image.pixels[index];
                    image.pixels[index] = previous.wrapping_add(1);
                    maximum = maximum.max(previous);
                }
            }
            let has_colormap = env
                .call_method(filter, "getColormap", "()Lcom/jhlabs/Colormap;", &[])
                .ok()
                .and_then(|value| value.l().ok())
                .is_some_and(|value| !value.is_null());
            if has_colormap {
                for color in &mut image.pixels {
                    *color = colormap_color(env, filter, *color as f32 / maximum as f32, *color);
                }
            }
        }
        "FlareFilter" => {
            let custom_center = bool_field(env, filter, "customCentre", false);
            let center_x = if custom_center {
                int_field(env, filter, "centreX", 0) as f32
            } else {
                image.width as f32 / 2.0
            };
            let center_y = if custom_center {
                int_field(env, filter, "centreY", 0) as f32
            } else {
                image.height as f32 / 2.0
            };
            let radius = int_param(env, filter, "getRadius", 25) as f32;
            let ring_width = float_param(env, filter, "getRingWidth", 1.6);
            let base_amount = float_param(env, filter, "getBaseAmount", 1.0);
            let ring_amount = float_param(env, filter, "getRingAmount", 0.2);
            let ray_amount = float_param(env, filter, "getRayAmount", 0.1);
            let color = int_param(env, filter, "getColor", 0xffffffffu32 as i32) as u32;
            for y in 0..image.height {
                for x in 0..image.width {
                    let dx = x as f32 - center_x;
                    let dy = y as f32 - center_y;
                    let distance = (dx * dx + dy * dy).sqrt();
                    let mut amount = ((-distance * distance * 0.006).exp() * 0.5
                        + (-distance * 0.03).exp() * 0.5)
                        * base_amount;
                    if distance > radius + ring_width {
                        amount += ((distance - radius - ring_width) / 6.0).clamp(0.0, 1.0)
                            * (0.0 - amount);
                    }
                    let ring = if distance < radius - ring_width || distance > radius + ring_width {
                        0.0
                    } else {
                        let value = (distance - radius).abs() / ring_width;
                        (1.0 - value * value * (3.0 - 2.0 * value)) * ring_amount
                    };
                    amount += ring;
                    let mut angle = dx.atan2(dy) + std::f32::consts::PI;
                    angle = ((angle / std::f32::consts::PI * 17.0
                        + 1.0
                        + perlin_noise().noise1(angle * 10.0))
                    .rem_euclid(1.0)
                        - 0.5)
                        * 2.0;
                    amount += ray_amount * angle.abs().powi(5) / (1.0 + distance * 0.1);
                    let index = y * image.width + x;
                    image.pixels[index] =
                        mix_colors(amount.clamp(0.0, 1.0), image.pixels[index], color);
                }
            }
        }
        "SparkleFilter" => {
            let center_x = int_field(env, filter, "centreX", 0) as f32;
            let center_y = int_field(env, filter, "centreY", 0) as f32;
            let radius = int_param(env, filter, "getRadius", 25);
            let amount = int_param(env, filter, "getAmount", 50);
            let rays = int_param(env, filter, "getRays", 50).max(1) as usize;
            let randomness = int_param(env, filter, "getRandomness", 25);
            let color = int_param(env, filter, "getColor", 0xffffffffu32 as i32) as u32;
            let mut random = JavaRandom::new(long_field(env, filter, "seed", 371));
            let ray_lengths = (0..rays)
                .map(|_| {
                    radius as f32
                        + randomness as f32 / 100.0 * radius as f32 * random.next_gaussian() as f32
                })
                .collect::<Vec<_>>();
            for y in 0..image.height {
                for x in 0..image.width {
                    let dx = x as f32 - center_x;
                    let dy = y as f32 - center_y;
                    let distance = dx * dx + dy * dy;
                    let position =
                        (dy.atan2(dx) + std::f32::consts::PI) / std::f32::consts::TAU * rays as f32;
                    let ray = position as usize;
                    let mut factor = position - ray as f32;
                    if radius != 0 {
                        let length = ray_lengths[ray % rays]
                            + factor * (ray_lengths[(ray + 1) % rays] - ray_lengths[ray % rays]);
                        let gain = (length * length / (distance + 0.0001))
                            .powf((100 - amount) as f32 / 50.0);
                        factor -= 0.5;
                        factor = (1.0 - factor * factor) * gain;
                    }
                    let index = y * image.width + x;
                    image.pixels[index] =
                        mix_colors(factor.clamp(0.0, 1.0), image.pixels[index], color);
                }
            }
        }
        "WeaveFilter" => {
            let x_width = float_param(env, filter, "getXWidth", 16.0).max(1.0);
            let y_width = float_param(env, filter, "getYWidth", 16.0).max(1.0);
            let x_gap = float_param(env, filter, "getXGap", 6.0).max(0.0);
            let y_gap = float_param(env, filter, "getYGap", 6.0).max(0.0);
            let use_colors = bool_param(env, filter, "getUseImageColors", true);
            let round_threads = bool_param(env, filter, "getRoundThreads", false);
            let shade_crossings = bool_param(env, filter, "getShadeCrossings", true);
            let crossings = int_2d_array_param(env, filter, "getCrossings").unwrap_or_else(|| {
                vec![
                    vec![0, 1, 0, 1],
                    vec![1, 0, 1, 0],
                    vec![0, 1, 0, 1],
                    vec![1, 0, 1, 0],
                ]
            });
            let rows = crossings.len();
            let columns = crossings.first().map(Vec::len).unwrap_or(0);
            for y in 0..image.height {
                for x in 0..image.width {
                    let shifted_x = x as i32 + (x_width + x_gap / 2.0) as i32;
                    let shifted_y = y as i32 + (y_width + y_gap / 2.0) as i32;
                    let nx = (shifted_x as f32).rem_euclid(x_width + x_gap);
                    let ny = (shifted_y as f32).rem_euclid(y_width + y_gap);
                    let cell_x = (shifted_x as f32 / (x_width + x_gap)) as usize;
                    let cell_y = (shifted_y as f32 / (y_width + y_gap)) as usize;
                    let in_x = nx < x_width;
                    let in_y = ny < y_width;
                    let (distance_x, distance_y) = if round_threads {
                        (
                            (x_width / 2.0 - nx).abs() / x_width / 2.0,
                            (y_width / 2.0 - ny).abs() / y_width / 2.0,
                        )
                    } else {
                        (0.0, 0.0)
                    };
                    let (mut crossing_x, mut crossing_y) = if shade_crossings {
                        (
                            smooth_step(
                                x_width / 2.0,
                                x_width / 2.0 + x_gap,
                                (x_width / 2.0 - nx).abs(),
                            ),
                            smooth_step(
                                y_width / 2.0,
                                y_width / 2.0 + y_gap,
                                (y_width / 2.0 - ny).abs(),
                            ),
                        )
                    } else {
                        (0.0, 0.0)
                    };
                    let index = y * image.width + x;
                    let mut color_x = if use_colors {
                        image.pixels[index]
                    } else {
                        0xffff8080
                    };
                    let mut color_y = if use_colors {
                        image.pixels[index]
                    } else {
                        0xff8080ff
                    };
                    let crossing = crossings[cell_y % rows][cell_x % columns];
                    image.pixels[index] = if in_x {
                        if in_y {
                            let color = if crossing == 1 { color_x } else { color_y };
                            mix_colors(
                                2.0 * if crossing == 1 {
                                    distance_x
                                } else {
                                    distance_y
                                },
                                color,
                                0xff000000,
                            )
                        } else {
                            if shade_crossings {
                                if crossing != crossings[(cell_y + 1) % rows][cell_x % columns] {
                                    if crossing == 0 {
                                        crossing_y = 1.0 - crossing_y;
                                    }
                                    crossing_y *= 0.5;
                                    color_x = mix_colors(crossing_y, color_x, 0xff000000);
                                } else if crossing == 0 {
                                    color_x = mix_colors(0.5, color_x, 0xff000000);
                                }
                            }
                            mix_colors(2.0 * distance_x, color_x, 0xff000000)
                        }
                    } else if in_y {
                        if shade_crossings {
                            if crossing != crossings[cell_y % rows][(cell_x + 1) % columns] {
                                if crossing == 1 {
                                    crossing_x = 1.0 - crossing_x;
                                }
                                crossing_x *= 0.5;
                                color_y = mix_colors(crossing_x, color_y, 0xff000000);
                            } else if crossing == 1 {
                                color_y = mix_colors(0.5, color_y, 0xff000000);
                            }
                        }
                        mix_colors(2.0 * distance_y, color_y, 0xff000000)
                    } else {
                        0
                    };
                }
            }
        }
        "ShapeFilter" => {
            image.pixels =
                shape_filter_exact(env, filter, &image.pixels, image.width, image.height);
        }
        "SmearFilter" => {
            let shape = int_param(env, filter, "getShape", 1);
            let distance = int_param(env, filter, "getDistance", 8).max(1);
            let density = float_param(env, filter, "getDensity", 0.5);
            let angle = float_param(env, filter, "getAngle", 0.0);
            let mix = float_param(env, filter, "getMix", 0.5);
            let background = bool_param(env, filter, "getBackground", false);
            let mut random = JavaRandom::new(long_field(env, filter, "seed", 567));
            let source = image.pixels.clone();
            if background {
                image.pixels.fill(0xffffffff);
            }
            let mut paint = |x: i32, y: i32, color: u32| {
                if x >= 0 && x < image.width as i32 && y >= 0 && y < image.height as i32 {
                    let index = y as usize * image.width + x as usize;
                    let base = if background {
                        0xffffffff
                    } else {
                        image.pixels[index]
                    };
                    image.pixels[index] = mix_colors(mix, base, color);
                }
            };
            let positive = |value: i32| value & 0x7fff_ffff;
            match shape {
                0 => {
                    let count = (2.0 * density * image.width as f32 * image.height as f32
                        / (distance + 1) as f32) as i32;
                    for _ in 0..count {
                        let x = positive(random.next_int()) % image.width as i32;
                        let y = positive(random.next_int()) % image.height as i32;
                        let length = random.next_int() % distance + 1;
                        let color = source[y as usize * image.width + x as usize];
                        for target_x in x - length..x + length + 1 {
                            paint(target_x, y, color);
                        }
                        for target_y in y - length..y + length + 1 {
                            paint(x, target_y, color);
                        }
                    }
                }
                1 => {
                    let count = (density * image.width as f32 * image.height as f32) as i32;
                    for _ in 0..count {
                        let source_x = positive(random.next_int()) % image.width as i32;
                        let source_y = positive(random.next_int()) % image.height as i32;
                        let color = source[source_y as usize * image.width + source_x as usize];
                        let length = positive(random.next_int()) % distance;
                        let dx = (length as f32 * angle.cos()) as i32;
                        let dy = (length as f32 * angle.sin()) as i32;
                        let mut x0 = source_x - dx;
                        let mut y0 = source_y - dy;
                        let x1 = source_x + dx;
                        let y1 = source_y + dy;
                        let delta_x = (x1 - x0).abs();
                        let step_x = if x0 < x1 { 1 } else { -1 };
                        let delta_y = -(y1 - y0).abs();
                        let step_y = if y0 < y1 { 1 } else { -1 };
                        let mut error = delta_x + delta_y;
                        loop {
                            paint(x0, y0, color);
                            if x0 == x1 && y0 == y1 {
                                break;
                            }
                            let twice = 2 * error;
                            if twice >= delta_y {
                                error += delta_y;
                                x0 += step_x;
                            }
                            if twice <= delta_x {
                                error += delta_x;
                                y0 += step_y;
                            }
                        }
                    }
                }
                _ => {
                    let radius = distance + 1;
                    let count = (2.0 * density * image.width as f32 * image.height as f32
                        / radius as f32) as i32;
                    for _ in 0..count {
                        let source_x = positive(random.next_int()) % image.width as i32;
                        let source_y = positive(random.next_int()) % image.height as i32;
                        let color = source[source_y as usize * image.width + source_x as usize];
                        for x in source_x - radius..source_x + radius + 1 {
                            for y in source_y - radius..source_y + radius + 1 {
                                if shape == 3
                                    || (x - source_x).pow(2) + (y - source_y).pow(2)
                                        <= radius * radius
                                {
                                    paint(x, y, color);
                                }
                            }
                        }
                    }
                }
            }
        }
        "DisplaceFilter" => {
            let bitmap = env
                .call_method(
                    filter,
                    "getDisplacementBitmap",
                    "()Landroid/graphics/Bitmap;",
                    &[],
                )
                .ok()
                .and_then(|value| value.l().ok())
                .filter(|value| !value.is_null());
            let array = env
                .call_method(filter, "getDisplacementMap", "()[I", &[])
                .ok()
                .and_then(|value| value.l().ok())
                .filter(|value| !value.is_null())
                .map(JIntArray::from);
            let mut map = image
                .pixels
                .iter()
                .map(|color| *color as i32)
                .collect::<Vec<_>>();
            let mut map_width = image.width;
            let mut map_height = image.height;
            if let Some(bitmap) = bitmap
                && let Some(bitmap) = read_bitmap(env, &bitmap)
            {
                map = bitmap
                    .pixels
                    .into_iter()
                    .map(|color| color as i32)
                    .collect();
                map_width = bitmap.width;
                map_height = bitmap.height;
            } else if let Some(array) = array {
                let length = env.get_array_length(&array).unwrap_or(0) as usize;
                let mut supplied = vec![0; length];
                if env.get_int_array_region(&array, 0, &mut supplied).is_ok() {
                    map = supplied;
                    map_width = int_field(env, filter, "dw", image.width as i32).max(1) as usize;
                    map_height = int_field(env, filter, "dh", image.height as i32).max(1) as usize;
                }
            } else {
                let _ = env.exception_clear();
            }
            if map.len() >= map_width * map_height {
                let luminance = map
                    .iter()
                    .map(|color| brightness(*color as u32) / 8)
                    .collect::<Vec<_>>();
                let amount = float_param(env, filter, "getAmount", 1.0);
                let source = image.pixels.clone();
                for y in 0..image.height {
                    for x in 0..image.width {
                        let mx = x % map_width;
                        let my = y % map_height;
                        let left = (mx + map_width - 1) % map_width;
                        let right = (mx + 1) % map_width;
                        let top = (my + map_height - 1) % map_height;
                        let bottom = (my + 1) % map_height;
                        let x_displacement = luminance[top * map_width + left]
                            + luminance[my * map_width + left]
                            + luminance[bottom * map_width + left]
                            - luminance[top * map_width + right]
                            - luminance[my * map_width + right]
                            - luminance[bottom * map_width + right];
                        let y_displacement = luminance[bottom * map_width + left]
                            + luminance[bottom * map_width + mx]
                            + luminance[bottom * map_width + right]
                            - luminance[top * map_width + left]
                            - luminance[top * map_width + mx]
                            - luminance[top * map_width + right];
                        image.pixels[y * image.width + x] = bilinear(
                            &source,
                            image.width,
                            image.height,
                            x as f32 + amount * x_displacement as f32,
                            y as f32 + amount * y_displacement as f32,
                            3,
                        );
                    }
                }
            }
        }
        "MapFilter" => {
            let x_function = env
                .call_method(
                    filter,
                    "getXMapFunction",
                    "()Lcom/jhlabs/math/Function2D;",
                    &[],
                )
                .ok()
                .and_then(|value| value.l().ok());
            let y_function = env
                .call_method(
                    filter,
                    "getYMapFunction",
                    "()Lcom/jhlabs/math/Function2D;",
                    &[],
                )
                .ok()
                .and_then(|value| value.l().ok());
            if let (Some(x_function), Some(y_function)) = (x_function, y_function) {
                let source = image.pixels.clone();
                for y in 0..image.height {
                    for x in 0..image.width {
                        let arguments = [JValue::Float(x as f32), JValue::Float(y as f32)];
                        let sx = env
                            .call_method(&x_function, "evaluate", "(FF)F", &arguments)
                            .ok()
                            .and_then(|value| value.f().ok())
                            .unwrap_or(x as f32 / image.width as f32)
                            * image.width as f32;
                        let sy = env
                            .call_method(&y_function, "evaluate", "(FF)F", &arguments)
                            .ok()
                            .and_then(|value| value.f().ok())
                            .unwrap_or(y as f32 / image.height as f32)
                            * image.height as f32;
                        image.pixels[y * image.width + x] =
                            bilinear(&source, image.width, image.height, sx, sy, 3);
                    }
                }
            } else {
                let _ = env.exception_clear();
            }
        }
        "FieldWarpFilter" => {
            image.pixels = field_warp_exact(env, filter, &image.pixels, image.width, image.height);
        }
        "BoxBlurFilter" => {
            image.pixels = box_blur_exact(
                &image.pixels,
                image.width,
                image.height,
                float_param(env, filter, "getHRadius", 0.0),
                float_param(env, filter, "getVRadius", 0.0),
                int_param(env, filter, "getIterations", 1),
                bool_param(env, filter, "getPremultiplyAlpha", true),
            );
        }
        "SmartBlurFilter" => {
            let kernel = gaussian_kernel_exact(int_param(env, filter, "getHRadius", 5) as f32);
            let threshold = int_param(env, filter, "getThreshold", 10);
            let first = threshold_blur_transpose_exact(
                &image.pixels,
                image.width,
                image.height,
                &kernel,
                threshold,
            );
            image.pixels = threshold_blur_transpose_exact(
                &first,
                image.height,
                image.width,
                &kernel,
                threshold,
            );
        }
        "BlockFilter" => {
            let size = int_param(env, filter, "getBlockSize", 2).max(1) as usize;
            for top in (0..image.height).step_by(size) {
                for left in (0..image.width).step_by(size) {
                    let block_width = size.min(image.width - left);
                    let block_height = size.min(image.height - top);
                    let count = block_width * block_height;
                    let mut totals = [0_usize; 3];
                    for y in top..top + block_height {
                        for x in left..left + block_width {
                            let color = image.pixels[y * image.width + x];
                            totals[0] += red(color) as usize;
                            totals[1] += green(color) as usize;
                            totals[2] += blue(color) as usize;
                        }
                    }
                    for y in top..(top + size).min(image.height) {
                        for x in left..(left + size).min(image.width) {
                            let color = image.pixels[y * image.width + x];
                            image.pixels[y * image.width + x] = argb(
                                alpha(color),
                                (totals[0] / count) as u8,
                                (totals[1] / count) as u8,
                                (totals[2] / count) as u8,
                            );
                        }
                    }
                }
            }
        }
        "FlipFilter" => {
            let operation = int_param(env, filter, "getOperation", 1);
            let source = image.pixels.clone();
            let source_width = image.width;
            let source_height = image.height;
            let (output_width, output_height) = if matches!(operation, 3..=5) {
                (source_height, source_width)
            } else {
                (source_width, source_height)
            };
            let mut output = vec![0; output_width * output_height];
            for row in 0..source_height {
                for column in 0..source_width {
                    let (new_row, new_column) = match operation {
                        1 => (row, source_width - column - 1),
                        2 => (source_height - row - 1, column),
                        3 => (column, row),
                        4 => (column, source_height - row - 1),
                        5 => (source_width - column - 1, row),
                        6 => (source_height - row - 1, source_width - column - 1),
                        _ => (row, column),
                    };
                    output[new_row * output_width + new_column] =
                        source[row * source_width + column];
                }
            }
            image.pixels = output;
            image.width = output_width;
            image.height = output_height;
        }
        _ => {}
    }
    image
}

fn null_object() -> jobject {
    ptr::null_mut()
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_com_jhlabs_NativeJhlabs_filter(
    mut env: JNIEnv,
    _class: JObject,
    bitmap: JObject,
    filter: JObject,
) -> jobject {
    catch_unwind(AssertUnwindSafe(|| {
        let Some(name) = class_name(&mut env, &filter) else {
            return null_object();
        };
        let Some(image) = read_bitmap(&mut env, &bitmap) else {
            return null_object();
        };
        let output = apply_filter(&mut env, &filter, &name, image);
        create_bitmap(&mut env, &output.pixels, output.width, output.height)
            .map(JObject::into_raw)
            .unwrap_or_else(null_object)
    }))
    .unwrap_or_else(|_| null_object())
}
