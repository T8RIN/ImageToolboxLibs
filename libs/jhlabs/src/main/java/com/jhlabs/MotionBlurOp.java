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

import android.graphics.Bitmap;

/**
 * A filter which produces motion blur the faster, but lower-quality way.
 */
public class MotionBlurOp implements JhFilter {

    private float centreX = 0.5f, centreY = 0.5f;
    private float distance;
    private float angle;
    private float rotation;
    private float zoom;

    /**
     * Construct a MotionBlurOp.
     */
    public MotionBlurOp() {
    }

    /**
     * Construct a MotionBlurOp.
     *
     * @param distance the distance of blur.
     * @param angle    the angle of blur.
     * @param rotation the angle of rotation.
     * @param zoom     the zoom factor.
     */
    public MotionBlurOp(float distance, float angle, float rotation, float zoom) {
        this.distance = distance;
        this.angle = angle;
        this.rotation = rotation;
        this.zoom = zoom;
    }

    /**
     * Returns the angle of blur.
     *
     * @return the angle of blur.
     * @see #setAngle
     */
    public float getAngle() {
        return angle;
    }

    /**
     * Specifies the angle of blur.
     *
     * @param angle the angle of blur.
     * @angle
     * @see #getAngle
     */
    public void setAngle(float angle) {
        this.angle = angle;
    }

    /**
     * Get the distance of blur.
     *
     * @return the distance of blur.
     * @see #setDistance
     */
    public float getDistance() {
        return distance;
    }

    /**
     * Set the distance of blur.
     *
     * @param distance the distance of blur.
     * @see #getDistance
     */
    public void setDistance(float distance) {
        this.distance = distance;
    }

    /**
     * Get the blur rotation.
     *
     * @return the angle of rotation.
     * @see #setRotation
     */
    public float getRotation() {
        return rotation;
    }

    /**
     * Set the blur rotation.
     *
     * @param rotation the angle of rotation.
     * @see #getRotation
     */
    public void setRotation(float rotation) {
        this.rotation = rotation;
    }

    /**
     * Get the blur zoom.
     *
     * @return the zoom factor.
     * @see #setZoom
     */
    public float getZoom() {
        return zoom;
    }

    /**
     * Set the blur zoom.
     *
     * @param zoom the zoom factor.
     * @see #getZoom
     */
    public void setZoom(float zoom) {
        this.zoom = zoom;
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
    public void setCentre(float centerX, float centerY) {
        this.centreX = centerX;
        this.centreY = centerY;
    }

    private int log2(int n) {
        int m = 1;
        int log2n = 0;

        while (m < n) {
            m *= 2;
            log2n++;
        }
        return log2n;
    }

    /**
     * TODO 문제가 있음 Bitmap 해제를 해줄것
     *
     * @param src
     * @param w
     * @param h
     * @return
     */

    public String toString() {
        return "Blur/Faster Motion Blur...";
    }
}
