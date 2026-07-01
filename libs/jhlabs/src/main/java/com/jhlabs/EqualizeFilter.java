/*
 ** Copyright 2005 Huxtable.com. All rights reserved.
 */

package com.jhlabs;

/**
 * A filter to perform auto-equalization on an image.
 */
public class EqualizeFilter extends WholeImageFilter implements java.io.Serializable {

    public EqualizeFilter() {
    }

    public String toString() {
        return "Colors/Equalize";
    }
}
