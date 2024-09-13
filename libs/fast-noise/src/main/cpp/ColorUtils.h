//
// Created by malik on 17.07.2024.
//

#include <cstring>
#include <cstdlib>
#include <cmath>
#include <cmath>
#include <string>
#include "ColorTranslator.h"

#ifndef IMAGETOOLBOXLIBS_COLORUTILS_H
#define IMAGETOOLBOXLIBS_COLORUTILS_H

float LinearSRGBTosRGB(float linear);

float SRGBToLinear(float v);

float luminance(float red, float green, float blue);

LAB colorToLAB(RGB rgb);

RGB labToColor(LAB lab);

uint32_t argb_to_bgra(uint32_t argb);

uint32_t bgra_to_argb(uint32_t bgra);

double colorDiff(uint32_t color1, uint32_t color2);

double colorDiff(RGB color1, RGB color2);

RGB ColorToRGB(int color);

ARGB ColorToARGB(int color);

#endif //IMAGETOOLBOXLIBS_COLORUTILS_H
