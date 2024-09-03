package com.t8rin.djvu_coder

import android.graphics.Bitmap
import java.io.File


class DJVUDecoder(private val file: File) {

    /**
     * @return the decoded image; `null` in case of an error
     */
    fun decode(
        page: Int,
        dpi: Int = 300
    ): Bitmap? = decodeDJVUFile(
        path = file.absolutePath,
        page = page,
        dpi = dpi
    )

    private external fun decodeDJVUFile(
        path: String,
        page: Int,
        dpi: Int
    ): Bitmap?

    companion object {
        init {
            System.loadLibrary("djvu-coder")
        }
    }
}