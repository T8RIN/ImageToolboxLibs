package com.t8rin.awebp.coil

import coil.ImageLoader
import coil.decode.DecodeResult
import coil.decode.Decoder
import coil.decode.ImageSource
import coil.fetch.SourceResult
import coil.request.Options
import com.github.penfeizhou.animation.loader.ByteBufferLoader
import com.github.penfeizhou.animation.webp.WebPDrawable
import com.github.penfeizhou.animation.webp.decode.WebPParser
import kotlinx.coroutines.runInterruptible
import java.nio.ByteBuffer

class AnimatedWebPDecoder private constructor(
    private val source: ImageSource,
    private val options: Options,
) : Decoder {

    override suspend fun decode() = runInterruptible {
        val byteArray = source.source().readByteArray()
        val byteBuffer = ByteBuffer.allocateDirect(byteArray.size).put(byteArray)
        val drawable = WebPDrawable(
            object : ByteBufferLoader() {
                override fun getByteBuffer(): ByteBuffer {
                    byteBuffer.position(0)
                    return byteBuffer
                }
            }
        )
        DecodeResult(
            drawable = drawable,
            isSampled = false
        )
    }


    class Factory : Decoder.Factory {

        override fun create(
            result: SourceResult,
            options: Options,
            imageLoader: ImageLoader
        ): Decoder? {
            if (!WebPParser.isAWebP(result.source.file().toFile().absolutePath)) return null

            return AnimatedWebPDecoder(result.source, options)
        }

    }

}