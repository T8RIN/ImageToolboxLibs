package com.jhlabs;

public class ContrastFilter extends TransferFilter {
    private float brightness = 1.0f;
    private float contrast = 0.5f;

    public float getBrightness() {
        return brightness;
    }

    public void setBrightness(float brightness) {
        this.brightness = brightness;
    }

    public float getContrast() {
        return contrast;
    }

    public void setContrast(float contrast) {
        this.contrast = contrast;
    }

    public String toString() {
        return "Colors/Contrast...";
    }
}
