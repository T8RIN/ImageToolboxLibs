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
                0.75f
        );

        assertEquals(1440, result[2]);
        assertEquals(1440, result[3]);
    }
}
