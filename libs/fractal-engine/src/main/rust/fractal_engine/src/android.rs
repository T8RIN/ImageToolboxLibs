use std::collections::HashMap;
use std::ffi::c_void;
use std::panic::{AssertUnwindSafe, catch_unwind};
use std::ptr;
use std::sync::atomic::{AtomicBool, AtomicI64, Ordering};
use std::sync::{Arc, Mutex, MutexGuard, OnceLock};

use jni::JNIEnv;
use jni::objects::{JDoubleArray, JIntArray, JObject, JString};
use jni::sys::{jint, jlong, jobject, jstring};

use crate::render::{BitmapAlphaMode, RenderOutcome, RenderSettings, render_into};

const NATIVE_API_VERSION: jint = 1;
const SOURCE_CHECKSUM: &str = env!("FRACTAL_ENGINE_SOURCE_CHECKSUM");
const RESULT_COMPLETED: jint = 0;
const RESULT_CANCELLED: jint = 1;
const ERROR_INVALID_SESSION: jint = -1;
const ERROR_BITMAP: jint = -2;
const ERROR_UNSUPPORTED_TYPE: jint = -3;
const ERROR_INVALID_ARGUMENT: jint = -4;
const ERROR_SESSION_CONSUMED: jint = -5;
const ERROR_INTERNAL: jint = -6;
const ERROR_WORK_LIMIT: jint = -7;
const ANDROID_BITMAP_FORMAT_RGBA_8888: i32 = 1;
const ANDROID_BITMAP_FLAGS_ALPHA_MASK: u32 = 0x3;
const ANDROID_BITMAP_FLAGS_ALPHA_PREMUL: u32 = 0;
const ANDROID_BITMAP_FLAGS_ALPHA_OPAQUE: u32 = 1;
const ANDROID_BITMAP_FLAGS_ALPHA_UNPREMUL: u32 = 2;

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

struct Operation {
    cancelled: AtomicBool,
    started: AtomicBool,
}

impl Operation {
    fn new() -> Self {
        Self {
            cancelled: AtomicBool::new(false),
            started: AtomicBool::new(false),
        }
    }
}

struct LockedBitmap {
    env: *mut jni::sys::JNIEnv,
    bitmap: jobject,
    pixels: *mut u8,
    info: AndroidBitmapInfo,
}

impl LockedBitmap {
    fn new(env: &JNIEnv, bitmap: &JObject) -> Option<Self> {
        let mut info = AndroidBitmapInfo {
            width: 0,
            height: 0,
            stride: 0,
            format: 0,
            flags: 0,
        };
        if unsafe { AndroidBitmap_getInfo(env.get_raw(), bitmap.as_raw(), &mut info) } != 0
            || info.format != ANDROID_BITMAP_FORMAT_RGBA_8888
            || info.width == 0
            || info.height == 0
            || info.stride < info.width.checked_mul(4)?
        {
            return None;
        }

        let mut address = ptr::null_mut();
        if unsafe { AndroidBitmap_lockPixels(env.get_raw(), bitmap.as_raw(), &mut address) } != 0
            || address.is_null()
        {
            return None;
        }
        Some(Self {
            env: env.get_raw(),
            bitmap: bitmap.as_raw(),
            pixels: address.cast(),
            info,
        })
    }

    fn pixels(&mut self) -> Option<&mut [u8]> {
        let length = (self.info.stride as usize).checked_mul(self.info.height as usize)?;
        Some(unsafe { std::slice::from_raw_parts_mut(self.pixels, length) })
    }

    fn alpha_mode(&self) -> BitmapAlphaMode {
        match self.info.flags & ANDROID_BITMAP_FLAGS_ALPHA_MASK {
            ANDROID_BITMAP_FLAGS_ALPHA_OPAQUE => BitmapAlphaMode::Opaque,
            ANDROID_BITMAP_FLAGS_ALPHA_UNPREMUL => BitmapAlphaMode::Unpremultiplied,
            ANDROID_BITMAP_FLAGS_ALPHA_PREMUL => BitmapAlphaMode::Premultiplied,
            _ => BitmapAlphaMode::Premultiplied,
        }
    }
}

impl Drop for LockedBitmap {
    fn drop(&mut self) {
        unsafe {
            AndroidBitmap_unlockPixels(self.env, self.bitmap);
        }
    }
}

static OPERATIONS: OnceLock<Mutex<HashMap<i64, Arc<Operation>>>> = OnceLock::new();
static NEXT_OPERATION_ID: AtomicI64 = AtomicI64::new(1);

fn operations() -> &'static Mutex<HashMap<i64, Arc<Operation>>> {
    OPERATIONS.get_or_init(|| Mutex::new(HashMap::new()))
}

fn lock_operations() -> MutexGuard<'static, HashMap<i64, Arc<Operation>>> {
    operations()
        .lock()
        .unwrap_or_else(|poisoned| poisoned.into_inner())
}

