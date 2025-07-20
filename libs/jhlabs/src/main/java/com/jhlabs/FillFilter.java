/*
 ** Copyright 2005 Huxtable.com. All rights reserved.
 */

package com.jhlabs;

/**
 * A filter which fills an image with a given color. Normally you would just call Graphics.fillRect but it can sometimes be useful
 * to go via a filter to fit in with an existing API.
 */
public class FillFilter extends PointFilter {

    private int fillColor;

    public FillFilter() {
        this(0xff000000);
    }

    public FillFilter(int color) {
        this.fillColor = color;
    }

    public int getFillColor() {
        return fillColor;
    }

    public void setFillColor(int fillColor) {
        this.fillColor = fillColor;
    }

    public int filterRGB(int x, int y, int rgb) {
        return fillColor;
    }
}

