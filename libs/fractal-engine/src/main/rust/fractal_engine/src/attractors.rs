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
    Clifford,
    DeJong,
    Ikeda,
    Tinkerbell,
    GumowskiMira,
    BarnsleyFern,
    IfsDragon,
    IfsTwig,
    ChristmasTree,
    VicsekCross,
    PythagorasTree,
    HTree,
    HeighwayDragon,
    KochSnowflake,
    HilbertCurve,
}

#[derive(Clone, Copy)]
pub(crate) struct Attractor2dParameters {
    pub(crate) a: f64,
    pub(crate) b: f64,
    pub(crate) c: f64,
    pub(crate) d: f64,
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
    detail_iterations: usize,
    cancelled: &AtomicBool,
) -> Result<Vec<u32>, DensityError> {
    match kind {
        Attractor2dKind::BarnsleyFern
        | Attractor2dKind::IfsDragon
        | Attractor2dKind::IfsTwig
        | Attractor2dKind::ChristmasTree
        | Attractor2dKind::VicsekCross => {
            return ifs_density(kind, viewport, steps, cancelled);
        }
        Attractor2dKind::PythagorasTree
        | Attractor2dKind::HTree
        | Attractor2dKind::HeighwayDragon
        | Attractor2dKind::KochSnowflake
        | Attractor2dKind::HilbertCurve => {
            return geometric_density(kind, viewport, detail_iterations, cancelled);
        }
        _ => {}
    }

    let pixel_count = viewport
        .width
        .checked_mul(viewport.height)
        .ok_or(DensityError::AllocationFailed)?;
    let mut density = try_zeroed_density(pixel_count)?;
    let (mut x, mut y) = match kind {
        Attractor2dKind::Gingerbreadman | Attractor2dKind::GumowskiMira => (-0.1, 0.0),
        Attractor2dKind::Tinkerbell => (-0.72, -0.64),
        _ => (0.1, 0.0),
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
        Attractor2dKind::Clifford => (
            (parameters.a * y).sin() + parameters.c * (parameters.a * x).cos(),
            (parameters.b * x).sin() + parameters.d * (parameters.b * y).cos(),
        ),
        Attractor2dKind::DeJong => (
            (parameters.a * y).sin() - (parameters.b * x).cos(),
            (parameters.c * x).sin() - (parameters.d * y).cos(),
        ),
        Attractor2dKind::Ikeda => {
            let angle = parameters.c - parameters.d / (1.0 + x * x + y * y);
            let cosine = angle.cos();
            let sine = angle.sin();
            (
                parameters.a + parameters.b * (x * cosine - y * sine),
                parameters.b * (x * sine + y * cosine),
            )
        }
        Attractor2dKind::Tinkerbell => (
            x * x - y * y + parameters.a * x + parameters.b * y,
            2.0 * x * y + parameters.c * x + parameters.d * y,
        ),
        Attractor2dKind::GumowskiMira => {
            let map = |value: f64| {
                parameters.b * value
                    + 2.0 * (1.0 - parameters.b) * value * value / (1.0 + value * value)
            };
            let next_x = y + parameters.a * (1.0 - parameters.b * y * y) * y + map(x);
            (next_x, -x + map(next_x))
        }
        Attractor2dKind::BarnsleyFern
        | Attractor2dKind::IfsDragon
        | Attractor2dKind::IfsTwig
        | Attractor2dKind::ChristmasTree
        | Attractor2dKind::VicsekCross
        | Attractor2dKind::PythagorasTree
        | Attractor2dKind::HTree
        | Attractor2dKind::HeighwayDragon
        | Attractor2dKind::KochSnowflake
        | Attractor2dKind::HilbertCurve => unreachable!("non-map fractal dispatched as a map"),
    }
}

