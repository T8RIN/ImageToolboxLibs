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

import android.graphics.Rect;

/**
 * An abstract superclass for filters which distort images in some way. The subclass only needs to override
 * two methods to provide the mapping between source and destination pixels.
 */
public abstract class TransformFilter implements JhFilter {

    /**
     * Treat pixels off the edge as zero.
     */
    public final static int ZERO = 0;

    /**
     * Clamp pixels to the image edges.
     */
    public final static int CLAMP = 1;

    /**
     * Wrap pixels off the edge onto the oppsoite edge.
     */
    public final static int WRAP = 2;

    /**
     * Clamp pixels RGB to the image edges, but zero the alpha. This prevents gray borders on your image.
     */
    public final static int RGB_CLAMP = 3;

    /**
     * Use nearest-neighbout interpolation.
     */
    public final static int NEAREST_NEIGHBOUR = 0;

    /**
     * Use bilinear interpolation.
     */
    public final static int BILINEAR = 1;

    /**
     * The action to take for pixels off the image edge.
     */
    protected int edgeAction = RGB_CLAMP;

    /**
     * The type of interpolation to use.
     */
    protected int interpolation = BILINEAR;

    /**
     * The output image rectangle.
     */
    protected Rect transformedSpace;

    /**
     * The input image rectangle.
     */
    protected Rect originalSpace;

    /**
     * Get the action to perform for pixels off the edge of the image.
     *
     * @return one of ZERO, CLAMP or WRAP
     * @see #setEdgeAction
     */
    public int getEdgeAction() {
        return edgeAction;
    }

    /**
     * Set the action to perform for pixels off the edge of the image.
     *
     * @param edgeAction one of ZERO, CLAMP or WRAP
     * @see #getEdgeAction
     */
    public void setEdgeAction(int edgeAction) {
        this.edgeAction = edgeAction;
    }

    /**
     * Get the type of interpolation to perform.
     *
     * @return one of NEAREST_NEIGHBOUR or BILINEAR
     * @see #setInterpolation
     */
    public int getInterpolation() {
        return interpolation;
    }

    /**
     * Set the type of interpolation to perform.
     *
     * @param interpolation one of NEAREST_NEIGHBOUR or BILINEAR
     * @see #getInterpolation
     */
    public void setInterpolation(int interpolation) {
        this.interpolation = interpolation;
    }

    /**
     * Forward transform a rectangle. Used to determine the size of the output image.
     *
     * @param rect the rectangle to transform
     */
    protected void transformSpace(Rect rect) {
    }
}
