#include "PatternTextures.h"
#include "TextureMath.h"
#include <cmath>
#include <limits>

namespace textures {
namespace {

constexpr float PI = 3.14159265358979323846f;
constexpr float SQRT3 = 1.7320508075688772f;

float dot(Point a, Point b) { return a.x * b.x + a.y * b.y; }
float length(Point p) { return std::sqrt(dot(p, p)); }
float cross(Point a, Point b) { return a.x * b.y - a.y * b.x; }
Point rotate(Point p, float angle) {
    float c = std::cos(angle), s = std::sin(angle);
    return {c * p.x - s * p.y, s * p.x + c * p.y};
}
float segmentDistance(Point p, Point a, Point b) {
    Point v = b - a;
    return length(p - a - v * std::clamp(dot(p - a, v) / std::max(dot(v, v), 0.000001f), 0.0f, 1.0f));
}
uint32_t premultiply(uint32_t color) {
    uint32_t alpha = color >> 24;
    return (alpha << 24) | ((((color >> 16) & 255) * alpha + 127) / 255 << 16) |
            ((((color >> 8) & 255) * alpha + 127) / 255 << 8) |
            (((color & 255) * alpha + 127) / 255);
}
float finiteValue(const std::vector<float> &p, size_t index, float fallback) {
    return index < p.size() && std::isfinite(p[index]) ? p[index] : fallback;
}
bool insideTriangle(Point p, Point a, Point b, Point c) {
    float ab = cross(b - a, p - a), bc = cross(c - b, p - b), ca = cross(a - c, p - c);
    return (ab >= -0.00001f && bc >= -0.00001f && ca >= -0.00001f) ||
            (ab <= 0.00001f && bc <= 0.00001f && ca <= 0.00001f);
}

}

PatternTexture::PatternTexture(int type, int seed, int width, int height,
                               const std::vector<float> &parameters,
                               const std::vector<uint32_t> &palette) :
        type(type), seed(seed), width(width), height(height), noise(seed) {
    scale = std::clamp(finiteValue(parameters, 0, 0.012f), 0.001f, 0.05f) *
            768.0f / static_cast<float>(std::min(width, height));
    aa = scale * 0.7f;
    extentX = width * scale * 0.5f;
    extentY = height * scale * 0.5f;
    for (size_t i = 0; i < values.size(); ++i) {
        values[i] = std::clamp(finiteValue(parameters, i + 1, 0.5f), 0.0f, 1.0f);
    }
    constexpr uint32_t defaults[]{0xff101b2bu, 0xff246b8du, 0xffdfad57u, 0xfff4ead3u};
    for (size_t i = 0; i < colors.size(); ++i) {
        colors[i] = premultiply(i < palette.size() ? palette[i] : defaults[i]);
    }
    noise.SetNoiseType(FastNoiseLite::NoiseType_OpenSimplex2S);
    noise.SetFrequency(1.0f);
    noise.SetFractalType(FastNoiseLite::FractalType_FBm);
    noise.SetFractalOctaves(3);
}

Point PatternTexture::position(int x, int y) const {
    return {(x + 0.5f - width * 0.5f) * scale, (y + 0.5f - height * 0.5f) * scale};
}

float PatternTexture::random(int x, int y) const {
    uint32_t value = static_cast<uint32_t>(x) * 0x9e3779b9u ^
            static_cast<uint32_t>(y) * 0x85ebca6bu ^ static_cast<uint32_t>(seed);
    value ^= value >> 16;
    value *= 0x7feb352du;
    value ^= value >> 15;
    value *= 0x846ca68bu;
    return static_cast<float>(value >> 8) / 16777215.0f;
}

float PatternTexture::grain(Point p, float frequency) const {
    return noise.GetNoise(p.x * frequency, p.y * frequency) * 0.5f + 0.5f;
}

uint32_t PatternTexture::shade(float light) const {
    light = clamp01(light);
    return light < 0.5f ? mixColor(colors[0], colors[1], light * 2.0f) :
            mixColor(colors[1], colors[3], (light - 0.5f) * 2.0f);
}

uint32_t PatternTexture::line(uint32_t background, float distance, float radius, uint32_t color) const {
    return mixColor(background, color, 1.0f - smoothstep(radius - aa, radius + aa, distance));
}

void PatternTexture::indexBounds(int index, Point minimum, Point maximum) {
    if (maximum.x < -extentX || minimum.x > extentX || maximum.y < -extentY || minimum.y > extentY) return;
    auto bx = [&](float x) { return std::clamp(static_cast<int>((x + extentX) * 32.0f / extentX), 0, 63); };
    auto by = [&](float y) { return std::clamp(static_cast<int>((y + extentY) * 32.0f / extentY), 0, 63); };
    for (int y = by(minimum.y); y <= by(maximum.y); ++y) {
        for (int x = bx(minimum.x); x <= bx(maximum.x); ++x) bins[y * 64 + x].push_back(index);
    }
}

const std::vector<int> &PatternTexture::nearby(Point p) const {
    int x = std::clamp(static_cast<int>((p.x + extentX) * 32.0f / extentX), 0, 63);
    int y = std::clamp(static_cast<int>((p.y + extentY) * 32.0f / extentY), 0, 63);
    return bins[y * 64 + x];
}

void PatternTexture::addSegment(Point a, Point b, float value) {
    int index = static_cast<int>(segments.size());
    segments.push_back({a, b, value});
    float radius = type == 142 ? 0.02f + values[3] * 0.13f :
            std::min(7.0f / ((1 << (2 + static_cast<int>(std::lround(values[0] * 5)))) - 1) *
                     (0.08f + values[1] * 0.32f), 0.23f);
    float padding = radius * 8 + aa;
    indexBounds(index, {std::min(a.x, b.x) - padding, std::min(a.y, b.y) - padding},
                {std::max(a.x, b.x) + padding, std::max(a.y, b.y) + padding});
}

bool PatternTexture::prepare(bool (*cancelled)()) {
    if (type == 122) {
        struct YarnPoint { float x; float y; float z; };
        constexpr int steps = 48;
        std::array<YarnPoint, steps + 1> loop{};
        for (int i = 0; i <= steps; ++i) {
            float t = static_cast<float>(i) / steps, angle = t * PI * 2;
            loop[i] = {t + 0.24f * std::sin(angle), 0.45f + 0.72f * std::cos(angle), 0.18f * std::sin(angle)};
        }
        float radius = 0.09f + values[0] * 0.16f;
        yarnTile.resize(yarnResolution * yarnResolution);
        for (int y = 0; y < yarnResolution; ++y) {
            if (cancelled()) return false;
            for (int x = 0; x < yarnResolution; ++x) {
                Point p{(x + 0.5f) / yarnResolution, (y + 0.5f) / yarnResolution};
                float upper = -10;
                YarnSample sample{};
                for (int row = -1; row <= 1; ++row) for (int column = -1; column <= 1; ++column) {
                    for (int i = 0; i < steps; ++i) {
                        const auto &a = loop[i], &b = loop[i + 1];
                        Point direction{b.x - a.x, b.y - a.y};
                        Point local{p.x - column - a.x, p.y - row - a.y};
                        float t = clamp01(dot(local, direction) / dot(direction, direction));
                        Point normal = local - direction * t;
                        float distance = length(normal);
                        float cover = 1 - smoothstep(radius - aa, radius + aa, distance);
                        if (cover <= 0) continue;
                        float nz = std::sqrt(std::max(0.0f, radius * radius - distance * distance));
                        float surface = a.z + (b.z - a.z) * t + nz;
                        if (surface <= upper) continue;
                        upper = surface;
                        float diffuse = std::max(0.0f, (-0.35f * normal.x - 0.45f * normal.y + 0.82f * nz) / radius);
                        float along = 2 * PI * (i + t) / steps;
                        float across = std::atan2(cross(normal, direction), nz * length(direction));
                        float fiber = 1 - values[1] * 0.16f * (0.5f + 0.5f * std::sin(34 * along + 5 * across));
                        sample = {cover, (0.2f + 0.8f * diffuse) * fiber, column, row};
                    }
                }
                yarnTile[y * yarnResolution + x] = sample;
            }
        }
    } else if (type == 130) {
        // Robinson triangle substitution; omitting the base joins triangles into Penrose rhombi.
        constexpr float golden = 1.618033988749895f;
        float radius = 48.0f;
        float angle = values[0] * PI * 2.0f + random(0, 0) * 0.2f;
        for (int i = 0; i < 10; ++i) {
            Point b = rotate({radius, 0}, (2 * i - 1) * PI / 10.0f + angle);
            Point c = rotate({radius, 0}, (2 * i + 1) * PI / 10.0f + angle);
            if (i % 2 == 0) std::swap(b, c);
            triangles.push_back({0, {0, 0}, b, c});
        }
        for (int level = 0; level < 8; ++level) {
            if (cancelled()) return false;
            std::vector<Triangle> next;
            next.reserve(triangles.size() * 3);
            for (const Triangle &t : triangles) {
                if (t.type == 0) {
                    Point p = t.a + (t.b - t.a) * (1.0f / golden);
                    next.push_back({0, t.c, p, t.b});
                    next.push_back({1, p, t.c, t.a});
                } else {
                    Point q = t.b + (t.a - t.b) * (1.0f / golden);
                    Point r = t.b + (t.c - t.b) * (1.0f / golden);
                    next.push_back({1, r, t.c, t.a});
                    next.push_back({1, q, r, t.b});
                    next.push_back({0, r, q, t.a});
                }
            }
            triangles = std::move(next);
        }
        for (int i = 0; i < static_cast<int>(triangles.size()); ++i) {
            if ((i & 1023) == 0 && cancelled()) return false;
            const Triangle &t = triangles[i];
            indexBounds(i, {std::min({t.a.x, t.b.x, t.c.x}), std::min({t.a.y, t.b.y, t.c.y})},
                        {std::max({t.a.x, t.b.x, t.c.x}), std::max({t.a.y, t.b.y, t.c.y})});
        }
    } else if (type == 142) {
        int a = 1 + static_cast<int>(std::lround(values[0] * 8.0f));
        int b = 1 + static_cast<int>(std::lround(values[1] * 8.0f));
        float phase = values[2] * PI * 2.0f + random(1, 1) * PI;
        Point radius{3.8f, 3.8f};
        auto point = [&](float t) { return Point{std::sin(a * t + phase) * radius.x, std::sin(b * t) * radius.y}; };
        constexpr int steps = 4096;
        Point previous = point(0);
        for (int i = 1; i <= steps; ++i) {
            if ((i & 255) == 0 && cancelled()) return false;
            float t = static_cast<float>(i) / steps;
            Point p = point(t * PI * 2.0f);
            addSegment(previous, p, t);
            previous = p;
        }
    } else if (type == 144) {
        int order = 2 + static_cast<int>(std::lround(values[0] * 5.0f));
        int side = 1 << order;
        float span = 7.0f;
        auto point = [&](int distance) {
            int x = 0, y = 0;
            for (int size = 1, t = distance; size < side; size *= 2, t /= 4) {
                int rx = (t / 2) & 1, ry = (t ^ rx) & 1;
                if (!ry) {
                    if (rx) { x = size - 1 - x; y = size - 1 - y; }
                    std::swap(x, y);
                }
                x += size * rx;
                y += size * ry;
            }
            return rotate({(static_cast<float>(x) / (side - 1) - 0.5f) * span,
                           (static_cast<float>(y) / (side - 1) - 0.5f) * span},
                          static_cast<int>(std::lround(values[3] * 3.0f)) * PI * 0.5f);
        };
        Point previous = point(0);
        for (int i = 1; i < side * side; ++i) {
            if ((i & 255) == 0 && cancelled()) return false;
            Point p = point(i);
            addSegment(previous, p, static_cast<float>(i) / (side * side));
            previous = p;
        }
    }
    return !cancelled();
}

uint32_t PatternTexture::material(Point p) const {
    float a = values[0], b = values[1], c = values[2], d = values[3], e = values[4];
    if (type == 122) {
        p = rotate(p, (d - 0.5f) * PI);
        int x = std::min(yarnResolution - 1, static_cast<int>(fract(p.x) * yarnResolution));
        int y = std::min(yarnResolution - 1, static_cast<int>(fract(p.y) * yarnResolution));
        const YarnSample &sample = yarnTile[y * yarnResolution + x];
        int column = static_cast<int>(std::floor(p.x)) + sample.column;
        int row = static_cast<int>(std::floor(p.y)) + sample.row;
        float light = clamp01(sample.light + (grain(p, 70) - 0.5f) * c * 0.16f);
        uint32_t yarn = mixColor(colors[1], colors[2], random(column, row) * e * 0.45f);
        yarn = light < 0.5f ? mixColor(colors[0], yarn, light * 2) : mixColor(yarn, colors[3], (light - 0.5f) * 0.8f);
        return mixColor(colors[0], yarn, sample.coverage);
    }
    if (type == 123) {
        p = rotate(p, (d - 0.5f) * PI);
        int x = static_cast<int>(std::floor(p.x)), y = static_cast<int>(std::floor(p.y));
        bool over = ((x + y) & 1) == 0;
        float local = fract(over ? p.y : p.x) - 0.5f;
        float width = 0.25f + a * 0.22f;
        float edge = smoothstep(width - aa, width + aa, std::abs(local));
        float fibers = std::sin((over ? p.y : p.x) * (35.0f + b * 65.0f)) * 0.07f;
        float light = 0.46f + 0.3f * std::cos(local / width * PI) + fibers + (grain(p, 20) - 0.5f) * c * 0.2f;
        uint32_t base = mixColor(colors[1], colors[2], over ? 0.15f + random(x, y) * e * 0.3f : 0.7f);
        base = mixColor(base, colors[3], clamp01(light - 0.4f));
        return mixColor(base, colors[0], edge * 0.95f);
    }
    if (type == 124) {
        float radius = 0.33f + a * 0.15f, tube = 0.035f + b * 0.08f;
        float distance = 10, light = 0;
        int row = static_cast<int>(std::floor(p.y / 0.64f));
        int column = static_cast<int>(std::floor(p.x));
        for (int oy = -1; oy <= 1; ++oy) {
            int ry = row + oy;
            for (int ox = -1; ox <= 1; ++ox) {
                Point q{p.x - column - ox - (ry & 1) * 0.5f, (p.y - ry * 0.64f) * (1.1f + c * 0.5f)};
                float angle = std::atan2(q.y, q.x);
                float sd = std::abs(length(q) - radius);
                float weave = std::sin(angle * 2.0f + (ry & 1) * PI);
                sd += std::max(weave, 0.0f) * tube * d;
                if (sd < distance) {
                    distance = sd;
                    light = 0.45f + 0.25f * std::cos(angle - 2.3f) +
                            0.35f * std::pow(clamp01(1.0f - sd / tube), 2.0f + e * 10.0f);
                }
            }
        }
        return line(colors[0], distance, tube, shade(light));
    }
    if (type == 125) {
        p = rotate(p, PI * 0.25f + (d - 0.5f) * PI * 0.5f);
        int size = 2 + static_cast<int>(a * 5.0f);
        int x = static_cast<int>(std::floor(p.x)), y = static_cast<int>(std::floor(p.y));
        int band = ((x + y) % (2 * size) + 2 * size) % (2 * size);
        bool horizontal = band < size;
        int plank = horizontal ? x - band : y - (band - size);
        float u = horizontal ? p.x : p.y, v = horizontal ? p.y : p.x;
        float crossGrain = fract(v);
        float end = horizontal ? fract((u + y) / size) * size : fract((u + x - size) / size) * size;
        float edge = std::min({crossGrain, 1.0f - crossGrain, end, size - end});
        float wood = 0.5f + 0.5f * std::sin(v * 65.0f + grain({u * 0.15f, v * 2}, 1) * 16.0f);
        uint32_t base = mixColor(colors[1], colors[2], random(plank, horizontal ? y : x) * 0.7f + wood * c * 0.3f);
        base = mixColor(base, colors[3], std::pow(wood, 7.0f) * e * 0.18f);
        return line(base, edge, 0.008f + b * 0.07f, colors[0]);
    }
    if (type == 126) {
        int row = static_cast<int>(std::floor(p.y / 0.68f));
        Point q{fract(p.x + (row & 1) * 0.5f) - 0.5f, fract(p.y / 0.68f) * 0.68f};
        float curve = std::sqrt(std::max(0.0f, 0.25f - q.x * q.x));
        float rim = std::abs(q.y - (0.16f + curve * (0.4f + a * 0.4f)));
        float edge = std::min(0.5f - std::abs(q.x), rim);
        float light = 0.45f + 0.3f * std::cos(q.x * PI * 2) * c - q.y * d * 0.15f;
        uint32_t base = mixColor(colors[1], colors[2], random(static_cast<int>(std::floor(p.x + (row & 1) * 0.5f)), row) * e);
        base = mixColor(base, colors[3], light * 0.3f + (grain(p, 35) - 0.5f) * b * 0.1f);
        return line(base, edge, 0.012f + b * 0.035f, colors[0]);
    }
    if (type == 127) {
        int x = static_cast<int>(std::floor(p.x)), y = static_cast<int>(std::floor(p.y));
        float best = 10, lx = 0, ly = 0;
        for (int oy = -1; oy <= 1; ++oy) for (int ox = -1; ox <= 1; ++ox) {
            Point center{x + ox + 0.5f + (random(x + ox, y + oy) - 0.5f) * a,
                         y + oy + 0.5f + (random(x + ox + 291, y + oy) - 0.5f) * a};
            Point q = p - center;
            float dist = length(q);
            if (dist < best) { best = dist; lx = q.x; ly = q.y; }
        }
        float normal = (lx * -0.55f + ly * -0.8f) / std::max(0.2f, best);
        float light = clamp01(0.48f + normal * b * 0.35f + std::pow(clamp01(1.0f - std::abs(best - 0.45f) * 5), 4) * c * 0.35f);
        light += (grain(p, 60) - 0.5f) * d * 0.15f;
        uint32_t result = shade(light);
        return mixColor(result, colors[2], grain(p, 0.4f) * e * 0.55f);
    }
    if (type == 128) {
        p = rotate(p, (a - 0.5f) * PI);
        float wave = std::sin(p.x * PI * 2);
        float normal = std::cos(p.x * PI * 2);
        float light = clamp01(0.42f + normal * 0.28f + std::pow(clamp01(wave * 0.5f + 0.5f), 3 + b * 20) * c * 0.5f);
        light += (grain(p, 45) - 0.5f) * d * 0.16f;
        float seam = std::min(fract(p.y / 6), 1 - fract(p.y / 6)) * 6;
        return line(mixColor(shade(light), colors[2], grain(p, 0.3f) * e * 0.45f), seam, 0.015f, colors[0]);
    }
    if (type == 129) {
        int row = static_cast<int>(std::floor(p.y / 0.8660254f));
        Point q{fract(p.x + (row & 1) * 0.5f) - 0.5f,
                (fract(p.y / 0.8660254f) - 0.5f) * 0.8660254f};
        float radius = 0.27f + a * 0.19f, dist = length(q), height = std::sqrt(clamp01(1 - dist * dist / (radius * radius)));
        float gleam = std::exp(-length(q - Point{-radius * 0.32f, -radius * 0.35f}) * (22 + c * 25));
        uint32_t result = mixColor(colors[0], colors[1], 0.3f + height * b * 0.5f);
        result = line(result, std::abs(dist - radius), 0.008f + d * 0.025f, colors[2]);
        return mixColor(result, colors[3], (gleam * 0.9f + std::pow(height, 12) * 0.2f) * (0.3f + e * 0.7f));
    }
    return colors[0];
}

uint32_t PatternTexture::ornament(Point p) const {
    float a = values[0], b = values[1], c = values[2], d = values[3], e = values[4];
    if (type == 130) {
        for (int index : nearby(p)) {
            const Triangle &t = triangles[index];
            if (!insideTriangle(p, t.a, t.b, t.c)) continue;
            float edge = std::min(segmentDistance(p, t.a, t.b), segmentDistance(p, t.a, t.c));
            uint32_t fill = t.type ? colors[1] : colors[2];
            fill = mixColor(fill, colors[3], grain(p, 3) * c * 0.22f);
            float inset = 0.1f + d * 0.2f;
            fill = line(fill, std::abs(edge - inset), 0.007f + e * 0.012f, colors[3]);
            return line(fill, edge, 0.01f + b * 0.07f, colors[0]);
        }
        return colors[0];
    }
    if (type == 131) {
        p = rotate(p, a * PI * 0.5f);
        constexpr Point polygon[]{{-5.0f / 3, -1}, {-1.0f / 3, -1}, {0, 0}, {-1, 1.0f / 3}, {-2, 0}};
        int cx = static_cast<int>(std::round(p.x / 2)), cy = static_cast<int>(std::round(p.y / 2));
        for (int oy = -1; oy <= 1; ++oy) for (int ox = -1; ox <= 1; ++ox) {
            int gx = cx + ox, gy = cy + oy;
            Point q = p - Point{gx * 2.0f, gy * 2.0f};
            if ((gx + gy) & 1) q.y = -q.y;
            for (int rotation = 0; rotation < 4; ++rotation) {
                Point r = rotate(q, rotation * PI * 0.5f);
                if (r.x < -2 - aa || r.x > aa || r.y < -1 - aa || r.y > 0.334f + aa) continue;
                float edge = 10;
                bool inside = true;
                for (int i = 0; i < 5; ++i) {
                    Point v = polygon[(i + 1) % 5] - polygon[i];
                    if (cross(v, r - polygon[i]) < -0.00001f) { inside = false; break; }
                    edge = std::min(edge, segmentDistance(r, polygon[i], polygon[(i + 1) % 5]));
                }
                if (!inside) continue;
                float variation = random(gx * 4 + rotation, gy);
                uint32_t fill = mixColor(colors[1], colors[2], variation * (0.2f + c * 0.8f));
                fill = mixColor(fill, colors[3], grain(p, 35) * d * 0.15f);
                fill = line(fill, std::abs(edge - 0.11f), 0.006f + e * 0.01f, colors[3]);
                return line(fill, edge, 0.015f + b * 0.07f, colors[0]);
            }
        }
        return colors[0];
    }
    if (type == 132) {
        Point q{fract(p.x) - 0.5f, fract(p.y) - 0.5f};
        float angle = std::atan2(q.y, q.x), radius = length(q);
        float sector = std::fmod(angle + PI * 4, PI / 4) - PI / 8;
        float outer = (0.39f + a * 0.08f) * std::cos(PI / 8) / std::cos(sector);
        float star = std::min(std::max(std::abs(q.x), std::abs(q.y)),
                              std::max(std::abs(q.x + q.y), std::abs(q.x - q.y)) / 1.41421356f);
        float edge = std::abs(star - (0.22f + c * 0.1f));
        uint32_t result = radius < outer ? colors[1] : colors[2];
        result = line(result, edge, 0.012f + b * 0.035f, colors[3]);
        result = line(result, std::abs(radius - outer), 0.01f + b * 0.025f, colors[0]);
        return mixColor(result, colors[3], grain(p, 45) * d * 0.06f + e * 0.03f);
    }
    if (type == 133) {
        p = p + Point{random(133, 0), random(0, 133)} * 2;
        p = rotate(p, PI * 0.25f + (a - 0.5f) * PI * 0.5f);
        float wave = 0.22f + c * 0.05f;
        float phaseX = p.x * PI, phaseY = p.y * PI;
        float one = std::abs(std::sin(phaseY) - wave * std::sin(phaseX)) / PI;
        float two = std::abs(std::sin(phaseX) + wave * std::sin(phaseY)) / PI;
        int x = int(std::floor(p.x + 0.5f)), y = int(std::floor(p.y + 0.5f));
        bool over = ((x + y) & 1) == 0;
        float lower = over ? one : two, upper = over ? two : one;
        float radius = 0.035f + b * 0.065f;
        uint32_t result = line(colors[0], lower, radius, colors[1]);
        result = line(result, std::abs(lower - radius * 0.57f), 0.006f + c * 0.006f, colors[3]);
        result = line(result, upper, radius + 0.025f + d * 0.02f, colors[0]);
        result = line(result, upper, radius, colors[2]);
        return line(result, std::abs(upper - radius * 0.57f), 0.005f + e * 0.008f, colors[3]);
    }
    if (type == 134) {
        p = rotate(p, (a - 0.5f) * PI);
        int row = static_cast<int>(std::floor(p.y / 0.5f));
        uint32_t result = colors[0];
        float best = 10;
        int rings = 3 + static_cast<int>(c * 5);
        for (int oy = 0; oy <= 2; ++oy) {
            int y = row + oy;
            float centerX = std::floor(p.x - (y & 1) * 0.5f) + (y & 1) * 0.5f + 0.5f;
            for (int ox = -1; ox <= 1; ++ox) {
                Point q = p - Point{centerX + ox, y * 0.5f};
                if (q.y > 0.0f || length(q) > 0.72f) continue;
                float r = length(q);
                float dist = std::abs(fract(r * rings / 0.72f) - 0.5f) * 0.72f / rings;
                if (r < best) {
                    best = r;
                    result = mixColor(colors[1], colors[2], d * r);
                    result = line(result, dist, 0.008f + b * 0.025f, colors[3]);
                    result = line(result, std::abs(r - 0.72f), 0.012f, colors[0]);
                }
            }
        }
        return mixColor(result, colors[3], grain(p, 60) * e * 0.08f);
    }
    if (type == 135) {
        p = rotate(p, a * PI / 3);
        float row = std::round(p.y / (SQRT3 * 0.5f));
        float column = std::round(p.x - row * 0.5f);
        float distance = 10, detail = 10;
        for (int oy = -1; oy <= 1; ++oy) for (int ox = -1; ox <= 1; ++ox) {
            Point center{column + ox + (row + oy) * 0.5f, (row + oy) * SQRT3 * 0.5f};
            if (length(p - center) > 1.1f) continue;
            for (int i = 0; i < 6; ++i) {
                Point end = center + rotate({1, 0}, i * PI / 3);
                Point middle = center + rotate({0.5f, 0}, i * PI / 3) + rotate({0, SQRT3 / 6}, i * PI / 3);
                distance = std::min(distance, segmentDistance(p, center, end));
                detail = std::min({detail, segmentDistance(p, center, middle), segmentDistance(p, end, middle)});
            }
        }
        uint32_t result = mixColor(colors[0], colors[1], c * 0.6f);
        result = line(result, detail, 0.008f + b * 0.018f, colors[2]);
        result = line(result, distance, 0.012f + b * 0.025f, colors[3]);
        return mixColor(result, colors[1], grain(p, 35) * (d + e) * 0.06f);
    }
    if (type == 136) {
        p = rotate(p, (a - 0.5f) * PI * 0.5f) * 8;
        int x = static_cast<int>(std::floor(p.x)), y = static_cast<int>(std::floor(p.y));
        bool over = (((x - y) % 4) + 4) % 4 < 2;
        bool dark = over ? ((x % 8) + 8) % 8 < 4 : ((y % 8) + 8) % 8 < 4;
        float thread = std::sin(p.x * (35 + b * 45)) * std::sin(p.y * (35 + b * 45));
        uint32_t result = dark ? colors[0] : colors[2];
        result = mixColor(result, dark ? colors[1] : colors[3], clamp01(0.2f + thread * c * 0.15f));
        return mixColor(result, colors[3], grain(p, 55) * d * 0.12f + e * 0.02f);
    }
    if (type == 137) {
        float distance = 10;
        int cx = static_cast<int>(std::floor(p.x)), cy = static_cast<int>(std::floor(p.y));
        float radius = 0.08f + a * 0.11f;
        for (int axis = 0; axis < 2; ++axis) {
            Point q = axis ? Point{p.y, p.x} : p;
            int x = axis ? cy : cx, y = axis ? cx : cy;
            for (int offset = 0; offset <= 1; ++offset) {
                int edge = x + offset;
                float sign = random(edge * 2 + axis, y) > 0.5f ? 1.0f : -1.0f;
                float center = 0.5f + (random(edge + 71, y * 2 + axis) - 0.5f) * b * 0.25f;
                float t = q.y - y - center;
                float dx = q.x - edge;
                float straight = std::hypot(dx, std::max(0.0f, radius * 0.6f - std::abs(t)));
                Point local{dx - sign * radius * 0.65f, t};
                float circle = std::abs(length(local) - radius);
                if (sign * local.x < -radius * 0.65f) circle = 10;
                distance = std::min({distance, straight, circle});
            }
        }
        uint32_t result = mixColor(colors[1], colors[2], grain(p, 0.5f) * c + (1 - c) * 0.4f);
        result = mixColor(result, colors[3], grain(p, 45) * d * 0.12f);
        result = line(result, std::abs(distance - 0.035f), 0.012f, mixColor(result, colors[3], e));
        return line(result, distance, 0.008f + values[4] * 0.018f, colors[0]);
    }
    return colors[0];
}

uint32_t PatternTexture::organic(Point p) const {
    float a = values[0], b = values[1], c = values[2], d = values[3], e = values[4];
    if (type == 138) {
        Point q{p.x + (grain({p.x * 0.5f, p.y * 0.14f}) - 0.5f) * (0.5f + a * 2), p.y * 0.1f};
        float furrow = grain(q, 2.2f), fine = grain({q.x * 5, p.y * 0.3f}, 2);
        float ridge = std::pow(clamp01(furrow), 0.6f + b * 2.5f);
        float light = clamp01(0.12f + smoothstep(0.3f, 0.66f, ridge) * 0.65f + (fine - 0.5f) * c * 0.45f);
        uint32_t result = shade(light);
        float cracks = std::pow(1.0f - fine, 8) * d;
        result = mixColor(result, colors[0], cracks);
        return mixColor(result, colors[2], smoothstep(0.52f, 0.75f, grain({p.x, p.y * 0.5f}, 1.6f)) * e * 0.65f);
    }
    if (type == 139) {
        float rowHeight = 0.55f + a * 0.2f;
        int row = int(std::floor(p.y / rowHeight));
        uint32_t result = colors[0];
        for (int offset = 1; offset >= -1; --offset) {
            int gy = row + offset;
            float stagger = (gy & 1) * 0.5f;
            int gx = int(std::floor(p.x - stagger));
            Point q{fract(p.x - stagger) - 0.5f, p.y / rowHeight - gy};
            float edge = 0.65f + 0.46f * std::sqrt(clamp01(1 - q.x * q.x * 4));
            if (q.y < 0 || q.y > edge) continue;
            float grainLine = 0.5f + 0.5f * std::sin(q.x * (65 + c * 75) + grain(p, 14) * 3);
            float rim = edge - q.y;
            float light = clamp01(0.16f + (q.y / edge) * b * 0.4f + 0.2f * std::cos(q.x * PI * 2) + grainLine * c * 0.1f);
            uint32_t scale = mixColor(shade(light), colors[2], random(gx, gy) * d * 0.25f);
            scale = mixColor(scale, colors[3], std::exp(-rim * 30) * (0.2f + e * 0.5f));
            result = line(scale, rim, 0.025f + e * 0.02f, colors[0]);
        }
        return result;
    }
    if (type == 140) {
        p = rotate(p, (a - 0.5f) * PI);
        float bend = (grain({p.x * 0.35f, p.y * 0.65f}, 0.8f) - 0.5f) * (1 + b * 5);
        float stripe = std::sin((p.x + bend) * (4 + c * 5) + grain(p, 1.5f) * d * 4);
        float mask = smoothstep(-aa * 8, aa * 8, stripe - (e - 0.5f));
        uint32_t dark = mixColor(colors[0], colors[1], grain(p, 60) * 0.15f);
        uint32_t light = mixColor(colors[2], colors[3], grain(p, 30) * 0.3f);
        return mixColor(dark, light, mask);
    }
    if (type == 141) {
        int x = static_cast<int>(std::floor(p.x)), y = static_cast<int>(std::floor(p.y));
        float best = 10;
        Point local{};
        int gx = 0, gy = 0;
        for (int oy = -1; oy <= 1; ++oy) for (int ox = -1; ox <= 1; ++ox) {
            Point q = p - Point{x + ox + 0.5f + (random(x + ox, y + oy) - 0.5f) * a * 0.7f,
                               y + oy + 0.5f + (random(x + ox + 17, y + oy) - 0.5f) * a * 0.7f};
            if (length(q) < best) { best = length(q); local = q; gx = x + ox; gy = y + oy; }
        }
        float angle = std::atan2(local.y, local.x);
        float radius = 0.2f + b * 0.17f + std::sin(angle * (3 + std::floor(c * 4)) + random(gx, gy) * PI * 2) * 0.055f;
        float ring = std::abs(best - radius);
        float broken = std::sin(angle * 3 + random(gx + 43, gy) * PI * 2);
        uint32_t base = mixColor(colors[1], colors[3], grain(p, 30) * e * 0.3f);
        if (best < radius) base = mixColor(base, colors[2], 0.65f);
        if (broken > -0.85f + d * 0.8f) base = line(base, ring, 0.035f + c * 0.04f, colors[0]);
        return base;
    }
    return colors[0];
}

uint32_t PatternTexture::curves(Point p) const {
    float a = values[0], b = values[1], c = values[2], d = values[3], e = values[4];
    if (type == 143) {
        p = rotate(p, (a - 0.5f) * PI * 0.5f);
        int x = static_cast<int>(std::floor(p.x / 3)), y = static_cast<int>(std::floor(p.y / 3));
        float distance = 10, terrace = 0, face = 0;
        for (int oy = -1; oy <= 1; ++oy) for (int ox = -1; ox <= 1; ++ox) {
            int gx = x + ox, gy = y + oy;
            Point q = p - Point{(gx + 0.5f) * 3 + (random(gx, gy) - 0.5f) * b,
                               (gy + 0.5f) * 3 + (random(gx + 51, gy) - 0.5f) * b};
            q = rotate(q, random(gx, gy) * PI * 0.5f);
            float square = std::max(std::abs(q.x), std::abs(q.y));
            if (square < distance) { distance = square; terrace = square * (5 + c * 12); face = q.x > q.y ? 1.0f : 0.0f; }
        }
        float step = fract(terrace);
        float rainbow = fract(std::floor(terrace) * (0.08f + d * 0.15f) + random(x, y) * 0.2f);
        uint32_t result = rainbow < 0.5f ? mixColor(colors[1], colors[2], rainbow * 2) : mixColor(colors[2], colors[3], (rainbow - 0.5f) * 2);
        result = mixColor(result, colors[0], (face * 0.18f + (1 - step) * 0.25f) * e);
        float edgeAA = aa * (5 + c * 12);
        return mixColor(result, colors[3], 1 - smoothstep(0.055f - edgeAA, 0.055f + edgeAA, std::min(step, 1 - step)));
    }
    float distance = std::numeric_limits<float>::max(), progress = 0;
    for (int index : nearby(p)) {
        const Segment &segment = segments[index];
        float sd = segmentDistance(p, segment.a, segment.b);
        if (sd < distance) { distance = sd; progress = segment.value; }
    }
    float radius = type == 142 ? 0.02f + d * 0.13f :
            7.0f / ((1 << (2 + static_cast<int>(std::lround(a * 5)))) - 1) * (0.08f + b * 0.32f);
    radius = std::min(radius, 0.23f);
    uint32_t ribbon = progress < 0.5f ? mixColor(colors[1], colors[2], progress * 2) :
            mixColor(colors[2], colors[3], (progress - 0.5f) * 2);
    float glow = type == 142 ? e : c;
    uint32_t result = mixColor(colors[0], ribbon, std::exp(-distance / std::max(radius * 2.5f, 0.01f)) * glow * 0.35f *
                               (1 - smoothstep(radius * 6, radius * 8, distance)));
    result = line(result, distance, radius, ribbon);
    return line(result, distance, radius * 0.22f, mixColor(ribbon, colors[3], type == 142 ? 0.3f : e));
}

uint32_t PatternTexture::pixel(int x, int y) const {
    Point p = position(x, y);
    if (type <= 129) return material(p);
    if (type <= 137) return ornament(p);
    if (type <= 141) return organic(p);
    return curves(p);
}

uint32_t mazePixel(const Maze &maze, int x, int y, int width, int height,
                   float wallWidth, float padding, bool openings,
                   uint32_t wallColor, uint32_t backgroundColor) {
    float inset = std::min(width, height) * padding;
    float cellX = (width - inset * 2) / maze.columns, cellY = (height - inset * 2) / maze.rows;
    float px = x + 0.5f - inset, py = y + 0.5f - inset;
    float radius = std::min(cellX, cellY) * wallWidth * 0.5f;
    float distance = std::numeric_limits<float>::max();
    int column = std::clamp(static_cast<int>(std::floor(px / cellX)), 0, maze.columns - 1);
    int row = std::clamp(static_cast<int>(std::floor(py / cellY)), 0, maze.rows - 1);
    for (int oy = -1; oy <= 1; ++oy) for (int ox = -1; ox <= 1; ++ox) {
        int cx = column + ox, cy = row + oy;
        if (cx < 0 || cx >= maze.columns || cy < 0 || cy >= maze.rows) continue;
        uint8_t passages = maze.passages[cy * maze.columns + cx];
        Point local{px - cx * cellX, py - cy * cellY};
        if (!(passages & 1)) distance = std::min(distance, segmentDistance(local, {0, 0}, {cellX, 0}));
        if (!(passages & 8) && !(openings && cx == 0 && cy == 0)) {
            distance = std::min(distance, segmentDistance(local, {0, 0}, {0, cellY}));
        }
        if (cx == maze.columns - 1 && !(openings && cy == maze.rows - 1)) {
            distance = std::min(distance, segmentDistance(local, {cellX, 0}, {cellX, cellY}));
        }
        if (cy == maze.rows - 1) distance = std::min(distance, segmentDistance(local, {0, cellY}, {cellX, cellY}));
    }
    return mixColor(premultiply(backgroundColor), premultiply(wallColor),
                    1 - smoothstep(radius - 0.65f, radius + 0.65f, distance));
}

}
