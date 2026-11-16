#pragma once

#include "PatternTextures.h"
#include <array>
#include <vector>

namespace textures {

class DecorativeTexture {
public:
    DecorativeTexture(int type, int seed, int width, int height,
                      const std::vector<float> &parameters, const std::vector<uint32_t> &palette);
    bool prepare(bool (*cancelled)());
    uint32_t pixel(int x, int y) const;

private:
    struct Stroke { Point a; Point b; float radius; float depth; float value; };
    struct Disc { Point center; float radius; float depth; float value; };
    struct Face { Point a; Point b; Point c; Point d; float light; float value; };
    struct Cell { float first; float second; Point local; int x; int y; float value; };
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
    std::vector<Disc> discs;
    std::vector<Face> faces;
    std::vector<uint32_t> motifTile;
    std::array<std::vector<int>, 64 * 64> bins;

    float random(int x, int y = 0) const;
    float grain(Point p, float frequency = 1) const;
    Cell cell(Point p) const;
    uint32_t palette(float t) const;
    uint32_t shade(uint32_t color, float light) const;
    uint32_t line(uint32_t background, uint32_t color, float distance, float radius) const;
    uint32_t finish(uint32_t color) const;
    void bounds(int index, Point low, Point high);
    const std::vector<int> &nearby(Point p) const;
    void stroke(Point a, Point b, float radius, float depth, float value);
    void disc(Point p, float radius, float depth, float value);
    void face(Point a, Point b, Point c, Point d, float light, float value);
    uint32_t minerals(Point p) const;
    uint32_t textiles(Point p) const;
    uint32_t botanical(Point p) const;
    uint32_t geometry(Point p) const;
    uint32_t atmosphere(Point p) const;
    uint32_t curves(Point p) const;
    uint32_t spheres(Point p) const;
    uint32_t gyroid(Point p) const;
};

}
