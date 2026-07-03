package com.t8rin.fast_noise.texture

import com.t8rin.fast_noise.texture.internal.TextureType

data class ConcreteTextureParameters(
    override val seed: Int = 1337,
    val scale: Float = 0.014f,
    val aggregate: Float = 0.45f,
    val stains: Float = 0.28f,
    val roughness: Float = 0.65f,
    val cracks: Float = 0.14f,
    val contrast: Float = 1.08f,
    val darkColor: Int = 0xFF5A5955.toInt(),
    val concreteColor: Int = 0xFF99968E.toInt(),
    val lightColor: Int = 0xFFC7C3B9.toInt(),
    val crackColor: Int = 0xFF353532.toInt()
) : ProceduralTextureParameters

class ConcreteTextureGenerator :
    NativeProceduralTextureGenerator<ConcreteTextureParameters>(TextureType.CONCRETE) {
    override fun colors(parameters: ConcreteTextureParameters) = intArrayOf(
        parameters.darkColor,
        parameters.concreteColor,
        parameters.lightColor,
        parameters.crackColor
    )

    override fun values(parameters: ConcreteTextureParameters) = floatArrayOf(
        parameters.scale,
        parameters.aggregate,
        parameters.stains,
        parameters.roughness,
        parameters.cracks,
        parameters.contrast
    )
}
