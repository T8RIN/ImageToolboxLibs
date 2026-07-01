/*
 ** Copyright 2005 Huxtable.com. All rights reserved.
 */

package com.jhlabs;

/**
 * Given a binary image, this filter performs binary erosion, setting all removed pixels to the given 'new' color.
 */
public class ErodeFilter extends BinaryFilter {

    protected int threshold = 2;

    public ErodeFilter() {
        newColor = 0xffffffff;
    }

    /**
     * Return the threshold - the number of neighbouring pixels for dilation to occur.
     *
     * @return the current threshold
     */
    public int getThreshold() {
        return threshold;
    }

    /**
     * Set the threshold - the number of neighbouring pixels for dilation to occur.
     *
     * @param threshold the new threshold
     */
    public void setThreshold(int threshold) {
        this.threshold = threshold;
    }

    public String toString() {
        return "Binary/Erode...";
    }

}
