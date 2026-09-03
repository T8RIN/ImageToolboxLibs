use std::f64::consts::PI;
use std::sync::OnceLock;
use std::sync::atomic::{AtomicBool, Ordering};

use crate::attractors::{
    Attractor2dKind, Attractor2dParameters, Attractor3dKind, Attractor3dParameters, DensityError,
    DensityViewport, attractor_2d_density, buddhabrot_density, for_each_attractor_3d_point,
    try_zeroed_density,
};
use crate::decimal::{Decimal, fixed_to_f64, power_of_ten};
use crate::distance_estimators::{
    apollonian_gasket, hybrid_mandelbulb_julia, icosahedral_ifs, kleinian, octahedral_ifs,
    quaternion_cubic, sierpinski_gasket,
};
use crate::math::{Complex, Quaternion, Vec3};
use num_bigint::BigInt;
use rayon::prelude::*;
use rayon::{ThreadPool, ThreadPoolBuilder};

const TYPE_MANDELBROT: i32 = 1;
const TYPE_JULIA: i32 = 2;
const TYPE_BURNING_SHIP: i32 = 3;
const TYPE_TRICORN: i32 = 4;
const TYPE_MULTIBROT: i32 = 5;
const TYPE_MULTICORN: i32 = 6;
const TYPE_CELTIC: i32 = 7;
const TYPE_BUFFALO: i32 = 8;
const TYPE_PERPENDICULAR_BURNING_SHIP: i32 = 9;
const TYPE_PHOENIX: i32 = 10;
const TYPE_NOVA: i32 = 11;
const TYPE_NEWTON: i32 = 12;
const TYPE_MAGNET_I: i32 = 101;
const TYPE_MAGNET_II: i32 = 102;
const TYPE_LYAPUNOV: i32 = 103;
const TYPE_SIERPINSKI_CARPET: i32 = 104;
const TYPE_SIERPINSKI_TRIANGLE: i32 = 105;
const TYPE_BURNING_SHIP_JULIA: i32 = 106;
const TYPE_CELTIC_JULIA: i32 = 107;
const TYPE_COLLATZ: i32 = 108;
const TYPE_BUDDHABROT: i32 = 109;
const TYPE_HOPALONG: i32 = 110;
const TYPE_MARTIN: i32 = 111;
const TYPE_GINGERBREADMAN: i32 = 112;
const TYPE_CHIP: i32 = 113;
const TYPE_QUADRUPTWO: i32 = 114;
const TYPE_THREEPLY: i32 = 115;
const TYPE_MANDELBULB: i32 = 1001;
const TYPE_MANDELBOX: i32 = 1002;
const TYPE_MENGER_SPONGE: i32 = 1003;
const TYPE_SIERPINSKI_TETRAHEDRON: i32 = 1004;
const TYPE_QUATERNION_JULIA: i32 = 1005;
const TYPE_OCTAHEDRAL_IFS: i32 = 1006;
const TYPE_ICOSAHEDRAL_IFS: i32 = 1007;
const TYPE_APOLLONIAN_GASKET: i32 = 1008;
const TYPE_KLEINIAN: i32 = 1009;
const TYPE_HYBRID_MANDELBULB_JULIA: i32 = 1010;
const TYPE_QUATERNION_CUBIC: i32 = 1011;
const TYPE_SIERPINSKI_GASKET: i32 = 1012;
const TYPE_PICKOVER: i32 = 1013;
const TYPE_LORENZ: i32 = 1014;
const TYPE_ROSSLER: i32 = 1015;

const PARAM_CENTER_X: usize = 0;
const PARAM_CENTER_Y: usize = 1;
const PARAM_VERTICAL_SPAN: usize = 2;
const PARAM_VIEWPORT_ASPECT_RATIO: usize = 3;
const PARAM_POWER: usize = 4;
const PARAM_BAILOUT: usize = 5;
const PARAM_SUPERSAMPLING: usize = 6;
const PARAM_COLORING_ID: usize = 7;
const PARAM_PALETTE_CYCLES: usize = 8;
const PARAM_PALETTE_OFFSET: usize = 9;
const PARAM_INSIDE_COLOR_ARGB: usize = 10;
const PARAM_JULIA_REAL: usize = 11;
const PARAM_JULIA_IMAGINARY: usize = 12;
const PARAM_PHOENIX_REAL: usize = 13;
const PARAM_PHOENIX_IMAGINARY: usize = 14;
const PARAM_NOVA_RELAXATION: usize = 15;
const PARAM_CAMERA_YAW: usize = 16;
const PARAM_CAMERA_PITCH: usize = 17;
const PARAM_CAMERA_DISTANCE: usize = 18;
const PARAM_CAMERA_TARGET_X: usize = 19;
const PARAM_CAMERA_TARGET_Y: usize = 20;
const PARAM_CAMERA_TARGET_Z: usize = 21;
const PARAM_QUATERNION_X: usize = 22;
const PARAM_QUATERNION_Y: usize = 23;
const PARAM_QUATERNION_Z: usize = 24;
const PARAM_QUATERNION_W: usize = 25;
const PARAM_FIELD_OF_VIEW_DEGREES: usize = 26;
pub(crate) const REQUIRED_PARAMETER_COUNT: usize = 27;
pub(crate) const MAX_RENDER_WORK_UNITS: u64 = 500_000_000;

#[derive(Clone, Copy, Debug, Eq, PartialEq)]
pub(crate) enum RenderOutcome {
    Completed,
    Cancelled,
    AllocationFailed,
    WorkLimitExceeded,
}

#[derive(Clone, Copy, Debug, Eq, PartialEq)]
pub(crate) enum BitmapAlphaMode {
    Premultiplied,
    Unpremultiplied,
    Opaque,
}

#[derive(Clone, Copy, Debug, Eq, PartialEq)]
pub(crate) enum FractalKind {
    Mandelbrot,
    Julia,
    BurningShip,
    Tricorn,
    Multibrot,
    Multicorn,
    Celtic,
    Buffalo,
    PerpendicularBurningShip,
    Phoenix,
    Nova,
    Newton,
    MagnetI,
    MagnetII,
    Lyapunov,
    SierpinskiCarpet,
    SierpinskiTriangle,
    BurningShipJulia,
    CelticJulia,
    Collatz,
    Buddhabrot,
    Hopalong,
    Martin,
    Gingerbreadman,
    Chip,
    Quadruptwo,
    Threeply,
    Mandelbulb,
    Mandelbox,
    MengerSponge,
    SierpinskiTetrahedron,
    QuaternionJulia,
    OctahedralIfs,
    IcosahedralIfs,
    ApollonianGasket,
    Kleinian,
    HybridMandelbulbJulia,
    QuaternionCubic,
    SierpinskiGasket,
    Pickover,
    Lorenz,
    Rossler,
}

impl FractalKind {
    pub(crate) fn from_stable_id(stable_id: i32) -> Option<Self> {
        Some(match stable_id {
            TYPE_MANDELBROT => Self::Mandelbrot,
            TYPE_JULIA => Self::Julia,
            TYPE_BURNING_SHIP => Self::BurningShip,
            TYPE_TRICORN => Self::Tricorn,
            TYPE_MULTIBROT => Self::Multibrot,
            TYPE_MULTICORN => Self::Multicorn,
            TYPE_CELTIC => Self::Celtic,
            TYPE_BUFFALO => Self::Buffalo,
            TYPE_PERPENDICULAR_BURNING_SHIP => Self::PerpendicularBurningShip,
            TYPE_PHOENIX => Self::Phoenix,
            TYPE_NOVA => Self::Nova,
            TYPE_NEWTON => Self::Newton,
            TYPE_MAGNET_I => Self::MagnetI,
            TYPE_MAGNET_II => Self::MagnetII,
            TYPE_LYAPUNOV => Self::Lyapunov,
            TYPE_SIERPINSKI_CARPET => Self::SierpinskiCarpet,
            TYPE_SIERPINSKI_TRIANGLE => Self::SierpinskiTriangle,
            TYPE_BURNING_SHIP_JULIA => Self::BurningShipJulia,
            TYPE_CELTIC_JULIA => Self::CelticJulia,
            TYPE_COLLATZ => Self::Collatz,
            TYPE_BUDDHABROT => Self::Buddhabrot,
            TYPE_HOPALONG => Self::Hopalong,
            TYPE_MARTIN => Self::Martin,
            TYPE_GINGERBREADMAN => Self::Gingerbreadman,
            TYPE_CHIP => Self::Chip,
            TYPE_QUADRUPTWO => Self::Quadruptwo,
            TYPE_THREEPLY => Self::Threeply,
            TYPE_MANDELBULB => Self::Mandelbulb,
            TYPE_MANDELBOX => Self::Mandelbox,
            TYPE_MENGER_SPONGE => Self::MengerSponge,
            TYPE_SIERPINSKI_TETRAHEDRON => Self::SierpinskiTetrahedron,
            TYPE_QUATERNION_JULIA => Self::QuaternionJulia,
            TYPE_OCTAHEDRAL_IFS => Self::OctahedralIfs,
            TYPE_ICOSAHEDRAL_IFS => Self::IcosahedralIfs,
            TYPE_APOLLONIAN_GASKET => Self::ApollonianGasket,
            TYPE_KLEINIAN => Self::Kleinian,
            TYPE_HYBRID_MANDELBULB_JULIA => Self::HybridMandelbulbJulia,
            TYPE_QUATERNION_CUBIC => Self::QuaternionCubic,
            TYPE_SIERPINSKI_GASKET => Self::SierpinskiGasket,
            TYPE_PICKOVER => Self::Pickover,
            TYPE_LORENZ => Self::Lorenz,
            TYPE_ROSSLER => Self::Rossler,
            _ => return None,
        })
    }

    fn is_three_dimensional(self) -> bool {
        matches!(
            self,
            Self::Mandelbulb
                | Self::Mandelbox
                | Self::MengerSponge
                | Self::SierpinskiTetrahedron
                | Self::QuaternionJulia
                | Self::OctahedralIfs
                | Self::IcosahedralIfs
                | Self::ApollonianGasket
                | Self::Kleinian
                | Self::HybridMandelbulbJulia
                | Self::QuaternionCubic
                | Self::SierpinskiGasket
                | Self::Pickover
                | Self::Lorenz
                | Self::Rossler
        )
    }

    fn is_two_dimensional_density(self) -> bool {
        matches!(
            self,
            Self::Buddhabrot
                | Self::Hopalong
                | Self::Martin
                | Self::Gingerbreadman
                | Self::Chip
                | Self::Quadruptwo
                | Self::Threeply
        )
    }

    fn is_three_dimensional_attractor(self) -> bool {
        matches!(self, Self::Pickover | Self::Lorenz | Self::Rossler)
    }

    fn is_ray_marched(self) -> bool {
        self.is_three_dimensional() && !self.is_three_dimensional_attractor()
    }
}

#[derive(Clone, Copy)]
enum Coloring {
    Smooth,
    Banded,
    OrbitTrap,
    Angle,
}

pub(crate) struct RenderSettings {
    kind: FractalKind,
    max_iterations: usize,
    center_x: f64,
    center_y: f64,
    vertical_span: f64,
    exact_center_x: Decimal,
    exact_center_y: Decimal,
    exact_vertical_span: Decimal,
    viewport_aspect_ratio: f64,
    power: f64,
    bailout: f64,
    supersampling: usize,
    coloring: Coloring,
    palette_cycles: f64,
    palette_offset: f64,
    inside_color: u32,
    julia_constant: Complex,
    phoenix_constant: Complex,
    nova_relaxation: f64,
    lyapunov_sequence: Vec<bool>,
    camera_yaw: f64,
    camera_pitch: f64,
    camera_distance: f64,
    camera_target: Vec3,
    quaternion_constant: Quaternion,
    field_of_view_degrees: f64,
    palette: Palette,
}

pub(crate) struct ExactViewportWire<'a> {
    pub(crate) center_x: &'a str,
    pub(crate) center_y: &'a str,
    pub(crate) vertical_span: &'a str,
}

#[derive(Clone, Copy, Debug, Eq, PartialEq)]
struct RayMarchPlan {
    maximum_steps: usize,
    distance_iterations: usize,
}

#[derive(Clone, Copy, Debug, Eq, PartialEq)]
struct RenderWorkPlan {
    work_units: u64,
    density_iterations: usize,
    ray_march: Option<RayMarchPlan>,
}

