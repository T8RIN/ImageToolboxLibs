#include <jni.h>
#include <android/bitmap.h>
#include <algorithm>
#include <cmath>
#include <cstdint>
#include <cstring>
#include <utility>
#include <vector>

#include "../FastNoiseLite.h"
#include "TextureMath.h"

namespace {

    using textures::clamp01;
    using textures::contrast;
    using textures::fract;
    using textures::hash;
    using textures::argbToBitmapRgba;
    using textures::mixColor;
    using textures::smoothstep;

    constexpr float PI = 3.14159265358979323846f;

    class NoiseBank {
    public:
        explicit NoiseBank(int seed) :
                base(seed),
                detail(seed + 1013),
                warpX(seed + 2027),
                warpY(seed + 4093),
                cells(seed + 8191),
                cells2(seed + 12289),
                cellValue(seed + 16381) {
            configureFractal(base, FastNoiseLite::NoiseType_OpenSimplex2S, 5, 0.52f);
            configureFractal(detail, FastNoiseLite::NoiseType_Perlin, 3, 0.55f);
            configureFractal(warpX, FastNoiseLite::NoiseType_OpenSimplex2, 3, 0.5f);
            configureFractal(warpY, FastNoiseLite::NoiseType_OpenSimplex2, 3, 0.5f);

            configureCellular(cells, FastNoiseLite::CellularReturnType_Distance2Sub);
            configureCellular(cells2, FastNoiseLite::CellularReturnType_Distance2Sub);
            configureCellular(cellValue, FastNoiseLite::CellularReturnType_CellValue);
        }

        float fbm(float x, float y) const {
            return clamp01(base.GetNoise(x, y) * 0.5f + 0.5f);
        }

        float fine(float x, float y) const {
            return clamp01(detail.GetNoise(x, y) * 0.5f + 0.5f);
        }

        void warp(float &x, float &y, float scale, float amount) const {
            if (amount == 0.0f) return;
            float sx = x * scale;
            float sy = y * scale;
            float dx = warpX.GetNoise(sx + 19.17f, sy - 7.31f);
            float dy = warpY.GetNoise(sx - 13.43f, sy + 23.67f);
            x += dx * amount;
            y += dy * amount;
        }

        float cellEdge(float x, float y, float scale, float jitter) {
            cells.SetCellularJitter(std::clamp(jitter, 0.0f, 2.0f));
            return clamp01(cells.GetNoise(x * scale, y * scale) + 1.0f);
        }

        float secondaryCellEdge(float x, float y, float scale, float jitter) {
            cells2.SetCellularJitter(std::clamp(jitter, 0.0f, 2.0f));
            return clamp01(cells2.GetNoise(x * scale, y * scale) + 1.0f);
        }

        float cellVariation(float x, float y, float scale, float jitter) {
            cellValue.SetCellularJitter(std::clamp(jitter, 0.0f, 2.0f));
            return clamp01(cellValue.GetNoise(x * scale, y * scale) * 0.5f + 0.5f);
        }

    private:
        static void configureFractal(
                FastNoiseLite &noise,
                FastNoiseLite::NoiseType type,
                int octaves,
                float gain
        ) {
            noise.SetFrequency(1.0f);
            noise.SetNoiseType(type);
            noise.SetFractalType(FastNoiseLite::FractalType_FBm);
            noise.SetFractalOctaves(octaves);
            noise.SetFractalLacunarity(2.0f);
            noise.SetFractalGain(gain);
        }

        static void configureCellular(
                FastNoiseLite &noise,
                FastNoiseLite::CellularReturnType returnType
        ) {
            noise.SetFrequency(1.0f);
            noise.SetNoiseType(FastNoiseLite::NoiseType_Cellular);
            noise.SetCellularDistanceFunction(FastNoiseLite::CellularDistanceFunction_Euclidean);
            noise.SetCellularReturnType(returnType);
        }

        FastNoiseLite base;
        FastNoiseLite detail;
        FastNoiseLite warpX;
        FastNoiseLite warpY;
        FastNoiseLite cells;
        FastNoiseLite cells2;
        FastNoiseLite cellValue;
    };

    float parameter(const std::vector<float> &parameters, size_t index, float fallback) {
        if (index >= parameters.size() || !std::isfinite(parameters[index])) return fallback;
        return parameters[index];
    }

    uint32_t color(const std::vector<uint32_t> &colors, size_t index, uint32_t fallback) {
        return index < colors.size() ? colors[index] : fallback;
    }

    uint32_t lava(
            NoiseBank &noise,
            float x,
            float y,
            const std::vector<float> &p,
            const std::vector<uint32_t> &c
    ) {
        float scale = std::max(parameter(p, 0, 0.008f), 0.00001f);
        noise.warp(x, y, scale * 0.7f, parameter(p, 1, 32.0f));
        float flow = std::max(parameter(p, 2, 1.35f), 0.05f);
        float detail = clamp01(parameter(p, 3, 0.55f));
        float crust = clamp01(parameter(p, 4, 0.52f));
        float glow = clamp01(parameter(p, 5, 0.72f));
        float field = noise.fbm(x * scale, y * scale);
        field = field * (1.0f - detail * 0.35f) + noise.fine(x * scale * 3.7f, y * scale * 3.7f) * detail * 0.35f;
        float channels = std::pow(clamp01(1.0f - std::abs(field * 2.0f - 1.0f)), flow);
        float molten = smoothstep(crust - 0.18f, crust + 0.18f, channels);
        float core = smoothstep(0.66f, 0.98f, channels) * glow;
        uint32_t result = mixColor(color(c, 0, 0xff120b0au), color(c, 1, 0xffd52b0au), molten);
        return mixColor(result, color(c, 2, 0xffffd45au), core);
    }

    uint32_t clouds(
            NoiseBank &noise,
            float x,
            float y,
            const std::vector<float> &p,
            const std::vector<uint32_t> &c
    ) {
        float scale = std::max(parameter(p, 0, 0.0045f), 0.00001f);
        float coverage = clamp01(parameter(p, 1, 0.48f));
        float softness = std::max(parameter(p, 2, 0.22f), 0.005f);
        float detail = clamp01(parameter(p, 3, 0.62f));
        noise.warp(x, y, scale * 0.65f, parameter(p, 4, 24.0f));
        float density = std::max(parameter(p, 5, 0.92f), 0.0f);
        float field = noise.fbm(x * scale, y * scale) * (1.0f - detail * 0.3f) +
                noise.fine(x * scale * 3.2f, y * scale * 3.2f) * detail * 0.3f;
        float mask = smoothstep(coverage - softness, coverage + softness, field) * density;
        float light = clamp01(field + (noise.fbm((x - 7.0f) * scale, (y - 11.0f) * scale) - field) * 1.8f);
        uint32_t cloud = mixColor(color(c, 1, 0xff9eadbcu), color(c, 2, 0xfff8fbffu), light);
        return mixColor(color(c, 0, 0xff6fa9dcu), cloud, mask);
    }

    uint32_t smoke(
            NoiseBank &noise,
            float x,
            float y,
            const std::vector<float> &p,
            const std::vector<uint32_t> &c
    ) {
        float scale = std::max(parameter(p, 0, 0.006f), 0.00001f);
        noise.warp(x, y, scale * 0.7f, parameter(p, 1, 34.0f));
        float density = clamp01(parameter(p, 2, 0.56f));
        float wisps = std::max(parameter(p, 3, 2.2f), 0.0f);
        float amount = std::max(parameter(p, 4, 1.35f), 0.0f);
        float detail = clamp01(parameter(p, 5, 0.5f));
        float base = noise.fbm(x * scale, y * scale);
        float curls = 0.5f + 0.5f * std::sin((x + y * 0.35f) * scale * wisps * 4.0f + base * 8.0f);
        float field = base * (0.8f - detail * 0.25f) + noise.fine(x * scale * 4.0f, y * scale * 4.0f) * detail * 0.25f + curls * 0.2f;
        field = contrast(field, amount);
        float mask = smoothstep(1.0f - density, 0.92f, field);
        uint32_t plume = mixColor(color(c, 1, 0xff444a52u), color(c, 2, 0xffd5d9ddu), smoothstep(0.42f, 0.9f, field));
        return mixColor(color(c, 0, 0xff111318u), plume, mask);
    }

    uint32_t stone(
            NoiseBank &noise,
            float x,
            float y,
            const std::vector<float> &p,
            const std::vector<uint32_t> &c
    ) {
        float scale = std::max(parameter(p, 0, 0.011f), 0.00001f);
        float grain = clamp01(parameter(p, 1, 0.3f));
        float veins = clamp01(parameter(p, 2, 0.58f));
        float veinScale = std::max(parameter(p, 3, 0.024f), 0.00001f);
        noise.warp(x, y, scale, parameter(p, 4, 18.0f));
        float amount = std::max(parameter(p, 5, 1.12f), 0.0f);
        float body = noise.fbm(x * scale, y * scale);
        body = contrast(body + (noise.fine(x * scale * 7.0f, y * scale * 7.0f) - 0.5f) * grain * 0.35f, amount);
        float veinField = std::abs(std::sin((x + y * 0.42f) * veinScale + noise.fbm(x * scale * 0.7f, y * scale * 0.7f) * 9.0f));
        float vein = (1.0f - smoothstep(0.0f, 0.16f, veinField)) * veins;
        return mixColor(mixColor(color(c, 0, 0xff343536u), color(c, 1, 0xffaaa79fu), body), color(c, 2, 0xffe2ded3u), vein);
    }

    uint32_t wood(
            NoiseBank &noise,
            float x,
            float y,
            const std::vector<float> &p,
            const std::vector<uint32_t> &c
    ) {
        float scale = std::max(parameter(p, 0, 0.0039f), 0.00001f);
        float rings = std::max(parameter(p, 1, 11.4f), 0.1f);
        float grain = clamp01(parameter(p, 2, 0.75f));
        float distortion = parameter(p, 3, 15.0f);
        float stretch = std::max(parameter(p, 4, 7.42f), 0.1f);
        float amount = std::max(parameter(p, 5, 0.17f), 0.0f);
        float low = noise.fbm(x * scale / stretch, y * scale);
        float phase = (x * scale / stretch + low * distortion * scale) * rings * 2.0f * PI;
        float ring = contrast(0.5f + 0.5f * std::sin(phase), amount);
        float fibers = noise.fine(x * scale * 7.0f / stretch, y * scale * 7.0f);
        float tone = clamp01(ring * (1.0f - grain * 0.35f) + fibers * grain * 0.35f);
        float poreNoise = noise.fine(x * scale * 19.0f / stretch, y * scale * 12.0f);
        float elongatedPores = std::pow(clamp01(1.0f - std::abs(poreNoise * 2.0f - 1.0f)), 7.0f);
        float pores = smoothstep(0.62f, 0.94f, elongatedPores) * grain * 0.85f;
        return mixColor(mixColor(color(c, 0, 0xff3a190bu), color(c, 1, 0xffb96e32u), tone), color(c, 2, 0xff1d0d07u), pores);
    }

    uint32_t camouflage(
            NoiseBank &noise,
            float x,
            float y,
            const std::vector<float> &p,
            const std::vector<uint32_t> &c
    ) {
        float scale = std::max(parameter(p, 0, 0.007f), 0.00001f);
        noise.warp(x, y, scale * 0.75f, parameter(p, 4, 28.0f));
        float field = noise.fbm(x * scale, y * scale);
        float softness = std::max(parameter(p, 5, 0.035f), 0.0001f);
        float firstMask = smoothstep(
                parameter(p, 1, 0.36f) - softness,
                parameter(p, 1, 0.36f) + softness,
                field
        );
        float secondMask = smoothstep(
                parameter(p, 2, 0.58f) - softness,
                parameter(p, 2, 0.58f) + softness,
                field
        );
        float thirdMask = smoothstep(
                parameter(p, 3, 0.76f) - softness,
                parameter(p, 3, 0.76f) + softness,
                field
        );
        uint32_t result = mixColor(color(c, 0, 0xff202719u), color(c, 1, 0xff4f6134u), firstMask);
        result = mixColor(result, color(c, 2, 0xff786344u), secondMask);
        return mixColor(result, color(c, 3, 0xffb4a477u), thirdMask);
    }

    uint32_t paper(
            NoiseBank &noise,
            float x,
            float y,
            int seed,
            const std::vector<float> &p,
            const std::vector<uint32_t> &c
    ) {
        float scale = std::max(parameter(p, 0, 0.012f), 0.00001f);
        float density = std::max(parameter(p, 1, 72.0f), 1.0f);
        float fiberStrength = clamp01(parameter(p, 2, 0.24f));
        float grain = clamp01(parameter(p, 3, 0.16f));
        float stains = clamp01(parameter(p, 4, 0.14f));
        float roughness = clamp01(parameter(p, 5, 0.35f));
        float body = noise.fbm(x * scale, y * scale);
        float fibers = std::pow(std::abs(std::sin((x + noise.fine(x * scale, y * scale) * 8.0f) * scale * density)), 18.0f);
        fibers = std::max(fibers, std::pow(std::abs(std::sin((y + body * 7.0f) * scale * density * 0.83f)), 22.0f));
        float speckle = hash(x, y, seed);
        float tone = clamp01(0.58f + (body - 0.5f) * roughness + (speckle - 0.5f) * grain);
        uint32_t result = mixColor(color(c, 0, 0xffe4d4aeu), color(c, 1, 0xfff5e9c9u), tone);
        result = mixColor(result, color(c, 2, 0xffb59c70u), fibers * fiberStrength);
        float stain = smoothstep(0.72f, 0.94f, noise.fbm(x * scale * 0.35f, y * scale * 0.35f)) * stains;
        return mixColor(result, color(c, 3, 0xff8a6841u), stain);
    }

    uint32_t rust(
            NoiseBank &noise,
            float x,
            float y,
            const std::vector<float> &p,
            const std::vector<uint32_t> &c
    ) {
        float scale = std::max(parameter(p, 0, 0.009f), 0.00001f);
        float corrosion = clamp01(parameter(p, 1, 0.55f));
        float pitting = clamp01(parameter(p, 2, 0.34f));
        float flakes = clamp01(parameter(p, 3, 0.42f));
        noise.warp(x, y, scale, parameter(p, 4, 16.0f));
        float amount = std::max(parameter(p, 5, 1.25f), 0.0f);
        float field = contrast(noise.fbm(x * scale, y * scale), amount);
        float rustMask = smoothstep(0.72f - corrosion * 0.55f, 0.82f - corrosion * 0.25f, field);
        float oxidation = noise.fine(x * scale * 4.2f, y * scale * 4.2f);
        uint32_t rustColor = mixColor(color(c, 1, 0xff3a170bu), color(c, 2, 0xff9b3e13u), oxidation);
        rustColor = mixColor(rustColor, color(c, 3, 0xffe07822u), smoothstep(0.68f, 0.95f, oxidation) * flakes);
        float pits = (1.0f - smoothstep(0.0f, 0.16f, noise.cellEdge(x, y, scale * 2.4f, 1.0f))) * pitting;
        return mixColor(mixColor(color(c, 0, 0xff596065u), rustColor, rustMask), color(c, 1, 0xff3a170bu), pits);
    }

    uint32_t fabric(
            NoiseBank &noise,
            float x,
            float y,
            int seed,
            const std::vector<float> &p,
            const std::vector<uint32_t> &c
    ) {
        float scale = std::max(parameter(p, 0, 0.018f), 0.00001f);
        float horizontal = std::max(parameter(p, 1, 28.0f), 1.0f);
        float vertical = std::max(parameter(p, 2, 28.0f), 1.0f);
        float irregularity = clamp01(parameter(p, 3, 0.14f));
        float depth = clamp01(parameter(p, 4, 0.48f));
        float fuzz = clamp01(parameter(p, 5, 0.1f));
        float wobble = (noise.fine(x * scale * 2.0f, y * scale * 2.0f) - 0.5f) * irregularity * 2.0f;
        float warp = 0.5f + 0.5f * std::cos((x * scale * vertical + wobble) * 2.0f * PI);
        float weft = 0.5f + 0.5f * std::cos((y * scale * horizontal - wobble) * 2.0f * PI);
        float over = std::fmod(std::floor(x * scale * vertical) + std::floor(y * scale * horizontal), 2.0f);
        float thread = over < 1.0f ? warp : weft;
        uint32_t result = mixColor(color(c, 0, 0xff496b82u), color(c, 1, 0xff7895a7u), over);
        result = mixColor(result, color(c, 2, 0xff263c4bu), (1.0f - thread) * depth);
        result = mixColor(result, color(c, 3, 0xffb6cad4u), smoothstep(0.72f, 1.0f, thread) * depth);
        return mixColor(result, color(c, 3, 0xffb6cad4u), smoothstep(1.0f - fuzz * 0.12f, 1.0f, hash(x, y, seed)) * fuzz);
    }

    uint32_t topography(
            NoiseBank &noise,
            float x,
            float y,
            const std::vector<float> &p,
            const std::vector<uint32_t> &c
    ) {
        float scale = std::max(parameter(p, 0, 0.0055f), 0.00001f);
        float lineCount = std::max(parameter(p, 1, 16.0f), 1.0f);
        float thickness = std::clamp(parameter(p, 2, 0.11f), 0.001f, 0.49f);
        float shading = clamp01(parameter(p, 3, 0.42f));
        noise.warp(x, y, scale * 0.75f, parameter(p, 4, 12.0f));
        float height = contrast(noise.fbm(x * scale, y * scale), parameter(p, 5, 1.1f));
        float contourPosition = fract(height * lineCount);
        float distance = std::min(contourPosition, 1.0f - contourPosition);
        float line = 1.0f - smoothstep(thickness * 0.45f, thickness, distance);
        float fill = 0.5f + (height - 0.5f) * shading * 2.0f;
        return mixColor(mixColor(color(c, 0, 0xff173c3bu), color(c, 1, 0xffb5c98au), fill), color(c, 2, 0xfff2e6b6u), line);
    }

    uint32_t cells(
            NoiseBank &noise,
            float x,
            float y,
            const std::vector<float> &p,
            const std::vector<uint32_t> &c
    ) {
        float scale = std::max(parameter(p, 0, 0.018f), 0.00001f);
        float jitter = parameter(p, 1, 0.92f);
        float borderWidth = std::clamp(parameter(p, 2, 0.12f), 0.001f, 0.9f);
        float glow = clamp01(parameter(p, 3, 0.28f));
        noise.warp(x, y, scale, parameter(p, 4, 6.0f));
        float variation = clamp01(parameter(p, 5, 0.42f));
        float edgeDistance = noise.cellEdge(x, y, scale, jitter);
        float edge = 1.0f - smoothstep(borderWidth * 0.35f, borderWidth, edgeDistance);
        float halo = (1.0f - smoothstep(borderWidth, borderWidth * 3.5f, edgeDistance)) * glow;
        float cellTone = noise.cellVariation(x, y, scale, jitter) * variation;
        uint32_t result = mixColor(color(c, 0, 0xff101a24u), color(c, 1, 0xff216e78u), 0.55f + cellTone);
        result = mixColor(result, color(c, 2, 0xff5ad1c8u), std::max(edge, halo));
        return mixColor(result, color(c, 3, 0xffc9fff4u), edge * glow);
    }

    uint32_t cracks(
            NoiseBank &noise,
            float x,
            float y,
            const std::vector<float> &p,
            const std::vector<uint32_t> &c
    ) {
        float scale = std::max(parameter(p, 0, 0.019f), 0.00001f);
        float width = std::clamp(parameter(p, 1, 0.065f), 0.001f, 0.5f);
        float density = clamp01(parameter(p, 2, 0.72f));
        noise.warp(x, y, scale, parameter(p, 3, 9.0f));
        float depth = clamp01(parameter(p, 4, 0.72f));
        float branching = clamp01(parameter(p, 5, 0.45f));
        float primaryDistance = noise.cellEdge(x, y, scale, 1.0f);
        float secondaryDistance = noise.secondaryCellEdge(x + 37.0f, y - 19.0f, scale * 1.9f, 0.82f);
        float primary = 1.0f - smoothstep(width * 0.45f, width, primaryDistance);
        float secondary = (1.0f - smoothstep(width * 0.28f, width * 0.72f, secondaryDistance)) * branching;
        float crack = std::max(primary, secondary) * density;
        float edge = (1.0f - smoothstep(width, width * 2.8f, primaryDistance)) * (1.0f - crack) * depth;
        float surface = noise.fbm(x * scale * 0.65f, y * scale * 0.65f);
        uint32_t result = mixColor(color(c, 0, 0xff8d8374u), color(c, 1, 0xffc0b6a5u), surface);
        result = mixColor(result, color(c, 3, 0xff554e45u), edge);
        return mixColor(result, color(c, 2, 0xff161412u), crack * depth);
    }

