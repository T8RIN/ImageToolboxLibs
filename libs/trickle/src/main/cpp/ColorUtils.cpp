//
// Created by malik on 17.07.2024.
//
#include <android/bitmap.h>
#include <cstring>
#include <cstdlib>
#include <cmath>
#include <algorithm>
#include <cmath>
#include <vector>
#include <string>
#include "ColorUtils.h"

float LinearSRGBTosRGB(float linear) {
    if (linear <= 0.0031308f) {
        return 12.92f * linear;
    } else {
        return 1.055f * pow(linear, 1.0f / 2.4f) - 0.055f;
    }
}

float SRGBToLinear(float v) {
    if (v <= 0.045) {
        return v / 12.92;
    } else {
        return pow((v + 0.055) / 1.055, 2.4);
    }
}

float luminance(float red, float green, float blue) {
    float r = SRGBToLinear(red / 255.0f);
    float g = SRGBToLinear(green / 255.0f);
    float b = SRGBToLinear(blue / 255.0f);

    return 0.2125f * r + 0.7154f * g + 0.0721 * b;
}

LAB colorToLAB(RGB rgb) {
    float R = SRGBToLinear(rgb.r / 255.0) * 100.;
    float G = SRGBToLinear(rgb.g / 255.0) * 100.;
    float B = SRGBToLinear(rgb.b / 255.0) * 100.;

    float X = 0.4124564 * R + 0.3575761 * G + 0.1804375 * B;
    float Y = 0.2126729 * R + 0.7151522 * G + 0.0721750 * B;
    float Z = 0.0193339 * R + 0.1191920 * G + 0.9503041 * B;

    auto f = [](double t) {
        float T0 = 6.0 / 29.0;
        if (t > T0 * T0 * T0) {
            return cbrt(t);
        } else {
            float T0_inv = 1 / T0;
            return (1.0 / 3.0) * T0_inv * T0_inv * t + (4.0 / 29.0);
        }
    };

    float fx = f(X / 95.047);
    float fy = f(Y / 100.0000);
    float fz = f(Z / 108.883);

    return LAB(
            116.0 * fy - 16.0,
            500.0 * (fx - fy),
            200.0 * (fy - fz)
    );
}


RGB labToColor(LAB lab) {
    float fy = (lab.l + 16.0) / 116.0;
    float fx = lab.a / 500.0 + fy;
    float fz = fy - lab.b / 200.0;

    auto f_inv = [](float t) {
        float T0 = 6.0 / 29.0;
        if (t > T0) {
            return t * t * t;
        } else {
            return 3.0f * T0 * T0 * (t - 4.0f / 29.0f);
        }
    };

    float X = f_inv(fx) * 0.95047;
    float Y = f_inv(fy);
    float Z = f_inv(fz) * 1.08883;

    float R = 3.2404542 * X - 1.5371385 * Y - 0.4985314 * Z;
    float G = -0.9692660 * X + 1.8760108 * Y + 0.0415560 * Z;
    float B = 0.0556434 * X - 0.2040259 * Y + 1.0572252 * Z;

    return RGB(
            std::round(std::max(0.0f, std::min(1.0f, LinearSRGBTosRGB(R))) * 255.0f),
            std::round(std::max(0.0f, std::min(1.0f, LinearSRGBTosRGB(G))) * 255.0f),
            std::round(std::max(0.0f, std::min(1.0f, LinearSRGBTosRGB(B))) * 255.0f)
    );
}

uint32_t argb_to_bgra(uint32_t argb) {
    // Extract individual color components (alpha, red, green, blue)
    uint8_t alpha = (argb >> 24) & 0xFF;
    uint8_t red = (argb >> 16) & 0xFF;
    uint8_t green = (argb >> 8) & 0xFF;
    uint8_t blue = argb & 0xFF;

    // Combine components in BGRA order
    uint32_t bgra = (blue << 24) | (green << 16) | (red << 8) | alpha;

    return bgra;
}

uint32_t bgra_to_argb(uint32_t bgra) {
    uint8_t blue = (bgra >> 24) & 0xFF;
    uint8_t green = (bgra >> 16) & 0xFF;
    uint8_t red = (bgra >> 8) & 0xFF;
    uint8_t alpha = bgra & 0xFF;

    // Combine components in BGRA order
    uint32_t argb = (alpha << 24) | (red << 16) | (green << 8) | blue;

    return argb;
}

double colorDiff(uint32_t color1, uint32_t color2) {
    int r1 = (color1 >> 16) & 0xFF;
    int g1 = (color1 >> 8) & 0xFF;
    int b1 = (color1) & 0xFF;

    int r2 = (color2 >> 16) & 0xFF;
    int g2 = (color2 >> 8) & 0xFF;
    int b2 = (color2) & 0xFF;

    return sqrt(pow(r1 - r2, 2) + pow(g1 - g2, 2) + pow(b1 - b2, 2));
}

double colorDiff(RGB color1, RGB color2) {
    return sqrt(pow(color1.r - color2.r, 2) + pow(color1.g - color2.g, 2) +
                pow(color1.b - color2.b, 2));
}

RGB ColorToRGB(int color) {
    uint8_t alpha = (color >> 24) & 0xFF;
    uint8_t red = (color >> 16) & 0xFF;
    uint8_t green = (color >> 8) & 0xFF;
    uint8_t blue = color & 0xFF;

    return {red, green, blue};
}