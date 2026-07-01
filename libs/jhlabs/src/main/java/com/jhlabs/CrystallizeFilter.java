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
 * A filter which applies a crystallizing effect to an image, by producing Voronoi cells filled with colours from the image.
 */
public class CrystallizeFilter extends CellularFilter {

    private float edgeThickness = 0.4f;
    private boolean fadeEdges = false;
    private int edgeColor = 0xff000000;

    public CrystallizeFilter() {
        setScale(16);
        setRandomness(0.0f);
    }

    public float getEdgeThickness() {
        return edgeThickness;
    }

    public void setEdgeThickness(float edgeThickness) {
        this.edgeThickness = edgeThickness;
    }

    public boolean getFadeEdges() {
        return fadeEdges;
    }

    public void setFadeEdges(boolean fadeEdges) {
        this.fadeEdges = fadeEdges;
    }

    public int getEdgeColor() {
        return edgeColor;
    }

    public void setEdgeColor(int edgeColor) {
        this.edgeColor = edgeColor;
    }

    public String toString() {
        return "Pixellate/Crystallize...";
    }

}
