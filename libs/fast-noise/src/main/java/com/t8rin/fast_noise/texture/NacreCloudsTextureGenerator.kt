package com.t8rin.fast_noise.texture

import com.t8rin.fast_noise.texture.internal.TextureType

data class NacreCloudsTextureParameters(
    override val seed: Int = 1337,
    val scale: Float = 0.008f,
    val waveDensity: Float = 0.55f,
    val turbulence: Float = 0.48f,
    val iridescence: Float = 0.55f,
    val light: Float = 0.65f,
    val contrast: Float = 1.05f,
    val backgroundColor: Int = 0xFF263650.toInt(),
    val primaryColor: Int = 0xFF789AB0.toInt(),
    val secondaryColor: Int = 0xFFCA9FBB.toInt(),
    val highlightColor: Int = 0xFFF2D6AC.toInt()
) : ProceduralTextureParameters

class NacreCloudsTextureGenerator :
    NativeProceduralTextureGenerator<NacreCloudsTextureParameters>(TextureType.NACRE_CLOUDS) {
    override fun colors(parameters: NacreCloudsTextureParameters) = intArrayOf(
        parameters.backgroundColor,
        parameters.primaryColor,
        parameters.secondaryColor,
        parameters.highlightColor
    )

    override fun values(parameters: NacreCloudsTextureParameters) = floatArrayOf(
        parameters.scale,
        parameters.waveDensity,
        parameters.turbulence,
        parameters.iridescence,
        parameters.light,
        parameters.contrast
    )
}
