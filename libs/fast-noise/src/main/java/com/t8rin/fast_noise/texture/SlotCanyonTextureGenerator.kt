package com.t8rin.fast_noise.texture

import com.t8rin.fast_noise.texture.internal.TextureType

data class SlotCanyonTextureParameters(
    override val seed: Int = 1337,
    val scale: Float = 0.008f,
    val strata: Float = 0.55f,
    val meander: Float = 0.48f,
    val relief: Float = 0.55f,
    val light: Float = 0.65f,
    val contrast: Float = 1.05f,
    val backgroundColor: Int = 0xFF261B2A.toInt(),
    val primaryColor: Int = 0xFF864332.toInt(),
    val secondaryColor: Int = 0xFFCE8B5A.toInt(),
    val highlightColor: Int = 0xFFF0D5A1.toInt()
) : ProceduralTextureParameters

class SlotCanyonTextureGenerator :
    NativeProceduralTextureGenerator<SlotCanyonTextureParameters>(TextureType.SLOT_CANYON) {
    override fun colors(parameters: SlotCanyonTextureParameters) = intArrayOf(
        parameters.backgroundColor,
        parameters.primaryColor,
        parameters.secondaryColor,
        parameters.highlightColor
    )

    override fun values(parameters: SlotCanyonTextureParameters) = floatArrayOf(
        parameters.scale,
        parameters.strata,
        parameters.meander,
        parameters.relief,
        parameters.light,
        parameters.contrast
    )
}
