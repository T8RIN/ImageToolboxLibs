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
 * A filter which performs a "smart blur". i.e. a blur which blurs smotth parts of the image while preserving edges.
 */
public class SmartBlurFilter implements JhFilter {

    private int hRadius = 5;
    private int vRadius = 5;
    private int threshold = 10;

    /**
     * Convolve with a kernel consisting of one row
     */

    /**
     * Get the horizontal size of the blur.
     *
     * @return the radius of the blur in the horizontal direction
     * @see #setHRadius
     */
    public int getHRadius() {
        return hRadius;
    }

    /**
     * Set the horizontal size of the blur.
     *
     * @param hRadius the radius of the blur in the horizontal direction
     * @min-value 0
     * @see #getHRadius
     */
    public void setHRadius(int hRadius) {
        this.hRadius = hRadius;
    }

    /**
     * Get the vertical size of the blur.
     *
     * @return the radius of the blur in the vertical direction
     * @see #setVRadius
     */
    public int getVRadius() {
        return vRadius;
    }

    /**
     * Set the vertical size of the blur.
     *
     * @param vRadius the radius of the blur in the vertical direction
     * @min-value 0
     * @see #getVRadius
     */
    public void setVRadius(int vRadius) {
        this.vRadius = vRadius;
    }

    /**
     * Get the radius of the effect.
     *
     * @return the radius
     * @see #setRadius
     */
    public int getRadius() {
        return hRadius;
    }

    /**
     * Set the radius of the effect.
     *
     * @param radius the radius
     * @min-value 0
     * @see #getRadius
     */
    public void setRadius(int radius) {
        this.hRadius = this.vRadius = radius;
    }

    /**
     * Get the threshold value.
     *
     * @return the threshold value
     * @see #setThreshold
     */
    public int getThreshold() {
        return threshold;
    }

    /**
     * Set the threshold value.
     *
     * @param threshold the threshold value
     * @see #getThreshold
     */
    public void setThreshold(int threshold) {
        this.threshold = threshold;
    }

    public String toString() {
        return "Blur/Smart Blur...";
    }
}