fn ifs_density(
    kind: Attractor2dKind,
    viewport: DensityViewport,
    _steps: usize,
    cancelled: &AtomicBool,
) -> Result<Vec<u32>, DensityError> {
    let pixel_count = viewport
        .width
        .checked_mul(viewport.height)
        .ok_or(DensityError::AllocationFailed)?;
    let mut density = try_zeroed_density(pixel_count)?;
    let (maps, bounds): (&[AffineMap], AffineBounds) = match kind {
        Attractor2dKind::BarnsleyFern => (
            &[
                AffineMap::new(0.0, 0.0, 0.0, 0.16, 0.0, 0.0),
                AffineMap::new(0.85, 0.04, -0.04, 0.85, 0.0, 1.6),
                AffineMap::new(0.2, -0.26, 0.23, 0.22, 0.0, 1.6),
                AffineMap::new(-0.15, 0.28, 0.26, 0.24, 0.0, 0.44),
            ],
            AffineBounds::new(-2.19, 2.66, 0.0, 10.0),
        ),
        Attractor2dKind::IfsDragon => (
            &[
                AffineMap::new(
                    0.824_074, 0.281_428, -0.212_346, 0.864_198, -1.882_29, -0.110_607,
                ),
                AffineMap::new(
                    0.088_272, 0.520_988, -0.463_889, -0.377_778, 0.785_36, 8.095_795,
                ),
            ],
            AffineBounds::new(-3.1, 3.1, -0.7, 9.2),
        ),
        Attractor2dKind::IfsTwig => (
            &[
                AffineMap::new(0.387, 0.43, 0.43, -0.387, 0.256, 0.522),
                AffineMap::new(0.441, -0.091, -0.009, -0.322, 0.4219, 0.5059),
                AffineMap::new(-0.468, 0.02, -0.113, 0.015, 0.4, 0.4),
            ],
            AffineBounds::new(-0.05, 1.05, -0.05, 1.05),
        ),
        Attractor2dKind::ChristmasTree => (
            &[
                AffineMap::new(0.0, -0.5, 0.5, 0.0, 0.5, 0.0),
                AffineMap::new(0.0, 0.5, -0.5, 0.0, 0.5, 0.5),
                AffineMap::new(0.5, 0.0, 0.0, 0.5, 0.25, 0.5),
            ],
            AffineBounds::new(-0.05, 1.05, -0.05, 1.05),
        ),
        Attractor2dKind::VicsekCross => (
            &[
                AffineMap::new(1.0 / 3.0, 0.0, 0.0, 1.0 / 3.0, -2.0 / 3.0, -2.0 / 3.0),
                AffineMap::new(1.0 / 3.0, 0.0, 0.0, 1.0 / 3.0, 2.0 / 3.0, -2.0 / 3.0),
                AffineMap::new(1.0 / 3.0, 0.0, 0.0, 1.0 / 3.0, 0.0, 0.0),
                AffineMap::new(1.0 / 3.0, 0.0, 0.0, 1.0 / 3.0, -2.0 / 3.0, 2.0 / 3.0),
                AffineMap::new(1.0 / 3.0, 0.0, 0.0, 1.0 / 3.0, 2.0 / 3.0, 2.0 / 3.0),
            ],
            AffineBounds::new(-1.05, 1.05, -1.05, 1.05),
        ),
        _ => unreachable!("non-IFS fractal dispatched as an IFS"),
    };
    draw_affine_ifs(
        &mut density,
        viewport,
        maps,
        bounds,
        AffineMap::IDENTITY,
        0,
        cancelled,
    )?;
    Ok(density)
}

#[derive(Clone, Copy)]
struct AffineMap {
    xx: f64,
    xy: f64,
    yx: f64,
    yy: f64,
    tx: f64,
    ty: f64,
}

impl AffineMap {
    const IDENTITY: Self = Self::new(1.0, 0.0, 0.0, 1.0, 0.0, 0.0);

    const fn new(xx: f64, xy: f64, yx: f64, yy: f64, tx: f64, ty: f64) -> Self {
        Self {
            xx,
            xy,
            yx,
            yy,
            tx,
            ty,
        }
    }

