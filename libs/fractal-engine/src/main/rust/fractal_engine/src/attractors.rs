use std::sync::atomic::{AtomicBool, Ordering};

use crate::math::{Complex, Vec3};

#[derive(Clone, Copy)]
pub(crate) struct DensityViewport {
    pub(crate) width: usize,
    pub(crate) height: usize,
    pub(crate) center_x: f64,
    pub(crate) center_y: f64,
    pub(crate) vertical_span: f64,
    pub(crate) aspect_ratio: f64,
}

#[derive(Clone, Copy)]
pub(crate) enum Attractor2dKind {
    Hopalong,
    Martin,
    Gingerbreadman,
    Chip,
    Quadruptwo,
    Threeply,
}

#[derive(Clone, Copy)]
pub(crate) struct Attractor2dParameters {
    pub(crate) a: f64,
    pub(crate) b: f64,
    pub(crate) c: f64,
}

#[derive(Clone, Copy)]
pub(crate) enum Attractor3dKind {
    Pickover,
    Lorenz,
    Rossler,
}

#[derive(Clone, Copy)]
pub(crate) struct Attractor3dParameters {
    pub(crate) a: f64,
    pub(crate) b: f64,
    pub(crate) c: f64,
    pub(crate) d: f64,
}

#[derive(Clone, Copy, Debug, Eq, PartialEq)]
pub(crate) enum DensityError {
    Cancelled,
    AllocationFailed,
}

pub(crate) fn try_zeroed_density(pixel_count: usize) -> Result<Vec<u32>, DensityError> {
    let mut density = Vec::new();
    density
        .try_reserve_exact(pixel_count)
        .map_err(|_| DensityError::AllocationFailed)?;
    density.resize(pixel_count, 0);
    Ok(density)
}

pub(crate) fn buddhabrot_density(
    viewport: DensityViewport,
    max_iterations: usize,
    sample_count: usize,
    cancelled: &AtomicBool,
) -> Result<Vec<u32>, DensityError> {
    let pixel_count = viewport
        .width
        .checked_mul(viewport.height)
        .ok_or(DensityError::AllocationFailed)?;
    let mut density = try_zeroed_density(pixel_count)?;
    let orbit_limit = max_iterations.max(32);
    let minimum_escape_iterations = 20.max(orbit_limit / 10);
    let mut orbit = Vec::new();
    orbit
        .try_reserve_exact(orbit_limit)
        .map_err(|_| DensityError::AllocationFailed)?;

    for sample_index in 1..=sample_count {
        if sample_index & 63 == 0 && cancelled.load(Ordering::Acquire) {
            return Err(DensityError::Cancelled);
        }
        let c = Complex::new(
            -2.5 + 3.5 * halton(sample_index as u64, 2),
            -1.5 + 3.0 * halton(sample_index as u64, 3),
        );
        if is_inside_mandelbrot_main_bulbs(c) {
            continue;
        }
        orbit.clear();
        let mut z = Complex::default();
        let mut escape_iterations = None;
        for iteration in 0..orbit_limit {
            if iteration & 255 == 0 && cancelled.load(Ordering::Acquire) {
                return Err(DensityError::Cancelled);
            }
            z = z * z + c;
            orbit.push(z);
            if !z.is_finite() || z.norm_squared() > 4.0 {
                escape_iterations = Some(iteration + 1);
                break;
            }
        }
        if escape_iterations.is_some_and(|iterations| iterations >= minimum_escape_iterations) {
            for point in &orbit {
                plot_density(&mut density, viewport, point.im, -point.re, 1);
            }
        }
    }
    Ok(density)
}

pub(crate) fn attractor_2d_density(
    kind: Attractor2dKind,
    parameters: Attractor2dParameters,
    viewport: DensityViewport,
    steps: usize,
    cancelled: &AtomicBool,
) -> Result<Vec<u32>, DensityError> {
    let pixel_count = viewport
        .width
        .checked_mul(viewport.height)
        .ok_or(DensityError::AllocationFailed)?;
    let mut density = try_zeroed_density(pixel_count)?;
    let (mut x, mut y) = if matches!(kind, Attractor2dKind::Gingerbreadman) {
        (-0.1, 0.0)
    } else {
        (0.1, 0.0)
    };

    for _ in 0..100 {
        (x, y) = attractor_2d_step(kind, x, y, parameters);
    }
    for iteration in 0..steps {
        if iteration & 255 == 0 && cancelled.load(Ordering::Acquire) {
            return Err(DensityError::Cancelled);
        }
        (x, y) = attractor_2d_step(kind, x, y, parameters);
        if !x.is_finite() || !y.is_finite() || x.abs() > 1.0e7 || y.abs() > 1.0e7 {
            break;
        }
        plot_density(&mut density, viewport, x, y, 2);
    }
    Ok(density)
}

