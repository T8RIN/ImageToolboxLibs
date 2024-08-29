package com.t8rin.awebp.decoder

import android.graphics.Bitmap
import com.github.penfeizhou.animation.decode.FrameSeqDecoder
import com.github.penfeizhou.animation.io.FileReader
import com.github.penfeizhou.animation.io.Reader
import com.github.penfeizhou.animation.io.StreamReader
import com.github.penfeizhou.animation.webp.decode.WebPDecoder
import java.io.Closeable
import java.io.File
import java.io.InputStream
import java.nio.ByteBuffer

class AnimatedWebpDecoder private constructor(
    private val decoder: WebPDecoder
) : AutoCloseable, Closeable {

    val frameCount: Int
        get() = decoder.frameCount

    fun getFrame(index: Int): Bitmap = decoder.getFrameBitmap(index)

    override fun close() = decoder.stop()

    class Builder(
        private val decoder: WebPDecoder
    ) {
        constructor(sourceInputStream: InputStream) : this(
            createDecoder(
                StreamReader(
                    sourceInputStream
                )
            )
        )

        constructor(sourceFile: File) : this(
            createDecoder(
                FileReader(sourceFile)
            )
        )

        fun build(): AnimatedWebpDecoder = AnimatedWebpDecoder(decoder).also {
            decoder.start()
        }
    }

    companion object {
        private fun createDecoder(reader: Reader): WebPDecoder = WebPDecoder(
            { reader },
            object : FrameSeqDecoder.RenderListener {
                override fun onStart() = Unit
                override fun onRender(byteBuffer: ByteBuffer?) = Unit
                override fun onEnd() = Unit
            }
        )
    }

}