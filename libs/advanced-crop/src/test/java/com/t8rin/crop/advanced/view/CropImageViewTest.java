package com.t8rin.crop.advanced.view;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class CropImageViewTest {

    @Test
    public void scaleAboveLimitDoesNotGrowOrSnapBack() {
        assertEquals(25f, CropImageView.constrainScale(25f, 26f, 20f), 0f);
        assertEquals(24f, CropImageView.constrainScale(25f, 24f, 20f), 0f);
    }

    @Test
    public void scaleBelowLimitIsCappedAtLimit() {
        assertEquals(20f, CropImageView.constrainScale(19f, 21f, 20f), 0f);
        assertEquals(18f, CropImageView.constrainScale(19f, 18f, 20f), 0f);
    }

    @Test
    public void currentZoomIsNotCappedWhenScaleIsAlreadyAboveLimit() {
        assertEquals(25f, CropImageView.calculateCurrentZoom(25f, 1f), 0f);
    }
}
