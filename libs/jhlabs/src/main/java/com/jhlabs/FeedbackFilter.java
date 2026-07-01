package com.jhlabs;

public class FeedbackFilter extends WholeImageFilter {
    private float angle, distance, rotation, zoom, centreX = 0.5f, centreY = 0.5f;
    private int iterations = 3;

    public FeedbackFilter() {
    }

    public FeedbackFilter(float distance, float angle, float rotation, float zoom) {
        this.distance = distance;
        this.angle = angle;
        this.rotation = rotation;
        this.zoom = zoom;
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

    public float getRotation() {
        return rotation;
    }

    public void setRotation(float v) {
        rotation = v;
    }

    public float getZoom() {
        return zoom;
    }

    public void setZoom(float v) {
        zoom = v;
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

    public int getIterations() {
        return iterations;
    }

    public void setIterations(int v) {
        iterations = v;
    }

    public String toString() {
        return "Effects/Feedback...";
    }
}
