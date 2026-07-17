package com.t8rin.fast_noise.texture

import com.t8rin.fast_noise.texture.internal.TextureType

data class KelpForestTextureParameters(
    override val seed: Int = 1337,
    val scale: Float = 0.006f,
    val density: Float = 0.64f,
    val current: Float = 0.68f,
    val depthSharpness: Float = 0.62f,
    val lightRays: Float = 0.76f,
    val contrast: Float = 1.35f,
    val backgroundColor: Int = 0xFF031C22.toInt(),
    val primaryColor: Int = 0xFF07594F.toInt(),
    val secondaryColor: Int = 0xFF42A66E.toInt(),
    val highlightColor: Int = 0xFFD1F29B.toInt()
) : ProceduralTextureParameters

class KelpForestTextureGenerator :
    NativeProceduralTextureGenerator<KelpForestTextureParameters>(TextureType.KELP_FOREST) {
    override fun colors(parameters: KelpForestTextureParameters) = intArrayOf(
        parameters.backgroundColor,
        parameters.primaryColor,
        parameters.secondaryColor,
        parameters.highlightColor
    )

    override fun values(parameters: KelpForestTextureParameters) = floatArrayOf(
        parameters.scale,
        parameters.density,
        parameters.current,
        parameters.depthSharpness,
        parameters.lightRays,
        parameters.contrast
    )
}
