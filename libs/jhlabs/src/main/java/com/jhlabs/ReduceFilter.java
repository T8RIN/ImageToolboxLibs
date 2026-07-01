/*
 ** Copyright 2005 Huxtable.com. All rights reserved.
 */

package com.jhlabs;

public class ReduceFilter extends PointFilter implements java.io.Serializable {

    private int numLevels;

    public ReduceFilter() {
        setNumLevels(6);
    }

    public int getNumLevels() {
        return numLevels;
    }

    public void setNumLevels(int numLevels) {
        this.numLevels = numLevels;
    }

    public String toString() {
        return "Colors/Posterize...";
    }

}
