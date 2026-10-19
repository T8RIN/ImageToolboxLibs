package com.t8rin.crop.advanced.task;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class CropBoundsCalculatorTest {

    @Test
    public void squareCropKeepsRoundedSizeOnBothAxes() {
        int[] result = CropBoundsCalculator.calculate(
                420.3f,
                0.4f,
                1079.8f,
                1079.8f,
                0f,
                0f,
                0.75f,
                0.75f
        );

        assertEquals(1440, result[2]);
        assertEquals(1440, result[3]);
    }

    @Test
    public void verticalCropKeepsFullSourceWidthWithRoundedPreview() {
        float[] scales = CropBoundsCalculator.calculateScales(
                1080, 30000, 73f, 2048f, 73f, 512f, false);
        int[] result = CropBoundsCalculator.calculate(
                10f, 120f, 73f, 512f,
                10f, 20f,
                scales[0], scales[1]
        );

        assertEquals(0, result[0]);
        assertEquals(1465, result[1]);
        assertEquals(1080, result[2]);
        assertEquals(7500, result[3]);
    }

    @Test
    public void horizontalCropKeepsFullSourceHeightWithRoundedPreview() {
        float[] scales = CropBoundsCalculator.calculateScales(
                30000, 1080, 2048f, 73f, 512f, 73f, false);
        int[] result = CropBoundsCalculator.calculate(
                120f, 10f, 512f, 73f,
                20f, 10f,
                scales[0], scales[1]
        );

        assertEquals(1465, result[0]);
        assertEquals(0, result[1]);
        assertEquals(7500, result[2]);
        assertEquals(1080, result[3]);
    }

    @Test
    public void fixedSquareCropKeepsFullWidthAndAspectRatio() {
        float[] scales = CropBoundsCalculator.calculateScales(
                1080, 30000, 73f, 2048f, 73f, 73f, true);
        int[] result = CropBoundsCalculator.calculate(
                0f, 256f, 73f, 73f, 0f, 0f, scales[0], scales[1]);

        assertEquals(1080, result[2]);
        assertEquals(1080, result[3]);
    }

    @Test
    public void verticalCropKeepsFullWidthWithRoundedViewBounds() {
        float[] scales = CropBoundsCalculator.calculateScales(
                1080, 30000, 53.3f, 1495f, 53.3f, 500f, false);
        int[] result = CropBoundsCalculator.calculate(
                0f, 100f, 53.3f, 500f, 0f, 0f, scales[0], scales[1]);

        assertEquals(1080, result[2]);
    }
}