impl RenderSettings {
    pub(crate) fn from_wire(
        type_id: i32,
        max_iterations: i32,
        parameters: &[f64],
        palette: &[i32],
        lyapunov_sequence: &str,
        exact_viewport: ExactViewportWire<'_>,
    ) -> Option<Self> {
        if parameters.len() != REQUIRED_PARAMETER_COUNT
            || parameters.iter().any(|value| !value.is_finite())
            || !(1..=16384).contains(&max_iterations)
            || !(2..=64).contains(&palette.len())
        {
            return None;
        }
        let kind = FractalKind::from_stable_id(type_id)?;

        let supersampling = parameters[PARAM_SUPERSAMPLING].round() as usize;
        let coloring = match parameters[PARAM_COLORING_ID].round() as i32 {
            0 => Coloring::Smooth,
            1 => Coloring::Banded,
            2 => Coloring::OrbitTrap,
            3 => Coloring::Angle,
            _ => return None,
        };
        let sequence = lyapunov_sequence
            .bytes()
            .map(|value| match value {
                b'A' => Some(false),
                b'B' => Some(true),
                _ => None,
            })
            .collect::<Option<Vec<_>>>()?;
        let parsed_center_x = Decimal::parse(exact_viewport.center_x)?;
        let parsed_center_y = Decimal::parse(exact_viewport.center_y)?;
        let parsed_vertical_span = Decimal::parse(exact_viewport.vertical_span)?;
        let minimum_span = Decimal::parse("1E-300")?;

        if sequence.is_empty()
            || sequence.len() > 64
            || parameters[PARAM_VERTICAL_SPAN] <= 0.0
            || !parsed_vertical_span.is_positive()
            || parsed_vertical_span.cmp(&minimum_span).is_lt()
            || exact_viewport.center_x.parse::<f64>().ok()? != parameters[PARAM_CENTER_X]
            || exact_viewport.center_y.parse::<f64>().ok()? != parameters[PARAM_CENTER_Y]
            || exact_viewport.vertical_span.parse::<f64>().ok()? != parameters[PARAM_VERTICAL_SPAN]
            || parameters[PARAM_VIEWPORT_ASPECT_RATIO] <= 0.0
            || !formula_parameters_are_valid(kind, parameters)
            || !(2.0..=1.0e12).contains(&parameters[PARAM_BAILOUT])
            || !(1..=4).contains(&supersampling)
            || !(0.01..=64.0).contains(&parameters[PARAM_PALETTE_CYCLES])
            || parameters[PARAM_CAMERA_DISTANCE] <= 0.0
            || !(10.0..=120.0).contains(&parameters[PARAM_FIELD_OF_VIEW_DEGREES])
        {
            return None;
        }

        Some(Self {
            kind,
            max_iterations: max_iterations as usize,
            center_x: parameters[PARAM_CENTER_X],
            center_y: parameters[PARAM_CENTER_Y],
            vertical_span: parameters[PARAM_VERTICAL_SPAN],
            exact_center_x: parsed_center_x,
            exact_center_y: parsed_center_y,
            exact_vertical_span: parsed_vertical_span,
            viewport_aspect_ratio: parameters[PARAM_VIEWPORT_ASPECT_RATIO],
            power: parameters[PARAM_POWER],
            bailout: parameters[PARAM_BAILOUT],
            supersampling,
            coloring,
            palette_cycles: parameters[PARAM_PALETTE_CYCLES],
            palette_offset: parameters[PARAM_PALETTE_OFFSET],
            inside_color: parameters[PARAM_INSIDE_COLOR_ARGB] as i64 as u32,
            julia_constant: Complex::new(
                parameters[PARAM_JULIA_REAL],
                parameters[PARAM_JULIA_IMAGINARY],
            ),
            phoenix_constant: Complex::new(
                parameters[PARAM_PHOENIX_REAL],
                parameters[PARAM_PHOENIX_IMAGINARY],
            ),
            nova_relaxation: parameters[PARAM_NOVA_RELAXATION],
            lyapunov_sequence: sequence,
            camera_yaw: parameters[PARAM_CAMERA_YAW],
            camera_pitch: parameters[PARAM_CAMERA_PITCH],
            camera_distance: parameters[PARAM_CAMERA_DISTANCE],
            camera_target: Vec3::new(
                parameters[PARAM_CAMERA_TARGET_X],
                parameters[PARAM_CAMERA_TARGET_Y],
                parameters[PARAM_CAMERA_TARGET_Z],
            ),
            quaternion_constant: Quaternion::new(
                parameters[PARAM_QUATERNION_X],
                parameters[PARAM_QUATERNION_Y],
                parameters[PARAM_QUATERNION_Z],
                parameters[PARAM_QUATERNION_W],
            ),
            field_of_view_degrees: parameters[PARAM_FIELD_OF_VIEW_DEGREES],
            palette: Palette::new(palette),
        })
    }

    fn work_plan(&self, width: usize, height: usize) -> Option<RenderWorkPlan> {
        let pixel_count = (width as u64).checked_mul(height as u64)?;
        let supersampling_squared = (self.supersampling as u64).checked_pow(2)?;
        let sampled_pixels = pixel_count.checked_mul(supersampling_squared)?;
        let (work_units, density_iterations, ray_march) = match self.kind {
            FractalKind::Buddhabrot => {
                let orbit_iterations = self.max_iterations.max(32) as u64;
                let desired_samples = (sampled_pixels / 2).max(8_192);
                let affordable_samples =
                    MAX_RENDER_WORK_UNITS.checked_sub(pixel_count)? / orbit_iterations;
                let samples = desired_samples.min(affordable_samples);
                (
                    pixel_count.checked_add(samples.checked_mul(orbit_iterations)?)?,
                    usize::try_from(samples).ok()?,
                    None,
                )
            }
            kind if kind.is_two_dimensional_density() => {
                let base = (self.max_iterations as u64)
                    .saturating_mul(128)
                    .clamp(24_000, 1_000_000);
                let desired_steps = base.checked_mul(density_resolution_multiplier(
                    pixel_count,
                    supersampling_squared,
                )?)?;
                let affordable_steps = MAX_RENDER_WORK_UNITS.checked_sub(pixel_count)? / 25;
                let steps = desired_steps.min(affordable_steps);
                (
                    pixel_count.checked_add(steps.checked_mul(25)?)?,
                    usize::try_from(steps).ok()?,
                    None,
                )
            }
            kind if kind.is_three_dimensional_attractor() => {
                let base = (self.max_iterations as u64)
                    .saturating_mul(32)
                    .clamp(12_000, 240_000);
                let desired_steps = base.checked_mul(density_resolution_multiplier(
                    pixel_count,
                    supersampling_squared,
                )?)?;
                let affordable_steps = MAX_RENDER_WORK_UNITS.checked_sub(pixel_count)? / 9;
                let steps = desired_steps.min(affordable_steps);
                (
                    pixel_count.checked_add(steps.checked_mul(9)?)?,
                    usize::try_from(steps).ok()?,
                    None,
                )
            }
            kind if kind.is_ray_marched() => {
                let plan = ray_march_plan(kind, self.max_iterations, sampled_pixels)?;
                let work = sampled_pixels
                    .checked_mul((plan.maximum_steps + 6) as u64)?
                    .checked_mul(plan.distance_iterations as u64)?;
                (work, 0, Some(plan))
            }
            _ => (
                sampled_pixels.checked_mul(self.max_iterations as u64)?,
                0,
                None,
            ),
        };
        (work_units <= MAX_RENDER_WORK_UNITS).then_some(RenderWorkPlan {
            work_units,
            density_iterations,
            ray_march,
        })
    }

    pub(crate) fn is_within_work_limit(&self, width: usize, height: usize) -> bool {
        self.work_plan(width, height).is_some()
    }
}

fn density_resolution_multiplier(pixel_count: u64, supersampling_squared: u64) -> Option<u64> {
    const REFERENCE_PIXELS: u64 = 640 * 480;
    let sampled_pixels = pixel_count.checked_mul(supersampling_squared)?;
    let ratio_ceiling = ceil_div(sampled_pixels, REFERENCE_PIXELS);
    Some(ceil_sqrt(ratio_ceiling).max(1))
}

fn ceil_div(dividend: u64, divisor: u64) -> u64 {
    dividend / divisor + u64::from(!dividend.is_multiple_of(divisor))
}

fn ceil_sqrt(value: u64) -> u64 {
    let mut lower = 1;
    let mut upper = value.max(1);
    while lower < upper {
        let middle = lower + (upper - lower) / 2;
        if middle >= value / middle + u64::from(!value.is_multiple_of(middle)) {
            upper = middle;
        } else {
            lower = middle + 1;
        }
    }
    lower
}

fn ray_march_plan(
    kind: FractalKind,
    requested_iterations: usize,
    sampled_pixels: u64,
) -> Option<RayMarchPlan> {
    let desired_steps = requested_iterations.clamp(24, 192);
    let minimum_steps = 24;
    let (desired_iterations, minimum_iterations) = match kind {
        FractalKind::MengerSponge => ((requested_iterations / 48).clamp(3, 7), 3),
        FractalKind::SierpinskiTetrahedron => ((requested_iterations / 32).clamp(6, 16), 6),
        FractalKind::OctahedralIfs
        | FractalKind::IcosahedralIfs
        | FractalKind::ApollonianGasket => ((requested_iterations / 16).clamp(4, 18), 4),
        FractalKind::Kleinian => ((requested_iterations / 16).clamp(6, 16), 6),
        FractalKind::HybridMandelbulbJulia | FractalKind::QuaternionCubic => {
            ((requested_iterations / 16).clamp(6, 24), 6)
        }
        FractalKind::SierpinskiGasket => ((requested_iterations / 16).clamp(5, 16), 5),
        FractalKind::Mandelbulb | FractalKind::Mandelbox | FractalKind::QuaternionJulia => {
            ((requested_iterations / 16).clamp(8, 28), 8)
        }
        _ => return None,
    };
    let allowance = MAX_RENDER_WORK_UNITS.checked_div(sampled_pixels)?;
    let minimum_cost = (minimum_steps + 6) as u64 * minimum_iterations as u64;
    if allowance < minimum_cost {
        return None;
    }

    let mut maximum_steps = desired_steps;
    let mut distance_iterations = desired_iterations;
    let desired_cost = (desired_steps + 6) as u64 * desired_iterations as u64;
    if desired_cost > allowance {
        distance_iterations = (allowance / (desired_steps + 6) as u64)
            .min(desired_iterations as u64)
            .max(minimum_iterations as u64) as usize;
        if (desired_steps + 6) as u64 * distance_iterations as u64 > allowance {
            maximum_steps = (allowance / distance_iterations as u64)
                .saturating_sub(6)
                .min(desired_steps as u64)
                .max(minimum_steps as u64) as usize;
        }
    }
    Some(RayMarchPlan {
        maximum_steps,
        distance_iterations,
    })
}

fn formula_parameters_are_valid(kind: FractalKind, parameters: &[f64]) -> bool {
    let a = parameters[PARAM_JULIA_REAL];
    let b = parameters[PARAM_JULIA_IMAGINARY];
    let c = parameters[PARAM_POWER];
    let d = parameters[PARAM_PHOENIX_REAL];
    match kind {
        FractalKind::Hopalong => {
            (-10.0..=10.0).contains(&a)
                && (-10.0..=10.0).contains(&b)
                && (-10.0..=10.0).contains(&c)
        }
        FractalKind::Martin => (-10.0..=10.0).contains(&a) && (2.0..=16.0).contains(&c),
        FractalKind::Chip | FractalKind::Quadruptwo | FractalKind::Threeply => {
            (-100.0..=100.0).contains(&a)
                && (-100.0..=100.0).contains(&b)
                && (-100.0..=100.0).contains(&c)
        }
        FractalKind::OctahedralIfs | FractalKind::IcosahedralIfs => {
            (0.5..=5.0).contains(&a) && (0.5..=3.0).contains(&b) && (2.0..=16.0).contains(&c)
        }
        FractalKind::ApollonianGasket => {
            (0.5..=3.0).contains(&a) && (0.8..=2.0).contains(&b) && (0.1..=2.0).contains(&c)
        }
        FractalKind::Kleinian => {
            (0.5..=3.0).contains(&a) && (0.5..=2.0).contains(&b) && (0.5..=3.0).contains(&c)
        }
        FractalKind::SierpinskiGasket => {
            (0.5..=5.0).contains(&a) && (0.0..=2.0).contains(&b) && (0.1..=2.0).contains(&c)
        }
        FractalKind::HybridMandelbulbJulia => {
            (-2.0..=2.0).contains(&a) && (-2.0..=2.0).contains(&b) && (2.0..=16.0).contains(&c)
        }
        FractalKind::QuaternionCubic => {
            parameters[PARAM_QUATERNION_X..=PARAM_QUATERNION_W]
                .iter()
                .all(|value| (-2.0..=2.0).contains(value))
                && (2.0..=16.0).contains(&c)
        }
        FractalKind::Pickover => {
            (-10.0..=10.0).contains(&a)
                && (-10.0..=10.0).contains(&b)
                && (-10.0..=10.0).contains(&c)
                && (-10.0..=10.0).contains(&d)
        }
        FractalKind::Lorenz => {
            (0.1..=30.0).contains(&a) && (0.1..=100.0).contains(&b) && (0.1..=10.0).contains(&c)
        }
        FractalKind::Rossler => {
            (-1.0..=1.0).contains(&a) && (0.01..=2.0).contains(&b) && (0.1..=20.0).contains(&c)
        }
        _ => (2.0..=16.0).contains(&c),
    }
}

