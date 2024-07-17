//
// Created by malik on 17.07.2024.
//

#include <cstring>
#include <cstdlib>
#include <cmath>
#include <cmath>
#include <string>

#ifndef IMAGETOOLBOXLIBS_COLORUTILS_H
#define IMAGETOOLBOXLIBS_COLORUTILS_H

float LinearSRGBTosRGB(float linear);

float SRGBToLinear(float v);

float luminance(float red, float green, float blue);

void colorToLAB(int r, int g, int b, float lab[3]);

void labToColor(double l, double a, double b, int rgb[3]);

uint32_t argb_to_bgra(uint32_t argb);

uint32_t bgra_to_argb(uint32_t bgra);

int colorDiff(uint32_t color1, uint32_t color2);

#endif //IMAGETOOLBOXLIBS_COLORUTILS_H
