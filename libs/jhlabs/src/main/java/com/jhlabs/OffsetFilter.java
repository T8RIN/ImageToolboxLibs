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

public class OffsetFilter extends TransformFilter {

    private int width, height;
    private int xOffset, yOffset;
    private boolean wrap;

    public OffsetFilter() {
        this(0, 0, true);
    }

    public OffsetFilter(int xOffset, int yOffset, boolean wrap) {
        this.xOffset = xOffset;
        this.yOffset = yOffset;
        this.wrap = wrap;
        setEdgeAction(ZERO);
    }

    public int getXOffset() {
        return xOffset;
    }

    public void setXOffset(int xOffset) {
        this.xOffset = xOffset;
    }

    public int getYOffset() {
        return yOffset;
    }

    public void setYOffset(int yOffset) {
        this.yOffset = yOffset;
    }

    public boolean getWrap() {
        return wrap;
    }

    public void setWrap(boolean wrap) {
        this.wrap = wrap;
    }

    public String toString() {
        return "Distort/Offset...";
    }
}
