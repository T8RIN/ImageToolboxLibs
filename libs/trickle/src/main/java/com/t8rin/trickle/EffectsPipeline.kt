package com.t8rin.trickle

import android.graphics.Bitmap

interface EffectsPipeline {

    fun oil(input: Bitmap, oilRange: Int): Bitmap

    fun tv(input: Bitmap): Bitmap

    fun hdr(input: Bitmap): Bitmap

    fun sketch(input: Bitmap): Bitmap

    fun gotham(input: Bitmap): Bitmap

    fun cropToContent(
        input: Bitmap,
        colorToIgnore: Int,
        tolerance: Float
    ): Bitmap

    fun transferPalette(
        source: Bitmap,
        target: Bitmap,
        intensity: Float
    ): Bitmap

    fun noise(
        input: Bitmap,
        threshold: Int
    ): Bitmap

    fun shuffleBlur(
        input: Bitmap,
        threshold: Float,
        strength: Float
    ): Bitmap

}