#pragma once

#include <algorithm>
#include <cmath>
#include <cstdint>
#include <numeric>
#include <queue>
#include <vector>

namespace textures {

enum class MazeAlgorithm {
    Backtracking, Prim, Kruskal, AldousBroder, Wilson, HuntAndKill, GrowingTree, Eller,
    Sidewinder, BinaryTree, RecursiveDivision, BlobbyDivision, Houston, ReverseDelete,
    GrowingBinaryTree, OriginShift
};

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
    int index(int count) { return int(next() % uint32_t(count)); }
    float unit() { return float(next() >> 8) / 16777216.0f; }
private:
    uint32_t state;
};

template<typename Cancel>
bool buildMaze(Maze &maze, int seed, MazeAlgorithm algorithm, Cancel cancelled,
               float directionBias = 0.5f, float branching = 0.5f, float braid = 0.0f) {
    const int count = maze.columns * maze.rows;
    if (count <= 0) return false;
    directionBias = std::isfinite(directionBias) ? std::clamp(directionBias, 0.0f, 1.0f) : 0.5f;
    branching = std::isfinite(branching) ? std::clamp(branching, 0.0f, 1.0f) : 0.5f;
    braid = std::isfinite(braid) ? std::clamp(braid, 0.0f, 1.0f) : 0.0f;
    maze.passages.assign(count, 0);
    MazeRandom random{uint32_t(seed)};
    struct Edge { int from; int to; uint8_t direction; float priority = 0; };
    auto opposite = [](uint8_t direction) -> uint8_t { return direction == 1 ? 4 : direction == 2 ? 8 : direction == 4 ? 1 : 2; };
    auto connect = [&](const Edge &edge) {
        maze.passages[edge.from] |= edge.direction;
        maze.passages[edge.to] |= opposite(edge.direction);
    };
    auto disconnect = [&](const Edge &edge) {
        maze.passages[edge.from] &= uint8_t(~edge.direction);
        maze.passages[edge.to] &= uint8_t(~opposite(edge.direction));
    };
    auto neighbors = [&](int index, auto add) {
        int x = index % maze.columns, y = index / maze.columns;
        if (y > 0) add(Edge{index, index - maze.columns, 1});
        if (x + 1 < maze.columns) add(Edge{index, index + 1, 2});
        if (y + 1 < maze.rows) add(Edge{index, index + maze.columns, 4});
        if (x > 0) add(Edge{index, index - 1, 8});
    };
    auto edgeTo = [&](int from, int to) {
        uint8_t direction = to / maze.columns == from / maze.columns ? (to > from ? 2 : 8) : (to > from ? 4 : 1);
        return Edge{from, to, direction};
    };
    auto pick = [&](Edge *options, int available) {
        float total = 0;
        for (int i = 0; i < available; ++i) total += options[i].direction == 2 || options[i].direction == 8 ? 0.05f + directionBias : 1.05f - directionBias;
        float selected = random.unit() * total;
        for (int i = 0; i < available - 1; ++i) {
            selected -= options[i].direction == 2 || options[i].direction == 8 ? 0.05f + directionBias : 1.05f - directionBias;
            if (selected < 0) return options[i];
        }
        return options[available - 1];
    };
    auto degree = [&](int index) {
        uint8_t bits = maze.passages[index]; int result = 0;
        for (; bits; bits &= uint8_t(bits - 1)) ++result;
        return result;
    };
    std::vector<uint8_t> visited(count, 0);
    int processed = 0;
    auto stopped = [&] { return (++processed & 1023) == 0 && cancelled(); };
    if (cancelled()) return false;

    if (algorithm == MazeAlgorithm::Kruskal || algorithm == MazeAlgorithm::ReverseDelete) {
        std::vector<Edge> edges;
        for (int i = 0; i < count; ++i) {
            if (stopped()) return false;
            neighbors(i, [&](Edge edge) {
                if (edge.direction != 2 && edge.direction != 4) return;
                float preference = edge.direction == 2 ? 0.1f + directionBias : 1.1f - directionBias;
                edge.priority = algorithm == MazeAlgorithm::ReverseDelete ? random.unit() * preference : random.unit() / preference;
                edges.push_back(edge);
            });
        }
        std::sort(edges.begin(), edges.end(), [&](const Edge &a, const Edge &b) {
            return algorithm == MazeAlgorithm::ReverseDelete ? a.priority > b.priority : a.priority < b.priority;
        });
        std::vector<int> parent(count), sizes(count, 1); std::iota(parent.begin(), parent.end(), 0);
        auto root = [&](int index) {
            while (parent[index] != index) { parent[index] = parent[parent[index]]; index = parent[index]; }
            return index;
        };
        // Reversing the deletion order selects the same surviving tree with a disjoint-set pass.
        for (const Edge &edge : edges) {
            if (stopped()) return false;
            int a = root(edge.from), b = root(edge.to); if (a == b) continue;
            if (sizes[a] < sizes[b]) std::swap(a, b);
            parent[b] = a; sizes[a] += sizes[b]; connect(edge);
        }
    } else if (algorithm == MazeAlgorithm::BinaryTree || algorithm == MazeAlgorithm::Sidewinder) {
        for (int y = 0; y < maze.rows; ++y) {
            int run = 0;
            for (int x = 0; x < maze.columns; ++x) {
                if (stopped()) return false;
                int index = y * maze.columns + x;
                if (algorithm == MazeAlgorithm::BinaryTree) {
                    Edge options[2]; int n = 0;
                    if (y > 0) options[n++] = {index, index - maze.columns, 1};
                    if (x + 1 < maze.columns) options[n++] = {index, index + 1, 2};
                    if (n) connect(pick(options, n));
                } else {
                    bool close = x + 1 == maze.columns || (y > 0 && random.unit() > 0.15f + directionBias * 0.7f);
                    if (close) {
                        if (y > 0) { int member = y * maze.columns + run + random.index(x - run + 1); connect({member, member - maze.columns, 1}); }
                        run = x + 1;
                    } else connect({index, index + 1, 2});
                }
            }
        }
    } else if (algorithm == MazeAlgorithm::Eller) {
        std::vector<int> sets(maze.columns, 0); int nextId = 1;
        for (int y = 0; y < maze.rows; ++y) {
            if (cancelled()) return false;
            for (int &s : sets) if (!s) s = nextId++;
            for (int x = 0; x + 1 < maze.columns; ++x) {
                if (sets[x] == sets[x + 1]) continue;
                if (y + 1 != maze.rows && random.unit() > 0.15f + directionBias * 0.7f) continue;
                int from = sets[x + 1], to = sets[x];
                for (int &s : sets) if (s == from) s = to;
                connect({y * maze.columns + x, y * maze.columns + x + 1, 2});
            }
            if (y + 1 == maze.rows) break;
            std::vector<int> next(maze.columns, 0), handled;
            for (int x = 0; x < maze.columns; ++x) {
                if (std::find(handled.begin(), handled.end(), sets[x]) != handled.end()) continue;
                handled.push_back(sets[x]); std::vector<int> members;
                for (int i = x; i < maze.columns; ++i) if (sets[i] == sets[x]) members.push_back(i);
                int required = members[random.index(int(members.size()))];
                for (int column : members) if (column == required || random.unit() < branching * 0.65f) {
                    int from = y * maze.columns + column;
                    connect({from, from + maze.columns, 4}); next[column] = sets[x];
                }
            }
            sets.swap(next);
        }
    } else if (algorithm == MazeAlgorithm::RecursiveDivision || algorithm == MazeAlgorithm::BlobbyDivision) {
        for (int i = 0; i < count; ++i) neighbors(i, [&](Edge edge) { if (edge.direction == 2 || edge.direction == 4) connect(edge); });
        if (algorithm == MazeAlgorithm::RecursiveDivision) {
            struct Room { int x, y, w, h; }; std::vector<Room> rooms{{0, 0, maze.columns, maze.rows}};
            while (!rooms.empty()) {
                if (stopped()) return false;
                Room r = rooms.back(); rooms.pop_back(); if (r.w < 2 || r.h < 2) continue;
                bool horizontal = random.unit() * (r.w + r.h) < r.h * (1.5f - directionBias);
                if (horizontal) {
                    int split = 1 + random.index(r.h - 1), opening = random.index(r.w);
                    for (int x = 0; x < r.w; ++x) if (x != opening) { int from = (r.y + split - 1) * maze.columns + r.x + x; disconnect({from, from + maze.columns, 4}); }
                    rooms.push_back({r.x, r.y, r.w, split}); rooms.push_back({r.x, r.y + split, r.w, r.h - split});
                } else {
                    int split = 1 + random.index(r.w - 1), opening = random.index(r.h);
                    for (int y = 0; y < r.h; ++y) if (y != opening) { int from = (r.y + y) * maze.columns + r.x + split - 1; disconnect({from, from + 1, 2}); }
                    rooms.push_back({r.x, r.y, split, r.h}); rooms.push_back({r.x + split, r.y, r.w - split, r.h});
                }
            }
        } else {
            std::vector<int> all(count); std::iota(all.begin(), all.end(), 0);
            std::vector<std::vector<int>> regions; regions.push_back(std::move(all));
            std::vector<int> membership(count, 0), side(count, -1); int stamp = 0;
            while (!regions.empty()) {
                if (cancelled()) return false;
                auto region = std::move(regions.back()); regions.pop_back(); if (region.size() < 2) continue;
                ++stamp; for (int i : region) { membership[i] = stamp; side[i] = -1; }
                int first = region[random.index(int(region.size()))], second = first;
                while (second == first) second = region[random.index(int(region.size()))];
                std::vector<int> groups[2], frontier[2];
                auto assign = [&](int i, int group) {
                    side[i] = group; groups[group].push_back(i);
                    neighbors(i, [&](Edge e) { if (membership[e.to] == stamp && side[e.to] < 0) frontier[group].push_back(e.to); });
                };
                assign(first, 0); assign(second, 1);
                while (!frontier[0].empty() || !frontier[1].empty()) {
                    if (stopped()) return false;
                    int group = frontier[0].empty() ? 1 : frontier[1].empty() ? 0 : random.index(2);
                    auto &queue = frontier[group]; int slot = random.index(int(queue.size())), i = queue[slot];
                    queue[slot] = queue.back(); queue.pop_back(); if (side[i] < 0) assign(i, group);
                }
                std::vector<Edge> boundary;
                for (int i : region) neighbors(i, [&](Edge e) {
                    if ((e.direction == 2 || e.direction == 4) && membership[e.to] == stamp && side[i] != side[e.to]) boundary.push_back(e);
                });
                int opening = random.index(int(boundary.size()));
                for (int i = 0; i < int(boundary.size()); ++i) if (i != opening) disconnect(boundary[i]);
                regions.push_back(std::move(groups[0])); regions.push_back(std::move(groups[1]));
            }
        }
    } else if (algorithm == MazeAlgorithm::OriginShift) {
        std::vector<int> parent(count, -1);
        for (int i = 1; i < count; ++i) {
            int to = i >= maze.columns ? i - maze.columns : i - 1;
            parent[i] = to; connect(edgeTo(i, to));
        }
        int origin = 0, steps = count * (16 + int(branching * 64));
        for (int i = 0; i < steps; ++i) {
            if (stopped()) return false;
            Edge options[4]; int n = 0; neighbors(origin, [&](Edge e) { options[n++] = e; });
            if (!n) break;
            Edge e = pick(options, n);
            if (parent[e.to] >= 0) disconnect(edgeTo(e.to, parent[e.to]));
            connect(e); parent[origin] = e.to; parent[e.to] = -1; origin = e.to;
        }
    } else if (algorithm == MazeAlgorithm::AldousBroder || algorithm == MazeAlgorithm::Wilson || algorithm == MazeAlgorithm::Houston) {
        int current = random.index(count), remaining = count - 1; visited[current] = 1;
        if (algorithm != MazeAlgorithm::Wilson) {
            int threshold = algorithm == MazeAlgorithm::Houston ? int(count * (0.35f + branching * 0.3f)) : 0;
            while (remaining > threshold) {
                if (stopped()) return false;
                Edge options[4]; int n = 0; neighbors(current, [&](Edge e) { options[n++] = e; });
                Edge e = pick(options, n);
                if (!visited[e.to]) { connect(e); visited[e.to] = 1; --remaining; }
                current = e.to;
            }
        }
        std::vector<int> positions(count, -1), path;
        for (int start = 0; remaining > 0 && start < count; ++start) {
            if (visited[start]) continue;
            path.clear(); current = start;
            while (!visited[current]) {
                if (stopped()) return false;
                if (positions[current] >= 0) {
                    int keep = positions[current] + 1;
                    while (int(path.size()) > keep) { positions[path.back()] = -1; path.pop_back(); }
                } else { positions[current] = int(path.size()); path.push_back(current); }
                Edge options[4]; int n = 0; neighbors(current, [&](Edge e) { options[n++] = e; });
                current = pick(options, n).to;
            }
            for (size_t i = 0; i < path.size(); ++i) {
                int from = path[i], to = i + 1 < path.size() ? path[i + 1] : current;
                connect(edgeTo(from, to)); visited[from] = 1; positions[from] = -1; --remaining;
            }
        }
    } else if (algorithm == MazeAlgorithm::Prim) {
        int start = random.index(count); visited[start] = 1;
        std::vector<Edge> frontier;
        auto extend = [&](int i) { neighbors(i, [&](Edge e) { if (!visited[e.to]) frontier.push_back(e); }); };
        extend(start);
        while (!frontier.empty()) {
            if (stopped()) return false;
            int slot = random.index(int(frontier.size()));
            for (int attempt = 0; attempt < 8; ++attempt) {
                const Edge &candidate = frontier[slot];
                float preference = candidate.direction == 2 || candidate.direction == 8 ? 0.05f + directionBias : 1.05f - directionBias;
                if (random.unit() < preference) break;
                slot = random.index(int(frontier.size()));
            }
            Edge edge = frontier[slot];
            frontier[slot] = frontier.back(); frontier.pop_back(); if (visited[edge.to]) continue;
            connect(edge); visited[edge.to] = 1; extend(edge.to);
        }
    } else if (algorithm == MazeAlgorithm::GrowingBinaryTree) {
        int root = maze.columns - 1; visited[root] = 1;
        std::vector<int> frontier;
        auto extend = [&](int i) {
            neighbors(i, [&](Edge e) {
                if ((e.direction == 4 || e.direction == 8) && !visited[e.to]) frontier.push_back(e.to);
            });
        };
        extend(root);
        while (!frontier.empty()) {
            if (stopped()) return false;
            int slot = random.unit() < branching ? random.index(int(frontier.size())) : int(frontier.size()) - 1;
            int current = frontier[slot]; frontier[slot] = frontier.back(); frontier.pop_back();
            if (visited[current]) continue;
            Edge options[2]; int n = 0;
            neighbors(current, [&](Edge e) {
                if ((e.direction == 1 || e.direction == 2) && visited[e.to]) options[n++] = e;
            });
            connect(pick(options, n)); visited[current] = 1; extend(current);
        }
    } else if (algorithm == MazeAlgorithm::HuntAndKill) {
        std::priority_queue<int, std::vector<int>, std::greater<int>> frontier;
        int current = random.index(count); visited[current] = 1;
        auto extend = [&](int i) { neighbors(i, [&](Edge e) { if (!visited[e.to]) frontier.push(e.to); }); };
        extend(current);
        while (true) {
            if (stopped()) return false;
            Edge options[4]; int n = 0;
            neighbors(current, [&](Edge e) { if (!visited[e.to]) options[n++] = e; });
            if (n) {
                Edge e = pick(options, n); connect(e); current = e.to; visited[current] = 1; extend(current);
            } else {
                while (!frontier.empty() && visited[frontier.top()]) frontier.pop();
                if (frontier.empty()) break;
                current = frontier.top(); frontier.pop(); n = 0;
                neighbors(current, [&](Edge e) { if (visited[e.to]) options[n++] = e; });
                connect(pick(options, n)); visited[current] = 1; extend(current);
            }
        }
    } else {
        int start = random.index(count); visited[start] = 1;
        std::vector<int> active{start};
        while (!active.empty()) {
            if (stopped()) return false;
            int slot = int(active.size()) - 1;
            if (algorithm != MazeAlgorithm::Backtracking && random.unit() < branching) slot = random.index(int(active.size()));
            int current = active[slot]; Edge options[4]; int n = 0;
            neighbors(current, [&](Edge e) { if (!visited[e.to]) options[n++] = e; });
            if (!n) { active[slot] = active.back(); active.pop_back(); continue; }
            Edge edge = pick(options, n); connect(edge); visited[edge.to] = 1; active.push_back(edge.to);
        }
    }
    if (braid > 0) for (int i = 0; i < count; ++i) {
        if (stopped()) return false;
        if (degree(i) != 1 || random.unit() > braid) continue;
        Edge options[4]; int n = 0;
        neighbors(i, [&](Edge e) { if (!(maze.passages[i] & e.direction)) options[n++] = e; });
        if (n) connect(pick(options, n));
    }
    return !cancelled();
}

}
