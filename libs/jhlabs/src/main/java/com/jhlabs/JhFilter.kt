package com.jhlabs

import android.graphics.Bitmap
import kotlinx.coroutines.coroutineScope

interface JhFilter {
    suspend fun filter(
        image: Bitmap
    ): Bitmap = coroutineScope {
        NativeJhlabs.filter(image, this@JhFilter)
    }
}

private object NativeJhlabs {
    external fun filter(bitmap: Bitmap, filter: JhFilter): Bitmap

    init {
        System.loadLibrary("jhlabs")
    }
}