package com.jhlabs;

public class TextureFilter extends PointFilter {
    private float amount = 1.0f, scale = 32.0f, stretch = 1.0f, angle, turbulence = 1.0f;
    private int operation;
    private Colormap colormap = new GrayscaleColormap();

    public float getAmount() {
        return amount;
    }

    public void setAmount(float v) {
        amount = v;
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

    public float getTurbulence() {
        return turbulence;
    }

    public void setTurbulence(float v) {
        turbulence = v;
    }

    public int getOperation() {
        return operation;
    }

    public void setOperation(int v) {
        operation = v;
    }

    public Colormap getColormap() {
        return colormap;
    }

    public void setColormap(Colormap v) {
        colormap = v;
    }

    public String toString() {
        return "Texture/Texture...";
    }
}
