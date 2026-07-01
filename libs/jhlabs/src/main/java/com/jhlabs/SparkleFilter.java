/*
 ** Copyright 2005 Huxtable.com. All rights reserved.
 */

package com.jhlabs;

import java.util.Date;
import java.util.Random;

public class SparkleFilter extends PointFilter implements java.io.Serializable {

    private int amount = 50;
    private int rays = 50;
    private int radius = 25;
    private int randomness = 25;
    private final int centreX;
    private final int centreY;
    private int color = 0xffffffff;

    private int width, height;
    private long seed = 371;
    private float[] rayLengths;
    private final Random randomNumbers = new Random();

    public SparkleFilter(int centreX, int centreY) {
        this.centreX = centreX;
        this.centreY = centreY;
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

    public void randomize() {
        seed = new Date().getTime();
    }

    public void setDimensions(int width, int height) {
        this.width = width;
        this.height = height;
        super.setDimensions(width, height);
        randomNumbers.setSeed(seed);
        rayLengths = new float[rays];
        for (int i = 0; i < rays; i++)
            rayLengths[i] = radius + randomness / 100.0f * radius * (float) randomNumbers.nextGaussian();
    }

    public String toString() {
        return "Stylize/Sparkle...";
    }
}
