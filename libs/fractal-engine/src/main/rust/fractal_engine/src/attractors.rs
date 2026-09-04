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
    steps: usize,
    cancelled: &AtomicBool,
) -> Result<Vec<u32>, DensityError> {
    let pixel_count = viewport
        .width
        .checked_mul(viewport.height)
        .ok_or(DensityError::AllocationFailed)?;
    let mut density = try_zeroed_density(pixel_count)?;
    let mut state = 0x9e37_79b9_u32;
    let (mut x, mut y) = (0.0, 0.0);

    for iteration in 0..steps {
        if iteration & 255 == 0 && cancelled.load(Ordering::Acquire) {
            return Err(DensityError::Cancelled);
        }
        state ^= state << 13;
        state ^= state >> 17;
        state ^= state << 5;
        let choice = state % 100;
        (x, y) = match kind {
            Attractor2dKind::BarnsleyFern => match choice {
                0 => (0.0, 0.16 * y),
                1..=85 => (0.85 * x + 0.04 * y, -0.04 * x + 0.85 * y + 1.6),
                86..=92 => (0.2 * x - 0.26 * y, 0.23 * x + 0.22 * y + 1.6),
                _ => (-0.15 * x + 0.28 * y, 0.26 * x + 0.24 * y + 0.44),
            },
            Attractor2dKind::IfsDragon => {
                if choice < 80 {
                    (
                        0.824_074 * x + 0.281_428 * y - 1.882_29,
                        -0.212_346 * x + 0.864_198 * y - 0.110_607,
                    )
                } else {
                    (
                        0.088_272 * x + 0.520_988 * y + 0.785_36,
                        -0.463_889 * x - 0.377_778 * y + 8.095_795,
                    )
                }
            }
            Attractor2dKind::IfsTwig => match choice % 3 {
                0 => (0.387 * x + 0.43 * y + 0.256, 0.43 * x - 0.387 * y + 0.522),
                1 => (
                    0.441 * x - 0.091 * y + 0.4219,
                    -0.009 * x - 0.322 * y + 0.5059,
                ),
                _ => (-0.468 * x + 0.02 * y + 0.4, -0.113 * x + 0.015 * y + 0.4),
            },
            Attractor2dKind::ChristmasTree => match choice % 3 {
                0 => (-0.5 * y + 0.5, 0.5 * x),
                1 => (0.5 * y + 0.5, -0.5 * x + 0.5),
                _ => (0.5 * x + 0.25, 0.5 * y + 0.5),
            },
            Attractor2dKind::VicsekCross => {
                let (translation_x, translation_y) = match choice % 5 {
                    0 => (-2.0 / 3.0, -2.0 / 3.0),
                    1 => (2.0 / 3.0, -2.0 / 3.0),
                    2 => (0.0, 0.0),
                    3 => (-2.0 / 3.0, 2.0 / 3.0),
                    _ => (2.0 / 3.0, 2.0 / 3.0),
                };
                (x / 3.0 + translation_x, y / 3.0 + translation_y)
            }
            _ => unreachable!("non-IFS fractal dispatched as an IFS"),
        };
        if iteration >= 32 {
            plot_density(&mut density, viewport, x, y, 1);
        }
    }
    Ok(density)
}

fn geometric_density(
    kind: Attractor2dKind,
    viewport: DensityViewport,
    detail_iterations: usize,
    cancelled: &AtomicBool,
) -> Result<Vec<u32>, DensityError> {
    let pixel_count = viewport
        .width
        .checked_mul(viewport.height)
        .ok_or(DensityError::AllocationFailed)?;
    let mut density = try_zeroed_density(pixel_count)?;
    match kind {
        Attractor2dKind::PythagorasTree => {
            let depth = detail_depth(detail_iterations, 6, 10);
            draw_pythagoras_square(
                &mut density,
                viewport,
                (-0.34, -0.95),
                (0.34, -0.95),
                depth,
                cancelled,
            )?;
        }
        Attractor2dKind::HTree => {
            let depth = detail_depth(detail_iterations, 5, 9);
            draw_h_tree(
                &mut density,
                viewport,
                (0.0, 0.0),
                0.72,
                true,
                depth,
                cancelled,
            )?;
        }
        Attractor2dKind::HeighwayDragon => {
            let order = detail_depth(detail_iterations, 11, 16);
            draw_heighway_dragon(&mut density, viewport, order, cancelled)?;
        }
        Attractor2dKind::KochSnowflake => {
            let depth = (detail_depth(detail_iterations, 6, 12) / 2).clamp(3, 6);
            let vertices = [(-0.84, -0.48), (0.84, -0.48), (0.0, 0.975)];
            for index in 0..3 {
                draw_koch_segment(
                    &mut density,
                    viewport,
                    vertices[index],
                    vertices[(index + 1) % 3],
                    depth,
                    cancelled,
                )?;
            }
        }
        Attractor2dKind::HilbertCurve => {
            let order = detail_depth(detail_iterations, 5, 6);
            draw_hilbert_curve(&mut density, viewport, order, cancelled)?;
        }
        _ => unreachable!("non-geometric fractal dispatched as geometry"),
    }
    Ok(density)
}

