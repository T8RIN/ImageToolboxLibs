package com.jhlabs;

public class PlasmaFilter extends WholeImageFilter implements MutatableFilter {
    private float turbulence = 1.0f, scaling;
    private boolean useImageColors;
    private long seed = 567;
    private Colormap colormap = new LinearColormap(0xff000000, 0xffffffff);

    public float getTurbulence() {
        return turbulence;
    }

    public void setTurbulence(float v) {
        turbulence = v;
    }

    public float getScaling() {
        return scaling;
    }

    public void setScaling(float v) {
        scaling = v;
    }

    public boolean getUseImageColors() {
        return useImageColors;
    }

    public void setUseImageColors(boolean v) {
        useImageColors = v;
    }

    public long getSeed() {
        return seed;
    }

    public Colormap getColormap() {
        return colormap;
    }

    public void setColormap(Colormap v) {
        colormap = v;
    }

    public void randomize() {
        seed = System.nanoTime();
    }

    public void mutate(float amount) {
        seed += (long) (amount * 1000.0f);
    }

    public String toString() {
        return "Texture/Plasma...";
    }
}
