package com.t8rin.fast_noise.texture

import com.t8rin.fast_noise.texture.internal.TextureType

data class ChiralPinwheelsTextureParameters(
    override val seed: Int = 1337,
    val scale: Float = 0.008f,
    val tileDensity: Float = 0.55f,
    val chirality: Float = 0.48f,
    val curvature: Float = 0.55f,
    val relief: Float = 0.65f,
    val contrast: Float = 1.05f,
    val backgroundColor: Int = 0xFF132F3D.toInt(),
    val primaryColor: Int = 0xFF519399.toInt(),
    val secondaryColor: Int = 0xFFC88370.toInt(),
    val highlightColor: Int = 0xFFF2D49E.toInt()
) : ProceduralTextureParameters

class ChiralPinwheelsTextureGenerator :
    NativeProceduralTextureGenerator<ChiralPinwheelsTextureParameters>(TextureType.CHIRAL_PINWHEELS) {
    override fun colors(parameters: ChiralPinwheelsTextureParameters) = intArrayOf(
        parameters.backgroundColor,
        parameters.primaryColor,
        parameters.secondaryColor,
        parameters.highlightColor
    )

    override fun values(parameters: ChiralPinwheelsTextureParameters) = floatArrayOf(
        parameters.scale,
        parameters.tileDensity,
        parameters.chirality,
        parameters.curvature,
        parameters.relief,
        parameters.contrast
    )
}
