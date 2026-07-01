package com.jhlabs;

public class WoodFilter extends PointFilter {
    private float rings = 0.5f, turbulence = 1.0f, gain = 0.8f, bias = 0.1f, scale = 200.0f, stretch = 10.0f, angle;
    private Colormap colormap = new LinearColormap(0xffe5c494, 0xff987b51);

    public float getRings() {
        return rings;
    }

    public void setRings(float v) {
        rings = v;
    }

    public float getTurbulence() {
        return turbulence;
    }

    public void setTurbulence(float v) {
        turbulence = v;
    }

    public float getGain() {
        return gain;
    }

    public void setGain(float v) {
        gain = v;
    }

    public float getBias() {
        return bias;
    }

    public void setBias(float v) {
        bias = v;
    }

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

    public Colormap getColormap() {
        return colormap;
    }

    public void setColormap(Colormap v) {
        colormap = v;
    }

    public String toString() {
        return "Texture/Wood...";
    }
}
