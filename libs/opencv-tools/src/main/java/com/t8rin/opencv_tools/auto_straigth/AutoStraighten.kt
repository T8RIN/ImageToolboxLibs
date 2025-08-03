package com.t8rin.opencv_tools.auto_straigth

import android.graphics.Bitmap
import androidx.annotation.IntRange
import com.t8rin.opencv_tools.utils.OpenCV
import com.t8rin.opencv_tools.utils.getMat
import com.t8rin.opencv_tools.utils.toBitmap
import org.opencv.core.Core
import org.opencv.core.Mat
import org.opencv.core.Point
import org.opencv.core.Rect
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc
import org.opencv.photo.Photo
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

object AutoStraighten : OpenCV() {

    fun process(
        input: Bitmap,
        @IntRange(0, 90) maxSkew: Int = 10,
        allowCrop: Boolean = true
    ): Bitmap {
        val srcMat = input.getMat()

        val gray = Mat()
        Imgproc.cvtColor(srcMat, gray, Imgproc.COLOR_BGR2GRAY)
        Photo.fastNlMeansDenoising(gray, gray, 3f)

        val binary = Mat()
        Imgproc.threshold(
            gray, binary, 0.0, 255.0,
            Imgproc.THRESH_BINARY_INV or Imgproc.THRESH_OTSU
        )

        val lines = Mat()
        Imgproc.HoughLinesP(
            binary,
            lines,
            1.0,
            Math.PI / 180,
            200,
            srcMat.width() / 12.0,
            srcMat.width() / 150.0
        )

        if (lines.rows() == 0) return input

        val angles = mutableListOf<Double>()
        for (i in 0 until lines.rows()) {
            val l = lines.get(i, 0)
            val x1 = l[0]
            val y1 = l[1]
            val x2 = l[2]
            val y2 = l[3]
            angles += atan2(y2 - y1, x2 - x1)
        }

        val landscape = angles.count { abs(it) > Math.PI / 4 } > angles.size / 2

        val filtered = if (landscape) {
            angles.filter {
                val deg = abs(Math.toDegrees(it))
                deg > (90 - maxSkew) && deg < (90 + maxSkew)
            }
        } else {
            angles.filter { abs(Math.toDegrees(it)) < maxSkew }
        }

        if (filtered.size < 5) return input

        var angleDeg = Math.toDegrees(filtered.median())

        val rotated = Mat()

        if (landscape) {
            angleDeg = if (angleDeg < 0) {
                Core.rotate(srcMat, rotated, Core.ROTATE_90_CLOCKWISE)
                angleDeg + 90
            } else {
                Core.rotate(srcMat, rotated, Core.ROTATE_90_COUNTERCLOCKWISE)
                angleDeg - 90
            }
        } else {
            srcMat.copyTo(rotated)
        }

        val center = Point(rotated.width() / 2.0, rotated.height() / 2.0)
        val rotMat = Imgproc.getRotationMatrix2D(center, angleDeg, 1.0)

        val angleRad = Math.toRadians(angleDeg)
        val absSin = abs(sin(angleRad))
        val absCos = abs(cos(angleRad))
        val newWidth = (rotated.height() * absSin + rotated.width() * absCos).toInt()
        val newHeight = (rotated.height() * absCos + rotated.width() * absSin).toInt()

        rotMat.put(0, 2, rotMat.get(0, 2)[0] + (newWidth / 2.0 - center.x))
        rotMat.put(1, 2, rotMat.get(1, 2)[0] + (newHeight / 2.0 - center.y))

        val rotatedFull = Mat()
        Imgproc.warpAffine(
            rotated,
            rotatedFull,
            rotMat,
            Size(newWidth.toDouble(), newHeight.toDouble()),
            Imgproc.INTER_LINEAR,
            Core.BORDER_REPLICATE,
        )

        return if (allowCrop) {
            val cropRect = getLargestRotatedRect(
                width = rotated.width(),
                height = rotated.height(),
                angle = angleRad
            )

            Mat(rotatedFull, cropRect).clone()
        } else {
            rotatedFull
        }.toBitmap()
    }

    private fun getLargestRotatedRect(width: Int, height: Int, angle: Double): Rect {
        val absSin = abs(sin(angle))
        val absCos = abs(cos(angle))

        val boundWidth = (width * absCos - height * absSin).let { if (it < 0) 0.0 else it }
        val boundHeight = (height * absCos - width * absSin).let { if (it < 0) 0.0 else it }

        val newWidth = (height * absSin + width * absCos)
        val newHeight = (height * absCos + width * absSin)

        val x = ((newWidth - boundWidth) / 2.0).toInt()
        val y = ((newHeight - boundHeight) / 2.0).toInt()

        return Rect(x, y, boundWidth.toInt(), boundHeight.toInt())
    }

    private fun List<Double>.median(): Double {
        if (isEmpty()) return 0.0
        val sorted = this.sorted()
        return if (size % 2 == 0)
            (sorted[size / 2 - 1] + sorted[size / 2]) / 2
        else
            sorted[size / 2]
    }

}