package com.t8rin.fast_noise.texture

import com.t8rin.fast_noise.texture.internal.TextureType

data class InkWashMountainsTextureParameters(
    override val seed: Int = 1337,
    val scale: Float = 0.006f,
    val ridgeCount: Float = 0.62f,
    val mist: Float = 0.58f,
    val ridgeSharpness: Float = 0.72f,
    val paperGlow: Float = 0.42f,
    val contrast: Float = 1.40f,
    val backgroundColor: Int = 0xFFF1EBDD.toInt(),
    val primaryColor: Int = 0xFF9A9A91.toInt(),
    val secondaryColor: Int = 0xFF444844.toInt(),
    val highlightColor: Int = 0xFF111716.toInt()
) : ProceduralTextureParameters

class InkWashMountainsTextureGenerator :
    NativeProceduralTextureGenerator<InkWashMountainsTextureParameters>(TextureType.INK_WASH_MOUNTAINS) {
    override fun colors(parameters: InkWashMountainsTextureParameters) = intArrayOf(
        parameters.backgroundColor,
        parameters.primaryColor,
        parameters.secondaryColor,
        parameters.highlightColor
    )

    override fun values(parameters: InkWashMountainsTextureParameters) = floatArrayOf(
        parameters.scale,
        parameters.ridgeCount,
        parameters.mist,
        parameters.ridgeSharpness,
        parameters.paperGlow,
        parameters.contrast
    )
}
