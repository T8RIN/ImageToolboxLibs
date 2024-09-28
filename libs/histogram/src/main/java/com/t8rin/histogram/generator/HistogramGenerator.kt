package com.t8rin.histogram.generator

import android.graphics.Bitmap

internal object HistogramGenerator {

    external fun generate(bitmap: Bitmap): List<IntArray>

    init {
        System.loadLibrary("histogram")
    }

}