    fn apply(self, point: (f64, f64)) -> (f64, f64) {
        (
            self.xx * point.0 + self.xy * point.1 + self.tx,
            self.yx * point.0 + self.yy * point.1 + self.ty,
        )
    }

    fn compose(self, inner: Self) -> Self {
        Self::new(
            self.xx * inner.xx + self.xy * inner.yx,
            self.xx * inner.xy + self.xy * inner.yy,
            self.yx * inner.xx + self.yy * inner.yx,
            self.yx * inner.xy + self.yy * inner.yy,
            self.xx * inner.tx + self.xy * inner.ty + self.tx,
            self.yx * inner.tx + self.yy * inner.ty + self.ty,
        )
    }
}

#[derive(Clone, Copy)]
struct AffineBounds {
    minimum_x: f64,
    maximum_x: f64,
    minimum_y: f64,
    maximum_y: f64,
}

impl AffineBounds {
    const fn new(minimum_x: f64, maximum_x: f64, minimum_y: f64, maximum_y: f64) -> Self {
        Self {
            minimum_x,
            maximum_x,
            minimum_y,
            maximum_y,
        }
    }

    fn transformed(self, transformation: AffineMap) -> Self {
        let corners = [
            (self.minimum_x, self.minimum_y),
            (self.minimum_x, self.maximum_y),
            (self.maximum_x, self.minimum_y),
            (self.maximum_x, self.maximum_y),
        ];
        let mut transformed = corners.into_iter().map(|point| transformation.apply(point));
        let first = transformed.next().expect("affine bounds have corners");
        transformed.fold(
            Self::new(first.0, first.0, first.1, first.1),
            |bounds, point| {
                Self::new(
                    bounds.minimum_x.min(point.0),
                    bounds.maximum_x.max(point.0),
                    bounds.minimum_y.min(point.1),
                    bounds.maximum_y.max(point.1),
                )
            },
        )
    }
}

#[allow(clippy::too_many_arguments)]
fn draw_affine_ifs(
    density: &mut [u32],
    viewport: DensityViewport,
    maps: &[AffineMap],
    attractor_bounds: AffineBounds,
    transformation: AffineMap,
    depth: usize,
    cancelled: &AtomicBool,
) -> Result<(), DensityError> {
    if cancelled.load(Ordering::Acquire) {
        return Err(DensityError::Cancelled);
    }
    let bounds = attractor_bounds.transformed(transformation);
    if !viewport_intersects_bounds(
        viewport,
        bounds.minimum_x,
        bounds.maximum_x,
        bounds.minimum_y,
        bounds.maximum_y,
    ) {
        return Ok(());
    }
    let horizontal_span = viewport.vertical_span * viewport.aspect_ratio;
    let projected_width =
        (bounds.maximum_x - bounds.minimum_x) * viewport.width as f64 / horizontal_span;
    let projected_height =
        (bounds.maximum_y - bounds.minimum_y) * viewport.height as f64 / viewport.vertical_span;
    if depth >= 64 || projected_width.max(projected_height) <= 0.75 {
        plot_density(
            density,
            viewport,
            (bounds.minimum_x + bounds.maximum_x) * 0.5,
            (bounds.minimum_y + bounds.maximum_y) * 0.5,
            1,
        );
        return Ok(());
    }
    for map in maps {
        draw_affine_ifs(
            density,
            viewport,
            maps,
            attractor_bounds,
            transformation.compose(*map),
            depth + 1,
            cancelled,
        )?;
    }
    Ok(())
}

