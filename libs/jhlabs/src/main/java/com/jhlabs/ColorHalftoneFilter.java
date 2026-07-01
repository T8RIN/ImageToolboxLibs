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
 * A Filter to pixellate images.
 */
public class ColorHalftoneFilter implements JhFilter {

    private float dotRadius = 2;
    private float cyanScreenAngle = (float) Math.toRadians(108);
    private float magentaScreenAngle = (float) Math.toRadians(162);
    private float yellowScreenAngle = (float) Math.toRadians(90);

    public ColorHalftoneFilter() {
    }

    /**
     * Set the pixel block size.
     *
     * @param dotRadius the number of pixels along each block edge
     * @min-value 1
     * @max-value 100+
     * @see #getdotRadius
     */
    public void setdotRadius(float dotRadius) {
        this.dotRadius = dotRadius;
    }

    /**
     * Get the pixel block size.
     *
     * @return the number of pixels along each block edge
     * @see #setdotRadius
     */
    public float getdotRadius() {
        return dotRadius;
    }

    /**
     * Get the cyan screen angle.
     *
     * @return the cyan screen angle (in radians)
     * @see #setCyanScreenAngle
     */
    public float getCyanScreenAngle() {
        return cyanScreenAngle;
    }

    /**
     * Set the cyan screen angle.
     *
     * @param cyanScreenAngle the cyan screen angle (in radians)
     * @see #getCyanScreenAngle
     */
    public void setCyanScreenAngle(float cyanScreenAngle) {
        this.cyanScreenAngle = cyanScreenAngle;
    }

    /**
     * Get the magenta screen angle.
     *
     * @return the magenta screen angle (in radians)
     * @see #setMagentaScreenAngle
     */
    public float getMagentaScreenAngle() {
        return magentaScreenAngle;
    }

    /**
     * Set the magenta screen angle.
     *
     * @param magentaScreenAngle the magenta screen angle (in radians)
     * @see #getMagentaScreenAngle
     */
    public void setMagentaScreenAngle(float magentaScreenAngle) {
        this.magentaScreenAngle = magentaScreenAngle;
    }

    /**
     * Get the yellow screen angle.
     *
     * @return the yellow screen angle (in radians)
     * @see #setYellowScreenAngle
     */
    public float getYellowScreenAngle() {
        return yellowScreenAngle;
    }

    /**
     * Set the yellow screen angle.
     *
     * @param yellowScreenAngle the yellow screen angle (in radians)
     * @see #getYellowScreenAngle
     */
    public void setYellowScreenAngle(float yellowScreenAngle) {
        this.yellowScreenAngle = yellowScreenAngle;
    }

    public String toString() {
        return "Pixellate/Color Halftone...";
    }
}
