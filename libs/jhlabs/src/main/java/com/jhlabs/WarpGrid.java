package com.jhlabs;

public class WarpGrid {
    public final int rows;
    public final int cols;
    public final float[] xGrid;
    public final float[] yGrid;

    public WarpGrid(int rows, int cols, int width, int height) {
        this.rows = rows;
        this.cols = cols;
        xGrid = new float[rows * cols];
        yGrid = new float[rows * cols];
        for (int y = 0; y < rows; y++)
            for (int x = 0; x < cols; x++) {
                int i = y * cols + x;
                xGrid[i] = x * width / (float) (cols - 1);
                yGrid[i] = y * height / (float) (rows - 1);
            }
    }
}