fn geometric_density(
    kind: Attractor2dKind,
    viewport: DensityViewport,
    _detail_iterations: usize,
    cancelled: &AtomicBool,
) -> Result<Vec<u32>, DensityError> {
    let pixel_count = viewport
        .width
        .checked_mul(viewport.height)
        .ok_or(DensityError::AllocationFailed)?;
    let mut density = try_zeroed_density(pixel_count)?;
    match kind {
        Attractor2dKind::PythagorasTree => {
            draw_pythagoras_square(
                &mut density,
                viewport,
                (-0.34, -0.95),
                (0.34, -0.95),
                64,
                cancelled,
            )?;
        }
        Attractor2dKind::HTree => {
            draw_h_tree(
                &mut density,
                viewport,
                (0.0, 0.0),
                0.72,
                true,
                64,
                cancelled,
            )?;
        }
        Attractor2dKind::HeighwayDragon => {
            draw_heighway_dragon(&mut density, viewport, 64, cancelled)?;
        }
        Attractor2dKind::KochSnowflake => {
            let vertices = [(-0.84, -0.48), (0.84, -0.48), (0.0, 0.975)];
            for index in 0..3 {
                draw_koch_segment(
                    &mut density,
                    viewport,
                    vertices[index],
                    vertices[(index + 1) % 3],
                    32,
                    cancelled,
                )?;
            }
        }
        Attractor2dKind::HilbertCurve => {
            draw_hilbert_curve(&mut density, viewport, 32, cancelled)?;
        }
        _ => unreachable!("non-geometric fractal dispatched as geometry"),
    }
    Ok(density)
}

fn draw_pythagoras_square(
    density: &mut [u32],
    viewport: DensityViewport,
    base_start: (f64, f64),
    base_end: (f64, f64),
    depth: usize,
    cancelled: &AtomicBool,
) -> Result<(), DensityError> {
    if cancelled.load(Ordering::Acquire) {
        return Err(DensityError::Cancelled);
    }
    let edge = (base_end.0 - base_start.0, base_end.1 - base_start.1);
    let edge_length = edge.0.hypot(edge.1);
    let outward = (-edge.1, edge.0);
    let top_start = (base_start.0 + outward.0, base_start.1 + outward.1);
    let top_end = (base_end.0 + outward.0, base_end.1 + outward.1);
    let center = (
        (base_start.0 + base_end.0 + outward.0) * 0.5,
        (base_start.1 + base_end.1 + outward.1) * 0.5,
    );
    if !viewport_intersects_circle(viewport, center, edge_length * 4.0) {
        return Ok(());
    }
    plot_line_density(density, viewport, base_start, base_end);
    plot_line_density(density, viewport, base_end, top_end);
    plot_line_density(density, viewport, top_end, top_start);
    plot_line_density(density, viewport, top_start, base_start);
    if depth <= 1 || projected_length(viewport, edge_length) <= 0.6 {
        return Ok(());
    }
    let top_edge = (top_end.0 - top_start.0, top_end.1 - top_start.1);
    let apex = (
        top_start.0 + (top_edge.0 - top_edge.1) * 0.5,
        top_start.1 + (top_edge.1 + top_edge.0) * 0.5,
    );
    draw_pythagoras_square(density, viewport, top_start, apex, depth - 1, cancelled)?;
    draw_pythagoras_square(density, viewport, apex, top_end, depth - 1, cancelled)
}

#[allow(clippy::too_many_arguments)]
fn draw_h_tree(
    density: &mut [u32],
    viewport: DensityViewport,
    center: (f64, f64),
    half_length: f64,
    horizontal: bool,
    depth: usize,
    cancelled: &AtomicBool,
) -> Result<(), DensityError> {
    if cancelled.load(Ordering::Acquire) {
        return Err(DensityError::Cancelled);
    }
    if !viewport_intersects_circle(viewport, center, half_length * 4.0) {
        return Ok(());
    }
    let (first, second) = if horizontal {
        (
            (center.0 - half_length, center.1),
            (center.0 + half_length, center.1),
        )
    } else {
        (
            (center.0, center.1 - half_length),
            (center.0, center.1 + half_length),
        )
    };
    plot_line_density(density, viewport, first, second);
    if depth <= 1 || projected_length(viewport, half_length * 2.0) <= 0.6 {
        return Ok(());
    }
    let child_length = half_length / 2.0_f64.sqrt();
    draw_h_tree(
        density,
        viewport,
        first,
        child_length,
        !horizontal,
        depth - 1,
        cancelled,
    )?;
    draw_h_tree(
        density,
        viewport,
        second,
        child_length,
        !horizontal,
        depth - 1,
        cancelled,
    )
}

