package com.jhlabs

import android.graphics.Bitmap
import com.jhlabs.util.AndroidUtils

interface JhFilter {
    fun filter(
        src: IntArray,
        w: Int,
        h: Int
    ): IntArray

    fun filter(image: Bitmap): Bitmap {
        val pixels = AndroidUtils.bitmapToIntArray(image.copy(Bitmap.Config.ARGB_8888, false))

        val result = filter(
            src = pixels,
            w = image.width,
            h = image.height
        )

        return Bitmap.createBitmap(result, image.width, image.height, Bitmap.Config.ARGB_8888)
    }
}