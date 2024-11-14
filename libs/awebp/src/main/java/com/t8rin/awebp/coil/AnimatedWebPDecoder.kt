package com.t8rin.awebp.coil

import coil3.ImageLoader
import coil3.asImage
import coil3.decode.DecodeResult
import coil3.decode.Decoder
import coil3.decode.ImageSource
import coil3.fetch.SourceFetchResult
import coil3.request.Options
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
            image = drawable.asImage(),
            isSampled = false
        )
    }


    class Factory : Decoder.Factory {

        override fun create(
            result: SourceFetchResult,
            options: Options,
            imageLoader: ImageLoader
        ): Decoder? {
            if (!WebPParser.isAWebP(result.source.file().toFile().absolutePath)) return null

            return AnimatedWebPDecoder(result.source, options)
        }

    }

}