fn draw_heighway_dragon(
    density: &mut [u32],
    viewport: DensityViewport,
    order: usize,
    cancelled: &AtomicBool,
) -> Result<(), DensityError> {
    draw_dragon_segment(
        density,
        viewport,
        (-0.65, -0.15),
        (0.65, -0.15),
        order,
        cancelled,
    )
}

#[allow(clippy::too_many_arguments)]
fn draw_dragon_segment(
    density: &mut [u32],
    viewport: DensityViewport,
    start: (f64, f64),
    end: (f64, f64),
    depth: usize,
    cancelled: &AtomicBool,
) -> Result<(), DensityError> {
    if cancelled.load(Ordering::Acquire) {
        return Err(DensityError::Cancelled);
    }
    let dx = end.0 - start.0;
    let dy = end.1 - start.1;
    let length = dx.hypot(dy);
    let center = ((start.0 + end.0) * 0.5, (start.1 + end.1) * 0.5);
    if !viewport_intersects_circle(viewport, center, length * 1.25) {
        return Ok(());
    }
    if depth == 0 || projected_length(viewport, length) <= 0.6 {
        plot_line_density(density, viewport, start, end);
        return Ok(());
    }
    let midpoint = (center.0 + dy * 0.5, center.1 - dx * 0.5);
    draw_dragon_segment(density, viewport, start, midpoint, depth - 1, cancelled)?;
    draw_dragon_segment(density, viewport, end, midpoint, depth - 1, cancelled)
}

fn draw_koch_segment(
    density: &mut [u32],
    viewport: DensityViewport,
    start: (f64, f64),
    end: (f64, f64),
    depth: usize,
    cancelled: &AtomicBool,
) -> Result<(), DensityError> {
    if cancelled.load(Ordering::Acquire) {
        return Err(DensityError::Cancelled);
    }
    let length = (end.0 - start.0).hypot(end.1 - start.1);
    let center = ((start.0 + end.0) * 0.5, (start.1 + end.1) * 0.5);
    if !viewport_intersects_circle(viewport, center, length * 0.8) {
        return Ok(());
    }
    if depth == 0 || projected_length(viewport, length) <= 0.6 {
        plot_line_density(density, viewport, start, end);
        return Ok(());
    }
    let delta = ((end.0 - start.0) / 3.0, (end.1 - start.1) / 3.0);
    let first = (start.0 + delta.0, start.1 + delta.1);
    let third = (start.0 + delta.0 * 2.0, start.1 + delta.1 * 2.0);
    let cosine = 0.5;
    let sine = -(3.0_f64).sqrt() * 0.5;
    let peak = (
        first.0 + delta.0 * cosine - delta.1 * sine,
        first.1 + delta.0 * sine + delta.1 * cosine,
    );
    draw_koch_segment(density, viewport, start, first, depth - 1, cancelled)?;
    draw_koch_segment(density, viewport, first, peak, depth - 1, cancelled)?;
    draw_koch_segment(density, viewport, peak, third, depth - 1, cancelled)?;
    draw_koch_segment(density, viewport, third, end, depth - 1, cancelled)
}

fn draw_hilbert_curve(
    density: &mut [u32],
    viewport: DensityViewport,
    order: usize,
    cancelled: &AtomicBool,
) -> Result<(), DensityError> {
    let mut previous = None;
    draw_hilbert_recursive(
        density,
        viewport,
        -0.9,
        -0.9,
        1.8,
        0.0,
        0.0,
        1.8,
        order,
        &mut previous,
        cancelled,
    )
}

