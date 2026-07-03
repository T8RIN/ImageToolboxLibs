package com.t8rin.fast_noise.texture

import com.t8rin.fast_noise.texture.internal.TextureType

data class BioluminescenceTextureParameters(override val seed: Int = 1337, val scale: Float = 0.009f, val veins: Float = 0.72f, val branching: Float = 0.58f, val turbulence: Float = 30f, val glow: Float = 0.86f, val depth: Float = 0.62f, val backgroundColor: Int = 0xFF031A1A.toInt(), val tissueColor: Int = 0xFF0B514D.toInt(), val glowColor: Int = 0xFF20E6C2.toInt(), val coreColor: Int = 0xFFC4FFF1.toInt()) : ProceduralTextureParameters
class BioluminescenceTextureGenerator : NativeProceduralTextureGenerator<BioluminescenceTextureParameters>(TextureType.BIOLUMINESCENCE) {
    override fun colors(parameters: BioluminescenceTextureParameters) = intArrayOf(parameters.backgroundColor, parameters.tissueColor, parameters.glowColor, parameters.coreColor)
    override fun values(parameters: BioluminescenceTextureParameters) = floatArrayOf(parameters.scale, parameters.veins, parameters.branching, parameters.turbulence, parameters.glow, parameters.depth)
}
