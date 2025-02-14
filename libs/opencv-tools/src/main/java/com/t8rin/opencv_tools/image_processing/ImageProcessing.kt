package com.t8rin.opencv_tools.image_processing

import android.graphics.Bitmap
import com.t8rin.opencv_tools.utils.getMat
import com.t8rin.opencv_tools.utils.toBitmap
import org.opencv.android.OpenCVLoader
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc

object ImageProcessing {

    init {
        OpenCVLoader.initDebug()
    }

    fun canny(
        bitmap: Bitmap,
        thresholdOne: Float,
        thresholdTwo: Float
    ): Bitmap {
        val matGrayScale = Mat()
        Imgproc.cvtColor(bitmap.getMat(), matGrayScale, Imgproc.COLOR_RGBA2GRAY)
        Imgproc.medianBlur(matGrayScale, matGrayScale, 3)

        val matCanny = Mat()
        Imgproc.Canny(matGrayScale, matCanny, thresholdOne.toDouble(), thresholdTwo.toDouble())

        return matCanny.toBitmap()
    }

}