package com.t8rin.fast_noise.texture

import com.t8rin.fast_noise.texture.internal.TextureType

data class RustTextureParameters(
    override val seed: Int = 1337,
    val scale: Float = 0.009f,
    val corrosion: Float = 0.55f,
    val pitting: Float = 0.34f,
    val flakes: Float = 0.42f,
    val distortion: Float = 16f,
    val contrast: Float = 1.25f,
    val metalColor: Int = 0xFF596065.toInt(),
    val darkRustColor: Int = 0xFF3A170B.toInt(),
    val rustColor: Int = 0xFF9B3E13.toInt(),
    val orangeColor: Int = 0xFFE07822.toInt()
) : ProceduralTextureParameters

class RustTextureGenerator :
    NativeProceduralTextureGenerator<RustTextureParameters>(TextureType.RUST) {
    override fun colors(parameters: RustTextureParameters) = intArrayOf(
        parameters.metalColor,
        parameters.darkRustColor,
        parameters.rustColor,
        parameters.orangeColor
    )

    override fun values(parameters: RustTextureParameters) = floatArrayOf(
        parameters.scale,
        parameters.corrosion,
        parameters.pitting,
        parameters.flakes,
        parameters.distortion,
        parameters.contrast
    )
}
