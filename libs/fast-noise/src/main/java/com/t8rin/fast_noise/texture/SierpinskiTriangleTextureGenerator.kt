package com.t8rin.fast_noise.texture

import com.t8rin.fast_noise.texture.internal.TextureType

data class SierpinskiTriangleTextureParameters(
    override val seed: Int = 1337,
    val scale: Float = 0.008f,
    val depth: Float = 0.68f,
    val rotation: Float = 0.50f,
    val edgeSharpness: Float = 0.82f,
    val glow: Float = 0.66f,
    val contrast: Float = 1.55f,
    val backgroundColor: Int = 0xFF050713.toInt(),
    val primaryColor: Int = 0xFF244B9A.toInt(),
    val secondaryColor: Int = 0xFF46BFD0.toInt(),
    val highlightColor: Int = 0xFFFFE49A.toInt()
) : ProceduralTextureParameters

class SierpinskiTriangleTextureGenerator :
    NativeProceduralTextureGenerator<SierpinskiTriangleTextureParameters>(TextureType.SIERPINSKI_TRIANGLE) {
    override fun colors(parameters: SierpinskiTriangleTextureParameters) = intArrayOf(
        parameters.backgroundColor,
        parameters.primaryColor,
        parameters.secondaryColor,
        parameters.highlightColor
    )

    override fun values(parameters: SierpinskiTriangleTextureParameters) = floatArrayOf(
        parameters.scale,
        parameters.depth,
        parameters.rotation,
        parameters.edgeSharpness,
        parameters.glow,
        parameters.contrast
    )
}
