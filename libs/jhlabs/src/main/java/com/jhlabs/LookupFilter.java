/*
 ** Copyright 2005 Huxtable.com. All rights reserved.
 */

package com.jhlabs;

/**
 * A filter which uses the brightness of each pixel to lookup a color from a colormap.
 */
public class LookupFilter extends PointFilter {

    private Colormap colormap = new Gradient();

    public LookupFilter() {
        canFilterIndexColorModel = true;
    }

    public LookupFilter(Colormap colormap) {
        canFilterIndexColorModel = true;
        this.colormap = colormap;
    }

    public Colormap getColormap() {
        return colormap;
    }

    public void setColormap(Colormap colormap) {
        this.colormap = colormap;
    }

    public String toString() {
        return "Colors/Lookup...";
    }

}
