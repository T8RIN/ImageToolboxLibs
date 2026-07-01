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

import com.jhlabs.math.ImageMath;

/**
 * A filter which applies Gaussian blur to an image. This is a subclass of ConvolveFilter
 * which simply creates a kernel with a Gaussian distribution for blurring.
 *
 * @author Jerry Huxtable
 */
public class GaussianFilter extends ConvolveFilter {

    /**
     * The blur radius.
     */
    protected float radius;

    /**
     * The convolution kernel.
     */
    protected Kernel kernel;

    /**
     * Construct a Gaussian filter.
     */
    public GaussianFilter() {
        this(2);
    }

    /**
     * Construct a Gaussian filter.
     *
     * @param radius blur radius in pixels
     */
    public GaussianFilter(float radius) {
        setRadius(radius);
    }

    /**
     * Blur and transpose a block of ARGB pixels.
     *
     * @param kernel     the blur kernel
     * @param inPixels   the input pixels
     * @param outPixels  the output pixels
     * @param width      the width of the pixel array
     * @param height     the height of the pixel array
     * @param alpha      whether to blur the alpha channel
     * @param edgeAction what to do at the edges
     */

    /**
     * Make a Gaussian blur kernel.
     *
     * @param radius the blur radius
     * @return the kernel
     */
    public static Kernel makeKernel(float radius) {
        int r = (int) Math.ceil(radius);
        int rows = r * 2 + 1;
        float[] matrix = new float[rows];
        float sigma = radius / 3;
        float sigma22 = 2 * sigma * sigma;
        float sigmaPi2 = 2 * ImageMath.PI * sigma;
        float sqrtSigmaPi2 = (float) Math.sqrt(sigmaPi2);
        float radius2 = radius * radius;
        float total = 0;
        int index = 0;
        for (int row = -r; row <= r; row++) {
            float distance = row * row;
            if (distance > radius2)
                matrix[index] = 0;
            else
                matrix[index] = (float) Math.exp(-(distance) / sigma22) / sqrtSigmaPi2;
            total += matrix[index];
            index++;
        }
        for (int i = 0; i < rows; i++)
            matrix[i] /= total;

        return new Kernel(rows, 1, matrix);
    }

    /**
     * Get the radius of the kernel.
     *
     * @return the radius
     * @see #setRadius
     */
    public float getRadius() {
        return radius;
    }

    /**
     * Set the radius of the kernel, and hence the amount of blur. The bigger the radius, the longer this filter will take.
     *
     * @param radius the radius of the blur in pixels.
     * @min-value 0
     * @max-value 100+
     * @see #getRadius
     */
    public void setRadius(float radius) {
        this.radius = radius;
        kernel = makeKernel(radius);
    }

    public String toString() {
        return "Blur/Gaussian Blur...";
    }
}
