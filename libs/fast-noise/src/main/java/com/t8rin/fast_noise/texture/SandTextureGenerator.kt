package com.t8rin.fast_noise.texture

import com.t8rin.fast_noise.texture.internal.TextureType

data class SandTextureParameters(
    override val seed: Int = 1337,
    val scale: Float = 0.005f,
    val duneFrequency: Float = 12f,
    val windAngle: Float = 0.32f,
    val ripples: Float = 0.62f,
    val grain: Float = 0.22f,
    val contrast: Float = 1.18f,
    val shadowColor: Int = 0xFF9A632E.toInt(),
    val sandColor: Int = 0xFFD6A85A.toInt(),
    val lightColor: Int = 0xFFF2D58D.toInt()
) : ProceduralTextureParameters

class SandTextureGenerator :
    NativeProceduralTextureGenerator<SandTextureParameters>(TextureType.SAND) {
    override fun colors(parameters: SandTextureParameters) = intArrayOf(
        parameters.shadowColor,
        parameters.sandColor,
        parameters.lightColor
    )

    override fun values(parameters: SandTextureParameters) = floatArrayOf(
        parameters.scale,
        parameters.duneFrequency,
        parameters.windAngle,
        parameters.ripples,
        parameters.grain,
        parameters.contrast
    )
}
