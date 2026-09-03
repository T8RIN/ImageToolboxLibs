#[cfg(any(target_os = "android", test))]
mod attractors;
#[cfg(any(target_os = "android", test))]
mod decimal;
#[cfg(any(target_os = "android", test))]
mod distance_estimators;
#[cfg(any(target_os = "android", test))]
mod math;
#[cfg(any(target_os = "android", test))]
mod render;

#[cfg(target_os = "android")]
mod android;