struct Palette {
    colors: Vec<u32>,
}

impl Palette {
    fn new(colors: &[i32]) -> Self {
        Self {
            colors: colors.iter().map(|color| *color as u32).collect(),
        }
    }

    fn sample(&self, position: f64) -> u32 {
        let wrapped = if position.is_finite() {
            position.rem_euclid(1.0)
        } else {
            0.0
        };
        let scaled = wrapped * (self.colors.len() - 1) as f64;
        let first_index = scaled.floor() as usize;
        let second_index = (first_index + 1).min(self.colors.len() - 1);
        interpolate_color(
            self.colors[first_index],
            self.colors[second_index],
            scaled - first_index as f64,
        )
    }
}

struct EscapeSample {
    iterations: usize,
    escaped: bool,
    smooth_iteration: f64,
    orbit_distance: f64,
    angle: f64,
}

struct PerturbationReference {
    real: Vec<f64>,
    imaginary: Vec<f64>,
    julia: bool,
}

impl PerturbationReference {
    fn create(settings: &RenderSettings, cancelled: &AtomicBool) -> Result<Option<Self>, ()> {
        if !matches!(settings.kind, FractalKind::Mandelbrot | FractalKind::Julia)
            || settings.power != 2.0
            || settings
                .exact_vertical_span
                .cmp(&Decimal::parse("1E-12").expect("constant decimal"))
                .is_gt()
        {
            return Ok(None);
        }

        let span_magnitude = settings
            .exact_vertical_span
            .floor_log10_abs()
            .unwrap_or(-300);
        let fractional_digits = (-span_magnitude).max(0) as u32 + REFERENCE_GUARD_DIGITS;
        let scale = power_of_ten(fractional_digits);
        let zero = BigInt::from(0_u8);
        let julia = settings.kind == FractalKind::Julia;
        let mut z_real = if julia {
            settings.exact_center_x.to_fixed(fractional_digits)
        } else {
            zero.clone()
        };
        let mut z_imaginary = if julia {
            settings.exact_center_y.to_fixed(fractional_digits)
        } else {
            zero.clone()
        };
        let c_real = if julia {
            Decimal::from_f64(settings.julia_constant.re)
                .expect("validated Julia constant")
                .to_fixed(fractional_digits)
        } else {
            settings.exact_center_x.to_fixed(fractional_digits)
        };
        let c_imaginary = if julia {
            Decimal::from_f64(settings.julia_constant.im)
                .expect("validated Julia constant")
                .to_fixed(fractional_digits)
        } else {
            settings.exact_center_y.to_fixed(fractional_digits)
        };
        let mut real = vec![f64::NAN; settings.max_iterations + 1];
        let mut imaginary = vec![f64::NAN; settings.max_iterations + 1];
        real[0] = fixed_to_f64(&z_real, fractional_digits);
        imaginary[0] = fixed_to_f64(&z_imaginary, fractional_digits);

        for index in 0..settings.max_iterations {
            if index & REFERENCE_CANCELLATION_CHECK_MASK == 0 && cancelled.load(Ordering::Acquire) {
                return Err(());
            }
            let next_real = (&z_real * &z_real - &z_imaginary * &z_imaginary) / &scale + &c_real;
            let next_imaginary = ((&z_real * &z_imaginary) << 1) / &scale + &c_imaginary;
            z_real = next_real;
            z_imaginary = next_imaginary;
            real[index + 1] = fixed_to_f64(&z_real, fractional_digits);
            imaginary[index + 1] = fixed_to_f64(&z_imaginary, fractional_digits);

            if !real[index + 1].is_finite()
                || !imaginary[index + 1].is_finite()
                || real[index + 1].abs() > MAX_REFERENCE_MAGNITUDE
                || imaginary[index + 1].abs() > MAX_REFERENCE_MAGNITUDE
            {
                break;
            }
        }

        Ok(Some(Self {
            real,
            imaginary,
            julia,
        }))
    }

    fn sample(
        &self,
        delta_real: f64,
        delta_imaginary: f64,
        settings: &RenderSettings,
        cancelled: &AtomicBool,
    ) -> EscapeSample {
        let mut dz_real = if self.julia { delta_real } else { 0.0 };
        let mut dz_imaginary = if self.julia { delta_imaginary } else { 0.0 };
        let dc_real = if self.julia { 0.0 } else { delta_real };
        let dc_imaginary = if self.julia { 0.0 } else { delta_imaginary };
        let bailout_squared = settings.bailout * settings.bailout;
        let mut orbit_distance = f64::INFINITY;
        let mut magnitude_squared = 0.0;
        let mut actual_real = self.real[0] + dz_real;
        let mut actual_imaginary = self.imaginary[0] + dz_imaginary;
        let mut iterations = 0;
        let mut reference_index = 0;
        let mut escaped = false;

        while iterations < settings.max_iterations {
            if iterations > 0
                && iterations & PERTURBATION_CANCELLATION_CHECK_MASK == 0
                && cancelled.load(Ordering::Acquire)
            {
                break;
            }

            let reference_real = self.real[reference_index];
            let reference_imaginary = self.imaginary[reference_index];
            let next_reference_real = self.real[reference_index + 1];
            let next_reference_imaginary = self.imaginary[reference_index + 1];
            if !next_reference_real.is_finite() || !next_reference_imaginary.is_finite() {
                escaped = true;
                break;
            }

            let old_delta_real = dz_real;
            let old_delta_imaginary = dz_imaginary;
            dz_real = 2.0
                * (reference_real * old_delta_real - reference_imaginary * old_delta_imaginary)
                + old_delta_real * old_delta_real
                - old_delta_imaginary * old_delta_imaginary
                + dc_real;
            dz_imaginary = 2.0
                * (reference_real * old_delta_imaginary + reference_imaginary * old_delta_real)
                + 2.0 * old_delta_real * old_delta_imaginary
                + dc_imaginary;
            actual_real = next_reference_real + dz_real;
            actual_imaginary = next_reference_imaginary + dz_imaginary;
            iterations += 1;

            magnitude_squared = actual_real * actual_real + actual_imaginary * actual_imaginary;
            orbit_distance = orbit_distance.min(
                actual_real
                    .abs()
                    .min(actual_imaginary.abs())
                    .min((magnitude_squared.max(0.0).sqrt() - 1.0).abs()),
            );
            if !magnitude_squared.is_finite() || magnitude_squared > bailout_squared {
                escaped = true;
                break;
            }

            let reference_magnitude_squared = next_reference_real * next_reference_real
                + next_reference_imaginary * next_reference_imaginary;
            if reference_index > 0
                && reference_magnitude_squared.is_finite()
                && magnitude_squared < GLITCH_THRESHOLD * reference_magnitude_squared
            {
                dz_real = actual_real - self.real[0];
                dz_imaginary = actual_imaginary - self.imaginary[0];
                reference_index = 0;
            } else {
                reference_index += 1;
            }
        }

        let safe_magnitude_squared = if magnitude_squared.is_finite() && magnitude_squared > 1.0 {
            magnitude_squared
        } else {
            bailout_squared.max(4.0)
        };
        let smooth_iteration = if escaped {
            let value =
                iterations as f64 + 1.0 - safe_magnitude_squared.sqrt().ln().ln() / 2.0_f64.ln();
            if value.is_finite() {
                value
            } else {
                iterations as f64
            }
        } else {
            iterations as f64
        };

        EscapeSample {
            iterations,
            escaped,
            smooth_iteration,
            orbit_distance: if orbit_distance.is_finite() {
                orbit_distance
            } else {
                1.0
            },
            angle: (actual_imaginary.atan2(actual_real) / (2.0 * PI) + 1.0).rem_euclid(1.0),
        }
    }
}

const REFERENCE_GUARD_DIGITS: u32 = 20;
const MAX_REFERENCE_MAGNITUDE: f64 = 1.0e100;
const GLITCH_THRESHOLD: f64 = 1.0e-6;
const PERTURBATION_CANCELLATION_CHECK_MASK: usize = 255;
const REFERENCE_CANCELLATION_CHECK_MASK: usize = 15;

pub(crate) fn render_into(
    settings: &RenderSettings,
    target: &mut [u8],
    width: usize,
    height: usize,
    stride: usize,
    alpha_mode: BitmapAlphaMode,
    cancelled: &AtomicBool,
) -> Option<RenderOutcome> {
    let required_bytes = height
        .checked_sub(1)?
        .checked_mul(stride)?
        .checked_add(width.checked_mul(4)?)?;
    if width == 0 || height == 0 || stride < width * 4 || target.len() < required_bytes {
        return None;
    }
    let Some(work_plan) = settings.work_plan(width, height) else {
        return Some(RenderOutcome::WorkLimitExceeded);
    };

    if settings.kind.is_two_dimensional_density() {
        return Some(render_2d_density(
            settings,
            work_plan.density_iterations,
            target,
            width,
            height,
            stride,
            alpha_mode,
            cancelled,
        ));
    }
    if settings.kind.is_three_dimensional_attractor() {
        return Some(render_3d_attractor_density(
            settings,
            work_plan.density_iterations,
            target,
            width,
            height,
            stride,
            alpha_mode,
            cancelled,
        ));
    }

    let perturbation = match PerturbationReference::create(settings, cancelled) {
        Ok(value) => value,
        Err(()) => return Some(RenderOutcome::Cancelled),
    };
    let pool = render_pool()?;
    let camera = settings
        .kind
        .is_three_dimensional()
        .then(|| Camera::new(settings));
    let result = pool.install(|| {
        target
            .par_chunks_mut(stride)
            .take(height)
            .enumerate()
            .try_for_each(|(y, row)| {
                render_row(
                    settings,
                    perturbation.as_ref(),
                    camera.as_ref(),
                    work_plan.ray_march,
                    row,
                    y,
                    width,
                    height,
                    alpha_mode,
                    cancelled,
                )
            })
    });
    Some(if result.is_ok() {
        RenderOutcome::Completed
    } else {
        RenderOutcome::Cancelled
    })
}

static RENDER_POOL: OnceLock<Result<ThreadPool, ()>> = OnceLock::new();

fn render_pool_thread_count(available_parallelism: usize) -> usize {
    available_parallelism.saturating_sub(1).clamp(1, 8)
}

fn render_pool() -> Option<&'static ThreadPool> {
    RENDER_POOL
        .get_or_init(|| {
            let threads = std::thread::available_parallelism()
                .map(usize::from)
                .map(render_pool_thread_count)
                .unwrap_or(1);
            ThreadPoolBuilder::new()
                .num_threads(threads)
                .thread_name(|index| format!("fractal-render-{index}"))
                .build()
                .map_err(|_| ())
        })
        .as_ref()
        .ok()
}

