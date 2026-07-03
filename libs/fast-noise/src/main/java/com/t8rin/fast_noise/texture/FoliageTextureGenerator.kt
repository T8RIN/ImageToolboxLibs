package com.t8rin.fast_noise.texture

import com.t8rin.fast_noise.texture.internal.TextureType

data class FoliageTextureParameters(
    override val seed: Int = 1337,
    val scale: Float = 0.022f,
    val density: Float = 0.72f,
    val edgeSoftness: Float = 0.16f,
    val veins: Float = 0.38f,
    val lighting: Float = 0.62f,
    val variation: Float = 0.58f,
    val shadowColor: Int = 0xFF102A13.toInt(),
    val darkLeafColor: Int = 0xFF245C25.toInt(),
    val leafColor: Int = 0xFF4E9A3D.toInt(),
    val highlightColor: Int = 0xFFA4CE65.toInt()
) : ProceduralTextureParameters

class FoliageTextureGenerator :
    NativeProceduralTextureGenerator<FoliageTextureParameters>(TextureType.FOLIAGE) {
    override fun colors(parameters: FoliageTextureParameters) = intArrayOf(
        parameters.shadowColor,
        parameters.darkLeafColor,
        parameters.leafColor,
        parameters.highlightColor
    )

    override fun values(parameters: FoliageTextureParameters) = floatArrayOf(
        parameters.scale,
        parameters.density,
        parameters.edgeSoftness,
        parameters.veins,
        parameters.lighting,
        parameters.variation
    )
}
