/*
 ** Copyright 2005 Huxtable.com. All rights reserved.
 */

package com.jhlabs;

/**
 * An experimental filter for rendering lens flares.
 */
public class FlareFilter extends PointFilter {

    private final int rays = 50;
    private int radius;
    private float baseAmount = 1.0f;
    private float ringAmount = 0.2f;
    private float rayAmount = 0.1f;
    private int color = 0xffffffff;
    private int width, height;
    private int centreX, centreY;
    private float ringWidth = 1.6f;

    private final float linear = 0.03f;
    private final float gauss = 0.006f;
    private final float mix = 0.50f;
    private final float falloff = 6.0f;
    private float sigma;

    public FlareFilter() {
        setRadius(25);
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public float getRingWidth() {
        return ringWidth;
    }

    public void setRingWidth(float ringWidth) {
        this.ringWidth = ringWidth;
    }

    public float getBaseAmount() {
        return baseAmount;
    }

    public void setBaseAmount(float baseAmount) {
        this.baseAmount = baseAmount;
    }

    public float getRingAmount() {
        return ringAmount;
    }

    public void setRingAmount(float ringAmount) {
        this.ringAmount = ringAmount;
    }

    public float getRayAmount() {
        return rayAmount;
    }

    public void setRayAmount(float rayAmount) {
        this.rayAmount = rayAmount;
    }

    public int getRadius() {
        return radius;
    }

    public void setRadius(int radius) {
        this.radius = radius;
        sigma = (float) radius / 3;
    }

    public void setDimensions(int width, int height) {
        this.width = width;
        this.height = height;
//		radius = (int)(Math.min(width/2, height/2) - ringWidth - falloff);
        centreX = width / 2;
        centreY = height / 2;
        super.setDimensions(width, height);
    }

    public String toString() {
        return "Stylize/Flare...";
    }
}
