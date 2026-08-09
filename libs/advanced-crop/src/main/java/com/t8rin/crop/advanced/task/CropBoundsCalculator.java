package com.t8rin.crop.advanced.task;

final class CropBoundsCalculator {

    private CropBoundsCalculator() {
    }

    static int[] calculate(float cropLeft,
                           float cropTop,
                           float cropWidth,
                           float cropHeight,
                           float imageLeft,
                           float imageTop,
                           float currentScale) {
        int left = Math.round((cropLeft - imageLeft) / currentScale);
        int top = Math.round((cropTop - imageTop) / currentScale);
        int width = Math.max(1, Math.round(cropWidth / currentScale));
        int height = Math.max(1, Math.round(cropHeight / currentScale));

        return new int[]{left, top, width, height};
    }
}
