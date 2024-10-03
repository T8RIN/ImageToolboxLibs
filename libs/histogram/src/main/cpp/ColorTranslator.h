/*
 * ColorTranslator.h
 *
 *  Created on: 2014-4-17
 *      Author: ragnarok
 */

#ifndef COLORTRANSLATOR_H_
#define COLORTRANSLATOR_H_

#include "ColorGetter.h"

typedef struct _rgb { // 0 ~ 225
    _rgb(int r = 0, int g = 0, int b = 0) {
        this->r = r;
        this->g = g;
        this->b = b;
    }

    int r;
    int g;
    int b;
} RGB;

typedef struct _argb { // 0 ~ 225
    _argb(int a = 0, int r = 0, int g = 0, int b = 0) {
        this->a = a;
        this->r = r;
        this->g = g;
        this->b = b;
    }

    int a;
    int r;
    int g;
    int b;
} ARGB;

typedef struct _hsi {
    _hsi(double h = 0, double s = 0, double i = 0) {
        this->h = h;
        this->s = s;
        this->i = i;
    }

    double h; // 0 ~ 360
    double s; // 0 ~ 1
    double i; // 0 ~ 1
} HSI;

typedef struct _lab {
    _lab(double l = 0, double a = 0, double b = 0) {
        this->l = l;
        this->a = a;
        this->b = b;
    }

    double l; // 0 ~ 100
    double a; // -128..127
    double b; // -128..127
} LAB;

class ColorTranslator {
public:
    static HSI RGB2HSI(double r, double g, double b);

    static RGB HSI2RGB(double h, double s, double i);

    static bool checkRGB(double r, double g, double b);

    static bool checkRGB(RGB rgb);

};


#endif /* COLORTRANSLATOR_H_ */
