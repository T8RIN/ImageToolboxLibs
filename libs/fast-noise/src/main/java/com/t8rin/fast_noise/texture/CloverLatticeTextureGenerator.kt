package com.t8rin.fast_noise.texture

import com.t8rin.fast_noise.texture.internal.TextureType

data class CloverLatticeTextureParameters(
    override val seed: Int = 1337,
    val scale: Float = 0.008f,
    val tileDensity: Float = 0.55f,
    val curvature: Float = 0.48f,
    val groutWidth: Float = 0.55f,
    val glaze: Float = 0.65f,
    val contrast: Float = 1.05f,
    val backgroundColor: Int = 0xFFE8DDC9.toInt(),
    val primaryColor: Int = 0xFF386C79.toInt(),
    val secondaryColor: Int = 0xFF9E656A.toInt(),
    val highlightColor: Int = 0xFFE7B96B.toInt()
) : ProceduralTextureParameters

class CloverLatticeTextureGenerator :
    NativeProceduralTextureGenerator<CloverLatticeTextureParameters>(TextureType.CLOVER_LATTICE) {
    override fun colors(parameters: CloverLatticeTextureParameters) = intArrayOf(
        parameters.backgroundColor,
        parameters.primaryColor,
        parameters.secondaryColor,
        parameters.highlightColor
    )

    override fun values(parameters: CloverLatticeTextureParameters) = floatArrayOf(
        parameters.scale,
        parameters.tileDensity,
        parameters.curvature,
        parameters.groutWidth,
        parameters.glaze,
        parameters.contrast
    )
}
