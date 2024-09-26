package com.t8rin.opencv_tools.autocrop

import android.graphics.Bitmap
import com.t8rin.opencv_tools.autocrop.model.CropEdges
import org.opencv.android.Utils
import org.opencv.core.Core
import org.opencv.core.Mat

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