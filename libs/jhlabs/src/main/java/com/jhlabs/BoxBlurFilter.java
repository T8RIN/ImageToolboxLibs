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
 * A filter which performs a box blur on an image. The horizontal and vertical blurs can be specified separately
 * and a number of iterations can be given which allows an approximation to Gaussian blur.
 */
public class BoxBlurFilter implements JhFilter {

    private float hRadius;
    private float vRadius;
    private int iterations = 1;
    private boolean premultiplyAlpha = true;

    /**
     * Construct a default BoxBlurFilter.
     */
    public BoxBlurFilter() {
    }

    /**
     * Construct a BoxBlurFilter.
     *
     * @param hRadius    the horizontal radius of blur
     * @param vRadius    the vertical radius of blur
     * @param iterations the number of time to iterate the blur
     */
    public BoxBlurFilter(float hRadius, float vRadius, int iterations) {
        this.hRadius = hRadius;
        this.vRadius = vRadius;
        this.iterations = iterations;
    }

    /**
     * Blur and transpose a block of ARGB pixels.
     *
     * @param in     the input pixels
     * @param out    the output pixels
     * @param width  the width of the pixel array
     * @param height the height of the pixel array
     * @param radius the radius of blur
     */

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

    /**
     * Get the horizontal size of the blur.
     *
     * @return the radius of the blur in the horizontal direction
     * @see #setHRadius
     */
    public float getHRadius() {
        return hRadius;
    }

    /**
     * Set the horizontal size of the blur.
     *
     * @param hRadius the radius of the blur in the horizontal direction
     * @min-value 0
     * @see #getHRadius
     */
    public void setHRadius(float hRadius) {
        this.hRadius = hRadius;
    }

    /**
     * Get the vertical size of the blur.
     *
     * @return the radius of the blur in the vertical direction
     * @see #setVRadius
     */
    public float getVRadius() {
        return vRadius;
    }

    /**
     * Set the vertical size of the blur.
     *
     * @param vRadius the radius of the blur in the vertical direction
     * @min-value 0
     * @see #getVRadius
     */
    public void setVRadius(float vRadius) {
        this.vRadius = vRadius;
    }

    /**
     * Get the size of the blur.
     *
     * @return the radius of the blur in the horizontal direction
     * @see #setRadius
     */
    public float getRadius() {
        return hRadius;
    }

    /**
     * Set both the horizontal and vertical sizes of the blur.
     *
     * @param radius the radius of the blur in both directions
     * @min-value 0
     * @see #getRadius
     */
    public void setRadius(float radius) {
        this.hRadius = this.vRadius = radius;
    }

    /**
     * Get the number of iterations the blur is performed.
     *
     * @return the number of iterations
     * @see #setIterations
     */
    public int getIterations() {
        return iterations;
    }

    /**
     * Set the number of iterations the blur is performed.
     *
     * @param iterations the number of iterations
     * @min-value 0
     * @see #getIterations
     */
    public void setIterations(int iterations) {
        this.iterations = iterations;
    }

    public String toString() {
        return "Blur/Box Blur...";
    }
}