#[allow(clippy::too_many_arguments)]
fn render_2d_density(
    settings: &RenderSettings,
    iterations: usize,
    target: &mut [u8],
    width: usize,
    height: usize,
    stride: usize,
    alpha_mode: BitmapAlphaMode,
    cancelled: &AtomicBool,
) -> RenderOutcome {
    let viewport = DensityViewport {
        width,
        height,
        center_x: settings.center_x,
        center_y: settings.center_y,
        vertical_span: settings.vertical_span,
        aspect_ratio: settings.viewport_aspect_ratio,
    };
    let density = match settings.kind {
        FractalKind::Buddhabrot => {
            buddhabrot_density(viewport, settings.max_iterations, iterations, cancelled)
        }
        kind => attractor_2d_density(
            match kind {
                FractalKind::Hopalong => Attractor2dKind::Hopalong,
                FractalKind::Martin => Attractor2dKind::Martin,
                FractalKind::Gingerbreadman => Attractor2dKind::Gingerbreadman,
                FractalKind::Chip => Attractor2dKind::Chip,
                FractalKind::Quadruptwo => Attractor2dKind::Quadruptwo,
                FractalKind::Threeply => Attractor2dKind::Threeply,
                _ => unreachable!("density dispatch only accepts 2D density formulas"),
            },
            Attractor2dParameters {
                a: settings.julia_constant.re,
                b: settings.julia_constant.im,
                c: settings.power,
            },
            viewport,
            iterations,
            cancelled,
        ),
    };
    let density = match density {
        Ok(density) => density,
        Err(DensityError::Cancelled) => return RenderOutcome::Cancelled,
        Err(DensityError::AllocationFailed) => return RenderOutcome::AllocationFailed,
    };
    write_density(
        settings, &density, target, width, height, stride, alpha_mode, cancelled,
    )
}

#[allow(clippy::too_many_arguments)]
fn render_3d_attractor_density(
    settings: &RenderSettings,
    iterations: usize,
    target: &mut [u8],
    width: usize,
    height: usize,
    stride: usize,
    alpha_mode: BitmapAlphaMode,
    cancelled: &AtomicBool,
) -> RenderOutcome {
    let kind = match settings.kind {
        FractalKind::Pickover => Attractor3dKind::Pickover,
        FractalKind::Lorenz => Attractor3dKind::Lorenz,
        FractalKind::Rossler => Attractor3dKind::Rossler,
        _ => unreachable!("attractor dispatch only accepts 3D attractors"),
    };
    let camera = Camera::new(settings);
    let Some(pixel_count) = width.checked_mul(height) else {
        return RenderOutcome::AllocationFailed;
    };
    let mut density = match try_zeroed_density(pixel_count) {
        Ok(density) => density,
        Err(DensityError::AllocationFailed) => return RenderOutcome::AllocationFailed,
        Err(DensityError::Cancelled) => unreachable!("allocation cannot be cancelled"),
    };
    let result = for_each_attractor_3d_point(
        kind,
        Attractor3dParameters {
            a: settings.julia_constant.re,
            b: settings.julia_constant.im,
            c: settings.power,
            d: settings.phoenix_constant.re,
        },
        iterations,
        cancelled,
        |point| {
            let Some((pixel_x, pixel_y, depth)) =
                camera.project(point, width, height, settings.viewport_aspect_ratio)
            else {
                return;
            };
            let weight = (24.0 / depth.max(0.25)).round().clamp(1.0, 24.0) as u32;
            plot_projected_density(&mut density, width, height, pixel_x, pixel_y, weight);
        },
    );
    match result {
        Ok(()) => {}
        Err(DensityError::Cancelled) => return RenderOutcome::Cancelled,
        Err(DensityError::AllocationFailed) => return RenderOutcome::AllocationFailed,
    }
    write_density(
        settings, &density, target, width, height, stride, alpha_mode, cancelled,
    )
}

#[allow(clippy::too_many_arguments)]
fn write_density(
    settings: &RenderSettings,
    density: &[u32],
    target: &mut [u8],
    width: usize,
    height: usize,
    stride: usize,
    alpha_mode: BitmapAlphaMode,
    cancelled: &AtomicBool,
) -> RenderOutcome {
    let maximum = density.iter().copied().max().unwrap_or(0);
    let logarithmic_maximum = (maximum as f64 + 1.0).ln().max(1.0);
    for (y, row) in target.chunks_mut(stride).take(height).enumerate() {
        if cancelled.load(Ordering::Acquire) {
            return RenderOutcome::Cancelled;
        }
        for x in 0..width {
            let value = density[y * width + x];
            let color = if value == 0 {
                settings.inside_color
            } else {
                let intensity = (value as f64 + 1.0).ln() / logarithmic_maximum;
                settings.palette.sample(
                    (0.06 + intensity.powf(0.58) * 0.92) * settings.palette_cycles
                        + settings.palette_offset,
                )
            };
            write_pixel(row, x, color, alpha_mode);
        }
    }
    RenderOutcome::Completed
}

fn plot_projected_density(
    density: &mut [u32],
    width: usize,
    height: usize,
    center_x: f64,
    center_y: f64,
    weight: u32,
) {
    let center_x = center_x.floor() as isize;
    let center_y = center_y.floor() as isize;
    for offset_y in -1..=1 {
        for offset_x in -1..=1 {
            let x = center_x + offset_x;
            let y = center_y + offset_y;
            if x < 0 || y < 0 || x >= width as isize || y >= height as isize {
                continue;
            }
            let falloff = if offset_x == 0 && offset_y == 0 { 2 } else { 1 };
            let index = y as usize * width + x as usize;
            density[index] = density[index].saturating_add(weight.saturating_mul(falloff));
        }
    }
}

fn write_pixel(row: &mut [u8], x: usize, color: u32, alpha_mode: BitmapAlphaMode) {
    let source_alpha = color >> 24 & 0xff;
    let source_red = color >> 16 & 0xff;
    let source_green = color >> 8 & 0xff;
    let source_blue = color & 0xff;
    let (red, green, blue, alpha) = match alpha_mode {
        BitmapAlphaMode::Premultiplied => (
            (source_red * source_alpha + 127) / 255,
            (source_green * source_alpha + 127) / 255,
            (source_blue * source_alpha + 127) / 255,
            source_alpha,
        ),
        BitmapAlphaMode::Unpremultiplied => (source_red, source_green, source_blue, source_alpha),
        BitmapAlphaMode::Opaque => (source_red, source_green, source_blue, 255),
    };
    let offset = x * 4;
    row[offset] = red as u8;
    row[offset + 1] = green as u8;
    row[offset + 2] = blue as u8;
    row[offset + 3] = alpha as u8;
}

#[allow(clippy::too_many_arguments)]
fn render_row(
    settings: &RenderSettings,
    perturbation: Option<&PerturbationReference>,
    camera: Option<&Camera>,
    ray_march_plan: Option<RayMarchPlan>,
    row: &mut [u8],
    y: usize,
    width: usize,
    height: usize,
    alpha_mode: BitmapAlphaMode,
    cancelled: &AtomicBool,
) -> Result<(), ()> {
    if cancelled.load(Ordering::Acquire) {
        return Err(());
    }
    for x in 0..width {
        if x & 7 == 0 && cancelled.load(Ordering::Acquire) {
            return Err(());
        }

        let mut alpha_sum = 0_u64;
        let mut premultiplied_red_sum = 0_u64;
        let mut premultiplied_green_sum = 0_u64;
        let mut premultiplied_blue_sum = 0_u64;
        for sample_y in 0..settings.supersampling {
            for sample_x in 0..settings.supersampling {
                let subpixel_x = (sample_x as f64 + 0.5) / settings.supersampling as f64;
                let subpixel_y = (sample_y as f64 + 0.5) / settings.supersampling as f64;
                let color = if let Some(camera) = camera {
                    render_3d_sample(
                        settings,
                        camera,
                        ray_march_plan.expect("3D rendering requires a ray-march work plan"),
                        x as f64 + subpixel_x,
                        y as f64 + subpixel_y,
                        width,
                        height,
                        cancelled,
                    )
                } else {
                    render_2d_sample(
                        settings,
                        perturbation,
                        x as f64 + subpixel_x,
                        y as f64 + subpixel_y,
                        width,
                        height,
                        cancelled,
                    )
                };
                let alpha = (color >> 24 & 0xff) as u64;
                alpha_sum += alpha;
                premultiplied_red_sum += (color >> 16 & 0xff) as u64 * alpha;
                premultiplied_green_sum += (color >> 8 & 0xff) as u64 * alpha;
                premultiplied_blue_sum += (color & 0xff) as u64 * alpha;
            }
        }

        let sample_count = (settings.supersampling * settings.supersampling) as u64;
        let averaged_alpha = divide_rounded(alpha_sum, sample_count);
        let (red, green, blue, alpha) = match alpha_mode {
            BitmapAlphaMode::Premultiplied => (
                divide_rounded(premultiplied_red_sum, sample_count * 255),
                divide_rounded(premultiplied_green_sum, sample_count * 255),
                divide_rounded(premultiplied_blue_sum, sample_count * 255),
                averaged_alpha,
            ),
            BitmapAlphaMode::Unpremultiplied => (
                divide_rounded(premultiplied_red_sum, alpha_sum.max(1)),
                divide_rounded(premultiplied_green_sum, alpha_sum.max(1)),
                divide_rounded(premultiplied_blue_sum, alpha_sum.max(1)),
                averaged_alpha,
            ),
            BitmapAlphaMode::Opaque => (
                divide_rounded(premultiplied_red_sum, alpha_sum.max(1)),
                divide_rounded(premultiplied_green_sum, alpha_sum.max(1)),
                divide_rounded(premultiplied_blue_sum, alpha_sum.max(1)),
                255,
            ),
        };
        let pixel_offset = x * 4;
        row[pixel_offset] = red as u8;
        row[pixel_offset + 1] = green as u8;
        row[pixel_offset + 2] = blue as u8;
        row[pixel_offset + 3] = alpha as u8;
    }
    Ok(())
}

fn divide_rounded(numerator: u64, denominator: u64) -> u64 {
    (numerator + denominator / 2) / denominator
}

fn render_2d_sample(
    settings: &RenderSettings,
    perturbation: Option<&PerturbationReference>,
    pixel_x: f64,
    pixel_y: f64,
    width: usize,
    height: usize,
    cancelled: &AtomicBool,
) -> u32 {
    let normalized_x = pixel_x / width as f64;
    let normalized_y = pixel_y / height as f64;
    let delta_real = (normalized_x - 0.5) * settings.vertical_span * settings.viewport_aspect_ratio;
    let delta_imaginary = (0.5 - normalized_y) * settings.vertical_span;
    if let Some(perturbation) = perturbation {
        return color_escape(
            settings,
            perturbation.sample(delta_real, delta_imaginary, settings, cancelled),
        );
    }
    let point = Complex::new(
        settings.center_x + delta_real,
        settings.center_y + delta_imaginary,
    );

    match settings.kind {
        FractalKind::MagnetI | FractalKind::MagnetII => {
            color_escape(settings, iterate_magnet(settings, point, cancelled))
        }
        FractalKind::Lyapunov => color_lyapunov(settings, point, cancelled),
        FractalKind::SierpinskiCarpet => {
            color_geometry(settings, sierpinski_carpet(point, settings.max_iterations))
        }
        FractalKind::SierpinskiTriangle => color_geometry(
            settings,
            sierpinski_triangle(point, settings.max_iterations),
        ),
        FractalKind::Collatz => color_collatz(settings, point, cancelled),
        _ => color_escape(settings, iterate_escape_time(settings, point, cancelled)),
    }
}

