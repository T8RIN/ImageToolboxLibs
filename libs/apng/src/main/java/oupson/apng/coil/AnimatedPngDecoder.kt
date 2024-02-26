package oupson.apng.coil

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import coil.ImageLoader
import coil.decode.DecodeResult
import coil.decode.Decoder
import coil.decode.ImageSource
import coil.fetch.SourceResult
import coil.request.Options
import okio.BufferedSource
import okio.ByteString.Companion.toByteString
import oupson.apng.decoder.ApngDecoder
import oupson.apng.drawable.ApngDrawable
import oupson.apng.utils.Utils
import java.io.ByteArrayInputStream

class AnimatedPngDecoder private constructor(
    private val source: ImageSource,
    private val context: Context
) : Decoder {

    override suspend fun decode(): DecodeResult? {
        val array = source.source().readByteArray()
        val inputStream = ByteArrayInputStream(array)

        val drawable: ApngDrawable = ApngDecoder(
            input = inputStream
        ).decodeApng(context).getOrNull() as? ApngDrawable ?: return null
        return if (drawable.numberOfFrames == 1) DecodeResult(
            drawable = BitmapDrawable(
                context.resources,
                BitmapFactory.decodeByteArray(
                    array, 0, array.size
                )
            ),
            false
        ) else {
            DecodeResult(
                drawable = drawable,
                isSampled = false
            )
        }
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
            return source.rangeEquals(0, Utils.pngSignature.toByteString())
        }
    }
}