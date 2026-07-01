package com.jhlabs;

public class LensBlurFilter extends WholeImageFilter {
    private float radius = 10.0f, bloom = 2.0f, bloomThreshold = 255.0f;
    private int sides = 5;

    public float getRadius() {
        return radius;
    }

    public void setRadius(float v) {
        radius = v;
    }

    public int getSides() {
        return sides;
    }

    public void setSides(int v) {
        sides = v;
    }

    public float getBloom() {
        return bloom;
    }

    public void setBloom(float v) {
        bloom = v;
    }

    public float getBloomThreshold() {
        return bloomThreshold;
    }

    public void setBloomThreshold(float v) {
        bloomThreshold = v;
    }

    public String toString() {
        return "Blur/Lens Blur...";
    }
}
