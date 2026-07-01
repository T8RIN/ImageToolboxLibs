package com.jhlabs;

public class BorderFilter extends WholeImageFilter {
    private int leftBorder;
    private int rightBorder;
    private int topBorder;
    private int bottomBorder;
    private int borderColor;

    public BorderFilter() {
    }

    public BorderFilter(int left, int top, int right, int bottom, int color) {
        leftBorder = left;
        topBorder = top;
        rightBorder = right;
        bottomBorder = bottom;
        borderColor = color;
    }

    public int getLeftBorder() {
        return leftBorder;
    }

    public void setLeftBorder(int value) {
        leftBorder = value;
    }

    public int getRightBorder() {
        return rightBorder;
    }

    public void setRightBorder(int value) {
        rightBorder = value;
    }

    public int getTopBorder() {
        return topBorder;
    }

    public void setTopBorder(int value) {
        topBorder = value;
    }

    public int getBottomBorder() {
        return bottomBorder;
    }

    public void setBottomBorder(int value) {
        bottomBorder = value;
    }

    public int getBorderColor() {
        return borderColor;
    }

    public void setBorderColor(int color) {
        borderColor = color;
    }

    public String toString() {
        return "Distort/Add Border...";
    }
}
