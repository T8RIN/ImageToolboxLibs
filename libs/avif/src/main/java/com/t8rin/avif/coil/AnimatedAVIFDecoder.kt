package com.t8rin.avif.coil

import coil3.ImageLoader
import coil3.asImage
import coil3.decode.DecodeResult
import coil3.decode.Decoder
import coil3.decode.ImageSource
import coil3.fetch.SourceFetchResult
import coil3.request.Options
import com.github.penfeizhou.animation.avif.AVIFDrawable
import com.github.penfeizhou.animation.avif.decode.AVIFParser
import com.github.penfeizhou.animation.loader.ByteBufferLoader
import kotlinx.coroutines.runInterruptible
import java.nio.ByteBuffer

class AnimatedAVIFDecoder private constructor(
    private val source: ImageSource,
    private val options: Options,
) : Decoder {

    override suspend fun decode() = runInterruptible {
        val byteArray = source.source().readByteArray()
        val byteBuffer = ByteBuffer.allocateDirect(byteArray.size).put(byteArray)
        val drawable = AVIFDrawable(
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
            if (!AVIFParser.isAVIF(result.source.file().toFile().absolutePath)) return null

            return AnimatedAVIFDecoder(result.source, options)
        }

    }

}