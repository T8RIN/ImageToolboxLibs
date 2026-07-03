package com.t8rin.fast_noise.texture

import com.t8rin.fast_noise.texture.internal.TextureType

data class NautilusShellTextureParameters(
    override val seed: Int = 1337,
    val scale: Float = 0.011f,
    val turns: Float = 3.4f,
    val chambers: Float = 19f,
    val opening: Float = 0.13f,
    val ridges: Float = 0.68f,
    val pearlescence: Float = 0.46f,
    val backgroundColor: Int = 0xFF152228.toInt(),
    val shadowColor: Int = 0xFF4A2D22.toInt(),
    val shellColor: Int = 0xFFD9AD79.toInt(),
    val pearlColor: Int = 0xFFFFF0D1.toInt()
) : ProceduralTextureParameters

class NautilusShellTextureGenerator :
    NativeProceduralTextureGenerator<NautilusShellTextureParameters>(TextureType.NAUTILUS_SHELL) {
    override fun colors(parameters: NautilusShellTextureParameters) = intArrayOf(
        parameters.backgroundColor,
        parameters.shadowColor,
        parameters.shellColor,
        parameters.pearlColor
    )

    override fun values(parameters: NautilusShellTextureParameters) = floatArrayOf(
        parameters.scale,
        parameters.turns,
        parameters.chambers,
        parameters.opening,
        parameters.ridges,
        parameters.pearlescence
    )
}
