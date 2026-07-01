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

import android.graphics.Point;

/**
 * A class which warps an image using a field Warp algorithm.
 */
public class FieldWarpFilter extends TransformFilter {

    private float amount = 1.0f;
    private float power = 1.0f;
    private float strength = 2.0f;
    private Line[] inLines;
    private Line[] outLines;
    private Line[] intermediateLines;
    public FieldWarpFilter() {
    }

    /**
     * Get the amount of warp.
     *
     * @return the amount
     * @see #setAmount
     */
    public float getAmount() {
        return amount;
    }

    /**
     * Set the amount of warp.
     *
     * @param amount the amount
     * @min-value 0
     * @max-value 1
     * @see #getAmount
     */
    public void setAmount(float amount) {
        this.amount = amount;
    }

    public float getPower() {
        return power;
    }

    public void setPower(float power) {
        this.power = power;
    }

    public float getStrength() {
        return strength;
    }

    public void setStrength(float strength) {
        this.strength = strength;
    }

    public Line[] getInLines() {
        return inLines;
    }

    public void setInLines(Line[] inLines) {
        this.inLines = inLines;
    }

    public Line[] getOutLines() {
        return outLines;
    }

    public void setOutLines(Line[] outLines) {
        this.outLines = outLines;
    }

    protected void transform(int x, int y, Point out) {
    }

    public String toString() {
        return "Distort/Field Warp...";
    }

    public static class Line {
        public int x1, y1, x2, y2;
        public int dx, dy;
        public float length, lengthSquared;

        public Line(int x1, int y1, int x2, int y2) {
            this.x1 = x1;
            this.y1 = y1;
            this.x2 = x2;
            this.y2 = y2;
        }

        public void setup() {
            dx = x2 - x1;
            dy = y2 - y1;
            lengthSquared = dx * dx + dy * dy;
            length = (float) Math.sqrt(lengthSquared);
        }
    }

}
