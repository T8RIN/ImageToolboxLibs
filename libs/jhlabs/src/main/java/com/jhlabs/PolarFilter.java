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
 * A filter which distorts and image by performing coordinate conversions between rectangular and polar coordinates.
 */
public class PolarFilter extends TransformFilter {

    /**
     * Convert from rectangular to polar coordinates.
     */
    public final static int RECT_TO_POLAR = 0;

    /**
     * Convert from polar to rectangular coordinates.
     */
    public final static int POLAR_TO_RECT = 1;

    /**
     * Invert the image in a circle.
     */
    public final static int INVERT_IN_CIRCLE = 2;

    private int type;
    private float width, height;
    private float centreX, centreY;
    private float radius;

    /**
     * Construct a PolarFilter.
     */
    public PolarFilter() {
        this(RECT_TO_POLAR);
    }

    /**
     * Construct a PolarFilter.
     *
     * @param type the distortion type
     */
    public PolarFilter(int type) {
        this.type = type;
        setEdgeAction(CLAMP);
    }

    /**
     * Get the distortion type.
     *
     * @return the distortion type
     * @see #setType
     */
    public int getType() {
        return type;
    }

    /**
     * Set the distortion type.
     *
     * @param type the distortion type
     * @see #getType
     */
    public void setType(int type) {
        this.type = type;
    }

    private float sqr(float x) {
        return x * x;
    }

    public String toString() {
        return "Distort/Polar Coordinates...";
    }

}