fn iterate_escape_time(
    settings: &RenderSettings,
    point: Complex,
    cancelled: &AtomicBool,
) -> EscapeSample {
    let (mut z, c) = match settings.kind {
        FractalKind::Julia | FractalKind::BurningShipJulia | FractalKind::CelticJulia => {
            (point, settings.julia_constant)
        }
        FractalKind::Nova => (Complex::new(1.0, 0.0), point),
        FractalKind::Newton => (point, Complex::default()),
        _ => (Complex::default(), point),
    };
    let mut previous = Complex::default();
    let mut orbit_distance = f64::INFINITY;
    let mut magnitude_squared = z.norm_squared();
    let mut iterations = 0;
    let mut escaped = false;

    while iterations < settings.max_iterations {
        if iterations > 0 && iterations & 255 == 0 && cancelled.load(Ordering::Acquire) {
            break;
        }
        let old = z;
        let mut converged = false;
        z = match settings.kind {
            FractalKind::Mandelbrot | FractalKind::Multibrot | FractalKind::Julia => {
                old.powf(settings.power) + c
            }
            FractalKind::BurningShip | FractalKind::BurningShipJulia => {
                old.component_abs().powf(settings.power) + c
            }
            FractalKind::Tricorn | FractalKind::Multicorn => {
                old.conjugate().powf(settings.power) + c
            }
            FractalKind::Celtic | FractalKind::CelticJulia => {
                let powered = old.powf(settings.power);
                Complex::new(powered.re.abs() + c.re, powered.im + c.im)
            }
            FractalKind::Buffalo => {
                let powered = old.component_abs().powf(settings.power);
                Complex::new(powered.re.abs() + c.re, powered.im.abs() + c.im)
            }
            FractalKind::PerpendicularBurningShip => {
                Complex::new(old.re.abs(), old.im).powf(settings.power) + c
            }
            FractalKind::Phoenix => {
                let next = old.powf(settings.power) + c + previous * settings.phoenix_constant;
                previous = old;
                next
            }
            FractalKind::Nova | FractalKind::Newton => {
                let numerator = old.powf(settings.power) - Complex::new(1.0, 0.0);
                let denominator = old.powf(settings.power - 1.0) * settings.power;
                let Some(quotient) = numerator.checked_div(denominator) else {
                    iterations += 1;
                    break;
                };
                let relaxation = if settings.kind == FractalKind::Nova {
                    settings.nova_relaxation
                } else {
                    1.0
                };
                let next = old - quotient * relaxation + c;
                converged = (next - old).norm_squared() < 1.0e-20;
                next
            }
            _ => old,
        };

        iterations += 1;
        magnitude_squared = z.norm_squared();
        orbit_distance = orbit_distance.min(
            z.re.abs()
                .min(z.im.abs())
                .min((magnitude_squared.max(0.0).sqrt() - 1.0).abs()),
        );
        if converged
            || !magnitude_squared.is_finite()
            || magnitude_squared > settings.bailout * settings.bailout
        {
            escaped = true;
            break;
        }
    }

    let safe_magnitude_squared = if magnitude_squared.is_finite() && magnitude_squared > 1.0 {
        magnitude_squared
    } else {
        (settings.bailout * settings.bailout).max(4.0)
    };
    let smooth_iteration =
        if escaped && settings.kind != FractalKind::Newton && settings.kind != FractalKind::Nova {
            let value = iterations as f64 + 1.0
                - safe_magnitude_squared.sqrt().ln().ln() / settings.power.max(2.0).ln();
            if value.is_finite() {
                value
            } else {
                iterations as f64
            }
        } else {
            iterations as f64
        };

    EscapeSample {
        iterations,
        escaped,
        smooth_iteration,
        orbit_distance: if orbit_distance.is_finite() {
            orbit_distance
        } else {
            1.0
        },
        angle: (z.im.atan2(z.re) / (2.0 * PI) + 1.0).rem_euclid(1.0),
    }
}

fn iterate_magnet(settings: &RenderSettings, c: Complex, cancelled: &AtomicBool) -> EscapeSample {
    let mut z = Complex::default();
    let one = Complex::new(1.0, 0.0);
    let two = Complex::new(2.0, 0.0);
    let mut orbit_distance = f64::INFINITY;
    let mut iterations = 0;
    let mut escaped = false;

    while iterations < settings.max_iterations {
        if iterations > 0 && iterations & 255 == 0 && cancelled.load(Ordering::Acquire) {
            break;
        }
        let quotient = if settings.kind == FractalKind::MagnetI {
            let numerator = z * z + c - one;
            let denominator = z * 2.0 + c - two;
            numerator.checked_div(denominator)
        } else {
            let c_minus_one = c - one;
            let c_minus_two = c - two;
            let common = c_minus_one * c_minus_two;
            let numerator = z.powf(3.0) + c_minus_one * z * 3.0 + common;
            let denominator = z * z * 3.0 + c_minus_two * z * 3.0 + common + one;
            numerator.checked_div(denominator)
        };
        let Some(quotient) = quotient else {
            escaped = true;
            iterations += 1;
            break;
        };
        z = quotient * quotient;
        iterations += 1;
        let distance_to_one = (z - one).norm_squared();
        orbit_distance = orbit_distance.min(distance_to_one.sqrt());
        if !z.is_finite() || z.norm_squared() > settings.bailout.max(100.0).powi(2) {
            escaped = true;
            break;
        }
        if distance_to_one < 1.0e-20 {
            break;
        }
    }

    EscapeSample {
        iterations,
        escaped,
        smooth_iteration: iterations as f64,
        orbit_distance: orbit_distance.max(1.0e-15),
        angle: (z.im.atan2(z.re) / (2.0 * PI) + 1.0).rem_euclid(1.0),
    }
}

fn color_escape(settings: &RenderSettings, sample: EscapeSample) -> u32 {
    if !sample.escaped {
        return settings.inside_color;
    }

    let iteration_fraction = sample.iterations as f64 / settings.max_iterations as f64;
    let position = match settings.coloring {
        Coloring::Smooth => sample.smooth_iteration / settings.max_iterations as f64,
        Coloring::Banded => (iteration_fraction * 64.0).floor() / 64.0,
        Coloring::OrbitTrap => {
            -sample.orbit_distance.clamp(1.0e-15, 1.0).ln() / 16.0 + iteration_fraction * 0.15
        }
        Coloring::Angle => sample.angle + iteration_fraction * 0.1,
    };
    settings
        .palette
        .sample(position * settings.palette_cycles + settings.palette_offset)
}

fn color_lyapunov(settings: &RenderSettings, point: Complex, cancelled: &AtomicBool) -> u32 {
    let Some(exponent) = lyapunov_exponent(settings, point, cancelled) else {
        return settings.inside_color;
    };
    let position = if exponent < 0.0 {
        0.42 + (-exponent / 2.5).clamp(0.0, 1.0) * 0.58
    } else {
        0.18 * (1.0 - (exponent / 2.5).clamp(0.0, 1.0))
    };
    settings
        .palette
        .sample(position * settings.palette_cycles + settings.palette_offset)
}

fn color_collatz(settings: &RenderSettings, point: Complex, cancelled: &AtomicBool) -> u32 {
    let mut value = point;
    let mut minimum_distance = f64::INFINITY;
    let bailout_squared = settings.bailout * settings.bailout;
    let mut palette_position = None;
    for iteration in 0..settings.max_iterations {
        if iteration & 255 == 0 && cancelled.load(Ordering::Acquire) {
            return settings.inside_color;
        }
        let magnitude_squared = value.norm_squared();
        if !magnitude_squared.is_finite() || magnitude_squared > bailout_squared {
            let logarithm = magnitude_squared.max(1.0).ln() * 0.5;
            let smooth =
                iteration as f64 + 1.0 - (logarithm / 2.0_f64.ln()).max(1.0e-4).ln() / 2.0_f64.ln();
            palette_position = Some((smooth / 50.0).rem_euclid(1.0));
            break;
        }
        minimum_distance = minimum_distance.min(magnitude_squared.sqrt());

        let real = PI * value.re;
        let imaginary = PI * value.im;
        let cosine = Complex::new(
            real.cos() * imaginary.cosh(),
            -real.sin() * imaginary.sinh(),
        );
        value = (Complex::new(2.0, 0.0) + value * 7.0
            - (Complex::new(2.0, 0.0) + value * 5.0) * cosine)
            * 0.25;
    }
    let position = palette_position.unwrap_or_else(|| {
        if minimum_distance.is_finite() {
            (minimum_distance * 2.0).rem_euclid(1.0)
        } else {
            0.0
        }
    });
    settings
        .palette
        .sample(position * settings.palette_cycles + settings.palette_offset)
}

fn lyapunov_exponent(
    settings: &RenderSettings,
    point: Complex,
    cancelled: &AtomicBool,
) -> Option<f64> {
    let mut state = 0.5_f64;
    let warmup = 32;
    for index in 0..warmup {
        let rate = if settings.lyapunov_sequence[index % settings.lyapunov_sequence.len()] {
            point.im
        } else {
            point.re
        };
        state = rate * state * (1.0 - state);
    }

    let mut sum = 0.0;
    for index in 0..settings.max_iterations {
        if index & 255 == 0 && cancelled.load(Ordering::Acquire) {
            return None;
        }
        let rate = if settings.lyapunov_sequence[index % settings.lyapunov_sequence.len()] {
            point.im
        } else {
            point.re
        };
        let derivative = (rate * (1.0 - 2.0 * state)).abs().max(1.0e-15);
        sum += derivative.ln();
        state = rate * state * (1.0 - state);
    }

    Some(sum / settings.max_iterations as f64)
}

fn sierpinski_carpet(point: Complex, requested_depth: usize) -> Option<f64> {
    let mut x = (point.re + 1.0) * 0.5;
    let mut y = (point.im + 1.0) * 0.5;
    if !(0.0..=1.0).contains(&x) || !(0.0..=1.0).contains(&y) {
        return None;
    }
    x = x.min(1.0 - f64::EPSILON);
    y = y.min(1.0 - f64::EPSILON);
    let depth = requested_depth.clamp(1, 14);
    for iteration in 0..depth {
        x *= 3.0;
        y *= 3.0;
        let cell_x = x.floor() as i32;
        let cell_y = y.floor() as i32;
        if cell_x == 1 && cell_y == 1 {
            return None;
        }
        x = x.fract();
        y = y.fract();
        if iteration + 1 == depth {
            return Some((iteration + 1) as f64 / depth as f64);
        }
    }
    Some(1.0)
}

fn sierpinski_triangle(point: Complex, requested_depth: usize) -> Option<f64> {
    let top = (0.0, 0.96);
    let left = (-1.0, -0.78);
    let right = (1.0, -0.78);
    let denominator =
        (left.1 - right.1) * (top.0 - right.0) + (right.0 - left.0) * (top.1 - right.1);
    let mut a = ((left.1 - right.1) * (point.re - right.0)
        + (right.0 - left.0) * (point.im - right.1))
        / denominator;
    let mut b = ((right.1 - top.1) * (point.re - right.0)
        + (top.0 - right.0) * (point.im - right.1))
        / denominator;
    let mut c = 1.0 - a - b;
    if a < 0.0 || b < 0.0 || c < 0.0 {
        return None;
    }

    let depth = requested_depth.clamp(1, 18);
    for iteration in 0..depth {
        if a >= 0.5 {
            a = a * 2.0 - 1.0;
            b *= 2.0;
            c *= 2.0;
        } else if b >= 0.5 {
            b = b * 2.0 - 1.0;
            a *= 2.0;
            c *= 2.0;
        } else if c >= 0.5 {
            c = c * 2.0 - 1.0;
            a *= 2.0;
            b *= 2.0;
        } else {
            return None;
        }
        if iteration + 1 == depth {
            return Some((iteration + 1) as f64 / depth as f64);
        }
    }
    Some(1.0)
}

fn color_geometry(settings: &RenderSettings, depth: Option<f64>) -> u32 {
    let Some(depth) = depth else {
        return settings.inside_color;
    };
    settings
        .palette
        .sample((0.2 + depth * 0.72) * settings.palette_cycles + settings.palette_offset)
}

struct Camera {
    origin: Vec3,
    forward: Vec3,
    right: Vec3,
    up: Vec3,
    tangent_half_field_of_view: f64,
}

impl Camera {
    fn new(settings: &RenderSettings) -> Self {
        let pitch = settings.camera_pitch.clamp(-PI * 0.49, PI * 0.49);
        let orbit = Vec3::new(
            pitch.cos() * settings.camera_yaw.cos(),
            pitch.sin(),
            pitch.cos() * settings.camera_yaw.sin(),
        ) * settings.camera_distance;
        let origin = settings.camera_target + orbit;
        let forward = (settings.camera_target - origin).normalized();
        let mut right = forward.cross(Vec3::new(0.0, 1.0, 0.0)).normalized();
        if right.length() < 1.0e-10 {
            right = Vec3::new(1.0, 0.0, 0.0);
        }
        let up = right.cross(forward).normalized();
        Self {
            origin,
            forward,
            right,
            up,
            tangent_half_field_of_view: (settings.field_of_view_degrees.to_radians() * 0.5).tan(),
        }
    }

    fn ray_direction(
        &self,
        pixel_x: f64,
        pixel_y: f64,
        width: usize,
        height: usize,
        viewport_aspect_ratio: f64,
    ) -> Vec3 {
        let x = (pixel_x / width as f64 * 2.0 - 1.0)
            * viewport_aspect_ratio
            * self.tangent_half_field_of_view;
        let y = (1.0 - pixel_y / height as f64 * 2.0) * self.tangent_half_field_of_view;
        (self.forward + self.right * x + self.up * y).normalized()
    }

