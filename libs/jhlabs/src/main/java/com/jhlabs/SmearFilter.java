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

import java.util.Date;
import java.util.Random;

public class SmearFilter extends WholeImageFilter {

    public final static int CROSSES = 0;
    public final static int LINES = 1;
    public final static int CIRCLES = 2;
    public final static int SQUARES = 3;

    private float angle = 0;
    private float density = 0.5f;
    private int distance = 8;
    private int shape = LINES;
    private float mix = 0.5f;

    private final Random randomGenerator;
    private long seed = 567;
    private boolean background = false;

    public SmearFilter() {
        randomGenerator = new Random();
    }

    public int getShape() {
        return shape;
    }

    public void setShape(int shape) {
        this.shape = shape;
    }

    public int getDistance() {
        return distance;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }

    public float getDensity() {
        return density;
    }

    public void setDensity(float density) {
        this.density = density;
    }

    /**
     * Returns the angle of the texture.
     *
     * @return the angle of the texture.
     * @see #setAngle
     */
    public float getAngle() {
        return angle;
    }

    /**
     * Specifies the angle of the texture.
     *
     * @param angle the angle of the texture.
     * @angle
     * @see #getAngle
     */
    public void setAngle(float angle) {
        this.angle = angle;
    }

    public float getMix() {
        return mix;
    }

    public void setMix(float mix) {
        this.mix = mix;
    }

    public boolean getBackground() {
        return background;
    }

    public void setBackground(boolean background) {
        this.background = background;
    }

    public void randomize() {
        seed = new Date().getTime();
    }

    private float random(float low, float high) {
        return low + (high - low) * randomGenerator.nextFloat();
    }

    public String toString() {
        return "Effects/Smear...";
    }

}
