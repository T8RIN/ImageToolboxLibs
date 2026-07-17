package com.t8rin.fast_noise.texture

import com.t8rin.fast_noise.texture.internal.TextureType

data class SoapFilmTextureParameters(
    override val seed: Int = 1337,
    val scale: Float = 0.007f,
    val bands: Float = 0.68f,
    val flow: Float = 0.72f,
    val filmSharpness: Float = 0.55f,
    val shine: Float = 0.72f,
    val contrast: Float = 1.25f,
    val backgroundColor: Int = 0xFF10182B.toInt(),
    val primaryColor: Int = 0xFF20CDE2.toInt(),
    val secondaryColor: Int = 0xFFF45CB4.toInt(),
    val highlightColor: Int = 0xFFFFE66D.toInt()
) : ProceduralTextureParameters

class SoapFilmTextureGenerator :
    NativeProceduralTextureGenerator<SoapFilmTextureParameters>(TextureType.SOAP_FILM) {
    override fun colors(parameters: SoapFilmTextureParameters) = intArrayOf(
        parameters.backgroundColor,
        parameters.primaryColor,
        parameters.secondaryColor,
        parameters.highlightColor
    )

    override fun values(parameters: SoapFilmTextureParameters) = floatArrayOf(
        parameters.scale,
        parameters.bands,
        parameters.flow,
        parameters.filmSharpness,
        parameters.shine,
        parameters.contrast
    )
}
