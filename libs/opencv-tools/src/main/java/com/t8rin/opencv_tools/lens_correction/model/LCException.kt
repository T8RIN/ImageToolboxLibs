package com.t8rin.opencv_tools.lens_correction.model

@Suppress("JavaIoSerializableObjectMustHaveReadResolve")
sealed class LCException(message: String) : Exception(message) {
    data object MissingFisheyeParams : LCException("No fisheye_params in JSON")
    data object InvalidMatrixSize : LCException("Incorrect camera_matrix size (pass 3x3)")
    data object InvalidCalibDimensions : LCException("Invalid calibration dimensions")
    data object InvalidDistortionCoeffs : LCException("Bad distortion coefficients (pass only 4)")
}