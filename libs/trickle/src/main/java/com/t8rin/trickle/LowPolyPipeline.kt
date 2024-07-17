package com.t8rin.trickle

import android.graphics.Bitmap

interface LowPolyPipeline {

    /**
     * generate lowpoly picture
     *
     * @param input             src Bitmap
     * @param alphaOrPointCount (0.0, 1) or [1, max)
     * @param fill              fill ? Paint.Style.FILL : Paint.Style.STROKE
     * @return out Bitmap
     */
    fun lowPoly(input: Bitmap, alphaOrPointCount: Float, fill: Boolean): Bitmap

    /**
     * generate sandpainting
     *
     * @param input             src Bitmap
     * @param threshold         recommend（30, 90）
     * @param alphaOrPointCount (0.0, 1) or [1, max)
     * @return out Bitmap
     */
    fun sandPainting(input: Bitmap, threshold: Int, alphaOrPointCount: Float): Bitmap

}