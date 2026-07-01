package com.jhlabs;

public class CheckFilter extends PointFilter {
    private int foreground = 0xffffffff;
    private int background = 0xff000000;
    private int xScale = 8;
    private int yScale = 8;
    private int fuzziness;
    private int operation;
    private float angle;

    public int getForeground() {
        return foreground;
    }

    public void setForeground(int value) {
        foreground = value;
    }

    public int getBackground() {
        return background;
    }

    public void setBackground(int value) {
        background = value;
    }

    public int getXScale() {
        return xScale;
    }

    public void setXScale(int value) {
        xScale = value;
    }

    public int getYScale() {
        return yScale;
    }

    public void setYScale(int value) {
        yScale = value;
    }

    public int getFuzziness() {
        return fuzziness;
    }

    public void setFuzziness(int value) {
        fuzziness = value;
    }

    public int getOperation() {
        return operation;
    }

    public void setOperation(int value) {
        operation = value;
    }

    public float getAngle() {
        return angle;
    }

    public void setAngle(float value) {
        angle = value;
    }

    public String toString() {
        return "Texture/Checkerboard...";
    }
}
