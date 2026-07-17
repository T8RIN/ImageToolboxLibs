package com.t8rin.fast_noise.texture

import com.t8rin.fast_noise.texture.internal.TextureType

data class DendriticCrystalTextureParameters(
    override val seed: Int = 1337,
    val scale: Float = 0.008f,
    val branchCount: Float = 0.68f,
    val branching: Float = 0.52f,
    val crystalSharpness: Float = 0.80f,
    val glow: Float = 0.82f,
    val contrast: Float = 1.50f,
    val backgroundColor: Int = 0xFF071829.toInt(),
    val primaryColor: Int = 0xFF25678C.toInt(),
    val secondaryColor: Int = 0xFFA8E5F0.toInt(),
    val highlightColor: Int = 0xFFFFFFFF.toInt()
) : ProceduralTextureParameters

class DendriticCrystalTextureGenerator :
    NativeProceduralTextureGenerator<DendriticCrystalTextureParameters>(TextureType.DENDRITIC_CRYSTAL) {
    override fun colors(parameters: DendriticCrystalTextureParameters) = intArrayOf(
        parameters.backgroundColor,
        parameters.primaryColor,
        parameters.secondaryColor,
        parameters.highlightColor
    )

    override fun values(parameters: DendriticCrystalTextureParameters) = floatArrayOf(
        parameters.scale,
        parameters.branchCount,
        parameters.branching,
        parameters.crystalSharpness,
        parameters.glow,
        parameters.contrast
    )
}