    fn project(
        &self,
        point: Vec3,
        width: usize,
        height: usize,
        viewport_aspect_ratio: f64,
    ) -> Option<(f64, f64, f64)> {
        let relative = point - self.origin;
        let depth = relative.dot(self.forward);
        if !depth.is_finite() || depth <= 1.0e-6 {
            return None;
        }
        let horizontal = relative.dot(self.right)
            / (depth * self.tangent_half_field_of_view * viewport_aspect_ratio);
        let vertical = relative.dot(self.up) / (depth * self.tangent_half_field_of_view);
        let pixel_x = (horizontal + 1.0) * 0.5 * width as f64;
        let pixel_y = (1.0 - vertical) * 0.5 * height as f64;
        if pixel_x < -1.0
            || pixel_y < -1.0
            || pixel_x > width as f64 + 1.0
            || pixel_y > height as f64 + 1.0
        {
            return None;
        }
        Some((pixel_x, pixel_y, depth))
    }
}

#[allow(clippy::too_many_arguments)]
fn render_3d_sample(
    settings: &RenderSettings,
    camera: &Camera,
    plan: RayMarchPlan,
    pixel_x: f64,
    pixel_y: f64,
    width: usize,
    height: usize,
    cancelled: &AtomicBool,
) -> u32 {
    let ray_direction = camera.ray_direction(
        pixel_x,
        pixel_y,
        width,
        height,
        settings.viewport_aspect_ratio,
    );
    let maximum_steps = plan.maximum_steps;
    let maximum_distance = settings.camera_distance + 12.0;
    let mut travel = 0.0;
    let mut hit = None;

    for step in 0..maximum_steps {
        if step & 7 == 0 && cancelled.load(Ordering::Acquire) {
            return settings.inside_color;
        }
        let point = camera.origin + ray_direction * travel;
        let distance = distance_estimate(settings, point, plan.distance_iterations);
        if !distance.is_finite() {
            break;
        }
        let epsilon = if settings.kind == FractalKind::Kleinian {
            3.5e-4
        } else {
            0.00045 * (1.0 + travel * 0.08)
        };
        if distance.abs() < epsilon {
            hit = Some((point, step));
            break;
        }
        travel += distance.abs().clamp(epsilon * 0.5, 0.75);
        if travel > maximum_distance {
            break;
        }
    }

    let Some((point, step)) = hit else {
        return settings.inside_color;
    };
    let normal = surface_normal(
        settings,
        point,
        0.0009 * (1.0 + travel * 0.05),
        plan.distance_iterations,
    );
    let light_direction = Vec3::new(-0.45, 0.8, -0.38).normalized();
    let diffuse = normal.dot(light_direction).max(0.0);
    let half_vector = (light_direction - ray_direction).normalized();
    let specular = normal.dot(half_vector).max(0.0).powf(28.0);
    let rim = (1.0 - normal.dot(ray_direction * -1.0).abs()).powf(2.0);
    let step_detail = step as f64 / maximum_steps as f64;
    let palette_position = (0.12 + diffuse * 0.52 + rim * 0.18 + step_detail * 0.18)
        * settings.palette_cycles
        + settings.palette_offset;
    let surface_color = settings.palette.sample(palette_position);
    let shaded = shade_color(surface_color, 0.28 + diffuse * 0.72, specular * 0.55);
    let fog = (travel / maximum_distance).powf(1.7).clamp(0.0, 0.82);
    mix_color(shaded, settings.inside_color, fog)
}

fn distance_estimate(settings: &RenderSettings, point: Vec3, iterations: usize) -> f64 {
    match settings.kind {
        FractalKind::Mandelbulb => mandelbulb_distance(point, settings.power, iterations),
        FractalKind::Mandelbox => mandelbox_distance(point, iterations),
        FractalKind::MengerSponge => menger_distance(point, iterations),
        FractalKind::SierpinskiTetrahedron => tetrahedron_distance(point, iterations),
        FractalKind::QuaternionJulia => {
            quaternion_julia_distance(point, settings.quaternion_constant, iterations)
        }
        FractalKind::OctahedralIfs => octahedral_ifs(
            point,
            settings.julia_constant.re,
            settings.julia_constant.im,
            iterations,
        ),
        FractalKind::IcosahedralIfs => icosahedral_ifs(
            point,
            settings.julia_constant.re,
            settings.julia_constant.im,
            iterations,
        ),
        FractalKind::ApollonianGasket => apollonian_gasket(
            point,
            settings.julia_constant.re,
            settings.julia_constant.im,
            settings.power,
            iterations,
        ),
        FractalKind::Kleinian => kleinian(
            point,
            settings.julia_constant.re,
            settings.julia_constant.im,
            settings.power,
            iterations,
        ),
        FractalKind::HybridMandelbulbJulia => hybrid_mandelbulb_julia(
            point,
            settings.power,
            Vec3::new(settings.julia_constant.re, settings.julia_constant.im, 0.3),
            iterations,
        ),
        FractalKind::QuaternionCubic => {
            quaternion_cubic(point, settings.quaternion_constant, iterations)
        }
        FractalKind::SierpinskiGasket => sierpinski_gasket(
            point,
            settings.julia_constant.re,
            settings.julia_constant.im,
            settings.power,
            iterations,
        ),
        _ => f64::INFINITY,
    }
}

fn mandelbulb_distance(point: Vec3, power: f64, iterations: usize) -> f64 {
    let mut z = point;
    let mut derivative = 1.0;
    let mut radius = 0.0;
    for _ in 0..iterations {
        radius = z.length();
        if radius > 2.5 {
            break;
        }
        if radius < 1.0e-12 {
            return 0.0;
        }
        let theta = (z.z / radius).clamp(-1.0, 1.0).acos();
        let phi = z.y.atan2(z.x);
        derivative = radius.powf(power - 1.0) * power * derivative + 1.0;
        let powered_radius = radius.powf(power);
        let powered_theta = theta * power;
        let powered_phi = phi * power;
        z = Vec3::new(
            powered_theta.sin() * powered_phi.cos(),
            powered_theta.sin() * powered_phi.sin(),
            powered_theta.cos(),
        ) * powered_radius
            + point;
    }
    0.5 * radius.ln() * radius / derivative.abs().max(1.0e-12)
}

fn mandelbox_distance(point: Vec3, iterations: usize) -> f64 {
    let mut z = point;
    let mut derivative = 1.0;
    let scale: f64 = -1.65;
    let minimum_radius_squared = 0.25;
    for _ in 0..iterations {
        z = z.clamp(-1.0, 1.0) * 2.0 - z;
        let radius_squared = z.dot(z);
        if radius_squared < minimum_radius_squared {
            let factor = 1.0 / minimum_radius_squared;
            z = z * factor;
            derivative *= factor;
        } else if radius_squared < 1.0 {
            let factor = 1.0 / radius_squared.max(1.0e-12);
            z = z * factor;
            derivative *= factor;
        }
        z = z * scale + point;
        derivative = derivative * scale.abs() + 1.0;
    }
    z.length() / derivative.abs().max(1.0e-12)
}

fn menger_distance(point: Vec3, iterations: usize) -> f64 {
    let mut distance = signed_box_distance(point, Vec3::splat(1.0));
    let mut scale = 1.0;
    for _ in 0..iterations {
        let repeated = Vec3::new(
            (point.x * scale).rem_euclid(2.0) - 1.0,
            (point.y * scale).rem_euclid(2.0) - 1.0,
            (point.z * scale).rem_euclid(2.0) - 1.0,
        );
        scale *= 3.0;
        let r = (Vec3::splat(1.0) - repeated.abs() * 3.0).abs();
        let da = r.x.max(r.y);
        let db = r.y.max(r.z);
        let dc = r.z.max(r.x);
        let cut = (da.min(db).min(dc) - 1.0) / scale;
        distance = distance.max(cut);
    }
    distance
}

fn signed_box_distance(point: Vec3, bounds: Vec3) -> f64 {
    let q = point.abs() - bounds;
    let outside = Vec3::new(q.x.max(0.0), q.y.max(0.0), q.z.max(0.0)).length();
    outside + q.max_component().min(0.0)
}

fn tetrahedron_distance(mut point: Vec3, iterations: usize) -> f64 {
    let vertices = [
        Vec3::new(0.68, 0.68, 0.68),
        Vec3::new(-0.68, -0.68, 0.68),
        Vec3::new(-0.68, 0.68, -0.68),
        Vec3::new(0.68, -0.68, -0.68),
    ];
    let mut scale = 1.0;
    for _ in 0..iterations {
        let mut nearest = vertices[0];
        let mut nearest_distance = (point - nearest).dot(point - nearest);
        for vertex in vertices.iter().skip(1) {
            let distance = (point - *vertex).dot(point - *vertex);
            if distance < nearest_distance {
                nearest = *vertex;
                nearest_distance = distance;
            }
        }
        point = (point - nearest) * 2.0;
        scale *= 2.0;
    }
    point.length() / scale - 0.012
}

fn quaternion_julia_distance(point: Vec3, constant: Quaternion, iterations: usize) -> f64 {
    let mut value = Quaternion::new(point.x, point.y, point.z, 0.0);
    let mut derivative = 1.0;
    let mut radius_squared = value.norm_squared();
    for _ in 0..iterations {
        derivative *= 2.0 * radius_squared.sqrt().max(1.0e-8);
        value = value.square() + constant;
        radius_squared = value.norm_squared();
        if radius_squared > 16.0 {
            break;
        }
    }
    let radius = radius_squared.sqrt().max(1.0e-12);
    0.5 * radius.ln() * radius / derivative.max(1.0e-12)
}

fn surface_normal(settings: &RenderSettings, point: Vec3, epsilon: f64, iterations: usize) -> Vec3 {
    let x = Vec3::new(epsilon, 0.0, 0.0);
    let y = Vec3::new(0.0, epsilon, 0.0);
    let z = Vec3::new(0.0, 0.0, epsilon);
    Vec3::new(
        distance_estimate(settings, point + x, iterations)
            - distance_estimate(settings, point - x, iterations),
        distance_estimate(settings, point + y, iterations)
            - distance_estimate(settings, point - y, iterations),
        distance_estimate(settings, point + z, iterations)
            - distance_estimate(settings, point - z, iterations),
    )
    .normalized()
}

fn interpolate_color(first: u32, second: u32, fraction: f64) -> u32 {
    fn channel(first: u32, second: u32, shift: u32, fraction: f64) -> u32 {
        let start = first >> shift & 0xff;
        let end = second >> shift & 0xff;
        (start as f64 + (end as f64 - start as f64) * fraction)
            .round()
            .clamp(0.0, 255.0) as u32
    }

    channel(first, second, 24, fraction) << 24
        | channel(first, second, 16, fraction) << 16
        | channel(first, second, 8, fraction) << 8
        | channel(first, second, 0, fraction)
}

fn shade_color(color: u32, light: f64, specular: f64) -> u32 {
    let channel = |shift: u32| {
        (((color >> shift & 0xff) as f64 * light + 255.0 * specular)
            .round()
            .clamp(0.0, 255.0)) as u32
    };
    (color >> 24 & 0xff) << 24 | channel(16) << 16 | channel(8) << 8 | channel(0)
}

fn mix_color(first: u32, second: u32, amount: f64) -> u32 {
    interpolate_color(first, second, amount.clamp(0.0, 1.0))
}

#[cfg(test)]
mod tests {
    use super::*;

    fn parameters() -> Vec<f64> {
        vec![
            -0.5,
            0.0,
            3.0,
            1.0,
            2.0,
            4.0,
            1.0,
            0.0,
            1.0,
            0.0,
            -16777216.0,
            -0.8,
            0.156,
            -0.5,
            0.0,
            1.0,
            0.65,
            0.3,
            3.5,
            0.0,
            0.0,
            0.0,
            -0.2,
            0.7,
            0.0,
            0.0,
            45.0,
        ]
    }

    fn palette() -> Vec<i32> {
        vec![
            0xff05051au32 as i32,
            0xff26bce1u32 as i32,
            0xfff8e16cu32 as i32,
        ]
    }