fn operation(operation_id: i64) -> Option<Arc<Operation>> {
    lock_operations().get(&operation_id).cloned()
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_com_t8rin_fractal_1engine_NativeFractalEngine_nativeApiVersion(
    _env: JNIEnv,
    _object: JObject,
) -> jint {
    catch_unwind(AssertUnwindSafe(|| NATIVE_API_VERSION)).unwrap_or(0)
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_com_t8rin_fractal_1engine_NativeFractalEngine_nativeSourceChecksum(
    env: JNIEnv,
    _object: JObject,
) -> jstring {
    catch_unwind(AssertUnwindSafe(|| {
        env.new_string(SOURCE_CHECKSUM)
            .map(JString::into_raw)
            .unwrap_or(ptr::null_mut())
    }))
    .unwrap_or(ptr::null_mut())
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_com_t8rin_fractal_1engine_NativeFractalEngine_nativeCreateSession(
    _env: JNIEnv,
    _object: JObject,
) -> jlong {
    catch_unwind(AssertUnwindSafe(|| {
        let operation_id = NEXT_OPERATION_ID.fetch_add(1, Ordering::Relaxed);
        if operation_id <= 0 {
            return 0;
        }
        lock_operations().insert(operation_id, Arc::new(Operation::new()));
        operation_id as jlong
    }))
    .unwrap_or(0)
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_com_t8rin_fractal_1engine_NativeFractalEngine_nativeCancel(
    _env: JNIEnv,
    _object: JObject,
    operation_id: jlong,
) {
    let _ = catch_unwind(AssertUnwindSafe(|| {
        if let Some(operation) = operation(operation_id) {
            operation.cancelled.store(true, Ordering::Release);
        }
    }));
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_com_t8rin_fractal_1engine_NativeFractalEngine_nativeDestroySession(
    _env: JNIEnv,
    _object: JObject,
    operation_id: jlong,
) {
    let _ = catch_unwind(AssertUnwindSafe(|| {
        if let Some(operation) = lock_operations().remove(&operation_id) {
            operation.cancelled.store(true, Ordering::Release);
        }
    }));
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_com_t8rin_fractal_1engine_NativeFractalEngine_nativeRenderInto(
    mut env: JNIEnv,
    _object: JObject,
    operation_id: jlong,
    bitmap: JObject,
    type_id: jint,
    max_iterations: jint,
    parameters: JDoubleArray,
    palette: JIntArray,
    lyapunov_sequence: JString,
) -> jint {
    catch_unwind(AssertUnwindSafe(|| {
        render_into_bitmap(
            &mut env,
            operation_id,
            &bitmap,
            type_id,
            max_iterations,
            &parameters,
            &palette,
            &lyapunov_sequence,
        )
    }))
    .unwrap_or(ERROR_INTERNAL)
}

#[allow(clippy::too_many_arguments)]
fn render_into_bitmap(
    env: &mut JNIEnv,
    operation_id: i64,
    bitmap: &JObject,
    type_id: i32,
    max_iterations: i32,
    parameters: &JDoubleArray,
    palette: &JIntArray,
    lyapunov_sequence: &JString,
) -> jint {
    let Some(operation) = operation(operation_id) else {
        return ERROR_INVALID_SESSION;
    };
    if operation
        .started
        .compare_exchange(false, true, Ordering::AcqRel, Ordering::Acquire)
        .is_err()
    {
        return ERROR_SESSION_CONSUMED;
    }
    if operation.cancelled.load(Ordering::Acquire) {
        return RESULT_CANCELLED;
    }
    if crate::render::FractalKind::from_stable_id(type_id).is_none() {
        return ERROR_UNSUPPORTED_TYPE;
    }

    let Ok(parameter_count) = env.get_array_length(parameters) else {
        return ERROR_INVALID_ARGUMENT;
    };
    let Ok(palette_count) = env.get_array_length(palette) else {
        return ERROR_INVALID_ARGUMENT;
    };
    if parameter_count != crate::render::REQUIRED_PARAMETER_COUNT as i32
        || !(2..=64).contains(&palette_count)
    {
        return ERROR_INVALID_ARGUMENT;
    }
    let mut parameter_values = vec![0.0_f64; parameter_count as usize];
    let mut palette_values = vec![0_i32; palette_count as usize];
    if env
        .get_double_array_region(parameters, 0, &mut parameter_values)
        .is_err()
        || env
            .get_int_array_region(palette, 0, &mut palette_values)
            .is_err()
    {
        return ERROR_INVALID_ARGUMENT;
    }
    let Ok(sequence) = env.get_string(lyapunov_sequence) else {
        return ERROR_INVALID_ARGUMENT;
    };
    let sequence: String = sequence.into();
    let Some(settings) = RenderSettings::from_wire(
        type_id,
        max_iterations,
        &parameter_values,
        &palette_values,
        &sequence,
    ) else {
        return ERROR_INVALID_ARGUMENT;
    };

    let Some(mut locked_bitmap) = LockedBitmap::new(env, bitmap) else {
        return ERROR_BITMAP;
    };
    let width = locked_bitmap.info.width as usize;
    let height = locked_bitmap.info.height as usize;
    let stride = locked_bitmap.info.stride as usize;
    let alpha_mode = locked_bitmap.alpha_mode();
    if !settings.is_within_work_limit(width, height) {
        return ERROR_WORK_LIMIT;
    }
    let Some(pixels) = locked_bitmap.pixels() else {
        return ERROR_BITMAP;
    };
    match render_into(
        &settings,
        pixels,
        width,
        height,
        stride,
        alpha_mode,
        &operation.cancelled,
    ) {
        Some(RenderOutcome::Completed) => RESULT_COMPLETED,
        Some(RenderOutcome::Cancelled) => RESULT_CANCELLED,
        None => ERROR_BITMAP,
    }
}
