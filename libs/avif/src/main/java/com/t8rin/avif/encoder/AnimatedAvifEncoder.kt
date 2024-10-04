package com.t8rin.avif.encoder

import android.content.Context
import android.graphics.Bitmap
import androidx.annotation.RequiresApi
import androidx.heifwriter.AvifWriter
import java.io.File

@RequiresApi(28)
class AnimatedAvifEncoder(
    context: Context,
    width: Int,
    height: Int,
    quality: Int,
    highBitDepthEnabled: Boolean
) {
    private val buffer = File(context.cacheDir, "file.avif")

    private val encoder: AvifWriter = AvifWriter.Builder(
        buffer.absolutePath, width, height, AvifWriter.INPUT_MODE_BITMAP
    ).setHighBitDepthEnabled(highBitDepthEnabled).setQuality(quality).build()

    init {
        encoder.start()
    }

    fun addFrame(
        bitmap: Bitmap
    ): AnimatedAvifEncoder = apply {
        encoder.addBitmap(bitmap)
    }

    fun encode(): ByteArray {
        encoder.stop(0L)

        return buffer.readBytes()
    }

}