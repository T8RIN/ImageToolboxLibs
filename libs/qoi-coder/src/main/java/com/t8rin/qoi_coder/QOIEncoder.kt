package com.t8rin.qoi_coder

import android.graphics.Bitmap
import kotlinx.coroutines.runInterruptible

class QOIEncoder(private val bmp: Bitmap) {

    suspend fun encode(): ByteArray = runInterruptible {
        encodeQOIBitmap(bmp)
    }

    private external fun encodeQOIBitmap(
        bitmap: Bitmap
    ): ByteArray

    companion object {
        init {
            System.loadLibrary("qoi-coder")
        }
    }
}
