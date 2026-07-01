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
 * A filter which produces motion blur the slow, but higher-quality way.
 */
public class MotionBlurFilter implements JhFilter {

    private float angle = 0.0f;
    private final float falloff = 1.0f;
    private float distance = 1.0f;
    private float zoom = 0.0f;
    private float rotation = 0.0f;
    private boolean wrapEdges = false;
    private boolean premultiplyAlpha = true;

    /**
     * Construct a MotionBlurFilter.
     */
    public MotionBlurFilter() {
    }

    /**
     * Construct a MotionBlurFilter.
     *
     * @param distance the distance of blur.
     * @param angle    the angle of blur.
     * @param rotation the angle of rotation.
     * @param zoom     the zoom factor.
     */
    public MotionBlurFilter(float distance, float angle, float rotation, float zoom) {
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
     * Get whether to wrap at the image edges.
     *
     * @return true if it should wrap.
     * @see #setWrapEdges
     */
    public boolean getWrapEdges() {
        return wrapEdges;
    }

    /**
     * Set whether to wrap at the image edges.
     *
     * @param wrapEdges true if it should wrap.
     * @see #getWrapEdges
     */
    public void setWrapEdges(boolean wrapEdges) {
        this.wrapEdges = wrapEdges;
    }

    /**
     * Get whether to premultiply the alpha channel.
     *
     * @return true to premultiply the alpha
     * @see #setPremultiplyAlpha
     */
    public boolean getPremultiplyAlpha() {
        return premultiplyAlpha;
    }

    /**
     * Set whether to premultiply the alpha channel.
     *
     * @param premultiplyAlpha true to premultiply the alpha
     * @see #getPremultiplyAlpha
     */
    public void setPremultiplyAlpha(boolean premultiplyAlpha) {
        this.premultiplyAlpha = premultiplyAlpha;
    }

    public String toString() {
        return "Blur/Motion Blur...";
    }
}
