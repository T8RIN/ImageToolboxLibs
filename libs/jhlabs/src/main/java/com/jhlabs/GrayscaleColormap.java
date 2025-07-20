/*
 ** Copyright 2005 Huxtable.com. All rights reserved.
 */

package com.jhlabs;

/**
 * A grayscale colormap. Black is 0, white is 1.
 */
public class GrayscaleColormap implements Colormap, java.io.Serializable {

    static final long serialVersionUID = -6015170137060961021L;

    public GrayscaleColormap() {
    }

    /**
     * Convert a value in the range 0..1 to an RGB color.
     *
     * @param v a value in the range 0..1
     * @return an RGB color
     */
    public int getColor(float v) {
        int n = (int) (v * 255);
        if (n < 0)
            n = 0;
        else if (n > 255)
            n = 255;
        return 0xff000000 | (n << 16) | (n << 8) | n;
    }

}
