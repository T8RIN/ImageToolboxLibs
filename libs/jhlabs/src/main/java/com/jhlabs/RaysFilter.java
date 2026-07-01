package com.jhlabs;

public class RaysFilter extends WholeImageFilter {
    private float opacity = 1.0f, threshold, strength = 0.5f;
    private boolean raysOnly;
    private Colormap colormap;

    public float getOpacity() {
        return opacity;
    }

    public void setOpacity(float v) {
        opacity = v;
    }

    public float getThreshold() {
        return threshold;
    }

    public void setThreshold(float v) {
        threshold = v;
    }

    public float getStrength() {
        return strength;
    }

    public void setStrength(float v) {
        strength = v;
    }

    public boolean getRaysOnly() {
        return raysOnly;
    }

    public void setRaysOnly(boolean v) {
        raysOnly = v;
    }

    public Colormap getColormap() {
        return colormap;
    }

    public void setColormap(Colormap v) {
        colormap = v;
    }

    public String toString() {
        return "Blur/Rays...";
    }
}
