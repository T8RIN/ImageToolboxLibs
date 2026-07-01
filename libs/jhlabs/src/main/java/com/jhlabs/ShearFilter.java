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

import android.graphics.Rect;

public class ShearFilter extends TransformFilter {

    private float xangle = 0.0f;
    private float yangle = 0.0f;
    private float shx = 0.0f;
    private float shy = 0.0f;
    private float xoffset = 0.0f;
    private float yoffset = 0.0f;
    private boolean resize = true;

    public ShearFilter() {
    }

    public boolean isResize() {
        return resize;
    }

    public void setResize(boolean resize) {
        this.resize = resize;
    }

    public float getXAngle() {
        return xangle;
    }

    public void setXAngle(float xangle) {
        this.xangle = xangle;
        initialize();
    }

    public float getYAngle() {
        return yangle;
    }

    public void setYAngle(float yangle) {
        this.yangle = yangle;
        initialize();
    }

    private void initialize() {
        shx = (float) Math.sin(xangle);
        shy = (float) Math.sin(yangle);
    }

    protected void transformSpace(Rect r) {
        float tangent = (float) Math.tan(xangle);
        xoffset = -r.bottom * tangent;
        if (tangent < 0.0)
            tangent = -tangent;
        r.right = (int) (r.bottom * tangent + r.right + 0.999999f);
        tangent = (float) Math.tan(yangle);
        yoffset = -r.right * tangent;
        if (tangent < 0.0)
            tangent = -tangent;
        r.bottom = (int) (r.right * tangent + r.bottom + 0.999999f);
    }

    public String toString() {
        return "Distort/Shear...";
    }

}
