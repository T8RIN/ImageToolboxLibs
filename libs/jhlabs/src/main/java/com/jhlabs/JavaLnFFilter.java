/*
 ** Copyright 2005 Huxtable.com. All rights reserved.
 */

package com.jhlabs;

/**
 * A filter which produces  the stipple effect for Swing icons specified in the Java Look and Feel Guidelines.
 */
public class JavaLnFFilter extends PointFilter {

    public JavaLnFFilter() {
    }

    public int filterRGB(int x, int y, int rgb) {
        if ((x & 1) == (y & 1))
            return rgb;
        return ImageMath.mixColors(0.25f, 0xff999999, rgb);
    }

    public String toString() {
        return "Stylize/Java L&F Stipple";
    }

}