fn detail_depth(iterations: usize, minimum: usize, maximum: usize) -> usize {
    ((iterations.max(1) as f64).log2().round() as usize).clamp(minimum, maximum)
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
    let outward = (-edge.1, edge.0);
    let top_start = (base_start.0 + outward.0, base_start.1 + outward.1);
    let top_end = (base_end.0 + outward.0, base_end.1 + outward.1);
    plot_line_density(density, viewport, base_start, base_end);
    plot_line_density(density, viewport, base_end, top_end);
    plot_line_density(density, viewport, top_end, top_start);
    plot_line_density(density, viewport, top_start, base_start);
    if depth <= 1 {
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
    if depth <= 1 {
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
    let turn_count = (1_usize << order).saturating_sub(1);
    let mut turns = Vec::new();
    turns
        .try_reserve_exact(turn_count)
        .map_err(|_| DensityError::AllocationFailed)?;
    for _ in 0..order {
        let old_length = turns.len();
        turns.push(true);
        for index in (0..old_length).rev() {
            turns.push(!turns[index]);
        }
    }
    let bounds = dragon_bounds(&turns);
    let mut point = (0_i32, 0_i32);
    let mut direction = 0_i32;
    for segment in 0..=turns.len() {
        if segment & 1023 == 0 && cancelled.load(Ordering::Acquire) {
            return Err(DensityError::Cancelled);
        }
        let next = advance_lattice(point, direction);
        plot_line_density(
            density,
            viewport,
            normalize_lattice(point, bounds),
            normalize_lattice(next, bounds),
        );
        point = next;
        if let Some(turn_left) = turns.get(segment) {
            direction = (direction + if *turn_left { 1 } else { 3 }) % 4;
        }
    }
    Ok(())
}

fn dragon_bounds(turns: &[bool]) -> (i32, i32, i32, i32) {
    let mut point = (0_i32, 0_i32);
    let mut direction = 0_i32;
    let (mut minimum_x, mut maximum_x, mut minimum_y, mut maximum_y) = (0, 0, 0, 0);
    for segment in 0..=turns.len() {
        point = advance_lattice(point, direction);
        minimum_x = minimum_x.min(point.0);
        maximum_x = maximum_x.max(point.0);
        minimum_y = minimum_y.min(point.1);
        maximum_y = maximum_y.max(point.1);
        if let Some(turn_left) = turns.get(segment) {
            direction = (direction + if *turn_left { 1 } else { 3 }) % 4;
        }
    }
    (minimum_x, maximum_x, minimum_y, maximum_y)
}

fn advance_lattice(point: (i32, i32), direction: i32) -> (i32, i32) {
    match direction {
        0 => (point.0 + 1, point.1),
        1 => (point.0, point.1 + 1),
        2 => (point.0 - 1, point.1),
        _ => (point.0, point.1 - 1),
    }
}

fn normalize_lattice(point: (i32, i32), bounds: (i32, i32, i32, i32)) -> (f64, f64) {
    let width = (bounds.1 - bounds.0).max(1) as f64;
    let height = (bounds.3 - bounds.2).max(1) as f64;
    let scale = 1.8 / width.max(height);
    (
        (point.0 as f64 - (bounds.0 + bounds.1) as f64 * 0.5) * scale,
        (point.1 as f64 - (bounds.2 + bounds.3) as f64 * 0.5) * scale,
    )
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
    if depth == 0 {
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
    let side = 1_u32 << order;
    let point_count = side * side;
    let mut previous = hilbert_point(side, 0);
    for index in 1..point_count {
        if index & 1023 == 0 && cancelled.load(Ordering::Acquire) {
            return Err(DensityError::Cancelled);
        }
        let point = hilbert_point(side, index);
        let normalize = |value: u32| -0.9 + 1.8 * value as f64 / (side - 1) as f64;
        plot_line_density(
            density,
            viewport,
            (normalize(previous.0), normalize(previous.1)),
            (normalize(point.0), normalize(point.1)),
        );
        previous = point;
    }
    Ok(())
}

fn hilbert_point(side: u32, mut index: u32) -> (u32, u32) {
    let (mut x, mut y) = (0_u32, 0_u32);
    let mut scale = 1_u32;
    while scale < side {
        let right = (index / 2) & 1;
        let up = (index ^ right) & 1;
        if up == 0 {
            if right == 1 {
                x = scale - 1 - x;
                y = scale - 1 - y;
            }
            std::mem::swap(&mut x, &mut y);
        }
        x += scale * right;
        y += scale * up;
        index /= 4;
        scale *= 2;
    }
    (x, y)
}

fn plot_line_density(
    density: &mut [u32],
    viewport: DensityViewport,
    start: (f64, f64),
    end: (f64, f64),
) {
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
