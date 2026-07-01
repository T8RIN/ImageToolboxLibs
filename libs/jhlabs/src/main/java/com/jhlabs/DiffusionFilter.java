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
 * A filter which uses Floyd-Steinberg error diffusion dithering to halftone an image.
 */
public class DiffusionFilter extends WholeImageFilter {

    private final static int[] diffusionMatrix = {
            0, 0, 0,
            0, 0, 7,
            3, 5, 1,
    };

    private int[] matrix;
    private int sum = 3 + 5 + 7 + 1;
    private boolean serpentine = true;
    private boolean colorDither = true;
    private int levels = 6;

    /**
     * Construct a DiffusionFilter.
     */
    public DiffusionFilter() {
        setMatrix(diffusionMatrix);
    }

    /**
     * Return the serpentine setting.
     *
     * @return the current setting
     * @see #setSerpentine
     */
    public boolean getSerpentine() {
        return serpentine;
    }

    /**
     * Set whether to use a serpentine pattern for return or not. This can reduce 'avalanche' artifacts in the output.
     *
     * @param serpentine true to use serpentine pattern
     * @see #getSerpentine
     */
    public void setSerpentine(boolean serpentine) {
        this.serpentine = serpentine;
    }

    /**
     * Get whether to use a color dither.
     *
     * @return true to use a color dither
     * @see #setColorDither
     */
    public boolean getColorDither() {
        return colorDither;
    }

    /**
     * Set whether to use a color dither.
     *
     * @param colorDither true to use a color dither
     * @see #getColorDither
     */
    public void setColorDither(boolean colorDither) {
        this.colorDither = colorDither;
    }

    /**
     * Get the dither matrix.
     *
     * @return the dither matrix
     * @see #setMatrix
     */
    public int[] getMatrix() {
        return matrix;
    }

    /**
     * Set the dither matrix.
     *
     * @param matrix the dither matrix
     * @see #getMatrix
     */
    public void setMatrix(int[] matrix) {
        this.matrix = matrix;
        sum = 0;
        for (int i = 0; i < matrix.length; i++)
            sum += matrix[i];
    }

    /**
     * Get the number of dither levels.
     *
     * @return the number of levels
     * @see #setLevels
     */
    public int getLevels() {
        return levels;
    }

    /**
     * Set the number of dither levels.
     *
     * @param levels the number of levels
     * @see #getLevels
     */
    public void setLevels(int levels) {
        this.levels = levels;
    }

    public String toString() {
        return "Colors/Diffusion Dither...";
    }

}
