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

import com.jhlabs.math.ImageMath;

/**
 * A class to emboss an image.
 */
public class EmbossFilter extends WholeImageFilter {

    private final static float pixelScale = 255.9f;

    private float azimuth = 135.0f * ImageMath.PI / 180.0f, elevation = 30.0f * ImageMath.PI / 180f;
    private boolean emboss = false;
    private float width45 = 3.0f;

    public EmbossFilter() {
    }

    public float getAzimuth() {
        return azimuth;
    }

    public void setAzimuth(float azimuth) {
        this.azimuth = azimuth;
    }

    public float getElevation() {
        return elevation;
    }

    public void setElevation(float elevation) {
        this.elevation = elevation;
    }

    public float getBumpHeight() {
        return width45 / 3;
    }

    public void setBumpHeight(float bumpHeight) {
        this.width45 = 3 * bumpHeight;
    }

    public boolean getEmboss() {
        return emboss;
    }

    public void setEmboss(boolean emboss) {
        this.emboss = emboss;
    }

    public String toString() {
        return "Stylize/Emboss...";
    }

}
