package com.t8rin.fast_noise.texture

import com.t8rin.fast_noise.texture.internal.TextureType

data class ReactionDiffusionTextureParameters(
    override val seed: Int = 1337,
    val scale: Float = 0.009f,
    val patternScale: Float = 0.72f,
    val feedback: Float = 0.68f,
    val edgeSharpness: Float = 0.72f,
    val glow: Float = 0.68f,
    val contrast: Float = 1.45f,
    val backgroundColor: Int = 0xFF090B17.toInt(),
    val primaryColor: Int = 0xFF303785.toInt(),
    val secondaryColor: Int = 0xFFE64C93.toInt(),
    val highlightColor: Int = 0xFFFFD76A.toInt()
) : ProceduralTextureParameters

class ReactionDiffusionTextureGenerator :
    NativeProceduralTextureGenerator<ReactionDiffusionTextureParameters>(TextureType.REACTION_DIFFUSION) {
    override fun colors(parameters: ReactionDiffusionTextureParameters) = intArrayOf(
        parameters.backgroundColor,
        parameters.primaryColor,
        parameters.secondaryColor,
        parameters.highlightColor
    )

    override fun values(parameters: ReactionDiffusionTextureParameters) = floatArrayOf(
        parameters.scale,
        parameters.patternScale,
        parameters.feedback,
        parameters.edgeSharpness,
        parameters.glow,
        parameters.contrast
    )
}
