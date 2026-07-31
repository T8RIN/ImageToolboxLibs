package com.t8rin.histogram.generator

import android.graphics.Bitmap
import java.nio.ByteBuffer

internal object HistogramGenerator {

    external fun generate(bitmap: Bitmap): List<IntArray>

    external fun generateScope(
        bitmap: Bitmap,
        type: Int,
        size: Int,
        outputBuffer: ByteBuffer
    ): Boolean

    init {
        System.loadLibrary("histogram")
    }

}
