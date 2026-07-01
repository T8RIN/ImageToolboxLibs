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
 * A filter which applies a convolution kernel to an image.
 *
 * @author Jerry Huxtable
 */
public class ConvolveFilter implements JhFilter {

    /**
     * Treat pixels off the edge as zero.
     */
    public static int ZERO_EDGES = 0;

    /**
     * Clamp pixels off the edge to the nearest edge.
     */
    public static int CLAMP_EDGES = 1;

    /**
     * Wrap pixels off the edge to the opposite edge.
     */
    public static int WRAP_EDGES = 2;

    /**
     * The convolution kernel.
     */
    protected Kernel kernel = null;

    /**
     * Whether to convolve alpha.
     */
    protected boolean alpha = true;

    /**
     * Whether to promultiply the alpha before convolving.
     */
    protected boolean premultiplyAlpha = true;

    /**
     * What do do at the image edges.
     */
    private int edgeAction = CLAMP_EDGES;

    /**
     * Construct a filter with a null kernel. This is only useful if you're going to change the kernel later on.
     */
    public ConvolveFilter() {
        this(new float[9]);
    }

    /**
     * Construct a filter with the given 3x3 kernel.
     *
     * @param matrix an array of 9 floats containing the kernel
     */
    public ConvolveFilter(float[] matrix) {
        this(new Kernel(3, 3, matrix));
    }

    /**
     * Construct a filter with the given kernel.
     *
     * @param rows   the number of rows in the kernel
     * @param cols   the number of columns in the kernel
     * @param matrix an array of rows*cols floats containing the kernel
     */
    public ConvolveFilter(int rows, int cols, float[] matrix) {
        this(new Kernel(cols, rows, matrix));
    }

    /**
     * Construct a filter with the given 3x3 kernel.
     *
     * @param kernel the convolution kernel
     */
    public ConvolveFilter(Kernel kernel) {
        this.kernel = kernel;
    }

    /**
     * Convolve a block of pixels.
     *
     * @param kernel     the kernel
     * @param inPixels   the input pixels
     * @param outPixels  the output pixels
     * @param width      the width
     * @param height     the height
     * @param edgeAction what to do at the edges
     */

    /**
     * Convolve a block of pixels.
     *
     * @param kernel     the kernel
     * @param inPixels   the input pixels
     * @param outPixels  the output pixels
     * @param width      the width
     * @param height     the height
     * @param alpha      include alpha channel
     * @param edgeAction what to do at the edges
     */

    /**
     * Convolve with a 2D kernel.
     *
     * @param kernel     the kernel
     * @param inPixels   the input pixels
     * @param outPixels  the output pixels
     * @param width      the width
     * @param height     the height
     * @param alpha      include alpha channel
     * @param edgeAction what to do at the edges
     */

    /**
     * Convolve with a kernel consisting of one row.
     *
     * @param kernel     the kernel
     * @param inPixels   the input pixels
     * @param outPixels  the output pixels
     * @param width      the width
     * @param height     the height
     * @param alpha      include alpha channel
     * @param edgeAction what to do at the edges
     */

    /**
     * Convolve with a kernel consisting of one column.
     *
     * @param kernel     the kernel
     * @param inPixels   the input pixels
     * @param outPixels  the output pixels
     * @param width      the width
     * @param height     the height
     * @param alpha      include alpha channel
     * @param edgeAction what to do at the edges
     */

    /**
     * Get the convolution kernel.
     *
     * @return the kernel
     * @see #setKernel
     */
    public Kernel getKernel() {
        return kernel;
    }

    /**
     * Set the convolution kernel.
     *
     * @param kernel the kernel
     * @see #getKernel
     */
    public void setKernel(Kernel kernel) {
        this.kernel = kernel;
    }

    /**
     * Get the action to perfomr for pixels off the image edges.
     *
     * @return the action
     * @see #setEdgeAction
     */
    public int getEdgeAction() {
        return edgeAction;
    }

    /**
     * Set the action to perfomr for pixels off the image edges.
     *
     * @param edgeAction the action
     * @see #getEdgeAction
     */
    public void setEdgeAction(int edgeAction) {
        this.edgeAction = edgeAction;
    }

    /**
     * Get whether to convolve the alpha channel.
     *
     * @return true to convolve the alpha
     * @see #setUseAlpha
     */
    public boolean getUseAlpha() {
        return alpha;
    }

    /**
     * Set whether to convolve the alpha channel.
     *
     * @param useAlpha true to convolve the alpha
     * @see #getUseAlpha
     */
    public void setUseAlpha(boolean useAlpha) {
        this.alpha = useAlpha;
    }

    /**
     * Get whether to premultiply the alpha channel.
     *
     * @return true to premultiply the alpha
     * @see #setPremultiplyAlpha
     */
    public boolean getPremultiplyAlpha() {
        return premultiplyAlpha;
    }

    /**
     * Set whether to premultiply the alpha channel.
     *
     * @param premultiplyAlpha true to premultiply the alpha
     * @see #getPremultiplyAlpha
     */
    public void setPremultiplyAlpha(boolean premultiplyAlpha) {
        this.premultiplyAlpha = premultiplyAlpha;
    }

    public String toString() {
        return "Blur/Convolve...";
    }
}
