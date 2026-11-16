#pragma once

#include <algorithm>
#include <cstdint>
#include <numeric>
#include <vector>

namespace textures {

enum class MazeAlgorithm { Backtracking, Prim, Kruskal };

struct Maze {
    int columns;
    int rows;
    std::vector<uint8_t> passages;
};

class MazeRandom {
public:
    explicit MazeRandom(uint32_t seed) : state(seed + 0x9e3779b9u) {}

    uint32_t next() {
        uint32_t value = (state += 0x9e3779b9u);
        value = (value ^ (value >> 16)) * 0x21f0aaadu;
        value = (value ^ (value >> 15)) * 0x735a2d97u;
        return value ^ (value >> 15);
    }

    int index(int count) { return static_cast<int>(next() % static_cast<uint32_t>(count)); }

private:
    uint32_t state;
};

template<typename Cancel>
bool buildMaze(Maze &maze, int seed, MazeAlgorithm algorithm, Cancel cancelled) {
    const int count = maze.columns * maze.rows;
    maze.passages.assign(count, 0);
    MazeRandom random(static_cast<uint32_t>(seed));
    struct Edge { int from; int to; uint8_t direction; };
    auto connect = [&](const Edge &edge) {
        maze.passages[edge.from] |= edge.direction;
        maze.passages[edge.to] |= edge.direction == 1 ? 4 : edge.direction == 2 ? 8 :
                edge.direction == 4 ? 1 : 2;
    };
    auto neighbors = [&](int index, auto add) {
        int x = index % maze.columns, y = index / maze.columns;
        if (y > 0) add(Edge{index, index - maze.columns, 1});
        if (x + 1 < maze.columns) add(Edge{index, index + 1, 2});
        if (y + 1 < maze.rows) add(Edge{index, index + maze.columns, 4});
        if (x > 0) add(Edge{index, index - 1, 8});
    };

    if (algorithm == MazeAlgorithm::Kruskal) {
        std::vector<Edge> edges;
        edges.reserve(static_cast<size_t>(count) * 2);
        for (int index = 0; index < count; ++index) {
            if ((index & 1023) == 0 && cancelled()) return false;
            neighbors(index, [&](Edge edge) {
                if (edge.direction == 2 || edge.direction == 4) edges.push_back(edge);
            });
        }
        for (int index = static_cast<int>(edges.size()) - 1; index > 0; --index) {
            if ((index & 1023) == 0 && cancelled()) return false;
            std::swap(edges[index], edges[random.index(index + 1)]);
        }
        std::vector<int> parent(count), sizes(count, 1);
        std::iota(parent.begin(), parent.end(), 0);
        auto root = [&](int index) {
            while (parent[index] != index) {
                parent[index] = parent[parent[index]];
                index = parent[index];
            }
            return index;
        };
        int processed = 0;
        for (const Edge &edge : edges) {
            if ((processed++ & 1023) == 0 && cancelled()) return false;
            int a = root(edge.from), b = root(edge.to);
            if (a == b) continue;
            if (sizes[a] < sizes[b]) std::swap(a, b);
            parent[b] = a;
            sizes[a] += sizes[b];
            connect(edge);
        }
    } else {
        std::vector<uint8_t> visited(count, 0);
        int start = random.index(count);
        visited[start] = 1;
        if (algorithm == MazeAlgorithm::Backtracking) {
            std::vector<int> stack{start};
            int processed = 0;
            while (!stack.empty()) {
                if ((processed++ & 1023) == 0 && cancelled()) return false;
                Edge options[4];
                int available = 0;
                neighbors(stack.back(), [&](Edge edge) {
                    if (!visited[edge.to]) options[available++] = edge;
                });
                if (!available) {
                    stack.pop_back();
                } else {
                    Edge edge = options[random.index(available)];
                    connect(edge);
                    visited[edge.to] = 1;
                    stack.push_back(edge.to);
                }
            }
        } else {
            std::vector<Edge> frontier;
            auto extend = [&](int index) {
                neighbors(index, [&](Edge edge) {
                    if (!visited[edge.to]) frontier.push_back(edge);
                });
            };
            extend(start);
            int processed = 0;
            while (!frontier.empty()) {
                if ((processed++ & 1023) == 0 && cancelled()) return false;
                int index = random.index(static_cast<int>(frontier.size()));
                Edge edge = frontier[index];
                frontier[index] = frontier.back();
                frontier.pop_back();
                if (visited[edge.to]) continue;
                connect(edge);
                visited[edge.to] = 1;
                extend(edge.to);
            }
        }
    }
    return !cancelled();
}

}
