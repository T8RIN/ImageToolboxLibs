package com.t8rin.histogram

import android.graphics.Bitmap

internal object Histogram {

    external fun generateHistogram(bitmap: Bitmap): List<FloatArray>

    init {
        System.loadLibrary("histogram")
    }

}