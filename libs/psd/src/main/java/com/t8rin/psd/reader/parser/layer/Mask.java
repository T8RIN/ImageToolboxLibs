package com.t8rin.psd.reader.parser.layer;

public class Mask {
    int top;            // Rectangle enclosing layer mask: Top, left, bottom, right
    int left;
    int bottom;
    int right;
    int defaultColor;    // 0 or 255
    boolean relative;        // position relative to layer
    boolean disabled;        // layer mask disabled
    boolean invert;            // invert layer mask when blending
    //byte*mask_data;

    public int getLeft() {
        return left;
    }

    public int getTop() {
        return top;
    }

    public int getRight() {
        return right;
    }

    public int getBottom() {
        return bottom;
    }

    public int getWidth() {
        return right - left;
    }

    public int getHeight() {
        return bottom - top;
    }

    public int getDefaultColor() {
        return defaultColor;
    }

    public boolean isRelative() {
        return relative;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public boolean isInvert() {
        return invert;
    }
}