    uint32_t waterRipples(
            NoiseBank &noise,
            float x,
            float y,
            const std::vector<float> &p,
            const std::vector<uint32_t> &c
    ) {
        float scale = std::max(parameter(p, 0, 0.0075f), 0.00001f);
        float frequency = std::max(parameter(p, 1, 22.0f), 0.1f);
        float distortion = parameter(p, 2, 22.0f);
        float caustics = clamp01(parameter(p, 3, 0.58f));
        float depth = clamp01(parameter(p, 4, 0.55f));
        float highlights = clamp01(parameter(p, 5, 0.62f));
        noise.warp(x, y, scale * 0.8f, distortion);
        float flow = noise.fbm(x * scale, y * scale);
        float waveA = std::sin((x * 0.78f + y * 0.22f) * scale * frequency + flow * 5.0f);
        float waveB = std::sin((x * -0.31f + y * 0.95f) * scale * frequency * 0.73f - flow * 4.0f);
        float waves = clamp01(0.5f + (waveA + waveB) * 0.22f);
        float lightLines = std::pow(clamp01(1.0f - std::abs(waveA + waveB) * 0.5f), 7.0f) * caustics;
        float tone = clamp01(waves * depth + flow * (1.0f - depth));
        return mixColor(mixColor(color(c, 0, 0xff073456u), color(c, 1, 0xff168eaeu), tone), color(c, 2, 0xffb9f7f2u), lightLines * highlights);
    }

    uint32_t foliage(
            NoiseBank &noise,
            float x,
            float y,
            const std::vector<float> &p,
            const std::vector<uint32_t> &c
    ) {
        float scale = std::max(parameter(p, 0, 0.022f), 0.00001f);
        float density = clamp01(parameter(p, 1, 0.72f));
        float softness = std::clamp(parameter(p, 2, 0.16f), 0.01f, 0.6f);
        float veins = clamp01(parameter(p, 3, 0.38f));
        float lighting = clamp01(parameter(p, 4, 0.62f));
        float variation = clamp01(parameter(p, 5, 0.58f));
        noise.warp(x, y, scale, 4.0f + variation * 10.0f);
        float edgeDistance = noise.cellEdge(x, y, scale, 0.92f);
        float cellTone = noise.cellVariation(x, y, scale, 0.92f);
        float leafMask = smoothstep(softness * 0.35f, softness, edgeDistance);
        float overlap = noise.fbm(x * scale * 0.7f, y * scale * 0.7f);
        leafMask *= smoothstep(1.0f - density, 1.0f, overlap + density * 0.55f);
        float vein = (1.0f - smoothstep(softness, softness * 2.1f, edgeDistance)) * veins;
        float detail = noise.fine(x * scale * 2.5f, y * scale * 2.5f);
        float light = clamp01(cellTone * variation + detail * lighting);
        uint32_t leaf = mixColor(color(c, 1, 0xff245c25u), color(c, 2, 0xff4e9a3du), light);
        leaf = mixColor(leaf, color(c, 3, 0xffa4ce65u), smoothstep(0.68f, 0.96f, light) * lighting);
        leaf = mixColor(leaf, color(c, 1, 0xff245c25u), vein);
        return mixColor(color(c, 0, 0xff102a13u), leaf, leafMask);
    }

    uint32_t bricks(
            NoiseBank &noise,
            float x,
            float y,
            const std::vector<float> &p,
            const std::vector<uint32_t> &c
    ) {
        float scale = std::max(parameter(p, 0, 0.012f), 0.00001f);
        float aspect = std::max(parameter(p, 1, 2.15f), 0.25f);
        float mortarWidth = std::clamp(parameter(p, 2, 0.09f), 0.005f, 0.45f);
        float irregularity = clamp01(parameter(p, 3, 0.18f));
        float roughness = clamp01(parameter(p, 4, 0.42f));
        float bevel = clamp01(parameter(p, 5, 0.55f));
        float row = std::floor(y * scale);
        float stagger = std::fmod(std::abs(row), 2.0f) * 0.5f;
        float wobble = (noise.fine(x * scale * 0.7f, y * scale * 0.7f) - 0.5f) * irregularity;
        float ux = fract(x * scale / aspect + stagger + wobble * 0.12f);
        float uy = fract(y * scale + wobble * 0.1f);
        float dx = std::min(ux, 1.0f - ux) * aspect;
        float edgeDistance = std::min(dx, std::min(uy, 1.0f - uy));
        float brickMask = smoothstep(mortarWidth * 0.65f, mortarWidth, edgeDistance);
        float edgeLight = smoothstep(mortarWidth, mortarWidth * 3.5f, edgeDistance) * bevel;
        float surface = noise.fine(x * scale * 5.0f, y * scale * 5.0f);
        float brickId = hash(std::floor(x * scale / aspect + stagger), row, 1777);
        float tone = clamp01(0.35f + brickId * 0.38f + (surface - 0.5f) * roughness * 0.7f);
        uint32_t brick = mixColor(color(c, 1, 0xff6f2418u), color(c, 2, 0xffb64c32u), tone);
        brick = mixColor(brick, color(c, 3, 0xffd77a55u), edgeLight * 0.32f);
        return mixColor(color(c, 0, 0xffb8ad99u), brick, brickMask);
    }

    uint32_t terrain(
            NoiseBank &noise,
            float x,
            float y,
            const std::vector<float> &p,
            const std::vector<uint32_t> &c
    ) {
        float scale = std::max(parameter(p, 0, 0.0045f), 0.00001f);
        float waterLevel = clamp01(parameter(p, 1, 0.34f));
        float mountainLevel = clamp01(parameter(p, 2, 0.7f));
        float erosion = clamp01(parameter(p, 3, 0.46f));
        float detail = clamp01(parameter(p, 4, 0.58f));
        float snowLevel = clamp01(parameter(p, 5, 0.86f));
        noise.warp(x, y, scale * 0.7f, erosion * 38.0f);
        float broad = noise.fbm(x * scale, y * scale);
        float fine = noise.fine(x * scale * 2.3f, y * scale * 2.3f);
        float ridges = 1.0f - std::abs(fine * 2.0f - 1.0f);
        float height = clamp01(broad * (1.0f - detail * 0.3f) + ridges * detail * 0.3f);
        float left = noise.fbm((x - 2.0f) * scale, y * scale);
        float right = noise.fbm((x + 2.0f) * scale, y * scale);
        float slope = std::abs(right - left) * 9.0f;
        float shore = smoothstep(waterLevel - 0.025f, waterLevel + 0.035f, height);
        float rock = smoothstep(mountainLevel - 0.12f, mountainLevel + 0.1f, height + slope);
        uint32_t land = mixColor(color(c, 1, 0xff5f873fu), color(c, 2, 0xff746c5du), rock);
        land = mixColor(land, color(c, 3, 0xffe9eee8u), smoothstep(snowLevel - 0.07f, snowLevel, height));
        uint32_t water = mixColor(color(c, 0, 0xff194d73u), color(c, 1, 0xff5f873fu), smoothstep(waterLevel - 0.12f, waterLevel, height) * 0.2f);
        return mixColor(water, land, shore);
    }

    uint32_t ice(
            NoiseBank &noise,
            float x,
            float y,
            int seed,
            const std::vector<float> &p,
            const std::vector<uint32_t> &c
    ) {
        float scale = std::max(parameter(p, 0, 0.014f), 0.00001f);
        float crackWidth = std::clamp(parameter(p, 1, 0.075f), 0.002f, 0.4f);
        float frost = clamp01(parameter(p, 2, 0.48f));
        float depth = clamp01(parameter(p, 3, 0.64f));
        noise.warp(x, y, scale, parameter(p, 4, 8.0f));
        float sparkle = clamp01(parameter(p, 5, 0.32f));
        float body = noise.fbm(x * scale * 0.65f, y * scale * 0.65f);
        float edgeDistance = noise.cellEdge(x, y, scale, 0.95f);
        float crack = 1.0f - smoothstep(crackWidth * 0.45f, crackWidth, edgeDistance);
        float fine = noise.fine(x * scale * 3.2f, y * scale * 3.2f);
        float frostMask = smoothstep(0.58f, 0.9f, fine) * frost;
        float speck = smoothstep(1.0f - sparkle * 0.025f, 1.0f, hash(x, y, seed)) * sparkle;
        uint32_t result = mixColor(color(c, 0, 0xff155a79u), color(c, 1, 0xff79c8dcu), clamp01(body + depth * 0.2f));
        result = mixColor(result, color(c, 2, 0xffd8f4f5u), std::max(frostMask, speck));
        return mixColor(result, color(c, 3, 0xfff5ffffu), crack * depth);
    }

    uint32_t sand(
            NoiseBank &noise,
            float x,
            float y,
            int seed,
            const std::vector<float> &p,
            const std::vector<uint32_t> &c
    ) {
        float scale = std::max(parameter(p, 0, 0.005f), 0.00001f);
        float frequency = std::max(parameter(p, 1, 12.0f), 0.1f);
        float angle = parameter(p, 2, 0.32f) * 2.0f * PI;
        float ripples = clamp01(parameter(p, 3, 0.62f));
        float grain = clamp01(parameter(p, 4, 0.22f));
        float amount = std::max(parameter(p, 5, 1.18f), 0.0f);
        float dune = noise.fbm(x * scale, y * scale);
        float axis = x * std::cos(angle) + y * std::sin(angle);
        float wave = 0.5f + 0.5f * std::sin(axis * scale * frequency + dune * 5.0f);
        float grains = hash(x, y, seed);
        float tone = dune * (1.0f - ripples * 0.42f) + wave * ripples * 0.42f;
        tone = contrast(tone + (grains - 0.5f) * grain * 0.22f, amount);
        uint32_t result = mixColor(color(c, 0, 0xff9a632eu), color(c, 1, 0xffd6a85au), tone);
        float highlight = smoothstep(0.7f, 0.96f, wave) * ripples * 0.45f;
        return mixColor(result, color(c, 2, 0xfff2d58du), highlight);
    }

    uint32_t nebula(
            NoiseBank &noise,
            float x,
            float y,
            int seed,
            const std::vector<float> &p,
            const std::vector<uint32_t> &c
    ) {
        float scale = std::max(parameter(p, 0, 0.004f), 0.00001f);
        noise.warp(x, y, scale * 0.65f, parameter(p, 1, 44.0f));
        float density = clamp01(parameter(p, 2, 0.64f));
        float stars = clamp01(parameter(p, 3, 0.38f));
        float glow = clamp01(parameter(p, 4, 0.72f));
        float amount = std::max(parameter(p, 5, 1.45f), 0.0f);
        float a = noise.fbm(x * scale, y * scale);
        float b = noise.fine(x * scale * 2.1f + 11.0f, y * scale * 2.1f - 7.0f);
        float cloud = contrast(a * 0.68f + b * 0.32f, amount);
        float mask = smoothstep(0.7f - density * 0.42f, 0.88f, cloud);
        uint32_t gas = mixColor(color(c, 1, 0xff542577u), color(c, 2, 0xff2367a2u), b);
        gas = mixColor(gas, color(c, 3, 0xfff2a4dfu), smoothstep(0.7f, 0.96f, cloud) * glow);
        uint32_t result = mixColor(color(c, 0, 0xff050713u), gas, mask);
        float star = smoothstep(1.0f - stars * 0.012f, 1.0f, hash(x, y, seed));
        return mixColor(result, 0xffffffffu, star);
    }

    uint32_t honeycomb(
            NoiseBank &noise,
            float x,
            float y,
            const std::vector<float> &p,
            const std::vector<uint32_t> &c
    ) {
        float scale = std::max(parameter(p, 0, 0.018f), 0.00001f);
        float borderWidth = std::clamp(parameter(p, 1, 0.095f), 0.005f, 0.4f);
        float bevel = clamp01(parameter(p, 2, 0.5f));
        float irregularity = clamp01(parameter(p, 3, 0.12f));
        float fill = clamp01(parameter(p, 4, 0.72f));
        float glow = clamp01(parameter(p, 5, 0.35f));
        float wobble = (noise.fine(x * scale, y * scale) - 0.5f) * irregularity;
        float px = x * scale + wobble;
        float py = y * scale + wobble * 0.7f;
        float ax = fract(px / 1.7320508f) * 1.7320508f - 0.8660254f;
        float ay = fract(py) - 0.5f;
        float bx = fract((px - 0.8660254f) / 1.7320508f) * 1.7320508f - 0.8660254f;
        float by = fract(py - 0.5f) - 0.5f;
        bool useA = ax * ax + ay * ay < bx * bx + by * by;
        float gx = useA ? ax : bx;
        float gy = useA ? ay : by;
        float hexDistance = std::max(std::abs(gx) * 0.8660254f + std::abs(gy) * 0.5f, std::abs(gy));
        float edgeDistance = 0.5f - hexDistance;
        float interior = smoothstep(borderWidth * 0.55f, borderWidth, edgeDistance);
        float bevelLight = smoothstep(borderWidth, borderWidth * 3.2f, edgeDistance) * bevel;
        float center = clamp01(1.0f - hexDistance * 1.7f);
        uint32_t cell = mixColor(color(c, 1, 0xff6f3a08u), color(c, 2, 0xffe59b12u), center * fill);
        cell = mixColor(cell, color(c, 3, 0xffffd45cu), bevelLight * glow);
        return mixColor(color(c, 0, 0xff241305u), cell, interior);
    }

    uint32_t grass(NoiseBank &n, float x, float y, const std::vector<float> &p, const std::vector<uint32_t> &c) {
        float s = std::max(parameter(p, 0, 0.012f), 0.00001f);
        float density = clamp01(parameter(p, 1, 1.0f));
        float length = std::max(parameter(p, 2, 50.0f), 1.0f);
        float wind = parameter(p, 3, 1.0f);
        float patchiness = clamp01(parameter(p, 4, 0.35f));
        float highlights = clamp01(parameter(p, 5, 0.45f));
        float patch = n.fbm(x * s * 0.45f, y * s * 0.45f);
        float bend = n.fbm(x * s, y * s) * wind * 12.0f;
        float blades = std::pow(clamp01(0.5f + 0.5f * std::sin((x * s + bend) * length + y * s * 1.7f)), 9.0f);
        float cover = smoothstep(1.0f - density, 0.92f, patch + density * 0.42f);
        float tone = clamp01(n.fine(x * s * 3.0f, y * s * 3.0f) + patchiness * (patch - 0.5f));
        uint32_t blade = mixColor(color(c, 1, 0xff1d491fu), color(c, 2, 0xff4d8a32u), tone);
        blade = mixColor(blade, color(c, 3, 0xff9bc45au), blades * highlights);
        return mixColor(color(c, 0, 0xff3a2b18u), blade, clamp01(cover * 0.75f + blades * 0.5f));
    }

    uint32_t dirt(NoiseBank &n, float x, float y, int seed, const std::vector<float> &p, const std::vector<uint32_t> &c) {
        float s = std::max(parameter(p, 0, 0.009f), 0.00001f);
        float clumps = clamp01(parameter(p, 1, 0.62f));
        float moisture = clamp01(parameter(p, 2, 0.35f));
        float pebbles = clamp01(parameter(p, 3, 0.22f));
        float roughness = clamp01(parameter(p, 4, 0.68f));
        float variation = clamp01(parameter(p, 5, 0.52f));
        float broad = n.fbm(x * s, y * s);
        float crumb = n.fine(x * s * 5.0f, y * s * 5.0f);
        float body = clamp01(broad * (1.0f - clumps * 0.3f) + crumb * clumps * 0.3f);
        body = clamp01(body + (hash(x, y, seed) - 0.5f) * roughness * 0.22f);
        uint32_t earth = mixColor(color(c, 0, 0xff2a190eu), color(c, 1, 0xff694226u), body + moisture * 0.1f);
        earth = mixColor(earth, color(c, 2, 0xffa1764du), smoothstep(0.58f, 0.9f, broad) * variation * (1.0f - moisture));
        float pebble = smoothstep(1.0f - pebbles * 0.018f, 1.0f, hash(x * 0.37f, y * 0.37f, seed + 91));
        return mixColor(earth, color(c, 3, 0xffb1a28cu), pebble);
    }

    uint32_t leather(NoiseBank &n, float x, float y, const std::vector<float> &p, const std::vector<uint32_t> &c) {
        float s = std::max(parameter(p, 0, 0.011f), 0.00001f);
        float wrinkles = clamp01(parameter(p, 1, 0.58f));
        float pores = clamp01(parameter(p, 2, 0.42f));
        float grain = clamp01(parameter(p, 3, 0.5f));
        float softness = clamp01(parameter(p, 4, 0.38f));
        float shine = clamp01(parameter(p, 5, 0.035f));
        n.warp(x, y, s, 10.0f * wrinkles);
        float base = n.fbm(x * s, y * s);
        float fold = 1.0f - std::abs(std::sin((x * 0.63f + y) * s * 8.0f + base * 7.0f));
        float fine = n.fine(x * s * 8.0f, y * s * 8.0f);
        float pore = smoothstep(0.78f, 0.96f, fine) * pores;
        float tone = clamp01(0.42f + (base - 0.5f) * grain + fold * wrinkles * 0.24f);
        uint32_t result = mixColor(color(c, 0, 0xff30160fu), color(c, 1, 0xff7b3d25u), tone + softness * 0.18f);
        result = mixColor(result, color(c, 2, 0xffb36b43u), smoothstep(0.72f, 0.98f, fold) * shine);
        return mixColor(result, color(c, 3, 0xff1c0c08u), pore);
    }

    uint32_t concrete(NoiseBank &n, float x, float y, int seed, const std::vector<float> &p, const std::vector<uint32_t> &c) {
        float s = std::max(parameter(p, 0, 0.014f), 0.00001f);
        float aggregate = clamp01(parameter(p, 1, 0.45f));
        float stains = clamp01(parameter(p, 2, 0.28f));
        float roughness = clamp01(parameter(p, 3, 0.65f));
        float cracks = clamp01(parameter(p, 4, 0.14f));
        float amount = parameter(p, 5, 1.08f);
        n.warp(x, y, s * 0.7f, 16.0f);
        float body = contrast(n.fbm(x * s, y * s), amount);
        float grit = n.fine(x * s * 6.0f, y * s * 6.0f);
        float tone = clamp01(body * 0.65f + grit * roughness * 0.35f);
        uint32_t result = mixColor(color(c, 0, 0xff5a5955u), color(c, 2, 0xffc7c3b9u), tone);
        result = mixColor(result, color(c, 1, 0xff99968eu), 0.35f + aggregate * 0.3f);
        result = mixColor(result, color(c, 0, 0xff5a5955u), smoothstep(0.72f, 0.94f, n.fbm(x * s * 0.3f, y * s * 0.3f)) * stains);
        float line = std::abs(std::sin((x + y * 0.41f) * s * 3.0f + body * 12.0f));
        float crack = (1.0f - smoothstep(0.0f, 0.018f + cracks * 0.09f, line)) * cracks;
        float speck = smoothstep(0.995f, 1.0f, hash(x, y, seed)) * aggregate;
        return mixColor(mixColor(result, color(c, 3, 0xff353532u), crack), color(c, 2, 0xffc7c3b9u), speck);
    }

    uint32_t asphalt(NoiseBank &n, float x, float y, int seed, const std::vector<float> &p, const std::vector<uint32_t> &c) {
        float s = std::max(parameter(p, 0, 0.02f), 0.00001f);
        float aggregate = clamp01(parameter(p, 1, 0.72f));
        float tar = clamp01(parameter(p, 2, 0.44f));
        float wear = clamp01(parameter(p, 3, 0.28f));
        float speckles = clamp01(parameter(p, 4, 0.48f));
        float amount = parameter(p, 5, 1.3f);
        float coarse = contrast(n.fine(x * s * 2.2f, y * s * 2.2f), amount);
        float fine = hash(x, y, seed);
        float tone = clamp01(coarse * aggregate + fine * (1.0f - aggregate));
        uint32_t result = mixColor(color(c, 0, 0xff17191au), color(c, 1, 0xff3e4242u), tone + tar * 0.12f);
        float stone = smoothstep(0.72f, 0.96f, coarse) * aggregate;
        result = mixColor(result, color(c, 2, 0xff898b85u), stone);
        float dust = smoothstep(1.0f - speckles * 0.035f, 1.0f, fine) * (0.4f + wear * 0.6f);
        return mixColor(result, color(c, 3, 0xffb0a995u), dust);
    }

