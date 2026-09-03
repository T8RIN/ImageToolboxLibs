use crate::math::{Quaternion, Vec3};

pub(crate) fn octahedral_ifs(point: Vec3, object_scale: f64, fold: f64, iterations: usize) -> f64 {
    let mut point = point / object_scale;
    let scale = fold + 1.0;
    let mut derivative = 1.0;
    for _ in 0..iterations.clamp(4, 18) {
        point = point.abs();
        sort_descending(&mut point);
        point = point * scale - Vec3::splat(scale - 1.0);
        derivative *= scale;
        if point.length() > 100.0 {
            break;
        }
    }
    (point.length() - 2.0) / derivative.abs().max(1.0e-12) * object_scale
}

pub(crate) fn icosahedral_ifs(point: Vec3, object_scale: f64, fold: f64, iterations: usize) -> f64 {
    let mut point = point / object_scale;
    let scale = fold + 1.0;
    let phi = (1.0 + 5.0_f64.sqrt()) * 0.5;
    let normals = [
        Vec3::new(1.0, phi, 0.0).normalized(),
        Vec3::new(0.0, 1.0, phi).normalized(),
        Vec3::new(phi, 0.0, 1.0).normalized(),
        Vec3::new(-1.0, phi, 0.0).normalized(),
        Vec3::new(0.0, -1.0, phi).normalized(),
    ];
    let mut derivative = 1.0;
    for _ in 0..iterations.clamp(4, 18) {
        for normal in normals {
            let distance = point.dot(normal);
            if distance < 0.0 {
                point = point - normal * (2.0 * distance);
            }
        }
        point = point.abs();
        sort_descending(&mut point);
        point = point * scale - Vec3::splat(scale - 1.0);
        derivative *= scale;
        if point.length() > 100.0 {
            break;
        }
    }
    (point.length() - 2.0) / derivative.abs().max(1.0e-12) * object_scale
}

pub(crate) fn apollonian_gasket(
    point: Vec3,
    object_scale: f64,
    fold: f64,
    packing: f64,
    iterations: usize,
) -> f64 {
    let mut point = point / object_scale;
    let scale = (1.0 + packing) * fold;
    let mut derivative = 1.0;
    for _ in 0..iterations.clamp(4, 18) {
        point = point.abs();
        let radius_squared = point.dot(point).max(1.0e-15);
        let packing_squared = packing * packing;
        if radius_squared < packing_squared {
            let factor = 1.0 / packing_squared;
            point = point * factor;
            derivative *= factor;
        } else if radius_squared < 1.0 {
            let factor = 1.0 / radius_squared;
            point = point * factor;
            derivative *= factor;
        }
        sort_descending(&mut point);
        point = point * scale - Vec3::splat(packing);
        derivative *= scale;
        if point.length() > 100.0 {
            break;
        }
    }
    (point.length() - 1.0) / derivative.abs().max(1.0e-12) * object_scale
}

pub(crate) fn kleinian(
    point: Vec3,
    object_scale: f64,
    fold: f64,
    minimum_radius: f64,
    iterations: usize,
) -> f64 {
    let mut point = point / object_scale;
    let scale = fold + 1.0;
    let size = Vec3::new(1.0, 1.0, 1.3);
    let offset = Vec3::new(0.0, 0.0, -minimum_radius);
    let mut derivative = 1.0;
    for _ in 0..iterations.clamp(6, 16) {
        point = point.abs();
        sort_descending(&mut point);
        point = point * scale - size * (scale - 1.0);
        let radius_squared = point.dot(point).max(1.0e-15);
        let inversion = (1.0 / radius_squared).clamp(1.0, 3.0);
        point = point * inversion + offset;
        derivative *= inversion * scale;
    }
    (point.length() - 0.4) / derivative.abs().max(1.0e-12) * object_scale
}

pub(crate) fn hybrid_mandelbulb_julia(
    point: Vec3,
    power: f64,
    julia: Vec3,
    iterations: usize,
) -> f64 {
    let power = power.clamp(2.0, 16.0);
    let mut value = point;
    let mut derivative = 1.0;
    let mut radius = value.length();
    for iteration in 0..iterations.clamp(6, 24) {
        if radius > 2.0 {
            break;
        }
        if radius < 1.0e-12 {
            return 0.0;
        }
        let theta = (value.z / radius).clamp(-1.0, 1.0).acos();
        let phi = value.y.atan2(value.x);
        derivative = radius.powf(power - 1.0) * power * derivative + 1.0;
        let powered_radius = radius.powf(power);
        let powered_theta = theta * power;
        let powered_phi = phi * power;
        value = Vec3::new(
            powered_theta.sin() * powered_phi.cos(),
            powered_theta.sin() * powered_phi.sin(),
            powered_theta.cos(),
        ) * powered_radius
            + if iteration & 1 == 0 { point } else { julia };
        radius = value.length();
    }
    0.5 * radius.max(1.0e-12).ln() * radius / derivative.abs().max(1.0e-12)
}

