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
 * A filter which allows levels adjustment on an image.
 */
public class LevelsFilter extends WholeImageFilter {

    private float lowLevel = 0;
    private float highLevel = 1;
    private float lowOutputLevel = 0;
    private float highOutputLevel = 1;

    public LevelsFilter() {
    }

    public float getLowLevel() {
        return lowLevel;
    }

    public void setLowLevel(float lowLevel) {
        this.lowLevel = lowLevel;
    }

    public float getHighLevel() {
        return highLevel;
    }

    public void setHighLevel(float highLevel) {
        this.highLevel = highLevel;
    }

    public float getLowOutputLevel() {
        return lowOutputLevel;
    }

    public void setLowOutputLevel(float lowOutputLevel) {
        this.lowOutputLevel = lowOutputLevel;
    }

    public float getHighOutputLevel() {
        return highOutputLevel;
    }

    public void setHighOutputLevel(float highOutputLevel) {
        this.highOutputLevel = highOutputLevel;
    }

    public String toString() {
        return "Colors/Levels...";
    }
}
