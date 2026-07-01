package com.jhlabs;

public class BrushedMetalFilter extends WholeImageFilter {
    private int color = 0xff888888;
    private int radius = 10;
    private float amount = 0.1f;
    private boolean monochrome = true;
    private float shine = 0.1f;

    public BrushedMetalFilter() {
    }

    public BrushedMetalFilter(int color, int radius, float amount, boolean monochrome, float shine) {
        this.color = color;
        this.radius = radius;
        this.amount = amount;
        this.monochrome = monochrome;
        this.shine = shine;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int value) {
        color = value;
    }

    public int getRadius() {
        return radius;
    }

    public void setRadius(int value) {
        radius = value;
    }

    public float getAmount() {
        return amount;
    }

    public void setAmount(float value) {
        amount = value;
    }

    public boolean getMonochrome() {
        return monochrome;
    }

    public void setMonochrome(boolean value) {
        monochrome = value;
    }

    public float getShine() {
        return shine;
    }

    public void setShine(float value) {
        shine = value;
    }

    public String toString() {
        return "Texture/Brushed Metal...";
    }
}
