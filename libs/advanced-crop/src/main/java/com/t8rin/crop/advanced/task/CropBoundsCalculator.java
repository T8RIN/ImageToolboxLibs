package com.t8rin.crop.advanced.task;

final class CropBoundsCalculator {

    private CropBoundsCalculator() {
    }

    static float[] calculateScales(int sourceWidth,
                                   int sourceHeight,
                                   float imageWidth,
                                   float imageHeight,
                                   float cropWidth,
                                   float cropHeight,
                                   boolean fixedAspectRatio) {
        // Use the displayed bounds: both preview pixels and view coordinates are rounded.
        float scaleX = imageWidth / sourceWidth;
        float scaleY = imageHeight / sourceHeight;
        if (fixedAspectRatio) {
            float scale = cropWidth / imageWidth >= cropHeight / imageHeight ? scaleX : scaleY;
            return new float[]{scale, scale};
        }
        return new float[]{scaleX, scaleY};
    }

    static int[] calculate(float cropLeft,
                           float cropTop,
                           float cropWidth,
                           float cropHeight,
                           float imageLeft,
                           float imageTop,
                           float currentScaleX,
                           float currentScaleY) {
        int left = Math.round((cropLeft - imageLeft) / currentScaleX);
        int top = Math.round((cropTop - imageTop) / currentScaleY);
        int width = Math.max(1, Math.round(cropWidth / currentScaleX));
        int height = Math.max(1, Math.round(cropHeight / currentScaleY));

        return new int[]{left, top, width, height};
    }
}
