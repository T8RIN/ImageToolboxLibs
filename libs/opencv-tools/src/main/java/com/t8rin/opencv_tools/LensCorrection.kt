@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package com.t8rin.opencv_tools

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.core.graphics.createBitmap
import org.json.JSONObject
import org.opencv.android.OpenCVLoader
import org.opencv.android.Utils
import org.opencv.calib3d.Calib3d
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader

object LensCorrection {

    init {
        OpenCVLoader.initDebug()
    }

    fun undistort(
        context: Context,
        bitmap: Bitmap,
        lensDataUri: Uri,
        applyDimensions: Boolean = false
    ): Bitmap {
        return undistort(
            bitmap = bitmap,
            lensData = context.contentResolver.openInputStream(lensDataUri) ?: return bitmap,
            applyDimensions = applyDimensions
        )
    }

    fun undistort(
        bitmap: Bitmap,
        lensData: InputStream,
        applyDimensions: Boolean = false
    ): Bitmap {
        val json = lensData.use { inputStream ->
            BufferedReader(InputStreamReader(inputStream)).use { it.readText() }
        }

        val jsonObj = JSONObject(json)
        val fisheyeParams = jsonObj.getJSONObject("fisheye_params")

        val cameraMatrixArray = fisheyeParams.getJSONArray("camera_matrix")
        val cameraMatrix = DoubleArray(9) {
            cameraMatrixArray.getJSONArray(it / 3).getDouble(it % 3)
        }

        val distCoeffsArray = fisheyeParams.getJSONArray("distortion_coeffs")
        val distCoeffs = DoubleArray(distCoeffsArray.length()) { distCoeffsArray.getDouble(it) }

        val radialDistortionLimit = fisheyeParams.optDouble("radial_distortion_limit", Double.NaN)

        val inputHorizontalStretch = jsonObj.optDouble("input_horizontal_stretch", 1.0)
        val inputVerticalStretch = jsonObj.optDouble("input_vertical_stretch", 1.0)

        val calibDimension = jsonObj.optJSONObject("calib_dimension")?.let {
            it.getInt("w") to it.getInt("h")
        }

        return undistort(
            bitmap = bitmap,
            cameraMatrix = cameraMatrix,
            distCoeffs = distCoeffs,
            radialDistortionLimit = radialDistortionLimit,
            inputHorizontalStretch = inputHorizontalStretch,
            inputVerticalStretch = inputVerticalStretch,
            calibDimension = calibDimension.takeIf { applyDimensions }
        )
    }

    fun undistort(
        bitmap: Bitmap,
        cameraMatrix: DoubleArray,
        distCoeffs: DoubleArray,
        radialDistortionLimit: Double = Double.NaN,
        inputHorizontalStretch: Double = 1.0,
        inputVerticalStretch: Double = 1.0,
        calibDimension: Pair<Int, Int>? = null
    ): Bitmap {
        val src = Mat()
        Utils.bitmapToMat(bitmap, src)

        if (inputHorizontalStretch != 1.0 || inputVerticalStretch != 1.0) {
            Imgproc.resize(
                src,
                src,
                Size(src.width() * inputHorizontalStretch, src.height() * inputVerticalStretch)
            )
        }

        val camMatrix = Mat(3, 3, CvType.CV_64F).apply { put(0, 0, *cameraMatrix) }

        val distMat = Mat(1, distCoeffs.size, CvType.CV_64F).apply { put(0, 0, *distCoeffs) }

        val size =
            calibDimension?.let { Size(it.first.toDouble(), it.second.toDouble()) } ?: src.size()

        val dst = Mat()
        val newCamMatrix = Calib3d.getOptimalNewCameraMatrix(camMatrix, distMat, size, 1.0, size)

        if (!radialDistortionLimit.isNaN()) {
            val mask = Mat(src.size(), CvType.CV_8U, org.opencv.core.Scalar(255.0))
            Calib3d.undistort(src, dst, camMatrix, distMat, newCamMatrix)
            dst.setTo(org.opencv.core.Scalar(0.0), mask)
        } else {
            Calib3d.undistort(src, dst, camMatrix, distMat, newCamMatrix)
        }

        val resultBitmap = createBitmap(dst.cols(), dst.rows())
        Utils.matToBitmap(dst, resultBitmap)

        src.release()
        dst.release()
        camMatrix.release()
        distMat.release()
        newCamMatrix.release()

        return resultBitmap
    }

}