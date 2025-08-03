@file:Suppress("unused")

package com.t8rin.opencv_tools.utils

import android.graphics.Bitmap
import androidx.core.graphics.createBitmap
import com.t8rin.opencv_tools.autocrop.model.CropEdges
import org.opencv.android.OpenCVLoader
import org.opencv.android.Utils
import org.opencv.core.Core
import org.opencv.core.Mat
import org.opencv.core.Rect
import org.opencv.core.Scalar
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc

internal fun Bitmap.getMat(): Mat {
    val mat = Mat()
    Utils.bitmapToMat(copy(Bitmap.Config.ARGB_8888, false), mat)
    return mat
}

internal fun Mat.toBitmap(): Bitmap {
    val output = createBitmap(cols(), rows())
    Utils.matToBitmap(this, output)

    return output
}

// Note: Range.s included, Range.e excluded
internal fun Mat.getCrop(edges: CropEdges): Mat =
    rowRange(edges.top, edges.bottom)

/**
 * Mean
 */

internal fun Mat.multiChannelMean(): Double =
    Core.mean(this).`val`.average()

internal fun Mat.singleChannelMean(): Double =
    Core.mean(this).`val`.first()

internal fun Mat.resizeAndCrop(targetSize: Size): Mat {
    val aspectRatio = this.width().toDouble() / this.height()
    val targetAspectRatio = targetSize.width / targetSize.height

    val resizedMat = Mat()
    if (aspectRatio > targetAspectRatio) {
        val newWidth = (this.height() * targetAspectRatio).toInt()
        Imgproc.resize(this, resizedMat, Size(newWidth.toDouble(), this.height().toDouble()))
    } else {
        val newHeight = (this.width() / targetAspectRatio).toInt()
        Imgproc.resize(this, resizedMat, Size(this.width().toDouble(), newHeight.toDouble()))
    }

    val xOffset = (resizedMat.width() - targetSize.width).toInt() / 2
    val yOffset = (resizedMat.height() - targetSize.height).toInt() / 2
    return Mat(
        resizedMat,
        Rect(xOffset, yOffset, targetSize.width.toInt(), targetSize.height.toInt())
    )
}

internal fun Mat.resizeAndPad(targetSize: Size): Mat {
    val aspectRatio = this.width().toDouble() / this.height()
    val targetAspectRatio = targetSize.width / targetSize.height

    val resizedMat = Mat()
    if (aspectRatio > targetAspectRatio) {
        val newHeight = (targetSize.width / aspectRatio).toInt()
        Imgproc.resize(this, resizedMat, Size(targetSize.width, newHeight.toDouble()))
    } else {
        val newWidth = (targetSize.height * aspectRatio).toInt()
        Imgproc.resize(this, resizedMat, Size(newWidth.toDouble(), targetSize.height))
    }

    val paddedMat = Mat(targetSize, this.type(), Scalar(0.0, 0.0, 0.0))

    resizedMat.copyTo(paddedMat.submat(Rect(0, 0, resizedMat.width(), resizedMat.height())))

    return paddedMat
}

internal fun Int.toScalar(): Scalar {
    val alpha = (this shr 24 and 0xFF).toDouble()
    val red = (this shr 16 and 0xFF).toDouble()
    val green = (this shr 8 and 0xFF).toDouble()
    val blue = (this and 0xFF).toDouble()

    return Scalar(red, green, blue, alpha)
}

abstract class OpenCV {
    init {
        OpenCVLoader.initLocal()
    }
}