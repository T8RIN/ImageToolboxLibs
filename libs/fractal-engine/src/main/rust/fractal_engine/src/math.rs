use std::ops::{Add, Div, Mul, Sub};

#[derive(Clone, Copy, Debug, Default)]
pub(crate) struct Complex {
    pub(crate) re: f64,
    pub(crate) im: f64,
}

impl Complex {
    pub(crate) const fn new(re: f64, im: f64) -> Self {
        Self { re, im }
    }

    pub(crate) fn norm_squared(self) -> f64 {
        self.re * self.re + self.im * self.im
    }

    pub(crate) fn conjugate(self) -> Self {
        Self::new(self.re, -self.im)
    }

    pub(crate) fn component_abs(self) -> Self {
        Self::new(self.re.abs(), self.im.abs())
    }

    pub(crate) fn powf(self, power: f64) -> Self {
        if power == 2.0 {
            return self * self;
        }

        let integer_power = power.round() as i32;
        if (power - integer_power as f64).abs() < 1.0e-10 && integer_power > 0 {
            let mut result = Self::new(1.0, 0.0);
            let mut factor = self;
            let mut remaining = integer_power;
            while remaining > 0 {
                if remaining & 1 == 1 {
                    result = result * factor;
                }
                remaining >>= 1;
                if remaining > 0 {
                    factor = factor * factor;
                }
            }
            return result;
        }

        let radius = self.norm_squared().powf(power * 0.5);
        let angle = self.im.atan2(self.re) * power;
        Self::new(radius * angle.cos(), radius * angle.sin())
    }

    pub(crate) fn checked_div(self, denominator: Self) -> Option<Self> {
        if !denominator.re.is_finite() || !denominator.im.is_finite() {
            return None;
        }

        if denominator.re.abs() >= denominator.im.abs() {
            if denominator.re == 0.0 {
                return None;
            }
            let ratio = denominator.im / denominator.re;
            let scaled = denominator.re + denominator.im * ratio;
            Some(Self::new(
                (self.re + self.im * ratio) / scaled,
                (self.im - self.re * ratio) / scaled,
            ))
        } else {
            let ratio = denominator.re / denominator.im;
            let scaled = denominator.im + denominator.re * ratio;
            Some(Self::new(
                (self.re * ratio + self.im) / scaled,
                (self.im * ratio - self.re) / scaled,
            ))
        }
    }

    pub(crate) fn is_finite(self) -> bool {
        self.re.is_finite() && self.im.is_finite()
    }
}

impl Add for Complex {
    type Output = Self;

    fn add(self, rhs: Self) -> Self::Output {
        Self::new(self.re + rhs.re, self.im + rhs.im)
    }
}

impl Sub for Complex {
    type Output = Self;

    fn sub(self, rhs: Self) -> Self::Output {
        Self::new(self.re - rhs.re, self.im - rhs.im)
    }
}

impl Mul for Complex {
    type Output = Self;

    fn mul(self, rhs: Self) -> Self::Output {
        Self::new(
            self.re * rhs.re - self.im * rhs.im,
            self.re * rhs.im + self.im * rhs.re,
        )
    }
}

impl Mul<f64> for Complex {
    type Output = Self;

    fn mul(self, rhs: f64) -> Self::Output {
        Self::new(self.re * rhs, self.im * rhs)
    }
}

impl Div<f64> for Complex {
    type Output = Self;

    fn div(self, rhs: f64) -> Self::Output {
        Self::new(self.re / rhs, self.im / rhs)
    }
}

#[derive(Clone, Copy, Debug, Default)]
pub(crate) struct Vec3 {
    pub(crate) x: f64,
    pub(crate) y: f64,
    pub(crate) z: f64,
}

impl Vec3 {
    pub(crate) const fn new(x: f64, y: f64, z: f64) -> Self {
        Self { x, y, z }
    }

    pub(crate) fn splat(value: f64) -> Self {
        Self::new(value, value, value)
    }

    pub(crate) fn dot(self, other: Self) -> f64 {
        self.x * other.x + self.y * other.y + self.z * other.z
    }

    pub(crate) fn length(self) -> f64 {
        self.dot(self).sqrt()
    }

    pub(crate) fn normalized(self) -> Self {
        let length = self.length();
        if length > 1.0e-15 {
            self / length
        } else {
            self
        }
    }

    pub(crate) fn cross(self, other: Self) -> Self {
        Self::new(
            self.y * other.z - self.z * other.y,
            self.z * other.x - self.x * other.z,
            self.x * other.y - self.y * other.x,
        )
    }

    pub(crate) fn abs(self) -> Self {
        Self::new(self.x.abs(), self.y.abs(), self.z.abs())
    }

    pub(crate) fn max_component(self) -> f64 {
        self.x.max(self.y).max(self.z)
    }

    pub(crate) fn clamp(self, minimum: f64, maximum: f64) -> Self {
        Self::new(
            self.x.clamp(minimum, maximum),
            self.y.clamp(minimum, maximum),
            self.z.clamp(minimum, maximum),
        )
    }
}

impl Add for Vec3 {
    type Output = Self;

    fn add(self, rhs: Self) -> Self::Output {
        Self::new(self.x + rhs.x, self.y + rhs.y, self.z + rhs.z)
    }
}

impl Sub for Vec3 {
    type Output = Self;

    fn sub(self, rhs: Self) -> Self::Output {
        Self::new(self.x - rhs.x, self.y - rhs.y, self.z - rhs.z)
    }
}

impl Mul<f64> for Vec3 {
    type Output = Self;

    fn mul(self, rhs: f64) -> Self::Output {
        Self::new(self.x * rhs, self.y * rhs, self.z * rhs)
    }
}

impl Div<f64> for Vec3 {
    type Output = Self;

    fn div(self, rhs: f64) -> Self::Output {
        Self::new(self.x / rhs, self.y / rhs, self.z / rhs)
    }
}

#[derive(Clone, Copy, Debug)]
pub(crate) struct Quaternion {
    pub(crate) x: f64,
    pub(crate) y: f64,
    pub(crate) z: f64,
    pub(crate) w: f64,
}

impl Quaternion {
    pub(crate) const fn new(x: f64, y: f64, z: f64, w: f64) -> Self {
        Self { x, y, z, w }
    }

    pub(crate) fn norm_squared(self) -> f64 {
        self.x * self.x + self.y * self.y + self.z * self.z + self.w * self.w
    }

    pub(crate) fn square(self) -> Self {
        Self::new(
            2.0 * self.w * self.x,
            2.0 * self.w * self.y,
            2.0 * self.w * self.z,
            self.w * self.w - self.x * self.x - self.y * self.y - self.z * self.z,
        )
    }
}

impl Add for Quaternion {
    type Output = Self;

    fn add(self, rhs: Self) -> Self::Output {
        Self::new(
            self.x + rhs.x,
            self.y + rhs.y,
            self.z + rhs.z,
            self.w + rhs.w,
        )
    }
}
