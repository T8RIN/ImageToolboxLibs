/*
Copyright 2006 Jerry Huxtable

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package com.jhlabs;

/**
 * A filter which quantizes an image to a set number of colors - useful for producing
 * images which are to be encoded using an index color model. The filter can perform
 * Floyd-Steinberg error-diffusion dithering if required. At present, the quantization
 * is done using an octtree algorithm but I eventually hope to add more quantization
 * methods such as median cut. Note: at present, the filter produces an image which
 * uses the RGB color model (because the application it was written for required it).
 * I hope to extend it to produce an IndexColorModel by request.
 */
public class QuantizeFilter extends WholeImageFilter {

    /**
     * Floyd-Steinberg dithering matrix.
     */
    protected final static int[] matrix = {
            0, 0, 0,
            0, 0, 7,
            3, 5, 1,
    };
    private final int sum = 3 + 5 + 7 + 1;

    private boolean dither;
    private int numColors = 256;
    private boolean serpentine = true;

    /**
     * Get the number of colors to quantize to.
     *
     * @return the number of colors.
     */
    public int getNumColors() {
        return numColors;
    }

    /**
     * Set the number of colors to quantize to.
     *
     * @param numColors the number of colors. The default is 256.
     */
    public void setNumColors(int numColors) {
        this.numColors = Math.min(Math.max(numColors, 8), 256);
    }

    /**
     * Return the dithering setting
     *
     * @return the current setting
     */
    public boolean getDither() {
        return dither;
    }

    /**
     * Set whether to use dithering or not. If not, the image is posterized.
     *
     * @param dither true to use dithering
     */
    public void setDither(boolean dither) {
        this.dither = dither;
    }

    /**
     * Return the serpentine setting
     *
     * @return the current setting
     */
    public boolean getSerpentine() {
        return serpentine;
    }

    /**
     * Set whether to use a serpentine pattern for return or not. This can reduce 'avalanche' artifacts in the output.
     *
     * @param serpentine true to use serpentine pattern
     */
    public void setSerpentine(boolean serpentine) {
        this.serpentine = serpentine;
    }

    public String toString() {
        return "Colors/Quantize...";
    }

}
