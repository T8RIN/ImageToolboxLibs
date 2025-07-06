package com.t8rin.qoi_coder

import android.graphics.Bitmap

class QOIEncoder(private val bmp: Bitmap) {

    fun encode(): ByteArray = encodeQOIBitmap(bmp)

    private external fun encodeQOIBitmap(
        bitmap: Bitmap
    ): ByteArray

    companion object {
        init {
            System.loadLibrary("qoi-coder")
        }
    }
}
