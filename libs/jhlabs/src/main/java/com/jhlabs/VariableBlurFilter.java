package com.jhlabs;

import android.graphics.Bitmap;

public class VariableBlurFilter extends WholeImageFilter {
    private int hRadius = 5, vRadius = 5, iterations = 1;
    private Bitmap blurMask;

    public int getHRadius() {
        return hRadius;
    }

    public void setHRadius(int v) {
        hRadius = v;
    }

    public int getVRadius() {
        return vRadius;
    }

    public void setVRadius(int v) {
        vRadius = v;
    }

    public int getRadius() {
        return hRadius;
    }

    public void setRadius(int v) {
        hRadius = v;
        vRadius = v;
    }

    public int getIterations() {
        return iterations;
    }

    public void setIterations(int v) {
        iterations = v;
    }

    public Bitmap getBlurMask() {
        return blurMask;
    }

    public void setBlurMask(Bitmap v) {
        blurMask = v;
    }

    public String toString() {
        return "Blur/Variable Blur...";
    }
}
