package com.t8rin.opencv_tools.spot_heal

import android.graphics.Bitmap
import com.t8rin.opencv_tools.autocrop.getMat
import org.opencv.android.OpenCVLoader
import org.opencv.android.Utils
import org.opencv.core.Mat
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc
import org.opencv.photo.Photo

object SpotHealer {

    init {
        OpenCVLoader.initDebug()
    }

    fun heal(
        image: Bitmap,
        mask: Bitmap,
        radius: Float,
        type: Type
    ): Bitmap {
        val src = image.getMat()
        val inpaintMask = Mat()

        Imgproc.resize(
            mask.getMat(),
            inpaintMask,
            Size(image.width.toDouble(), image.height.toDouble())
        )

        Imgproc.cvtColor(src, src, Imgproc.COLOR_RGB2XYZ)
        Imgproc.cvtColor(inpaintMask, inpaintMask, Imgproc.COLOR_BGR2GRAY)

        val dst = src.clone()

        Photo.inpaint(
            src,
            inpaintMask,
            dst,
            radius.toDouble(),
            type.ordinal
        )

        val output = Bitmap.createBitmap(
            dst.cols(),
            dst.rows(),
            Bitmap.Config.ARGB_8888
        )

        Imgproc.cvtColor(dst, dst, Imgproc.COLOR_XYZ2RGB)

        Utils.matToBitmap(dst, output)

        return output
    }

    enum class Type {
        NS, TELEA
    }
}