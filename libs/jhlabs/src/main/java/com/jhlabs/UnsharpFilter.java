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
 * A filter which subtracts Gaussian blur from an image, sharpening it.
 *
 * @author Jerry Huxtable
 */
public class UnsharpFilter extends GaussianFilter {

    private float amount = 0.5f;
    private int threshold = 1;

    public UnsharpFilter() {
        radius = 2;
    }

    /**
     * Get the threshold value.
     *
     * @return the threshold value
     * @see #setThreshold
     */
    public int getThreshold() {
        return threshold;
    }

    /**
     * Set the threshold value.
     *
     * @param threshold the threshold value
     * @see #getThreshold
     */
    public void setThreshold(int threshold) {
        this.threshold = threshold;
    }

    /**
     * Get the amount of sharpening.
     *
     * @return the amount
     * @see #setAmount
     */
    public float getAmount() {
        return amount;
    }

    /**
     * Set the amount of sharpening.
     *
     * @param amount the amount
     * @min-value 0
     * @max-value 1
     * @see #getAmount
     */
    public void setAmount(float amount) {
        this.amount = amount;
    }

    public String toString() {
        return "Blur/Unsharp Mask...";
    }
}