    uint32_t moss(NoiseBank &n, float x, float y, int seed, const std::vector<float> &p, const std::vector<uint32_t> &c) {
        float s = std::max(parameter(p, 0, 0.018f), 0.00001f);
        float density = clamp01(parameter(p, 1, 0.74f));
        float fibers = clamp01(parameter(p, 2, 0.58f));
        float moisture = clamp01(parameter(p, 3, 0.38f));
        float variation = clamp01(parameter(p, 4, 0.62f));
        float clumps = clamp01(parameter(p, 5, 0.46f));
        float patch = n.fbm(x * s * 0.65f, y * s * 0.65f);
        float fuzz = n.fine(x * s * 4.0f, y * s * 4.0f);
        float mask = smoothstep(1.0f - density, 0.9f, patch + density * 0.45f);
        float tone = clamp01(fuzz * variation + patch * (1.0f - variation));
        uint32_t result = mixColor(color(c, 1, 0xff29451cu), color(c, 2, 0xff648044u), tone);
        float tips = smoothstep(0.75f, 0.96f, fuzz) * fibers;
        tips += smoothstep(0.995f, 1.0f, hash(x, y, seed)) * fibers;
        result = mixColor(result, color(c, 3, 0xffb1bd69u), clamp01(tips));
        result = mixColor(result, color(c, 1, 0xff29451cu), moisture * (1.0f - tone) * 0.4f);
        return mixColor(color(c, 0, 0xff292516u), result, mask * (0.7f + clumps * 0.3f));
    }

    uint32_t fire(NoiseBank &n, float x, float y, const std::vector<float> &p, const std::vector<uint32_t> &c) {
        float s = std::max(parameter(p, 0, 0.006f), 0.00001f);
        float frequency = parameter(p, 1, 8.0f);
        float turbulence = parameter(p, 2, 36.0f);
        float intensity = clamp01(parameter(p, 3, 0.78f));
        float smoke = clamp01(parameter(p, 4, 0.18f));
        float detail = clamp01(parameter(p, 5, 0.62f));
        n.warp(x, y, s, turbulence);
        float base = n.fbm(x * s, y * s * 1.7f);
        float tongues = 0.5f + 0.5f * std::sin(x * s * frequency + base * 9.0f - y * s * 3.0f);
        float flame = clamp01(base * (1.0f - detail * 0.35f) + tongues * detail * 0.35f);
        flame = smoothstep(0.28f + smoke * 0.25f, 0.9f, flame) * intensity;
        uint32_t result = mixColor(color(c, 0, 0xff100807u), color(c, 1, 0xffb51b08u), flame);
        result = mixColor(result, color(c, 2, 0xffff7a0au), smoothstep(0.38f, 0.75f, flame));
        return mixColor(result, color(c, 3, 0xffffe98au), smoothstep(0.72f, 0.98f, flame));
    }

    uint32_t aurora(NoiseBank &n, float x, float y, int seed, const std::vector<float> &p, const std::vector<uint32_t> &c) {
        float s = std::max(parameter(p, 0, 0.004f), 0.00001f);
        float ribbons = parameter(p, 1, 7.0f);
        n.warp(x, y, s * 0.7f, parameter(p, 2, 34.0f));
        float glow = clamp01(parameter(p, 3, 0.72f));
        float stars = clamp01(parameter(p, 4, 0.2f));
        float amount = parameter(p, 5, 1.32f);
        float flow = n.fbm(x * s, y * s);
        float band = std::pow(clamp01(0.5f + 0.5f * std::sin(y * s * ribbons + flow * 9.0f)), 3.0f);
        band = contrast(band, amount);
        uint32_t light = mixColor(color(c, 1, 0xff42d9a0u), color(c, 2, 0xff65e4e8u), flow);
        light = mixColor(light, color(c, 3, 0xffa56de2u), smoothstep(0.62f, 0.95f, flow));
        uint32_t result = mixColor(color(c, 0, 0xff061227u), light, band * glow);
        float star = smoothstep(1.0f - stars * 0.009f, 1.0f, hash(x, y, seed));
        return mixColor(result, 0xffffffffu, star);
    }

    uint32_t oilSlick(NoiseBank &n, float x, float y, const std::vector<float> &p, const std::vector<uint32_t> &c) {
        float s = std::max(parameter(p, 0, 0.008f), 0.00001f);
        float bands = parameter(p, 1, 13.0f);
        n.warp(x, y, s, parameter(p, 2, 32.0f));
        float iridescence = clamp01(parameter(p, 3, 0.82f));
        float darkness = clamp01(parameter(p, 4, 0.3f));
        float amount = parameter(p, 5, 1.2f);
        float field = n.fbm(x * s, y * s);
        float phase = fract((field + x * s * 0.13f - y * s * 0.08f) * bands);
        phase = contrast(phase, amount);
        uint32_t spectrum = mixColor(color(c, 1, 0xffd5329bu), color(c, 2, 0xff25d4d0u), smoothstep(0.0f, 0.5f, phase));
        spectrum = mixColor(spectrum, color(c, 3, 0xfff0c33cu), smoothstep(0.5f, 1.0f, phase));
        return mixColor(color(c, 0, 0xff10101au), spectrum, iridescence * (1.0f - darkness * 0.65f));
    }

    uint32_t watercolor(NoiseBank &n, float x, float y, int seed, const std::vector<float> &p, const std::vector<uint32_t> &c) {
        float s = std::max(parameter(p, 0, 0.006f), 0.00001f);
        float blooms = clamp01(parameter(p, 1, 0.72f));
        float pigment = clamp01(parameter(p, 2, 0.65f));
        float edges = clamp01(parameter(p, 3, 0.38f));
        float paper = clamp01(parameter(p, 4, 0.22f));
        float diffusion = clamp01(parameter(p, 5, 0.62f));
        n.warp(x, y, s, diffusion * 28.0f);
        float a = n.fbm(x * s, y * s);
        float b = n.fbm(x * s * 0.82f + 9.0f, y * s * 0.82f - 13.0f);
        float granulation = n.fine(x * s * 5.5f, y * s * 5.5f);
        float firstWash = smoothstep(0.38f - blooms * 0.16f, 0.68f, a);
        float secondWash = smoothstep(0.48f - blooms * 0.12f, 0.77f, b);
        firstWash *= 0.58f + granulation * 0.42f;
        secondWash *= 0.62f + (1.0f - granulation) * 0.38f;

        float firstBoundary = 1.0f - smoothstep(0.018f, 0.09f, std::abs(a - 0.5f));
        float secondBoundary = 1.0f - smoothstep(0.018f, 0.085f, std::abs(b - 0.56f));
        float pooling = std::max(firstBoundary, secondBoundary) * edges;

        uint32_t result = color(c, 0, 0xfff4ebd8u);
        result = mixColor(result, color(c, 1, 0xff337fb3u), firstWash * pigment * 0.72f);
        result = mixColor(result, color(c, 2, 0xffd85d79u), secondWash * pigment * 0.68f);
        result = mixColor(result, color(c, 3, 0xff493f76u), pooling * pigment * 0.7f);

        float horizontalFiber = std::pow(std::abs(std::sin((y + a * 3.0f) * s * 85.0f)), 24.0f);
        float paperGrain = std::abs(hash(x, y, seed) - 0.5f) * 2.0f;
        float fiber = clamp01(horizontalFiber * 0.55f + paperGrain * 0.45f) * paper;
        uint32_t paperShade = hash(x, y, seed + 31) > 0.5f ? 0xffffffffu : 0xffc8bba4u;
        return mixColor(result, paperShade, fiber * 0.16f);
    }

    uint32_t flowTexture(NoiseBank &n, float x, float y, const std::vector<float> &p, const std::vector<uint32_t> &c) {
        float s = std::max(parameter(p, 0, 0.006f), 0.00001f);
        float frequency = parameter(p, 1, 12.0f);
        n.warp(x, y, s, parameter(p, 2, 46.0f));
        float symmetry = clamp01(parameter(p, 3, 0.3f));
        float sharpness = std::max(parameter(p, 4, 1.25f), 0.05f);
        float glow = clamp01(parameter(p, 5, 0.62f));
        float q = n.fbm(x * s, y * s);
        float phase = std::sin((x + y * symmetry) * s * frequency + q * 11.0f);
        float band = std::pow(clamp01(0.5f + 0.5f * phase), sharpness);
        uint32_t result = mixColor(color(c, 0, 0xff111128u), color(c, 1, 0xff5155d9u), band);
        result = mixColor(result, color(c, 2, 0xffde4bb3u), smoothstep(0.42f, 0.82f, q));
        float line = std::pow(clamp01(1.0f - std::abs(phase)), 8.0f) * glow;
        return mixColor(result, color(c, 3, 0xff7ef2e7u), line);
    }

    uint32_t opal(NoiseBank &n, float x, float y, const std::vector<float> &p, const std::vector<uint32_t> &c) {
        float s = std::max(parameter(p, 0, 0.007f), 0.00001f); float play = clamp01(parameter(p, 1, 0.82f));
        float milk = clamp01(parameter(p, 2, 0.48f)); float bands = parameter(p, 3, 8.0f);
        n.warp(x, y, s, parameter(p, 4, 28.0f)); float glow = clamp01(parameter(p, 5, 0.62f));
        float a = n.fbm(x * s, y * s); float b = n.fine(x * s * 2.0f, y * s * 2.0f);
        float phase = fract((a * 0.7f + b * 0.3f) * bands); uint32_t spectrum = mixColor(color(c, 1, 0xff4fd5d1u), color(c, 2, 0xfff072b6u), smoothstep(0.15f, 0.58f, phase));
        spectrum = mixColor(spectrum, color(c, 3, 0xffffc857u), smoothstep(0.58f, 0.95f, phase));
        return mixColor(color(c, 0, 0xffdde8dfu), spectrum, play * (1.0f - milk * 0.55f) * smoothstep(0.22f, 0.8f, a + glow * 0.12f));
    }

    uint32_t damascus(NoiseBank &n, float x, float y, const std::vector<float> &p, const std::vector<uint32_t> &c) {
        float s = std::max(parameter(p, 0, 0.006f), 0.00001f); float layers = parameter(p, 1, 22.0f);
        float folding = clamp01(parameter(p, 2, 0.72f)); n.warp(x, y, s, parameter(p, 3, 34.0f));
        float polish = clamp01(parameter(p, 4, 0.58f)); float amount = parameter(p, 5, 1.4f);
        float q = n.fbm(x * s, y * s); float band = contrast(0.5f + 0.5f * std::sin((x + y * 0.22f) * s * layers + q * folding * 13.0f), amount);
        uint32_t metal = mixColor(color(c, 0, 0xff171c20u), color(c, 1, 0xff66727au), band);
        metal = mixColor(metal, color(c, 2, 0xffc8d0d2u), smoothstep(0.76f, 0.98f, band) * polish);
        return mixColor(metal, color(c, 3, 0xff304c57u), smoothstep(0.42f, 0.58f, q) * (1.0f - band) * 0.35f);
    }

    uint32_t lightning(NoiseBank &n, float x, float y, const std::vector<float> &p, const std::vector<uint32_t> &c) {
        float s = std::max(parameter(p, 0, 0.006f), 0.00001f); float branches = parameter(p, 1, 7.0f);
        n.warp(x, y, s, parameter(p, 2, 42.0f)); float width = std::max(parameter(p, 3, 0.055f), 0.002f);
        float glow = clamp01(parameter(p, 4, 0.82f)); float intensity = clamp01(parameter(p, 5, 0.9f));
        float q = n.fbm(x * s, y * s); float pathA = std::abs(std::sin((x * 0.35f + y) * s * branches + q * 11.0f));
        float pathB = std::abs(std::sin((x - y * 0.48f) * s * branches * 1.7f - q * 9.0f));
        float distance = std::min(pathA, pathB + 0.08f); float halo = 1.0f - smoothstep(width, width * 5.0f, distance);
        float bolt = 1.0f - smoothstep(width * 0.25f, width, distance); uint32_t result = mixColor(color(c, 0, 0xff030612u), color(c, 1, 0xff2546a8u), halo * glow);
        result = mixColor(result, color(c, 2, 0xff68c9ffu), bolt * intensity); return mixColor(result, color(c, 3, 0xfff2fcffu), bolt * bolt);
    }

    uint32_t velvet(NoiseBank &n, float x, float y, const std::vector<float> &p, const std::vector<uint32_t> &c) {
        float s = std::max(parameter(p, 0, 0.014f), 0.00001f); float fibers = clamp01(parameter(p, 1, 0.78f));
        float angle = parameter(p, 2, 0.18f) * 2.0f * PI; float softness = clamp01(parameter(p, 3, 0.7f));
        float sheen = clamp01(parameter(p, 4, 0.62f)); float folds = clamp01(parameter(p, 5, 0.34f));
        float axis = x * std::cos(angle) + y * std::sin(angle); float q = n.fbm(x * s * 0.5f, y * s * 0.5f);
        float nap = std::pow(clamp01(0.5f + 0.5f * std::sin(axis * s * 70.0f + q * 4.0f)), 5.0f);
        float fold = 0.5f + 0.5f * std::sin((x - y * 0.3f) * s * 4.0f + q * 8.0f); uint32_t result = mixColor(color(c, 0, 0xff17051eu), color(c, 1, 0xff5e176au), softness + fold * folds * 0.3f);
        result = mixColor(result, color(c, 2, 0xffc148b8u), nap * fibers * sheen); return mixColor(result, color(c, 3, 0xfff0a6d8u), std::pow(nap, 4.0f) * sheen);
    }

    uint32_t inkMarbling(NoiseBank &n, float x, float y, const std::vector<float> &p, const std::vector<uint32_t> &c) {
        float s = std::max(parameter(p, 0, 0.006f), 0.00001f); float ribbons = parameter(p, 1, 14.0f);
        n.warp(x, y, s, parameter(p, 2, 48.0f)); float feather = clamp01(parameter(p, 3, 0.52f));
        float balance = clamp01(parameter(p, 4, 0.5f)); float amount = parameter(p, 5, 1.25f); float q = n.fbm(x * s, y * s);
        float wave = contrast(0.5f + 0.5f * std::sin((x + y * 0.45f) * s * ribbons + q * 12.0f), amount);
        float ink = smoothstep(0.22f - feather * 0.1f, 0.78f + feather * 0.1f, wave); uint32_t result = mixColor(color(c, 0, 0xfff3e9d3u), color(c, 1, 0xff164b78u), ink * (1.0f - balance * 0.35f));
        result = mixColor(result, color(c, 2, 0xff9d2949u), smoothstep(0.58f, 0.92f, q) * balance); return mixColor(result, color(c, 3, 0xff1c1831u), std::pow(clamp01(1.0f - std::abs(wave * 2.0f - 1.0f)), 8.0f) * 0.55f);
    }

    uint32_t holographic(NoiseBank &n, float x, float y, const std::vector<float> &p, const std::vector<uint32_t> &c) {
        float s = std::max(parameter(p, 0, 0.008f), 0.00001f); float spectrum = parameter(p, 1, 12.0f);
        float crinkles = clamp01(parameter(p, 2, 0.7f)); float diffraction = clamp01(parameter(p, 3, 0.82f));
        float angle = parameter(p, 4, 0.3f) * 2.0f * PI; float shine = clamp01(parameter(p, 5, 0.72f));
        float q = n.fbm(x * s, y * s); float axis = x * std::cos(angle) + y * std::sin(angle); float phase = fract(axis * s * spectrum + q * crinkles * 3.0f);
        uint32_t result = mixColor(color(c, 1, 0xff4de5e2u), color(c, 2, 0xffe95ac8u), smoothstep(0.1f, 0.55f, phase)); result = mixColor(result, color(c, 3, 0xfff7ea72u), smoothstep(0.55f, 0.95f, phase));
        result = mixColor(color(c, 0, 0xffcdd4deu), result, diffraction); return mixColor(result, 0xffffffffu, std::pow(clamp01(1.0f - std::abs(phase - 0.5f) * 2.0f), 12.0f) * shine);
    }

    uint32_t bioluminescence(NoiseBank &n, float x, float y, const std::vector<float> &p, const std::vector<uint32_t> &c) {
        float s = std::max(parameter(p, 0, 0.009f), 0.00001f); float veins = clamp01(parameter(p, 1, 0.72f)); float branching = clamp01(parameter(p, 2, 0.58f));
        n.warp(x, y, s, parameter(p, 3, 30.0f)); float glow = clamp01(parameter(p, 4, 0.86f)); float depth = clamp01(parameter(p, 5, 0.62f));
        float a = n.fbm(x * s, y * s); float b = n.fine(x * s * 2.2f, y * s * 2.2f); float ridgeA = 1.0f - std::abs(a * 2.0f - 1.0f); float ridgeB = 1.0f - std::abs(b * 2.0f - 1.0f);
        float network = std::max(std::pow(ridgeA, 9.0f), std::pow(ridgeB, 12.0f) * branching) * veins; uint32_t result = mixColor(color(c, 0, 0xff031a1au), color(c, 1, 0xff0b514du), a * depth);
        result = mixColor(result, color(c, 2, 0xff20e6c2u), smoothstep(0.05f, 0.55f, network) * glow); return mixColor(result, color(c, 3, 0xffc4fff1u), smoothstep(0.62f, 0.98f, network));
    }

    uint32_t cosmicVortex(NoiseBank &n, float x, float y, int width, int height, int seed, const std::vector<float> &p, const std::vector<uint32_t> &c) {
        float s = std::max(parameter(p, 0, 0.008f), 0.00001f); float arms = parameter(p, 1, 5.0f); float twist = parameter(p, 2, 12.0f); float turbulence = clamp01(parameter(p, 3, 0.52f));
        float stars = clamp01(parameter(p, 4, 0.28f)); float coreGlow = clamp01(parameter(p, 5, 0.82f)); float dx = (x - width * 0.5f) / std::max(width, 1); float dy = (y - height * 0.5f) / std::max(height, 1);
        float radius = std::sqrt(dx * dx + dy * dy); float angle = std::atan2(dy, dx); float q = n.fbm(x * s, y * s); float spiral = 0.5f + 0.5f * std::sin(angle * arms + radius * twist * 20.0f + q * turbulence * 8.0f);
        float mask = smoothstep(0.52f, 0.08f, radius) * smoothstep(0.35f, 0.92f, spiral); uint32_t gas = mixColor(color(c, 1, 0xff274aa8u), color(c, 2, 0xff8d3ec2u), q); uint32_t result = mixColor(color(c, 0, 0xff02030bu), gas, mask);
        result = mixColor(result, color(c, 3, 0xffffdda0u), smoothstep(0.18f, 0.0f, radius) * coreGlow); return mixColor(result, 0xffffffffu, smoothstep(1.0f - stars * 0.009f, 1.0f, hash(x, y, seed)));
    }

    uint32_t lavaLamp(NoiseBank &n, float x, float y, const std::vector<float> &p, const std::vector<uint32_t> &c) {
        float s = std::max(parameter(p, 0, 0.007f), 0.00001f); float blobs = parameter(p, 1, 6.0f); float softness = clamp01(parameter(p, 2, 0.24f)); n.warp(x, y, s, parameter(p, 3, 32.0f));
        float glow = clamp01(parameter(p, 4, 0.58f)); float amount = parameter(p, 5, 1.3f); float a = std::sin(x * s * blobs + n.fbm(x * s, y * s) * 7.0f); float b = std::sin(y * s * blobs * 0.83f - n.fine(x * s, y * s) * 6.0f);
        float field = contrast(clamp01(0.5f + (a + b) * 0.24f), amount); float mask = smoothstep(0.52f - softness * 0.3f, 0.52f + softness * 0.3f, field); uint32_t blob = mixColor(color(c, 1, 0xffff3d81u), color(c, 2, 0xffff8a24u), field);
        blob = mixColor(blob, color(c, 3, 0xffffd45cu), smoothstep(0.74f, 0.98f, field) * glow); return mixColor(color(c, 0, 0xff1c0929u), blob, mask);
    }

    uint32_t eventHorizon(NoiseBank &n, float x, float y, int width, int height, int seed, const std::vector<float> &p, const std::vector<uint32_t> &c) {
        float s = std::max(parameter(p, 0, 0.008f), 0.00001f);
        float tilt = clamp01(parameter(p, 1, 0.72f));
        float horizon = std::clamp(parameter(p, 2, 0.15f), 0.04f, 0.35f);
        float diskWidth = std::max(parameter(p, 3, 0.065f), 0.005f);
        float lensing = clamp01(parameter(p, 4, 0.82f));
        float stars = clamp01(parameter(p, 5, 0.22f));
        float unit = static_cast<float>(std::max(std::min(width, height), 1));
        float dx = (x - width * 0.5f) / unit;
        float dy = (y - height * 0.5f) / unit;
        float radius = std::sqrt(dx * dx + dy * dy);
        float flatten = 0.42f - tilt * 0.34f;
        float diskRadius = std::sqrt(dx * dx + (dy / std::max(flatten, 0.04f)) * (dy / std::max(flatten, 0.04f)));
        float grain = n.fbm(x * s, y * s);
        float inner = horizon * 1.18f;
        float outer = horizon * 2.75f;
        float disk = smoothstep(inner, inner + diskWidth, diskRadius) * (1.0f - smoothstep(outer - diskWidth, outer, diskRadius));
        disk *= 0.62f + grain * 0.38f;
        float hot = disk * smoothstep(outer, inner, diskRadius);
        float photonRing = (1.0f - smoothstep(horizon * 0.045f, horizon * 0.18f, std::abs(radius - horizon * 1.08f))) * lensing;
        float upperLens = photonRing * smoothstep(-horizon * 0.25f, -horizon, dy);
        uint32_t result = mixColor(color(c, 0, 0xff010107u), color(c, 3, 0xff7c48ffu), upperLens * 0.7f);
        result = mixColor(result, color(c, 1, 0xffff5a18u), disk);
        result = mixColor(result, color(c, 2, 0xffffe0a3u), hot * (0.45f + 0.55f * smoothstep(0.0f, outer, dx)));
        result = mixColor(result, color(c, 2, 0xffffe0a3u), photonRing);
        if (radius < horizon) result = mixColor(color(c, 0, 0xff010107u), 0xff000000u, 0.94f);
        float star = smoothstep(1.0f - stars * 0.008f, 1.0f, hash(x, y, seed));
        star *= smoothstep(horizon * 3.2f, horizon * 4.0f, radius);
        return mixColor(result, 0xffffffffu, star);
    }

