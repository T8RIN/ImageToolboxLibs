package com.jhlabs;

public class MarbleTexFilter extends PointFilter {
    private float scale = 32.0f, stretch = 1.0f, angle, turbulence = 1.0f, turbulenceFactor = 0.4f;
    private Colormap colormap = new LinearColormap(0xff000000, 0xffffffff);

    public float getScale() {
        return scale;
    }

    public void setScale(float v) {
        scale = v;
    }

    public float getStretch() {
        return stretch;
    }

    public void setStretch(float v) {
        stretch = v;
    }

    public float getAngle() {
        return angle;
    }

    public void setAngle(float v) {
        angle = v;
    }

    public float getTurbulence() {
        return turbulence;
    }

    public void setTurbulence(float v) {
        turbulence = v;
    }

    public float getTurbulenceFactor() {
        return turbulenceFactor;
    }

    public void setTurbulenceFactor(float v) {
        turbulenceFactor = v;
    }

    public Colormap getColormap() {
        return colormap;
    }

    public void setColormap(Colormap v) {
        colormap = v;
    }

    public String toString() {
        return "Texture/Marble...";
    }
}
