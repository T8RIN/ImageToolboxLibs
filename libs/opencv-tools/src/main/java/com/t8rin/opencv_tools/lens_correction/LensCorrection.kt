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
        val rgbaMat = Mat()
        val rgbMat = Mat()
        val K = Mat()
        val D = Mat()
        val scaledK = Mat()
        val undistorted = Mat()
        val outMat = Mat()

        try {
            val json = JSONObject(lensDataJson)

            if (!json.has("fisheye_params")) {
                throw IllegalArgumentException("No fisheye_params in JSON")
            }

            val fisheyeParams = json.getJSONObject("fisheye_params")

            if (!fisheyeParams.has("camera_matrix") || !fisheyeParams.has("distortion_coeffs")) {
                throw IllegalArgumentException("No camera_matrix or distortion_coeffs")
            }

            val cameraMatrixArray = fisheyeParams.getJSONArray("camera_matrix")
            val distCoeffsArray = fisheyeParams.getJSONArray("distortion_coeffs")

            if (cameraMatrixArray.length() != 3 || distCoeffsArray.length() != 4) {
                throw IllegalArgumentException("Incorrect camera_matrix or distortion_coeffs")
            }

            bitmap.getMat().copyTo(rgbaMat)
            Imgproc.cvtColor(rgbaMat, rgbMat, Imgproc.COLOR_RGBA2RGB)

            val inputSize = rgbMat.size()

            K.create(3, 3, CvType.CV_64F)
            for (i in 0 until 3) {
                val row = cameraMatrixArray.getJSONArray(i)
                if (row.length() != 3) {
                    throw IllegalArgumentException("Неверный размер строки camera_matrix")
                }
                for (j in 0 until 3) {
                    K.put(i, j, row.getDouble(j))
                }
            }

            D.create(4, 1, CvType.CV_64F)
            for (i in 0 until 4) {
                D.put(i, 0, -distCoeffsArray.getDouble(i))
            }

            // Scale K if bitmap resolution != calib resolution
            val calibDim = json.getJSONObject("calib_dimension")
                ?: json.getJSONObject("orig_dimension")
                ?: json.getJSONObject("output_dimension")
                ?: throw IllegalArgumentException("No calib data")

            val calibW = calibDim.getInt("w")
            val calibH = calibDim.getInt("h")

            if (calibW <= 0 || calibH <= 0) {
                throw IllegalArgumentException("No calib data")
            }

            val scaleX = bitmap.width.toDouble() / calibW
            val scaleY = bitmap.height.toDouble() / calibH

            scaledK.create(3, 3, CvType.CV_64F)
            K.copyTo(scaledK)

            scaledK.put(0, 0, K.get(0, 0)[0] * scaleX) // fx
            scaledK.put(0, 2, K.get(0, 2)[0] * scaleX) // cx
            scaledK.put(1, 1, K.get(1, 1)[0] * scaleY) // fy
            scaledK.put(1, 2, K.get(1, 2)[0] * scaleY) // cy

            Calib3d.fisheye_undistortImage(
                rgbMat,
                undistorted,
                scaledK,
                D,
                scaledK,
                inputSize
            )

            Imgproc.cvtColor(undistorted, outMat, Imgproc.COLOR_RGB2RGBA)

            return outMat.toBitmap()
        } catch (e: Exception) {
            // В случае ошибки возвращаем исходное изображение
            return bitmap
        } finally {
            // Освобождаем ресурсы Mat объектов
            rgbaMat.release()
            rgbMat.release()
            K.release()
            D.release()
            scaledK.release()
            undistorted.release()
            outMat.release()
        }
    }
}