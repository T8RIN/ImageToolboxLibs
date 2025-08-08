@file:Suppress("unused")

package com.t8rin.opencv_tools.color_map

import android.graphics.Bitmap
import com.t8rin.opencv_tools.color_map.model.ColorMapType
import com.t8rin.opencv_tools.utils.OpenCV
import com.t8rin.opencv_tools.utils.getMat
import com.t8rin.opencv_tools.utils.toBitmap
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc

object ColorMap : OpenCV() {

    fun apply(
        bitmap: Bitmap,
        map: ColorMapType = ColorMapType.JET
    ): Bitmap {
        val grayMat = bitmap.getMat()

        Imgproc.cvtColor(grayMat, grayMat, Imgproc.COLOR_RGBA2BGR)
        Imgproc.cvtColor(grayMat, grayMat, Imgproc.COLOR_BGR2GRAY)

        val colorMat = Mat()

        Imgproc.applyColorMap(grayMat, colorMat, map.ordinal)

        Imgproc.cvtColor(colorMat, colorMat, Imgproc.COLOR_BGR2RGBA)

        grayMat.release()
        return colorMat.toBitmap()
    }

}