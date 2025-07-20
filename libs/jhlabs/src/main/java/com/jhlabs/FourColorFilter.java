/*
 ** Copyright 2005 Huxtable.com. All rights reserved.
 */

package com.jhlabs;

/**
 * A filter which draws a gradient interpolated between four colors defined at the corners of the image.
 */
public class FourColorFilter extends PointFilter {

    private int width;
    private int height;
    private int colorNW;
    private int colorNE;
    private int colorSW;
    private int colorSE;
    private int rNW, gNW, bNW;
    private int rNE, gNE, bNE;
    private int rSW, gSW, bSW;
    private int rSE, gSE, bSE;

    public FourColorFilter() {
        setColorNW(0xffff0000);
        setColorNE(0xffff00ff);
        setColorSW(0xff0000ff);
        setColorSE(0xff00ffff);
    }

    public int getColorNW() {
        return colorNW;
    }

    public void setColorNW(int color) {
        this.colorNW = color;
        rNW = (color >> 16) & 0xff;
        gNW = (color >> 8) & 0xff;
        bNW = color & 0xff;
    }

    public int getColorNE() {
        return colorNE;
    }

    public void setColorNE(int color) {
        this.colorNE = color;
        rNE = (color >> 16) & 0xff;
        gNE = (color >> 8) & 0xff;
        bNE = color & 0xff;
    }

    public int getColorSW() {
        return colorSW;
    }

    public void setColorSW(int color) {
        this.colorSW = color;
        rSW = (color >> 16) & 0xff;
        gSW = (color >> 8) & 0xff;
        bSW = color & 0xff;
    }

    public int getColorSE() {
        return colorSE;
    }

    public void setColorSE(int color) {
        this.colorSE = color;
        rSE = (color >> 16) & 0xff;
        gSE = (color >> 8) & 0xff;
        bSE = color & 0xff;
    }

    public void setDimensions(int width, int height) {
        this.width = width;
        this.height = height;
        super.setDimensions(width, height);
    }

    public int filterRGB(int x, int y, int rgb) {
        float fx = (float) x / width;
        float fy = (float) y / height;
        float p, q;

        p = rNW + (rNE - rNW) * fx;
        q = rSW + (rSE - rSW) * fx;
        int r = (int) (p + (q - p) * fy + 0.5f);

        p = gNW + (gNE - gNW) * fx;
        q = gSW + (gSE - gSW) * fx;
        int g = (int) (p + (q - p) * fy + 0.5f);

        p = bNW + (bNE - bNW) * fx;
        q = bSW + (bSE - bSW) * fx;
        int b = (int) (p + (q - p) * fy + 0.5f);

        return 0xff000000 | (r << 16) | (g << 8) | b;
    }

    public String toString() {
        return "Texture/Four Color Fill...";
    }
}