    uint32_t fractalBloom(NoiseBank &n, float x, float y, int width, int height, const std::vector<float> &p, const std::vector<uint32_t> &c) {
        float s = std::max(parameter(p, 0, 0.01f), 0.00001f);
        float petals = std::max(parameter(p, 1, 7.0f), 2.0f);
        float layers = std::max(parameter(p, 2, 5.0f), 1.0f);
        float curl = parameter(p, 3, 4.2f);
        float filigree = clamp01(parameter(p, 4, 0.7f));
        float glow = clamp01(parameter(p, 5, 0.72f));
        float unit = static_cast<float>(std::max(std::min(width, height), 1));
        float dx = (x - width * 0.5f) / unit;
        float dy = (y - height * 0.5f) / unit;
        float radius = std::sqrt(dx * dx + dy * dy);
        float angle = std::atan2(dy, dx);
        float organic = n.fbm(x * s, y * s) - 0.5f;
        float petalWave = 0.5f + 0.5f * std::cos(angle * petals + radius * curl * 10.0f + organic * 1.2f);
        float boundary = 0.18f + 0.19f * std::pow(petalWave, 0.62f);
        float body = smoothstep(boundary + 0.018f, boundary - 0.018f, radius);
        float layerWave = 0.5f + 0.5f * std::cos(radius * layers * 42.0f - angle * 1.7f + petalWave * 2.5f);
        float veins = std::pow(clamp01(1.0f - std::abs(layerWave * 2.0f - 1.0f)), 9.0f) * filigree * body;
        float edge = (1.0f - smoothstep(0.0f, 0.026f, std::abs(radius - boundary))) * body;
        uint32_t bloom = mixColor(color(c, 1, 0xff5940d6u), color(c, 2, 0xffff3e93u), smoothstep(boundary, 0.04f, radius));
        uint32_t result = mixColor(color(c, 0, 0xff09061bu), bloom, body);
        result = mixColor(result, color(c, 3, 0xffffe88au), veins * glow);
        result = mixColor(result, color(c, 3, 0xffffe88au), edge * 0.55f);
        return mixColor(result, color(c, 3, 0xffffe88au), smoothstep(0.075f, 0.0f, radius) * glow);
    }

    uint32_t chromaticTunnel(NoiseBank &n, float x, float y, int width, int height, const std::vector<float> &p, const std::vector<uint32_t> &c) {
        float s = std::max(parameter(p, 0, 0.009f), 0.00001f);
        float depth = std::max(parameter(p, 1, 18.0f), 1.0f);
        float twist = parameter(p, 2, 5.5f);
        float facets = std::max(parameter(p, 3, 7.0f), 2.0f);
        float curvature = clamp01(parameter(p, 4, 0.48f));
        float glow = clamp01(parameter(p, 5, 0.78f));
        float unit = static_cast<float>(std::max(std::min(width, height), 1));
        float bend = (n.fbm(x * s * 0.35f, y * s * 0.35f) - 0.5f) * curvature * 0.08f;
        float dx = (x - width * 0.5f) / unit + bend;
        float dy = (y - height * 0.5f) / unit - bend * 0.55f;
        float radius = std::sqrt(dx * dx + dy * dy);
        float angle = std::atan2(dy, dx);
        float perspective = 1.0f / (radius + 0.045f);
        float spiral = angle * twist + perspective * depth;
        float pulse = 0.5f + 0.5f * std::sin(spiral);
        float facet = 0.5f + 0.5f * std::cos(angle * facets - std::log(radius + 0.025f) * twist);
        float ribbon = std::pow(clamp01(1.0f - std::abs(pulse * 2.0f - 1.0f)), 5.0f);
        uint32_t spectrum = mixColor(color(c, 1, 0xff00d5ffu), color(c, 2, 0xffff2c9cu), facet);
        uint32_t result = mixColor(color(c, 0, 0xff030516u), spectrum, smoothstep(0.02f, 0.48f, radius) * (0.32f + ribbon * 0.68f));
        result = mixColor(result, color(c, 3, 0xfffff1b8u), ribbon * glow * smoothstep(0.5f, 0.06f, radius));
        return mixColor(result, color(c, 0, 0xff030516u), smoothstep(0.05f, 0.0f, radius));
    }

    uint32_t eclipseCorona(NoiseBank &n, float x, float y, int width, int height, const std::vector<float> &p, const std::vector<uint32_t> &c) {
        float s = std::max(parameter(p, 0, 0.008f), 0.00001f);
        float moon = std::clamp(parameter(p, 1, 0.23f), 0.06f, 0.4f);
        float coronaSize = std::max(parameter(p, 2, 0.2f), 0.02f);
        float rays = std::max(parameter(p, 3, 34.0f), 2.0f);
        float turbulence = clamp01(parameter(p, 4, 0.62f));
        float diamondRing = clamp01(parameter(p, 5, 0.8f));
        float unit = static_cast<float>(std::max(std::min(width, height), 1));
        float dx = (x - width * 0.5f) / unit;
        float dy = (y - height * 0.5f) / unit;
        float radius = std::sqrt(dx * dx + dy * dy);
        float angle = std::atan2(dy, dx);
        float noise = n.fbm(x * s, y * s);
        float rayShape = 0.38f + 0.62f * std::pow(clamp01(0.5f + 0.5f * std::sin(angle * rays + noise * turbulence * 7.0f)), 3.0f);
        float outer = moon + coronaSize * rayShape;
        float corona = smoothstep(outer, moon * 1.01f, radius) * smoothstep(moon * 0.94f, moon * 1.04f, radius);
        float halo = smoothstep(moon + coronaSize * 1.8f, moon, radius) * 0.32f;
        float rim = 1.0f - smoothstep(moon * 0.015f, moon * 0.06f, std::abs(radius - moon));
        float pointAngle = -0.72f;
        float px = std::cos(pointAngle) * moon;
        float py = std::sin(pointAngle) * moon;
        float pointDistance = std::sqrt((dx - px) * (dx - px) + (dy - py) * (dy - py));
        float diamond = (1.0f - smoothstep(0.004f, 0.035f, pointDistance)) * diamondRing;
        uint32_t result = mixColor(color(c, 0, 0xff02030au), color(c, 1, 0xff765bffu), halo);
        result = mixColor(result, color(c, 2, 0xffff9b45u), corona);
        result = mixColor(result, color(c, 3, 0xffffffffu), rim * 0.82f + diamond);
        if (radius < moon * 0.985f) result = mixColor(color(c, 0, 0xff02030au), 0xff000000u, 0.88f);
        return result;
    }

    uint32_t strangeAttractor(NoiseBank &n, float x, float y, int width, int height, const std::vector<float> &p, const std::vector<uint32_t> &c) {
        float s = std::max(parameter(p, 0, 0.01f), 0.00001f);
        float lobes = std::max(parameter(p, 1, 3.0f), 1.0f);
        float density = std::clamp(parameter(p, 2, 18.0f), 4.0f, 40.0f);
        float curvature = parameter(p, 3, 6.0f);
        float thickness = std::max(parameter(p, 4, 0.035f), 0.002f);
        float glow = clamp01(parameter(p, 5, 0.8f));
        float unit = static_cast<float>(std::max(std::min(width, height), 1));
        float dx = (x - width * 0.5f) / unit;
        float dy = (y - height * 0.5f) / unit;
        float minimumDistance = 10.0f;
        int samples = static_cast<int>(64.0f + density * 3.0f);
        float phase = (n.fbm(x * s * 0.2f, y * s * 0.2f) - 0.5f) * 0.12f;
        for (int i = 0; i < samples; ++i) {
            float t = static_cast<float>(i) / static_cast<float>(samples - 1) * 2.0f * PI;
            float orbitX = std::sin(t * lobes + std::sin(t * curvature) * 0.72f) * 0.31f;
            float orbitY = std::sin(t * (lobes + 1.0f) + std::cos(t * curvature * 0.73f) * 0.58f) * 0.31f;
            orbitX += std::sin(t * 2.0f) * phase;
            orbitY += std::cos(t * 3.0f) * phase;
            float distance = std::sqrt((dx - orbitX) * (dx - orbitX) + (dy - orbitY) * (dy - orbitY));
            minimumDistance = std::min(minimumDistance, distance);
        }
        float line = 1.0f - smoothstep(thickness * 0.22f, thickness, minimumDistance);
        float halo = 1.0f - smoothstep(thickness, thickness * 4.5f, minimumDistance);
        float tone = clamp01(0.5f + dx * 1.8f + dy * 0.6f);
        uint32_t orbit = mixColor(color(c, 1, 0xff14d9c5u), color(c, 2, 0xffff3a79u), tone);
        uint32_t result = mixColor(color(c, 0, 0xff030511u), orbit, halo * glow * 0.62f);
        result = mixColor(result, orbit, line);
        return mixColor(result, color(c, 3, 0xfffff1b5u), line * line * glow);
    }

    uint32_t ferrofluidCrown(NoiseBank &n, float x, float y, int width, int height, const std::vector<float> &p, const std::vector<uint32_t> &c) {
        float s = std::max(parameter(p, 0, 0.012f), 0.00001f);
        float spikes = std::max(parameter(p, 1, 19.0f), 3.0f);
        float spikeLength = std::clamp(parameter(p, 2, 0.14f), 0.0f, 0.3f);
        float bodySize = std::clamp(parameter(p, 3, 0.22f), 0.05f, 0.4f);
        float metallic = clamp01(parameter(p, 4, 0.86f));
        float distortion = clamp01(parameter(p, 5, 0.42f));
        float unit = static_cast<float>(std::max(std::min(width, height), 1));
        float dx = (x - width * 0.5f) / unit;
        float dy = (y - height * 0.52f) / unit;
        float radius = std::sqrt(dx * dx + dy * dy);
        float angle = std::atan2(dy, dx);
        float noise = n.fbm(x * s, y * s) - 0.5f;
        float spikeWave = std::max(std::cos(angle * spikes + noise * distortion * 5.0f), 0.0f);
        float boundary = bodySize + std::pow(spikeWave, 9.0f) * spikeLength * (0.72f + noise * 0.35f);
        float body = smoothstep(boundary + 0.008f, boundary - 0.008f, radius);
        float shadowRadius = std::sqrt((dx + 0.025f) * (dx + 0.025f) + (dy - 0.035f) * (dy - 0.035f));
        float shadow = smoothstep(boundary + 0.09f, boundary, shadowRadius) * (1.0f - body) * 0.34f;
        float normalLight = clamp01(0.45f - dx * 1.15f - dy * 0.92f + noise * 0.18f);
        float specular = std::pow(normalLight, 9.0f) * metallic;
        uint32_t result = mixColor(color(c, 0, 0xffe8e1d4u), color(c, 1, 0xff07090cu), shadow);
        uint32_t metal = mixColor(color(c, 1, 0xff07090cu), color(c, 2, 0xff353d43u), normalLight);
        metal = mixColor(metal, color(c, 3, 0xffd9f7ffu), specular);
        result = mixColor(result, metal, body);
        float rim = (1.0f - smoothstep(0.0f, 0.012f, std::abs(radius - boundary))) * body;
        return mixColor(result, color(c, 3, 0xffd9f7ffu), rim * metallic * 0.55f);
    }

    uint32_t supernova(NoiseBank &n, float x, float y, int width, int height, int seed, const std::vector<float> &p, const std::vector<uint32_t> &c) {
        float s = std::max(parameter(p, 0, 0.01f), 0.00001f);
        float shockRadius = std::clamp(parameter(p, 1, 0.27f), 0.08f, 0.45f);
        float shellWidth = std::max(parameter(p, 2, 0.075f), 0.008f);
        float ejecta = clamp01(parameter(p, 3, 0.72f));
        float turbulence = clamp01(parameter(p, 4, 0.68f));
        float stars = clamp01(parameter(p, 5, 0.24f));
        float unit = static_cast<float>(std::max(std::min(width, height), 1));
        float dx = (x - width * 0.5f) / unit;
        float dy = (y - height * 0.5f) / unit;
        float radius = std::sqrt(dx * dx + dy * dy);
        float angle = std::atan2(dy, dx);
        float noise = n.fbm(x * s, y * s);
        float fine = n.fine(x * s * 2.7f, y * s * 2.7f);
        float distortedRadius = radius + (noise - 0.5f) * shellWidth * turbulence * 1.8f;
        float shell = 1.0f - smoothstep(shellWidth * 0.2f, shellWidth, std::abs(distortedRadius - shockRadius));
        float rays = std::pow(clamp01(0.5f + 0.5f * std::sin(angle * 37.0f + fine * 8.0f)), 5.0f);
        float radialEjecta = rays * smoothstep(shockRadius + shellWidth * 2.8f, shockRadius * 0.35f, radius) * ejecta;
        float cloud = smoothstep(shockRadius + shellWidth * 2.1f, shockRadius * 0.18f, distortedRadius) * (0.28f + noise * 0.55f);
        uint32_t result = mixColor(color(c, 0, 0xff02030bu), color(c, 1, 0xff6923b7u), cloud);
        result = mixColor(result, color(c, 2, 0xffff4d22u), shell * (0.62f + fine * 0.38f) + radialEjecta * 0.54f);
        float core = smoothstep(shockRadius * 0.22f, 0.0f, radius);
        result = mixColor(result, color(c, 3, 0xfffff2c5u), core + shell * shell * 0.72f);
        float star = smoothstep(1.0f - stars * 0.008f, 1.0f, hash(x, y, seed));
        star *= smoothstep(shockRadius * 1.7f, shockRadius * 2.2f, radius);
        return mixColor(result, 0xffffffffu, star);
    }

    uint32_t iris(NoiseBank &n, float x, float y, int width, int height, const std::vector<float> &p, const std::vector<uint32_t> &c) {
        float s = std::max(parameter(p, 0, 0.014f), 0.00001f);
        float pupil = std::clamp(parameter(p, 1, 0.12f), 0.03f, 0.28f);
        float irisSize = std::clamp(parameter(p, 2, 0.38f), pupil + 0.04f, 0.48f);
        float fibers = std::max(parameter(p, 3, 46.0f), 6.0f);
        float variation = clamp01(parameter(p, 4, 0.72f));
        float catchlight = clamp01(parameter(p, 5, 0.82f));
        float unit = static_cast<float>(std::max(std::min(width, height), 1));
        float dx = (x - width * 0.5f) / unit;
        float dy = (y - height * 0.5f) / unit;
        float radius = std::sqrt(dx * dx + dy * dy);
        float angle = std::atan2(dy, dx);
        float noise = n.fbm(x * s, y * s);
        float angular = 0.5f + 0.5f * std::sin(angle * fibers + noise * 8.0f + radius * 65.0f);
        float radialFibers = std::pow(clamp01(angular), 3.0f) * smoothstep(pupil, irisSize, radius);
        float innerGold = smoothstep(pupil + 0.11f, pupil, radius) * smoothstep(pupil * 0.92f, pupil * 1.08f, radius);
        float irisMask = smoothstep(irisSize + 0.008f, irisSize - 0.008f, radius);
        uint32_t irisColor = mixColor(color(c, 1, 0xff123646u), color(c, 2, 0xff37b6a5u), radialFibers * variation + noise * 0.18f);
        irisColor = mixColor(irisColor, color(c, 3, 0xffe6b650u), innerGold);
        uint32_t result = mixColor(color(c, 0, 0xff080a0du), irisColor, irisMask);
        float limbalRing = (1.0f - smoothstep(0.0f, 0.026f, std::abs(radius - irisSize))) * irisMask;
        result = mixColor(result, color(c, 0, 0xff080a0du), limbalRing * 0.86f);
        if (radius < pupil) result = 0xff010103u;
        float lightDx = dx + irisSize * 0.34f;
        float lightDy = dy + irisSize * 0.38f;
        float reflection = (1.0f - smoothstep(irisSize * 0.035f, irisSize * 0.13f, std::sqrt(lightDx * lightDx + lightDy * lightDy))) * catchlight;
        return mixColor(result, 0xffffffffu, reflection);
    }

    uint32_t peacockFeather(NoiseBank &n, float x, float y, int width, int height, const std::vector<float> &p, const std::vector<uint32_t> &c) {
        float s = std::max(parameter(p, 0, 0.012f), 0.00001f);
        float eyeSize = std::clamp(parameter(p, 1, 0.24f), 0.08f, 0.4f);
        float density = std::max(parameter(p, 2, 54.0f), 8.0f);
        float curvature = clamp01(parameter(p, 3, 0.58f));
        float iridescence = clamp01(parameter(p, 4, 0.8f));
        float softness = clamp01(parameter(p, 5, 0.42f));
        float unit = static_cast<float>(std::max(std::min(width, height), 1));
        float dx = (x - width * 0.5f) / unit;
        float dy = (y - height * 0.43f) / unit;
        float curvedX = dx + dy * dy * curvature * 0.48f;
        float featherRadius = std::sqrt((curvedX / 0.34f) * (curvedX / 0.34f) + (dy / 0.46f) * (dy / 0.46f));
        float featherMask = smoothstep(1.05f, 0.86f - softness * 0.08f, featherRadius);
        float eyeX = curvedX / eyeSize;
        float eyeY = dy / (eyeSize * 1.22f);
        float eyeRadius = std::sqrt(eyeX * eyeX + eyeY * eyeY);
        float noise = n.fbm(x * s, y * s);
        float barbPhase = std::abs(curvedX) * density + dy * density * 0.16f + noise * 3.0f;
        float barbs = std::pow(clamp01(0.5f + 0.5f * std::sin(barbPhase)), 5.0f) * featherMask;
        uint32_t result = mixColor(color(c, 0, 0xff06150fu), color(c, 1, 0xff167d55u), featherMask * (0.58f + barbs * 0.42f));
        float goldRing = (1.0f - smoothstep(0.08f, 0.18f, std::abs(eyeRadius - 0.83f))) * featherMask;
        float blueEye = smoothstep(0.72f, 0.56f, eyeRadius) * featherMask;
        float darkEye = smoothstep(0.38f, 0.18f, eyeRadius) * featherMask;
        result = mixColor(result, color(c, 3, 0xffe4bc45u), goldRing * iridescence);
        result = mixColor(result, color(c, 2, 0xff1248bcu), blueEye * iridescence);
        result = mixColor(result, color(c, 0, 0xff06150fu), darkEye * 0.88f);
        float shaft = (1.0f - smoothstep(0.003f, 0.012f, std::abs(curvedX))) * smoothstep(0.18f, 0.55f, dy + 0.36f);
        return mixColor(result, color(c, 3, 0xffe4bc45u), shaft * 0.62f);
    }

