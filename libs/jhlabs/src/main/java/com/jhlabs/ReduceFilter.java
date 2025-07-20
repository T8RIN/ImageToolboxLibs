/*
 ** Copyright 2005 Huxtable.com. All rights reserved.
 */

package com.jhlabs;

public class ReduceFilter extends PointFilter implements java.io.Serializable {

    private int numLevels;
    private int[] levels;
    private boolean initialized = false;

    public ReduceFilter() {
        setNumLevels(6);
    }

    public int getNumLevels() {
        return numLevels;
    }

    public void setNumLevels(int numLevels) {
        this.numLevels = numLevels;
        initialized = false;
    }

    protected void initialize() {
        levels = new int[256];
        if (numLevels != 1)
            for (int i = 0; i < 256; i++)
                levels[i] = 255 * (numLevels * i / 256) / (numLevels - 1);
    }

    public int filterRGB(int x, int y, int rgb) {
        if (!initialized) {
            initialized = true;
            initialize();
        }
        int a = rgb & 0xff000000;
        int r = (rgb >> 16) & 0xff;
        int g = (rgb >> 8) & 0xff;
        int b = rgb & 0xff;
        r = levels[r];
        g = levels[g];
        b = levels[b];
        return a | (r << 16) | (g << 8) | b;
    }

    public String toString() {
        return "Colors/Posterize...";
    }

}

