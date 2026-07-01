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
 * A filter to posterize an image.
 */
public class PosterizeFilter extends PointFilter {

    private int numLevels;

    public PosterizeFilter() {
        setNumLevels(6);
    }

    /**
     * Get the number of levels in the output image.
     *
     * @return the number of levels
     * @see #setNumLevels
     */
    public int getNumLevels() {
        return numLevels;
    }

    /**
     * Set the number of levels in the output image.
     *
     * @param numLevels the number of levels
     * @see #getNumLevels
     */
    public void setNumLevels(int numLevels) {
        this.numLevels = numLevels;
    }

    public String toString() {
        return "Colors/Posterize...";
    }

}
