package com.gemalto.jp2

import android.graphics.Bitmap


internal object Utils {
    @JvmField
    val JP2_RFC3745_MAGIC = byteArrayOf(
        0x00.toByte(),
        0x00.toByte(),
        0x00.toByte(),
        0x0c.toByte(),
        0x6a.toByte(),
        0x50.toByte(),
        0x20.toByte(),
        0x20.toByte(),
        0x0d.toByte(),
        0x0a.toByte(),
        0x87.toByte(),
        0x0a.toByte()
    )

    @JvmField
    val JP2_MAGIC = byteArrayOf(0x0d.toByte(), 0x0a.toByte(), 0x87.toByte(), 0x0a.toByte())

    @JvmField
    val J2K_CODESTREAM_MAGIC =
        byteArrayOf(0xff.toByte(), 0x4f.toByte(), 0xff.toByte(), 0x51.toByte())

    fun Bitmap.flexibleResize(
        max: Int?
    ): Bitmap {
        val image = this

        if (max == null) return image

        return runCatching {
            if (image.height >= image.width) {
                val aspectRatio = image.width.toDouble() / image.height.toDouble()
                val targetWidth = (max * aspectRatio).toInt()
                Bitmap.createScaledBitmap(image, targetWidth, max, true)
            } else {
                val aspectRatio = image.height.toDouble() / image.width.toDouble()
                val targetHeight = (max * aspectRatio).toInt()
                Bitmap.createScaledBitmap(image, max, targetHeight, true)
            }
        }.getOrNull() ?: image
    }
}