package com.t8rin.opencv_tools.lens_correction

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import com.t8rin.opencv_tools.utils.OpenCV
import com.t8rin.opencv_tools.utils.getMat
import com.t8rin.opencv_tools.utils.toBitmap
import org.json.JSONObject
import org.opencv.calib3d.Calib3d
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader

object LensCorrection : OpenCV() {

    fun undistort(
        context: Context,
        bitmap: Bitmap,
        lensDataUri: Uri
    ): Bitmap {
        return undistort(
            bitmap = bitmap,
            lensData = context.contentResolver.openInputStream(lensDataUri) ?: return bitmap
        )
    }

    fun undistort(
        bitmap: Bitmap,
        lensData: InputStream
    ): Bitmap {
        val json = lensData.use { inputStream ->
            BufferedReader(InputStreamReader(inputStream)).use { it.readText() }
        }

        return undistort(
            bitmap = bitmap,
            lensDataJson = json
        )
    }

    fun undistort(
        bitmap: Bitmap,
        lensDataJson: String
    ): Bitmap {
        val json = JSONObject(lensDataJson)
        val fisheyeParams = json.getJSONObject("fisheye_params")
        val cameraMatrixArray = fisheyeParams.getJSONArray("camera_matrix")
        val distCoeffsArray = fisheyeParams.getJSONArray("distortion_coeffs")

        // Convert Bitmap to Mat
        val rgbaMat = bitmap.getMat()
        val rgbMat = Mat()
        Imgproc.cvtColor(rgbaMat, rgbMat, Imgproc.COLOR_RGBA2RGB)

        val inputSize = rgbMat.size()

        // Build camera matrix K
        val K = Mat(3, 3, CvType.CV_64F)
        for (i in 0 until 3) {
            val row = cameraMatrixArray.getJSONArray(i)
            for (j in 0 until 3) {
                K.put(i, j, row.getDouble(j))
            }
        }

        // Build distortion coefficients D
        val D = Mat(4, 1, CvType.CV_64F)
        for (i in 0 until 4) {
            D.put(i, 0, distCoeffsArray.getDouble(i))
        }

        // Scale K if bitmap resolution != calib resolution
        val calibDim = json.getJSONObject("calib_dimension")
            ?: json.getJSONObject("orig_dimension")
            ?: json.getJSONObject("output_dimension")

        val calibW = calibDim.getInt("w")
        val calibH = calibDim.getInt("h")
        val scaleX = bitmap.width.toDouble() / calibW
        val scaleY = bitmap.height.toDouble() / calibH

        val scaledK = K.clone()
        scaledK.put(0, 0, K.get(0, 0)[0] * scaleX) // fx
        scaledK.put(0, 2, K.get(0, 2)[0] * scaleX) // cx
        scaledK.put(1, 1, K.get(1, 1)[0] * scaleY) // fy
        scaledK.put(1, 2, K.get(1, 2)[0] * scaleY) // cy

        val undistorted = Mat()
        Calib3d.fisheye_undistortImage(
            rgbMat,
            undistorted,
            scaledK,
            D,
            scaledK,
            inputSize
        )

        val outMat = Mat()
        Imgproc.cvtColor(undistorted, outMat, Imgproc.COLOR_RGB2RGBA)

        return outMat.toBitmap()
    }

}