pub(crate) fn for_each_attractor_3d_point(
    kind: Attractor3dKind,
    parameters: Attractor3dParameters,
    steps: usize,
    cancelled: &AtomicBool,
    mut visit: impl FnMut(Vec3),
) -> Result<(), DensityError> {
    let transient = match kind {
        Attractor3dKind::Pickover => 500,
        Attractor3dKind::Lorenz => 1_000,
        Attractor3dKind::Rossler => 2_000,
    };
    let mut point = match kind {
        Attractor3dKind::Pickover => Vec3::new(0.1, 0.1, 0.1),
        Attractor3dKind::Lorenz | Attractor3dKind::Rossler => Vec3::new(0.1, 0.0, 0.0),
    };
    for iteration in 0..transient {
        if iteration & 255 == 0 && cancelled.load(Ordering::Acquire) {
            return Err(DensityError::Cancelled);
        }
        point = attractor_3d_step(kind, point, parameters);
    }

    for iteration in 0..steps {
        if iteration & 255 == 0 && cancelled.load(Ordering::Acquire) {
            return Err(DensityError::Cancelled);
        }
        point = attractor_3d_step(kind, point, parameters);
        if !point.x.is_finite() || !point.y.is_finite() || !point.z.is_finite() {
            break;
        }
        visit(match kind {
            Attractor3dKind::Pickover => point * 0.3,
            Attractor3dKind::Lorenz => Vec3::new(point.x, point.y, point.z - 25.0) * 0.05,
            Attractor3dKind::Rossler => point * 0.1,
        });
    }
    Ok(())
}

fn attractor_2d_step(
    kind: Attractor2dKind,
    x: f64,
    y: f64,
    parameters: Attractor2dParameters,
) -> (f64, f64) {
    let sign = x.signum();
    match kind {
        Attractor2dKind::Hopalong => (
            y - sign * (parameters.b * x - parameters.c).abs().sqrt(),
            parameters.a - x,
        ),
        Attractor2dKind::Martin => (y - x.sin(), parameters.a - x),
        Attractor2dKind::Gingerbreadman => (1.0 - y + x.abs(), x),
        Attractor2dKind::Chip => {
            let first = (parameters.b * x - parameters.c).abs().max(0.001).ln();
            let second = (parameters.c * x - parameters.b).abs().max(0.001).ln();
            (
                y - sign * (first * first).cos() * (second * second).atan(),
                parameters.a - x,
            )
        }
        Attractor2dKind::Quadruptwo => {
            let first = (parameters.b * x - parameters.c).abs().max(0.001).ln();
            let second = (parameters.c * x - parameters.b).abs();
            (
                y - sign * first.sin() * (second * second).atan(),
                parameters.a - x,
            )
        }
        Attractor2dKind::Threeply => {
            let term = x.sin() * parameters.b.cos() + parameters.c
                - x * (parameters.a + parameters.b + parameters.c).sin();
            (y - sign * term.abs(), parameters.a - x)
        }
    }
}

fn attractor_3d_step(
    kind: Attractor3dKind,
    point: Vec3,
    parameters: Attractor3dParameters,
) -> Vec3 {
    match kind {
        Attractor3dKind::Pickover => Vec3::new(
            (parameters.a * point.y).sin() - point.z * (parameters.b * point.x).cos(),
            point.z * (parameters.c * point.x).sin() - (parameters.d * point.y).cos(),
            point.x.sin(),
        ),
        Attractor3dKind::Lorenz => {
            let dt = 0.005;
            Vec3::new(
                point.x + parameters.a * (point.y - point.x) * dt,
                point.y + (point.x * (parameters.b - point.z) - point.y) * dt,
                point.z + (point.x * point.y - parameters.c * point.z) * dt,
            )
        }
        Attractor3dKind::Rossler => {
            let dt = 0.02;
            Vec3::new(
                point.x + (-point.y - point.z) * dt,
                point.y + (point.x + parameters.a * point.y) * dt,
                point.z + (parameters.b + point.z * (point.x - parameters.c)) * dt,
            )
        }
    }
}

