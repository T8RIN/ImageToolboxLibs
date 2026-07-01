package com.jhlabs;

public class SphereFilter extends TransformFilter {
    private float refractionIndex = 1.5f, radius, centreX = 0.5f, centreY = 0.5f;

    public SphereFilter() {
        setEdgeAction(CLAMP);
    }

    public float getRefractionIndex() {
        return refractionIndex;
    }

    public void setRefractionIndex(float v) {
        refractionIndex = v;
    }

    public float getRadius() {
        return radius;
    }

    public void setRadius(float v) {
        radius = v;
    }

    public float getCentreX() {
        return centreX;
    }

    public void setCentreX(float v) {
        centreX = v;
    }

    public float getCentreY() {
        return centreY;
    }

    public void setCentreY(float v) {
        centreY = v;
    }

    public void setCentre(float x, float y) {
        centreX = x;
        centreY = y;
    }

    public String toString() {
        return "Distort/Sphere...";
    }
}
