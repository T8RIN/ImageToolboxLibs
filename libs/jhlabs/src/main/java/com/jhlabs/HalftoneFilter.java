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
 * A filter which uses a another image as a ask to produce a halftoning effect.
 */
public class HalftoneFilter implements JhFilter {

    private float softness = 0.1f;
    private boolean invert;
    private boolean monochrome;
    private int[] mask;
    private int maskWidth;
    private int maskHeight;

    public HalftoneFilter() {
    }

    /**
     * Get the softness of the effect.
     *
     * @return the softness
     * @see #setSoftness
     */
    public float getSoftness() {
        return softness;
    }

    /**
     * Set the softness of the effect in the range 0..1.
     *
     * @param softness the softness
     * @min-value 0
     * @max-value 1
     * @see #getSoftness
     */
    public void setSoftness(float softness) {
        this.softness = softness;
    }

    /**
     * Get the halftone mask.
     *
     * @return the mask
     * @see #setMask
     */
    public int[] getMask() {
        return mask;
    }

    /**
     * Set the halftone mask.
     *
     * @param mask the mask
     * @see #getMask
     */
    public void setMask(int[] mask) {
        this.mask = mask;
    }

    public void setMaskWidth(int maskwidth) {
        maskWidth = maskwidth;
    }

    public void setMaskHeight(int maskheight) {
        maskHeight = maskheight;
    }

    public boolean getInvert() {
        return invert;
    }

    public void setInvert(boolean invert) {
        this.invert = invert;
    }

    /**
     * Get whether to do monochrome halftoning.
     *
     * @return true for monochrome halftoning
     * @see #setMonochrome
     */
    public boolean getMonochrome() {
        return monochrome;
    }

    /**
     * Set whether to do monochrome halftoning.
     *
     * @param monochrome true for monochrome halftoning
     * @see #getMonochrome
     */
    public void setMonochrome(boolean monochrome) {
        this.monochrome = monochrome;
    }

    public String toString() {
        return "Stylize/Halftone...";
    }
}
