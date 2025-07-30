@file:Suppress("unused")

package com.t8rin.opencv_tools.red_eye

import android.content.Context
import android.graphics.Bitmap
import androidx.core.graphics.createBitmap
import com.t8rin.opencv_tools.utils.OpenCV
import kotlinx.coroutines.coroutineScope
import org.opencv.android.Utils
import org.opencv.core.Core
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.MatOfRect
import org.opencv.core.Point
import org.opencv.core.Rect
import org.opencv.core.Scalar
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc
import org.opencv.objdetect.CascadeClassifier
import java.io.File
import java.io.FileOutputStream

object RedEyeRemover : OpenCV() {

    suspend fun removeRedEyes(
        context: Context,
        bitmap: Bitmap,
        minEyeSize: Double = 50.0,
        redThreshold: Double = 150.0
    ): Bitmap = coroutineScope {
        val srcMat = Mat()
        Utils.bitmapToMat(bitmap.copy(Bitmap.Config.ARGB_8888, false), srcMat)
        Imgproc.cvtColor(srcMat, srcMat, Imgproc.COLOR_RGBA2BGR)
        val resultMat = srcMat.clone()

        val grayMat = Mat()
        Imgproc.cvtColor(srcMat, grayMat, Imgproc.COLOR_BGR2GRAY)

        val eyes = MatOfRect()

        loadCascade(context).detectMultiScale(
            grayMat, eyes,
            1.3, 4, 0,
            Size(minEyeSize, minEyeSize), Size()
        )

        for (rect in eyes.toArray()) {
            val roi = Rect(rect.x, rect.y, rect.width, rect.height)
            val eyeMat = srcMat.submat(roi).clone()

            val channels = ArrayList<Mat>()
            Core.split(eyeMat, channels)
            val blue = channels[0]
            val green = channels[1]
            val red = channels[2]

            val bg = Mat()
            Core.add(blue, green, bg)

            val maskRed = Mat()
            Imgproc.threshold(red, maskRed, redThreshold, 255.0, Imgproc.THRESH_BINARY)

            val maskCmp = Mat()
            Core.compare(red, bg, maskCmp, Core.CMP_GT)

            val redEyeMask = Mat()
            Core.bitwise_and(maskRed, maskCmp, redEyeMask)

            val filledMask = fillHoles(redEyeMask)
            val dilatedMask = Mat()
            Imgproc.dilate(filledMask, dilatedMask, Mat(), Point(-1.0, -1.0), 3)

            val meanMat = Mat()
            Core.divide(bg, Scalar(2.0), meanMat)

            val mean3 = Mat()
            Core.merge(listOf(meanMat, meanMat, meanMat), mean3)

            val eyeOut = eyeMat.clone()
            mean3.copyTo(eyeOut, dilatedMask)

            eyeOut.copyTo(resultMat.submat(roi))
        }

        Imgproc.cvtColor(resultMat, resultMat, Imgproc.COLOR_BGR2RGBA)
        val outputBitmap = createBitmap(resultMat.cols(), resultMat.rows())
        Utils.matToBitmap(resultMat, outputBitmap)

        outputBitmap
    }

    private fun fillHoles(mask: Mat): Mat {
        val maskFloodfill = mask.clone()
        val rows = maskFloodfill.rows()
        val cols = maskFloodfill.cols()
        val floodfillMask = Mat.zeros(rows + 2, cols + 2, CvType.CV_8UC1)
        Imgproc.floodFill(maskFloodfill, floodfillMask, Point(0.0, 0.0), Scalar(255.0))
        val maskInv = Mat()
        Core.bitwise_not(maskFloodfill, maskInv)
        val filledMask = Mat()
        Core.bitwise_or(maskInv, mask, filledMask)
        return filledMask
    }

    private fun loadCascade(context: Context): CascadeClassifier {
        val inputStream = context.assets.open(EYE_DETECTION)
        val cascadeFile = File(context.cacheDir, EYE_DETECTION)
        FileOutputStream(cascadeFile).use { output ->
            inputStream.copyTo(output)
        }
        return CascadeClassifier(cascadeFile.absolutePath)
    }

    private const val EYE_DETECTION = "haarcascade_eye.xml"

}