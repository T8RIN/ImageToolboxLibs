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
 * Scales an image using the area-averaging algorithm, which can't be done with AffineTransformOp.
 */
public class ScaleFilter implements JhFilter {

    private final int width;
    private final int height;

    /**
     * Construct a ScaleFilter.
     */
    public ScaleFilter() {
        this(32, 32);
    }

    /**
     * Construct a ScaleFilter.
     *
     * @param width  the width to scale to
     * @param height the height to scale to
     */
    public ScaleFilter(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public String toString() {
        return "Distort/Scale";
    }

}