    uint32_t nautilusShell(NoiseBank &n, float x, float y, int width, int height, const std::vector<float> &p, const std::vector<uint32_t> &c) {
        float s = std::max(parameter(p, 0, 0.011f), 0.00001f);
        float turns = std::max(parameter(p, 1, 3.4f), 0.5f);
        float chambers = std::max(parameter(p, 2, 19.0f), 3.0f);
        float opening = std::clamp(parameter(p, 3, 0.13f), 0.03f, 0.3f);
        float ridges = clamp01(parameter(p, 4, 0.68f));
        float pearl = clamp01(parameter(p, 5, 0.46f));
        float unit = static_cast<float>(std::max(std::min(width, height), 1));
        float dx = (x - width * 0.5f) / unit;
        float dy = (y - height * 0.5f) / unit;
        float shellRadius = std::sqrt((dx / 0.43f) * (dx / 0.43f) + (dy / 0.36f) * (dy / 0.36f));
        float radius = std::sqrt(dx * dx + dy * dy);
        float angle = std::atan2(dy, dx);
        float mask = smoothstep(1.02f, 0.96f, shellRadius);
        float noise = n.fbm(x * s, y * s);
        float spiralPhase = angle - std::log(radius + 0.018f) * turns * 2.0f;
        float spiral = std::pow(clamp01(1.0f - std::abs(std::sin(spiralPhase))), 8.0f);
        float chamberLine = std::pow(clamp01(1.0f - std::abs(std::sin(angle * chambers * 0.5f + std::log(radius + 0.025f) * 3.0f))), 12.0f);
        float relief = clamp01(spiral * 0.8f + chamberLine * ridges * 0.65f);
        uint32_t shell = mixColor(color(c, 1, 0xff4a2d22u), color(c, 2, 0xffd9ad79u), clamp01(0.35f + noise * 0.42f + (0.5f - dy) * 0.28f));
        shell = mixColor(shell, color(c, 3, 0xfffff0d1u), relief * pearl);
        uint32_t result = mixColor(color(c, 0, 0xff152228u), shell, mask);
        float mouthX = dx - 0.25f;
        float mouthY = dy + 0.02f;
        float mouth = 1.0f - smoothstep(opening * 0.45f, opening, std::sqrt(mouthX * mouthX + mouthY * mouthY));
        result = mixColor(result, color(c, 1, 0xff4a2d22u), mouth * mask * 0.72f);
        float rim = (1.0f - smoothstep(0.0f, 0.025f, std::abs(shellRadius - 0.98f))) * mask;
        return mixColor(result, color(c, 3, 0xfffff0d1u), rim * 0.54f);
    }

    uint32_t ringedPlanet(NoiseBank &n, float x, float y, int width, int height, int seed, const std::vector<float> &p, const std::vector<uint32_t> &c) {
        float s = std::max(parameter(p, 0, 0.009f), 0.00001f);
        float planetSize = std::clamp(parameter(p, 1, 0.24f), 0.08f, 0.38f);
        float tilt = clamp01(parameter(p, 2, 0.72f));
        float ringWidth = std::clamp(parameter(p, 3, 0.16f), 0.03f, 0.3f);
        float atmosphere = clamp01(parameter(p, 4, 0.62f));
        float stars = clamp01(parameter(p, 5, 0.25f));
        float unit = static_cast<float>(std::max(std::min(width, height), 1));
        float dx = (x - width * 0.5f) / unit;
        float dy = (y - height * 0.5f) / unit;
        float radius = std::sqrt(dx * dx + dy * dy);
        float flatten = 0.5f - tilt * 0.38f;
        float ringRadius = std::sqrt(dx * dx + (dy / std::max(flatten, 0.06f)) * (dy / std::max(flatten, 0.06f)));
        float ringInner = planetSize * 1.15f;
        float ringOuter = ringInner + ringWidth;
        float ring = smoothstep(ringInner, ringInner + 0.012f, ringRadius) * (1.0f - smoothstep(ringOuter - 0.012f, ringOuter, ringRadius));
        float ringBands = 0.5f + 0.5f * std::sin(ringRadius * 260.0f + n.fine(x * s, y * s) * 2.0f);
        float star = smoothstep(1.0f - stars * 0.008f, 1.0f, hash(x, y, seed));
        uint32_t result = mixColor(color(c, 0, 0xff02040du), 0xffffffffu, star);
        uint32_t rings = mixColor(color(c, 1, 0xff15234du), color(c, 3, 0xffffe1a6u), 0.46f + ringBands * 0.45f);
        result = mixColor(result, rings, ring * 0.68f);
        float planetMask = smoothstep(planetSize + 0.006f, planetSize - 0.006f, radius);
        float light = clamp01(0.52f - dx * 1.55f - dy * 0.75f);
        float bands = 0.5f + 0.5f * std::sin(dy / planetSize * 23.0f + n.fbm(x * s, y * s) * 2.8f);
        uint32_t globe = mixColor(color(c, 1, 0xff15234du), color(c, 2, 0xffe58c62u), light * (0.72f + bands * 0.28f));
        result = mixColor(result, globe, planetMask);
        result = mixColor(result, rings, ring * smoothstep(-0.01f, 0.02f, dy));
        float atmosphereRim = (1.0f - smoothstep(0.0f, planetSize * 0.06f, std::abs(radius - planetSize))) * atmosphere;
        return mixColor(result, color(c, 3, 0xffffe1a6u), atmosphereRim * 0.48f);
    }

    uint32_t palette4(
            const std::vector<uint32_t> &colors,
            float value
    ) {
        float t = clamp01(value) * 3.0f;
        if (t < 1.0f) return mixColor(color(colors, 0, 0xff050611u), color(colors, 1, 0xff294a8au), t);
        if (t < 2.0f) return mixColor(color(colors, 1, 0xff294a8au), color(colors, 2, 0xffd94b91u), t - 1.0f);
        return mixColor(color(colors, 2, 0xffd94b91u), color(colors, 3, 0xffffe8a3u), t - 2.0f);
    }

    uint32_t showcaseTexture(
            int textureType,
            NoiseBank &n,
            float x,
            float y,
            int seed,
            int width,
            int height,
            const std::vector<float> &p,
            const std::vector<uint32_t> &c
    ) {
        float s = std::max(parameter(p, 0, 0.009f), 0.00001f);
        float complexity = clamp01(parameter(p, 1, 0.62f));
        float distortion = clamp01(parameter(p, 2, 0.48f));
        float sharpness = clamp01(parameter(p, 3, 0.58f));
        float glow = clamp01(parameter(p, 4, 0.52f));
        float contrastAmount = std::max(parameter(p, 5, 1.25f), 0.05f);
        float unit = static_cast<float>(std::min(width, height));
        float u = (x - width * 0.5f) / unit;
        float v = (y - height * 0.5f) / unit;
        float spatialScale = std::clamp(s / 0.008f, 0.125f, 5.0f);
        u *= spatialScale;
        v *= spatialScale;
        float nx = x;
        float ny = y;
        n.warp(nx, ny, s * 0.7f, distortion * 54.0f);
        float base = n.fbm(nx * s, ny * s);
        float fine = n.fine(nx * s * 4.1f, ny * s * 4.1f);
        float result = base;
        float accent = 0.0f;

        switch (textureType) {
            case 51: {
                float radius = std::sqrt(u * u + v * v) + (base - 0.5f) * distortion * 0.16f;
                float bands = 0.5f + 0.5f * std::sin(radius * (55.0f + complexity * 95.0f) + fine * 8.0f);
                float cavity = 1.0f - smoothstep(0.05f, 0.2f, radius);
                float crystals = std::pow(clamp01(0.5f + 0.5f * std::sin(std::atan2(v, u) * (10.0f + complexity * 24.0f) + radius * 180.0f)), 5.0f);
                result = contrast(bands * (1.0f - cavity * 0.55f) + cavity * crystals, contrastAmount);
                accent = cavity * crystals * glow;
                break;
            }
            case 52: {
                float angle = std::atan2(v, u);
                float radius = std::sqrt(u * u + v * v);
                float facets = std::abs(fract((angle / (2.0f * PI) + 0.5f) * (5.0f + complexity * 13.0f)) - 0.5f);
                float beam = std::abs(std::sin((u * 0.84f + v * 0.54f) * (42.0f + complexity * 86.0f) + facets * 9.0f));
                float spectrum = fract(angle / (2.0f * PI) + radius * (2.0f + distortion * 5.0f) + facets);
                accent = (1.0f - smoothstep(0.01f, 0.08f + (1.0f - sharpness) * 0.12f, beam)) * glow;
                return mixColor(palette4(c, spectrum), color(c, 3, 0xffffe47au), accent);
                break;
            }
            case 53: {
                float edge = n.cellEdge(nx, ny, s * (1.3f + complexity * 3.2f), 1.0f);
                float lead = 1.0f - smoothstep(0.035f, 0.095f + (1.0f - sharpness) * 0.08f, edge);
                float glass = n.cellVariation(nx, ny, s * (1.3f + complexity * 3.2f), 1.0f);
                uint32_t pane = palette4(c, fract(glass * 2.8f + fine * 0.22f));
                pane = mixColor(pane, color(c, 3, 0xffffd34eu), fine * glow * 0.3f);
                return mixColor(pane, color(c, 0, 0xff120f18u), lead);
            }
            case 54: {
                float depth = clamp01(v + 0.5f);
                float stems = std::abs(std::sin((u + std::sin(v * 8.0f + base * 3.0f) * distortion * 0.12f) * (24.0f + complexity * 46.0f)));
                float blades = 1.0f - smoothstep(0.015f, 0.07f + (1.0f - sharpness) * 0.12f, stems);
                float rays = std::pow(clamp01(0.5f + 0.5f * std::sin((u + v * 0.24f) * 18.0f + fine * 2.0f)), 7.0f);
                result = clamp01(base * 0.28f + blades * (0.45f + depth * 0.35f) + rays * (1.0f - depth) * glow);
                accent = rays * (1.0f - depth) * glow;
                break;
            }
            case 55: {
                float angle = std::atan2(v, u);
                float radius = std::sqrt(u * u + v * v);
                float branches = std::abs(std::sin(angle * (5.0f + complexity * 13.0f) + radius * 68.0f + base * distortion * 7.0f));
                float needles = std::abs(std::sin((u * 0.7f - v) * (170.0f + complexity * 260.0f)));
                accent = (1.0f - smoothstep(0.02f, 0.14f, branches)) * (0.55f + 0.45f * (1.0f - needles));
                result = clamp01(base * 0.45f + accent * (0.7f + glow * 0.35f));
                break;
            }
            case 56: {
                float angle = std::atan2(v, u);
                float phase = base * 14.0f + angle * (4.0f + complexity * 10.0f) + (u - v) * 28.0f;
                result = fract(phase / (2.0f * PI));
                accent = std::pow(0.5f + 0.5f * std::sin(phase * 1.73f), 5.0f) * glow;
                break;
            }
            case 57: {
                float cellsX = x * s * (7.0f + complexity * 12.0f);
                float cellsY = y * s * (5.0f + complexity * 9.0f);
                float row = std::floor(cellsY);
                float px = fract(cellsX + (static_cast<int>(row) & 1) * 0.5f) - 0.5f;
                float py = fract(cellsY) - 0.32f;
                float scaleShape = std::sqrt(px * px + py * py * 0.65f);
                float rim = 1.0f - smoothstep(0.32f, 0.42f + (1.0f - sharpness) * 0.09f, scaleShape);
                result = clamp01(rim * (0.34f + 0.66f * (0.5f - py)) + fine * 0.2f);
                accent = rim * smoothstep(0.19f, -0.1f, py) * glow;
                break;
            }
            case 58: {
                float density = 7.0f + complexity * 15.0f;
                float gx = (u + 0.5f) * density;
                float gy = (v + 0.5f) * density;
                float nearest = 10.0f;
                float trail = 10.0f;
                int cellX = static_cast<int>(std::floor(gx));
                int cellY = static_cast<int>(std::floor(gy));
                for (int oy = -1; oy <= 1; ++oy) {
                    for (int ox = -1; ox <= 1; ++ox) {
                        int cx = cellX + ox;
                        int cy = cellY + oy;
                        float px = cx + hash(static_cast<float>(cx), static_cast<float>(cy), seed) * 0.76f + 0.12f;
                        float py = cy + hash(static_cast<float>(cy), static_cast<float>(cx), seed + 31) * 0.76f + 0.12f;
                        float dx = gx - px;
                        float dy = gy - py;
                        nearest = std::min(nearest, std::sqrt(dx * dx + dy * dy));
                        trail = std::min(trail, std::sqrt((dx + dy * distortion * 0.8f) * (dx + dy * distortion * 0.8f) + dy * dy * 0.08f));
                    }
                }
                float spark = 1.0f - smoothstep(0.015f, 0.10f + (1.0f - sharpness) * 0.12f, nearest);
                float tail = 1.0f - smoothstep(0.03f, 0.18f, trail);
                result = clamp01(base * 0.18f + tail * 0.34f + spark);
                accent = spark * glow;
                break;
            }
            case 59: {
                float edge1 = n.cellEdge(nx, ny, s * (2.0f + complexity * 3.0f), 1.2f);
                float edge2 = n.secondaryCellEdge(nx + 37.0f, ny - 19.0f, s * (4.0f + complexity * 5.0f), 1.4f);
                accent = (1.0f - smoothstep(0.018f, 0.11f, edge1)) * 0.7f + (1.0f - smoothstep(0.012f, 0.08f, edge2)) * 0.5f;
                result = clamp01(base * 0.3f + accent * (0.65f + glow * 0.35f));
                break;
            }
            case 60: {
                float edge = n.cellEdge(nx, ny, s * (1.3f + complexity * 3.7f), 1.3f);
                float crack = 1.0f - smoothstep(0.012f, 0.065f + (1.0f - sharpness) * 0.07f, edge);
                uint32_t ceramic = mixColor(color(c, 0, 0xff10131au), color(c, 1, 0xff344158u), base * 0.7f);
                uint32_t gold = mixColor(color(c, 2, 0xffd99b32u), color(c, 3, 0xffffe7a3u), fine * glow);
                return mixColor(ceramic, gold, crack);
            }
            case 61: {
                float frequency = 0.08f + s * 8.0f + complexity * 0.11f;
                float ax = std::sin((x + y * 0.48f) * frequency * PI);
                float ay = std::sin((y - x * 0.48f) * frequency * PI);
                float weave = ax * ay;
                result = clamp01(0.38f + weave * 0.3f + fine * 0.22f);
                accent = std::pow(clamp01(weave), 4.0f) * glow;
                break;
            }
            case 62: {
                float grid = 12.0f + complexity * 32.0f;
                float gx = fract(x * s * grid);
                float gy = fract(y * s * grid);
                float trace = std::min(std::abs(gx - 0.5f), std::abs(gy - 0.5f));
                float path = 1.0f - smoothstep(0.025f, 0.08f + (1.0f - sharpness) * 0.06f, trace);
                float via = 1.0f - smoothstep(0.08f, 0.17f, std::sqrt((gx - 0.5f) * (gx - 0.5f) + (gy - 0.5f) * (gy - 0.5f)));
                result = clamp01(base * 0.3f + path * 0.48f + via * 0.5f);
                accent = via * glow;
                break;
            }
            case 63: {
                float film = base * (8.0f + complexity * 18.0f) + fine * distortion * 5.0f + (u * u - v * v) * 18.0f;
                result = fract(film);
                accent = std::pow(0.5f + 0.5f * std::sin(film * PI * 2.0f), 6.0f) * glow;
                break;
            }
            case 64: {
                float r1 = std::sqrt((u - 0.18f) * (u - 0.18f) + v * v);
                float r2 = std::sqrt((u + 0.18f) * (u + 0.18f) + v * v);
                float pattern = std::sin(r1 * (120.0f + complexity * 260.0f)) * std::sin(r2 * (124.0f + complexity * 252.0f));
                result = contrast(pattern * 0.5f + 0.5f, contrastAmount);
                accent = std::pow(result, 7.0f) * glow;
                break;
            }
            case 65: {
                float edge = n.cellEdge(nx, ny, s * (2.5f + complexity * 4.0f), 0.78f);
                float inside = n.cellVariation(nx, ny, s * (2.5f + complexity * 4.0f), 0.78f);
                float rim = 1.0f - smoothstep(0.025f, 0.11f, edge);
                result = clamp01(inside * 0.78f + (1.0f - rim) * fine * 0.2f);
                accent = rim * glow;
                break;
            }
            case 66: {
                float cells = n.cellVariation(nx, ny, s * (4.0f + complexity * 8.0f), 1.7f);
                float edge = n.cellEdge(nx, ny, s * (4.0f + complexity * 8.0f), 1.7f);
                float chips = smoothstep(0.68f - complexity * 0.22f, 0.9f, cells) * smoothstep(0.02f, 0.13f, edge);
                if (chips > 0.05f) return palette4(c, fract(cells * 3.7f + fine * 0.2f));
                return mixColor(color(c, 0, 0xffe5dccfu), color(c, 3, 0xffe5a73du), fine * 0.08f);
            }
            case 67: {
                float e1 = 1.0f - smoothstep(0.015f, 0.13f, n.cellEdge(nx, ny, s * (1.2f + complexity * 2.0f), 1.4f));
                float e2 = 1.0f - smoothstep(0.01f, 0.08f, n.secondaryCellEdge(nx, ny, s * (3.2f + complexity * 4.0f), 1.5f));
                float stars = smoothstep(0.998f - complexity * 0.006f, 1.0f, hash(x, y, seed));
                result = clamp01(e1 * 0.72f + e2 * 0.38f + stars);
                accent = stars + e1 * e2 * glow;
                break;
            }
            case 68: {
                float facets = n.cellVariation(nx, ny, s * (1.8f + complexity * 4.0f), 1.2f);
                float edge = 1.0f - smoothstep(0.02f, 0.12f, n.cellEdge(nx, ny, s * (1.8f + complexity * 4.0f), 1.2f));
                result = clamp01(facets * 0.25f + edge * 0.22f + fine * 0.08f);
                accent = edge * smoothstep(0.82f, 1.0f, base) * glow;
                break;
            }
            case 69: {
                float boardScale = 2.2f + complexity * 4.8f;
                float boardX = x * s * boardScale;
                float boardY = y * s * boardScale;
                int cellX = static_cast<int>(std::floor(boardX));
                int cellY = static_cast<int>(std::floor(boardY));
                float localX = fract(boardX) - 0.5f;
                float localY = fract(boardY) - 0.5f;
                float cellKind = hash(static_cast<float>(cellX), static_cast<float>(cellY), seed);
                float horizontal = 1.0f - smoothstep(0.018f, 0.055f, std::abs(localY - (cellKind - 0.5f) * 0.42f));
                float vertical = 1.0f - smoothstep(0.018f, 0.055f, std::abs(localX - (hash(cellY, cellX, seed + 17) - 0.5f) * 0.42f));
                float route = cellKind < 0.5f ? std::max(horizontal, vertical * smoothstep(0.05f, 0.42f, std::abs(localY))) :
                        std::max(vertical, horizontal * smoothstep(0.05f, 0.42f, std::abs(localX)));
                float viaDistance = std::sqrt((localX - (cellKind - 0.5f) * 0.38f) * (localX - (cellKind - 0.5f) * 0.38f) +
                        (localY - (hash(cellY, cellX, seed + 31) - 0.5f) * 0.38f) * (localY - (hash(cellY, cellX, seed + 31) - 0.5f) * 0.38f));
                float via = 1.0f - smoothstep(0.055f, 0.115f, viaDistance);
                float chip = cellKind > 0.76f ?
                        1.0f - smoothstep(0.22f, 0.29f, std::max(std::abs(localX), std::abs(localY) * 1.45f)) : 0.0f;
                float pins = chip * (1.0f - smoothstep(0.02f, 0.06f, std::abs(std::abs(localY) - 0.2f)));
                float heat = 0.0f;
                for (int oy = -1; oy <= 1; ++oy) {
                    for (int ox = -1; ox <= 1; ++ox) {
                        int hotX = cellX + ox;
                        int hotY = cellY + oy;
                        float hotKind = hash(static_cast<float>(hotX), static_cast<float>(hotY), seed);
                        if (hotKind <= 0.76f) continue;
                        float dx = boardX - (hotX + 0.5f);
                        float dy = boardY - (hotY + 0.5f);
                        heat = std::max(heat, std::exp(-(dx * dx + dy * dy) * (2.5f + distortion * 7.0f)));
                    }
                }
                uint32_t board = mixColor(color(c, 0, 0xff07120eu), color(c, 1, 0xff08705bu), 0.2f + base * 0.32f);
                board = mixColor(board, 0xff020504u, chip * 0.8f);
                board = mixColor(board, color(c, 2, 0xffff7b22u), clamp01(route * 0.62f + via * 0.78f + pins));
                board = mixColor(board, color(c, 3, 0xfffff1a8u), heat * glow * (0.35f + via * 0.65f));
                return board;
            }
            case 70: {
                float grid = 4.0f + complexity * 9.0f;
                float gx = fract((u + 0.5f) * grid) - 0.5f;
                float gy = fract((v + 0.5f) * grid) - 0.5f;
                float radius = std::sqrt(gx * gx + gy * gy);
                float angle = std::atan2(gy, gx);
                float shell = 1.0f - smoothstep(0.31f, 0.42f, radius);
                float pores = 0.5f + 0.5f * std::sin(angle * (8.0f + complexity * 14.0f) + radius * 90.0f);
                result = clamp01(shell * (0.38f + pores * 0.62f));
                accent = shell * std::pow(pores, 7.0f) * glow;
                break;
            }
            default:
                break;
        }
        result = contrast(result, contrastAmount);
        return mixColor(palette4(c, result), color(c, 3, 0xffffe8a3u), clamp01(accent));
    }

