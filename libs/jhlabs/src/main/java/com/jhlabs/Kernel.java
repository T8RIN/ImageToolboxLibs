package com.jhlabs;

public class Kernel {
    private final int mWidth;
    private final int mHeight;
    private final float[] mMatrix;

    public Kernel(int w, int h, float[] matrix) {
        mWidth = w;
        mHeight = h;
        mMatrix = matrix;
    }

    public int getWidth() {
        return mWidth;
    }

    public int getHeight() {
        return mHeight;
    }

    public float[] getKernelData(float[] data) {
        return mMatrix;
    }
}
