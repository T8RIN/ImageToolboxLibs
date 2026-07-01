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
 * This filter diffuses an image by moving its pixels in random directions.
 */
public class DiffuseFilter extends TransformFilter {

    private float[] sinTable, cosTable;
    private float scale = 4;

    public DiffuseFilter() {
        setEdgeAction(CLAMP);
    }

    /**
     * Returns the scale of the texture.
     *
     * @return the scale of the texture.
     * @see #setScale
     */
    public float getScale() {
        return scale;
    }

    /**
     * Specifies the scale of the texture.
     *
     * @param scale the scale of the texture.
     * @min-value 1
     * @max-value 100+
     * @see #getScale
     */
    public void setScale(float scale) {
        this.scale = scale;
    }

    public String toString() {
        return "Distort/Diffuse...";
    }
}
