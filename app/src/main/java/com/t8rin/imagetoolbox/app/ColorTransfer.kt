package com.t8rin.imagetoolbox.app

import android.graphics.Bitmap
import androidx.core.graphics.ColorUtils
import kotlin.math.pow
import kotlin.math.sqrt


fun colorTransfer(source: Bitmap, target: Bitmap, intensity: Float = 0.5f): Bitmap {
    val result = Bitmap.createBitmap(target.width, target.height, target.config)

    val sourceMean = DoubleArray(3)
    val sourceStd = DoubleArray(3)
    calculateMeanAndStdLAB(
        mean = sourceMean,
        std = sourceStd,
        bitmap = source
    )

    val targetMean = DoubleArray(3)
    val targetStd = DoubleArray(3)
    calculateMeanAndStdLAB(
        mean = targetMean,
        std = targetStd,
        bitmap = target
    )

    for (x in 0 until target.width) {
        for (y in 0 until target.height) {
            val targetPixel = target.getPixel(x, y)

            val lab = DoubleArray(3)
            ColorUtils.colorToLAB(targetPixel, lab)

            val newL = ((lab[0] - targetMean[0]) * (sourceStd[0] / targetStd[0]) + sourceMean[0])
            val newA = ((lab[1] - targetMean[1]) * (sourceStd[1] / targetStd[1]) + sourceMean[1])
            val newB = ((lab[2] - targetMean[2]) * (sourceStd[2] / targetStd[2]) + sourceMean[2])

            val finalL = lab[0] + intensity * (newL - lab[0])
            val finalA = lab[1] + intensity * (newA - lab[1])
            val finalB = lab[2] + intensity * (newB - lab[2])

            val newColor = ColorUtils.LABToColor(finalL, finalA, finalB)
            result.setPixel(x, y, newColor)
        }
    }

    return result
}


private fun calculateMeanAndStdLAB(mean: DoubleArray, std: DoubleArray, bitmap: Bitmap) {
    val pixelCount = bitmap.width * bitmap.height

    for (x in 0 until bitmap.width) {
        for (y in 0 until bitmap.height) {
            val pixel = bitmap.getPixel(x, y)
            val lab = DoubleArray(3)
            ColorUtils.colorToLAB(pixel, lab)
            mean[0] += lab[0]
            mean[1] += lab[1]
            mean[2] += lab[2]
        }
    }
    mean[0] /= pixelCount.toDouble()
    mean[1] /= pixelCount.toDouble()
    mean[2] /= pixelCount.toDouble()

    for (x in 0 until bitmap.width) {
        for (y in 0 until bitmap.height) {
            val pixel = bitmap.getPixel(x, y)
            val lab = DoubleArray(3)
            ColorUtils.colorToLAB(pixel, lab)
            std[0] += (lab[0] - mean[0]).pow(2.0)
            std[1] += (lab[1] - mean[1]).pow(2.0)
            std[2] += (lab[2] - mean[2]).pow(2.0)
        }
    }
    std[0] = sqrt(std[0] / pixelCount)
    std[1] = sqrt(std[1] / pixelCount)
    std[2] = sqrt(std[2] / pixelCount)
}