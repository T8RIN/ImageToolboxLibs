package com.jhlabs;

public class MirrorFilter extends WholeImageFilter {
    private float angle, distance, rotation, gap, opacity = 1.0f, centreY = 0.5f;

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

    public float getRotation() {
        return rotation;
    }

    public void setRotation(float v) {
        rotation = v;
    }

    public float getGap() {
        return gap;
    }

    public void setGap(float v) {
        gap = v;
    }

    public float getOpacity() {
        return opacity;
    }

    public void setOpacity(float v) {
        opacity = v;
    }

    public float getCentreY() {
        return centreY;
    }

    public void setCentreY(float v) {
        centreY = v;
    }

    public String toString() {
        return "Effects/Mirror...";
    }
}
