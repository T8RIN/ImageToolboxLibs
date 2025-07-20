/*
 ** Copyright 2005 Huxtable.com. All rights reserved.
 */

package com.jhlabs;

public class FadeFilter extends PointFilter {

    private int width, height;
    private float angle = 0.0f;
    private float fadeStart = 1.0f;
    private float fadeWidth = 10.0f;
    private int sides;
    private boolean invert;
    private float m00 = 1.0f;
    private float m01 = 0.0f;
    private float m10 = 0.0f;
    private float m11 = 1.0f;

    public float getAngle() {
        return angle;
    }

    public void setAngle(float angle) {
        this.angle = angle;
        float cos = (float) Math.cos(angle);
        float sin = (float) Math.sin(angle);
        m00 = cos;
        m01 = sin;
        m10 = -sin;
        m11 = cos;
    }

    public int getSides() {
        return sides;
    }

    public void setSides(int sides) {
        this.sides = sides;
    }

    public float getFadeStart() {
        return fadeStart;
    }

    public void setFadeStart(float fadeStart) {
        this.fadeStart = fadeStart;
    }

    public float getFadeWidth() {
        return fadeWidth;
    }

    public void setFadeWidth(float fadeWidth) {
        this.fadeWidth = fadeWidth;
    }

    public boolean getInvert() {
        return invert;
    }

    public void setInvert(boolean invert) {
        this.invert = invert;
    }

    public void setDimensions(int width, int height) {
        this.width = width;
        this.height = height;
        super.setDimensions(width, height);
    }

    public int filterRGB(int x, int y, int rgb) {
        float nx = m00 * x + m01 * y;
        float ny = m10 * x + m11 * y;
        if (sides == 2)
            nx = (float) Math.sqrt(nx * nx + ny * ny);
        else if (sides == 3)
            nx = ImageMath.mod(nx, 16);
        else if (sides == 4)
            nx = symmetry(nx, 16);
        int alpha = (int) (ImageMath.smoothStep(fadeStart, fadeStart + fadeWidth, nx) * 255);
        if (invert)
            alpha = 255 - alpha;
        return (alpha << 24) | (rgb & 0x00ffffff);
    }

    public float symmetry(float x, float b) {
/*
		int d = (int)(x / b);
		x = ImageMath.mod(x, b);
		if ((d & 1) == 1)
			return b-x;
		return x;
*/
        x = ImageMath.mod(x, 2 * b);
        if (x > b)
            return 2 * b - x;
        return x;
    }
	
/*
	public float star(float x, float y, int sides, float rMin, float rMax) {
		float sideAngle = 2*Math.PI / sides;
		float angle = Math.atan2(y, x);
		float r = Math.sqrt(x*x + y*y);
		float t = ImageMath.mod(angle, sideAngle) / sideAngle;
		if (t > 0.5)
			t = 1.0-t;
	}
*/

    public String toString() {
        return "Fade...";
    }

}