    float simulationField(
            int textureType,
            NoiseBank &n,
            float x,
            float y,
            int seed,
            int width,
            int height,
            const std::vector<float> &p
    ) {
        float s = std::max(parameter(p, 0, 0.009f), 0.00001f);
        float complexity = clamp01(parameter(p, 1, 0.62f));
        float distortion = clamp01(parameter(p, 2, 0.48f));
        float unit = static_cast<float>(std::min(width, height));
        float u = (x - width * 0.5f) / unit;
        float v = (y - height * 0.5f) / unit;
        float spatialScale = std::clamp(s / 0.008f, 0.125f, 5.0f);
        u *= spatialScale;
        v *= spatialScale;
        float nx = x;
        float ny = y;
        n.warp(nx, ny, s * 0.65f, distortion * 62.0f);
        float a = n.fbm(nx * s, ny * s);
        float b = n.fine(nx * s * 3.8f, ny * s * 3.8f);
        float radius = std::sqrt(u * u + v * v);
        float angle = std::atan2(v, u);

        switch (textureType) {
            case 71:
                return clamp01(0.5f + 0.5f * std::sin((a * 1.7f + b * 0.8f) * (10.0f + complexity * 18.0f)));
            case 72: {
                float spacing = 0.68f;
                float gridX = u / spacing;
                float gridY = v / spacing;
                int cellX = static_cast<int>(std::floor(gridX));
                int cellY = static_cast<int>(std::floor(gridY));
                float localX = fract(gridX) - 0.5f;
                float localY = fract(gridY) - 0.5f;
                float colonySeed = hash(static_cast<float>(cellX), static_cast<float>(cellY), seed);
                float sway = std::sin(localY * (7.0f + colonySeed * 5.0f) + colonySeed * PI * 2.0f) * distortion * 0.045f;
                float trunkWidth = 0.018f + colonySeed * 0.012f;
                float coral = (1.0f - smoothstep(trunkWidth, trunkWidth + 0.018f, std::abs(localX - sway))) *
                        smoothstep(0.48f, 0.31f, localY) * smoothstep(-0.48f, -0.38f, localY);
                int limbCount = 4 + static_cast<int>(complexity * 6.0f);
                for (int i = 0; i < limbCount; ++i) {
                    float originY = 0.3f - static_cast<float>(i) * 0.65f / static_cast<float>(limbCount);
                    float side = ((i + cellX + cellY) & 1) == 0 ? -1.0f : 1.0f;
                    float limbSeed = hash(static_cast<float>(cellX * 13 + i), static_cast<float>(cellY), seed + 23);
                    float reach = 0.13f + limbSeed * 0.18f;
                    float dy = localY - originY;
                    float progress = smoothstep(0.015f, -0.22f, dy);
                    float branchX = sway + side * reach * progress + std::sin(dy * 21.0f + limbSeed * 5.0f) * distortion * 0.018f;
                    float branch = 1.0f - smoothstep(0.012f, 0.032f, std::abs(localX - branchX));
                    branch *= smoothstep(-0.27f, -0.05f, dy) * (1.0f - smoothstep(-0.015f, 0.035f, dy));
                    float polyp = 1.0f - smoothstep(0.025f, 0.065f, std::sqrt((localX - branchX) * (localX - branchX) + (dy + 0.24f) * (dy + 0.24f)));
                    coral = std::max(coral, branch);
                    coral = std::max(coral, polyp);
                }
                return clamp01(coral * (0.72f + b * 0.28f));
            }
            case 73: {
                float flow = std::sin((u + std::sin(v * 8.0f + a * 3.0f) * distortion * 0.18f) * (28.0f + complexity * 62.0f));
                float split = std::sin((v - std::cos(u * 11.0f) * distortion * 0.14f) * (17.0f + complexity * 39.0f));
                float trails = std::min(std::abs(flow), std::abs(flow * 0.62f + split * 0.38f));
                return clamp01(1.0f - trails * 4.0f);
            }
            case 74: {
                float spacing = 0.72f;
                float localX = fract(u / spacing + 0.5f) - 0.5f;
                float localY = fract(v / spacing + 0.5f) - 0.5f;
                int cellX = static_cast<int>(std::floor(u / spacing + 0.5f));
                int cellY = static_cast<int>(std::floor(v / spacing + 0.5f));
                float crystalSeed = hash(static_cast<float>(cellX), static_cast<float>(cellY), seed);
                float localRadius = std::sqrt(localX * localX + localY * localY);
                float localAngle = std::atan2(localY, localX) + crystalSeed * 0.35f;
                float arms = 5.0f + std::floor(complexity * 5.0f);
                float sector = 2.0f * PI / arms;
                float foldedAngle = std::abs(fract((localAngle + PI) / sector) - 0.5f) * sector;
                float branchDistance = localRadius * std::sin(foldedAngle);
                float trunk = 1.0f - smoothstep(0.008f, 0.023f, std::abs(branchDistance));
                float sideBranches = 1.0f - smoothstep(0.0f, 0.065f,
                        std::abs(std::sin(localRadius * (65.0f + distortion * 55.0f) - foldedAngle * 9.0f)));
                sideBranches *= smoothstep(0.04f, 0.31f, localRadius) * (1.0f - smoothstep(0.3f, 0.42f, localRadius));
                float crystal = std::max(trunk, sideBranches * 0.72f);
                return clamp01(crystal * (1.0f - smoothstep(0.34f, 0.46f, localRadius)));
            }
            case 75: {
                float tileWidth = 0.58f;
                float tileHeight = 0.92f;
                float gridX = u / tileWidth;
                float gridY = v / tileHeight;
                int cellX = static_cast<int>(std::floor(gridX + 0.5f));
                int cellY = static_cast<int>(std::floor(gridY + 0.5f));
                float localX = fract(gridX + 0.5f) - 0.5f;
                float localY = fract(gridY + 0.5f) - 0.5f;
                float boltSeed = hash(static_cast<float>(cellX), static_cast<float>(cellY), seed);
                float mainPath = std::sin(localY * (12.0f + boltSeed * 9.0f) + boltSeed * 8.0f) * (0.025f + distortion * 0.07f) +
                        std::sin(localY * 37.0f + boltSeed * 17.0f) * distortion * 0.018f;
                float arc = 0.0f;
                arc = 1.0f - smoothstep(0.008f, 0.022f, std::abs(localX - mainPath));
                int branches = 2 + static_cast<int>(complexity * 5.0f);
                for (int i = 0; i < branches; ++i) {
                    float forkY = 0.28f - static_cast<float>(i) * 0.56f / std::max(branches - 1, 1);
                    float dy = localY - forkY;
                    float side = ((i + cellX) & 1) == 0 ? -1.0f : 1.0f;
                    float progress = smoothstep(-0.02f, -0.26f, dy);
                    float branchPath = mainPath + side * progress * (0.09f + 0.045f * i);
                    float branch = 1.0f - smoothstep(0.006f, 0.018f, std::abs(localX - branchPath));
                    branch *= smoothstep(-0.3f, -0.04f, dy) * (1.0f - smoothstep(-0.015f, 0.035f, dy));
                    arc = std::max(arc, branch * 0.82f);
                }
                return clamp01(arc * smoothstep(0.5f, 0.42f, std::abs(localY)));
            }
            case 76: {
                float tracks = 0.0f;
                float spacing = 0.09f + (1.0f - complexity) * 0.08f;
                float familySlope = -0.72f + distortion * 1.44f;
                int estimatedTrack = static_cast<int>(std::floor((v - familySlope * u) / spacing));
                for (int offset = -2; offset <= 2; ++offset) {
                    int trackIndex = estimatedTrack + offset;
                    float trackSeed = hash(static_cast<float>(trackIndex), 3.7f, seed);
                    float slope = familySlope + (trackSeed - 0.5f) * 0.65f;
                    float pathY = trackIndex * spacing + slope * u +
                            std::sin(u * (2.5f + trackSeed * 5.0f) + trackSeed * 9.0f) * distortion * 0.045f;
                    float trackWidth = 0.0025f + hash(static_cast<float>(trackIndex), 8.1f, seed + 17) * 0.0045f;
                    float track = 1.0f - smoothstep(trackWidth, trackWidth * 2.6f, std::abs(v - pathY));
                    float droplets = smoothstep(0.82f, 1.0f, std::sin(u * (48.0f + trackSeed * 42.0f) + trackSeed * 13.0f) * 0.5f + 0.5f);
                    tracks = std::max(tracks, track);
                    tracks = std::max(tracks, track * droplets);
                }
                return clamp01(tracks * (0.78f + b * 0.22f));
            }
            case 77:
                return clamp01(0.5f + 0.5f * std::sin((nx - ny * 0.35f) * s * (9.0f + complexity * 22.0f) + a * 12.0f));
            case 78: {
                float grid = 4.0f + complexity * 8.0f;
                float gx = fract((u + 0.5f) * grid) - 0.5f;
                float gy = fract((v + 0.5f) * grid) - 0.5f;
                int ix = static_cast<int>(std::floor((u + 0.5f) * grid));
                int iy = static_cast<int>(std::floor((v + 0.5f) * grid));
                float tilt = (hash(static_cast<float>(ix), static_cast<float>(iy), seed) - 0.5f) * distortion;
                float embryo = std::sqrt((gx + gy * tilt) * (gx + gy * tilt) / 0.09f + gy * gy / 0.16f);
                float membrane = 1.0f - smoothstep(0.78f, 1.0f, embryo);
                float nucleus = 1.0f - smoothstep(0.08f, 0.18f, std::sqrt((gx + 0.1f) * (gx + 0.1f) + (gy - 0.04f) * (gy - 0.04f)));
                return clamp01(membrane * 0.45f + nucleus * 0.75f);
            }
            case 79: {
                float spacing = 0.31f - complexity * 0.1f;
                int baseX = static_cast<int>(std::floor(u / spacing));
                int baseY = static_cast<int>(std::floor(v / spacing));
                float network = 0.0f;
                auto neuronPoint = [&](int px, int py) {
                    float jitterX = (hash(static_cast<float>(px), static_cast<float>(py), seed) - 0.5f) * spacing * 0.5f;
                    float jitterY = (hash(static_cast<float>(py), static_cast<float>(px), seed + 29) - 0.5f) * spacing * 0.5f;
                    return std::pair<float, float>((px + 0.5f) * spacing + jitterX, (py + 0.5f) * spacing + jitterY);
                };
                auto segmentDistance = [&](float ax, float ay, float bx, float by) {
                    float vx = bx - ax;
                    float vy = by - ay;
                    float lengthSquared = std::max(vx * vx + vy * vy, 0.000001f);
                    float t = clamp01(((u - ax) * vx + (v - ay) * vy) / lengthSquared);
                    float dx = u - ax - vx * t;
                    float dy = v - ay - vy * t;
                    return std::sqrt(dx * dx + dy * dy);
                };
                for (int oy = -1; oy <= 1; ++oy) {
                    for (int ox = -1; ox <= 1; ++ox) {
                        int neuronX = baseX + ox;
                        int neuronY = baseY + oy;
                        auto point = neuronPoint(neuronX, neuronY);
                        float somaDistance = std::sqrt((u - point.first) * (u - point.first) + (v - point.second) * (v - point.second));
                        float soma = 1.0f - smoothstep(spacing * 0.055f, spacing * 0.16f, somaDistance);
                        network = std::max(network, soma);
                        auto right = neuronPoint(neuronX + 1, neuronY);
                        auto diagonal = neuronPoint(neuronX + ((neuronY & 1) == 0 ? 1 : -1), neuronY + 1);
                        float axon = 1.0f - smoothstep(spacing * 0.012f, spacing * (0.035f + distortion * 0.025f),
                                std::min(segmentDistance(point.first, point.second, right.first, right.second),
                                        segmentDistance(point.first, point.second, diagonal.first, diagonal.second)));
                        network = std::max(network, axon * 0.72f);
                    }
                }
                return clamp01(network);
            }
            case 80: {
                float left = std::atan2(v, u + 0.18f);
                float right = std::atan2(v, u - 0.18f);
                return clamp01(0.5f + 0.5f * std::sin((left - right) * (8.0f + complexity * 18.0f) + a * distortion * 4.0f));
            }
            case 81: {
                float frequency = 4.0f + complexity * 12.0f;
                float meander = u + std::sin(v * 3.7f + a * 2.0f) * distortion * 0.16f;
                float trunk = std::abs(std::sin(meander * frequency * PI));
                float tributaryA = std::abs(std::sin((u * 0.62f + v * 0.78f + std::sin(v * 5.0f) * distortion * 0.08f) * frequency * 1.7f * PI));
                float tributaryB = std::abs(std::sin((u * 0.58f - v * 0.82f + std::cos(v * 4.2f) * distortion * 0.07f) * frequency * 2.3f * PI));
                float hierarchy = std::min(trunk, std::min(tributaryA * 1.25f, tributaryB * 1.55f));
                float width = 0.045f + (0.5f + 0.5f * std::sin(v * frequency * 0.7f)) * 0.028f;
                return clamp01(1.0f - smoothstep(width * 0.35f, width, hierarchy));
            }
            case 82: {
                float rosettes = std::sin(u * (18.0f + complexity * 38.0f) + std::sin(v * 13.0f) * 2.0f) *
                        std::sin(v * (21.0f + complexity * 31.0f) + std::cos(u * 11.0f) * 2.0f);
                float patches = smoothstep(0.38f - distortion * 0.3f, 0.82f, a);
                return clamp01((0.5f + rosettes * 0.5f) * patches + b * 0.18f);
            }
            case 83: {
                float spacing = 0.15f + (1.0f - complexity) * 0.12f;
                int baseX = static_cast<int>(std::floor(u / spacing));
                int baseY = static_cast<int>(std::floor(v / spacing));
                float rods = 0.0f;
                for (int oy = -1; oy <= 1; ++oy) {
                    for (int ox = -1; ox <= 1; ++ox) {
                        int bacteriaX = baseX + ox;
                        int bacteriaY = baseY + oy;
                        float bacteriaSeed = hash(static_cast<float>(bacteriaX), static_cast<float>(bacteriaY), seed);
                        if (bacteriaSeed < 0.18f + (1.0f - complexity) * 0.28f) continue;
                        float cx = (bacteriaX + 0.5f) * spacing +
                                (hash(static_cast<float>(bacteriaX), static_cast<float>(bacteriaY), seed + 13) - 0.5f) * spacing * 0.58f;
                        float cy = (bacteriaY + 0.5f) * spacing +
                                (hash(static_cast<float>(bacteriaY), static_cast<float>(bacteriaX), seed + 31) - 0.5f) * spacing * 0.58f;
                        float orientation = bacteriaSeed * PI;
                        float dx = u - cx;
                        float dy = v - cy;
                        float along = dx * std::cos(orientation) + dy * std::sin(orientation);
                        float across = -dx * std::sin(orientation) + dy * std::cos(orientation);
                        float halfLength = spacing * (0.22f + bacteriaSeed * 0.16f);
                        float bodyX = std::max(std::abs(along) - halfLength, 0.0f);
                        float bodyDistance = std::sqrt(bodyX * bodyX + across * across);
                        float body = 1.0f - smoothstep(spacing * 0.055f, spacing * 0.11f, bodyDistance);
                        float nucleus = 1.0f - smoothstep(spacing * 0.02f, spacing * 0.055f,
                                std::sqrt(along * along + across * across));
                        rods = std::max(rods, body * 0.7f + nucleus * 0.42f);
                    }
                }
                return clamp01(a * 0.12f + rods);
            }
            case 84: {
                float vortices = 0.0f;
                for (int i = 0; i < 4; ++i) {
                    float cx = (hash(static_cast<float>(i), 3.7f, seed) - 0.5f) * 0.72f;
                    float cy = (hash(8.4f, static_cast<float>(i), seed + 13) - 0.5f) * 0.72f;
                    float dx = u - cx;
                    float dy = v - cy;
                    float localRadius = std::sqrt(dx * dx + dy * dy);
                    float spiral = std::sin(std::atan2(dy, dx) * (2.0f + i) + localRadius * (42.0f + complexity * 58.0f));
                    vortices += spiral * std::exp(-localRadius * (7.0f - distortion * 3.0f));
                }
                return clamp01(0.5f + vortices * 0.34f);
            }
            case 85: {
                float shards = 0.0f;
                int shardCount = 7 + static_cast<int>(complexity * 13.0f);
                for (int i = 0; i < shardCount; ++i) {
                    float direction = 2.0f * PI * static_cast<float>(i) / static_cast<float>(shardCount);
                    float projection = u * std::cos(direction) + v * std::sin(direction);
                    float lateral = std::abs(-u * std::sin(direction) + v * std::cos(direction));
                    float shard = smoothstep(-0.03f, 0.05f, projection) *
                            (1.0f - smoothstep(0.018f, 0.055f + projection * 0.16f, lateral));
                    shards = std::max(shards, shard);
                }
                return clamp01(shards * (0.75f + b * 0.25f));
            }
            case 86: {
                float spacing = 0.27f + (1.0f - complexity) * 0.13f;
                int baseX = static_cast<int>(std::floor(u / spacing));
                int baseY = static_cast<int>(std::floor(v / spacing));
                float web = 0.0f;
                auto nodePoint = [&](int px, int py) {
                    float jitterX = (hash(static_cast<float>(px), static_cast<float>(py), seed) - 0.5f) * spacing * 0.62f;
                    float jitterY = (hash(static_cast<float>(py), static_cast<float>(px), seed + 37) - 0.5f) * spacing * 0.62f;
                    return std::pair<float, float>((px + 0.5f) * spacing + jitterX, (py + 0.5f) * spacing + jitterY);
                };
                auto segmentDistance = [&](const std::pair<float, float> &from, const std::pair<float, float> &to) {
                    float vx = to.first - from.first;
                    float vy = to.second - from.second;
                    float lengthSquared = std::max(vx * vx + vy * vy, 0.000001f);
                    float t = clamp01(((u - from.first) * vx + (v - from.second) * vy) / lengthSquared);
                    float dx = u - from.first - vx * t;
                    float dy = v - from.second - vy * t;
                    return std::sqrt(dx * dx + dy * dy);
                };
                for (int oy = -1; oy <= 1; ++oy) {
                    for (int ox = -1; ox <= 1; ++ox) {
                        int nodeX = baseX + ox;
                        int nodeY = baseY + oy;
                        auto node = nodePoint(nodeX, nodeY);
                        float nodeDistance = std::sqrt((u - node.first) * (u - node.first) + (v - node.second) * (v - node.second));
                        float halo = std::exp(-nodeDistance * nodeDistance / (spacing * spacing * 0.025f));
                        web = std::max(web, halo);
                        auto right = nodePoint(nodeX + 1, nodeY);
                        auto up = nodePoint(nodeX, nodeY + 1);
                        float linkDistance = std::min(segmentDistance(node, right), segmentDistance(node, up));
                        if (hash(static_cast<float>(nodeX), static_cast<float>(nodeY), seed + 71) > 0.48f) {
                            auto diagonal = nodePoint(nodeX + 1, nodeY + 1);
                            linkDistance = std::min(linkDistance, segmentDistance(node, diagonal));
                        }
                        float filament = 1.0f - smoothstep(spacing * 0.008f, spacing * (0.025f + distortion * 0.025f), linkDistance);
                        web = std::max(web, filament * 0.78f);
                    }
                }
                return clamp01(web);
            }
            case 87: {
                float mainVein = 1.0f - smoothstep(0.0f, 0.035f, std::abs(u));
                float side = 1.0f - std::abs(std::sin((v + a * 0.09f) * (22.0f + complexity * 38.0f) + std::abs(u) * 46.0f));
                return clamp01(mainVein + side * 0.6f + a * 0.22f);
            }
            case 88: {
                float pores = std::sin(u * (42.0f + complexity * 72.0f)) +
                        std::sin((u * 0.5f + v * 0.866f) * (45.0f + complexity * 68.0f)) +
                        std::sin((-u * 0.5f + v * 0.866f) * (39.0f + complexity * 76.0f));
                float holes = smoothstep(1.25f - distortion * 0.55f, 2.55f, pores);
                return clamp01((1.0f - holes) * 0.72f + b * 0.28f);
            }
            case 89: {
                auto dropletLayer = [&](float grid, int layerSeed, float stretch) {
                    float gx = u * grid;
                    float gy = v * grid;
                    int ix = static_cast<int>(std::floor(gx));
                    int iy = static_cast<int>(std::floor(gy));
                    float localX = fract(gx) - 0.5f;
                    float localY = fract(gy) - 0.5f;
                    localX += (hash(static_cast<float>(ix), static_cast<float>(iy), seed + layerSeed) - 0.5f) * 0.56f;
                    localY += (hash(static_cast<float>(iy), static_cast<float>(ix), seed + layerSeed + 19) - 0.5f) * 0.5f;
                    float dropSeed = hash(static_cast<float>(ix * 7), static_cast<float>(iy * 11), seed + layerSeed + 41);
                    float radiusX = 0.16f + dropSeed * 0.11f;
                    float radiusY = radiusX * stretch;
                    float teardropX = localX / radiusX;
                    float teardropY = (localY + std::max(localY, 0.0f) * 0.28f) / radiusY;
                    float distance = std::sqrt(teardropX * teardropX + teardropY * teardropY);
                    float dome = clamp01(1.0f - distance);
                    float rim = 1.0f - smoothstep(0.72f, 1.0f, distance);
                    float streak = (1.0f - smoothstep(0.0f, 0.18f, std::abs(teardropX))) *
                            smoothstep(0.15f, 0.48f, localY) * distortion;
                    return clamp01(dome * dome * 0.82f + rim * 0.16f + streak * 0.45f);
                };
                float largeDrops = dropletLayer(5.0f + complexity * 7.0f, 0, 1.35f);
                float smallDrops = dropletLayer(9.0f + complexity * 12.0f, 97, 1.05f);
                return clamp01(std::max(largeDrops, smallDrops * 0.72f));
            }
            case 90: {
                float embers = smoothstep(0.985f - complexity * 0.025f, 1.0f, hash(std::floor(x * 0.45f), std::floor(y * 0.45f), seed));
                return clamp01(a * 0.25f + embers * (0.65f + b * 0.35f));
            }
            case 91: {
                float foam = 0.0f;
                int waveCount = 4 + static_cast<int>(complexity * 8.0f);
                for (int i = 0; i < waveCount; ++i) {
                    float cx = hash(static_cast<float>(i), 2.6f, seed) - 0.5f;
                    float cy = hash(7.1f, static_cast<float>(i), seed + 37) - 0.5f;
                    float distance = std::sqrt((u - cx) * (u - cx) + (v - cy) * (v - cy));
                    foam += std::sin(distance * (48.0f + distortion * 82.0f) + i * 1.9f);
                }
                return clamp01(0.5f + foam / static_cast<float>(waveCount) * 0.5f);
            }
            case 92: {
                float modeA = 2.0f + std::floor(complexity * 6.0f);
                float modeB = modeA + 1.0f + std::floor(distortion * 4.0f);
                float plateX = u + 0.5f;
                float plateY = v + 0.5f;
                float vibration = std::sin(modeA * PI * plateX) * std::sin(modeB * PI * plateY) -
                        std::sin(modeB * PI * plateX) * std::sin(modeA * PI * plateY);
                return clamp01(1.0f - std::abs(vibration) * 4.5f);
            }
            case 93: {
                float petals = 5.0f + std::floor(complexity * 15.0f);
                float radialWave = std::sin(radius * (72.0f + complexity * 120.0f) + std::sin(angle * petals) * (2.0f + distortion * 8.0f));
                float angularWave = std::cos(angle * petals + radius * 18.0f);
                return clamp01(1.0f - std::abs(radialWave * 0.72f + angularWave * 0.28f) * 2.8f);
            }
            case 94: {
                float branches = 7.0f + std::floor(complexity * 22.0f);
                float fork = std::sin(angle * branches + std::sin(angle * 3.0f + radius * 31.0f) * (1.0f + distortion * 5.0f));
                float secondary = std::sin(angle * branches * 2.07f - radius * 44.0f + a * 4.0f);
                return clamp01(1.0f - std::min(std::abs(fork), std::abs(secondary) * 1.2f) * (5.0f + radius * 3.0f));
            }
            case 95: {
                int symmetry = 5 + static_cast<int>(complexity * 8.0f);
                float sum = 0.0f;
                for (int i = 0; i < symmetry; ++i) {
                    float direction = 2.0f * PI * static_cast<float>(i) / static_cast<float>(symmetry);
                    sum += std::cos((u * std::cos(direction) + v * std::sin(direction)) * (48.0f + distortion * 70.0f));
                }
                sum /= static_cast<float>(symmetry);
                return clamp01(1.0f - std::abs(sum) * 3.2f);
            }
            case 96: {
                float magnification = std::exp2(clamp01((s - 0.001f) / 0.039f) * 8.0f);
                float focus = smoothstep(1.6f, 8.0f, magnification);
                float centerX = -0.5f + (-0.7436439f + 0.5f) * focus + (distortion - 0.5f) * 0.16f / magnification;
                float centerY = 0.1318259f * focus + (distortion - 0.5f) * 0.1f / magnification;
                float cx = u / spatialScale * 3.1f / magnification + centerX;
                float cy = v / spatialScale * 3.1f / magnification + centerY;
                float zx = 0.0f;
                float zy = 0.0f;
                int iteration = 0;
                int maxIterations = 42 + static_cast<int>(complexity * 104.0f);
                while (iteration < maxIterations && zx * zx + zy * zy < 16.0f) {
                    float nextX = zx * zx - zy * zy + cx;
                    zy = 2.0f * zx * zy + cy;
                    zx = nextX;
                    ++iteration;
                }
                return static_cast<float>(iteration) / static_cast<float>(maxIterations);
            }
            case 97: {
                float magnification = std::exp2(clamp01((s - 0.001f) / 0.039f) * 7.5f);
                float focus = smoothstep(1.7f, 7.0f, magnification);
                float centerX = -0.5f + (-1.7443359f + 0.5f) * focus + (distortion - 0.5f) * 0.18f / magnification;
                float centerY = -0.48f + (-0.017451f + 0.48f) * focus;
                float cx = u / spatialScale * 3.2f / magnification + centerX;
                float cy = v / spatialScale * 3.2f / magnification + centerY;
                float zx = 0.0f;
                float zy = 0.0f;
                int iteration = 0;
                int maxIterations = 42 + static_cast<int>(complexity * 100.0f);
                while (iteration < maxIterations && zx * zx + zy * zy < 16.0f) {
                    zx = std::abs(zx);
                    zy = std::abs(zy);
                    float nextX = zx * zx - zy * zy + cx;
                    zy = 2.0f * zx * zy + cy;
                    zx = nextX;
                    ++iteration;
                }
                return static_cast<float>(iteration) / static_cast<float>(maxIterations);
            }
            case 98: {
                float magnification = std::exp2(clamp01((s - 0.001f) / 0.039f) * 7.0f);
                float focus = smoothstep(1.6f, 6.0f, magnification);
                float zx = u / spatialScale * 2.7f / magnification - 0.11f * focus;
                float zy = v / spatialScale * 2.7f / magnification + 0.64f * focus;
                float phase = distortion * 2.0f * PI;
                float cx = -0.73f + std::cos(phase) * 0.12f;
                float cy = std::sin(phase) * 0.21f;
                int iteration = 0;
                int maxIterations = 42 + static_cast<int>(complexity * 104.0f);
                while (iteration < maxIterations && zx * zx + zy * zy < 16.0f) {
                    float nextX = zx * zx - zy * zy + cx;
                    zy = 2.0f * zx * zy + cy;
                    zx = nextX;
                    ++iteration;
                }
                return static_cast<float>(iteration) / static_cast<float>(maxIterations);
            }
            case 99: {
                float segments = 4.0f + std::floor(complexity * 12.0f);
                float sector = 2.0f * PI / segments;
                float folded = std::abs(fract((angle + PI) / sector) - 0.5f) * sector;
                float crystal = std::sin(radius * (70.0f + distortion * 90.0f) + folded * 44.0f + std::sin(folded * segments) * 3.0f);
                return clamp01(1.0f - std::abs(crystal) * 2.9f);
            }
            case 100: {
                float facet = std::abs(fract((angle / (2.0f * PI) + 0.5f) * (4.0f + std::floor(complexity * 10.0f))) - 0.5f);
                float refraction = fract(radius * (5.0f + distortion * 11.0f) + facet * 3.0f + angle / (2.0f * PI));
                return clamp01(refraction * 0.75f + (1.0f - facet * 2.0f) * 0.25f);
            }
            case 101: {
                float turns = 2.0f + std::floor(complexity * 6.0f);
                float twist = 2.0f + std::floor(distortion * 7.0f);
                float knotRadius = 0.27f + 0.09f * std::cos(angle * turns);
                float tube = std::abs(radius - knotRadius - 0.045f * std::sin(angle * twist));
                float crossing = 0.5f + 0.5f * std::sin(angle * turns * twist);
                return clamp01(1.0f - tube * (20.0f + crossing * 18.0f));
            }
            case 102: {
                float tileWidth = 0.62f;
                float tileHeight = 0.94f;
                float gridX = u / tileWidth;
                float gridY = v / tileHeight;
                int plantX = static_cast<int>(std::floor(gridX + 0.5f));
                int plantY = static_cast<int>(std::floor(gridY + 0.5f));
                float localX = fract(gridX + 0.5f) - 0.5f;
                float localY = fract(gridY + 0.5f) - 0.5f;
                float plantSeed = hash(static_cast<float>(plantX), static_cast<float>(plantY), seed);
                float stemPath = std::sin(localY * (5.0f + plantSeed * 4.0f) + plantSeed * 6.0f) * distortion * 0.045f;
                float stem = (1.0f - smoothstep(0.007f, 0.022f, std::abs(localX - stemPath))) *
                        smoothstep(0.49f, 0.42f, std::abs(localY));
                float leaves = 0.0f;
                int leafCount = 5 + static_cast<int>(complexity * 6.0f);
                for (int i = 0; i < leafCount; ++i) {
                    float leafY = -0.36f + static_cast<float>(i) * 0.72f / std::max(leafCount - 1, 1);
                    float side = ((i + plantX + plantY) & 1) == 0 ? -1.0f : 1.0f;
                    float leafSeed = hash(static_cast<float>(plantX * 17 + i), static_cast<float>(plantY), seed + 43);
                    float leafX = stemPath + side * (0.13f + leafSeed * 0.08f);
                    float rotation = side * (0.28f + leafSeed * 0.34f);
                    float dx = localX - leafX;
                    float dy = localY - leafY;
                    float rx = dx * std::cos(rotation) + dy * std::sin(rotation);
                    float ry = -dx * std::sin(rotation) + dy * std::cos(rotation);
                    float leafDistance = std::sqrt(rx * rx / 0.038f + ry * ry / 0.0042f);
                    float leaf = 1.0f - smoothstep(0.72f, 1.0f, leafDistance);
                    float mainVein = 1.0f - smoothstep(0.004f, 0.018f, std::abs(ry));
                    float sideVeins = 1.0f - smoothstep(0.0f, 0.16f,
                            std::abs(std::sin(rx * (48.0f + complexity * 35.0f)) - ry * 18.0f));
                    float leafStructure = leaf * (0.22f + mainVein * 0.52f + sideVeins * 0.32f);
                    leaves = std::max(leaves, leafStructure);
                }
                return clamp01(stem + leaves);
            }
            case 103: {
                float spots = std::sin(u * (52.0f + complexity * 68.0f) + std::sin(v * 17.0f) * 3.0f) +
                        std::sin(v * (46.0f + complexity * 74.0f) + std::cos(u * 19.0f) * 3.0f);
                float pulse = 0.5f + 0.5f * std::sin(spots * 2.4f + distortion * 2.0f * PI + a * 3.0f);
                return clamp01(pulse);
            }
            case 104: {
                float ribs = std::abs(std::sin((u + std::sin(v * 9.0f) * distortion * 0.08f) * (48.0f + complexity * 90.0f)));
                float tendons = std::abs(std::sin((v - u * 0.28f) * (22.0f + complexity * 36.0f) + a * 5.0f));
                return clamp01(1.0f - std::min(ribs, tendons * 1.4f) * 2.7f);
            }
            case 105: {
                float curls = std::sin(angle * (4.0f + complexity * 11.0f) + radius * (34.0f + distortion * 55.0f));
                float scrolls = std::sin((u * u - v * v) * 95.0f + angle * 6.0f);
                return clamp01(1.0f - std::min(std::abs(curls), std::abs(scrolls)) * 4.2f);
            }
            case 106: {
                float grid = 5.0f + complexity * 9.0f;
                float gx = fract((u + 0.5f) * grid) - 0.5f;
                float gy = fract((v + 0.5f) * grid) - 0.5f;
                int cellX = static_cast<int>(std::floor((u + 0.5f) * grid));
                int cellY = static_cast<int>(std::floor((v + 0.5f) * grid));
                float glyph = hash(static_cast<float>(cellX), static_cast<float>(cellY), seed);
                float diagonal = glyph < 0.5f ? std::abs(gx - gy) : std::abs(gx + gy);
                float vertical = std::abs(gx + (glyph - 0.5f) * 0.32f);
                float crossbar = std::abs(gy - (glyph - 0.5f) * 0.26f);
                float stroke = std::min(diagonal, std::min(vertical, crossbar));
                float cellMask = smoothstep(0.48f, 0.36f, std::max(std::abs(gx), std::abs(gy)));
                return clamp01((1.0f - smoothstep(0.018f, 0.065f, stroke)) * cellMask);
            }
            case 107: {
                float convection = std::sin((a - 0.5f) * (18.0f + complexity * 34.0f) + b * 7.0f);
                float granules = 1.0f - std::abs(convection);
                return clamp01(granules * 0.78f + a * 0.3f);
            }
            case 108: {
                float spacing = 0.18f + (1.0f - complexity) * 0.14f;
                int baseX = static_cast<int>(std::floor(u / spacing));
                int baseY = static_cast<int>(std::floor(v / spacing));
                float craterField = 0.0f;
                float rays = 0.0f;
                float depressions = 0.0f;
                for (int oy = -1; oy <= 1; ++oy) {
                    for (int ox = -1; ox <= 1; ++ox) {
                        int craterX = baseX + ox;
                        int craterY = baseY + oy;
                        float craterSeed = hash(static_cast<float>(craterX), static_cast<float>(craterY), seed);
                        if (craterSeed < 0.22f) continue;
                        float cx = (craterX + 0.5f) * spacing +
                                (hash(static_cast<float>(craterX), static_cast<float>(craterY), seed + 19) - 0.5f) * spacing * 0.52f;
                        float cy = (craterY + 0.5f) * spacing +
                                (hash(static_cast<float>(craterY), static_cast<float>(craterX), seed + 37) - 0.5f) * spacing * 0.52f;
                        float craterRadius = spacing * (0.12f + craterSeed * 0.2f);
                        float dx = u - cx;
                        float dy = v - cy;
                        float distance = std::sqrt(dx * dx + dy * dy);
                        float rimWidth = craterRadius * (0.12f + distortion * 0.12f);
                        float rim = 1.0f - smoothstep(rimWidth * 0.35f, rimWidth, std::abs(distance - craterRadius));
                        float depression = (1.0f - smoothstep(0.0f, craterRadius, distance)) * (0.4f + craterSeed * 0.28f);
                        float rayPattern = 1.0f - smoothstep(0.0f, 0.13f,
                                std::abs(std::sin(std::atan2(dy, dx) * (4.0f + std::floor(craterSeed * 7.0f)))));
                        float rayFade = smoothstep(craterRadius * (5.0f + distortion * 4.0f), craterRadius * 1.2f, distance);
                        craterField = std::max(craterField, rim);
                        depressions = std::max(depressions, depression);
                        rays = std::max(rays, rayPattern * rayFade * 0.5f);
                    }
                }
                return clamp01(0.24f + (a - 0.5f) * 0.22f - depressions * 0.24f + craterField * 0.68f + rays * 0.42f);
            }
            case 109: {
                float spacing = 0.34f + (1.0f - complexity) * 0.18f;
                int baseX = static_cast<int>(std::floor(u / spacing));
                int baseY = static_cast<int>(std::floor(v / spacing));
                float velocityPhase = 0.0f;
                float totalWeight = 0.0f;
                for (int oy = -1; oy <= 1; ++oy) {
                    for (int ox = -1; ox <= 1; ++ox) {
                        int vortexX = baseX + ox;
                        int vortexY = baseY + oy;
                        float vortexSeed = hash(static_cast<float>(vortexX), static_cast<float>(vortexY), seed);
                        float cx = (vortexX + 0.5f) * spacing + (vortexSeed - 0.5f) * spacing * 0.48f;
                        float cy = (vortexY + 0.5f) * spacing +
                                (hash(static_cast<float>(vortexY), static_cast<float>(vortexX), seed + 53) - 0.5f) * spacing * 0.48f;
                        float dx = u - cx;
                        float dy = v - cy;
                        float localRadius = std::sqrt(dx * dx + dy * dy);
                        float weight = std::exp(-localRadius * localRadius / (spacing * spacing * 0.34f));
                        float spin = vortexSeed < 0.5f ? -1.0f : 1.0f;
                        velocityPhase += std::sin(std::atan2(dy, dx) * spin * (1.0f + std::floor(vortexSeed * 3.0f)) +
                                localRadius / spacing * (13.0f + distortion * 18.0f)) * weight;
                        totalWeight += weight;
                    }
                }
                float jet = std::sin((v + std::sin(u * 3.2f + a * 2.0f) * distortion * 0.12f) *
                        (18.0f + complexity * 34.0f));
                float current = velocityPhase / std::max(totalWeight, 0.001f) * 0.72f + jet * 0.28f;
                return clamp01(1.0f - std::abs(current) * 2.35f);
            }
            case 110: {
                float mountains = 0.0f;
                for (int layer = 0; layer < 6; ++layer) {
                    float depth = static_cast<float>(layer) / 5.0f;
                    float ridge = -0.34f + depth * 0.13f +
                            std::sin(u * (7.0f + layer * 2.3f) + layer * 1.7f) * (0.06f + depth * 0.025f) +
                            n.fbm((u + layer * 0.37f) * (3.0f + complexity * 4.0f), layer * 2.1f) * 0.13f;
                    float silhouette = smoothstep(ridge - 0.018f, ridge + 0.018f + distortion * 0.025f, v);
                    mountains = std::max(mountains, silhouette * (1.0f - depth * 0.13f));
                }
                return clamp01(mountains);
            }
            case 111: {
                float horizon = 0.08f + std::sin(u * 13.0f) * 0.025f;
                float perspective = clamp01((v - horizon) / 0.45f);
                float columns = fract((u + 0.5f) * (12.0f + complexity * 24.0f) / std::max(0.28f, 1.0f - perspective * 0.72f));
                float buildings = smoothstep(horizon - 0.32f * hash(std::floor(columns * 17.0f), std::floor(u * 31.0f), seed), horizon, v);
                float windows = std::pow(clamp01(0.5f + 0.5f * std::sin(u * 180.0f) * std::sin(v * 145.0f)), 7.0f);
                float road = smoothstep(horizon, 0.48f, v) * (1.0f - smoothstep(0.02f, 0.11f, std::abs(u)));
                return clamp01(buildings * 0.25f + windows * buildings + road * (0.55f + distortion * 0.4f));
            }
            case 112: {
                int seedCount = 150 + static_cast<int>(complexity * 330.0f);
                float normalizedRadius = clamp01(radius / 0.48f);
                int estimated = static_cast<int>(normalizedRadius * normalizedRadius * seedCount);
                float nearest = 1.0f;
                float goldenAngle = 2.3999632f + (distortion - 0.5f) * 0.24f;
                for (int offset = -7; offset <= 7; ++offset) {
                    int index = std::clamp(estimated + offset, 0, seedCount - 1);
                    float seedRadius = std::sqrt(static_cast<float>(index) / static_cast<float>(seedCount)) * 0.47f;
                    float seedAngle = static_cast<float>(index) * goldenAngle;
                    float px = std::cos(seedAngle) * seedRadius;
                    float py = std::sin(seedAngle) * seedRadius;
                    nearest = std::min(nearest, std::sqrt((u - px) * (u - px) + (v - py) * (v - py)));
                }
                return clamp01(1.0f - smoothstep(0.004f, 0.012f, nearest));
            }
            case 113: {
                int depth = 4 + static_cast<int>(complexity * 7.0f);
                int size = 1 << depth;
                float rotation = (distortion - 0.5f) * PI * 0.5f;
                float rx = u * std::cos(rotation) - v * std::sin(rotation);
                float ry = u * std::sin(rotation) + v * std::cos(rotation);
                int ix = std::clamp(static_cast<int>((rx + 0.5f) * size), 0, size - 1);
                int iy = std::clamp(static_cast<int>((ry + 0.5f) * size), 0, size - 1);
                float triangle = (ix & iy) == 0 ? 1.0f : 0.0f;
                triangle *= smoothstep(0.52f, 0.45f, std::max(std::abs(rx), std::abs(ry)));
                return triangle;
            }
            case 114: {
                float rings = 0.0f;
                int levels = 3 + static_cast<int>(complexity * 6.0f);
                for (int level = 0; level < levels; ++level) {
                    float frequency = std::exp2(static_cast<float>(level));
                    float qx = fract((u + 0.5f) * frequency) - 0.5f;
                    float qy = fract((v + 0.5f) * frequency) - 0.5f;
                    float localRadius = std::sqrt(qx * qx + qy * qy);
                    float target = 0.28f + std::sin(level * 1.7f) * distortion * 0.035f;
                    float ring = 1.0f - smoothstep(0.008f, 0.032f, std::abs(localRadius - target));
                    rings = std::max(rings, ring * (1.0f - level * 0.06f));
                }
                return clamp01(rings);
            }
            case 115: {
                constexpr float SQRT3 = 1.7320508f;
                float cellSize = 0.42f;
                float rowStep = cellSize * SQRT3 * 0.5f;
                int baseRow = static_cast<int>(std::floor(v / rowStep));
                float bestDistanceSquared = 1000.0f;
                float localX = 0.0f;
                float localY = 0.0f;
                int selectedColumn = 0;
                int selectedRow = 0;

                for (int row = baseRow - 1; row <= baseRow + 1; ++row) {
                    float rowOffset = row % 2 != 0 ? cellSize * 0.5f : 0.0f;
                    int baseColumn = static_cast<int>(std::floor((u - rowOffset) / cellSize));
                    for (int column = baseColumn - 1; column <= baseColumn + 1; ++column) {
                        float centerX = (static_cast<float>(column) + 0.5f) * cellSize + rowOffset;
                        float centerY = (static_cast<float>(row) + 0.5f) * rowStep;
                        float dx = u - centerX;
                        float dy = v - centerY;
                        float distanceSquared = dx * dx + dy * dy;
                        if (distanceSquared < bestDistanceSquared) {
                            bestDistanceSquared = distanceSquared;
                            localX = dx / cellSize;
                            localY = dy / cellSize;
                            selectedColumn = column;
                            selectedRow = row;
                        }
                    }
                }

                float hexRadius = std::max(std::abs(localX) * 2.0f,
                        std::abs(localX) + std::abs(localY) * SQRT3);
                hexRadius = std::min(hexRadius, 0.992f);
                float hyperbolicRadius = std::log((1.0f + hexRadius) /
                        std::max(1.0f - hexRadius, 0.008f));
                float localAngle = std::atan2(localY, localX);
                float sides = 4.0f + std::floor(complexity * 9.0f);
                float parity = std::abs((selectedColumn + selectedRow) % 2) > 0 ? 1.0f : 0.0f;
                float orientation = parity * PI / sides + static_cast<float>(seed) * 0.0017f;
                float curvedAngle = localAngle + orientation +
                        std::sin(hyperbolicRadius * 0.72f) * distortion * 0.16f;

                float radialFamily = std::abs(std::sin(
                        hyperbolicRadius * (2.15f + complexity * 1.35f) +
                                std::cos(curvedAngle * sides) * (0.85f + distortion * 1.25f)
                ));
                float diagonalFamily = std::abs(std::sin(
                        curvedAngle * sides + hyperbolicRadius * (0.62f + distortion * 1.45f)
                ));
                float counterFamily = std::abs(std::sin(
                        curvedAngle * (sides - 2.0f) - hyperbolicRadius * (1.18f + distortion * 0.82f)
                ));
                float geodesicDistance = std::min(radialFamily,
                        std::min(diagonalFamily, counterFamily));
                float geodesics = 1.0f - smoothstep(0.025f, 0.13f, geodesicDistance);
                float boundary = 1.0f - smoothstep(0.018f, 0.075f, 1.0f - hexRadius);
                float chambers = 0.5f + 0.5f * std::cos(
                        hyperbolicRadius * 1.32f - curvedAngle * (sides * 0.5f) + parity * PI
                );
                return clamp01(geodesics * 0.78f + boundary * 0.58f + chambers * 0.14f);
            }
            case 116: {
                float bands = 3.0f + std::floor(complexity * 9.0f);
                float twist = angle * (1.0f + distortion * 4.0f) + radius * 24.0f;
                float ribbon = std::sin(twist) * std::cos(angle * bands - radius * 8.0f);
                float frontBack = 0.5f + 0.5f * std::sin(angle + radius * PI * 2.0f);
                return clamp01((1.0f - std::abs(ribbon) * 2.9f) * (0.55f + frontBack * 0.45f));
            }
            case 117: {
                float mirroredX = std::abs(u);
                float ink = n.fbm((mirroredX + std::sin(v * 7.0f) * distortion * 0.08f) * (3.0f + complexity * 5.0f), v * (3.0f + complexity * 5.0f));
                float lobes = 0.5f + 0.5f * std::sin(v * (8.0f + complexity * 18.0f) + mirroredX * 11.0f);
                float silhouette = smoothstep(0.58f - distortion * 0.22f, 0.76f, ink * 0.72f + lobes * 0.28f);
                return clamp01(silhouette * smoothstep(0.48f, 0.03f, mirroredX));
            }
            case 118: {
                float waves = 0.0f;
                int sources = 2 + static_cast<int>(complexity * 5.0f);
                for (int i = 0; i < sources; ++i) {
                    float sourceX = (static_cast<float>(i) / std::max(sources - 1, 1) - 0.5f) * 0.72f;
                    float sourceY = (hash(static_cast<float>(i), 6.4f, seed) - 0.5f) * 0.42f;
                    float distance = std::sqrt((u - sourceX) * (u - sourceX) + (v - sourceY) * (v - sourceY));
                    waves += std::sin(distance * (58.0f + distortion * 72.0f) + i * 1.3f);
                }
                return clamp01(0.5f + waves / static_cast<float>(sources) * 0.5f);
            }
            case 119: {
                float frequency = 24.0f + complexity * 52.0f;
                float convection = std::sin(u * frequency + std::sin(v * 9.0f) * distortion * 5.0f) +
                        std::sin((u * 0.5f + v * 0.866f) * frequency) +
                        std::sin((-u * 0.5f + v * 0.866f) * frequency);
                float plumes = 0.5f + 0.5f * std::sin(v * 18.0f + a * 5.0f);
                return clamp01(convection / 6.0f + 0.5f + plumes * 0.2f);
            }
            case 120: {
                float foldA = std::abs(fract((u + v * 0.62f) * (5.0f + complexity * 13.0f)) - 0.5f);
                float foldB = std::abs(fract((v - u * 0.38f) * (6.0f + complexity * 11.0f) + distortion * 0.3f) - 0.5f);
                float crease = std::min(foldA, foldB);
                float face = fract(foldA * 2.0f + foldB * 3.0f);
                return clamp01(face * 0.58f + (1.0f - smoothstep(0.0f, 0.055f, crease)) * 0.62f);
            }
            case 121: {
                float grid = 8.0f + complexity * 19.0f;
                float gx = fract((u + 0.5f + std::sin(v * 8.0f) * distortion * 0.04f) * grid) - 0.5f;
                float gy = fract((v + 0.5f + std::cos(u * 7.0f) * distortion * 0.04f) * grid) - 0.5f;
                float core = std::sqrt(gx * gx + gy * gy);
                float ring = 1.0f - smoothstep(0.07f, 0.18f, core);
                float cladding = 1.0f - smoothstep(0.16f, 0.28f, core);
                return clamp01(ring + cladding * 0.28f);
            }
            default:
                return a;
        }
    }

