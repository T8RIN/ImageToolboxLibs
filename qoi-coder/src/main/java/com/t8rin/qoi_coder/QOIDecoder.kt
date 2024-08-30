package com.t8rin.qoi_coder

import android.graphics.Bitmap


class QOIDecoder(private val data: ByteArray) {

    /**
     * @return the decoded image; `null` in case of an error
     */
    fun decode(): Bitmap? {
        return decodeJP2ByteArray(data)
    }

    private external fun decodeJP2ByteArray(
        data: ByteArray
    ): Bitmap?

    companion object {
        init {
            System.loadLibrary("qoi-coder")
        }
    }
}
