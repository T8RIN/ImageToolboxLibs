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

public class HSBAdjustFilter extends PointFilter {

    private float hFactor, sFactor, bFactor;
    private final float[] hsb = new float[3];

    public HSBAdjustFilter() {
        this(0, 0, 0);
    }

    public HSBAdjustFilter(float r, float g, float b) {
        hFactor = r;
        sFactor = g;
        bFactor = b;
        canFilterIndexColorModel = true;
    }

    public float getHFactor() {
        return hFactor;
    }

    public void setHFactor(float hFactor) {
        this.hFactor = hFactor;
    }

    public float getSFactor() {
        return sFactor;
    }

    public void setSFactor(float sFactor) {
        this.sFactor = sFactor;
    }

    public float getBFactor() {
        return bFactor;
    }

    public void setBFactor(float bFactor) {
        this.bFactor = bFactor;
    }

    public String toString() {
        return "Colors/Adjust HSB...";
    }
}
