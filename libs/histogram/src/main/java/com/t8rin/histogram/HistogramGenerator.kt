package com.t8rin.histogram

import android.graphics.Bitmap

internal object HistogramGenerator {

    external fun generate(bitmap: Bitmap): List<FloatArray>

    init {
        System.loadLibrary("histogram")
    }

}