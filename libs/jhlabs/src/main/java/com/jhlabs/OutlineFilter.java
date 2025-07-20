/*
 ** Copyright 2005 Huxtable.com. All rights reserved.
 */

package com.jhlabs;

import android.graphics.Rect;

/**
 * Given a binary image, this filter converts it to its outline, replacing all interior pixels with the 'new' color.
 */
public class OutlineFilter extends BinaryFilter {

    public OutlineFilter() {
        newColor = 0xffffffff;
    }

    protected int[] filterPixels(int width, int height, int[] inPixels, Rect transformedSpace) {
        int index = 0;
        int[] outPixels = new int[width * height];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = inPixels[y * width + x];
                if (blackFunction.isBlack(pixel)) {
                    int neighbours = 0;

                    for (int dy = -1; dy <= 1; dy++) {
                        int iy = y + dy;
                        int ioffset;
                        if (0 <= iy && iy < height) {
                            ioffset = iy * width;
                            for (int dx = -1; dx <= 1; dx++) {
                                int ix = x + dx;
                                if (!(dy == 0 && dx == 0) && 0 <= ix && ix < width) {
                                    int rgb = inPixels[ioffset + ix];
                                    if (blackFunction.isBlack(rgb))
                                        neighbours++;
                                } else
                                    neighbours++;
                            }
                        }
                    }

                    if (neighbours == 9)
                        pixel = newColor;
                }
                outPixels[index++] = pixel;
            }

        }
        return outPixels;
    }

    public String toString() {
        return "Binary/Outline...";
    }

}

