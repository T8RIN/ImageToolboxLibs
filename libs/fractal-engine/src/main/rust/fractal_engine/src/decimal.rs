use std::cmp::Ordering;

use num_bigint::BigInt;
use num_traits::{Signed, Zero};

pub(crate) const MAX_DECIMAL_LENGTH: usize = 512;
const MAX_ABS_SCALE: i32 = 1_024;

#[derive(Clone, Debug, Eq, PartialEq)]
pub(crate) struct Decimal {
    coefficient: BigInt,
    scale: i32,
}

impl Decimal {
    pub(crate) fn parse(value: &str) -> Option<Self> {
        if value.is_empty() || value.len() > MAX_DECIMAL_LENGTH {
            return None;
        }
        let (mantissa, exponent) = split_exponent(value)?;
        let (negative, unsigned) = match mantissa.as_bytes().first() {
            Some(b'+') => (false, &mantissa[1..]),
            Some(b'-') => (true, &mantissa[1..]),
            _ => (false, mantissa),
        };
        if unsigned.is_empty() {
            return None;
        }

        let mut digits = String::with_capacity(unsigned.len() + usize::from(negative));
        if negative {
            digits.push('-');
        }
        let mut fraction_digits = 0_i32;
        let mut saw_dot = false;
        let mut digit_count = 0;
        for byte in unsigned.bytes() {
            match byte {
                b'0'..=b'9' => {
                    digits.push(byte as char);
                    digit_count += 1;
                    if saw_dot {
                        fraction_digits = fraction_digits.checked_add(1)?;
                    }
                }
                b'.' if !saw_dot => saw_dot = true,
                _ => return None,
            }
        }
        if digit_count == 0 {
            return None;
        }
        let scale = fraction_digits.checked_sub(exponent)?;
        if scale.abs() > MAX_ABS_SCALE {
            return None;
        }
        let coefficient = BigInt::parse_bytes(digits.as_bytes(), 10)?;
        Some(Self { coefficient, scale }.normalized())
    }

    pub(crate) fn from_f64(value: f64) -> Option<Self> {
        value
            .is_finite()
            .then(|| value.to_string())
            .and_then(|value| Self::parse(&value))
    }

    pub(crate) fn is_positive(&self) -> bool {
        self.coefficient.sign() == num_bigint::Sign::Plus
    }

    pub(crate) fn floor_log10_abs(&self) -> Option<i32> {
        if self.coefficient.is_zero() {
            return None;
        }
        let digits = self.coefficient.abs().to_str_radix(10).len() as i32;
        Some(digits - 1 - self.scale)
    }

    pub(crate) fn to_fixed(&self, fractional_digits: u32) -> BigInt {
        let shift = fractional_digits as i32 - self.scale;
        if shift >= 0 {
            &self.coefficient * power_of_ten(shift as u32)
        } else {
            &self.coefficient / power_of_ten((-shift) as u32)
        }
    }

    pub(crate) fn cmp(&self, other: &Self) -> Ordering {
        let common_scale = self.scale.max(other.scale);
        let left = &self.coefficient * power_of_ten((common_scale - self.scale) as u32);
        let right = &other.coefficient * power_of_ten((common_scale - other.scale) as u32);
        left.cmp(&right)
    }

    fn normalized(mut self) -> Self {
        if self.coefficient.is_zero() {
            self.scale = 0;
            return self;
        }
        let ten = BigInt::from(10_u8);
        while self.scale > -MAX_ABS_SCALE && (&self.coefficient % &ten).is_zero() {
            self.coefficient /= &ten;
            self.scale -= 1;
        }
        self
    }
}

pub(crate) fn power_of_ten(exponent: u32) -> BigInt {
    BigInt::from(10_u8).pow(exponent)
}

pub(crate) fn fixed_to_f64(value: &BigInt, fractional_digits: u32) -> f64 {
    if value.is_zero() {
        return 0.0;
    }
    let negative = value.sign() == num_bigint::Sign::Minus;
    let digits = value.abs().to_str_radix(10);
    let exponent = digits.len() as i32 - 1 - fractional_digits as i32;
    let sign = if negative { "-" } else { "" };
    let scientific = if digits.len() == 1 {
        format!("{sign}{digits}e{exponent}")
    } else {
        format!("{sign}{}.{}e{exponent}", &digits[..1], &digits[1..])
    };
    scientific.parse::<f64>().unwrap_or({
        if negative {
            f64::NEG_INFINITY
        } else {
            f64::INFINITY
        }
    })
}

fn split_exponent(value: &str) -> Option<(&str, i32)> {
    let mut parts = value.split(['e', 'E']);
    let mantissa = parts.next()?;
    let exponent = match parts.next() {
        Some(value) if !value.is_empty() => value.parse::<i32>().ok()?,
        Some(_) => return None,
        None => 0,
    };
    if parts.next().is_some() || !(-MAX_ABS_SCALE..=MAX_ABS_SCALE).contains(&exponent) {
        return None;
    }
    Some((mantissa, exponent))
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn parses_scientific_decimals_into_fixed_point() {
        let value = Decimal::parse("-1.234567890123456789E-100").unwrap();
        let fixed = value.to_fixed(120);
        assert_eq!(
            fixed,
            BigInt::parse_bytes(b"-123456789012345678900", 10).unwrap()
        );
        assert!((fixed_to_f64(&fixed, 120) + 1.2345678901234568e-100).abs() < 1e-115);
    }

    #[test]
    fn rejects_malformed_or_pathological_decimals() {
        assert!(Decimal::parse("").is_none());
        assert!(Decimal::parse("NaN").is_none());
        assert!(Decimal::parse("1.2.3").is_none());
        assert!(Decimal::parse("1E-10000").is_none());
        assert!(Decimal::parse("1E-2147483648").is_none());
        assert!(Decimal::parse(&"1".repeat(MAX_DECIMAL_LENGTH + 1)).is_none());
    }
}
