package com.t8rin.trickle.pipeline

import android.graphics.Bitmap
import com.t8rin.trickle.EffectsPipeline

internal object EffectsPipelineImpl : EffectsPipeline {

    override fun oil(
        input: Bitmap,
        oilRange: Int
    ): Bitmap = oilImpl(
        input = input,
        oilRange = oilRange
    ) ?: input

    override fun tv(input: Bitmap): Bitmap = tvImpl(input) ?: input

    override fun hdr(input: Bitmap): Bitmap = hdrImpl(input) ?: input

    override fun softGlow(
        input: Bitmap,
        blurSigma: Double
    ): Bitmap = softGlowImpl(
        input = input,
        blurSigma = blurSigma
    ) ?: input

    override fun sketch(input: Bitmap): Bitmap = sketchImpl(input) ?: input

    override fun gotham(input: Bitmap): Bitmap = gothamImpl(input) ?: input

    override fun cropToContent(
        input: Bitmap,
        colorToIgnore: Int,
        tolerance: Float
    ): Bitmap = cropToContentImpl(
        input = input,
        colorToIgnore = colorToIgnore,
        tolerance = tolerance
    ) ?: input

    override fun transferPalette(
        source: Bitmap,
        target: Bitmap,
        intensity: Float
    ): Bitmap = transferPaletteImpl(
        source = source,
        target = target,
        intensity = intensity
    ) ?: source

    override fun noise(
        input: Bitmap,
        threshold: Int
    ): Bitmap {
        val result = input.copy(input.config, true)
        noiseImpl(
            srcBitmap = result,
            threshold = threshold
        )
        return result
    }

    override fun shuffleBlur(
        input: Bitmap,
        threshold: Float,
        strength: Float
    ): Bitmap {
        val result = input.copy(input.config, true)
        shuffleBlurImpl(
            srcBitmap = result,
            threshold = threshold,
            strength = strength
        )
        return result
    }


    private external fun oilImpl(input: Bitmap, oilRange: Int): Bitmap?

    private external fun tvImpl(input: Bitmap): Bitmap?

    private external fun hdrImpl(input: Bitmap): Bitmap?

    private external fun softGlowImpl(input: Bitmap, blurSigma: Double): Bitmap?

    private external fun sketchImpl(input: Bitmap): Bitmap?

    private external fun gothamImpl(input: Bitmap): Bitmap?

    private external fun cropToContentImpl(
        input: Bitmap,
        colorToIgnore: Int,
        tolerance: Float
    ): Bitmap?

    private external fun transferPaletteImpl(
        source: Bitmap,
        target: Bitmap,
        intensity: Float
    ): Bitmap?

    private external fun noiseImpl(srcBitmap: Bitmap, threshold: Int)

    private external fun shuffleBlurImpl(srcBitmap: Bitmap, threshold: Float, strength: Float)

}