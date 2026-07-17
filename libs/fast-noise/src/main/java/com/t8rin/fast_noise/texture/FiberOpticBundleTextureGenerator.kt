package com.t8rin.fast_noise.texture

import com.t8rin.fast_noise.texture.internal.TextureType

data class FiberOpticBundleTextureParameters(
    override val seed: Int = 1337,
    val scale: Float = 0.008f,
    val fiberDensity: Float = 0.72f,
    val bend: Float = 0.62f,
    val coreSharpness: Float = 0.82f,
    val glow: Float = 0.88f,
    val contrast: Float = 1.55f,
    val backgroundColor: Int = 0xFF02040D.toInt(),
    val primaryColor: Int = 0xFF183C88.toInt(),
    val secondaryColor: Int = 0xFF36D7E1.toInt(),
    val highlightColor: Int = 0xFFFFFFFF.toInt()
) : ProceduralTextureParameters

class FiberOpticBundleTextureGenerator :
    NativeProceduralTextureGenerator<FiberOpticBundleTextureParameters>(TextureType.FIBER_OPTIC_BUNDLE) {
    override fun colors(parameters: FiberOpticBundleTextureParameters) = intArrayOf(
        parameters.backgroundColor,
        parameters.primaryColor,
        parameters.secondaryColor,
        parameters.highlightColor
    )

    override fun values(parameters: FiberOpticBundleTextureParameters) = floatArrayOf(
        parameters.scale,
        parameters.fiberDensity,
        parameters.bend,
        parameters.coreSharpness,
        parameters.glow,
        parameters.contrast
    )
}
