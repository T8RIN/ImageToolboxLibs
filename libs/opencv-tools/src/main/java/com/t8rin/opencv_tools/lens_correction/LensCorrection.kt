@file:Suppress("LocalVariableName", "unused", "JavaIoSerializableObjectMustHaveReadResolve")

package com.t8rin.opencv_tools.lens_correction

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import com.t8rin.opencv_tools.lens_correction.LCException.InvalidCalibDimensions
import com.t8rin.opencv_tools.lens_correction.LCException.InvalidDistortionCoeffs
import com.t8rin.opencv_tools.lens_correction.LCException.InvalidMatrixSize
import com.t8rin.opencv_tools.lens_correction.LCException.MissingFisheyeParams
import com.t8rin.opencv_tools.utils.OpenCV
import com.t8rin.opencv_tools.utils.getMat
import com.t8rin.opencv_tools.utils.toBitmap
import org.json.JSONArray
import org.json.JSONObject
import org.opencv.calib3d.Calib3d
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc
import java.io.InputStream
import java.io.Reader
import kotlin.contracts.contract

object LensCorrection : OpenCV() {

    fun undistort(
        context: Context,
        bitmap: Bitmap,
        lensDataUri: Uri
    ): Bitmap = undistort(
        bitmap = bitmap,
        lensData = context.contentResolver.openInputStream(lensDataUri)!!
    )

    fun undistort(
        bitmap: Bitmap,
        lensData: InputStream
    ): Bitmap = undistort(
        bitmap = bitmap,
        lensDataJson = lensData.bufferedReader().use(Reader::readText)
    )

    fun undistort(
        bitmap: Bitmap,
        lensDataJson: String
    ): Bitmap = undistort(
        bitmap = bitmap,
        lensProfile = JSONObject(lensDataJson).toLensProfile()
    )

    fun undistort(
        bitmap: Bitmap,
        lensProfile: LensProfile
    ): Bitmap = undistortImpl(
        bitmap = bitmap,
        cameraMatrix = lensProfile.cameraMatrix,
        distortionCoeffs = lensProfile.distortionCoeffs,
        calibWidth = lensProfile.calibWidth,
        calibHeight = lensProfile.calibHeight
    )


    private fun undistortImpl(
        bitmap: Bitmap,
        cameraMatrix: List<List<Double>>,
        distortionCoeffs: List<Double>,
        calibWidth: Int,
        calibHeight: Int
    ): Bitmap {
        lcCheck(
            value = distortionCoeffs.size == 4,
            message = InvalidDistortionCoeffs
        )

        val rgbaMat = bitmap.getMat()
        val K = Mat()
        val D = Mat()
        val scaledK = Mat()
        val undistorted = Mat()
        val outMat = Mat()

        try {
            Imgproc.cvtColor(rgbaMat, rgbaMat, Imgproc.COLOR_RGBA2RGB)

            K.create(3, 3, CvType.CV_64F)

            cameraMatrix.forEachIndexed { i, row ->
                lcCheck(
                    value = row.size == 3,
                    message = InvalidMatrixSize
                )

                row.forEachIndexed { j, value ->
                    K.put(i, j, value)
                }
            }

            D.create(4, 1, CvType.CV_64F)

            distortionCoeffs.forEachIndexed { i, value ->
                D.put(i, 0, -value)
            }

            val scaleX = bitmap.width.toDouble() / calibWidth
            val scaleY = bitmap.height.toDouble() / calibHeight

            scaledK.create(3, 3, CvType.CV_64F)
            K.copyTo(scaledK)

            scaledK.put(0, 0, K.get(0, 0)[0] * scaleX) // fx
            scaledK.put(0, 2, K.get(0, 2)[0] * scaleX) // cx
            scaledK.put(1, 1, K.get(1, 1)[0] * scaleY) // fy
            scaledK.put(1, 2, K.get(1, 2)[0] * scaleY) // cy

            Calib3d.fisheye_undistortImage(
                rgbaMat,
                undistorted,
                scaledK,
                D,
                scaledK,
                rgbaMat.size()
            )

            Imgproc.cvtColor(undistorted, outMat, Imgproc.COLOR_RGB2RGBA)

            return outMat.toBitmap()
        } finally {
            rgbaMat.release()
            K.release()
            D.release()
            scaledK.release()
            undistorted.release()
            outMat.release()
        }
    }

    private fun JSONObject.toLensProfile(): LensProfile {
        lcCheck(
            value = has("fisheye_params"),
            message = MissingFisheyeParams
        )

        val fisheyeParams = getJSONObject("fisheye_params")

        lcCheck(
            value = fisheyeParams.has("camera_matrix"),
            message = InvalidMatrixSize
        )

        lcCheck(
            value = fisheyeParams.has("distortion_coeffs"),
            message = InvalidDistortionCoeffs
        )

        val calibDim = lcCheck(
            value = safeJSONObject("calib_dimension")
                ?: safeJSONObject("orig_dimension")
                ?: safeJSONObject("output_dimension"),
            message = InvalidCalibDimensions
        )

        val calibW = calibDim.getInt("w")
        val calibH = calibDim.getInt("h")

        lcCheck(
            value = calibW > 0 && calibH > 0,
            message = InvalidCalibDimensions
        )

        return LensProfile(
            cameraMatrix = fisheyeParams.getCameraMatrix(),
            distortionCoeffs = fisheyeParams.getDistortionCoeffs(),
            calibWidth = calibW,
            calibHeight = calibH
        )
    }

    private fun JSONObject.getDistortionCoeffs(): List<Double> {
        val distCoeffsArray = safeJSONArray("distortion_coeffs")

        lcCheck(
            value = distCoeffsArray?.length() == 4,
            message = InvalidDistortionCoeffs
        )

        return List(size = 4, init = distCoeffsArray::getDouble)
    }

    private fun JSONObject.getCameraMatrix(): List<List<Double>> {
        val cameraMatrixArray = safeJSONArray("camera_matrix")

        lcCheck(
            value = cameraMatrixArray?.length() == 3,
            message = InvalidMatrixSize
        )

        return List(3) { i ->
            List(3) { j ->
                cameraMatrixArray.getJSONArray(i).getDouble(j)
            }
        }
    }

    private fun lcCheck(value: Boolean, message: LCException) {
        contract { returns() implies value }
        if (!value) throw message
    }

    private fun <T> lcCheck(value: T?, message: LCException): T {
        contract { returns() implies (value != null) }
        if (value == null) throw message else return value
    }

    private fun JSONObject.safeJSONObject(key: String): JSONObject? =
        runCatching { getJSONObject(key) }.getOrNull()

    private fun JSONObject.safeJSONArray(key: String): JSONArray? =
        runCatching { getJSONArray(key) }.getOrNull()
}


data class LensProfile(
    val cameraMatrix: List<List<Double>>,
    val distortionCoeffs: List<Double>,
    val calibWidth: Int,
    val calibHeight: Int
)

sealed class LCException(message: String) : Exception(message) {
    data object MissingFisheyeParams : LCException("No fisheye_params in JSON")
    data object InvalidMatrixSize : LCException("Incorrect camera_matrix size (pass 3x3)")
    data object InvalidCalibDimensions : LCException("Invalid calibration dimensions")
    data object InvalidDistortionCoeffs : LCException("Bad distortion coefficients (pass only 4)")
}