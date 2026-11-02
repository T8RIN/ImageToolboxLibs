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

    @Test
    public void longImageFrameKeepsFractionalWidth() {
        float aspectRatio = 73f / 2048f;
        float[] bounds = CropImageView.calculateCropBounds(1200, 1947, aspectRatio);

        assertEquals(1947f * aspectRatio, bounds[2] - bounds[0], 0.0001f);
        assertEquals(1947f, bounds[3] - bounds[1], 0.0001f);
    }

    @Test
    public void wideImageFrameKeepsFractionalHeight() {
        float aspectRatio = 2048f / 73f;
        float[] bounds = CropImageView.calculateCropBounds(1200, 1947, aspectRatio);

        assertEquals(1200f, bounds[2] - bounds[0], 0.0001f);
        assertEquals(1200f / aspectRatio, bounds[3] - bounds[1], 0.0001f);
    }
}