pub(crate) fn quaternion_cubic(point: Vec3, constant: Quaternion, iterations: usize) -> f64 {
    // The catalog formula stores quaternions scalar-first. The shared math type is scalar-last.
    let constant = Quaternion::new(constant.y, constant.z, constant.w, constant.x);
    let mut value = Quaternion::new(point.y, point.z, 0.0, point.x);
    let mut derivative = Quaternion::new(0.0, 0.0, 0.0, 1.0);
    let mut radius_squared = value.norm_squared();
    for _ in 0..iterations.clamp(6, 24) {
        let radius = radius_squared.sqrt();
        if radius > 2.0 {
            break;
        }
        let squared = quaternion_multiply(value, value);
        derivative = quaternion_multiply(
            quaternion_multiply(Quaternion::new(0.0, 0.0, 0.0, 3.0), squared),
            derivative,
        );
        value = quaternion_multiply(value, squared) + constant;
        radius_squared = value.norm_squared();
    }
    let radius = radius_squared.sqrt().max(1.0e-12);
    0.5 * radius.ln() * radius / derivative.norm_squared().sqrt().max(1.0e-12)
}

pub(crate) fn sierpinski_gasket(
    point: Vec3,
    object_scale: f64,
    fold: f64,
    minimum_radius: f64,
    iterations: usize,
) -> f64 {
    let mut point = point / object_scale;
    let scale_factor = 2.0 + fold * 0.5;
    let minimum_radius_squared = minimum_radius.powi(2);
    let mut scale = 1.0;
    for _ in 0..iterations.clamp(5, 16) {
        tetrahedral_fold(&mut point);
        let radius_squared = point.dot(point).max(1.0e-15);
        if radius_squared < minimum_radius_squared {
            let inversion = minimum_radius_squared / radius_squared;
            point = point * inversion;
            scale *= inversion;
        }
        point = point * scale_factor - Vec3::splat(scale_factor - 1.0);
        scale *= scale_factor;
    }
    (point.length() - 1.0) / scale.abs().max(1.0e-12) * object_scale
}

fn tetrahedral_fold(point: &mut Vec3) {
    if point.x + point.y < 0.0 {
        let old_x = point.x;
        point.x = -point.y;
        point.y = -old_x;
    }
    if point.x + point.z < 0.0 {
        let old_x = point.x;
        point.x = -point.z;
        point.z = -old_x;
    }
    if point.y + point.z < 0.0 {
        let old_y = point.y;
        point.y = -point.z;
        point.z = -old_y;
    }
}

fn sort_descending(point: &mut Vec3) {
    if point.x < point.y {
        std::mem::swap(&mut point.x, &mut point.y);
    }
    if point.x < point.z {
        std::mem::swap(&mut point.x, &mut point.z);
    }
    if point.y < point.z {
        std::mem::swap(&mut point.y, &mut point.z);
    }
}

fn quaternion_multiply(first: Quaternion, second: Quaternion) -> Quaternion {
    Quaternion::new(
        first.w * second.x + second.w * first.x + first.y * second.z - first.z * second.y,
        first.w * second.y + second.w * first.y + first.z * second.x - first.x * second.z,
        first.w * second.z + second.w * first.z + first.x * second.y - first.y * second.x,
        first.w * second.w - first.x * second.x - first.y * second.y - first.z * second.z,
    )
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn new_distance_estimators_are_finite_and_distinct() {
        let point = Vec3::new(0.37, -0.21, 0.54);
        let values = [
            octahedral_ifs(point, 2.0, 1.2, 10),
            icosahedral_ifs(point, 1.7, 1.5, 10),
            apollonian_gasket(point, 1.3, 1.35, 1.12, 10),
            kleinian(point, 1.0, 1.2, 1.5, 10),
            hybrid_mandelbulb_julia(point, 8.0, Vec3::new(-0.2, 0.8, 0.3), 12),
            quaternion_cubic(point, Quaternion::new(-0.2, 0.6, 0.3, 0.0), 12),
            sierpinski_gasket(point, 1.5, 1.0, 0.5, 10),
        ];
        assert!(values.iter().all(|value| value.is_finite()));
        let rounded: std::collections::HashSet<_> = values
            .iter()
            .map(|value| (value * 1.0e9).round() as i64)
            .collect();
        assert_eq!(values.len(), rounded.len());
    }

    #[test]
    fn quaternion_cubic_matches_scalar_first_reference_sample() {
        let distance = quaternion_cubic(
            Vec3::new(0.37, -0.21, 0.54),
            Quaternion::new(-0.2, 0.6, 0.3, 0.0),
            16,
        );
        assert!(
            (distance - 0.021641987147598706).abs() < 1.0e-12,
            "{distance}"
        );
    }

    #[test]
    fn hybrid_matches_alternating_reference_samples() {
        let julia = Vec3::new(-0.2, 0.8, 0.3);
        let derivative_sample =
            hybrid_mandelbulb_julia(Vec3::new(0.37, -0.21, 0.54), 8.0, julia, 12);
        assert!(
            (derivative_sample - -0.016340226140528893).abs() < 1.0e-12,
            "{derivative_sample}"
        );
        let bailout_sample = hybrid_mandelbulb_julia(Vec3::new(-1.5, -1.5, -1.3), 8.0, julia, 12);
        assert!(
            (bailout_sample - 1.1338524354114028).abs() < 1.0e-12,
            "{bailout_sample}"
        );
    }
}