    uint32_t shadeSimulation(
            int textureType,
            const std::vector<float> &field,
            int x,
            int y,
            int width,
            int height,
            const std::vector<float> &p,
            const std::vector<uint32_t> &c
    ) {
        auto sample = [&](int sx, int sy) {
            sx = std::clamp(sx, 0, width - 1);
            sy = std::clamp(sy, 0, height - 1);
            return field[static_cast<size_t>(sy) * width + sx];
        };
        float center = sample(x, y);
        float left = sample(x - 1, y);
        float right = sample(x + 1, y);
        float top = sample(x, y - 1);
        float bottom = sample(x, y + 1);
        float gx = right - left;
        float gy = bottom - top;
        float gradient = clamp01(std::sqrt(gx * gx + gy * gy) * 7.0f);
        float laplacian = clamp01(std::abs(left + right + top + bottom - center * 4.0f) * 5.0f);
        float sharpness = clamp01(parameter(p, 3, 0.58f));
        float glow = clamp01(parameter(p, 4, 0.52f));
        float contrastAmount = std::max(parameter(p, 5, 1.25f), 0.05f);
        float edge = smoothstep(0.02f + (1.0f - sharpness) * 0.08f, 0.45f, gradient + laplacian * 0.7f);
        float value = contrast(center * 0.72f + edge * 0.42f, contrastAmount);

        if (textureType == 74 || textureType == 75 || textureType == 79 || textureType == 86 || textureType == 87 ||
                textureType == 92 || textureType == 93 || textureType == 94 || textureType == 95 ||
                textureType == 99 || textureType == 101 || textureType == 102 || textureType == 104 ||
                textureType == 105 || textureType == 106 || textureType == 109 || textureType == 112 ||
                textureType == 113 || textureType == 114 || textureType == 115 || textureType == 116 ||
                textureType == 118 || textureType == 121) {
            value = contrast(edge * 0.8f + center * 0.42f, contrastAmount);
        } else if (textureType == 81 || textureType == 88 || textureType == 89) {
            value = contrast(center * 0.62f + (1.0f - edge) * 0.28f, contrastAmount);
        } else if (textureType == 84 || textureType == 91) {
            value = fract(center + gradient * 0.8f + laplacian * 0.45f);
        }

        switch (textureType) {
            case 71:
                value = contrast(smoothstep(0.38f, 0.62f, center) * 0.72f + edge * 0.35f, contrastAmount);
                break;
            case 72:
                value = contrast(center * 0.74f + edge * 0.38f + laplacian * 0.16f, contrastAmount);
                break;
            case 73:
                value = contrast(edge * 0.62f + center * 0.28f, contrastAmount);
                break;
            case 76:
                value = clamp01(center * 0.48f + edge * 0.72f + laplacian * 0.38f);
                break;
            case 78:
                value = contrast(center * 0.72f + gradient * 0.5f, contrastAmount);
                break;
            case 74:
                value = contrast(center * 0.48f + edge * 0.76f + laplacian * 0.22f, contrastAmount);
                break;
            case 75:
                value = contrast(center * 0.72f + edge * 0.62f, contrastAmount);
                break;
            case 79:
                value = contrast(center * 0.58f + edge * 0.68f + laplacian * 0.18f, contrastAmount);
                break;
            case 81:
                value = contrast(center * 0.82f + edge * 0.26f, contrastAmount);
                break;
            case 83:
                value = contrast(center * 0.78f + edge * 0.28f, contrastAmount);
                break;
            case 86:
                value = contrast(center * 0.54f + edge * 0.72f + laplacian * 0.18f, contrastAmount);
                break;
            case 89:
                value = contrast(center * 0.22f + gradient * 0.86f + clamp01((-gx - gy) * 2.0f + 0.5f) * 0.18f, contrastAmount);
                break;
            case 96:
            case 97:
            case 98:
                value = center > 0.995f ? 0.0f : fract(center * 7.0f + edge * 0.42f);
                break;
            case 100:
                value = fract(center + gradient * 0.7f);
                break;
            case 102:
                value = contrast(center * 0.42f + edge * 0.76f + laplacian * 0.22f, contrastAmount);
                break;
            case 107:
            case 119:
                value = contrast(center * 0.76f + laplacian * 0.48f, contrastAmount);
                break;
            case 110:
                value = contrast(center * 0.82f + edge * 0.16f, contrastAmount);
                break;
            case 109:
                value = contrast(center * 0.36f + edge * 0.74f + fract(center * 3.0f) * 0.18f, contrastAmount);
                break;
            case 115:
                value = contrast(center * 0.54f + edge * 0.68f + laplacian * 0.2f, contrastAmount);
                break;
            case 117:
                value = contrast(center * 0.9f + gradient * 0.16f, contrastAmount);
                break;
            case 120:
                value = contrast(center * 0.55f + (-gx - gy + 1.0f) * 0.22f + edge * 0.24f, contrastAmount);
                break;
            default:
                break;
        }

        uint32_t result = palette4(c, value);
        float light = clamp01((-gx - gy) * 2.8f + gradient * 0.35f);
        result = mixColor(result, color(c, 3, 0xffffe8a3u), light * (0.2f + glow * 0.55f));
        return mixColor(result, color(c, 3, 0xffffe8a3u), edge * glow * 0.32f);
    }

