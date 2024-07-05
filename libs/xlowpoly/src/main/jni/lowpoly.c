/*
 *  Copyright 2016 xyzxqs (xyzxqs@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

//
// Created by xyzxqs (xyzxqs@gmail.com) on 9/22/16.
//

#include <stdlib.h>
#include <stdbool.h>
#include <jni.h>
#include "lowpoly.h"
#include "sobel.h"
#include "delaunay.h"
#include "dilution.h"
#include "dedup.h"
#include "_log.h"
#include "point.h"



//sobel callback method
bool call(int magnitude, int x, int y);

///////////////////

int threshold = 40;

/**
 * pixels: colors are non-premultiplied ARGB values in the sRGB color space.
 *  int color = pixel[w * y + x];
 *  int blue = color & 0xFF;
 *  int green = (color >> 8) & 0xFF;
 *  int red = (color >> 16) & 0xFF;
 *  int alpha = (color >> 24) & 0xFF;
 *
 * size: pixels 's length
 * w: img width in pixels
 * h: img height in pixels
 * thre : threshold 阈值
 * alpha : in percent 过滤百分比或点数(0.0, 1) or [1, max)
 */
void get_triangles(const int *pixels, int size, int w, int h, int thre, float alpha,
                   int *result, int *size_result, unsigned char lowpoly) {
    threshold = thre;

    Point *collectors = (Point *) malloc((size / 2 + 1) * sizeof(Point));
    int size_collectors = 0;

    sobel(pixels, w, h, &call, collectors, &size_collectors);

    Point *vertices = (Point *) malloc((size_collectors + 4) * sizeof(Point));
    int size_vertices = 0;

    dilution(collectors, size_collectors, w, h, alpha, vertices, &size_vertices);

    free(collectors);

    if (alpha > 1.0f) {
        dedup(vertices, &size_vertices);
    }
    if (lowpoly) {
        PNode *triangles = (PNode *) malloc(sizeof(PNode));
        triangles->index = -1;
        triangles->next = NULL;

        triangulate(vertices, size_vertices, w, h, triangles);

        *size_result = 0;
        for (PNode *p = triangles->next; p != NULL;) {
            Point *pi = &(vertices[p->index]);
            result[(*size_result)++] = pi->x;
            result[(*size_result)++] = pi->y;
            p = p->next;
        }
        free(vertices);

        pnode_free(triangles);
        free(triangles);
        triangles = NULL;
    } else {
        for (int i = 0; i < size_vertices; i++) {
            Point *pi = &(vertices[i]);
            result[(*size_result)++] = pi->x;
            result[(*size_result)++] = pi->y;
        }
    }
}

bool call(int magnitude, int x, int y) {
    return magnitude > threshold;
}