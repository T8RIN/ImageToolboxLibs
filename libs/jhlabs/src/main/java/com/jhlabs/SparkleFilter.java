/*
 ** Copyright 2005 Huxtable.com. All rights reserved.
 */

package com.jhlabs;

import java.util.Random;

public class SparkleFilter extends PointFilter implements java.io.Serializable {

    static final long serialVersionUID = 1692413049411710802L;

    private int rays = 50;
    private int radius = 25;
    private int amount = 50;
    private int color = 0xffffffff;
    private int randomness = 25;
    private int width, height;
    private int centreX, centreY;
    private final long seed = 371;
    private float[] rayLengths;
    private final Random randomNumbers = new Random();

    public SparkleFilter() {
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public int getRandomness() {
        return randomness;
    }

    public void setRandomness(int randomness) {
        this.randomness = randomness;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public int getRays() {
        return rays;
    }

    public void setRays(int rays) {
        this.rays = rays;
    }

    public int getRadius() {
        return radius;
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }

    public void setDimensions(int width, int height) {
        this.width = width;
        this.height = height;
        centreX = width / 2;
        centreY = height / 2;
        super.setDimensions(width, height);
        randomNumbers.setSeed(seed);
        rayLengths = new float[rays];
        for (int i = 0; i < rays; i++)
            rayLengths[i] = radius + randomness / 100.0f * radius * (float) randomNumbers.nextGaussian();
    }

    public int filterRGB(int x, int y, int rgb) {
        float dx = x - centreX;
        float dy = y - centreY;
        float distance = dx * dx + dy * dy;
        float angle = (float) Math.atan2(dy, dx);
        float d = (angle + ImageMath.PI) / (ImageMath.TWO_PI) * rays;
        int i = (int) d;
        float f = d - i;

        if (radius != 0) {
            float length = ImageMath.lerp(f, rayLengths[i % rays], rayLengths[(i + 1) % rays]);
            float g = length * length / (distance + 0.0001f);
            g = (float) Math.pow(g, (100 - amount) / 50.0);
            f -= 0.5f;
//			f *= amount/50.0f;
            f = 1 - f * f;
            f *= g;
        }
        f = ImageMath.clamp(f, 0, 1);
        return ImageMath.mixColors(f, rgb, color);
    }

    public String toString() {
        return "Stylize/Sparkle...";
    }
}
