/*
 ** Copyright 2005 Huxtable.com. All rights reserved.
 */

package com.jhlabs;

import com.jhlabs.util.PixelUtils;

import java.io.Serializable;

/**
 * A Colormap implemented using Catmull-Rom colour splines. The map has a variable number
 * of knots with a minimum of four. The first and last knots give the tangent at the end
 * of the spline, and colours are interpolated from the second to the second-last knots.
 */
public class SplineColormap extends ArrayColormap implements Serializable {

    public int numKnots = 4;
    public int[] xKnots = {
            0, 0, 255, 255
    };
    public int[] yKnots = {
            0xff000000, 0xff000000, 0xffffffff, 0xffffffff,
    };

    public SplineColormap() {
        rebuildGradient();
    }

    public SplineColormap(int[] xKnots, int[] yKnots) {
        this.xKnots = xKnots;
        this.yKnots = yKnots;
        numKnots = xKnots.length;
        rebuildGradient();
    }

    public int getKnot(int n) {
        return yKnots[n];
    }

    public void setKnot(int n, int color) {
        yKnots[n] = color;
        rebuildGradient();
    }

    public void addKnot(int x, int color) {
        int[] nx = new int[numKnots + 1];
        int[] ny = new int[numKnots + 1];
        System.arraycopy(xKnots, 0, nx, 0, numKnots);
        System.arraycopy(yKnots, 0, ny, 0, numKnots);
        xKnots = nx;
        yKnots = ny;
        xKnots[numKnots] = x;
        yKnots[numKnots] = color;
        numKnots++;
        sortKnots();
        rebuildGradient();
    }

    public void removeKnot(int n) {
        if (numKnots <= 4)
            return;
        if (n < numKnots - 1) {
            System.arraycopy(xKnots, n + 1, xKnots, n, numKnots - n - 1);
            System.arraycopy(yKnots, n + 1, yKnots, n, numKnots - n - 1);
        }
        numKnots--;
        rebuildGradient();
    }

    public void setKnotPosition(int n, int x) {
        xKnots[n] = PixelUtils.clamp(x);
        sortKnots();
        rebuildGradient();
    }

    private void rebuildGradient() {
        xKnots[0] = -1;
        xKnots[numKnots - 1] = 256;
        yKnots[0] = yKnots[1];
        yKnots[numKnots - 1] = yKnots[numKnots - 2];
        for (int i = 0; i < 256; i++)
            map[i] = ImageMath.colorSpline(i, numKnots, xKnots, yKnots);
    }

    private void sortKnots() {
        for (int i = 1; i < numKnots; i++) {
            for (int j = 1; j < i; j++) {
                if (xKnots[i] < xKnots[j]) {
                    int t = xKnots[i];
                    xKnots[i] = xKnots[j];
                    xKnots[j] = t;
                    t = yKnots[i];
                    yKnots[i] = yKnots[j];
                    yKnots[j] = t;
                }
            }
        }
    }

}
