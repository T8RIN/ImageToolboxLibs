package com.jhlabs;

public class ChromeFilter extends WholeImageFilter {
    private float amount = 0.5f;
    private float exposure = 1.0f;

    public float getAmount() {
        return amount;
    }

    public void setAmount(float value) {
        amount = value;
    }

    public float getExposure() {
        return exposure;
    }

    public void setExposure(float value) {
        exposure = value;
    }

    public String toString() {
        return "Effects/Chrome...";
    }
}
