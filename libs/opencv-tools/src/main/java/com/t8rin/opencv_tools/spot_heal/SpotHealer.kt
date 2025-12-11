@file:Suppress("unused")

package com.t8rin.opencv_tools.spot_heal

import android.graphics.Bitmap
import com.t8rin.opencv_tools.spot_heal.model.HealType
import com.t8rin.opencv_tools.utils.OpenCV
import com.t8rin.opencv_tools.utils.toBitmap
import com.t8rin.opencv_tools.utils.toMat
import org.opencv.core.Mat
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc
import org.opencv.photo.Photo

object SpotHealer : OpenCV() {

    fun heal(
        image: Bitmap,
        mask: Bitmap,
        radius: Float,
        type: HealType
    ): Bitmap {
        val src = image.toMat()
        val inpaintMask = Mat()

        Imgproc.resize(
            mask.toMat(),
            inpaintMask,
            Size(image.width.toDouble(), image.height.toDouble())
        )

        Imgproc.cvtColor(src, src, Imgproc.COLOR_RGB2XYZ)
        Imgproc.cvtColor(inpaintMask, inpaintMask, Imgproc.COLOR_BGR2GRAY)

        val dst = Mat()

        Photo.inpaint(
            src,
            inpaintMask,
            dst,
            radius.toDouble(),
            type.ordinal
        )

        Imgproc.cvtColor(dst, dst, Imgproc.COLOR_XYZ2RGB)

        return dst.toBitmap()
    }


}