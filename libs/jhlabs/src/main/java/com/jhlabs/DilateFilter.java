/*
 ** Copyright 2005 Huxtable.com. All rights reserved.
 */

package com.jhlabs;

/**
 * Given a binary image, this filter performs binary dilation, setting all added pixels to the given 'new' color.
 */
public class DilateFilter extends BinaryFilter {

    public int threshold = 2;

    public DilateFilter() {
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
        return "Binary/Dilate...";
    }

}
