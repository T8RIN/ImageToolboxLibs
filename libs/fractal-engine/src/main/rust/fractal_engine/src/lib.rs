#[cfg(any(target_os = "android", test))]
mod math;
#[cfg(any(target_os = "android", test))]
mod render;

#[cfg(target_os = "android")]
mod android;
