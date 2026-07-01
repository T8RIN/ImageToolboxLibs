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

public class WeaveFilter extends PointFilter {

    public int[][] matrix = {
            {0, 1, 0, 1},
            {1, 0, 1, 0},
            {0, 1, 0, 1},
            {1, 0, 1, 0},
    };
    private float xWidth = 16;
    private float yWidth = 16;
    private float xGap = 6;
    private float yGap = 6;
    private final int rows = 4;
    private final int cols = 4;
    private final int rgbX = 0xffff8080;
    private final int rgbY = 0xff8080ff;
    private boolean useImageColors = true;
    private boolean roundThreads = false;
    private boolean shadeCrossings = true;

    public WeaveFilter() {
    }

    public float getXWidth() {
        return xWidth;
    }

    public void setXWidth(float xWidth) {
        this.xWidth = xWidth;
    }

    public float getYWidth() {
        return yWidth;
    }

    public void setYWidth(float yWidth) {
        this.yWidth = yWidth;
    }

    public float getXGap() {
        return xGap;
    }

    public void setXGap(float xGap) {
        this.xGap = xGap;
    }

    public float getYGap() {
        return yGap;
    }

    public void setYGap(float yGap) {
        this.yGap = yGap;
    }

    public int[][] getCrossings() {
        return matrix;
    }

    public void setCrossings(int[][] matrix) {
        this.matrix = matrix;
    }

    public boolean getUseImageColors() {
        return useImageColors;
    }

    public void setUseImageColors(boolean useImageColors) {
        this.useImageColors = useImageColors;
    }

    public boolean getRoundThreads() {
        return roundThreads;
    }

    public void setRoundThreads(boolean roundThreads) {
        this.roundThreads = roundThreads;
    }

    public boolean getShadeCrossings() {
        return shadeCrossings;
    }

    public void setShadeCrossings(boolean shadeCrossings) {
        this.shadeCrossings = shadeCrossings;
    }

    public String toString() {
        return "Texture/Weave...";
    }

}
