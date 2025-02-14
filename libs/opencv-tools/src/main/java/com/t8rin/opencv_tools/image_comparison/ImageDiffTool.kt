package com.t8rin.opencv_tools.image_comparison

import android.graphics.Bitmap
import com.t8rin.opencv_tools.utils.resizeAndPad
import com.t8rin.opencv_tools.utils.toScalar
import org.opencv.android.OpenCVLoader
import org.opencv.android.Utils
import org.opencv.core.*
import org.opencv.imgproc.Imgproc
import kotlin.math.log10
import kotlin.math.sqrt

enum class ComparisonType { SSIM, AE, MAE, NCC, PSNR, RMSE }

object ImageDiffTool {

    init {
        OpenCVLoader.initDebug()
    }

    fun highlightDifferences(
        input: Bitmap,
        other: Bitmap,
        comparisonType: ComparisonType,
        highlightColor: Int,
        threshold: Float = 4f
    ): Bitmap {
        val bitmap1 = input.copy(Bitmap.Config.ARGB_8888, false)
        val bitmap2 = other.copy(Bitmap.Config.ARGB_8888, false)

        val mat1 = Mat()
        val mat2Raw = Mat()
        Utils.bitmapToMat(bitmap1, mat1)
        Utils.bitmapToMat(bitmap2, mat2Raw)

        val mat2 = mat2Raw.resizeAndPad(mat1.size())

        if (mat1.size() != mat2.size() || mat1.type() != mat2.type()) {
            throw IllegalArgumentException("Bitmaps must have the same size and type")
        }

        val diff = Mat()
        val thresholdValue = (threshold.coerceIn(0f, 100f) * 2.55).coerceIn(0.0, 255.0)

        when (comparisonType) {
            ComparisonType.SSIM -> {
                val ssimMap = Mat()
                Imgproc.GaussianBlur(mat1, mat1, Size(11.0, 11.0), 1.5)
                Imgproc.GaussianBlur(mat2, mat2, Size(11.0, 11.0), 1.5)
                Core.absdiff(mat1, mat2, ssimMap)
                Imgproc.threshold(ssimMap, diff, 20.0, 255.0, Imgproc.THRESH_BINARY)
            }

            ComparisonType.AE -> {
                Core.absdiff(mat1, mat2, diff)
                Core.compare(diff, Scalar(thresholdValue), diff, Core.CMP_GT)
            }

            ComparisonType.MAE -> {
                Core.absdiff(mat1, mat2, diff)
                Core.divide(diff, Scalar(mat1.total().toDouble()), diff)
                Core.compare(diff, Scalar(thresholdValue), diff, Core.CMP_GT)
            }

            ComparisonType.NCC -> {
                val result = Mat()
                Imgproc.matchTemplate(mat1, mat2, result, Imgproc.TM_CCORR_NORMED)
                Core.absdiff(mat1, mat2, diff)
                Core.compare(diff, Scalar(thresholdValue), diff, Core.CMP_GT)
            }

            ComparisonType.PSNR -> {
                val mse = Mat()
                Core.absdiff(mat1, mat2, mse)
                Core.pow(mse, 2.0, mse)
                val meanMse = Core.mean(mse).`val`[0]
                val psnr = if (meanMse == 0.0) 100.0 else 10.0 * log10(255.0 * 255.0 / meanMse)
                Core.absdiff(mat1, mat2, diff)
                if (psnr > 30) Core.multiply(diff, Scalar(0.2), diff)
                Core.compare(diff, Scalar(thresholdValue), diff, Core.CMP_GT)
            }

            ComparisonType.RMSE -> {
                val mse = Mat()
                Core.absdiff(mat1, mat2, mse)
                Core.pow(mse, 2.0, mse)
                val meanMse = Core.mean(mse).`val`[0]
                val rmse = sqrt(meanMse)
                Core.absdiff(mat1, mat2, diff)
                if (rmse < 10) Core.multiply(diff, Scalar(0.2), diff)
                Core.compare(diff, Scalar(thresholdValue), diff, Core.CMP_GT)
            }
        }

        val mask = Mat()
        Imgproc.cvtColor(diff, mask, Imgproc.COLOR_BGR2GRAY)
        Imgproc.threshold(mask, mask, 1.0, 255.0, Imgproc.THRESH_BINARY)

        val result = mat1.clone()
        result.setTo(highlightColor.toScalar(), mask)

        val outputBitmap =
            Bitmap.createBitmap(bitmap1.width, bitmap1.height, Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(result, outputBitmap)

        return outputBitmap
    }

}