package com.t8rin.djvu_coder

import android.graphics.Bitmap
import androidx.core.graphics.scale


internal object Utils {

    val DJVU_MAGIC = "AT&TFORM".toByteArray()

    fun Bitmap.flexibleResize(
        max: Int?
    ): Bitmap {
        val image = this

        if (max == null) return image

        return runCatching {
            if (image.height >= image.width) {
                val aspectRatio = image.width.toDouble() / image.height.toDouble()
                val targetWidth = (max * aspectRatio).toInt()
                image.scale(targetWidth, max)
            } else {
                val aspectRatio = image.height.toDouble() / image.width.toDouble()
                val targetHeight = (max * aspectRatio).toInt()
                image.scale(max, targetHeight)
            }
        }.getOrNull() ?: image
    }
}