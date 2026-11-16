#include "DecorativeTextures.h"
#include "TextureMath.h"
#include <cmath>
#include <numeric>

namespace textures {
namespace {

constexpr float PI = 3.14159265358979323846f;
constexpr float TAU = PI * 2;
enum Type {
    ChromaticPlaid = 145,
    Malachite = 146,
    Labradorite = 147,
    Seraphinite = 148,
    PorcelainCrackle = 149,
    RakuGlaze = 150,
    MotherOfPearl = 151,
    TemperedTitanium = 152,
    EndGrainMosaic = 153,
    Cork = 154,
    CutCrystalGlass = 155,
    Sashiko = 156,
    Bargello = 157,
    Kilim = 158,
    Brocade = 159,
    Batik = 160,
    Ikat = 161,
    EmbossedPaisley = 162,
    ArtDecoFans = 163,
    CloverLattice = 164,
    GuillocheRosettes = 165,
    GinkgoLeaves = 166,
    MonsteraCanopy = 167,
    LotusPond = 168,
    SeaAnemone = 169,
    Jellyfish = 170,
    ButterflyWings = 171,
    FungalGills = 172,
    Radiolaria = 173,
    SlotCanyon = 174,
    GlacialCrevasses = 175,
    SpiralPearls = 176,
    HopfFibration = 177,
    Spirograph = 178,
    ChiralPinwheels = 179,
    TruchetBlocks = 180,
    ImpossibleStairs = 181,
    WovenHexagons = 182,
    Gyroid = 183,
    Hypercube = 184,
    LenticularWaves = 185,
    StellarNursery = 186,
    PulsarJets = 187,
    GravitationalLensing = 188,
    NacreClouds = 189,
    MercuryDroplets = 190,
    SonicBoom = 191,
    RibbonVortex = 192,
    IridescentFoam = 193,
    LavaCrust = 194,
    AbyssalVent = 195,
};
float dot(Point a, Point b) { return a.x * b.x + a.y * b.y; }
float length(Point p) { return std::sqrt(dot(p, p)); }
Point rotate(Point p, float angle) {
    float c = std::cos(angle), s = std::sin(angle);
    return {p.x * c - p.y * s, p.x * s + p.y * c};
}
float segment(Point p, Point a, Point b) {
    Point d = b - a;
    return length(p - a - d * clamp01(dot(p - a, d) / std::max(dot(d, d), 0.0000001f)));
}
float cross(Point a, Point b) { return a.x * b.y - a.y * b.x; }
bool triangle(Point p, Point a, Point b, Point c) {
    float x = cross(b - a, p - a), y = cross(c - b, p - b), z = cross(a - c, p - c);
    return (x >= 0 && y >= 0 && z >= 0) || (x <= 0 && y <= 0 && z <= 0);
}
float finite(const std::vector<float> &p, size_t i, float fallback) {
    return i < p.size() && std::isfinite(p[i]) ? p[i] : fallback;
}
uint32_t premultiply(uint32_t color) {
    uint32_t a = color >> 24;
    return a << 24 | (((color >> 16 & 255) * a + 127) / 255) << 16 |
            (((color >> 8 & 255) * a + 127) / 255) << 8 | ((color & 255) * a + 127) / 255;
}
struct Vec3 {
    float x, y, z;
    Vec3 operator+(Vec3 q) const { return {x + q.x, y + q.y, z + q.z}; }
    Vec3 operator-(Vec3 q) const { return {x - q.x, y - q.y, z - q.z}; }
    Vec3 operator*(float s) const { return {x * s, y * s, z * s}; }
};
float dot(Vec3 a, Vec3 b) { return a.x * b.x + a.y * b.y + a.z * b.z; }
Vec3 unit(Vec3 p) { return p * (1 / std::sqrt(std::max(dot(p, p), 0.0000001f))); }
Vec3 turn(Vec3 p, float yaw, float pitch) {
    Point xz = rotate({p.x, p.z}, yaw), yz = rotate({p.y, xz.y}, pitch);
    return {xz.x, yz.x, yz.y};
}

}

DecorativeTexture::DecorativeTexture(int type, int seed, int width, int height,
                                   const std::vector<float> &parameters,
                                   const std::vector<uint32_t> &palette) :
        type(type), seed(seed), width(width), height(height), noise(seed) {
    scale = std::clamp(finite(parameters, 0, 0.008f), 0.001f, 0.05f) / 0.008f / std::min(width, height);
    aa = scale * 0.8f;
    extentX = width * scale * 0.5f;
    extentY = height * scale * 0.5f;
    for (size_t i = 0; i < values.size(); ++i) values[i] = std::clamp(finite(parameters, i + 1, i == 4 ? 1.05f : 0.5f), 0.0f, i == 4 ? 3.0f : 1.0f);
    for (size_t i = 0; i < colors.size(); ++i) colors[i] = premultiply(i < palette.size() ? palette[i] : 0);
    noise.SetNoiseType(FastNoiseLite::NoiseType_OpenSimplex2S);
    noise.SetFractalType(FastNoiseLite::FractalType_FBm);
    noise.SetFractalOctaves(4);
    noise.SetFrequency(1);
}

float DecorativeTexture::random(int x, int y) const {
    uint32_t v = uint32_t(x) * 0x9e3779b9u ^ uint32_t(y) * 0x85ebca6bu ^ uint32_t(seed);
    v ^= v >> 16; v *= 0x7feb352du; v ^= v >> 15; v *= 0x846ca68bu; v ^= v >> 16;
    return float(v >> 8) / 16777215.0f;
}
float DecorativeTexture::grain(Point p, float frequency) const {
    return noise.GetNoise(p.x * frequency, p.y * frequency) * 0.5f + 0.5f;
}
DecorativeTexture::Cell DecorativeTexture::cell(Point p) const {
    int x = int(std::floor(p.x)), y = int(std::floor(p.y));
    Cell result{10, 10, {}, 0, 0, 0};
    for (int oy = -1; oy <= 1; ++oy) for (int ox = -1; ox <= 1; ++ox) {
        int gx = x + ox, gy = y + oy;
        Point q = p - Point{gx + 0.15f + random(gx, gy) * 0.7f, gy + 0.15f + random(gx + 91, gy) * 0.7f};
        float d = length(q);
        if (d < result.first) {
            result.second = result.first; result.first = d; result.local = q;
            result.x = gx; result.y = gy; result.value = random(gx + 37, gy);
        } else result.second = std::min(result.second, d);
    }
    return result;
}
uint32_t DecorativeTexture::palette(float t) const {
    t = clamp01(t) * 3;
    int i = std::min(2, int(t));
    return mixColor(colors[i], colors[i + 1], t - i);
}
uint32_t DecorativeTexture::shade(uint32_t color, float light) const {
    light = clamp01(light);
    return light < 0.5f ? mixColor(colors[0], color, light * 2) : mixColor(color, colors[3], (light - 0.5f) * 2);
}
uint32_t DecorativeTexture::line(uint32_t background, uint32_t color, float distance, float radius) const {
    return mixColor(background, color, 1 - smoothstep(radius - aa, radius + aa, distance));
}
uint32_t DecorativeTexture::finish(uint32_t color) const {
    uint32_t alpha = color >> 24;
    float contrast = std::max(values[4], 0.05f);
    auto channel = [&](int shift) {
        float bg = ((colors[0] >> shift) & 255) * float(alpha) / std::max(float(colors[0] >> 24), 1.0f);
        return uint32_t(std::lround(std::clamp(bg + (float((color >> shift) & 255) - bg) * contrast, 0.0f, float(alpha))));
    };
    return alpha << 24 | channel(16) << 16 | channel(8) << 8 | channel(0);
}
void DecorativeTexture::bounds(int index, Point low, Point high) {
    if (high.x < -extentX || low.x > extentX || high.y < -extentY || low.y > extentY) return;
    auto bx = [&](float x) { return std::clamp(int((x + extentX) * 32 / extentX), 0, 63); };
    auto by = [&](float y) { return std::clamp(int((y + extentY) * 32 / extentY), 0, 63); };
    for (int y = by(low.y); y <= by(high.y); ++y) for (int x = bx(low.x); x <= bx(high.x); ++x) bins[y * 64 + x].push_back(index);
}
const std::vector<int> &DecorativeTexture::nearby(Point p) const {
    int x = std::clamp(int((p.x + extentX) * 32 / extentX), 0, 63);
    int y = std::clamp(int((p.y + extentY) * 32 / extentY), 0, 63);
    return bins[y * 64 + x];
}
void DecorativeTexture::stroke(Point a, Point b, float radius, float depth, float value) {
    int i = int(strokes.size()); strokes.push_back({a, b, radius, depth, value});
    float pad = radius * 6 + aa * 2;
    bounds(i, {std::min(a.x, b.x) - pad, std::min(a.y, b.y) - pad}, {std::max(a.x, b.x) + pad, std::max(a.y, b.y) + pad});
}
void DecorativeTexture::disc(Point p, float radius, float depth, float value) {
    int i = int(discs.size()); discs.push_back({p, radius, depth, value});
    bounds(i, p - Point{radius + aa, radius + aa}, p + Point{radius + aa, radius + aa});
}
void DecorativeTexture::face(Point a, Point b, Point c, Point d, float light, float value) {
    int i = int(faces.size()); faces.push_back({a, b, c, d, light, value});
    bounds(i, {std::min({a.x, b.x, c.x, d.x}), std::min({a.y, b.y, c.y, d.y})},
           {std::max({a.x, b.x, c.x, d.x}), std::max({a.y, b.y, c.y, d.y})});
}

bool DecorativeTexture::prepare(bool (*cancelled)()) {
    float a = values[0], b = values[1], c = values[2];
    if (type == EmbossedPaisley) {
        auto bezier = [](Point a, Point b, Point c, Point d, float t) {
            float u = 1 - t;
            return a * (u * u * u) + b * (3 * u * u * t) + c * (3 * u * t * t) + d * (t * t * t);
        };
        Point control[][4]{
                {{0, 0.34f}, {-0.36f, 0.28f}, {-0.33f, -0.1f}, {-0.12f, -0.18f}},
                {{-0.12f, -0.18f}, {0.04f, -0.24f}, {0.2f + b * 0.08f, -0.24f}, {0.23f, -0.4f}},
                {{0.23f, -0.4f}, {0.4f, -0.12f}, {0.28f, 0.02f}, {0.2f, 0.15f}},
                {{0.2f, 0.15f}, {0.16f, 0.27f}, {0.1f, 0.34f}, {0, 0.34f}}
        };
        std::vector<Point> polygon;
        for (const auto &curve : control) for (int i = 0; i < 24; ++i) polygon.push_back(bezier(curve[0], curve[1], curve[2], curve[3], i / 24.0f));
        motifTile.resize(256 * 256);
        for (int y = 0; y < 256; ++y) {
            if (cancelled()) return false;
            for (int x = 0; x < 256; ++x) {
                Point p{(x + 0.5f) / 256 - 0.5f, (y + 0.5f) / 256 - 0.5f};
                float distance = 10; bool inside = false;
                for (size_t i = 0, j = polygon.size() - 1; i < polygon.size(); j = i++) {
                    Point a = polygon[i], b = polygon[j];
                    distance = std::min(distance, segment(p, a, b));
                    if ((a.y > p.y) != (b.y > p.y) && p.x < (b.x - a.x) * (p.y - a.y) / (b.y - a.y) + a.x) inside = !inside;
                }
                uint32_t color = colors[0];
                if (inside) {
                    color = shade(mixColor(colors[1], colors[2], 0.3f), 0.28f + std::min(distance * 5, 0.5f) * values[3]);
                    Point q = p - Point{-0.015f, 0.11f};
                    float r = length(q), angle = std::atan2(q.y, q.x);
                    float rosette = std::abs(r - 0.085f - 0.014f * std::cos(angle * 9));
                    color = line(color, colors[3], rosette, 0.004f);
                    color = line(color, colors[2], std::abs(r - 0.045f), 0.003f);
                    float dots = std::pow(0.5f + std::cos(angle * 30) * 0.5f, 10);
                    color = mixColor(color, colors[3], std::exp(-std::abs(distance - 0.035f) * 160) * dots * values[3]);
                }
                motifTile[y * 256 + x] = line(color, colors[2], distance, 0.004f + values[2] * 0.008f);
            }
        }
    } else if (type == SeaAnemone || type == Jellyfish) {
        int count = type == SeaAnemone ? 36 + int(a * 70) : 12 + int(a * 35);
        for (int i = 0; i < count; ++i) {
            if (cancelled()) return false;
            float angle = i * TAU / count, variation = random(i, 8);
            Point previous{};
            for (int step = 0; step <= 90; ++step) {
                float t = step / 90.0f;
                Point p;
                if (type == SeaAnemone) {
                    float r = 0.07f + t * (0.23f + variation * 0.14f);
                    float twist = angle + std::sin(t * PI) * (variation - 0.5f) * (1 + b * 3);
                    p = {std::cos(twist) * r, 0.08f + std::sin(twist) * r * 0.83f};
                } else {
                    float x = (i / float(count - 1) - 0.5f) * 0.34f;
                    p = {x + std::sin(t * (8 + b * 12) + angle) * (0.01f + t * 0.055f),
                         -0.07f + t * (0.4f + variation * 0.15f)};
                }
                if (step) stroke(previous, p, (type == SeaAnemone ? 0.006f + c * 0.005f : 0.0008f + c * 0.004f) * (1 - t * 0.35f),
                                 variation * 0.1f, variation);
                previous = p;
            }
            if (type == SeaAnemone) stroke(previous, previous, 0.005f + c * 0.004f, variation * 0.1f + 0.2f, variation);
        }
    } else if (type == Spirograph) {
        int n = 2 + int(b * 5), m = n + 3 + int(a * 9);
        float radius = n / float(m), pen = radius * (1.7f + b * 0.6f);
        int divisor = std::gcd(n, m), steps = 2048 + (m / divisor) * 128;
        for (int track = 0; track < 4; ++track) {
            Point previous{};
            float trackPen = pen * (1 - track * 0.13f);
            for (int i = 0; i <= steps; ++i) {
                if ((i & 127) == 0 && cancelled()) return false;
                float t = i / float(steps) * TAU * n / divisor;
                float phase = (1 - radius) / radius * t;
                Point p{(1 - radius) * std::cos(t) + trackPen * std::cos(phase),
                        (1 - radius) * std::sin(t) - trackPen * std::sin(phase)};
                p = rotate(p, random(0, 112) * TAU) * (0.44f / (1 - radius + pen));
                if (i) stroke(previous, p, 0.001f + c * 0.005f, 0, fract(i / float(steps) + track * 0.18f));
                previous = p;
            }
        }
    } else if (type == HopfFibration) {
        int count = 10 + int(a * 28);
        float eta = 0.35f + b * 0.5f;
        for (int i = 0; i < count; ++i) {
            Point previous{}; float lastDepth = 0;
            for (int j = 0; j <= 360; ++j) {
                if ((j & 127) == 0 && cancelled()) return false;
                float t = j * TAU / 360, phase = i * TAU / count + random(0, 112) * TAU;
                float divisor = 1 - std::sin(eta) * std::sin(t);
                Vec3 q{std::cos(eta) * std::cos(t + phase) / divisor,
                       std::cos(eta) * std::sin(t + phase) / divisor, std::sin(eta) * std::cos(t) / divisor};
                q = turn(q, -0.45f, 0.8f) * (0.43f * std::sqrt((1 - std::sin(eta)) / (1 + std::sin(eta))));
                Point p{q.x, q.y};
                if (j) stroke(previous, p, 0.0015f + c * 0.0035f, (lastDepth + q.z) * 0.5f, i / float(count));
                previous = p; lastDepth = q.z;
            }
        }
    } else if (type == Hypercube) {
        Vec3 points[16];
        for (int i = 0; i < 16; ++i) {
            float x = (i & 1) ? 1 : -1, y = (i & 2) ? 1 : -1;
            float z = (i & 4) ? 1 : -1, w = (i & 8) ? 1 : -1;
            Point xw = rotate({x, w}, 0.25f + b * 1.2f + random(0, 112) * 0.2f), yz = rotate({y, z}, 0.6f + b * 0.45f);
            float perspective = 2.2f + a * 1.8f - xw.y;
            points[i] = turn({xw.x / perspective, yz.x / perspective, yz.y / perspective}, 0.35f, -0.25f) * 0.61f;
        }
        for (int i = 0; i < 16; ++i) for (int bit = 0; bit < 4; ++bit) {
            int j = i ^ (1 << bit); if (j < i) continue;
            stroke({points[i].x, points[i].y}, {points[j].x, points[j].y}, 0.002f + c * 0.005f,
                   (points[i].z + points[j].z) * 0.5f, bit / 3.0f);
        }
    } else if (type == SpiralPearls) {
        int arms = 3 + int(a * 6);
        for (int arm = 0; arm < arms; ++arm) for (int i = 0; i < 32; ++i) {
            float t = i / 31.0f, r = 0.035f * std::exp(t * std::log(13.0f));
            float angle = arm * TAU / arms + t * (3.5f + b * 5) + random(0, 112) * TAU;
            disc(rotate({r, 0}, angle), (0.004f + c * 0.012f) * (0.35f + t), r, arm / float(arms));
        }
    } else if (type == MercuryDroplets || type == IridescentFoam) {
        bool foam = type == IridescentFoam;
        int count = foam ? 140 + int(a * 140) : 18 + int(a * 65);
        for (int i = 0; i < count; ++i) {
            if (cancelled()) return false;
            int identity = foam ? i + 3109 : i;
            float radius = foam ? 0.015f + random(identity, 6) * (0.04f + b * 0.04f) :
                    0.025f + random(identity, 6) * (0.025f + b * 0.06f);
            Point center{};
            bool placed = false;
            int attempts = foam ? 100 : 70;
            for (int trial = 0; trial < attempts; ++trial) {
                if (foam && trial > 0 && trial % 25 == 0) radius = std::max(radius * 0.72f, 0.006f);
                center = {(random(identity, trial * 2 + 20) - 0.5f) * 1.05f, (random(identity, trial * 2 + 21) - 0.5f) * 1.05f};
                placed = true;
                for (const Disc &other : discs) {
                    if (length(center - other.center) < (radius + other.radius) * (foam ? 0.98f : 0.86f)) { placed = false; break; }
                }
                if (placed) break;
            }
            if (placed) disc(center, radius, random(identity, 7) * 0.2f, random(identity, 8));
        }
    } else if (type == ImpossibleStairs) {
        int count = 5 + int(a * 8);
        Point vertices[]{{-0.38f, 0}, {0, -0.23f}, {0.38f, 0}, {0, 0.23f}};
        for (Point &p : vertices) p = rotate(p, (b - 0.48f) * 0.8f + (random(0, 112) - 0.5f) * 0.12f);
        for (int side = 0; side < 4; ++side) {
            Point start = vertices[side], end = vertices[(side + 1) % 4];
            Point v = end - start, inward = Point{-v.y, v.x} * 0.22f;
            for (int i = 0; i < count; ++i) {
                float t = i / float(count), next = (i + 1) / float(count);
                Point lift{0, -(0.025f + c * 0.04f) * t};
                Point p = start + v * t + lift, q = start + v * next + lift;
                Point down{0, 0.025f + c * 0.025f};
                face(p + inward, q + inward, q - inward, p - inward, 0.72f, side / 4.0f);
                face(p - inward, q - inward, q - inward + down, p - inward + down, 0.24f, side / 4.0f);
                face(q + inward, q - inward, q - inward + down, q + inward + down, 0.42f, side / 4.0f);
            }
        }
    }
    return !cancelled();
}

uint32_t DecorativeTexture::curves(Point p) const {
    float best = 10, radius = 0.001f, depth = -10, value = 0, halo = 0;
    for (int i : nearby(p)) {
        const Stroke &s = strokes[i];
        float distance = segment(p, s.a, s.b);
        halo = std::max(halo, std::exp(-distance / std::max(s.radius * 2.5f, aa)));
        float surface = s.depth + std::sqrt(std::max(0.0f, s.radius * s.radius - distance * distance));
        if (distance < s.radius + aa && surface > depth) {
            depth = surface; best = distance; radius = s.radius; value = s.value;
        }
    }
    uint32_t result = mixColor(colors[0], colors[1], halo * values[3] * 0.35f);
    uint32_t color = mixColor(colors[1], colors[2], value);
    float tube = std::sqrt(clamp01(1 - best * best / (radius * radius)));
    color = mixColor(color, colors[3], tube * 0.6f);
    result = line(result, color, best, radius);
    if (type == Jellyfish) {
        Point q{p.x / (0.24f + values[1] * 0.05f), (p.y + 0.095f) / 0.2f};
        float r = length(q), angle = std::atan2(q.y, q.x);
        float bell = (1 - smoothstep(0.94f, 1.02f, r)) * (1 - smoothstep(-0.05f, 0.11f, p.y));
        float ribs = std::pow(0.5f + 0.5f * std::cos(angle * 17), 12) * (0.3f + r * 0.7f);
        uint32_t body = mixColor(colors[1], colors[2], ribs * 0.55f + grain(p, 12) * 0.2f);
        body = mixColor(body, colors[3], std::exp(-std::abs(r - 0.95f) * 50) * 0.85f + ribs * values[3] * 0.35f);
        result = mixColor(result, body, bell * (0.45f + values[2] * 0.5f));
    }
    if (type == SeaAnemone) {
        float center = length(p - Point{0, 0.08f});
        uint32_t mouth = mixColor(colors[0], colors[2], smoothstep(0.02f, 0.07f, center));
        result = mixColor(result, mouth, 1 - smoothstep(0.045f, 0.065f, center));
    }
    return result;
}

uint32_t DecorativeTexture::spheres(Point p) const {
    uint32_t result = colors[0]; float upper = -10;
    for (int index : nearby(p)) {
        const Disc &s = discs[index]; Point q = (p - s.center) * (1 / s.radius);
        float r = length(q); if (r > 1 + aa / s.radius) continue;
        float z = std::sqrt(clamp01(1 - r * r));
        if (s.depth + z * s.radius < upper) continue;
        upper = s.depth + z * s.radius;
        uint32_t body;
        if (type == IridescentFoam) {
            float phase = fract(r * (2 + values[2] * 4) + s.value + q.y * 0.2f);
            body = mixColor(colors[1], colors[2], 0.5f + std::sin(phase * TAU) * 0.5f);
            body = mixColor(colors[0], body, 0.15f + std::pow(r, 6) * 0.75f);
            body = mixColor(body, colors[3], std::pow(clamp01(-q.x * 0.35f - q.y * 0.45f + z * 0.7f), 28) * values[3]);
        } else {
            float reflection = 0.5f + 0.5f * std::sin((q.y * 0.55f + z * 0.7f) * 7);
            uint32_t tint = mixColor(colors[1], colors[2], type == SpiralPearls ? s.value : reflection);
            body = shade(tint, 0.16f + z * 0.42f + reflection * 0.18f);
            float key = std::pow(clamp01(-q.x * 0.42f - q.y * 0.5f + z * 0.75f), 12 + values[3] * 38);
            body = mixColor(body, colors[3], key * 0.9f + std::pow(r, 12) * values[2] * 0.25f);
        }
        result = mixColor(result, body, 1 - smoothstep(1 - aa / s.radius, 1 + aa / s.radius, r));
    }
    return result;
}

uint32_t DecorativeTexture::minerals(Point p) const {
    float a = values[0], b = values[1], c = values[2], d = values[3];
    switch (type) {
        case Malachite: {
            Point q = p * (3 + a * 4);
            q = q + Point{grain(p, 8) - 0.5f, grain(p + Point{3, 7}, 8) - 0.5f} * b * 0.45f;
            Cell v = cell(q);
            float rings = 0.5f + 0.5f * std::sin(v.first * (32 + a * 55) + grain(p, 35) * b * 5);
            uint32_t stone = mixColor(colors[1], colors[2], std::pow(rings, 0.55f));
            stone = mixColor(colors[0], stone, 0.35f + rings * 0.65f);
            return mixColor(stone, colors[3], std::pow(rings, 15) * (0.12f + c * 0.25f) + std::pow(grain(p, 100), 8) * d * 0.2f);
        }
        case Labradorite: {
            Cell v = cell(p * (6 + a * 10));
            Point q = rotate(v.local, v.value * TAU);
            float flash = smoothstep(0.15f, 0.85f, v.value + std::sin(q.x * (12 + b * 25)) * 0.15f);
            float striation = 0.5f + 0.5f * std::sin(q.x * 70 + q.y * 8);
            uint32_t stone = mixColor(colors[0], colors[1], 0.3f + striation * 0.3f);
            stone = mixColor(stone, mixColor(colors[2], colors[3], v.value * 0.28f), flash * (0.5f + d * 0.5f));
            return shade(stone, 0.4f + (q.x - q.y) * c * 0.3f + striation * 0.12f);
        }
        case Seraphinite: {
            Cell v = cell(p * (3 + a * 5));
            Point q = rotate(v.local, v.value * TAU);
            float curve = q.x + q.y * q.y * (1 + b * 3);
            float fibers = std::pow(0.5f + 0.5f * std::sin((curve + std::sin(q.y * 3) * 0.07f) * (90 + a * 100)), 4);
            float plume = std::exp(-std::abs(curve) * 4) * (0.2f + fibers * 0.8f);
            uint32_t stone = mixColor(colors[0], colors[1], grain(p, 13) * 0.65f);
            return mixColor(stone, mixColor(colors[2], colors[3], 0.6f + fibers * d * 0.4f), clamp01(plume * 1.6f) * (0.6f + c * 0.4f));
        }
        case PorcelainCrackle: {
            Point q = p * (11 + a * 14);
            q = q + Point{grain(p, 30) - 0.5f, grain(p + Point{5, 2}, 30) - 0.5f} * b * 0.6f;
            Cell v = cell(q);
            float crack = 1 - smoothstep(0.008f, 0.02f + c * 0.035f, v.second - v.first);
            uint32_t glaze = mixColor(colors[1], colors[2], v.value * 0.18f + grain(p, 75) * 0.08f);
            glaze = mixColor(glaze, colors[3], std::pow(grain(p, 3), 3) * d * 0.4f);
            return mixColor(glaze, colors[0], crack * 0.85f);
        }
        case RakuGlaze: {
            Cell v = cell(p * (8 + a * 14) + Point{grain(p, 12), grain(p + Point{4, 2}, 12)} * b);
            float film = 0.5f + std::sin(v.value * 15 + v.local.x * 3 - v.local.y * 5) * 0.5f;
            uint32_t glaze = mixColor(colors[1], colors[2], film);
            glaze = shade(glaze, 0.22f + v.first * 0.5f + grain(p, 45) * 0.2f);
            glaze = mixColor(glaze, colors[3], std::pow(film, 12) * d * 0.55f);
            return mixColor(colors[0], glaze, smoothstep(0.008f, 0.025f + c * 0.03f, v.second - v.first));
        }
        case MotherOfPearl: {
            Cell v = cell(p * (5 + a * 8));
            Point q = rotate(v.local, v.value * TAU);
            float phase = 0.5f + 0.5f * std::sin(q.x * 6 + q.y * q.y * 7 + v.value * 9 + grain(p, 8) * b);
            uint32_t shell = mixColor(colors[1], colors[2], phase * d);
            float growth = 0.5f + 0.5f * std::sin((q.x + q.y * q.y) * (45 + a * 50));
            shell = mixColor(shell, colors[3], growth * c * 0.25f + std::pow(phase, 9) * d * 0.45f);
            return mixColor(colors[0], shell, smoothstep(0.008f, 0.03f, v.second - v.first));
        }
        case TemperedTitanium: {
            Point q = rotate(p, 0.12f);
            float heat = q.x * 0.9f + grain({q.x, q.y * 0.5f}, 1.2f + a * 2) * 0.8f;
            heat += (grain(p, 6) - 0.5f) * b * 0.2f;
            float phase = fract(heat * 2.5f);
            uint32_t oxide = phase < 0.5f ? mixColor(colors[1], colors[2], phase * 2) : mixColor(colors[2], colors[1], phase * 2 - 1);
            float brushing = grain({q.x * 250, q.y * 2}, 2);
            oxide = shade(oxide, 0.42f + (brushing - 0.5f) * c * 0.15f);
            float reflection = std::pow(clamp01(0.5f + std::sin(q.x * 7 + q.y * 1.3f) * 0.5f), 20);
            return mixColor(oxide, colors[3], reflection * d * 0.8f);
        }
        case EndGrainMosaic: {
            Point q = rotate(p, 0.12f) * (4 + a * 6);
            int x = int(std::floor(q.x)), y = int(std::floor(q.y));
            Point local{fract(q.x) - 0.5f, fract(q.y) - 0.5f};
            Point center{(random(x, y) - 0.5f) * 0.8f, (random(x + 13, y) - 0.5f) * 0.8f};
            float r = length(local - center) + (grain(q, 5) - 0.5f) * c * 0.08f;
            float ring = 0.5f + 0.5f * std::sin(r * (35 + b * 70));
            uint32_t wood = mixColor(colors[1], colors[2], random(x + 7, y) * 0.5f + ring * 0.35f);
            wood = mixColor(wood, colors[3], std::pow(ring, 12) * d * 0.3f);
            float edge = std::min(0.5f - std::abs(local.x), 0.5f - std::abs(local.y));
            return mixColor(colors[0], wood, smoothstep(0.008f, 0.025f, edge));
        }
        case Cork: {
            Cell v = cell(p * (28 + a * 38));
            float pore = grain(p, 350), edge = v.second - v.first;
            uint32_t chip = mixColor(colors[1], colors[2], v.value * 0.7f + pore * 0.2f);
            chip = mixColor(chip, colors[0], smoothstep(0.7f - c * 0.15f, 0.9f, pore) * 0.75f);
            chip = shade(chip, 0.4f + v.local.x * d * 0.15f - v.local.y * d * 0.2f);
            return mixColor(colors[0], chip, smoothstep(0.005f, 0.025f + b * 0.03f, edge));
        }
        case CutCrystalGlass: {
            Point q = p * (4 + a * 5) + Point{random(0, 112), random(1, 112)};
            q = {fract(q.x) - 0.5f, fract(q.y) - 0.5f};
            float r = length(q), angle = std::atan2(q.y, q.x);
            float cuts = 8 + std::floor(b * 8) * 2;
            float facet = fract(angle / TAU * cuts), ring = std::floor(r * 7) / 7;
            uint32_t glass = mixColor(colors[1], colors[2], clamp01(facet * 0.65f + ring * 0.35f));
            glass = shade(glass, 0.15f + facet * c * 0.55f + ring * 0.25f);
            float gleam = std::pow(1 - std::abs(facet * 2 - 1), 16) * d;
            glass = mixColor(glass, colors[3], gleam * 0.75f + std::exp(-r * 30) * 0.7f);
            return mixColor(colors[0], glass, 1 - smoothstep(0.47f, 0.49f, r));
        }
        default: return colors[0];
    }
}

uint32_t DecorativeTexture::textiles(Point p) const {
    float a = values[0], b = values[1], c = values[2], d = values[3];
    if (type == ChromaticPlaid) {
        p = rotate(p, (d - 0.5f) * PI) * 12;
        int x = int(std::floor(p.x)), y = int(std::floor(p.y));
        auto color = [&](float t) {
            t *= 4; int i = std::min(3, int(t));
            return mixColor(colors[i], colors[(i + 1) % 4], fract(t));
        };
        uint32_t warp = color(random(x, 1)), weft = color(random(y, 1));
        float over = fract((p.x + p.y) * (6 + b * 12)) > 0.5f ? 1 : 0;
        uint32_t result = mixColor(mixColor(warp, weft, 0.5f), mixColor(warp, weft, over), 0.4f + c * 0.6f);
        if (fract(p.x) < a * 0.2f || fract(p.y) < a * 0.2f) result = mixColor(result, colors[0], 0.6f);
        return result;
    }
    float grid = type == GuillocheRosettes ? 1.5f + a * 2 : type == EmbossedPaisley ? 2 + a * 2.5f : 3 + a * 5;
    Point q = p * grid;
    if (type == ArtDecoFans || type == EmbossedPaisley || type == GuillocheRosettes) {
        q = q + Point{random(0, 124), random(1, 124)};
    }
    int x = int(std::floor(q.x)), y = int(std::floor(q.y));
    Point local{fract(q.x) - 0.5f, fract(q.y) - 0.5f};
    float thread = grain({p.x * 130, p.y * 70}, 3);
    uint32_t background = mixColor(colors[0], colors[1], thread * 0.12f);
    switch (type) {
        case Sashiko: {
            float best = 10, angle = 0;
            for (float ox : {-0.5f, 0.5f}) for (float oy : {-0.5f, 0.5f}) {
                Point v = local - Point{ox, oy}; float distance = std::abs(length(v) - 0.5f);
                if (distance < best) { best = distance; angle = std::atan2(v.y, v.x); }
            }
            float dash = fract(angle / TAU * (24 + a * 30));
            float stitch = 1 - smoothstep(0.25f + b * 0.4f, 0.32f + b * 0.4f, dash);
            uint32_t yarn = mixColor(colors[2], colors[3], thread * d * 0.5f);
            return line(background, yarn, best + (1 - stitch), 0.007f + c * 0.012f);
        }
        case Bargello: {
            float stitchX = std::floor(p.x * (100 + a * 100)) / (100 + a * 100);
            float flame = std::abs(fract(stitchX * (3 + a * 4)) - 0.5f);
            float bands = fract(p.y * 8 + std::pow(flame * 2, 0.6f) * (1 + b * 3));
            uint32_t yarn = palette(0.15f + bands * 0.85f);
            float seam = 1 - smoothstep(0.02f, 0.08f + c * 0.1f, std::abs(fract(p.x * 150) - 0.5f));
            return shade(yarn, 0.4f + (1 - seam) * d * 0.25f + thread * 0.06f);
        }
        case Kilim: {
            float steps = 5 + std::floor(b * 8);
            Point stepped{std::floor(local.x * steps) / steps, std::floor(local.y * steps) / steps};
            float diamond = std::abs(stepped.x) + std::abs(stepped.y);
            float ring = fract(diamond * 5);
            uint32_t yarn = ((x + y) & 1) ? colors[1] : colors[2];
            yarn = mixColor(yarn, colors[3], smoothstep(0.6f, 0.7f, ring));
            yarn = mixColor(yarn, colors[0], 1 - smoothstep(0.03f, 0.08f + c * 0.08f, ring));
            return mixColor(yarn, colors[3], thread * d * 0.12f);
        }
        case Brocade: {
            if (y & 1) local.x = fract(q.x + 0.5f) - 0.5f;
            float stem = segment(local, {0, -0.3f}, {0, 0.34f});
            auto leaf = [&](Point center, float tilt) {
                Point v = rotate(local - center, tilt);
                return std::abs(v.x) / 0.1f + v.y * v.y / 0.045f;
            };
            float motif = std::min({leaf({0, -0.12f}, 0), leaf({-0.13f, 0.03f}, -0.8f - b * 0.3f), leaf({0.13f, 0.03f}, 0.8f + b * 0.3f)});
            float fill = 1 - smoothstep(0.8f, 1.02f, motif);
            float embroidery = 0.5f + 0.5f * std::sin(local.y * 100 + local.x * 50);
            uint32_t gold = mixColor(colors[2], colors[3], embroidery * d);
            uint32_t result = mixColor(background, gold, fill * (0.5f + c * 0.5f));
            return line(result, gold, stem, 0.005f + c * 0.012f);
        }
        case Batik: {
            Point v = local + Point{grain(p, 25) - 0.5f, grain(p + Point{4, 2}, 25) - 0.5f} * d * 0.05f;
            float r = length(v), angle = std::atan2(v.y, v.x);
            float petals = 5 + std::floor(b * 5);
            float edge = std::abs(r - 0.25f - 0.09f * std::cos(angle * petals));
            uint32_t fabric = mixColor(colors[0], colors[1], grain(p, 7) * 0.8f);
            fabric = line(fabric, colors[2], std::abs(r - 0.12f), 0.012f);
            return line(fabric, colors[3], std::min(edge, std::abs(r - 0.04f)), 0.009f + c * 0.01f);
        }
        case Ikat: {
            float drift = (grain({p.x * 25, p.y}, 3) - 0.5f) * (0.03f + c * 0.1f);
            float diamond = std::abs(fract(q.x + drift * grid) - 0.5f) + std::abs(local.y) * (0.5f + b);
            float wave = 0.5f + 0.5f * std::cos(diamond * TAU * 2.5f);
            float feather = grain({p.x * 150, p.y * 4}, 2);
            uint32_t yarn = mixColor(colors[1], colors[2], smoothstep(0.3f - c * 0.1f, 0.7f + c * 0.1f, wave + (feather - 0.5f) * c));
            yarn = mixColor(yarn, colors[3], std::pow(wave, 10) * 0.65f);
            return shade(yarn, 0.4f + thread * d * 0.25f);
        }
        case EmbossedPaisley: {
            local = rotate(local, ((x + y) & 1) ? -0.22f : 0.22f);
            int tx = std::clamp(int((local.x + 0.5f) * 256), 0, 255);
            int ty = std::clamp(int((local.y + 0.5f) * 256), 0, 255);
            return motifTile[ty * 256 + tx];
        }
        case ArtDecoFans: {
            int row = int(std::floor(q.y));
            Point v{fract(q.x + (row & 1) * 0.5f) - 0.5f, fract(q.y)};
            float r = length(v), angle = std::atan2(v.y, v.x);
            float fan = std::abs(r - 0.84f);
            float ribs = std::abs(std::sin(angle * (8 + std::floor(b * 8)))) * r;
            uint32_t enamel = mixColor(colors[0], colors[1], smoothstep(0, 0.8f, r));
            uint32_t gold = mixColor(colors[2], colors[3], d * (0.5f + std::sin(angle * 2) * 0.5f));
            return line(enamel, gold, std::min(fan, ribs), 0.006f + c * 0.014f);
        }
        case CloverLattice: {
            float r = 10;
            for (int side = 0; side < 4; ++side) r = std::min(r, length(local - rotate({0.17f + b * 0.04f, 0}, side * PI * 0.5f)) - 0.23f);
            uint32_t tile = ((x + y) & 1) ? colors[1] : colors[2];
            tile = mixColor(tile, colors[3], std::pow(grain(p, 12), 4) * d * 0.4f);
            return mixColor(colors[0], tile, 1 - smoothstep(-0.015f - c * 0.025f, -0.005f, r));
        }
        case GuillocheRosettes: {
            float r = length(local), angle = std::atan2(local.y, local.x), petals = 7 + std::floor(b * 10);
            float curve = r + std::cos(angle * petals) * 0.04f + std::sin(angle * (petals - 1)) * 0.018f;
            float distance = std::abs(fract(curve * (16 + d * 16)) - 0.5f) / (16 + d * 16);
            uint32_t ink = mixColor(colors[1], colors[2], r * 1.8f);
            ink = mixColor(ink, colors[3], std::pow(0.5f + 0.5f * std::sin(angle * petals), 5) * 0.5f);
            return line(colors[0], ink, distance / grid, 0.0006f + c * 0.0012f);
        }
        default: return background;
    }
}

uint32_t DecorativeTexture::botanical(Point p) const {
    float a = values[0], b = values[1], c = values[2], d = values[3];
    if (type == SeaAnemone || type == Jellyfish) return curves(p);
    if (type == GinkgoLeaves || type == MonsteraCanopy) {
        float grid = type == GinkgoLeaves ? 2.5f + a * 4 : 2 + a * 3;
        Point q = p * grid;
        int x = int(std::floor(q.x)), y = int(std::floor(q.y));
        uint32_t result = mixColor(colors[0], colors[1], grain(p, 8) * 0.12f);
        for (int oy = -1; oy <= 1; ++oy) for (int ox = -1; ox <= 1; ++ox) {
            int gx = x + ox, gy = y + oy;
            Point v = q - Point{gx + 0.5f + (random(gx, gy) - 0.5f) * 0.6f,
                                gy + 0.5f + (random(gx + 71, gy) - 0.5f) * 0.6f};
            v = rotate(v, random(gx + 37, gy) * TAU);
            float mask, veins;
            if (type == GinkgoLeaves) {
                v.y += 0.18f;
                float angle = std::atan2(v.x, -v.y), r = length(v);
                float width = 0.9f + b * 0.8f;
                float scallop = 0.47f + std::sin(angle * 31) * 0.009f;
                mask = (1 - smoothstep(scallop - aa * grid, scallop + aa * grid, r)) *
                        (1 - smoothstep(width - 0.03f, width + 0.03f, std::abs(angle)));
                veins = std::pow(0.5f + std::cos(angle * (30 + c * 60) + r * 2) * 0.5f, 16) * smoothstep(0.02f, 0.4f, r);
                float stem = segment(v, {0, 0}, {0, 0.2f});
                result = line(result, colors[1], stem / grid, 0.002f);
            } else {
                float leaf = v.x * v.x / 0.12f + v.y * v.y / 0.21f;
                float teeth = std::pow(std::abs(std::sin(v.y * (14 + b * 12))), 6) * smoothstep(0.1f, 0.3f, std::abs(v.x));
                float cut = 1 - smoothstep(0.2f, 0.5f + c * 0.2f, teeth);
                float holes = 0;
                for (int side : {-1, 1}) for (int i = -1; i <= 2; ++i) {
                    Point h = rotate(v - Point{side * 0.16f, i * 0.12f - 0.05f}, side * 0.7f);
                    holes = std::max(holes, 1 - smoothstep(0.7f, 1, length({h.x / (0.022f + c * 0.022f), h.y / 0.05f})));
                }
                mask = (1 - smoothstep(0.94f, 1.02f, leaf)) * cut * (1 - holes);
                veins = std::exp(-std::abs(v.x) * 200) + std::pow(0.5f + 0.5f * std::cos((v.y + std::abs(v.x) * 0.65f) * 55), 20) * 0.25f;
            }
            uint32_t leaf = mixColor(colors[1], colors[2], random(gx + 9, gy) * 0.55f + grain(p, 50) * 0.18f);
            leaf = shade(leaf, 0.4f - v.x * d * 0.3f - v.y * d * 0.2f);
            leaf = mixColor(leaf, colors[3], clamp01(veins) * d * 0.6f);
            result = mixColor(result, leaf, mask);
        }
        return result;
    }
    if (type == LotusPond) {
        Cell v = cell(p * (3 + a * 4));
        Point q = rotate(v.local, v.value * TAU);
        float r = length(q), angle = std::atan2(q.y, q.x), radius = 0.22f + b * 0.15f;
        uint32_t water = mixColor(colors[0], colors[1], grain(p, 5) * 0.2f);
        float ripples = std::pow(0.5f + 0.5f * std::sin(r * 70), 18) * std::exp(-r * 2) * c;
        water = mixColor(water, colors[3], ripples * 0.18f);
        float notch = smoothstep(0.06f, 0.22f, std::abs(angle));
        float pad = (1 - smoothstep(radius - 0.01f, radius, r)) * notch;
        uint32_t leaf = mixColor(colors[1], colors[3], std::pow(0.5f + std::cos(angle * 23) * 0.5f, 18) * 0.18f);
        water = mixColor(water, leaf, pad);
        if (v.value > 0.65f - d * 0.3f) {
            Point flower = q - Point{0.05f, -0.08f};
            float fr = length(flower), fa = std::atan2(flower.y, flower.x);
            float petals = 0.085f + 0.025f * std::cos(fa * 9);
            uint32_t petal = mixColor(colors[2], colors[3], std::pow(0.5f + 0.5f * std::cos(fa * 9), 2) * 0.7f);
            water = mixColor(water, petal, 1 - smoothstep(petals - 0.008f, petals, fr));
        }
        return water;
    }
    if (type == ButterflyWings) {
        Point q{std::abs(p.x), p.y};
        Point upper{(q.x - 0.24f) / (0.22f + b * 0.06f), (q.y + 0.14f) / 0.32f};
        Point lower{(q.x - 0.18f) / 0.22f, (q.y - 0.24f) / 0.23f};
        float wing = std::min(length(upper), length(lower));
        float mask = 1 - smoothstep(0.97f, 1.02f, wing);
        Cell v = cell({q.x * (20 + a * 15), q.y * (10 + a * 8)});
        float vein = 1 - smoothstep(0.015f, 0.045f + c * 0.04f, v.second - v.first);
        uint32_t body = mixColor(colors[1], colors[2], smoothstep(-0.2f, 0.35f, q.y) * d);
        body = mixColor(body, colors[3], std::pow(wing, 7) * 0.65f);
        body = mixColor(body, colors[0], vein * 0.95f + smoothstep(0.88f, 0.96f, wing) * 0.8f);
        float angle = std::atan2(upper.y, upper.x);
        float dots = std::pow(0.5f + std::cos(angle * 22) * 0.5f, 12) * std::exp(-std::abs(wing - 0.93f) * 65);
        body = mixColor(body, colors[3], dots);
        uint32_t result = mixColor(colors[0], body, mask);
        float insect = segment(p, {0, -0.25f}, {0, 0.34f});
        return line(result, colors[0], insect, 0.015f);
    }
    if (type == FungalGills) {
        Point q = p - Point{-0.06f, 0.06f};
        float r = length(q), angle = std::atan2(q.y, q.x);
        float phase = angle * (60 + std::floor(a * 100)) + random(0, 112) * TAU + std::sin(r * 12 + angle * 7) * b * 2;
        float gill = std::pow(0.5f + std::sin(phase) * 0.5f, 0.35f + c * 0.6f);
        uint32_t flesh = mixColor(colors[1], colors[2], gill * 0.8f);
        flesh = mixColor(flesh, colors[3], std::pow(gill, 15) * d * 0.65f);
        flesh = mixColor(colors[0], flesh, smoothstep(0.025f, 0.11f, r));
        return shade(flesh, 0.55f - r * 0.25f);
    }
    if (type == Radiolaria) {
        float r = length(p), angle = std::atan2(p.y, p.x), radius = 0.4f;
        float spikes = std::pow(0.5f + std::cos(angle * (28 + std::floor(b * 30))) * 0.5f, 30);
        float outline = radius + spikes * 0.055f;
        if (r > outline + aa) return colors[0];
        float z = std::sqrt(clamp01(1 - r * r / (radius * radius)));
        float latitude = std::asin(std::clamp(p.y / radius, -1.0f, 1.0f));
        float longitude = std::atan2(p.x, std::max(z * radius, 0.001f)) + (random(0, 112) - 0.5f) * 0.2f;
        Point uv{longitude * (7 + a * 11), latitude * (7 + a * 11)};
        int row = int(std::floor(uv.y));
        Point hex{fract(uv.x + (row & 1) * 0.5f) - 0.5f, fract(uv.y) - 0.5f};
        float holes = length(hex), membrane = smoothstep(0.2f, 0.26f + c * 0.06f, holes);
        uint32_t shell = shade(mixColor(colors[1], colors[2], z), 0.2f + z * 0.55f - p.x * 0.25f);
        shell = mixColor(shell, colors[3], std::pow(z, 9) * d * 0.4f);
        uint32_t result = mixColor(colors[0], shell, membrane * 0.9f);
        result = line(result, colors[3], std::abs(r - outline), 0.0018f + c * 0.001f);
        return result;
    }
    if (type == SlotCanyon) {
        float channel = std::sin(p.y * (3 + b * 5)) * 0.1f + std::sin(p.y * 13) * 0.025f;
        float x = p.x - channel, depth = std::abs(x);
        float strata = p.y * (70 + a * 100) + std::log(depth + 0.025f) * 8 + grain(p, 5) * b * 3;
        float bands = 0.5f + 0.5f * std::sin(strata);
        uint32_t rock = mixColor(colors[1], colors[2], bands * 0.55f + grain(p, 30) * 0.3f);
        rock = shade(rock, clamp01(0.2f + depth * (0.5f + c) + (x < 0 ? 0.16f : 0)));
        float opening = std::exp(-depth * 55) * (0.3f + d * 0.7f);
        return mixColor(rock, colors[3], opening * smoothstep(0.4f, -0.5f, p.y));
    }
    if (type == GlacialCrevasses) {
        Point q = p * (4 + a * 8) + Point{grain(p, 9), grain(p + Point{4, 1}, 9)} * b * 0.6f;
        Cell v = cell(q);
        float edge = v.second - v.first;
        float height = smoothstep(0.01f, 0.14f + c * 0.14f, edge);
        uint32_t ice = mixColor(colors[0], colors[1], height * 0.6f + v.value * 0.2f);
        ice = mixColor(ice, colors[2], std::pow(height, 2) * 0.8f);
        ice = mixColor(ice, colors[3], height * d * (0.3f + grain(p, 100) * 0.5f));
        return shade(ice, 0.4f - v.local.x * 0.2f - v.local.y * 0.25f + height * 0.15f);
    }
    return colors[0];
}

uint32_t DecorativeTexture::gyroid(Point p) const {
    float a = values[0], b = values[1], c = values[2], d = values[3];
    float frequency = (1.2f + a * 2.5f) * TAU;
    auto field = [&](Vec3 pos) {
        Vec3 q = turn(pos, 0.3f + b * PI + random(0, 112) * 0.3f, 0.6f) * frequency;
        float g = std::sin(q.x) * std::cos(q.y) + std::sin(q.y) * std::cos(q.z) + std::sin(q.z) * std::cos(q.x);
        float surface = (std::abs(g) - 0.08f - c * 0.25f) / (frequency * 2);
        float box = std::max({std::abs(pos.x), std::abs(pos.y), std::abs(pos.z)}) - 0.43f;
        return std::max(surface, box);
    };
    Vec3 origin{0, 0, 1.7f}, ray = unit(Vec3{p.x, -p.y, -1.4f});
    float travel = 0;
    Vec3 hit{};
    bool found = false;
    for (int i = 0; i < 150; ++i) {
        hit = origin + ray * travel;
        float distance = field(hit);
        if (distance < 0.0008f) { found = true; break; }
        travel += std::max(distance * 0.8f, 0.0003f);
        if (travel > 2.8f) break;
    }
    if (!found) return colors[0];
    constexpr float epsilon = 0.001f;
    Vec3 normal = unit(Vec3{field(hit + Vec3{epsilon, 0, 0}) - field(hit - Vec3{epsilon, 0, 0}),
                            field(hit + Vec3{0, epsilon, 0}) - field(hit - Vec3{0, epsilon, 0}),
                            field(hit + Vec3{0, 0, epsilon}) - field(hit - Vec3{0, 0, epsilon})});
    Vec3 light = unit(Vec3{-0.5f, 0.8f, 1});
    float diffuse = clamp01(dot(normal, light));
    Vec3 halfway = unit(light - ray);
    float specular = std::pow(clamp01(dot(normal, halfway)), 12 + d * 60);
    float tint = 0.5f + 0.5f * std::sin(hit.x * frequency + hit.y * frequency * 0.5f);
    uint32_t body = mixColor(colors[1], colors[2], tint * 0.65f);
    body = shade(body, 0.17f + diffuse * 0.53f);
    return mixColor(body, colors[3], specular * 0.85f);
}

uint32_t DecorativeTexture::geometry(Point p) const {
    float a = values[0], b = values[1], c = values[2], d = values[3];
    if (type == SpiralPearls) return spheres(p);
    if (type == HopfFibration || type == Spirograph || type == Hypercube) return curves(p);
    if (type == Gyroid) return gyroid(p);
    if (type == ImpossibleStairs) {
        uint32_t result = colors[0];
        for (int i : nearby(p)) {
            const Face &f = faces[i];
            if (triangle(p, f.a, f.b, f.c) || triangle(p, f.a, f.c, f.d)) {
                uint32_t surface = mixColor(colors[1], colors[2], f.value * 0.7f);
                result = shade(surface, 0.2f + f.light * (0.5f + d * 0.3f));
            }
        }
        return result;
    }
    if (type == ChiralPinwheels) {
        Point q = p * (4 + a * 6);
        int x = int(std::floor(q.x)), y = int(std::floor(q.y));
        q = {fract(q.x) - 0.5f, fract(q.y) - 0.5f};
        float r = length(q), angle = std::atan2(q.y, q.x);
        float spin = angle + (b - 0.5f) * 5 + (0.3f + c * 2) * r;
        int vane = int(std::floor(fract(spin / TAU + random(x, y) * 0.25f) * 4));
        uint32_t color = colors[1 + ((vane + x + y) % 3 + 3) % 3];
        float edge = std::abs(std::sin(spin * 2)) * r;
        color = shade(color, 0.38f + std::cos(spin * 4) * d * 0.14f + r * 0.2f);
        return line(color, colors[0], edge, 0.008f);
    }
    if (type == TruchetBlocks) {
        Point q = rotate(p, -0.35f) * (6 + a * 8);
        int x = int(std::floor(q.x)), y = int(std::floor(q.y));
        q = {fract(q.x) - 0.5f, fract(q.y) - 0.5f};
        if (random(x, y) > b) q.x = -q.x;
        auto path = [&](Point v) {
            float distance = 10;
            for (int side : {-1, 1}) {
                Point points[]{{0, -0.5f * side}, {0, -0.22f * side}, {-0.22f * side, -0.22f * side}, {-0.22f * side, 0}, {-0.5f * side, 0}};
                for (int i = 1; i < 5; ++i) distance = std::min(distance, segment(v, points[i - 1], points[i]));
            }
            return distance;
        };
        float width = 0.075f + c * 0.08f;
        float lower = path(q - Point{0.035f, 0.07f});
        float top = path(q);
        uint32_t result = line(colors[0], colors[1], lower, width);
        uint32_t stone = mixColor(colors[2], colors[3], 0.3f + d * 0.4f);
        return line(result, stone, top, width * 0.83f);
    }
    if (type == WovenHexagons) {
        Point q = p * (8 + a * 9) + Point{random(0, 112), random(1, 112)};
        float best = 10, light = 0; int selected = -1;
        float radius = 0.12f + b * 0.18f;
        for (int axis = 0; axis < 3; ++axis) {
            Point v = rotate(q, axis * PI / 3);
            float across = fract(v.y) - 0.5f, distance = std::abs(across);
            int lane = int(std::floor(v.y)), crossing = int(std::floor(v.x * 0.577350269f));
            float rank = float(((lane + crossing + axis) % 3 + 3) % 3);
            if (distance < radius && (selected < 0 || rank > best)) {
                selected = axis; best = rank;
                float fiber = std::sin(v.x * 65 + v.y * 12) * c * 0.06f;
                light = 0.22f + std::sqrt(clamp01(1 - across * across / (radius * radius))) * d * 0.65f + fiber;
            }
        }
        return selected < 0 ? colors[0] : shade(colors[1 + selected % 2], light);
    }
    if (type == LenticularWaves) {
        Point q = p * (3 + a * 5);
        q = {fract(q.x) - 0.5f, fract(q.y) - 0.5f};
        float radius = length(q), dome = std::sqrt(clamp01(1 - radius * radius * 4));
        float wave = 0.5f + 0.5f * std::sin(p.y * (75 + a * 90) + dome * (7 + c * 20) + b * TAU + random(0, 112) * TAU);
        uint32_t glass = mixColor(colors[1], colors[2], wave);
        glass = shade(glass, 0.3f + dome * d * 0.4f - q.x * 0.2f);
        glass = mixColor(glass, colors[3], std::pow(wave, 14) * d * 0.6f);
        return mixColor(colors[0], glass, 1 - smoothstep(0.48f, 0.5f, radius));
    }
    return colors[0];
}

uint32_t DecorativeTexture::atmosphere(Point p) const {
    float a = values[0], b = values[1], c = values[2], d = values[3];
    if (type == MercuryDroplets || type == IridescentFoam) return spheres(p);
    if (type == StellarNursery) {
        float pillars = 2 + std::floor(a * 3), density = 0, rim = 0;
        for (int i = 0; i < int(pillars); ++i) {
            float x = (i / std::max(pillars - 1, 1.0f) - 0.5f) * 0.8f - 0.05f;
            float top = -0.13f - random(i, 3) * 0.29f;
            float sway = (grain({p.y * 3, float(i)}, 3) - 0.5f) * b * 0.16f;
            float width = 0.03f + random(i, 4) * 0.04f + std::max(p.y - top, 0.0f) * 0.2f;
            float edge = std::abs(p.x - x - sway) / width;
            float cloud = (1 - smoothstep(0.75f, 1.2f, edge)) * smoothstep(top - width, top + 0.02f, p.y);
            float turbulence = grain({p.x * 2, p.y}, 30);
            density = std::max(density, cloud * (0.75f + turbulence * c * 0.25f));
            rim = std::max(rim, cloud * std::exp(-std::abs(edge - 0.88f) * 12) * (0.3f + turbulence) * (p.x < x + sway ? 0.5f : 1.0f));
        }
        float nebula = grain(p, 3);
        uint32_t gas = mixColor(colors[0], colors[1], 0.15f + nebula * 0.8f);
        gas = mixColor(gas, colors[2], std::pow(nebula, 3) * 0.45f);
        uint32_t dust = mixColor(colors[0], colors[2], 0.08f + grain(p, 18) * 0.24f);
        uint32_t result = mixColor(gas, dust, clamp01(density));
        result = mixColor(result, colors[3], rim * 1.4f);
        float star = random(int(p.x / aa), int(p.y / aa));
        return mixColor(result, colors[3], smoothstep(0.9995f - d * 0.0005f, 1, star));
    }
    if (type == PulsarJets) {
        Point q = rotate(p, 0.6f + b * 0.4f + (random(0, 112) - 0.5f) * 0.3f);
        float y = std::abs(q.y), bend = std::sin(q.y * 30 + b * TAU) * y * 0.045f;
        float width = 0.012f + a * 0.04f + y * 0.16f;
        float jet = std::exp(-std::abs(q.x - bend) / width) * std::exp(-y * 2) * 1.8f;
        float shock = std::pow(0.5f + 0.5f * std::cos(y * (55 + c * 80)), 10);
        uint32_t beam = mixColor(colors[1], colors[2], shock * 0.7f);
        beam = mixColor(beam, colors[3], shock * 0.5f + std::exp(-length(q) * 70));
        uint32_t result = mixColor(colors[0], beam, jet * (0.5f + d * 0.5f));
        for (int ring = 1; ring <= 5; ++ring) for (int side : {-1, 1}) {
            float position = 0.045f + ring * 0.075f;
            float radius = 0.018f + position * 0.16f;
            Point v{q.x / radius, (q.y - position * side) / (0.006f + position * 0.035f)};
            float distance = std::abs(length(v) - 1) * (0.006f + position * 0.035f);
            float shockRing = std::exp(-distance / (0.0012f + c * 0.0015f));
            uint32_t color = mixColor(colors[1], colors[2], ring / 6.0f);
            color = mixColor(color, colors[3], clamp01(0.5f - v.x * 0.4f));
            result = mixColor(result, color, shockRing * d);
        }
        float disk = std::exp(-length({q.x * 0.5f, q.y * 4}) * 25);
        return mixColor(result, colors[3], disk * d);
    }
    if (type == GravitationalLensing) {
        float r = std::max(length(p), 0.025f);
        Point warped = p - p * ((0.025f + b * 0.035f) / (r * r));
        Cell galaxy = cell(warped * (5 + a * 8));
        Point q = rotate(galaxy.local, galaxy.value * TAU);
        float radius = length({q.x * 1.6f, q.y * 0.6f});
        float light = std::exp(-radius * (18 - c * 6));
        uint32_t color = mixColor(colors[1], colors[2], galaxy.value);
        color = mixColor(color, colors[3], std::pow(light, 3));
        uint32_t result = mixColor(colors[0], color, light);
        float central = std::exp(-length({p.x * 0.8f, p.y * 1.4f}) * 32);
        result = mixColor(result, colors[2], central * 0.8f);
        float star = random(int(p.x / aa), int(p.y / aa));
        return mixColor(result, colors[3], smoothstep(0.9997f - d * 0.0003f, 1, star));
    }
    if (type == NacreClouds) {
        float billows = grain({p.x * 2, p.y * 9}, 3 + a * 4);
        float wave = p.y * (8 + a * 12) + std::sin(p.x * 7) * (0.4f + b) + billows * 1.5f;
        float cloud = std::pow(0.5f + 0.5f * std::cos(wave), 1.4f);
        float phase = 0.5f + 0.5f * std::sin(wave * 0.45f + p.x * 2 + billows * c * 3);
        uint32_t nacre = mixColor(colors[1], colors[2], phase);
        nacre = mixColor(nacre, colors[3], std::pow(cloud, 4) * d * 0.6f);
        return mixColor(colors[0], nacre, 0.25f + cloud * 0.75f);
    }
    if (type == SonicBoom) {
        Point q = p + Point{0.25f, 0};
        float mach = 0.2f + b * 0.8f;
        float distance = std::abs(std::abs(q.y) - std::max(q.x, 0.0f) * mach);
        float cone = std::exp(-distance * (90 - c * 45)) * smoothstep(-0.02f, 0.02f, q.x);
        float rings = std::pow(0.5f + 0.5f * std::cos(length({q.x, q.y / mach}) * (35 + a * 80) + random(0, 112) * TAU), 16);
        float halo = std::exp(-distance * 15) * std::exp(-std::max(q.x, 0.0f) * 2);
        uint32_t result = mixColor(colors[0], colors[1], halo * 0.7f);
        result = mixColor(result, colors[2], rings * cone * 0.8f);
        return mixColor(result, colors[3], cone * (0.3f + d * 0.7f));
    }
    if (type == RibbonVortex) {
        float r = length(p), angle = std::atan2(p.y, p.x), count = 5 + std::floor(a * 8);
        float phase = fract(angle / TAU * count + std::log(r + 0.035f) * (0.4f + b * 1.3f) + random(0, 112));
        float width = 0.3f + c * 0.3f;
        float ribbon = 1 - smoothstep(width - 0.03f, width + 0.03f, phase);
        float across = clamp01(phase / width);
        uint32_t color = mixColor(colors[1], colors[2], 0.5f + 0.5f * std::sin(angle + r * 4));
        color = shade(color, 0.17f + std::sin(across * PI) * (0.4f + d * 0.3f));
        color = mixColor(color, colors[3], std::pow(across, 12) * d * 0.6f);
        return mixColor(colors[0], color, ribbon * smoothstep(0.025f, 0.1f, r));
    }
    if (type == LavaCrust) {
        Cell v = cell(p * (6 + a * 13) + Point{grain(p, 15), grain(p + Point{4, 6}, 15)} * b);
        float edge = v.second - v.first;
        float crust = smoothstep(0.015f, 0.06f + c * 0.08f, edge);
        uint32_t rock = mixColor(colors[0], colors[1], 0.1f + grain(p, 80) * 0.7f);
        rock = shade(rock, 0.3f + v.local.x * 0.15f - v.local.y * 0.2f);
        uint32_t lava = mixColor(colors[2], colors[3], std::exp(-edge * 50) * d);
        uint32_t result = mixColor(lava, rock, crust);
        return mixColor(result, colors[2], std::exp(-edge * 15) * d * 0.18f);
    }
    if (type == AbyssalVent) {
        uint32_t result = mixColor(colors[0], colors[1], grain(p, 3) * 0.2f);
        int count = 2 + int(a * 4);
        for (int i = 0; i < count; ++i) {
            float x = (i / float(count) - 0.5f) * 0.85f + 0.08f;
            float top = 0.05f + random(i, 6) * 0.2f;
            float width = 0.018f + random(i, 7) * 0.018f;
            float current = std::sin(p.y * 9 + i) * b * 0.1f + (top - p.y) * b * 0.2f;
            float plume = std::exp(-std::pow((p.x - x - current) / (width + std::max(top - p.y, 0.0f) * 0.2f), 2));
            plume *= smoothstep(top + 0.01f, top - 0.06f, p.y) * (0.25f + grain({p.x * 2, p.y}, 35) * c);
            result = mixColor(result, mixColor(colors[1], colors[2], grain(p, 12)), plume * 0.9f);
            float chimney = 1 - smoothstep(width, width + 0.005f, std::abs(p.x - x) + (grain(p, 50) - 0.5f) * 0.012f);
            chimney *= smoothstep(top - 0.005f, top + 0.005f, p.y);
            uint32_t rock = mixColor(colors[0], colors[2], grain(p, 60) * 0.45f);
            result = mixColor(result, rock, chimney);
            float rim = std::exp(-length({(p.x - x) * 0.8f, (p.y - top) * 3}) * 80);
            result = mixColor(result, colors[3], rim * d * 0.8f);
        }
        return result;
    }
    return colors[0];
}

uint32_t DecorativeTexture::pixel(int x, int y) const {
    Point p{(x + 0.5f - width * 0.5f) * scale, (y + 0.5f - height * 0.5f) * scale};
    uint32_t result;
    if (type >= Malachite && type <= CutCrystalGlass) result = minerals(p);
    else if (type == ChromaticPlaid || (type >= Sashiko && type <= GuillocheRosettes)) result = textiles(p);
    else if (type >= GinkgoLeaves && type <= GlacialCrevasses) result = botanical(p);
    else if (type >= SpiralPearls && type <= LenticularWaves) result = geometry(p);
    else result = atmosphere(p);
    return finish(result);
}

}
