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

public class RGBAdjustFilter extends PointFilter {

    public float rFactor, gFactor, bFactor;

    public RGBAdjustFilter() {
        this(0, 0, 0);
    }

    public RGBAdjustFilter(float r, float g, float b) {
        rFactor = 1 + r;
        gFactor = 1 + g;
        bFactor = 1 + b;
        canFilterIndexColorModel = true;
    }

    public float getRFactor() {
        return rFactor - 1;
    }

    public void setRFactor(float rFactor) {
        this.rFactor = 1 + rFactor;
    }

    public float getGFactor() {
        return gFactor - 1;
    }

    public void setGFactor(float gFactor) {
        this.gFactor = 1 + gFactor;
    }

    public float getBFactor() {
        return bFactor - 1;
    }

    public void setBFactor(float bFactor) {
        this.bFactor = 1 + bFactor;
    }

    public String toString() {
        return "Colors/Adjust RGB...";
    }
}
