/*
 ** Copyright 2005 Huxtable.com. All rights reserved.
 */

package com.jhlabs;

// original code Copyright (C) Jerry Huxtable 1998
//
// customizations (C) Michele Puccini 19/12/2001
// - conversion from float to int math
// - complete rewrite of applyMap()
// - implemented merge to dest function

public class ShapeFilter extends WholeImageFilter {

    public final static int LINEAR = 0;
    public final static int CIRCLE_UP = 1;
    public final static int CIRCLE_DOWN = 2;
    public final static int SMOOTH = 3;
    protected Colormap colormap;
    private float factor = 1.0f;
    private boolean useAlpha = true;
    private boolean invert = false;
    private boolean merge = false;
    private int type;

    public ShapeFilter() {
        colormap = new LinearColormap();
    }

    public float getFactor() {
        return factor;
    }

    public void setFactor(float factor) {
        this.factor = factor;
    }

    public Colormap getColormap() {
        return colormap;
    }

    public void setColormap(Colormap colormap) {
        this.colormap = colormap;
    }

    public boolean getUseAlpha() {
        return useAlpha;
    }

    public void setUseAlpha(boolean useAlpha) {
        this.useAlpha = useAlpha;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public boolean getInvert() {
        return invert;
    }

    public void setInvert(boolean invert) {
        this.invert = invert;
    }

    public boolean getMerge() {
        return merge;
    }

    public void setMerge(boolean merge) {
        this.merge = merge;
    }

    public String toString() {
        return "Stylize/Shapeburst...";
    }

}
