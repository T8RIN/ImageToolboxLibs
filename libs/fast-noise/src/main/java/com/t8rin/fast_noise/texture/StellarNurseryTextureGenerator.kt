package com.t8rin.fast_noise.texture

import com.t8rin.fast_noise.texture.internal.TextureType

data class StellarNurseryTextureParameters(
    override val seed: Int = 1337,
    val scale: Float = 0.008f,
    val pillarDensity: Float = 0.55f,
    val turbulence: Float = 0.48f,
    val dust: Float = 0.55f,
    val stars: Float = 0.65f,
    val contrast: Float = 1.05f,
    val backgroundColor: Int = 0xFF081627.toInt(),
    val primaryColor: Int = 0xFF34536A.toInt(),
    val secondaryColor: Int = 0xFFC0836B.toInt(),
    val highlightColor: Int = 0xFFF6DBA9.toInt()
) : ProceduralTextureParameters

class StellarNurseryTextureGenerator :
    NativeProceduralTextureGenerator<StellarNurseryTextureParameters>(TextureType.STELLAR_NURSERY) {
    override fun colors(parameters: StellarNurseryTextureParameters) = intArrayOf(
        parameters.backgroundColor,
        parameters.primaryColor,
        parameters.secondaryColor,
        parameters.highlightColor
    )

    override fun values(parameters: StellarNurseryTextureParameters) = floatArrayOf(
        parameters.scale,
        parameters.pillarDensity,
        parameters.turbulence,
        parameters.dust,
        parameters.stars,
        parameters.contrast
    )
}
