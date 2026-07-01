package com.jhlabs;

public class WarpFilter extends WholeImageFilter {
    private WarpGrid sourceGrid, destGrid;
    private int frames = 1;

    public WarpFilter() {
    }

    public WarpFilter(WarpGrid sourceGrid, WarpGrid destGrid) {
        this.sourceGrid = sourceGrid;
        this.destGrid = destGrid;
    }

    public WarpGrid getSourceGrid() {
        return sourceGrid;
    }

    public void setSourceGrid(WarpGrid v) {
        sourceGrid = v;
    }

    public WarpGrid getDestGrid() {
        return destGrid;
    }

    public void setDestGrid(WarpGrid v) {
        destGrid = v;
    }

    public int getFrames() {
        return frames;
    }

    public void setFrames(int v) {
        frames = v;
    }

    public String toString() {
        return "Distort/Warp...";
    }
}
