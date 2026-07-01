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
 * A filter which produces a "oil-painting" effect.
 */
public class OilFilter extends WholeImageFilter {

    private int range = 3;
    private int levels = 256;

    public OilFilter() {
    }

    /**
     * Get the range of the effect in pixels.
     *
     * @return the range
     * @see #setRange
     */
    public int getRange() {
        return range;
    }

    /**
     * Set the range of the effect in pixels.
     *
     * @param range the range
     * @see #getRange
     */
    public void setRange(int range) {
        this.range = range;
    }

    /**
     * Get the number of levels for the effect.
     *
     * @return the number of levels
     * @see #setLevels
     */
    public int getLevels() {
        return levels;
    }

    /**
     * Set the number of levels for the effect.
     *
     * @param levels the number of levels
     * @see #getLevels
     */
    public void setLevels(int levels) {
        this.levels = levels;
    }

    public String toString() {
        return "Stylize/Oil...";
    }

}
