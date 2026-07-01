package com.jhlabs;

public class ShadowFilter extends WholeImageFilter {
    private float radius = 5.0f, angle = 4.712389f, distance = 5.0f, opacity = 0.5f;
    private boolean addMargins, shadowOnly;
    private int shadowColor = 0xff000000;

    public ShadowFilter() {
    }

    public ShadowFilter(float radius, float xOffset, float yOffset, float opacity) {
        this.radius = radius;
        angle = (float) Math.atan2(yOffset, xOffset);
        distance = (float) Math.hypot(xOffset, yOffset);
        this.opacity = opacity;
    }

    public float getRadius() {
        return radius;
    }

    public void setRadius(float v) {
        radius = v;
    }

    public float getAngle() {
        return angle;
    }

    public void setAngle(float v) {
        angle = v;
    }

    public float getDistance() {
        return distance;
    }

    public void setDistance(float v) {
        distance = v;
    }

    public float getOpacity() {
        return opacity;
    }

    public void setOpacity(float v) {
        opacity = v;
    }

    public int getShadowColor() {
        return shadowColor;
    }

    public void setShadowColor(int v) {
        shadowColor = v;
    }

    public boolean getAddMargins() {
        return addMargins;
    }

    public void setAddMargins(boolean v) {
        addMargins = v;
    }

    public boolean getShadowOnly() {
        return shadowOnly;
    }

    public void setShadowOnly(boolean v) {
        shadowOnly = v;
    }

    public String toString() {
        return "Stylize/Drop Shadow...";
    }
}
