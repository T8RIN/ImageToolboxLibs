package com.t8rin.qoi_coder

import android.graphics.Bitmap

class QOIEncoder(private val bmp: Bitmap) {


    fun encode(): ByteArray {
        return encodeInternal(bmp)
    }

    private fun encodeInternal(bmp: Bitmap): ByteArray {
        return encodeQOIBitmap(bmp)
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
