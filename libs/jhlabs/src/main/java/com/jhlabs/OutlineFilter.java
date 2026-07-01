/*
 ** Copyright 2005 Huxtable.com. All rights reserved.
 */

package com.jhlabs;

/**
 * Given a binary image, this filter converts it to its outline, replacing all interior pixels with the 'new' color.
 */
public class OutlineFilter extends BinaryFilter {

    public OutlineFilter() {
        newColor = 0xffffffff;
    }

    public String toString() {
        return "Binary/Outline...";
    }

}