    uint32_t generatePixel(
            int textureType,
            NoiseBank &noise,
            float x,
            float y,
            int seed,
            int width,
            int height,
            const std::vector<float> &parameters,
            const std::vector<uint32_t> &colors
    ) {
        switch (textureType) {
            case 0:
                return lava(noise, x, y, parameters, colors);
            case 1:
                return clouds(noise, x, y, parameters, colors);
            case 2:
                return smoke(noise, x, y, parameters, colors);
            case 3:
                return stone(noise, x, y, parameters, colors);
            case 4:
                return wood(noise, x, y, parameters, colors);
            case 5:
                return camouflage(noise, x, y, parameters, colors);
            case 6:
                return paper(noise, x, y, seed, parameters, colors);
            case 7:
                return rust(noise, x, y, parameters, colors);
            case 8:
                return fabric(noise, x, y, seed, parameters, colors);
            case 9:
                return topography(noise, x, y, parameters, colors);
            case 10:
                return cells(noise, x, y, parameters, colors);
            case 11:
                return cracks(noise, x, y, parameters, colors);
            case 12:
                return waterRipples(noise, x, y, parameters, colors);
            case 13:
                return foliage(noise, x, y, parameters, colors);
            case 14:
                return bricks(noise, x, y, parameters, colors);
            case 15:
                return terrain(noise, x, y, parameters, colors);
            case 16:
                return ice(noise, x, y, seed, parameters, colors);
            case 17:
                return sand(noise, x, y, seed, parameters, colors);
            case 18:
                return nebula(noise, x, y, seed, parameters, colors);
            case 19:
                return honeycomb(noise, x, y, parameters, colors);
            case 20:
                return grass(noise, x, y, parameters, colors);
            case 21:
                return dirt(noise, x, y, seed, parameters, colors);
            case 22:
                return leather(noise, x, y, parameters, colors);
            case 23:
                return concrete(noise, x, y, seed, parameters, colors);
            case 24:
                return asphalt(noise, x, y, seed, parameters, colors);
            case 25:
                return moss(noise, x, y, seed, parameters, colors);
            case 26:
                return fire(noise, x, y, parameters, colors);
            case 27:
                return aurora(noise, x, y, seed, parameters, colors);
            case 28:
                return oilSlick(noise, x, y, parameters, colors);
            case 29:
                return watercolor(noise, x, y, seed, parameters, colors);
            case 30:
                return flowTexture(noise, x, y, parameters, colors);
            case 31: return opal(noise, x, y, parameters, colors);
            case 32: return damascus(noise, x, y, parameters, colors);
            case 33: return lightning(noise, x, y, parameters, colors);
            case 34: return velvet(noise, x, y, parameters, colors);
            case 35: return inkMarbling(noise, x, y, parameters, colors);
            case 36: return holographic(noise, x, y, parameters, colors);
            case 37: return bioluminescence(noise, x, y, parameters, colors);
            case 38: return cosmicVortex(noise, x, y, width, height, seed, parameters, colors);
            case 39: return lavaLamp(noise, x, y, parameters, colors);
            case 40:
                return eventHorizon(noise, x, y, width, height, seed, parameters, colors);
            case 41:
                return fractalBloom(noise, x, y, width, height, parameters, colors);
            case 42:
                return chromaticTunnel(noise, x, y, width, height, parameters, colors);
            case 43:
                return eclipseCorona(noise, x, y, width, height, parameters, colors);
            case 44:
                return strangeAttractor(noise, x, y, width, height, parameters, colors);
            case 45:
                return ferrofluidCrown(noise, x, y, width, height, parameters, colors);
            case 46:
                return supernova(noise, x, y, width, height, seed, parameters, colors);
            case 47:
                return iris(noise, x, y, width, height, parameters, colors);
            case 48:
                return peacockFeather(noise, x, y, width, height, parameters, colors);
            case 49:
                return nautilusShell(noise, x, y, width, height, parameters, colors);
            case 50:
                return ringedPlanet(noise, x, y, width, height, seed, parameters, colors);
            case 51:
            case 52:
            case 53:
            case 54:
            case 55:
            case 56:
            case 57:
            case 58:
            case 59:
            case 60:
            case 61:
            case 62:
            case 63:
            case 64:
            case 65:
            case 66:
            case 67:
            case 68:
            case 69:
            case 70:
                return showcaseTexture(textureType, noise, x, y, seed, width, height, parameters, colors);
            default:
                return 0xff000000u;
        }
    }

    jobject createBitmap(JNIEnv *env, const std::vector<uint32_t> &pixels, int width, int height) {
        jclass configClass = env->FindClass("android/graphics/Bitmap$Config");
        if (configClass == nullptr) return nullptr;
        jfieldID argbField = env->GetStaticFieldID(
                configClass,
                "ARGB_8888",
                "Landroid/graphics/Bitmap$Config;"
        );
        jobject argbConfig = env->GetStaticObjectField(configClass, argbField);
        jclass bitmapClass = env->FindClass("android/graphics/Bitmap");
        if (bitmapClass == nullptr) return nullptr;
        jmethodID createMethod = env->GetStaticMethodID(
                bitmapClass,
                "createBitmap",
                "(IILandroid/graphics/Bitmap$Config;)Landroid/graphics/Bitmap;"
        );
        jobject bitmap = env->CallStaticObjectMethod(bitmapClass, createMethod, width, height, argbConfig);
        if (bitmap == nullptr || env->ExceptionCheck()) return nullptr;

        AndroidBitmapInfo info{};
        if (AndroidBitmap_getInfo(env, bitmap, &info) != ANDROID_BITMAP_RESULT_SUCCESS) return nullptr;
        void *target = nullptr;
        if (AndroidBitmap_lockPixels(env, bitmap, &target) != ANDROID_BITMAP_RESULT_SUCCESS) return nullptr;
        for (int y = 0; y < height; ++y) {
            std::memcpy(
                    static_cast<uint8_t *>(target) + static_cast<size_t>(y) * info.stride,
                    pixels.data() + static_cast<size_t>(y) * width,
                    static_cast<size_t>(width) * sizeof(uint32_t)
            );
        }
        AndroidBitmap_unlockPixels(env, bitmap);
        return bitmap;
    }

}

extern "C"
JNIEXPORT jobject JNICALL
Java_com_t8rin_fast_1noise_texture_internal_ProceduralTextureNative_generate(
        JNIEnv *env,
        jobject,
        jint width,
        jint height,
        jint textureType,
        jint seed,
        jintArray colorArray,
        jfloatArray parameterArray
) {
    if (width <= 0 || height <= 0 || colorArray == nullptr || parameterArray == nullptr) return nullptr;
    int64_t pixelCount = static_cast<int64_t>(width) * height;
    if (pixelCount <= 0 || pixelCount > INT32_MAX) return nullptr;

    jsize colorCount = env->GetArrayLength(colorArray);
    std::vector<jint> javaColors(static_cast<size_t>(colorCount));
    env->GetIntArrayRegion(colorArray, 0, colorCount, javaColors.data());
    std::vector<uint32_t> colors;
    colors.reserve(javaColors.size());
    for (jint value: javaColors) colors.push_back(static_cast<uint32_t>(value));

    jsize parameterCount = env->GetArrayLength(parameterArray);
    std::vector<float> parameters(static_cast<size_t>(parameterCount));
    env->GetFloatArrayRegion(parameterArray, 0, parameterCount, parameters.data());
    if (env->ExceptionCheck()) return nullptr;

    NoiseBank noise(seed);
    std::vector<uint32_t> pixels(static_cast<size_t>(pixelCount));
    if (textureType >= 71 && textureType <= 121) {
        std::vector<float> field(static_cast<size_t>(pixelCount));
        for (int y = 0; y < height; ++y) {
            for (int x = 0; x < width; ++x) {
                field[static_cast<size_t>(y) * width + x] = simulationField(
                        textureType,
                        noise,
                        static_cast<float>(x),
                        static_cast<float>(y),
                        seed,
                        width,
                        height,
                        parameters
                );
            }
        }
        for (int y = 0; y < height; ++y) {
            for (int x = 0; x < width; ++x) {
                pixels[static_cast<size_t>(y) * width + x] = argbToBitmapRgba(shadeSimulation(
                        textureType,
                        field,
                        x,
                        y,
                        width,
                        height,
                        parameters,
                        colors
                ));
            }
        }
    } else {
        for (int y = 0; y < height; ++y) {
            for (int x = 0; x < width; ++x) {
                pixels[static_cast<size_t>(y) * width + x] = argbToBitmapRgba(generatePixel(
                        textureType,
                        noise,
                        static_cast<float>(x),
                        static_cast<float>(y),
                        seed,
                        width,
                        height,
                        parameters,
                        colors
                ));
            }
        }
    }
    return createBitmap(env, pixels, width, height);
}
