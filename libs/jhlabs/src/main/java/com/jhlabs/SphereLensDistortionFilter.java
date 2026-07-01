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
 * A filter which simulates a lens placed over an image.
 */
public class SphereLensDistortionFilter extends TransformFilter {

    private float a = 0;
    private float b = 0;
    private float a2 = 0;
    private float b2 = 0;
    private float centreX = 0.5f;
    private float centreY = 0.5f;
    private float refractionIndex = 1.5f;

    private float icentreX;
    private float icentreY;

    public SphereLensDistortionFilter() {
        setEdgeAction(CLAMP);
        setRadius(100.0f);
    }

    /**
     * Get the index of refaction.
     *
     * @return the index of refaction
     * @see #setRefractionIndex
     */
    public float getRefractionIndex() {
        return refractionIndex;
    }

    /**
     * Set the index of refaction.
     *
     * @param refractionIndex the index of refaction
     * @see #getRefractionIndex
     */
    public void setRefractionIndex(float refractionIndex) {
        this.refractionIndex = refractionIndex;
    }

    /**
     * Get the radius of the effect.
     *
     * @return the radius
     * @see #setRadius
     */
    public float getRadius() {
        return a;
    }

    /**
     * Set the radius of the effect.
     *
     * @param r the radius
     * @min-value 0
     * @see #getRadius
     */
    public void setRadius(float r) {
        this.a = r;
        this.b = r;
    }

    public float getCentreX() {
        return centreX;
    }

    /**
     * Set the centre of the effect in the X direction as a proportion of the image size.
     *
     * @param centreX the center
     * @see #getCentreX
     */
    public void setCentreX(float centreX) {
        this.centreX = centreX;
    }

    /**
     * Get the centre of the effect in the Y direction as a proportion of the image size.
     *
     * @return the center
     * @see #setCentreY
     */
    public float getCentreY() {
        return centreY;
    }

    /**
     * Set the centre of the effect in the Y direction as a proportion of the image size.
     *
     * @param centreY the center
     * @see #getCentreY
     */
    public void setCentreY(float centreY) {
        this.centreY = centreY;
    }

    /**
     * Set the centre of the effect as a proportion of the image size.
     *
     * @param centre the center
     * @see #getCentre
     */
    public void setCentre(float x, float y) {
        this.centreX = x;
        this.centreY = y;
    }

    /**
     * Get the centre of the effect as a proportion of the image size.
     *
     * @return the center
     * @see #setCentre
     */
    public float[] getCentre() {
        float[] ret = new float[2];
        ret[0] = centreX;
        ret[1] = centreY;
        return ret;
    }

    public String toString() {
        return "Distort/Sphere...";
    }

}
