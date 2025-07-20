/*
 ** Copyright 2005 Huxtable.com. All rights reserved.
 */

package com.jhlabs;

import com.jhlabs.math.Function2D;

public class MapFilter extends TransformFilter {

    private Function2D xMapFunction;
    private Function2D yMapFunction;

    public MapFilter() {
    }

    public Function2D getXMapFunction() {
        return xMapFunction;
    }

    public void setXMapFunction(Function2D xMapFunction) {
        this.xMapFunction = xMapFunction;
    }

    public Function2D getYMapFunction() {
        return yMapFunction;
    }

    public void setYMapFunction(Function2D yMapFunction) {
        this.yMapFunction = yMapFunction;
    }

    protected void transformInverse(int x, int y, float[] out) {
        float xMap, yMap;
        xMap = xMapFunction.evaluate(x, y);
        yMap = yMapFunction.evaluate(x, y);
        out[0] = xMap * transformedSpace.width();
        out[1] = yMap * transformedSpace.height();
    }

    public String toString() {
        return "Distort/Map Coordinates...";
    }
}
