/*
 ** Copyright 2005 Huxtable.com. All rights reserved.
 */

package com.jhlabs;

/**
 * A filter which replaces one color by another in an image. This is frankly, not often useful, but has its occasional
 * uses when dealing with GIF transparency and the like.
 */
public class MapColorsFilter extends PointFilter {

    private final int oldColor;
    private final int newColor;

    public MapColorsFilter(int oldColor, int newColor) {
        canFilterIndexColorModel = true;
        this.oldColor = oldColor;
        this.newColor = newColor;
    }

    public int filterRGB(int x, int y, int rgb) {
        if (rgb == oldColor)
            return newColor;
        return rgb;
    }
}

