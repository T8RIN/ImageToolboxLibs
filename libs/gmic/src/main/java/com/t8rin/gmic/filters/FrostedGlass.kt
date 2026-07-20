package com.t8rin.gmic.filters

import com.t8rin.gmic.model.GmicAlphaMode
import com.t8rin.gmic.model.GmicOptions

/** Textured-glass refraction with independently controlled horizontal and vertical distortion. */
data class FrostedGlass(
    val xAmplitude: Float = 40f,
    val yAmplitude: Float = 40f,
    val xSmoothness: Float = 1f,
    val ySmoothness: Float = 1f,
    val edgeAttenuation: Float = 0f,
    val edgeInfluence: Float = 2f,
    val noiseScale: Int = 0
) : RawGmicFilter(
    command = gmicCommand(
        "fx_textured_glass",
        xAmplitude.inRange("xAmplitude", 0f, 400f),
        yAmplitude.inRange("yAmplitude", 0f, 400f),
        xSmoothness.inRange("xSmoothness", 0f, 5f),
        ySmoothness.inRange("ySmoothness", 0f, 5f),
        edgeAttenuation.inRange("edgeAttenuation", 0f, 1f),
        edgeInfluence.inRange("edgeInfluence", 0f, 10f),
        noiseScale.inRange("noiseScale", 0..16)
    ),
    options = GmicOptions(alphaMode = GmicAlphaMode.Process)
)
