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
 * A filter which flips images or rotates by multiples of 90 degrees.
 */
public class FlipFilter implements JhFilter {

    /**
     * Flip the image horizontally.
     */
    public static final int FLIP_H = 1;

    /**
     * Flip the image vertically.
     */
    public static final int FLIP_V = 2;

    /**
     * Flip the image horizontally and vertically.
     */
    public static final int FLIP_HV = 3;

    /**
     * Rotate the image 90 degrees clockwise.
     */
    public static final int FLIP_90CW = 4;

    /**
     * Rotate the image 90 degrees counter-clockwise.
     */
    public static final int FLIP_90CCW = 5;

    /**
     * Rotate the image 180 degrees.
     */
    public static final int FLIP_180 = 6;

    private int operation;

    /**
     * Construct a FlipFilter which flips horizontally and vertically.
     */
    public FlipFilter() {
        this(FLIP_HV);
    }

    /**
     * Construct a FlipFilter.
     *
     * @param operation the filter operation
     */
    public FlipFilter(int operation) {
        this.operation = operation;
    }

    /**
     * Get the filter operation.
     *
     * @return the filter operation
     * @see #setOperation
     */
    public int getOperation() {
        return operation;
    }

    /**
     * Set the filter operation.
     *
     * @param operation the filter operation
     * @see #getOperation
     */
    public void setOperation(int operation) {
        this.operation = operation;
    }

    public String toString() {
        switch (operation) {
            case FLIP_H:
                return "Flip Horizontal";
            case FLIP_V:
                return "Flip Vertical";
            case FLIP_HV:
                return "Flip Diagonal";
            case FLIP_90CW:
                return "Rotate 90";
            case FLIP_90CCW:
                return "Rotate -90";
            case FLIP_180:
                return "Rotate 180";
        }
        return "Flip";
    }
}
