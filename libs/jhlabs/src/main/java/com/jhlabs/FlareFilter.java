/*
 ** Copyright 2005 Huxtable.com. All rights reserved.
 */

package com.jhlabs;

import com.jhlabs.math.Noise;

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

    public int filterRGB(int x, int y, int rgb) {
        float dx = x - centreX;
        float dy = y - centreY;
        float distance = (float) Math.sqrt(dx * dx + dy * dy);
        float a = (float) Math.exp(-distance * distance * gauss) * mix + (float) Math.exp(-distance * linear) * (1 - mix);
        float ring;

        a *= baseAmount;

        if (distance > radius + ringWidth)
            a = ImageMath.lerp((distance - (radius + ringWidth)) / falloff, a, 0);

        if (distance < radius - ringWidth || distance > radius + ringWidth)
            ring = 0;
        else {
            ring = Math.abs(distance - radius) / ringWidth;
            ring = 1 - ring * ring * (3 - 2 * ring);
            ring *= ringAmount;
        }

        a += ring;

        float angle = (float) Math.atan2(dx, dy) + ImageMath.PI;
        angle = (ImageMath.mod(angle / ImageMath.PI * 17 + 1.0f + Noise.noise1(angle * 10), 1.0f) - 0.5f) * 2;
        angle = Math.abs(angle);
        angle = (float) Math.pow(angle, 5.0);

        float b = rayAmount * angle / (1 + distance * 0.1f);
        a += b;
//		b = ImageMath.clamp(b, 0, 1);
//		rgb = PixelUtils.combinePixels(0xff802010, rgb, PixelUtils.NORMAL, (int)(b*255));

        a = ImageMath.clamp(a, 0, 1);
        return ImageMath.mixColors(a, rgb, color);
    }

    public String toString() {
        return "Stylize/Flare...";
    }
}
