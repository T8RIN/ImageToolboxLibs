package com.t8rin.fast_noise.texture

import com.t8rin.fast_noise.texture.internal.TextureType

data class KnittedFabricTextureParameters(
    override val seed: Int = 1337,
    val scale: Float = 0.014f,
    val loopWidth: Float = 0.65f,
    val twist: Float = 0.65f,
    val fuzz: Float = 0.4f,
    val angle: Float = 0.5f,
    val variation: Float = 0.4f,
    val backgroundColor: Int = 0xFF21151D.toInt(),
    val primaryColor: Int = 0xFFA04E67.toInt(),
    val secondaryColor: Int = 0xFFDC8B8A.toInt(),
    val highlightColor: Int = 0xFFFFE8D2.toInt()
) : ProceduralTextureParameters

class KnittedFabricTextureGenerator :
    NativeProceduralTextureGenerator<KnittedFabricTextureParameters>(TextureType.KNITTED_FABRIC) {
    override fun colors(parameters: KnittedFabricTextureParameters) = intArrayOf(
        parameters.backgroundColor,
        parameters.primaryColor,
        parameters.secondaryColor,
        parameters.highlightColor
    )

    override fun values(parameters: KnittedFabricTextureParameters) = floatArrayOf(
        parameters.scale,
        parameters.loopWidth,
        parameters.twist,
        parameters.fuzz,
        parameters.angle,
        parameters.variation
    )
}
