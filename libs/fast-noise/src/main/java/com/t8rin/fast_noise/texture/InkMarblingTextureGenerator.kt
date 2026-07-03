package com.t8rin.fast_noise.texture

import com.t8rin.fast_noise.texture.internal.TextureType

data class InkMarblingTextureParameters(override val seed: Int = 1337, val scale: Float = 0.006f, val ribbons: Float = 14f, val turbulence: Float = 48f, val feathering: Float = 0.52f, val inkBalance: Float = 0.5f, val contrast: Float = 1.25f, val paperColor: Int = 0xFFF3E9D3.toInt(), val blueInkColor: Int = 0xFF164B78.toInt(), val redInkColor: Int = 0xFF9D2949.toInt(), val darkInkColor: Int = 0xFF1C1831.toInt()) : ProceduralTextureParameters
class InkMarblingTextureGenerator : NativeProceduralTextureGenerator<InkMarblingTextureParameters>(TextureType.INK_MARBLING) {
    override fun colors(parameters: InkMarblingTextureParameters) = intArrayOf(parameters.paperColor, parameters.blueInkColor, parameters.redInkColor, parameters.darkInkColor)
    override fun values(parameters: InkMarblingTextureParameters) = floatArrayOf(parameters.scale, parameters.ribbons, parameters.turbulence, parameters.feathering, parameters.inkBalance, parameters.contrast)
}
