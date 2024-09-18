package com.t8rin.histogram

import android.graphics.Bitmap
import com.t8rin.histogram.generator.HistogramGenerator
import kotlinx.coroutines.coroutineScope

data class Histogram(
    val redData: List<Double>,
    val greenData: List<Double>,
    val blueData: List<Double>,
    val brightnessData: List<Double>
) {
    companion object {
        val Empty by lazy {
            Histogram(
                redData = listOf(0.0, 0.0),
                greenData = listOf(0.0, 0.0),
                blueData = listOf(0.0, 0.0),
                brightnessData = listOf(0.0, 0.0)
            )
        }

        suspend fun from(
            bitmap: Bitmap
        ): Histogram = coroutineScope {
            val data = HistogramGenerator.generate(bitmap).map { floats ->
                floats.map { it.toDouble() }
            }

            val (redData, greenData, blueData, whiteData) = data

            Histogram(
                redData = redData,
                greenData = greenData,
                blueData = blueData,
                brightnessData = whiteData
            )
        }
    }
}