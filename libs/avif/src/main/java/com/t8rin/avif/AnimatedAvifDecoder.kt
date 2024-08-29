package com.t8rin.avif

import android.graphics.Bitmap
import com.github.penfeizhou.animation.avif.decode.AVIFDecoder
import com.github.penfeizhou.animation.decode.FrameSeqDecoder
import com.github.penfeizhou.animation.io.FileReader
import com.github.penfeizhou.animation.io.Reader
import java.io.Closeable
import java.io.File
import java.nio.ByteBuffer

class AnimatedAvifDecoder(
    sourceFile: File,
    private val onStart: (AnimatedAvifDecoder) -> Unit
) : AutoCloseable, Closeable {

    private val decoder: AVIFDecoder = createDecoder(
        FileReader(sourceFile)
    ).apply { start() }

    val frameCount: Int
        get() = decoder.frameCount

    fun getFrame(index: Int): Bitmap = decoder.getFrameBitmap(index)

    private fun createDecoder(
        reader: Reader
    ): AVIFDecoder = AVIFDecoder(
        { reader },
        object : FrameSeqDecoder.RenderListener {
            override fun onStart() {
                this@AnimatedAvifDecoder.use(onStart)
            }

            override fun onRender(byteBuffer: ByteBuffer?) = Unit
            override fun onEnd() = Unit
        }
    )

    override fun close() = decoder.stop()

}