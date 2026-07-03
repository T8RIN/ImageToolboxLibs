#include <jni.h>
#include <android/bitmap.h>
#include <algorithm>
#include <cmath>
#include <cstdint>
#include <cstring>
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
    return createBitmap(env, pixels, width, height);
}
