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
 * A filter which allows the red, green and blue channels of an image to be mixed into each other.
 */
public class ChannelMixFilter extends PointFilter {

    private int blueGreen, redBlue, greenRed;
    private int intoR, intoG, intoB;

    public ChannelMixFilter() {
        canFilterIndexColorModel = true;
    }

    public int getBlueGreen() {
        return blueGreen;
    }

    public void setBlueGreen(int blueGreen) {
        this.blueGreen = blueGreen;
    }

    public int getRedBlue() {
        return redBlue;
    }

    public void setRedBlue(int redBlue) {
        this.redBlue = redBlue;
    }

    public int getGreenRed() {
        return greenRed;
    }

    public void setGreenRed(int greenRed) {
        this.greenRed = greenRed;
    }

    public int getIntoR() {
        return intoR;
    }

    public void setIntoR(int intoR) {
        this.intoR = intoR;
    }

    public int getIntoG() {
        return intoG;
    }

    public void setIntoG(int intoG) {
        this.intoG = intoG;
    }

    public int getIntoB() {
        return intoB;
    }

    public void setIntoB(int intoB) {
        this.intoB = intoB;
    }

    public String toString() {
        return "Colors/Mix Channels...";
    }
}
