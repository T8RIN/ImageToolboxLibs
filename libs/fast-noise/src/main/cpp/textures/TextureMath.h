#pragma once

#include <algorithm>
#include <cmath>
#include <cstdint>

namespace textures {

    inline float clamp01(float value) {
        return std::clamp(value, 0.0f, 1.0f);
    }

    inline float smoothstep(float edge0, float edge1, float value) {
        if (edge0 == edge1) return value < edge0 ? 0.0f : 1.0f;
        float t = clamp01((value - edge0) / (edge1 - edge0));
        return t * t * (3.0f - 2.0f * t);
    }

    inline float fract(float value) {
        return value - std::floor(value);
    }

    inline float contrast(float value, float amount) {
        return clamp01((value - 0.5f) * std::max(amount, 0.0f) + 0.5f);
    }

    inline uint32_t mixColor(uint32_t from, uint32_t to, float amount) {
        float t = clamp01(amount);
        auto mixChannel = [t](uint32_t a, uint32_t b, int shift) {
            float av = static_cast<float>((a >> shift) & 0xffu);
            float bv = static_cast<float>((b >> shift) & 0xffu);
            return static_cast<uint32_t>(std::lround(av + (bv - av) * t)) << shift;
        };
        return mixChannel(from, to, 24) |
                mixChannel(from, to, 16) |
                mixChannel(from, to, 8) |
                mixChannel(from, to, 0);
    }

    inline uint32_t argbToBitmapRgba(uint32_t color) {
        return (color & 0xff00ff00u) |
                ((color & 0x00ff0000u) >> 16u) |
                ((color & 0x000000ffu) << 16u);
    }

    inline float hash(float x, float y, int seed) {
        float value = std::sin(x * 127.1f + y * 311.7f + static_cast<float>(seed) * 0.0137f) * 43758.5453f;
        return fract(value);
    }

}
