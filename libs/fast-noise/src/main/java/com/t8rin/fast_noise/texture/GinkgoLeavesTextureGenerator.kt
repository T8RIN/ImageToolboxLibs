package com.t8rin.fast_noise.texture

import com.t8rin.fast_noise.texture.internal.TextureType

data class GinkgoLeavesTextureParameters(
    override val seed: Int = 1337,
    val scale: Float = 0.008f,
    val leafDensity: Float = 0.55f,
    val fanWidth: Float = 0.48f,
    val veins: Float = 0.55f,
    val light: Float = 0.65f,
    val contrast: Float = 1.05f,
    val backgroundColor: Int = 0xFF17292D.toInt(),
    val primaryColor: Int = 0xFF6B7350.toInt(),
    val secondaryColor: Int = 0xFFCAA44D.toInt(),
    val highlightColor: Int = 0xFFF4DD91.toInt()
) : ProceduralTextureParameters

class GinkgoLeavesTextureGenerator :
    NativeProceduralTextureGenerator<GinkgoLeavesTextureParameters>(TextureType.GINKGO_LEAVES) {
    override fun colors(parameters: GinkgoLeavesTextureParameters) = intArrayOf(
        parameters.backgroundColor,
        parameters.primaryColor,
        parameters.secondaryColor,
        parameters.highlightColor
    )

    override fun values(parameters: GinkgoLeavesTextureParameters) = floatArrayOf(
        parameters.scale,
        parameters.leafDensity,
        parameters.fanWidth,
        parameters.veins,
        parameters.light,
        parameters.contrast
    )
}
