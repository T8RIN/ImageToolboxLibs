package com.t8rin.fast_noise.texture

import com.t8rin.fast_noise.texture.internal.TextureType

data class BismuthCrystalTextureParameters(
    override val seed: Int = 1337,
    val scale: Float = 0.011f,
    val angle: Float = 0.5f,
    val irregularity: Float = 0.55f,
    val steps: Float = 0.6f,
    val iridescence: Float = 0.75f,
    val relief: Float = 0.65f,
    val backgroundColor: Int = 0xFF211329.toInt(),
    val primaryColor: Int = 0xFF5B54B7.toInt(),
    val secondaryColor: Int = 0xFF23B8B8.toInt(),
    val highlightColor: Int = 0xFFFFCD70.toInt()
) : ProceduralTextureParameters

class BismuthCrystalTextureGenerator :
    NativeProceduralTextureGenerator<BismuthCrystalTextureParameters>(TextureType.BISMUTH_CRYSTAL) {
    override fun colors(parameters: BismuthCrystalTextureParameters) = intArrayOf(
        parameters.backgroundColor,
        parameters.primaryColor,
        parameters.secondaryColor,
        parameters.highlightColor
    )

    override fun values(parameters: BismuthCrystalTextureParameters) = floatArrayOf(
        parameters.scale,
        parameters.angle,
        parameters.irregularity,
        parameters.steps,
        parameters.iridescence,
        parameters.relief
    )
}
