package com.t8rin.awebp

import android.graphics.Bitmap
import com.github.penfeizhou.animation.awebpencoder.WebPEncoder
import com.github.penfeizhou.animation.decode.FrameSeqDecoder
import com.github.penfeizhou.animation.gif.decode.GifDecoder
import com.github.penfeizhou.animation.io.FileReader
import com.github.penfeizhou.animation.io.StreamReader
import java.io.File
import java.io.InputStream
import java.nio.ByteBuffer

class AnimatedWebpEncoder(
    private val quality: Int,
    private val loopCount: Int,
    private val backgroundColor: Int
) {
    private var encoder: WebPEncoder = WebPEncoder()

    fun loadGif(gifFile: File): AnimatedWebpEncoder = apply {
        encoder = WebPEncoder.fromDecoder(
            GifDecoder(
                { FileReader(gifFile) },
                object : FrameSeqDecoder.RenderListener {
                    override fun onStart() = Unit
                    override fun onRender(byteBuffer: ByteBuffer?) = Unit
                    override fun onEnd() = Unit
                }
            )
        )
    }

    fun loadGif(gifInputStream: InputStream): AnimatedWebpEncoder = apply {
        encoder = WebPEncoder.fromDecoder(
            GifDecoder(
                { StreamReader(gifInputStream) },
                object : FrameSeqDecoder.RenderListener {
                    override fun onStart() = Unit
                    override fun onRender(byteBuffer: ByteBuffer?) = Unit
                    override fun onEnd() = Unit
                }
            )
        )
    }

    fun addFrame(
        bitmap: Bitmap,
        duration: Int
    ): AnimatedWebpEncoder = apply {
        encoder.addFrame(
            bitmap,
            0,
            0,
            duration
        )
    }

    fun encode(): ByteArray = encoder
        .quality(quality)
        .loopCount(loopCount)
        .backgroundColor(backgroundColor)
        .build()

}