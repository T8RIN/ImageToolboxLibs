package com.jhlabs;

public class CausticsFilter extends WholeImageFilter {
    private float scale = 32.0f;
    private int brightness = 10;
    private float amount = 1.0f;
    private float turbulence = 1.0f;
    private float dispersion;
    private float time;
    private int samples = 2;
    private int bgColor = 0xff799fff;

    public float getScale() {
        return scale;
    }

    public void setScale(float value) {
        scale = value;
    }

    public int getBrightness() {
        return brightness;
    }

    public void setBrightness(int value) {
        brightness = value;
    }

    public float getAmount() {
        return amount;
    }

    public void setAmount(float value) {
        amount = value;
    }

    public float getTurbulence() {
        return turbulence;
    }

    public void setTurbulence(float value) {
        turbulence = value;
    }

    public float getDispersion() {
        return dispersion;
    }

    public void setDispersion(float value) {
        dispersion = value;
    }

    public float getTime() {
        return time;
    }

    public void setTime(float value) {
        time = value;
    }

    public int getSamples() {
        return samples;
    }

    public void setSamples(int value) {
        samples = value;
    }

    public int getBgColor() {
        return bgColor;
    }

    public void setBgColor(int value) {
        bgColor = value;
    }

    public String toString() {
        return "Texture/Caustics...";
    }
}
