#include "DetailedTextures.h"
#include "TextureMath.h"
#include <cmath>
#include <functional>
#include <limits>
#include <set>

namespace textures {
namespace {

constexpr float PI = 3.14159265358979323846f;
constexpr float TAU = PI * 2;
float dot(Point a, Point b) { return a.x * b.x + a.y * b.y; }
float length(Point p) { return std::sqrt(dot(p, p)); }
Point rotate(Point p, float a) {
    float c = std::cos(a), s = std::sin(a);
    return {p.x * c - p.y * s, p.x * s + p.y * c};
}
float segment(Point p, Point a, Point b) {
    Point v = b - a;
    return length(p - a - v * std::clamp(dot(p - a, v) / std::max(dot(v, v), 0.0000001f), 0.0f, 1.0f));
}
float finite(const std::vector<float> &p, size_t i, float fallback) {
    return i < p.size() && std::isfinite(p[i]) ? p[i] : fallback;
}
uint32_t premultiply(uint32_t color) {
    uint32_t a = color >> 24;
    return (a << 24) | ((((color >> 16) & 255) * a + 127) / 255 << 16) |
            ((((color >> 8) & 255) * a + 127) / 255 << 8) | (((color & 255) * a + 127) / 255);
}

}

bool hasDetailedTexture(int type) {
    switch (type) {
        case 4: case 8: case 12: case 13: case 15: case 16: case 20: case 22: case 25:
        case 26: case 27: case 28: case 34: case 36: case 38: case 39: case 48: case 49:
        case 51: case 54: case 55: case 56: case 57: case 58: case 59: case 61: case 62:
        case 63: case 65: case 67: case 70: case 71: case 72: case 73: case 74:
        case 75: case 78: case 79: case 81: case 87: case 88: case 89: case 94: case 100:
        case 101: case 105: case 106: case 107: case 108: case 110: case 111: case 112:
        case 114: case 115: case 116: case 120: case 121:
            return true;
        default: return false;
    }
}

DetailedTexture::DetailedTexture(int type, int seed, int width, int height,
                                const std::vector<float> &parameters,
                                const std::vector<uint32_t> &palette) :
        type(type), seed(seed), width(width), height(height), noise(seed) {
    float frequency = std::clamp(finite(parameters, 0, 0.008f), 0.001f, 0.05f);
    scale = frequency / 0.008f / static_cast<float>(std::min(width, height));
    aa = scale * 0.8f;
    extentX = width * scale * 0.5f;
    extentY = height * scale * 0.5f;
    for (size_t i = 0; i < values.size(); ++i) values[i] = std::clamp(finite(parameters, i + 1, 0.5f), 0.0f, 120.0f);
    if (type >= 51) aa *= 1.4f - clamp01(values[2]);
    constexpr uint32_t defaults[]{0xff081b27u, 0xff246a82u, 0xffa9d4c0u, 0xffffe8b4u};
    for (size_t i = 0; i < colors.size(); ++i) colors[i] = premultiply(i < palette.size() ? palette[i] : defaults[i]);
    noise.SetNoiseType(FastNoiseLite::NoiseType_OpenSimplex2S);
    noise.SetFractalType(FastNoiseLite::FractalType_FBm);
    noise.SetFractalOctaves(4);
    noise.SetFrequency(1);
}

float DetailedTexture::random(int x, int y) const {
    uint32_t value = static_cast<uint32_t>(x) * 0x9e3779b9u ^ static_cast<uint32_t>(y) * 0x85ebca6bu ^ static_cast<uint32_t>(seed);
    value ^= value >> 16; value *= 0x7feb352du;
    value ^= value >> 15; value *= 0x846ca68bu;
    return static_cast<float>(value >> 8) / 16777215.0f;
}

float DetailedTexture::grain(Point p, float frequency) const {
    return noise.GetNoise(p.x * frequency, p.y * frequency) * 0.5f + 0.5f;
}

uint32_t DetailedTexture::palette(float value) const {
    value = clamp01(value) * 3;
    int i = std::min(static_cast<int>(value), 2);
    return mixColor(colors[i], colors[i + 1], value - i);
}

uint32_t DetailedTexture::shade(uint32_t color, float light) const {
    return light < 0.5f ? mixColor(colors[0], color, light * 2) : mixColor(color, colors[3], (light - 0.5f) * 2);
}

void DetailedTexture::indexBounds(int index, Point minimum, Point maximum) {
    if (maximum.x < -extentX || minimum.x > extentX || maximum.y < -extentY || minimum.y > extentY) return;
    auto bx = [&](float x) { return std::clamp(static_cast<int>((x + extentX) * 32 / extentX), 0, 63); };
    auto by = [&](float y) { return std::clamp(static_cast<int>((y + extentY) * 32 / extentY), 0, 63); };
    for (int y = by(minimum.y); y <= by(maximum.y); ++y) for (int x = bx(minimum.x); x <= bx(maximum.x); ++x) {
        bins[y * 64 + x].push_back(index);
    }
}

const std::vector<int> &DetailedTexture::nearby(Point p) const {
    int x = std::clamp(static_cast<int>((p.x + extentX) * 32 / extentX), 0, 63);
    int y = std::clamp(static_cast<int>((p.y + extentY) * 32 / extentY), 0, 63);
    return bins[y * 64 + x];
}

void DetailedTexture::stroke(Point a, Point b, float radius, float depth, float value) {
    int index = static_cast<int>(strokes.size());
    strokes.push_back({a, b, radius, depth, value});
    float padding = radius * 5 + aa * 2;
    indexBounds(index, {std::min(a.x, b.x) - padding, std::min(a.y, b.y) - padding},
                {std::max(a.x, b.x) + padding, std::max(a.y, b.y) + padding});
}

bool DetailedTexture::reactionDiffusion(bool (*cancelled)()) {
    const int count = fieldSize * fieldSize;
    std::vector<float> a(count, 1), b(count), nextA(count), nextB(count);
    for (int y = 0; y < fieldSize; ++y) for (int x = 0; x < fieldSize; ++x) {
        float initial = random(x / 4, y / 4);
        b[y * fieldSize + x] = initial > 0.66f ? 0.7f : random(x, y) * 0.07f;
        a[y * fieldSize + x] -= b[y * fieldSize + x] * 0.5f;
    }
    // Gray-Scott: two chemical concentrations, a nine-point Laplacian and periodic boundaries.
    float feed = 0.045f + clamp01(values[0]) * 0.014f;
    float kill = 0.059f + clamp01(values[1]) * 0.0045f;
    for (int iteration = 0; iteration < 900; ++iteration) {
        if (cancelled()) return false;
        for (int y = 0; y < fieldSize; ++y) {
            int above = ((y + fieldSize - 1) % fieldSize) * fieldSize;
            int below = ((y + 1) % fieldSize) * fieldSize;
            for (int x = 0; x < fieldSize; ++x) {
                int left = (x + fieldSize - 1) % fieldSize, right = (x + 1) % fieldSize, i = y * fieldSize + x;
                auto laplacian = [&](const std::vector<float> &v) {
                    return -v[i] + 0.2f * (v[y * fieldSize + left] + v[y * fieldSize + right] + v[above + x] + v[below + x]) +
                            0.05f * (v[above + left] + v[above + right] + v[below + left] + v[below + right]);
                };
                float reaction = a[i] * b[i] * b[i];
                nextA[i] = clamp01(a[i] + laplacian(a) - reaction + feed * (1 - a[i]));
                nextB[i] = clamp01(b[i] + laplacian(b) * 0.5f + reaction - (kill + feed) * b[i]);
            }
        }
        a.swap(nextA); b.swap(nextB);
    }
    field = std::move(b);
    return !cancelled();
}

bool DetailedTexture::slimeMold(bool (*cancelled)()) {
    field.assign(fieldSize * fieldSize, 0);
    std::vector<float> next(field.size());
    struct Agent { Point p; float angle; };
    int count = 3500 + static_cast<int>(clamp01(values[0]) * 6500);
    std::vector<Agent> agents;
    for (int i = 0; i < count; ++i) agents.push_back({{random(i, 1) * fieldSize, random(i, 2) * fieldSize}, random(i, 3) * TAU});
    float sensor = 5.0f + clamp01(values[1]) * 6;
    auto read = [&](Point p) {
        int x = (static_cast<int>(p.x) + fieldSize * 2) % fieldSize;
        int y = (static_cast<int>(p.y) + fieldSize * 2) % fieldSize;
        return field[y * fieldSize + x];
    };
    for (int iteration = 0; iteration < 100; ++iteration) {
        if (cancelled()) return false;
        for (int i = 0; i < count; ++i) {
            Agent &agent = agents[i];
            auto sense = [&](float angle) { return read(agent.p + Point{std::cos(angle), std::sin(angle)} * sensor); };
            float forward = sense(agent.angle), left = sense(agent.angle - 0.65f), right = sense(agent.angle + 0.65f);
            if (forward < left && forward < right) agent.angle += random(i, iteration) > 0.5f ? -0.55f : 0.55f;
            else if (left > forward && left > right) agent.angle -= 0.55f;
            else if (right > forward && right > left) agent.angle += 0.55f;
            agent.p = agent.p + Point{std::cos(agent.angle), std::sin(agent.angle)} * 1.2f;
            agent.p = {fract(agent.p.x / fieldSize) * fieldSize, fract(agent.p.y / fieldSize) * fieldSize};
            field[static_cast<int>(agent.p.y) * fieldSize + static_cast<int>(agent.p.x)] += 0.7f;
        }
        for (int y = 0; y < fieldSize; ++y) for (int x = 0; x < fieldSize; ++x) {
            float sum = 0;
            for (int oy = -1; oy <= 1; ++oy) for (int ox = -1; ox <= 1; ++ox) {
                sum += field[((y + oy + fieldSize) % fieldSize) * fieldSize + (x + ox + fieldSize) % fieldSize];
            }
            next[y * fieldSize + x] = sum * (0.94f / 9);
        }
        field.swap(next);
    }
    float maximum = *std::max_element(field.begin(), field.end());
    for (float &value : field) value = std::log1p(value) / std::log1p(std::max(maximum, 0.01f));
    return !cancelled();
}

bool DetailedTexture::branching(bool (*cancelled)()) {
    int serial = 0;
    std::function<void(Point, float, float, float, int, int)> branch;
    branch = [&](Point from, float angle, float distance, float radius, int depth, int identity) {
        if (depth <= 0 || strokes.size() > 14000 || cancelled()) return;
        Point previous = from;
        for (int i = 1; i <= 5; ++i) {
            float t = i / 5.0f;
            Point p = from + Point{std::cos(angle), std::sin(angle)} * (distance * t);
            p = p + rotate({0, std::sin(t * PI) * (random(identity, 8) - 0.5f) * distance * 0.4f}, angle);
            stroke(previous, p, radius * (1 - t * 0.25f), 0, 0.3f + random(identity, 4) * 0.55f);
            previous = p;
        }
        float spread = (type == 81 ? 0.2f : type == 72 ? 0.3f : 0.2f) + clamp01(values[1]) * 0.2f;
        for (int side : {-1, 1}) {
            int id = ++serial;
            float bend = side * (spread + random(id, 3) * 0.5f);
            float nextAngle = angle + bend;
            if (type == 81) nextAngle = angle * 0.55f + PI * 0.5f * 0.45f + bend;
            branch(previous, nextAngle, distance * (0.64f + random(id, 5) * 0.22f), radius * 0.67f, depth - 1, id);
        }
    };
    if (type == 55) {
        for (int fern = 0; fern < 3; ++fern) {
            Point root{(fern - 1) * 0.28f, 0.46f};
            float angle = -PI * 0.5f + (fern - 1) * 0.32f;
            Point direction{std::cos(angle), std::sin(angle)};
            int branches = 9 + static_cast<int>(clamp01(values[0]) * 12);
            for (int i = 1; i <= branches; ++i) {
                float t = i / float(branches + 1);
                Point origin = root + direction * (t * 0.9f);
                stroke(origin - direction * 0.05f, origin, 0.0028f);
                for (int side : {-1, 1}) {
                    float reach = std::sin(t * PI) * (0.1f + clamp01(values[0]) * 0.09f);
                    Point end = origin + rotate(direction, side * (0.7f + clamp01(values[1]) * 0.5f)) * reach;
                    stroke(origin, end, 0.0018f);
                    for (int j = 1; j <= 7; ++j) {
                        Point start = origin + (end - origin) * (j / 8.0f);
                        Point tip = start + rotate(direction, side * 1.9f) * (reach * 0.16f * (1 - j / 9.0f));
                        stroke(start, tip, 0.00085f, 0, 0.6f);
                    }
                }
            }
        }
    } else if (type == 74) {
        for (int arm = 0; arm < 6; ++arm) {
            float angle = arm * TAU / 6 + random(0) * 0.3f;
            Point direction{std::cos(angle), std::sin(angle)};
            stroke({0, 0}, direction * 0.46f, 0.0035f);
            int branches = 7 + static_cast<int>(clamp01(values[0]) * 10);
            for (int i = 2; i < branches; ++i) {
                Point origin = direction * (i * 0.43f / branches);
                float reach = (1 - i / float(branches)) * (0.08f + clamp01(values[1]) * 0.08f);
                for (int side : {-1, 1}) {
                    Point end = origin + rotate(direction, side * PI / 3) * reach;
                    stroke(origin, end, 0.002f);
                    for (int j = 1; j <= 2 + static_cast<int>(clamp01(values[1]) * 4); ++j) {
                        Point p = origin + (end - origin) * (j / 5.0f);
                        stroke(p, p + rotate(direction, side * PI / 3 * 2) * (reach * 0.22f), 0.001f);
                    }
                }
            }
        }
    } else if (type == 81) {
        branch({0, -0.55f}, PI * 0.5f, 0.22f, 0.016f, 6 + static_cast<int>(clamp01(values[0]) * 4), 0);
    } else if (type == 72) {
        for (int i = 0; i < 4; ++i) branch({(i - 1.5f) * 0.25f, 0.5f}, -PI * 0.5f, 0.22f, 0.012f, 6 + static_cast<int>(clamp01(values[0]) * 3), i);
    } else if (type == 79) {
        for (int i = 0; i < 5 + static_cast<int>(clamp01(values[0]) * 10); ++i) {
            Point center{(random(i, 13) - 0.5f) * 0.9f, (random(i, 14) - 0.5f) * 0.9f};
            for (int arm = 0; arm < 5; ++arm) branch(center, arm * TAU / 5 + random(i, 15), 0.065f, 0.004f, 4, serial++);
        }
    } else {
        int arms = type == 94 ? 9 : type == 67 ? 8 : 6;
        for (int i = 0; i < arms; ++i) {
            float angle = i * TAU / arms + random(i, 19) * 0.4f;
            Point center = type == 59 ? Point{(random(i, 20) - 0.5f) * 0.8f, (random(i, 21) - 0.5f) * 0.8f} : Point{0, 0};
            branch(center, angle, 0.12f + random(i, 9) * 0.08f, type == 94 ? 0.0045f : 0.003f,
                   6 + static_cast<int>(clamp01(values[0]) * 4), serial++);
        }
    }
    return !cancelled();
}

bool DetailedTexture::packing(bool (*cancelled)()) {
    struct Disc { Point center; float curvature; };
    float r = std::sqrt(3.0f) / (2 + std::sqrt(3.0f));
    std::array<Disc, 4> initial;
    initial[0] = {{0, 0}, -1};
    for (int i = 1; i < 4; ++i) initial[i] = {rotate({1 - r, 0}, (i - 1) * TAU / 3), 1 / r};
    std::set<std::pair<int, int>> visited;
    auto add = [&](Disc disc) {
        float radius = 0.46f / disc.curvature;
        Point center = disc.center * 0.46f;
        if (radius < aa * 0.8f || disc.curvature < 0) return false;
        auto key = std::pair{static_cast<int>(std::lround(center.x * 100000)), static_cast<int>(std::lround(center.y * 100000))};
        if (!visited.insert(key).second) return false;
        int index = static_cast<int>(circles.size());
        circles.push_back({center, radius, random(index)});
        indexBounds(index, center - Point{radius + aa, radius + aa}, center + Point{radius + aa, radius + aa});
        return true;
    };
    for (Disc disc : initial) add(disc);
    std::function<void(std::array<Disc, 4>, int, int)> fill;
    fill = [&](std::array<Disc, 4> discs, int previous, int depth) {
        if (depth == 0 || circles.size() > 16000 || cancelled()) return;
        for (int replace = 0; replace < 4; ++replace) {
            if (replace == previous) continue;
            float k = -discs[replace].curvature;
            Point weighted = discs[replace].center * (-discs[replace].curvature);
            for (int i = 0; i < 4; ++i) if (i != replace) { k += discs[i].curvature * 2; weighted = weighted + discs[i].center * (discs[i].curvature * 2); }
            if (k < 0.01f) continue;
            Disc next{weighted * (1 / k), k};
            if (!add(next)) continue;
            auto updated = discs; updated[replace] = next;
            fill(updated, replace, depth - 1);
        }
    };
    fill(initial, -1, 8 + static_cast<int>(clamp01(values[0]) * 3));
    return !cancelled();
}

bool DetailedTexture::ribbons(bool (*cancelled)()) {
    if (type == 75) {
        int count = 2 + static_cast<int>(clamp01(values[0]) * 4);
        for (int arc = 0; arc < count; ++arc) {
            float level = (arc - (count - 1) * 0.5f) * 0.76f / count;
            Point previous{-0.55f, level};
            for (int i = 1; i <= 90; ++i) {
                if (cancelled()) return false;
                float t = i / 90.0f;
                Point p{-0.55f + t * 1.1f, level + std::sin(t * PI) * (random(i / 5, arc) - 0.5f) * (0.04f + clamp01(values[1]) * 0.2f)};
                p.y += (random(i, arc + 51) - 0.5f) * 0.035f;
                stroke(previous, p, 0.0018f, 0, 0.8f);
                if (i % 13 == 0) stroke(p, p + Point{0.05f, (random(i, arc) - 0.5f) * 0.1f}, 0.0008f, 0, 0.5f);
                previous = p;
            }
        }
    } else if (type == 101 || type == 116) {
        int strands = type == 116 ? 5 + static_cast<int>(clamp01(values[0]) * 13) : 1;
        for (int strand = 0; strand < strands; ++strand) {
            Point previous{}; float previousDepth = 0;
            for (int i = 0; i <= 1600; ++i) {
                if ((i & 127) == 0 && cancelled()) return false;
                float t = i * TAU / 1600;
                float x, y, z;
                if (type == 101) {
                    int turns = 2 + static_cast<int>(clamp01(values[0]) * 2);
                    float radius = 0.26f + 0.1f * std::cos((turns + 1) * t);
                    x = radius * std::cos(turns * t); y = radius * std::sin(turns * t); z = 0.14f * std::sin((turns + 1) * t);
                } else {
                    float across = (strand / float(strands - 1) - 0.5f) * 0.21f;
                    x = (0.29f + across * std::cos(t * 0.5f)) * std::cos(t);
                    y = (0.29f + across * std::cos(t * 0.5f)) * std::sin(t);
                    z = across * std::sin(t * 0.5f);
                }
                float tilt = 0.7f + clamp01(values[1]) * 0.5f;
                Point p{x, y * std::cos(tilt) - z * std::sin(tilt)};
                float depth = y * std::sin(tilt) + z * std::cos(tilt);
                if (i) stroke(previous, p, type == 101 ? 0.014f + clamp01(values[2]) * 0.01f : 0.003f,
                              (depth + previousDepth) * 0.5f, strand / float(std::max(strands - 1, 1)) * 0.7f + 0.15f);
                previous = p; previousDepth = depth;
            }
        }
    } else if (type == 121) {
        int count = 60 + static_cast<int>(clamp01(values[0]) * 100);
        for (int i = 0; i < count; ++i) {
            Point root{(random(i, 1) - 0.5f) * 0.08f, 0.56f};
            Point tip{(random(i, 2) - 0.5f) * 0.9f, -0.42f + random(i, 3) * 0.48f};
            Point previous = root;
            for (int j = 1; j <= 80; ++j) {
                float t = j / 80.0f;
                Point p{root.x + (tip.x - root.x) * ((1 - clamp01(values[1])) * t + clamp01(values[1]) * std::sin(t * PI * 0.5f)), root.y + (tip.y - root.y) * t};
                stroke(previous, p, 0.0008f + t * t * 0.0016f, i * 0.001f, random(i, 4));
                previous = p;
            }
            stroke(tip, tip, 0.0035f, i * 0.001f + 1, random(i, 4));
        }
    } else if (type == 105) {
        for (int i = 0; i < 10 + static_cast<int>(clamp01(values[0]) * 18); ++i) {
            Point center{(random(i, 1) - 0.5f) * 1.15f, (random(i, 2) - 0.5f) * 1.15f};
            float orientation = random(i, 3) * TAU, radius = 0.05f + random(i, 4) * 0.12f;
            Point previous = center;
            for (int j = 1; j <= 180; ++j) {
                float t = j / 180.0f, angle = orientation + t * TAU * (1.1f + clamp01(values[1]) * 1.3f);
                Point p = center + Point{std::cos(angle), std::sin(angle)} * (radius * t);
                stroke(previous, p, 0.0013f + t * 0.0016f, 0, 0.6f);
                previous = p;
            }
        }
    }
    return !cancelled();
}

bool DetailedTexture::prepare(bool (*cancelled)()) {
    if (type == 71) return reactionDiffusion(cancelled);
    if (type == 73) return slimeMold(cancelled);
    if (type == 55 || type == 59 || type == 72 || type == 74 || type == 79 || type == 81 || type == 94) return branching(cancelled);
    if (type == 114) return packing(cancelled);
    if (type == 75 || type == 101 || type == 105 || type == 116 || type == 121) return ribbons(cancelled);
    return !cancelled();
}

uint32_t DetailedTexture::renderStrokes(Point p) const {
    float distance = 10, radius = 0.002f, value = 0.5f, depth = -10, halo = 0;
    for (int index : nearby(p)) {
        const Stroke &s = strokes[index];
        float d = segment(p, s.a, s.b);
        halo = std::max(halo, std::exp(-d / std::max(s.radius * 2, aa)) * (1 - smoothstep(s.radius * 3, s.radius * 5, d)));
        bool ribbon = type == 101 || type == 116;
        float surfaceDepth = s.depth + std::sqrt(std::max(0.0f, s.radius * s.radius - d * d));
        if ((ribbon && d < s.radius + aa && surfaceDepth > depth) ||
            (!ribbon && d - s.radius < distance - radius)) {
            distance = d; radius = s.radius; value = s.value; depth = surfaceDepth;
        }
    }
    uint32_t background = colors[0];
    if (type == 81) {
        float ocean = smoothstep(0.25f, 0.53f, p.y + (grain(p, 8) - 0.5f) * 0.05f);
        background = mixColor(mixColor(colors[0], colors[1], 0.45f + grain(p, 25) * 0.25f), colors[2], ocean * 0.65f);
        uint32_t water = mixColor(colors[2], colors[3], 0.2f + halo * 0.25f);
        return mixColor(background, water, 1 - smoothstep(radius - aa, radius + aa, distance));
    }
    if (type == 59) background = mixColor(background, colors[1], grain(p, 8) * 0.28f);
    float feather = aa * (1.3f - clamp01(values[2]) * 0.6f);
    float cover = 1 - smoothstep(radius - feather, radius + feather, distance);
    float tube = std::sqrt(clamp01(1 - distance * distance / std::max(radius * radius, 0.0000001f)));
    uint32_t color = mixColor(colors[1], colors[2], clamp01(value * std::max(values[4], 0.2f)));
    if (type == 94 || type == 75 || type == 121) color = mixColor(colors[2], colors[3], 0.55f);
    color = mixColor(color, colors[3], tube * (type == 101 || type == 116 ? 0.55f : 0.2f));
    if (type == 55 || type == 74) color = mixColor(colors[2], colors[3], tube * 0.7f);
    background = mixColor(background, colors[2], halo * clamp01(values[3]) * (type == 94 ? 0.3f : 0.14f));
    return mixColor(background, color, cover);
}

uint32_t DetailedTexture::surface(Point p) const {
    float a = values[0], b = values[1], c = values[2], d = values[3], e = values[4];
    if (type == 4) {
        Point q{p.x * 1.3f + (grain({p.x * 2, p.y * 0.15f}, 1.2f) - 0.5f) * b * 2, p.y};
        float rings = q.x * (12 + a * 2) + grain({q.x, q.y * 0.2f}, 3) * 3;
        float line = 0.5f + 0.5f * std::sin(rings * TAU);
        float pores = grain({q.x * 80, q.y * 2}, 2);
        uint32_t result = mixColor(colors[0], colors[1], 0.3f + std::pow(line, 0.4f) * 0.55f);
        return mixColor(result, colors[2], std::pow(line, 9) * 0.25f + pores * 0.09f);
    }
    if (type == 8 || type == 61) {
        float horizontal = type == 8 ? a * 1.5f : 35 + a * 35;
        float vertical = type == 8 ? b * 1.5f : horizontal;
        Point q{p.x * horizontal, p.y * vertical};
        int x = static_cast<int>(std::floor(q.x)), y = static_cast<int>(std::floor(q.y));
        int period = 2 + static_cast<int>(std::lround(clamp01(values[1]) * 6));
        bool over = type == 8 ? ((x + y) & 1) == 0 : (((x - y) % period) + period) % period < period / 2;
        float across = fract(over ? q.y : q.x) - 0.5f;
        float lengthwise = fract(over ? q.x : q.y);
        float fiber = std::sin((over ? q.y : q.x) * TAU * 7) * 0.05f;
        float light = 0.25f + std::sqrt(clamp01(1 - across * across * 4)) * 0.5f + fiber - std::pow(std::abs(lengthwise - 0.5f) * 2, 10) * 0.15f;
        uint32_t base = type == 8 ? (over ? colors[0] : colors[1]) : mixColor(colors[1], colors[2], over ? 0.2f : 0.8f);
        return shade(base, light);
    }
    if (type == 22) {
        Point q = p * 65;
        int x = static_cast<int>(std::floor(q.x)), y = static_cast<int>(std::floor(q.y));
        float first = 10, second = 10;
        for (int oy = -1; oy <= 1; ++oy) for (int ox = -1; ox <= 1; ++ox) {
            Point center{x + ox + random(x + ox, y + oy), y + oy + random(x + ox + 31, y + oy)};
            float distance = length(q - center);
            if (distance < first) { second = first; first = distance; } else second = std::min(second, distance);
        }
        float creases = 1 - smoothstep(0.02f, 0.12f + a * 0.08f, second - first);
        float pebble = std::sqrt(clamp01(1 - first));
        uint32_t base = mixColor(colors[0], colors[1], 0.4f + grain(p, 3) * 0.4f);
        base = mixColor(base, colors[2], pebble * (0.15f + e * 0.4f));
        return mixColor(base, colors[3], creases * (0.4f + c * 0.4f));
    }
    if (type == 34) {
        float nap = grain({p.x * 100, p.y * 200}, 2);
        float fold = std::sin(p.x * 8 + grain(p, 2) * 3);
        float sheen = std::pow(clamp01(fold * 0.5f + 0.5f), 2 + c * 4) * d;
        uint32_t base = mixColor(colors[0], colors[1], 0.35f + nap * 0.12f + fold * e * 0.15f);
        base = mixColor(base, colors[2], sheen * 0.55f);
        return mixColor(base, colors[3], std::pow(nap, 8) * sheen * 0.2f);
    }
    if (type == 28 || type == 63) {
        float flow = grain({p.x + grain(p, 2) * 0.3f, p.y}, type == 28 ? 2.5f : 1.8f);
        float thickness = flow * (type == 28 ? a * 0.35f : 3 + a * 5) + grain(p, 0.6f) * 2;
        float phase = thickness + p.y * 0.7f;
        float red = 0.5f + 0.5f * std::cos(phase * TAU);
        float green = 0.5f + 0.5f * std::cos(phase * TAU * 1.17f + 1.2f);
        float blue = 0.5f + 0.5f * std::cos(phase * TAU * 1.36f + 2.4f);
        uint32_t color = mixColor(colors[1], colors[2], red);
        color = mixColor(color, colors[3], green * 0.55f);
        float strength = type == 28 ? clamp01(c) * (1 - clamp01(d) * 0.5f) : 0.8f;
        uint32_t result = mixColor(colors[0], color, strength * (0.3f + blue * 0.7f));
        if (type == 63) {
            float bubble = std::abs(length(p - Point{0.06f, 0}) - 0.39f);
            result = mixColor(colors[0], result, 1 - smoothstep(0.39f, 0.405f, length(p - Point{0.06f, 0})));
            result = mixColor(result, colors[3], std::exp(-bubble * 200) * 0.7f);
        }
        return result;
    }
    if (type == 36) {
        Point q = rotate(p, d * PI);
        float foldA = std::abs(fract(q.x * (4 + a * 0.25f) + grain(p, 3) * b * 0.6f) - 0.5f);
        float foldB = std::abs(fract((q.y + q.x * 0.4f) * 7) - 0.5f);
        float phase = fract(q.x * 0.8f + q.y * 0.25f + foldA * 0.8f + grain(p, 1.5f) * 0.65f);
        uint32_t spectrum = phase < 0.5f ? mixColor(colors[1], colors[2], phase * 2) : mixColor(colors[2], colors[3], phase * 2 - 1);
        spectrum = shade(spectrum, 0.3f + foldA * 0.65f + foldB * 0.2f);
        uint32_t foil = mixColor(colors[0], spectrum, clamp01(c) * 0.85f);
        return mixColor(foil, colors[3], std::exp(-std::abs(foldA - 0.43f) * 90) * e * 0.5f);
    }
    if (type == 56) {
        float orientation = p.x * 1.2f;
        int domains = 4 + static_cast<int>(a * 8);
        for (int i = 0; i < domains; ++i) {
            Point center{(random(i, 1) - 0.5f) * 1.2f, (random(i, 2) - 0.5f) * 1.2f};
            Point q = p - center;
            orientation += (i % 2 ? -0.5f : 0.5f) * std::atan2(q.y, q.x);
        }
        float extinction = std::pow(std::sin(orientation * 2), 2);
        float spectrum = 0.5f + 0.5f * std::sin(orientation * 2 + grain(p, 2) * b * 6);
        uint32_t domain = mixColor(colors[1], colors[2], spectrum);
        domain = mixColor(domain, colors[3], std::pow(spectrum, 7) * d * 0.6f);
        return mixColor(colors[0], domain, std::pow(extinction, 0.5f + c));
    }
    if (type == 51) {
        Point q = p + Point{(grain(p, 4) - 0.5f) * 0.06f * b, (grain(p + Point{3, 1}, 4) - 0.5f) * 0.06f * b};
        float radius = length({q.x, q.y * 1.15f}), angle = std::atan2(q.y, q.x);
        float band = 0.5f + 0.5f * std::sin(radius * (70 + a * 70) + grain(p, 3) * 2);
        uint32_t agate = mixColor(colors[1], colors[2], band * 0.75f);
        agate = mixColor(agate, colors[3], std::pow(band, 10) * 0.55f);
        float cavity = 0.19f + std::sin(angle * 7 + grain(p, 7)) * 0.018f;
        float faceting = std::floor(angle / TAU * 24) / 24;
        float crystal = 0.5f + 0.5f * std::cos(angle * 32 + radius * 80);
        uint32_t interior = mixColor(colors[0], colors[1], crystal * 0.45f);
        interior = mixColor(interior, colors[3], std::pow(crystal, 14) * c * smoothstep(0.03f, cavity, radius));
        interior = mixColor(interior, colors[2], faceting * 0.1f);
        return mixColor(interior, agate, smoothstep(cavity - aa, cavity + aa, radius));
    }
    if (type == 57 || type == 65) {
        float count = type == 57 ? 10 + a * 12 : 24 + a * 24;
        Point q = p * count;
        int row = static_cast<int>(std::floor(q.y));
        float x = fract(q.x + (row & 1) * 0.5f) - 0.5f, y = fract(q.y);
        float edge = std::abs(y - (0.94f - std::abs(x) * (type == 57 ? 1.1f : 0.8f)));
        float ridge = std::sqrt(clamp01(1 - x * x * 4)) * (0.2f + y * 0.7f);
        float marking = type == 65 ? 0.5f + 0.5f * std::sin(p.x * 25 + std::sin(p.y * 12) * 2) : 0.35f + ridge * 0.4f;
        uint32_t base = mixColor(colors[1], colors[2], marking);
        base = shade(base, 0.35f + ridge * 0.4f);
        base = mixColor(base, colors[3], std::pow(ridge, 7) * d);
        return mixColor(base, colors[0], 1 - smoothstep(0.015f, 0.055f + aa * count, edge));
    }
    if (type == 62) {
        Point q = p * (9 + a * 12);
        int x = static_cast<int>(std::floor(q.x)), y = static_cast<int>(std::floor(q.y));
        Point local{fract(q.x) - 0.5f, fract(q.y) - 0.5f};
        if (random(x, y) > 0.5f) local = rotate(local, PI * 0.5f);
        float trace = std::min(segment(local, {-0.5f, -0.2f}, {0.15f, -0.2f}), segment(local, {0.15f, -0.2f}, {0.45f, 0.5f}));
        float pad = std::abs(length(local - Point{-0.1f, 0.23f}) - 0.1f);
        float coverage = 1 - smoothstep(0.015f, 0.04f + aa * 12, std::min(trace, pad));
        uint32_t board = mixColor(colors[0], colors[1], 0.2f + grain(p, 60) * 0.2f);
        board = mixColor(board, colors[2], coverage * (0.65f + c * 0.3f));
        return mixColor(board, colors[3], std::pow(coverage, 8) * d * 0.25f);
    }
    if (type == 120) {
        Point q = rotate(p, -0.2f) * (10 + a * 12);
        Point cell{std::floor(q.x), std::floor(q.y)}, f{fract(q.x), fract(q.y)};
        bool diagonal = random(static_cast<int>(cell.x), static_cast<int>(cell.y)) > 0.5f;
        float side = diagonal ? f.x + f.y : f.x - f.y;
        float face = side > (diagonal ? 1.0f : 0.0f) ? 0.7f : 0.25f;
        float relief = (f.x * 0.4f + f.y * 0.3f) * c;
        uint32_t paper = mixColor(colors[1], colors[2], random(static_cast<int>(cell.x), static_cast<int>(cell.y) + 37) * 0.65f);
        paper = shade(paper, face + relief * 0.3f);
        float edge = std::min({f.x, f.y, 1 - f.x, 1 - f.y, std::abs(side - (diagonal ? 1 : 0)) * 0.7071f});
        return mixColor(paper, colors[3], (1 - smoothstep(0.006f, 0.025f + aa * 10, edge)) * d * 0.65f);
    }
    return colors[0];
}

uint32_t DetailedTexture::botanical(Point p) const {
    float a = values[0], b = values[1], c = values[2], d = values[3];
    if (type == 87) {
        float curve = b * 0.04f * std::sin(p.y * 9);
        float y = p.y / 0.47f, half = 0.29f * std::pow(clamp01(1 - y * y), 0.7f);
        float x = p.x - curve;
        float leaf = 1 - smoothstep(half - aa, half + aa, std::abs(x));
        if (std::abs(y) > 1) leaf = 0;
        float progress = fract((p.y + 0.5f) * (12 + a * 10) + std::abs(x) * 10);
        float side = std::min(progress, 1 - progress) / 18;
        float veins = std::min(std::abs(x), side);
        uint32_t body = mixColor(colors[1], colors[2], 0.35f + grain(p, 60) * 0.35f);
        body = mixColor(body, colors[3], (1 - smoothstep(0.001f, 0.009f - c * 0.006f, veins)) * 0.75f);
        return mixColor(colors[0], body, leaf);
    }
    if (type == 48) {
        Point q{p.x + p.y * p.y * c * 0.3f, p.y + 0.09f};
        float eyeRadius = length({q.x / std::max(a, 0.08f), q.y / std::max(a * 1.2f, 0.1f)});
        float featherRadius = length({q.x / 0.38f, q.y / 0.5f});
        float mask = 1 - smoothstep(0.94f, 1.03f, featherRadius);
        float barbPhase = (std::abs(q.x) + q.y * 0.45f - std::sin(q.y * 5) * 0.12f) * std::max(b, 8.0f) * 8;
        float fibers = 0.5f + 0.5f * std::sin(barbPhase);
        uint32_t body = mixColor(colors[1], colors[3], std::pow(fibers, 4) * d * 0.6f);
        body = shade(body, 0.25f + fibers * 0.45f);
        float gold = 1 - smoothstep(0.78f, 1.05f, eyeRadius);
        body = mixColor(body, colors[3], gold * (0.5f + fibers * 0.35f));
        body = mixColor(body, colors[2], 1 - smoothstep(0.53f, 0.73f, eyeRadius));
        body = mixColor(body, colors[0], 1 - smoothstep(0.22f, 0.4f, eyeRadius));
        body = mixColor(body, colors[1], std::pow(fibers, 8) * gold * 0.35f);
        float shaft = (1 - smoothstep(0.002f, 0.005f, std::abs(q.x))) * smoothstep(0.1f, 0.4f, q.y);
        return mixColor(mixColor(colors[0], body, mask), colors[3], shaft * 0.5f);
    }
    if (type == 13 || type == 20 || type == 25) {
        float grid = type == 13 ? 10 + a * 11 : type == 20 ? 20 + a * 15 : 30 + a * 34;
        Point q = p * grid;
        int x = static_cast<int>(std::floor(q.x)), y = static_cast<int>(std::floor(q.y));
        float best = 10, light = 0, identity = 0;
        for (int oy = -1; oy <= 1; ++oy) for (int ox = -1; ox <= 1; ++ox) {
            int gx = x + ox, gy = y + oy;
            Point local = q - Point{gx + random(gx, gy), gy + random(gx + 71, gy)};
            local = rotate(local, random(gx, gy + 41) * TAU);
            float shape;
            if (type == 20) {
                float curve = local.y * local.y * 0.7f;
                shape = std::hypot((local.x + curve) * 7, local.y * 0.65f);
            } else {
                shape = std::abs(local.x) / 0.46f + local.y * local.y / (type == 13 ? 0.85f : 0.45f);
            }
            if (shape < best) { best = shape; light = local.x * -0.2f - local.y * 0.2f + 0.55f; identity = random(gx + 37, gy); }
        }
        uint32_t leaf = mixColor(colors[1], colors[2], identity * 0.8f);
        leaf = shade(leaf, light + std::pow(clamp01(1 - best), 3) * 0.18f);
        float coverage = 1 - smoothstep(0.72f, 0.94f, best);
        return mixColor(colors[0], leaf, coverage);
    }
    if (type == 112) {
        float golden = 2.39996323f + (b - 0.62f) * 0.025f;
        float r = length(p), angle = std::atan2(p.y, p.x);
        int count = 220 + static_cast<int>(a * 360), estimate = static_cast<int>(r * r / (0.46f * 0.46f) * count);
        float nearest = 10, light = 0;
        for (int offset = -28; offset <= 28; ++offset) {
            int i = std::clamp(estimate + offset, 0, count - 1);
            float radius = std::sqrt((i + 0.5f) / count) * 0.46f;
            Point center = rotate({radius, 0}, i * golden + random(0) * TAU);
            Point q = rotate(p - center, i * golden);
            float distance = length({q.x * 0.75f, q.y * 1.3f});
            if (distance < nearest) { nearest = distance; light = 0.3f + radius * 0.8f + (q.x - q.y) * 15; }
        }
        float size = 0.0125f;
        uint32_t seedColor = mixColor(colors[1], colors[2], clamp01(light));
        seedColor = shade(seedColor, 0.25f + std::sqrt(clamp01(1 - nearest / size)) * 0.5f);
        uint32_t background = colors[0];
        if (r > 0.4f && r < 0.51f) {
            float petal = std::pow(0.5f + 0.5f * std::cos(angle * 34), 2);
            background = mixColor(background, colors[3], petal * smoothstep(0.52f, 0.44f, r) * 0.6f);
        }
        return mixColor(background, seedColor, 1 - smoothstep(size - aa, size + aa, nearest));
    }
    return colors[0];
}

uint32_t DetailedTexture::natural(Point p) const {
    float a = values[0], b = values[1], c = values[2], d = values[3], e = values[4];
    if (type == 71 || type == 73) {
        Point q{fract(p.x + 0.5f) * fieldSize, fract(p.y + 0.5f) * fieldSize};
        auto sample = [&](float x, float y) {
            int ix = (static_cast<int>(std::floor(x)) + fieldSize) % fieldSize, iy = (static_cast<int>(std::floor(y)) + fieldSize) % fieldSize;
            return field[iy * fieldSize + ix];
        };
        float tx = fract(q.x), ty = fract(q.y);
        float value = (sample(q.x, q.y) * (1 - tx) + sample(q.x + 1, q.y) * tx) * (1 - ty) +
                (sample(q.x, q.y + 1) * (1 - tx) + sample(q.x + 1, q.y + 1) * tx) * ty;
        float gx = sample(q.x + 1, q.y) - sample(q.x - 1, q.y);
        float gy = sample(q.x, q.y + 1) - sample(q.x, q.y - 1);
        if (type == 71) {
            float edge = 0.04f + (1 - clamp01(c)) * 0.12f;
            float body = smoothstep(0.15f - edge, 0.15f + edge, value);
            uint32_t result = mixColor(colors[0], colors[1], 0.1f + grain(p, 20) * 0.1f);
            uint32_t raised = mixColor(colors[2], colors[3], clamp01(0.2f + d * 0.4f - gx * 3 - gy * 3));
            return mixColor(result, raised, body);
        }
        float trail = std::pow(smoothstep(0.08f, 0.85f, value), 0.65f + clamp01(c) * 0.8f);
        uint32_t result = mixColor(colors[0], colors[1], trail * 0.45f);
        result = mixColor(result, colors[2], std::pow(trail, 2) * 0.85f);
        return mixColor(result, colors[3], std::pow(trail, 8) * d * 0.5f);
    }
    if (type == 26) {
        float height = clamp01(0.65f + p.y);
        float flow = grain({p.x * 3, p.y * 1.2f}, 3);
        float curl = std::sin(p.x * (10 + a) + flow * (4 + b * 0.1f) + p.y * 6);
        float tongues = 0.5f + 0.5f * curl;
        float flame = smoothstep(0.1f, 1.0f, height * (0.5f + tongues * 0.7f) + flow * 0.25f + p.y * 0.3f);
        return palette(std::pow(flame, 1.15f) * (0.6f + c * 0.4f));
    }
    if (type == 27) {
        uint32_t result = colors[0];
        float intensity = 0;
        for (int layer = 0; layer < 3; ++layer) {
            float bend = std::sin(p.x * (4 + a * 0.4f) + layer * 1.7f) * 0.08f +
                    (grain({p.x * 3, float(layer)}, 1) - 0.5f) * b * 0.006f;
            float edge = -0.14f + layer * 0.11f + bend;
            float y = p.y - edge;
            float curtain = std::exp(-std::max(-y, 0.0f) * 14) * (1 - smoothstep(0, 0.015f, y));
            float rays = 0.45f + 0.55f * grain({p.x * 120, float(layer)}, 1);
            float brightness = curtain * rays * clamp01(c) * (0.7f - layer * 0.13f);
            result = mixColor(result, mixColor(colors[1], colors[2], clamp01(-y * 5)), brightness);
            result = mixColor(result, colors[3], brightness * smoothstep(0.06f, 0.24f, -y) * 0.6f);
            intensity += brightness;
        }
        float star = random(static_cast<int>((p.x + 1) / aa), static_cast<int>((p.y + 1) / aa));
        return mixColor(result, colors[3], smoothstep(0.9995f - clamp01(d) * 0.0004f, 1, star) * (1 - intensity));
    }
    if (type == 38) {
        float radius = length({p.x, p.y * 1.3f}), angle = std::atan2(p.y * 1.3f, p.x);
        float phase = angle * std::max(a, 2.0f) + std::log(radius + 0.035f) * std::max(b * 0.45f, 1.0f);
        float arms = std::pow(0.5f + 0.5f * std::cos(phase + (grain(p, 8) - 0.5f) * c * 3), 5);
        float envelope = std::exp(-radius * 7);
        float dust = smoothstep(0.3f, 0.75f, grain(p, 18));
        uint32_t gas = mixColor(colors[1], colors[2], grain(p, 4));
        uint32_t result = mixColor(colors[0], gas, arms * envelope * (0.6f + dust * 0.7f));
        result = mixColor(result, colors[3], std::exp(-radius * radius * 600) * clamp01(e));
        float stars = smoothstep(0.9994f - clamp01(d) * 0.0004f, 1, random(static_cast<int>(p.x / aa), static_cast<int>(p.y / aa)));
        return mixColor(result, colors[3], stars);
    }
    if (type == 67) {
        Point q = p + Point{(grain(p, 3) - 0.5f) * b * 0.28f, (grain(p + Point{7, 2}, 3) - 0.5f) * b * 0.28f};
        float ridge = 1 - std::abs(grain(q, 3 + a * 5) * 2 - 1);
        float detail = 1 - std::abs(grain(q + Point{3, 9}, 17) * 2 - 1);
        float filaments = std::pow(ridge, 8 + c * 12) * (0.2f + grain(p, 2) * 0.8f);
        uint32_t result = mixColor(colors[0], colors[1], std::pow(ridge, 5) * 0.35f);
        result = mixColor(result, colors[2], filaments * 0.7f);
        return mixColor(result, colors[3], std::pow(filaments * detail, 4) * d);
    }
    if (type == 39) {
        float field = 0;
        for (int i = 0; i < 4 + static_cast<int>(a * 1.6f); ++i) {
            Point center{(random(i, 1) - 0.5f) * 1.1f, (random(i, 2) - 0.5f) * 1.1f};
            Point q = p - center;
            float radius = 0.025f + random(i, 3) * (0.025f + clamp01(b) * 0.12f);
            field += radius * radius / std::max(dot(q, q), 0.00001f);
        }
        float body = smoothstep(0.8f, 1.15f, field);
        uint32_t lava = mixColor(colors[1], colors[2], smoothstep(1, 2.5f, field));
        lava = mixColor(lava, colors[3], smoothstep(2, 6, field) * clamp01(d) * 0.45f);
        return mixColor(colors[0], lava, body);
    }
    if (type == 49) {
        float radius = length(p), angle = std::atan2(p.y, p.x);
        float growth = 0.18f, outer = 0.22f * std::exp(growth * (angle + PI));
        float mask = 1 - smoothstep(outer - aa, outer + aa, radius);
        float phase = std::log(std::max(radius, 0.003f) / 0.012f) / growth - angle;
        float spiral = std::abs(std::sin(phase * 0.5f));
        float chambers = std::abs(std::sin(phase * (1.1f + b * 0.025f) + radius * 25));
        float relief = std::exp(-spiral * 22) + std::exp(-chambers * 25) * d;
        uint32_t shell = mixColor(colors[1], colors[2], 0.55f + grain(p, 16) * 0.2f);
        shell = mixColor(shell, colors[3], relief * 0.75f);
        shell = shade(shell, 0.5f - p.y * 0.25f - spiral * 0.12f);
        return mixColor(colors[0], shell, mask);
    }
    if (type == 54) {
        uint32_t result = mixColor(colors[0], colors[1], 0.08f + grain(p, 3) * 0.12f);
        for (int i = 0; i < 12; ++i) {
            float depth = random(i, 3), x = (i / 11.0f - 0.5f) * 1.4f;
            float sway = std::sin(p.y * (4 + depth * 3) + random(i, 4) * TAU) * (0.025f + b * 0.04f);
            float blade = x + sway + std::sin(p.y * 28 + i) * 0.025f;
            float width = 0.003f + std::pow(0.5f + 0.5f * std::sin(p.y * 22 + i), 2) * (0.012f + a * 0.015f);
            float mask = 1 - smoothstep(width, width + aa, std::abs(p.x - blade));
            uint32_t leaf = mixColor(colors[1], colors[2], 0.2f + depth * 0.65f);
            result = mixColor(result, leaf, mask * (0.3f + depth * 0.65f));
        }
        float rays = std::pow(clamp01(0.5f + 0.5f * std::sin((p.x + p.y * 0.18f) * 34)), 10);
        return mixColor(result, colors[3], rays * smoothstep(0.5f, -0.5f, p.y) * d * 0.1f);
    }
    if (type == 12) {
        float wave = 0, gx = 0, gy = 0;
        for (int i = 0; i < 7; ++i) {
            Point center{(random(i, 1) - 0.5f) * 1.4f, (random(i, 2) - 0.5f) * 1.4f};
            Point q = p - center;
            float r = std::max(length(q), 0.001f), phase = r * 80 + random(i, 3) * TAU;
            float fade = std::exp(-r * 3);
            wave += std::sin(phase) * fade;
            gx += std::cos(phase) * q.x / r * fade;
            gy += std::cos(phase) * q.y / r * fade;
        }
        uint32_t water = mixColor(colors[0], colors[1], 0.35f + wave * 0.12f);
        return mixColor(water, colors[2], std::pow(clamp01(0.4f - gx * 0.2f - gy * 0.35f), 4) * clamp01(e));
    }
    if (type == 15) {
        float h = grain(p, 4) * 0.7f + grain(p, 12) * 0.3f;
        float dx = grain(p + Point{0.002f, 0}, 4) - grain(p - Point{0.002f, 0}, 4);
        float dy = grain(p + Point{0, 0.002f}, 4) - grain(p - Point{0, 0.002f}, 4);
        uint32_t result;
        if (h < 0.42f) result = mixColor(colors[0], colors[1], smoothstep(0.22f, 0.42f, h) * 0.3f);
        else if (h < 0.65f) result = mixColor(colors[1], colors[2], smoothstep(0.42f, 0.65f, h));
        else result = mixColor(colors[2], colors[3], smoothstep(0.65f, 0.8f, h));
        return shade(result, clamp01(0.5f - dx * 12 - dy * 16));
    }
    if (type == 16) {
        float crack = 10;
        for (int i = 0; i < 7; ++i) {
            float angle = random(i, 1) * PI, offset = (random(i, 2) - 0.5f) * 0.9f;
            Point q = rotate(p, angle);
            float path = offset + (grain({q.y * 8, float(i)}, 1) - 0.5f) * 0.05f;
            crack = std::min(crack, std::abs(q.x - path));
        }
        uint32_t ice = mixColor(colors[0], colors[1], 0.45f + grain(p, 3) * 0.4f);
        ice = mixColor(ice, colors[2], grain(p, 80) * 0.18f);
        return mixColor(ice, colors[3], (1 - smoothstep(0.0007f, 0.003f + aa, crack)) * 0.9f);
    }
    if (type == 58 || type == 70 || type == 78 || type == 88 || type == 89 || type == 107 || type == 108) {
        float grid = type == 58 ? 6 + a * 8 : type == 70 ? 3 + a * 3.2f : type == 78 ? 4 + a * 5 :
                type == 89 ? 8 + a * 9 : type == 108 ? 4 + a * 5 : type == 88 ? 12 + a * 20 : 24 + a * 20;
        Point q = p * grid;
        int x = static_cast<int>(std::floor(q.x)), y = static_cast<int>(std::floor(q.y));
        float nearest = 10, second = 10, variation = 0;
        Point local{};
        for (int oy = -1; oy <= 1; ++oy) for (int ox = -1; ox <= 1; ++ox) {
            int gx = x + ox, gy = y + oy;
            Point v = q - Point{gx + random(gx, gy), gy + random(gx + 91, gy)};
            float distance = length(v);
            float identity = random(gx + 37, gy);
            if (type == 70) {
                Point q = rotate(v, identity * TAU);
                float aspect = 0.65f + identity * (0.6f + b * 1.5f);
                distance = length({q.x * aspect, q.y / aspect});
            } else if (type == 78) {
                Point q = rotate(v, identity * TAU);
                float radius = 0.24f + identity * 0.15f;
                distance = length({q.x / radius, q.y / (radius * (0.9f + b * 0.65f))});
            } else if (type == 89) {
                float radius = 0.14f + identity * 0.18f;
                distance = length({v.x / radius, v.y / (radius * (1 + b * 0.7f))});
            }
            if (distance < nearest) { second = nearest; nearest = distance; local = v; variation = random(gx + 37, gy); }
            else second = std::min(second, distance);
        }
        if (type == 58) {
            float radius = 0.025f + variation * 0.04f;
            float spark = std::exp(-nearest * nearest / (radius * radius));
            float glow = std::exp(-nearest * 5) * d;
            uint32_t background = mixColor(colors[0], colors[1], grain(p, 3) * 0.2f);
            return mixColor(mixColor(background, colors[2], glow * 0.5f), colors[3], spark);
        }
        if (type == 70) {
            Point v = rotate(local, variation * TAU);
            float aspect = 0.65f + variation * (0.6f + b * 1.5f);
            float radius = length({v.x * aspect, v.y / aspect});
            float angle = std::atan2(v.y / aspect, v.x * aspect);
            float mask = 1 - smoothstep(0.32f, 0.36f, radius);
            float ribs = std::pow(0.5f + 0.5f * std::cos(angle * (18 + std::floor(a * 12))), 10);
            float pores = std::sin(radius * 100) * std::cos(angle * 30);
            float light = 0.2f + ribs * 0.4f + std::pow(clamp01(pores), 4) * 0.4f;
            uint32_t shell = mixColor(colors[1], colors[2], light);
            shell = mixColor(shell, colors[3], std::exp(-std::abs(radius - 0.3f) * 80) * 0.8f);
            return mixColor(colors[0], shell, mask);
        }
        if (type == 78) {
            Point q = rotate(local, variation * TAU);
            float radius = 0.24f + variation * 0.15f;
            float r = length({q.x / radius, q.y / (radius * (0.9f + b * 0.65f))});
            float nucleus = std::exp(-dot(q - Point{radius * 0.15f, 0}, q - Point{radius * 0.15f, 0}) / (radius * radius * 0.1f));
            uint32_t cell = mixColor(colors[1], colors[2], 0.3f + std::sqrt(clamp01(1 - r * r)) * 0.45f);
            cell = mixColor(cell, colors[0], nucleus * 0.65f);
            cell = mixColor(cell, colors[3], std::exp(-std::abs(r - 0.87f) * 45) * 0.6f);
            return mixColor(colors[0], cell, 1 - smoothstep(0.95f, 1.05f, r));
        }
        if (type == 88) {
            float hole = 0.12f + variation * 0.25f;
            float cavity = 1 - smoothstep(hole * 0.2f, hole, nearest);
            float rim = std::exp(-std::abs(nearest - hole) * 25);
            uint32_t sponge = mixColor(colors[1], colors[2], grain(p, 80) * 0.45f + 0.35f);
            sponge = mixColor(sponge, colors[3], rim * 0.35f);
            return mixColor(sponge, colors[0], cavity * 0.85f);
        }
        if (type == 89) {
            float radius = 0.14f + variation * 0.18f;
            Point v{local.x / radius, local.y / (radius * (1 + b * 0.7f))};
            float r = length(v), dome = std::sqrt(clamp01(1 - r * r));
            uint32_t background = mixColor(colors[0], colors[1], grain(p, 2));
            float lens = grain(p + Point{v.x, v.y} * dome * (0.01f + d * 0.05f), 4);
            uint32_t drop = mixColor(colors[0], colors[2], 0.2f + lens * 0.4f);
            float gleam = std::exp(-length(v - Point{-0.3f, -0.35f}) * 10);
            drop = mixColor(drop, colors[3], gleam * 0.95f);
            drop = mixColor(drop, colors[0], smoothstep(0.7f, 1, r) * 0.55f);
            return mixColor(background, drop, 1 - smoothstep(0.93f, 1.05f, r));
        }
        if (type == 107) {
            float border = second - nearest;
            float hot = smoothstep(0.02f, 0.25f, border);
            uint32_t sun = mixColor(colors[1], colors[2], hot * 0.8f);
            sun = mixColor(sun, colors[3], hot * (0.18f + grain(p, 110) * 0.2f));
            return mixColor(colors[0], sun, 0.25f + hot * 0.75f);
        }
        float radius = 0.16f + variation * 0.23f;
        float r = nearest / radius;
        float bowl = std::exp(-r * r * 2.5f);
        float rim = std::exp(-(r - 1) * (r - 1) * 28);
        float light = clamp01(0.5f + rim * 0.25f - bowl * 0.24f - local.x * bowl * 0.45f - local.y * bowl * 0.35f);
        uint32_t moon = mixColor(colors[1], colors[2], light + (grain(p, 100) - 0.5f) * 0.12f);
        float angle = std::atan2(local.y, local.x);
        float rays = std::pow(0.5f + 0.5f * std::cos(angle * (28 + std::floor(variation * 16)) + variation * TAU), 18);
        rays *= std::exp(-std::max(r - 1, 0.0f) * (4 - b * 2.5f)) * smoothstep(0.95f, 1.15f, r);
        return mixColor(moon, colors[3], rim * 0.22f + rays * (0.08f + d * 0.22f));
    }
    if (type == 110) {
        uint32_t paper = mixColor(colors[0], colors[1], grain(p, 100) * 0.025f);
        int layers = 3 + static_cast<int>(a * 4);
        for (int layer = 0; layer < layers; ++layer) {
            float phase = layer * 11.7f;
            float profile = -0.15f + layer * 0.12f - grain({p.x * (2 + layer * 0.5f), phase}, 1) * (0.25f + layer * 0.04f);
            float mask = smoothstep(profile - 0.003f, profile + 0.003f, p.y);
            float mist = smoothstep(profile, profile + 0.3f, p.y);
            uint32_t ink = mixColor(colors[1], colors[3], (layer + 1) / 6.0f);
            ink = mixColor(ink, colors[0], mist * b * (1.1f - layer * 0.14f));
            paper = mixColor(paper, ink, mask * (0.35f + layer * 0.12f));
        }
        return paper;
    }
    if (type == 111) {
        uint32_t result = mixColor(colors[0], colors[1], 0.1f + std::exp(-std::abs(p.y + 0.05f) * 8) * 0.3f);
        for (int layer = 0; layer < 3; ++layer) {
            float grid = 3 + a * 3.2f + layer * 5, column = std::floor(p.x * grid);
            float top = 0.2f + layer * 0.08f - random(static_cast<int>(column), layer) * 0.48f;
            if (p.y < top) continue;
            float x = fract(p.x * grid), y = fract(p.y * 100);
            uint32_t building = mixColor(colors[0], colors[1], 0.16f + layer * 0.04f);
            float lit = random(static_cast<int>(p.x * grid * 4), static_cast<int>(p.y * 100) + layer * 39);
            if (x > 0.12f && x < 0.88f && y > 0.35f && lit > 0.65f) building = mixColor(colors[2], colors[3], lit);
            float edge = 1 - smoothstep(0.01f, 0.035f, std::min(x, 1 - x));
            building = mixColor(building, layer % 2 ? colors[2] : colors[3], edge * 0.45f);
            result = building;
        }
        float road = smoothstep(0.08f, 0.14f, p.y) * (1 - smoothstep(0.03f + p.y * (0.15f + b * 0.55f), 0.04f + p.y * (0.15f + b * 0.55f), std::abs(p.x)));
        uint32_t asphalt = mixColor(colors[0], colors[1], 0.12f + grain({p.x, p.y * 6}, 30) * 0.15f);
        float reflection = std::pow(clamp01(0.5f + 0.5f * std::sin(p.y * 180)), 7);
        asphalt = mixColor(asphalt, colors[2], reflection * 0.4f);
        return mixColor(result, asphalt, road);
    }
    return colors[0];
}

uint32_t DetailedTexture::geometric(Point p) const {
    float a = values[0], b = values[1];
    if (type == 114) {
        uint32_t result = colors[0];
        for (int index : nearby(p)) {
            const Circle &circle = circles[index];
            float r = length(p - circle.center), edge = std::abs(r - circle.radius);
            if (r < circle.radius) result = mixColor(colors[1], colors[2], circle.value * 0.7f);
            result = mixColor(result, colors[3], 1 - smoothstep(aa * 0.3f, aa * 1.5f + circle.radius * 0.025f, edge));
        }
        return result;
    }
    if (type == 115) {
        Point q = rotate(p * (1 / 0.48f), random(0) * TAU);
        float r = length(q);
        if (r >= 1) return colors[0];
        int sides = 4 + static_cast<int>(std::lround(a * 2));
        float cotangent = 1 / std::tan(PI / (sides == 4 ? 5 : 4));
        float vertexRadius = std::sqrt((cotangent - std::tan(PI / sides)) / (cotangent + std::tan(PI / sides)));
        float center = (1 + vertexRadius * vertexRadius) / (2 * vertexRadius * std::cos(PI / sides));
        float circleRadius = std::sqrt(center * center - 1);
        int reflections = 0;
        for (int i = 0; i < 96; ++i) {
            float angle = std::atan2(q.y, q.x);
            q = rotate(q, -std::round(angle * sides / TAU) * TAU / sides);
            Point v = q - Point{center, 0};
            float squared = dot(v, v);
            if (squared >= circleRadius * circleRadius) break;
            q = Point{center, 0} + v * (circleRadius * circleRadius / squared);
            ++reflections;
        }
        float edge = std::abs(length(q - Point{center, 0}) - circleRadius) / std::max(1 - dot(q, q), 0.001f);
        uint32_t fill = mixColor(colors[1], colors[2], (reflections % 3) / 2.0f);
        fill = mixColor(fill, colors[3], std::exp(-length(q) * 8) * 0.2f);
        float line = 1 - smoothstep(0.008f, 0.023f + b * 0.015f, edge);
        return mixColor(fill, colors[3], line);
    }
    if (type == 106) {
        Point q = p * (6 + a * 7);
        int x = static_cast<int>(std::floor(q.x)), y = static_cast<int>(std::floor(q.y));
        q = {fract(q.x) - 0.5f, fract(q.y) - 0.5f};
        int glyph = static_cast<int>(random(x, y) * 8);
        float distance = 10;
        auto add = [&](Point from, Point to) { distance = std::min(distance, segment(q, from, to)); };
        if (glyph == 0) {
            add({-.15f,-.3f},{-.15f,.3f}); add({-.15f,-.1f},{.2f,-.3f}); add({-.15f,.08f},{.2f,-.12f});
        } else if (glyph == 1) {
            add({0,-.3f},{0,.3f}); add({0,-.05f},{-.23f,-.27f}); add({0,-.05f},{.23f,-.27f});
        } else if (glyph == 2) {
            add({0,-.3f},{0,.3f}); add({0,-.3f},{-.23f,-.07f}); add({0,-.3f},{.23f,-.07f});
        } else if (glyph == 3) {
            add({-.22f,-.28f},{.22f,.28f}); add({.22f,-.28f},{-.22f,.28f});
        } else if (glyph == 4) {
            add({.15f,-.28f},{-.2f,0}); add({-.2f,0},{.15f,.28f});
        } else if (glyph == 5) {
            add({-.18f,-.3f},{-.18f,.3f}); add({-.18f,-.3f},{.2f,-.12f}); add({.2f,-.12f},{-.18f,.05f}); add({-.18f,.05f},{.2f,.3f});
        } else if (glyph == 6) {
            add({0,-.3f},{.22f,0}); add({.22f,0},{0,.3f}); add({0,.3f},{-.22f,0}); add({-.22f,0},{0,-.3f});
        } else {
            add({0,-.3f},{0,.3f}); add({-.2f,-.12f},{.2f,.12f});
        }
        uint32_t stone = mixColor(colors[0], colors[1], grain(p, 35) * 0.08f);
        uint32_t gold = mixColor(colors[2], colors[3], 0.4f + grain(p, 15) * 0.35f);
        float edge = aa * (6 + a * 7);
        return mixColor(stone, gold, 1 - smoothstep(0.018f - edge, 0.025f + edge, distance));
    }
    if (type == 100) {
        Point triangle[]{{-0.18f, 0.2f}, {0.2f, 0.2f}, {0.015f, -0.23f}};
        float edge = std::min({segment(p, triangle[0], triangle[1]), segment(p, triangle[1], triangle[2]), segment(p, triangle[2], triangle[0])});
        float x = p.x, y = p.y;
        bool inside = y < 0.2f && y > -0.23f && x > -0.18f + (0.2f - y) * 0.45f && x < 0.2f - (0.2f - y) * 0.43f;
        uint32_t result = inside ? mixColor(colors[0], colors[1], 0.15f + grain(p, 5) * 0.15f) : colors[0];
        result = mixColor(result, colors[3], std::exp(-edge * 180) * 0.65f);
        if (x < -0.08f) {
            float beam = std::abs(y - (x + 0.08f) * 0.18f);
            result = mixColor(result, colors[3], std::exp(-beam * 240) * 0.9f);
        }
        if (x > 0.12f) {
            float width = 0.025f + (x - 0.12f) * (0.3f + b * 0.4f);
            float spectrum = (y + (x - 0.12f) * 0.1f) / width + 0.5f;
            if (spectrum > 0 && spectrum < 1) {
                uint32_t beam = spectrum < 0.5f ? mixColor(colors[1], colors[2], spectrum * 2) : mixColor(colors[2], colors[3], spectrum * 2 - 1);
                result = mixColor(result, beam, smoothstep(0, 0.05f, spectrum) * smoothstep(1, 0.95f, spectrum));
            }
        }
        return result;
    }
    return colors[0];
}

uint32_t DetailedTexture::pixel(int x, int y) const {
    Point p{(x + 0.5f - width * 0.5f) * scale, (y + 0.5f - height * 0.5f) * scale};
    uint32_t result;
    switch (type) {
        case 4: case 8: case 22: case 28: case 34: case 36: case 51: case 56: case 57: case 61:
        case 62: case 63: case 65: case 120: result = surface(p); break;
        case 13: case 20: case 25: case 48: case 87: case 112: result = botanical(p); break;
        case 55: case 59: case 72: case 74: case 75: case 79: case 81: case 94:
        case 101: case 105: case 116: case 121: result = renderStrokes(p); break;
        case 100: case 106: case 114: case 115: result = geometric(p); break;
        default: result = natural(p); break;
    }
    if (type < 51) return result;
    uint32_t alpha = result >> 24;
    float contrast = std::max(values[4], 0.05f) / 1.5f;
    auto channel = [&](int shift) {
        float value = (result >> shift) & 255;
        float background = ((colors[0] >> shift) & 255) * float(alpha) / std::max(float(colors[0] >> 24), 1.0f);
        return static_cast<uint32_t>(std::lround(std::clamp(background + (value - background) * contrast, 0.0f, float(alpha))));
    };
    return (alpha << 24) | (channel(16) << 16) | (channel(8) << 8) | channel(0);
}

}