fn plot_density(density: &mut [u32], viewport: DensityViewport, x: f64, y: f64, radius: usize) {
    let horizontal_span = viewport.vertical_span * viewport.aspect_ratio;
    let normalized_x = (x - (viewport.center_x - horizontal_span * 0.5)) / horizontal_span;
    let normalized_y =
        ((viewport.center_y + viewport.vertical_span * 0.5) - y) / viewport.vertical_span;
    if !(0.0..1.0).contains(&normalized_x) || !(0.0..1.0).contains(&normalized_y) {
        return;
    }
    let center_x = (normalized_x * viewport.width as f64) as isize;
    let center_y = (normalized_y * viewport.height as f64) as isize;
    let radius = radius as isize;
    for offset_y in -radius..=radius {
        for offset_x in -radius..=radius {
            if offset_x * offset_x + offset_y * offset_y > radius * radius {
                continue;
            }
            let pixel_x = center_x + offset_x;
            let pixel_y = center_y + offset_y;
            if pixel_x >= 0
                && pixel_y >= 0
                && pixel_x < viewport.width as isize
                && pixel_y < viewport.height as isize
            {
                let index = pixel_y as usize * viewport.width + pixel_x as usize;
                density[index] = density[index].saturating_add(1);
            }
        }
    }
}

fn halton(mut index: u64, base: u64) -> f64 {
    let mut fraction = 1.0;
    let mut result = 0.0;
    while index > 0 {
        fraction /= base as f64;
        result += fraction * (index % base) as f64;
        index /= base;
    }
    result
}

fn is_inside_mandelbrot_main_bulbs(point: Complex) -> bool {
    let x = point.re;
    let y_squared = point.im * point.im;
    let shifted = x - 0.25;
    let q = shifted * shifted + y_squared;
    q * (q + shifted) <= 0.25 * y_squared || (x + 1.0) * (x + 1.0) + y_squared <= 0.0625
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn halton_sequence_is_deterministic_and_bounded() {
        let first: Vec<_> = (1..32).map(|index| halton(index, 2)).collect();
        let second: Vec<_> = (1..32).map(|index| halton(index, 2)).collect();
        assert_eq!(first, second);
        assert!(first.into_iter().all(|value| (0.0..1.0).contains(&value)));
    }

    #[test]
    fn density_allocation_failure_is_explicit() {
        assert_eq!(
            try_zeroed_density(usize::MAX),
            Err(DensityError::AllocationFailed)
        );
    }

    #[test]
    fn four_k_density_allocation_succeeds() {
        let density = try_zeroed_density(3_840 * 2_160).expect("4K density buffer");
        assert_eq!(density.len(), 3_840 * 2_160);
    }

    #[test]
    fn every_planar_attractor_produces_finite_motion() {
        let cases = [
            (
                Attractor2dKind::Hopalong,
                Attractor2dParameters {
                    a: 0.4,
                    b: 1.0,
                    c: 0.0,
                },
            ),
            (
                Attractor2dKind::Martin,
                Attractor2dParameters {
                    a: std::f64::consts::PI,
                    b: 0.0,
                    c: 0.0,
                },
            ),
            (
                Attractor2dKind::Gingerbreadman,
                Attractor2dParameters {
                    a: 0.0,
                    b: 0.0,
                    c: 0.0,
                },
            ),
            (
                Attractor2dKind::Chip,
                Attractor2dParameters {
                    a: -15.0,
                    b: -19.0,
                    c: 1.0,
                },
            ),
            (
                Attractor2dKind::Quadruptwo,
                Attractor2dParameters {
                    a: 34.0,
                    b: 1.0,
                    c: 5.0,
                },
            ),
            (
                Attractor2dKind::Threeply,
                Attractor2dParameters {
                    a: -55.0,
                    b: -1.0,
                    c: -42.0,
                },
            ),
        ];
        for (kind, parameters) in cases {
            let (mut x, mut y) = (0.1, 0.0);
            for _ in 0..1_000 {
                (x, y) = attractor_2d_step(kind, x, y, parameters);
                assert!(x.is_finite() && y.is_finite());
            }
        }
    }
}
