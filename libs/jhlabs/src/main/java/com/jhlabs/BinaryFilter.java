/*
 ** Copyright 2005 Huxtable.com. All rights reserved.
 */

package com.jhlabs;

import com.jhlabs.math.BinaryFunction;
import com.jhlabs.math.BlackFunction;

public abstract class BinaryFilter extends WholeImageFilter {

    protected int newColor = 0xff000000;
    protected BinaryFunction blackFunction = new BlackFunction();
    protected int iterations = 1;
    protected Colormap colormap;

    public int getIterations() {
        return iterations;
    }

    public void setIterations(int iterations) {
        this.iterations = iterations;
    }

    public Colormap getColormap() {
        return colormap;
    }

    public void setColormap(Colormap colormap) {
        this.colormap = colormap;
    }

    public int getNewColor() {
        return newColor;
    }

    public void setNewColor(int newColor) {
        this.newColor = newColor;
    }

    public BinaryFunction getBlackFunction() {
        return blackFunction;
    }

    public void setBlackFunction(BinaryFunction blackFunction) {
        this.blackFunction = blackFunction;
    }

}

