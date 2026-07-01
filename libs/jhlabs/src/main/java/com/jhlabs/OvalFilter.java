/*
 ** Copyright 2005 Huxtable.com. All rights reserved.
 */

package com.jhlabs;

public class OvalFilter extends PointFilter {

    private float centreX = 0;
    private float centreY = 0;
    private float a = 0;
    private float b = 0;
    private float a2 = 0;
    private float b2 = 0;

    public OvalFilter() {
    }

    public void setDimensions(int width, int height) {
        super.setDimensions(width, height);
        centreX = a = width / 2;
        centreY = b = height / 2;
        a2 = a * a;
        b2 = b * b;
    }

    public String toString() {
        return "Stylize/Oval...";
    }

}
