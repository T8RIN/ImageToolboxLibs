package com.t8rin.avif.encoder

import android.content.Context
import android.graphics.Bitmap
import androidx.annotation.RequiresApi
import androidx.heifwriter.HeifWriter
import java.io.File

@RequiresApi(28)
class AnimatedHeifEncoder(
    context: Context,
    width: Int,
    height: Int,
    quality: Int,
) {
    private val buffer = File(context.cacheDir, "file.heif")

    private val encoder: HeifWriter = HeifWriter.Builder(
        buffer.absolutePath, width, height, HeifWriter.INPUT_MODE_BITMAP
    ).setQuality(quality).build()

    init {
        encoder.start()
    }

    fun addFrame(
        bitmap: Bitmap
    ): AnimatedHeifEncoder = apply {
        encoder.addBitmap(bitmap)
    }

    fun encode(): ByteArray {
        encoder.stop(0L)

        return buffer.readBytes()
    }

}