    fn settings_from_wire(
        type_id: i32,
        max_iterations: i32,
        parameters: &[f64],
        sequence: &str,
    ) -> Option<RenderSettings> {
        settings_from_exact(
            type_id,
            max_iterations,
            parameters,
            sequence,
            &parameters[PARAM_CENTER_X].to_string(),
            &parameters[PARAM_CENTER_Y].to_string(),
            &parameters[PARAM_VERTICAL_SPAN].to_string(),
        )
    }

    #[allow(clippy::too_many_arguments)]
    fn settings_from_exact(
        type_id: i32,
        max_iterations: i32,
        parameters: &[f64],
        sequence: &str,
        exact_center_x: &str,
        exact_center_y: &str,
        exact_span: &str,
    ) -> Option<RenderSettings> {
        RenderSettings::from_wire(
            type_id,
            max_iterations,
            parameters,
            &palette(),
            sequence,
            ExactViewportWire {
                center_x: exact_center_x,
                center_y: exact_center_y,
                vertical_span: exact_span,
            },
        )
    }

    fn normalized_angle(value: Complex) -> f64 {
        (value.im.atan2(value.re) / (2.0 * PI) + 1.0).rem_euclid(1.0)
    }

    fn all_stable_ids() -> [i32; 42] {
        [
            1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 101, 102, 103, 104, 105, 106, 107, 108, 109,
            110, 111, 112, 113, 114, 115, 1001, 1002, 1003, 1004, 1005, 1006, 1007, 1008, 1009,
            1010, 1011, 1012, 1013, 1014, 1015,
        ]
    }

    fn configure_parameters(type_id: i32, parameters: &mut [f64]) {
        let (center_x, center_y, span) = match type_id {
            TYPE_LYAPUNOV => (3.0, 3.0, 2.2),
            TYPE_SIERPINSKI_CARPET | TYPE_SIERPINSKI_TRIANGLE => (0.0, 0.0, 2.2),
            TYPE_BURNING_SHIP_JULIA | TYPE_CELTIC_JULIA => (0.0, 0.0, 3.0),
            TYPE_COLLATZ => {
                parameters[PARAM_BAILOUT] = 100.0;
                (0.0, 0.0, 4.0)
            }
            TYPE_BUDDHABROT => (0.4, 0.0, 4.444444444444445),
            TYPE_HOPALONG => {
                parameters[PARAM_JULIA_REAL] = 0.4;
                parameters[PARAM_JULIA_IMAGINARY] = 1.0;
                parameters[PARAM_POWER] = 0.0;
                (-0.1, 0.5, 4.2)
            }
            TYPE_MARTIN => {
                parameters[PARAM_JULIA_REAL] = PI;
                (PI * 0.5, PI * 0.5, 145.0)
            }
            TYPE_GINGERBREADMAN => (2.5, 2.5, 12.0),
            TYPE_CHIP => {
                parameters[PARAM_JULIA_REAL] = -15.0;
                parameters[PARAM_JULIA_IMAGINARY] = -19.0;
                parameters[PARAM_POWER] = 1.0;
                (-7.2, -7.9, 720.0)
            }
            TYPE_QUADRUPTWO => {
                parameters[PARAM_JULIA_REAL] = 34.0;
                parameters[PARAM_JULIA_IMAGINARY] = 1.0;
                parameters[PARAM_POWER] = 5.0;
                (16.5, 17.5, 200.0)
            }
            TYPE_THREEPLY => {
                parameters[PARAM_JULIA_REAL] = -55.0;
                parameters[PARAM_JULIA_IMAGINARY] = -1.0;
                parameters[PARAM_POWER] = -42.0;
                (-34.0, -21.0, 5_500.0)
            }
            TYPE_OCTAHEDRAL_IFS => {
                parameters[PARAM_JULIA_REAL] = 2.0;
                parameters[PARAM_JULIA_IMAGINARY] = 1.2;
                parameters[PARAM_CAMERA_DISTANCE] = 9.0;
                (0.0, 0.0, 2.0)
            }
            TYPE_ICOSAHEDRAL_IFS => {
                parameters[PARAM_JULIA_REAL] = 1.7;
                parameters[PARAM_JULIA_IMAGINARY] = 1.5;
                parameters[PARAM_CAMERA_DISTANCE] = 9.0;
                (0.0, 0.0, 2.0)
            }
            TYPE_APOLLONIAN_GASKET => {
                parameters[PARAM_JULIA_REAL] = 1.3;
                parameters[PARAM_JULIA_IMAGINARY] = 1.35;
                parameters[PARAM_POWER] = 1.12;
                parameters[PARAM_CAMERA_DISTANCE] = 8.0;
                (0.0, 0.0, 2.0)
            }
            TYPE_KLEINIAN => {
                parameters[PARAM_JULIA_REAL] = 1.5;
                parameters[PARAM_JULIA_IMAGINARY] = 1.0;
                parameters[PARAM_POWER] = 0.5;
                parameters[PARAM_CAMERA_DISTANCE] = 5.0;
                (0.0, 0.0, 2.0)
            }
            TYPE_HYBRID_MANDELBULB_JULIA => {
                parameters[PARAM_JULIA_REAL] = -0.2;
                parameters[PARAM_JULIA_IMAGINARY] = 0.8;
                parameters[PARAM_POWER] = 8.0;
                parameters[PARAM_CAMERA_DISTANCE] = 4.0;
                (0.0, 0.0, 2.0)
            }
            TYPE_QUATERNION_CUBIC => {
                parameters[PARAM_QUATERNION_X] = -0.2;
                parameters[PARAM_QUATERNION_Y] = 0.6;
                parameters[PARAM_QUATERNION_Z] = 0.3;
                parameters[PARAM_QUATERNION_W] = 0.0;
                parameters[PARAM_CAMERA_DISTANCE] = 3.5;
                (0.0, 0.0, 2.0)
            }
            TYPE_SIERPINSKI_GASKET => {
                parameters[PARAM_JULIA_REAL] = 1.5;
                parameters[PARAM_JULIA_IMAGINARY] = 1.0;
                parameters[PARAM_POWER] = 0.5;
                parameters[PARAM_CAMERA_DISTANCE] = 5.0;
                (0.0, 0.0, 2.0)
            }
            TYPE_PICKOVER => {
                parameters[PARAM_JULIA_REAL] = 2.24;
                parameters[PARAM_JULIA_IMAGINARY] = 0.43;
                parameters[PARAM_POWER] = -0.65;
                parameters[PARAM_PHOENIX_REAL] = -2.43;
                parameters[PARAM_CAMERA_DISTANCE] = 3.0;
                (0.0, 0.0, 2.0)
            }
            TYPE_LORENZ => {
                parameters[PARAM_JULIA_REAL] = 10.0;
                parameters[PARAM_JULIA_IMAGINARY] = 28.0;
                parameters[PARAM_POWER] = 8.0 / 3.0;
                parameters[PARAM_CAMERA_DISTANCE] = 5.0;
                (0.0, 0.0, 2.0)
            }
            TYPE_ROSSLER => {
                parameters[PARAM_JULIA_REAL] = 0.2;
                parameters[PARAM_JULIA_IMAGINARY] = 0.2;
                parameters[PARAM_POWER] = 5.7;
                parameters[PARAM_CAMERA_DISTANCE] = 5.0;
                (0.0, 0.0, 2.0)
            }
            id if id >= TYPE_MANDELBULB => {
                parameters[PARAM_POWER] = if id == TYPE_MANDELBULB { 8.0 } else { 2.0 };
                (0.0, 0.0, 2.0)
            }
            _ => return,
        };
        parameters[PARAM_CENTER_X] = center_x;
        parameters[PARAM_CENTER_Y] = center_y;
        parameters[PARAM_VERTICAL_SPAN] = span;
    }

    fn render_type(type_id: i32, width: usize, height: usize) -> Vec<u8> {
        let mut params = parameters();
        configure_parameters(type_id, &mut params);
        params[PARAM_VIEWPORT_ASPECT_RATIO] = width as f64 / height as f64;
        let max_iterations = match type_id {
            TYPE_KLEINIAN => 192,
            TYPE_QUATERNION_CUBIC => 256,
            _ => 96,
        };
        let settings =
            settings_from_wire(type_id, max_iterations, &params, "AB").expect("valid settings");
        let mut pixels = vec![0_u8; width * height * 4];
        assert_eq!(
            render_into(
                &settings,
                &mut pixels,
                width,
                height,
                width * 4,
                BitmapAlphaMode::Unpremultiplied,
                &AtomicBool::new(false),
            ),
            Some(RenderOutcome::Completed),
            "type {type_id}"
        );
        pixels
    }

    #[test]
    fn all_stable_ids_resolve() {
        assert!(
            all_stable_ids()
                .into_iter()
                .all(|id| FractalKind::from_stable_id(id).is_some())
        );
        assert!(FractalKind::from_stable_id(9999).is_none());
    }

    #[test]
    fn every_kind_renders_non_uniform_pixels() {
        for id in all_stable_ids() {
            let pixels = render_type(id, 48, 48);
            let first = &pixels[0..4];
            assert!(
                pixels.chunks_exact(4).any(|pixel| pixel != first),
                "type {id}"
            );
        }
    }

    #[test]
    fn new_types_render_distinct_and_density_types_are_deterministic() {
        let new_ids = [
            108, 109, 110, 111, 112, 113, 114, 115, 1006, 1007, 1008, 1009, 1010, 1011, 1012, 1013,
            1014, 1015,
        ];
        let outputs: std::collections::HashSet<_> = new_ids
            .into_iter()
            .map(|id| render_type(id, 40, 40))
            .collect();
        assert_eq!(outputs.len(), new_ids.len());

        for id in [109, 110, 111, 112, 113, 114, 115, 1013, 1014, 1015] {
            assert_eq!(
                render_type(id, 32, 24),
                render_type(id, 32, 24),
                "type {id}"
            );
        }
    }

    #[test]
    fn kleinian_default_has_visible_bounded_occupancy() {
        let pixels = render_type(TYPE_KLEINIAN, 96, 54);
        let background = [0_u8, 0, 0, 255];
        let occupied = pixels
            .chunks_exact(4)
            .filter(|pixel| *pixel != background)
            .count();
        let occupancy = occupied as f64 / (96 * 54) as f64;
        assert!((0.05..0.20).contains(&occupancy), "{occupancy}");
    }

    #[test]
    fn density_work_plans_are_resolution_aware_and_exact() {
        let mut buddha_params = parameters();
        configure_parameters(TYPE_BUDDHABROT, &mut buddha_params);
        let buddha = settings_from_wire(TYPE_BUDDHABROT, 800, &buddha_params, "AB")
            .expect("valid Buddhabrot settings");
        assert_eq!(
            buddha.work_plan(1_920, 1_080),
            Some(RenderWorkPlan {
                work_units: 500_000_000,
                density_iterations: 622_408,
                ray_march: None,
            })
        );
        let low_iteration_buddha = settings_from_wire(TYPE_BUDDHABROT, 16, &buddha_params, "AB")
            .expect("valid low-iteration Buddhabrot settings");
        assert_eq!(
            low_iteration_buddha.work_plan(640, 480),
            Some(RenderWorkPlan {
                work_units: 5_222_400,
                density_iterations: 153_600,
                ray_march: None,
            })
        );

        let mut planar_params = parameters();
        configure_parameters(TYPE_HOPALONG, &mut planar_params);
        let planar = settings_from_wire(TYPE_HOPALONG, 320, &planar_params, "AB")
            .expect("valid planar settings");
        assert_eq!(
            planar.work_plan(640, 480),
            Some(RenderWorkPlan {
                work_units: 1_331_200,
                density_iterations: 40_960,
                ray_march: None,
            })
        );

        let mut spatial_params = parameters();
        configure_parameters(TYPE_LORENZ, &mut spatial_params);
        spatial_params[PARAM_SUPERSAMPLING] = 4.0;
        let spatial = settings_from_wire(TYPE_LORENZ, 1_000, &spatial_params, "AB")
            .expect("valid spatial settings");
        assert_eq!(
            spatial.work_plan(1_920, 1_080),
            Some(RenderWorkPlan {
                work_units: 5_241_600,
                density_iterations: 352_000,
                ray_march: None,
            })
        );
    }

    #[test]
    fn density_resolution_multiplier_uses_exact_ceiling_sqrt() {
        assert_eq!(density_resolution_multiplier(640 * 480, 1), Some(1));
        assert_eq!(density_resolution_multiplier(640 * 480 + 1, 1), Some(2));
        assert_eq!(density_resolution_multiplier(4 * 640 * 480, 1), Some(2));
        assert_eq!(density_resolution_multiplier(4 * 640 * 480 + 1, 1), Some(3));
    }

