package com.jhlabs

import android.graphics.Bitmap
import kotlinx.coroutines.runInterruptible

interface JhFilter {
    suspend fun filter(
        image: Bitmap
    ): Bitmap = runInterruptible {
        NativeJhlabs.filter(image, this@JhFilter)
    }
}

private object NativeJhlabs {
    external fun filter(bitmap: Bitmap, filter: JhFilter): Bitmap

    init {
        System.loadLibrary("jhlabs")
    }
}