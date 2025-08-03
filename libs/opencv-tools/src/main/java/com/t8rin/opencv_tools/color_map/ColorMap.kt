package com.t8rin.opencv_tools.color_map

import android.graphics.Bitmap
import com.t8rin.opencv_tools.utils.OpenCV
import com.t8rin.opencv_tools.utils.getMat
import com.t8rin.opencv_tools.utils.toBitmap
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc

object ColorMap : OpenCV() {

    fun apply(
        bitmap: Bitmap,
        map: Type = Type.JET
    ): Bitmap {
        val grayMat = bitmap.getMat()

        if (grayMat.channels() == 4) {
            Imgproc.cvtColor(grayMat, grayMat, Imgproc.COLOR_RGBA2GRAY)
        } else if (grayMat.channels() == 3) {
            Imgproc.cvtColor(grayMat, grayMat, Imgproc.COLOR_RGB2GRAY)
        }

        val colorMat = Mat()

        Imgproc.applyColorMap(grayMat, colorMat, map.ordinal)

        return colorMat.toBitmap()
    }

    enum class Type {
        AUTUMN,
        BONE,
        JET,
        WINTER,
        RAINBOW,
        OCEAN,
        SUMMER,
        SPRING,
        COOL,
        HSV,
        PINK,
        HOT,
        PARULA,
        MAGMA,
        INFERNO,
        PLASMA,
        VIRIDIS,
        CIVIDIS,
        TWILIGHT,
        TWILIGHT_SHIFTED
    }


}