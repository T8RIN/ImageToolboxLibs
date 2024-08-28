package com.t8rin.awebp.encoder

import android.graphics.Bitmap
import com.github.penfeizhou.animation.io.FileReader
import com.github.penfeizhou.animation.io.StreamReader
import java.io.File
import java.io.InputStream

class AnimatedWebpEncoder(
    private val quality: Int,
    private val loopCount: Int,
    private val backgroundColor: Int
) {
    private var isGif: Boolean = false

    private var encoder: WebpEncoder = WebpEncoder()

    fun loadGif(gifFile: File): AnimatedWebpEncoder = apply {
        isGif = true
        encoder = WebpEncoder.fromGif { FileReader(gifFile) }
    }

    fun loadGif(gifInputStream: InputStream): AnimatedWebpEncoder = apply {
        isGif = true
        encoder = WebpEncoder.fromGif { StreamReader(gifInputStream) }
    }

    fun addFrame(
        bitmap: Bitmap,
        duration: Int
    ): AnimatedWebpEncoder = apply {
        isGif = false
        encoder.addFrame(
            WebpEncoder.FrameBuilder()
                .bitmap(bitmap)
                .offsetX(0)
                .offsetY(0)
                .duration(duration)
                .disposal(true)
                .blending(true)
                .build()
        )
    }

    fun encode(): ByteArray = encoder
        .apply {
            if (!isGif) {
                loopCount(loopCount)
                backgroundColor(backgroundColor)
            }
        }
        .quality(quality)
        .build()

}