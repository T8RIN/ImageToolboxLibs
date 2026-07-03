package com.t8rin.fast_noise.texture

import com.t8rin.fast_noise.texture.internal.TextureType

data class FabricTextureParameters(
    override val seed: Int = 1337,
    val scale: Float = 0.018f,
    val horizontalThreads: Float = 28f,
    val verticalThreads: Float = 28f,
    val irregularity: Float = 0.14f,
    val depth: Float = 0.48f,
    val fuzz: Float = 0.1f,
    val warpColor: Int = 0xFF496B82.toInt(),
    val weftColor: Int = 0xFF7895A7.toInt(),
    val shadowColor: Int = 0xFF263C4B.toInt(),
    val highlightColor: Int = 0xFFB6CAD4.toInt()
) : ProceduralTextureParameters

class FabricTextureGenerator :
    NativeProceduralTextureGenerator<FabricTextureParameters>(TextureType.FABRIC) {
    override fun colors(parameters: FabricTextureParameters) = intArrayOf(
        parameters.warpColor,
        parameters.weftColor,
        parameters.shadowColor,
        parameters.highlightColor
    )

    override fun values(parameters: FabricTextureParameters) = floatArrayOf(
        parameters.scale,
        parameters.horizontalThreads,
        parameters.verticalThreads,
        parameters.irregularity,
        parameters.depth,
        parameters.fuzz
    )
}
