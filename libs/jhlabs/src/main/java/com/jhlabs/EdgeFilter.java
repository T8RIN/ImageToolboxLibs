/*
Copyright 2006 Jerry Huxtable

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package com.jhlabs;

/**
 * An edge-detection filter.
 */
public class EdgeFilter extends WholeImageFilter {

    public final static float R2 = (float) Math.sqrt(2);

    public final static float[] ROBERTS_V = {
            0, 0, -1,
            0, 1, 0,
            0, 0, 0,
    };
    public final static float[] ROBERTS_H = {
            -1, 0, 0,
            0, 1, 0,
            0, 0, 0,
    };
    public final static float[] PREWITT_V = {
            -1, 0, 1,
            -1, 0, 1,
            -1, 0, 1,
    };
    public final static float[] PREWITT_H = {
            -1, -1, -1,
            0, 0, 0,
            1, 1, 1,
    };
    public final static float[] SOBEL_V = {
            -1, 0, 1,
            -2, 0, 2,
            -1, 0, 1,
    };
    public final static float[] FREI_CHEN_V = {
            -1, 0, 1,
            -R2, 0, R2,
            -1, 0, 1,
    };
    public static float[] SOBEL_H = {
            -1, -2, -1,
            0, 0, 0,
            1, 2, 1,
    };
    public static float[] FREI_CHEN_H = {
            -1, -R2, -1,
            0, 0, 0,
            1, R2, 1,
    };

    protected float[] vEdgeMatrix = SOBEL_V;
    protected float[] hEdgeMatrix = SOBEL_H;

    public EdgeFilter() {
    }

    public float[] getVEdgeMatrix() {
        return vEdgeMatrix;
    }

    public void setVEdgeMatrix(float[] vEdgeMatrix) {
        this.vEdgeMatrix = vEdgeMatrix;
    }

    public float[] getHEdgeMatrix() {
        return hEdgeMatrix;
    }

    public void setHEdgeMatrix(float[] hEdgeMatrix) {
        this.hEdgeMatrix = hEdgeMatrix;
    }

    public String toString() {
        return "Edges/Detect Edges";
    }
}
