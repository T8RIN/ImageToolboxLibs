package com.jhlabs;

public class BicubicScaleFilter extends WholeImageFilter {
    private int width = 32;
    private int height = 32;

    public BicubicScaleFilter() {
    }

    public BicubicScaleFilter(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public String toString() {
        return "Distort/Bicubic Scale";
    }
}
