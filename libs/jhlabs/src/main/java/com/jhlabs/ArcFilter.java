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
 * A filter which wraps an image around a circular arc.
 */
public class ArcFilter extends TransformFilter {

    private float radius = 10;
    private float height = 20;
    private float angle = 0;
    private float spreadAngle = (float) Math.PI;
    private float centreX = 0.5f;
    private float centreY = 0.5f;

    private float icentreX;
    private float icentreY;
    private float iWidth;
    private float iHeight;

    /**
     * Construct a CircleFilter.
     */
    public ArcFilter() {
        setEdgeAction(ZERO);
    }

    /**
     * Get the height of the arc.
     *
     * @return the height
     * @see #setHeight
     */
    public float getHeight() {
        return height;
    }

    /**
     * Set the height of the arc.
     *
     * @param height the height
     * @see #getHeight
     */
    public void setHeight(float height) {
        this.height = height;
    }

    /**
     * Returns the angle of the arc.
     *
     * @return the angle of the arc.
     * @see #setAngle
     */
    public float getAngle() {
        return angle;
    }

    /**
     * Set the angle of the arc.
     *
     * @param angle the angle of the arc.
     * @angle
     * @see #getAngle
     */
    public void setAngle(float angle) {
        this.angle = angle;
    }

    /**
     * Get the spread angle of the arc.
     *
     * @return the angle
     * @angle
     * @see #setSpreadAngle
     */
    public float getSpreadAngle() {
        return spreadAngle;
    }

    /**
     * Set the spread angle of the arc.
     *
     * @param spreadAngle the angle
     * @angle
     * @see #getSpreadAngle
     */
    public void setSpreadAngle(float spreadAngle) {
        this.spreadAngle = spreadAngle;
    }

    /**
     * Get the radius of the effect.
     *
     * @return the radius
     * @see #setRadius
     */
    public float getRadius() {
        return radius;
    }

    /**
     * Set the radius of the effect.
     *
     * @param radius the radius
     * @min-value 0
     * @see #getRadius
     */
    public void setRadius(float radius) {
        this.radius = radius;
    }

    /**
     * Get the centre of the effect in the X direction as a proportion of the image size.
     *
     * @return the center
     * @see #setCentreX
     */
    public float getCentreX() {
        return centreX;
    }

    /**
     * Set the centre of the effect in the Y direction as a proportion of the image size.
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
        return "Distort/Arc...";
    }

}
