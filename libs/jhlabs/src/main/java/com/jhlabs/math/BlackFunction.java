/*
 ** Copyright 2005 Huxtable.com. All rights reserved.
 */

package com.jhlabs.math;

public class BlackFunction implements BinaryFunction {
    public boolean isBlack(int rgb) {
        return rgb == 0xff000000;
    }
}

