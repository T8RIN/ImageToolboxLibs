package oupson.apng.coil

import android.content.Context
import coil.ImageLoader
import coil.decode.DecodeResult
import coil.decode.Decoder
import coil.decode.ImageSource
import coil.fetch.SourceResult
import coil.request.Options
import okio.BufferedSource
import oupson.apng.decoder.ApngDecoder
import oupson.apng.utils.Utils

class AnimatedPngDecoder private constructor(
    private val source: ImageSource,
    private val context: Context
) : Decoder {

    override suspend fun decode(): DecodeResult? {
        val drawable = ApngDecoder(
            file = source.file().toFile()
        ).decodeApng(context).getOrNull() ?: return null

        return DecodeResult(
            drawable = drawable,
            isSampled = false
        )
    }

    class Factory(
        private val context: Context
    ) : Decoder.Factory {

        override fun create(
            result: SourceResult,
            options: Options,
            imageLoader: ImageLoader
        ): Decoder? = if (isAPNG(result.source.source())) {
            AnimatedPngDecoder(
                source = result.source,
                context = context
            )
        } else null
        
        private fun isAPNG(source: BufferedSource): Boolean {
            val buffer = source.buffer.copy()
            val array = buffer.readByteArray()
            return Utils.isApng(array)
        }
    }
}