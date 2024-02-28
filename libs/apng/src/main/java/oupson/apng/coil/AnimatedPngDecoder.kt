package oupson.apng.coil

import android.content.Context
import android.graphics.Bitmap
import coil.ImageLoader
import coil.decode.DecodeResult
import coil.decode.Decoder
import coil.decode.ImageSource
import coil.fetch.SourceResult
import coil.request.Options
import coil.size.Size
import coil.size.pxOrElse
import okio.BufferedSource
import okio.ByteString.Companion.toByteString
import oupson.apng.decoder.ApngDecoder
import oupson.apng.drawable.ApngDrawable
import oupson.apng.utils.Utils
import oupson.apng.utils.Utils.flexibleResize
import java.io.ByteArrayInputStream

class AnimatedPngDecoder private constructor(
    private val source: ImageSource,
    private val options: Options,
    private val context: Context
) : Decoder {

    override suspend fun decode(): DecodeResult? {
        val array = source.source().readByteArray()
        val inputStream = ByteArrayInputStream(array)

        val drawable: ApngDrawable = ApngDecoder(
            input = inputStream,
            config = ApngDecoder.Config(
                bitmapConfig = options.config,
                width = if (options.size == Size.ORIGINAL) null
                else options.size.width.pxOrElse { 1 },
                height = if (options.size == Size.ORIGINAL) null
                else options.size.height.pxOrElse { 1 }
            )
        ).decodeApng(context).getOrNull() as? ApngDrawable ?: return null

        return DecodeResult(
            drawable = drawable,
            isSampled = options.size != Size.ORIGINAL
        )
    }

    private fun Bitmap.createScaledBitmap(
        size: Size
    ): Bitmap {
        if (size == Size.ORIGINAL) return this

        return flexibleResize(
            maxOf(
                size.width.pxOrElse { 1 },
                size.height.pxOrElse { 1 }
            )
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
                options = options,
                context = context
            )
        } else null

        private fun isAPNG(source: BufferedSource): Boolean {
            return source.rangeEquals(0, Utils.pngSignature.toByteString())
        }
    }
}