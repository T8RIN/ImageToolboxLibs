package com.jhlabs

import android.graphics.Bitmap
import com.jhlabs.util.AndroidUtils
import kotlinx.coroutines.coroutineScope

interface JhFilter {
    fun filter(
        src: IntArray,
        w: Int,
        h: Int
    ): IntArray

    suspend fun filter(
        image: Bitmap
    ): Bitmap = coroutineScope {
        filter(
            src = image.pixels,
            w = image.width,
            h = image.height
        ).bitmap(
            width = image.width,
            height = image.height
        )
    }
}

private val Bitmap.pixels: IntArray
    get() = AndroidUtils.bitmapToIntArray(copy(Bitmap.Config.ARGB_8888, false))

private fun IntArray.bitmap(
    width: Int, height: Int
): Bitmap = Bitmap.createBitmap(this, width, height, Bitmap.Config.ARGB_8888)