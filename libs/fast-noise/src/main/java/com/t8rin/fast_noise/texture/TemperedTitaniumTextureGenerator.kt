package com.t8rin.fast_noise.texture

import com.t8rin.fast_noise.texture.internal.TextureType

data class TemperedTitaniumTextureParameters(
    override val seed: Int = 1337,
    val scale: Float = 0.008f,
    val patchDensity: Float = 0.55f,
    val warping: Float = 0.48f,
    val brushing: Float = 0.55f,
    val polish: Float = 0.65f,
    val contrast: Float = 1.05f,
    val backgroundColor: Int = 0xFF10202D.toInt(),
    val primaryColor: Int = 0xFF6C66AD.toInt(),
    val secondaryColor: Int = 0xFFB98839.toInt(),
    val highlightColor: Int = 0xFFD5F5EF.toInt()
) : ProceduralTextureParameters

class TemperedTitaniumTextureGenerator :
    NativeProceduralTextureGenerator<TemperedTitaniumTextureParameters>(TextureType.TEMPERED_TITANIUM) {
    override fun colors(parameters: TemperedTitaniumTextureParameters) = intArrayOf(
        parameters.backgroundColor,
        parameters.primaryColor,
        parameters.secondaryColor,
        parameters.highlightColor
    )

    override fun values(parameters: TemperedTitaniumTextureParameters) = floatArrayOf(
        parameters.scale,
        parameters.patchDensity,
        parameters.warping,
        parameters.brushing,
        parameters.polish,
        parameters.contrast
    )
}
