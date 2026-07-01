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

import androidx.annotation.NonNull;

/**
 * A filter which draws contours on an image at given brightness levels.
 */
public class ContourFilter extends WholeImageFilter {

    private float levels = 5;
    private float scale = 1;
    private float offset = 0;
    private int contourColor = 0xff000000;

    public ContourFilter() {
    }

    public float getLevels() {
        return levels;
    }

    public void setLevels(float levels) {
        this.levels = levels;
    }

    /**
     * Returns the scale of the contours.
     *
     * @return the scale of the contours.
     * @see #setScale
     */
    public float getScale() {
        return scale;
    }

    /**
     * Specifies the scale of the contours.
     *
     * @param scale the scale of the contours.
     * @min-value 0
     * @max-value 1
     * @see #getScale
     */
    public void setScale(float scale) {
        this.scale = scale;
    }

    public float getOffset() {
        return offset;
    }

    public void setOffset(float offset) {
        this.offset = offset;
    }

    public int getContourColor() {
        return contourColor;
    }

    public void setContourColor(int contourColor) {
        this.contourColor = contourColor;
    }

    @NonNull
    @Override
    public String toString() {
        return "Contour";
    }
}
