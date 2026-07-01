package com.jhlabs;

public class FBMFilter extends PointFilter {
    public static final int NOISE = 0;
    public static final int RIDGED = 1;
    public static final int VLNOISE = 2;
    public static final int SCNOISE = 3;
    public static final int CELLULAR = 4;
    private float amount = 1.0f, scale = 32.0f, stretch = 1.0f, angle, octaves = 4.0f, h = 1.0f, lacunarity = 2.0f, gain = 0.5f, bias = 0.5f;
    private int operation, basisType;
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

    public float getOctaves() {
        return octaves;
    }

    public void setOctaves(float v) {
        octaves = v;
    }

    public float getH() {
        return h;
    }

    public void setH(float v) {
        h = v;
    }

    public float getLacunarity() {
        return lacunarity;
    }

    public void setLacunarity(float v) {
        lacunarity = v;
    }

    public float getGain() {
        return gain;
    }

    public void setGain(float v) {
        gain = v;
    }

    public float getBias() {
        return bias;
    }

    public void setBias(float v) {
        bias = v;
    }

    public int getOperation() {
        return operation;
    }

    public void setOperation(int v) {
        operation = v;
    }

    public int getBasisType() {
        return basisType;
    }

    public void setBasisType(int v) {
        basisType = v;
    }

    public Colormap getColormap() {
        return colormap;
    }

    public void setColormap(Colormap v) {
        colormap = v;
    }

    public String toString() {
        return "Texture/fBm...";
    }
}
