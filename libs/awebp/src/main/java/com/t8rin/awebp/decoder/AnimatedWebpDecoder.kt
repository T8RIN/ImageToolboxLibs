package com.t8rin.awebp.decoder

import android.graphics.Bitmap
import com.github.penfeizhou.animation.decode.FrameSeqDecoder
import com.github.penfeizhou.animation.io.FileReader
import com.github.penfeizhou.animation.io.Reader
import com.github.penfeizhou.animation.webp.decode.WebPDecoder
import java.io.Closeable
import java.io.File
import java.nio.ByteBuffer

class AnimatedWebpDecoder(
    sourceFile: File,
    private val onStart: (AnimatedWebpDecoder) -> Unit
) : AutoCloseable, Closeable {

    private val decoder: WebPDecoder = createDecoder(
        FileReader(sourceFile)
    ).apply { start() }

    val frameCount: Int
        get() = decoder.frameCount

    fun getFrame(index: Int): Bitmap = decoder.getFrameBitmap(index)

    private fun createDecoder(
        reader: Reader
    ): WebPDecoder = WebPDecoder(
        { reader },
        object : FrameSeqDecoder.RenderListener {
            override fun onStart() {
                this@AnimatedWebpDecoder.use(onStart)
            }

            override fun onRender(byteBuffer: ByteBuffer?) = Unit
            override fun onEnd() = Unit
        }
    )

    override fun close() = decoder.stop()

}