    #[test]
    fn ray_march_charged_work_matches_plan_and_four_k_rejects() {
        let ray_marched_ids = [
            TYPE_MANDELBULB,
            TYPE_MANDELBOX,
            TYPE_MENGER_SPONGE,
            TYPE_SIERPINSKI_TETRAHEDRON,
            TYPE_QUATERNION_JULIA,
            TYPE_OCTAHEDRAL_IFS,
            TYPE_ICOSAHEDRAL_IFS,
            TYPE_APOLLONIAN_GASKET,
            TYPE_KLEINIAN,
            TYPE_HYBRID_MANDELBULB_JULIA,
            TYPE_QUATERNION_CUBIC,
            TYPE_SIERPINSKI_GASKET,
        ];
        for id in ray_marched_ids {
            let mut params = parameters();
            configure_parameters(id, &mut params);
            let requested_iterations = if id == TYPE_QUATERNION_CUBIC {
                256
            } else {
                320
            };
            let settings = settings_from_wire(id, requested_iterations, &params, "AB")
                .expect("valid ray-marched settings");
            let plan = settings
                .work_plan(1_080, 1_080)
                .unwrap_or_else(|| panic!("type {id} must fit an app-sized render"));
            let ray = plan.ray_march.expect("ray plan");
            assert_eq!(
                plan.work_units,
                1_080_u64 * 1_080 * (ray.maximum_steps + 6) as u64 * ray.distance_iterations as u64
            );
            assert!(plan.work_units <= MAX_RENDER_WORK_UNITS);
            assert!(
                settings.work_plan(3_840, 2_160).is_none(),
                "type {id} accepted 4K"
            );
        }
    }

    #[test]
    fn density_renderers_observe_pre_cancellation() {
        for id in [TYPE_BUDDHABROT, TYPE_HOPALONG, TYPE_PICKOVER] {
            let mut params = parameters();
            configure_parameters(id, &mut params);
            let settings = settings_from_wire(id, 1_000, &params, "AB").expect("valid settings");
            let mut pixels = vec![0_u8; 32 * 32 * 4];
            assert_eq!(
                render_into(
                    &settings,
                    &mut pixels,
                    32,
                    32,
                    32 * 4,
                    BitmapAlphaMode::Unpremultiplied,
                    &AtomicBool::new(true),
                ),
                Some(RenderOutcome::Cancelled),
                "type {id}"
            );
        }
    }

    #[test]
    fn buddhabrot_uses_fixed_escape_radius_while_collatz_bailout_is_configurable() {
        fn render_with_bailout(type_id: i32, bailout: f64, max_iterations: i32) -> Vec<u8> {
            let mut params = parameters();
            configure_parameters(type_id, &mut params);
            params[PARAM_BAILOUT] = bailout;
            params[PARAM_VIEWPORT_ASPECT_RATIO] = 4.0 / 3.0;
            let settings =
                settings_from_wire(type_id, max_iterations, &params, "AB").expect("settings");
            let mut pixels = vec![0_u8; 32 * 24 * 4];
            assert_eq!(
                render_into(
                    &settings,
                    &mut pixels,
                    32,
                    24,
                    32 * 4,
                    BitmapAlphaMode::Unpremultiplied,
                    &AtomicBool::new(false),
                ),
                Some(RenderOutcome::Completed)
            );
            pixels
        }

        assert_eq!(
            render_with_bailout(TYPE_BUDDHABROT, 2.0, 96),
            render_with_bailout(TYPE_BUDDHABROT, 100.0, 96)
        );
        assert_ne!(
            render_with_bailout(TYPE_COLLATZ, 2.0, 96),
            render_with_bailout(TYPE_COLLATZ, 100.0, 96)
        );
        let low_iteration_buddha = render_with_bailout(TYPE_BUDDHABROT, 2.0, 16);
        let first = &low_iteration_buddha[..4];
        assert!(
            low_iteration_buddha
                .chunks_exact(4)
                .any(|pixel| pixel != first)
        );
    }

    #[test]
    fn cancellation_stops_before_first_row() {
        let settings = settings_from_wire(1, 320, &parameters(), "AB").expect("valid settings");
        let mut pixels = vec![0_u8; 16 * 16 * 4];
        let cancelled = AtomicBool::new(true);
        assert_eq!(
            render_into(
                &settings,
                &mut pixels,
                16,
                16,
                16 * 4,
                BitmapAlphaMode::Unpremultiplied,
                &cancelled,
            ),
            Some(RenderOutcome::Cancelled)
        );
    }

    #[test]
    fn deep_zoom_at_1e_100_is_non_uniform() {
        let mut params = parameters();
        params[PARAM_CENTER_X] = -2.0;
        params[PARAM_CENTER_Y] = 0.0;
        params[PARAM_VERTICAL_SPAN] = 4.0e-100;
        params[PARAM_VIEWPORT_ASPECT_RATIO] = 4.0;
        let settings = settings_from_exact(1, 600, &params, "AB", "-2", "0", "4E-100")
            .expect("valid deep settings");
        let mut pixels = vec![0_u8; 32 * 8 * 4];
        assert_eq!(
            render_into(
                &settings,
                &mut pixels,
                32,
                8,
                32 * 4,
                BitmapAlphaMode::Unpremultiplied,
                &AtomicBool::new(false),
            ),
            Some(RenderOutcome::Completed)
        );
        let first = &pixels[..4];
        assert!(pixels.chunks_exact(4).any(|pixel| pixel != first));
    }

    #[test]
    fn exact_center_changes_pixels_beyond_double_precision() {
        let mut params = parameters();
        params[PARAM_CENTER_X] = -2.0;
        params[PARAM_CENTER_Y] = 0.0;
        params[PARAM_VERTICAL_SPAN] = 4.0e-100;
        params[PARAM_VIEWPORT_ASPECT_RATIO] = 2.0;
        let first = settings_from_exact(1, 600, &params, "AB", "-2", "0", "4E-100")
            .expect("valid first deep settings");
        let second = settings_from_exact(
            1,
            600,
            &params,
            "AB",
            "-1.9999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999",
            "0",
            "4E-100",
        )
        .expect("valid shifted deep settings");
        let mut first_pixels = vec![0_u8; 24 * 8 * 4];
        let mut second_pixels = vec![0_u8; 24 * 8 * 4];
        for (settings, pixels) in [(&first, &mut first_pixels), (&second, &mut second_pixels)] {
            assert_eq!(
                render_into(
                    settings,
                    pixels,
                    24,
                    8,
                    24 * 4,
                    BitmapAlphaMode::Unpremultiplied,
                    &AtomicBool::new(false),
                ),
                Some(RenderOutcome::Completed)
            );
        }
        assert_ne!(first_pixels, second_pixels);
    }

    #[test]
    fn deep_reference_creation_observes_cancellation() {
        let mut params = parameters();
        params[PARAM_CENTER_X] = -2.0;
        params[PARAM_VERTICAL_SPAN] = 1.0e-300;
        let settings = settings_from_exact(1, 16_384, &params, "AB", "-2", "0", "1E-300")
            .expect("valid minimum-span settings");
        let mut pixels = [0_u8; 4];
        assert_eq!(
            render_into(
                &settings,
                &mut pixels,
                1,
                1,
                4,
                BitmapAlphaMode::Unpremultiplied,
                &AtomicBool::new(true),
            ),
            Some(RenderOutcome::Cancelled)
        );
    }

    #[test]
    fn julia_variants_start_at_the_pixel_and_use_the_julia_constant() {
        let point = Complex::new(0.25, -0.4);
        let cancelled = AtomicBool::new(false);

        let burning_ship = settings_from_wire(TYPE_BURNING_SHIP_JULIA, 1, &parameters(), "AB")
            .expect("valid Burning Ship Julia settings");
        let burning_ship_sample = iterate_escape_time(&burning_ship, point, &cancelled);
        let burning_ship_value = point.component_abs().powf(2.0) + burning_ship.julia_constant;
        assert!((burning_ship_sample.angle - normalized_angle(burning_ship_value)).abs() < 1.0e-12);

        let celtic = settings_from_wire(TYPE_CELTIC_JULIA, 1, &parameters(), "AB")
            .expect("valid Celtic Julia settings");
        let celtic_sample = iterate_escape_time(&celtic, point, &cancelled);
        let powered = point.powf(2.0);
        let celtic_value = Complex::new(
            powered.re.abs() + celtic.julia_constant.re,
            powered.im + celtic.julia_constant.im,
        );
        assert!((celtic_sample.angle - normalized_angle(celtic_value)).abs() < 1.0e-12);
    }

    #[test]
    fn render_pool_reserves_one_available_cpu_for_the_caller() {
        assert_eq!(render_pool_thread_count(1), 1);
        assert_eq!(render_pool_thread_count(2), 1);
        assert_eq!(render_pool_thread_count(3), 2);
        assert_eq!(render_pool_thread_count(9), 8);
        assert_eq!(render_pool_thread_count(64), 8);
    }

    #[test]
    fn palette_interpolation_preserves_argb_channels() {
        assert_eq!(interpolate_color(0xff000000, 0xffffffff, 0.5), 0xff808080);
    }

    #[test]
    fn bitmap_alpha_modes_write_expected_channels() {
        let mut params = parameters();
        params[PARAM_CENTER_X] = 0.0;
        params[PARAM_CENTER_Y] = 0.0;
        params[PARAM_INSIDE_COLOR_ARGB] = (0x804080c0u32 as i32) as f64;
        let settings = settings_from_wire(1, 1, &params, "AB").expect("valid settings");

        let cases = [
            (BitmapAlphaMode::Premultiplied, [32, 64, 96, 128]),
            (BitmapAlphaMode::Unpremultiplied, [64, 128, 192, 128]),
            (BitmapAlphaMode::Opaque, [64, 128, 192, 255]),
        ];
        for (alpha_mode, expected) in cases {
            let mut pixel = [0_u8; 4];
            assert_eq!(
                render_into(
                    &settings,
                    &mut pixel,
                    1,
                    1,
                    4,
                    alpha_mode,
                    &AtomicBool::new(false),
                ),
                Some(RenderOutcome::Completed)
            );
            assert_eq!(pixel, expected, "{alpha_mode:?}");
        }
    }

    #[test]
    fn lyapunov_derivative_uses_state_before_update() {
        let settings = settings_from_wire(103, 64, &parameters(), "ABB").expect("valid settings");
        let point = Complex::new(3.2, 3.7);
        let actual =
            lyapunov_exponent(&settings, point, &AtomicBool::new(false)).expect("not cancelled");

        let mut state = 0.5_f64;
        for index in 0..32 {
            let rate = if settings.lyapunov_sequence[index % 3] {
                point.im
            } else {
                point.re
            };
            state = rate * state * (1.0 - state);
        }
        let mut expected_sum = 0.0;
        for index in 0..settings.max_iterations {
            let rate = if settings.lyapunov_sequence[index % 3] {
                point.im
            } else {
                point.re
            };
            expected_sum += (rate * (1.0 - 2.0 * state)).abs().max(1.0e-15).ln();
            state = rate * state * (1.0 - state);
        }
        let expected = expected_sum / settings.max_iterations as f64;
        assert!((actual - expected).abs() < 1.0e-12);
    }

    #[test]
    fn wire_layout_and_work_budget_are_strict() {
        let params = parameters();
        let settings = settings_from_wire(1, 320, &params, "AB").expect("valid settings");
        assert!(settings.is_within_work_limit(1_000, 1_000));
        assert!(!settings.is_within_work_limit(2_000, 2_000));

        let mut trailing_parameter = params;
        trailing_parameter.push(0.0);
        assert!(settings_from_wire(1, 320, &trailing_parameter, "AB").is_none());
        assert!(settings_from_exact(1, 320, &parameters(), "AB", "NaN", "0", "3").is_none());
        let mut deep_params = parameters();
        deep_params[PARAM_VERTICAL_SPAN] = 1.0e-301;
        assert!(settings_from_exact(1, 320, &deep_params, "AB", "-0.5", "0", "1E-301").is_none());
        assert!(settings_from_exact(1, 320, &parameters(), "AB", "-0.4", "0", "3").is_none());
    }
}