#[allow(clippy::too_many_arguments)]
fn draw_hilbert_recursive(
    density: &mut [u32],
    viewport: DensityViewport,
    origin_x: f64,
    origin_y: f64,
    axis_x_x: f64,
    axis_x_y: f64,
    axis_y_x: f64,
    axis_y_y: f64,
    depth: usize,
    previous: &mut Option<(f64, f64)>,
    cancelled: &AtomicBool,
) -> Result<(), DensityError> {
    if cancelled.load(Ordering::Acquire) {
        return Err(DensityError::Cancelled);
    }
    let corners = [
        (origin_x, origin_y),
        (origin_x + axis_x_x, origin_y + axis_x_y),
        (origin_x + axis_y_x, origin_y + axis_y_y),
        (
            origin_x + axis_x_x + axis_y_x,
            origin_y + axis_x_y + axis_y_y,
        ),
    ];
    let minimum_x = corners
        .iter()
        .map(|point| point.0)
        .fold(f64::INFINITY, f64::min);
    let maximum_x = corners
        .iter()
        .map(|point| point.0)
        .fold(f64::NEG_INFINITY, f64::max);
    let minimum_y = corners
        .iter()
        .map(|point| point.1)
        .fold(f64::INFINITY, f64::min);
    let maximum_y = corners
        .iter()
        .map(|point| point.1)
        .fold(f64::NEG_INFINITY, f64::max);
    if !viewport_intersects_bounds(viewport, minimum_x, maximum_x, minimum_y, maximum_y) {
        *previous = None;
        return Ok(());
    }
    let cell_size = axis_x_x.hypot(axis_x_y).max(axis_y_x.hypot(axis_y_y));
    if depth == 0 || projected_length(viewport, cell_size) <= 0.6 {
        let point = (
            origin_x + (axis_x_x + axis_y_x) * 0.5,
            origin_y + (axis_x_y + axis_y_y) * 0.5,
        );
        if let Some(previous_point) = *previous {
            plot_line_density(density, viewport, previous_point, point);
        }
        *previous = Some(point);
        return Ok(());
    }
    let half_x_x = axis_x_x * 0.5;
    let half_x_y = axis_x_y * 0.5;
    let half_y_x = axis_y_x * 0.5;
    let half_y_y = axis_y_y * 0.5;
    draw_hilbert_recursive(
        density,
        viewport,
        origin_x,
        origin_y,
        half_y_x,
        half_y_y,
        half_x_x,
        half_x_y,
        depth - 1,
        previous,
        cancelled,
    )?;
    draw_hilbert_recursive(
        density,
        viewport,
        origin_x + half_x_x,
        origin_y + half_x_y,
        half_x_x,
        half_x_y,
        half_y_x,
        half_y_y,
        depth - 1,
        previous,
        cancelled,
    )?;
    draw_hilbert_recursive(
        density,
        viewport,
        origin_x + half_x_x + half_y_x,
        origin_y + half_x_y + half_y_y,
        half_x_x,
        half_x_y,
        half_y_x,
        half_y_y,
        depth - 1,
        previous,
        cancelled,
    )?;
    draw_hilbert_recursive(
        density,
        viewport,
        origin_x + half_x_x + axis_y_x,
        origin_y + half_x_y + axis_y_y,
        -half_y_x,
        -half_y_y,
        -half_x_x,
        -half_x_y,
        depth - 1,
        previous,
        cancelled,
    )
}

