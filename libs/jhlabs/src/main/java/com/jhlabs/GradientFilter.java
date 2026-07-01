package com.jhlabs;

import android.graphics.Point;

public class GradientFilter extends WholeImageFilter {
    public static final int LINEAR = 0, BILINEAR = 1, RADIAL = 2, CONICAL = 3, BICONICAL = 4, SQUARE = 5;
    public static final int INT_LINEAR = 0, INT_CIRCLE_UP = 1, INT_CIRCLE_DOWN = 2, INT_SMOOTH = 3;
    private Point point1 = new Point(0, 0), point2 = new Point(64, 64);
    private int type, interpolation, paintMode;
    private float angle;
    private Colormap colormap = new LinearColormap(0xff000000, 0xffffffff);

    public GradientFilter() {
    }

    public GradientFilter(Point p1, Point p2, int color1, int color2, boolean repeat, int type, int interpolation) {
        point1 = p1;
        point2 = p2;
        colormap = new LinearColormap(color1, color2);
        this.type = type;
        this.interpolation = interpolation;
    }

    public Point getPoint1() {
        return point1;
    }

    public void setPoint1(Point v) {
        point1 = v;
    }

    public Point getPoint2() {
        return point2;
    }

    public void setPoint2(Point v) {
        point2 = v;
    }

    public int getType() {
        return type;
    }

    public void setType(int v) {
        type = v;
    }

    public int getInterpolation() {
        return interpolation;
    }

    public void setInterpolation(int v) {
        interpolation = v;
    }

    public float getAngle() {
        return angle;
    }

    public void setAngle(float v) {
        angle = v;
    }

    public Colormap getColormap() {
        return colormap;
    }

    public void setColormap(Colormap v) {
        colormap = v;
    }

    public int getPaintMode() {
        return paintMode;
    }

    public void setPaintMode(int v) {
        paintMode = v;
    }

    public String toString() {
        return "Other/Gradient Fill...";
    }
}
