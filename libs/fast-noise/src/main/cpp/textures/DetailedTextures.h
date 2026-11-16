#pragma once

#include "PatternTextures.h"
#include <array>
#include <vector>

namespace textures {

bool hasDetailedTexture(int type);

class DetailedTexture {
public:
    DetailedTexture(int type, int seed, int width, int height,
                    const std::vector<float> &parameters, const std::vector<uint32_t> &palette);
    bool prepare(bool (*cancelled)());
    uint32_t pixel(int x, int y) const;

private:
    struct Stroke {
        Point a;
        Point b;
        float radius;
        float depth;
        float value;
    };
    struct Circle { Point center; float radius; float value; };
    int type;
    int seed;
    int width;
    int height;
    float scale;
    float aa;
    float extentX;
    float extentY;
    std::array<float, 5> values;
    std::array<uint32_t, 4> colors;
    FastNoiseLite noise;
    std::vector<Stroke> strokes;
    std::vector<Circle> circles;
    std::array<std::vector<int>, 64 * 64> bins;
    std::vector<float> field;
    static constexpr int fieldSize = 192;

    float random(int x, int y = 0) const;
    float grain(Point p, float frequency = 1.0f) const;
    uint32_t palette(float value) const;
    uint32_t shade(uint32_t color, float light) const;
    void indexBounds(int index, Point minimum, Point maximum);
    const std::vector<int> &nearby(Point p) const;
    void stroke(Point a, Point b, float radius, float depth = 0.0f, float value = 0.5f);
    bool reactionDiffusion(bool (*cancelled)());
    bool slimeMold(bool (*cancelled)());
    bool branching(bool (*cancelled)());
    bool packing(bool (*cancelled)());
    bool ribbons(bool (*cancelled)());
    bool attractor(bool (*cancelled)());
    bool cosmicWeb(bool (*cancelled)());
    static constexpr int webResolution = 768;
    static constexpr int attractorResolution = 768;
    uint32_t surface(Point p) const;
    uint32_t natural(Point p) const;
    uint32_t botanical(Point p) const;
    uint32_t geometric(Point p) const;
    uint32_t renderStrokes(Point p) const;
};

}
