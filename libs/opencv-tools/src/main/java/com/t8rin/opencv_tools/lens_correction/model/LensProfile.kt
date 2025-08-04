package com.t8rin.opencv_tools.lens_correction.model

data class LensProfile(
    val cameraMatrix: List<List<Double>>,
    val distortionCoeffs: List<Double>,
    val calibWidth: Int,
    val calibHeight: Int
) {
    companion object
}