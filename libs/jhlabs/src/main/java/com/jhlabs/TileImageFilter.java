package com.jhlabs;

public class TileImageFilter extends WholeImageFilter {
    public static final int FLIP_NONE = 0, FLIP_H = 1, FLIP_V = 2, FLIP_HV = 3, FLIP_180 = 4;
    private int width = 64, height = 64;
    private int[][] symmetryMatrix = {{FLIP_NONE, FLIP_H}, {FLIP_V, FLIP_HV}};

    public TileImageFilter() {
    }

    public TileImageFilter(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int v) {
        width = v;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int v) {
        height = v;
    }

    public int[][] getSymmetryMatrix() {
        return symmetryMatrix;
    }

    public void setSymmetryMatrix(int[][] v) {
        symmetryMatrix = v;
    }

    public String toString() {
        return "Distort/Tile Image...";
    }
}