fn plot_line_density(
    density: &mut [u32],
    viewport: DensityViewport,
    start: (f64, f64),
    end: (f64, f64),
) {
    let Some((start, end)) = clip_line_to_viewport(viewport, start, end) else {
        return;
    };
    let horizontal_span = viewport.vertical_span * viewport.aspect_ratio;
    let pixel_dx = (end.0 - start.0) * viewport.width as f64 / horizontal_span;
    let pixel_dy = (end.1 - start.1) * viewport.height as f64 / viewport.vertical_span;
    let steps = pixel_dx.hypot(pixel_dy).ceil().max(1.0) as usize;
    for index in 0..=steps {
        let fraction = index as f64 / steps as f64;
        plot_density(
            density,
            viewport,
            start.0 + (end.0 - start.0) * fraction,
            start.1 + (end.1 - start.1) * fraction,
            1,
        );
    }
}

fn projected_length(viewport: DensityViewport, world_length: f64) -> f64 {
    let horizontal_span = viewport.vertical_span * viewport.aspect_ratio;
    (world_length * viewport.width as f64 / horizontal_span)
        .max(world_length * viewport.height as f64 / viewport.vertical_span)
}

fn viewport_intersects_circle(viewport: DensityViewport, center: (f64, f64), radius: f64) -> bool {
    let horizontal_span = viewport.vertical_span * viewport.aspect_ratio;
    let minimum_x = viewport.center_x - horizontal_span * 0.5;
    let maximum_x = viewport.center_x + horizontal_span * 0.5;
    let minimum_y = viewport.center_y - viewport.vertical_span * 0.5;
    let maximum_y = viewport.center_y + viewport.vertical_span * 0.5;
    center.0 + radius >= minimum_x
        && center.0 - radius <= maximum_x
        && center.1 + radius >= minimum_y
        && center.1 - radius <= maximum_y
}

fn viewport_intersects_bounds(
    viewport: DensityViewport,
    minimum_x: f64,
    maximum_x: f64,
    minimum_y: f64,
    maximum_y: f64,
) -> bool {
    let horizontal_span = viewport.vertical_span * viewport.aspect_ratio;
    let viewport_minimum_x = viewport.center_x - horizontal_span * 0.5;
    let viewport_maximum_x = viewport.center_x + horizontal_span * 0.5;
    let viewport_minimum_y = viewport.center_y - viewport.vertical_span * 0.5;
    let viewport_maximum_y = viewport.center_y + viewport.vertical_span * 0.5;
    maximum_x >= viewport_minimum_x
        && minimum_x <= viewport_maximum_x
        && maximum_y >= viewport_minimum_y
        && minimum_y <= viewport_maximum_y
}

