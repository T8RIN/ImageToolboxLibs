#pragma once

#include "../FastNoiseLite.h"
#include "Maze.h"
#include <array>
#include <cstdint>
#include <vector>

namespace textures {

struct Point {
    float x;
    float y;
    Point operator+(Point p) const { return {x + p.x, y + p.y}; }
    Point operator-(Point p) const { return {x - p.x, y - p.y}; }
    Point operator*(float value) const { return {x * value, y * value}; }
};

class PatternTexture {
public:
    PatternTexture(int type, int seed, int width, int height,
                   const std::vector<float> &parameters, const std::vector<uint32_t> &colors);
    bool prepare(bool (*cancelled)());
    uint32_t pixel(int x, int y) const;

private:
    struct Segment { Point a; Point b; float value; };
    struct Triangle { int type; Point a; Point b; Point c; };
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
    std::vector<Segment> segments;
    std::vector<Triangle> triangles;
    std::array<std::vector<int>, 64 * 64> bins;

    Point position(int x, int y) const;
    float random(int x, int y) const;
    float grain(Point p, float frequency = 1.0f) const;
    uint32_t shade(float light) const;
    uint32_t line(uint32_t background, float distance, float radius, uint32_t color) const;
    uint32_t material(Point p) const;
    uint32_t ornament(Point p) const;
    uint32_t organic(Point p) const;
    uint32_t curves(Point p) const;
    void addSegment(Point a, Point b, float value = 0.0f);
    void indexBounds(int index, Point minimum, Point maximum);
    const std::vector<int> &nearby(Point p) const;
};

uint32_t mazePixel(const Maze &maze, int x, int y, int width, int height,
                   float wallWidth, float padding, bool openings,
                   uint32_t wallColor, uint32_t backgroundColor);

}
