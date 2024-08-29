package com.t8rin.avif

import android.graphics.Bitmap
import com.github.penfeizhou.animation.avif.decode.AVIFDecoder
import com.github.penfeizhou.animation.decode.FrameSeqDecoder
import com.github.penfeizhou.animation.io.FileReader
import com.github.penfeizhou.animation.io.Reader
import com.github.penfeizhou.animation.io.StreamReader
import java.io.Closeable
import java.io.File
import java.io.InputStream
import java.nio.ByteBuffer

class AnimatedAvifDecoder private constructor(
    private val decoder: AVIFDecoder
) : AutoCloseable, Closeable {

    val frameCount: Int
        get() = decoder.frameCount

    fun getFrame(index: Int): Bitmap = decoder.getFrameBitmap(index)

    class Builder(
        private val decoder: AVIFDecoder
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

        fun build(): AnimatedAvifDecoder = AnimatedAvifDecoder(decoder).also {
            decoder.start()
        }
    }

    companion object {
        private fun createDecoder(reader: Reader): AVIFDecoder = AVIFDecoder(
            { reader },
            object : FrameSeqDecoder.RenderListener {
                override fun onStart() = Unit
                override fun onRender(byteBuffer: ByteBuffer?) = Unit
                override fun onEnd() = Unit
            }
        )
    }

    override fun close() = decoder.stop()

}