/*
 ** Copyright 2005 Huxtable.com. All rights reserved.
 */

package com.jhlabs;

/**
 * A colormap with the colors of the spectrum.
 */
public class SpectrumColormap implements Colormap, java.io.Serializable {

    /**
     * Construct a spcetrum color map
     */
    public SpectrumColormap() {
    }

    /**
     * Convert a value in the range 0..1 to an RGB color.
     *
     * @param v a value in the range 0..1
     * @return an RGB color
     */
    public int getColor(float v) {
        return Spectrum.wavelengthToRGB(380 + 400 * ImageMath.clamp(v, 0, 1.0f));
    }

}
