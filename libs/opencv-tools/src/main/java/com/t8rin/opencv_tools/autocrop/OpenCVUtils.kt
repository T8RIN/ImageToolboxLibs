package com.t8rin.opencv_tools.autocrop

import android.graphics.Bitmap
import com.t8rin.opencv_tools.autocrop.model.CropEdges
import org.opencv.android.Utils
import org.opencv.core.Core
import org.opencv.core.Mat
import org.opencv.core.MatOfDouble

internal fun Bitmap.getMat(): Mat {
    val mat = Mat()
    Utils.bitmapToMat(this, mat)
    return mat
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

/**
 * Mean & StdDev
 */

internal fun Mat.getMeanStdDevMats(): Pair<MatOfDouble, MatOfDouble> {
    val mean = MatOfDouble()
    val stdDev = MatOfDouble()
    Core.meanStdDev(this, mean, stdDev)

    return mean to stdDev
}

/**
 * MinMax
 */

internal fun Mat.getMinMaxElements(): Pair<Double, Double> =
    when (channels()) {
        1 -> getSingleChannelMinMaxElements()
        else -> getMultiChannelMinMaxElements()
    }

internal fun Mat.getSingleChannelMinMaxElements(): Pair<Double, Double> =
    Core.minMaxLoc(this).run {
        minVal to maxVal
    }

internal fun Mat.getMultiChannelMinMaxElements(): Pair<Double, Double> {
    var min = Double.POSITIVE_INFINITY
    var max = Double.NEGATIVE_INFINITY

    (0 until rows()).forEach { i ->
        (0 until cols()).forEach { j ->
            get(i, j).forEach { element ->
                if (element < min)
                    min = element
                if (element > max)
                    max = element
            }
        }
    }

    return min to max
}