fn clip_line_to_viewport(
    viewport: DensityViewport,
    start: (f64, f64),
    end: (f64, f64),
) -> Option<((f64, f64), (f64, f64))> {
    let horizontal_span = viewport.vertical_span * viewport.aspect_ratio;
    let minimum_x = viewport.center_x - horizontal_span * 0.5;
    let maximum_x = viewport.center_x + horizontal_span * 0.5;
    let minimum_y = viewport.center_y - viewport.vertical_span * 0.5;
    let maximum_y = viewport.center_y + viewport.vertical_span * 0.5;
    let dx = end.0 - start.0;
    let dy = end.1 - start.1;
    let mut near = 0.0_f64;
    let mut far = 1.0_f64;
    for (direction, distance) in [
        (-dx, start.0 - minimum_x),
        (dx, maximum_x - start.0),
        (-dy, start.1 - minimum_y),
        (dy, maximum_y - start.1),
    ] {
        if direction.abs() < f64::EPSILON {
            if distance < 0.0 {
                return None;
            }
            continue;
        }
        let ratio = distance / direction;
        if direction < 0.0 {
            near = near.max(ratio);
        } else {
            far = far.min(ratio);
        }
        if near > far {
            return None;
        }
    }
    Some((
        (start.0 + dx * near, start.1 + dy * near),
        (start.0 + dx * far, start.1 + dy * far),
    ))
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
    fn geometric_detail_is_stable_across_iteration_budgets() {
        let viewport = DensityViewport {
            width: 128,
            height: 128,
            center_x: 0.0,
            center_y: 0.0,
            vertical_span: 2.2,
            aspect_ratio: 1.0,
        };
        let low = geometric_density(
            Attractor2dKind::HTree,
            viewport,
            16,
            &AtomicBool::new(false),
        )
        .expect("low-budget geometry");
        let high = geometric_density(
            Attractor2dKind::HTree,
            viewport,
            100_000,
            &AtomicBool::new(false),
        )
        .expect("high-budget geometry");

        assert_eq!(low, high);
    }

    #[test]
    fn affine_ifs_remains_deterministic_and_visible_when_zoomed() {
        let viewport = DensityViewport {
            width: 96,
            height: 96,
            center_x: 0.0,
            center_y: 5.0,
            vertical_span: 0.05,
            aspect_ratio: 1.0,
        };
        let first = ifs_density(
            Attractor2dKind::BarnsleyFern,
            viewport,
            32,
            &AtomicBool::new(false),
        )
        .expect("first fern render");
        let second = ifs_density(
            Attractor2dKind::BarnsleyFern,
            viewport,
            1_000_000,
            &AtomicBool::new(false),
        )
        .expect("second fern render");

        assert_eq!(first, second);
        assert!(first.into_iter().any(|value| value > 0));
    }

    #[test]
    fn hilbert_curve_keeps_visible_structure_at_deep_zoom() {
        let viewport = DensityViewport {
            width: 96,
            height: 96,
            center_x: 0.0,
            center_y: 0.0,
            vertical_span: 2.2 / 2.0_f64.powi(20),
            aspect_ratio: 1.0,
        };
        let density = geometric_density(
            Attractor2dKind::HilbertCurve,
            viewport,
            1_000,
            &AtomicBool::new(false),
        )
        .expect("deep Hilbert density");
        let occupied = density.into_iter().filter(|value| *value > 0).count();

        assert!(occupied > viewport.width, "occupied pixels: {occupied}");
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
                    d: 0.0,
                },
            ),
            (
                Attractor2dKind::Martin,
                Attractor2dParameters {
                    a: std::f64::consts::PI,
                    b: 0.0,
                    c: 0.0,
                    d: 0.0,
                },
            ),
            (
                Attractor2dKind::Gingerbreadman,
                Attractor2dParameters {
                    a: 0.0,
                    b: 0.0,
                    c: 0.0,
                    d: 0.0,
                },
            ),
            (
                Attractor2dKind::Chip,
                Attractor2dParameters {
                    a: -15.0,
                    b: -19.0,
                    c: 1.0,
                    d: 0.0,
                },
            ),
            (
                Attractor2dKind::Quadruptwo,
                Attractor2dParameters {
                    a: 34.0,
                    b: 1.0,
                    c: 5.0,
                    d: 0.0,
                },
            ),
            (
                Attractor2dKind::Threeply,
                Attractor2dParameters {
                    a: -55.0,
                    b: -1.0,
                    c: -42.0,
                    d: 0.0,
                },
            ),
            (
                Attractor2dKind::Clifford,
                Attractor2dParameters {
                    a: -1.4,
                    b: 1.6,
                    c: 1.0,
                    d: 0.7,
                },
            ),
            (
                Attractor2dKind::DeJong,
                Attractor2dParameters {
                    a: -2.24,
                    b: 0.43,
                    c: -0.65,
                    d: -2.43,
                },
            ),
            (
                Attractor2dKind::Ikeda,
                Attractor2dParameters {
                    a: 0.85,
                    b: 0.9,
                    c: 0.4,
                    d: 7.7,
                },
            ),
            (
                Attractor2dKind::Tinkerbell,
                Attractor2dParameters {
                    a: 0.9,
                    b: -0.6013,
                    c: 2.0,
                    d: 0.5,
                },
            ),
            (
                Attractor2dKind::GumowskiMira,
                Attractor2dParameters {
                    a: 0.008,
                    b: 0.05,
                    c: 2.0,
                    d: 0.0,
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
