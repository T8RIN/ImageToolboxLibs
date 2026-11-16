package com.t8rin.fast_noise.texture

import com.t8rin.fast_noise.texture.internal.TextureType

data class HerringboneParquetTextureParameters(
    override val seed: Int = 1337,
    val scale: Float = 0.014f,
    val plankLength: Float = 0.4f,
    val jointWidth: Float = 0.3f,
    val grain: Float = 0.6f,
    val angle: Float = 0.5f,
    val polish: Float = 0.55f,
    val backgroundColor: Int = 0xFF21160F.toInt(),
    val primaryColor: Int = 0xFF794020.toInt(),
    val secondaryColor: Int = 0xFFC58A45.toInt(),
    val highlightColor: Int = 0xFFF5DC9C.toInt()
) : ProceduralTextureParameters

class HerringboneParquetTextureGenerator :
    NativeProceduralTextureGenerator<HerringboneParquetTextureParameters>(TextureType.HERRINGBONE_PARQUET) {
    override fun colors(parameters: HerringboneParquetTextureParameters) = intArrayOf(
        parameters.backgroundColor,
        parameters.primaryColor,
        parameters.secondaryColor,
        parameters.highlightColor
    )

    override fun values(parameters: HerringboneParquetTextureParameters) = floatArrayOf(
        parameters.scale,
        parameters.plankLength,
        parameters.jointWidth,
        parameters.grain,
        parameters.angle,
        parameters.polish
    )
}
