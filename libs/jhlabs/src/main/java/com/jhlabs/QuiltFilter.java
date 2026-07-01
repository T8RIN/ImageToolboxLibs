/*
 ** Copyright 2005 Huxtable.com. All rights reserved.
 */

package com.jhlabs;

import java.util.Date;
import java.util.Random;

public class QuiltFilter extends WholeImageFilter implements java.io.Serializable {

    private final Random randomGenerator;
    private long seed = 567;
    private int iterations = 25000;
    private float a = -0.59f;
    private float b = 0.2f;
    private float c = 0.1f;
    private float d = 0;
    private int k = 0;
    private Colormap colormap = new LinearColormap();

    public QuiltFilter() {
        randomGenerator = new Random();
    }

    public void randomize() {
        seed = new Date().getTime();
        randomGenerator.setSeed(seed);
        a = randomGenerator.nextFloat();
        b = randomGenerator.nextFloat();
        c = randomGenerator.nextFloat();
        d = randomGenerator.nextFloat();
        k = randomGenerator.nextInt() % 20 - 10;
    }

    public int getIterations() {
        return iterations;
    }

    public void setIterations(int iterations) {
        this.iterations = iterations;
    }

    public float getA() {
        return a;
    }

    public void setA(float a) {
        this.a = a;
    }

    public float getB() {
        return b;
    }

    public void setB(float b) {
        this.b = b;
    }

    public float getC() {
        return c;
    }

    public void setC(float c) {
        this.c = c;
    }

    public float getD() {
        return d;
    }

    public void setD(float d) {
        this.d = d;
    }

    public int getK() {
        return k;
    }

    public void setK(int k) {
        this.k = k;
    }

    public Colormap getColormap() {
        return colormap;
    }

    public void setColormap(Colormap colormap) {
        this.colormap = colormap;
    }

    public String toString() {
        return "Texture/Chaotic Quilt...";
    }

}
