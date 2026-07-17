package com.t8rin.fast_noise.texture

import com.t8rin.fast_noise.texture.internal.TextureType

data class ChladniPlateTextureParameters(
    override val seed: Int = 1337,
    val scale: Float = 0.008f,
    val modeCount: Float = 0.62f,
    val asymmetry: Float = 0.38f,
    val lineSharpness: Float = 0.82f,
    val metallic: Float = 0.54f,
    val contrast: Float = 1.55f,
    val backgroundColor: Int = 0xFF06080B.toInt(),
    val primaryColor: Int = 0xFF2E343A.toInt(),
    val secondaryColor: Int = 0xFF8E9AA3.toInt(),
    val highlightColor: Int = 0xFFF1D78A.toInt()
) : ProceduralTextureParameters

class ChladniPlateTextureGenerator :
    NativeProceduralTextureGenerator<ChladniPlateTextureParameters>(TextureType.CHLADNI_PLATE) {
    override fun colors(parameters: ChladniPlateTextureParameters) = intArrayOf(
        parameters.backgroundColor,
        parameters.primaryColor,
        parameters.secondaryColor,
        parameters.highlightColor
    )

    override fun values(parameters: ChladniPlateTextureParameters) = floatArrayOf(
        parameters.scale,
        parameters.modeCount,
        parameters.asymmetry,
        parameters.lineSharpness,
        parameters.metallic,
        parameters.contrast
    )
}
