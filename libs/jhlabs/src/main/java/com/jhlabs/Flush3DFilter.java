/*
 ** Copyright 2005 Huxtable.com. All rights reserved.
 */

package com.jhlabs;

import android.graphics.Rect;

/**
 * This filter tries to apply the Swing "flush 3D" effect to the black lines in an image.
 */
public class Flush3DFilter extends WholeImageFilter {

    public Flush3DFilter() {
    }

    protected int[] filterPixels(int width, int height, int[] inPixels, Rect transformedSpace) {
        int index = 0;
        int[] outPixels = new int[width * height];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = inPixels[y * width + x];

                if (pixel != 0xff000000 && y > 0 && x > 0) {
                    int count = 0;
                    if (inPixels[y * width + x - 1] == 0xff000000)
                        count++;
                    if (inPixels[(y - 1) * width + x] == 0xff000000)
                        count++;
                    if (inPixels[(y - 1) * width + x - 1] == 0xff000000)
                        count++;
                    if (count >= 2)
                        pixel = 0xffffffff;
                }
                outPixels[index++] = pixel;
            }

        }
        return outPixels;
    }

    public String toString() {
        return "Stylize/Flush 3D...";
    }

}

