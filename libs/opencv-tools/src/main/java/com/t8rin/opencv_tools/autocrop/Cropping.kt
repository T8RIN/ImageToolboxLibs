package com.t8rin.opencv_tools.autocrop

import android.graphics.Bitmap
import com.t8rin.opencv_tools.autocrop.model.CropEdges
import com.t8rin.opencv_tools.autocrop.model.CropParameters
import org.opencv.android.OpenCVLoader
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc


object AutoCropper {

    init {
        OpenCVLoader.initDebug()
    }

    fun crop(
        bitmap: Bitmap,
        @CropSensitivity sensitivity: Int
    ): Bitmap? = bitmap.findEdges(sensitivity)?.run {
        bitmap.autoCropImage(edges)
    }

}


private fun Bitmap.autoCropImage(
    edges: CropEdges
): Bitmap = Bitmap.createBitmap(
    this,
    0,
    edges.top + 1,
    width,
    edges.height - 1
)

private fun Bitmap.findEdges(
    @CropSensitivity sensitivity: Int
): CropParameters? {
    val matRGBA = getMat()
    return getEdgeCandidates(matRGBA, sensitivity)?.let {
        CropParameters(
            edges = getMaxScoreCropEdges(candidates = it, matRGBA = matRGBA),
            candidates = it
        )
    }
}

private fun getEdgeCandidates(matRGBA: Mat, @CropSensitivity sensitivity: Int): List<Int>? {
    // Convert to gray scale
    val matGrayScale = Mat()
    Imgproc.cvtColor(matRGBA, matGrayScale, Imgproc.COLOR_RGBA2GRAY)

    // Get canny edge detected matrix
    val matCanny = Mat()
    Imgproc.Canny(matGrayScale, matCanny, 100.0, 200.0)

    // Convert sensitivity to threshold
    val threshold = edgeCandidateThreshold(sensitivity)

    return (0 until matCanny.rows()).filter { i ->
        matCanny.row(i).singleChannelMean() > threshold
    }
        .run {
            if (isEmpty())
                null
            else
                listOf(0) + this + listOf(matCanny.rows())
        }
}

private fun getMaxScoreCropEdges(candidates: List<Int>, matRGBA: Mat): CropEdges {
    val matSobel = Mat()
    Imgproc.Sobel(matRGBA, matSobel, CvType.CV_16U, 2, 2, 5)

    var maxScore = 0f
    var maxScoreEdges: CropEdges? = null

    candidates.windowed(2)
        .map { CropEdges(it) }
        .forEach { edges ->
            val cropAreaMean: Float = matSobel.getCrop(edges).multiChannelMean().toFloat()
            val heightPortion: Float = edges.height.toFloat() / matSobel.rows().toFloat()
            val score: Float = cropAreaMean * heightPortion

            if (score > maxScore) {
                maxScore = score
                maxScoreEdges = edges
            }
        }

    return maxScoreEdges!!
}