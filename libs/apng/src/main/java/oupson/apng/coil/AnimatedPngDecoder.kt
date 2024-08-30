package oupson.apng.coil

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.Build
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
import oupson.apng.utils.Utils
import oupson.apng.utils.Utils.flexibleResize
import java.io.ByteArrayInputStream

class AnimatedPngDecoder private constructor(
    private val source: ImageSource,
    private val options: Options
) : Decoder {

    override suspend fun decode(): DecodeResult {
        val array = source.source().readByteArray()
        val inputStream = ByteArrayInputStream(array)

        val isApng = Utils.isApng(array)

        val config = options.config.takeIf {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                it != Bitmap.Config.HARDWARE
            } else true
        } ?: Bitmap.Config.ARGB_8888

        val bitmapDrawable = {
            BitmapDrawable(
                options.context.resources,
                BitmapFactory
                    .decodeByteArray(
                        array, 0,
                        array.size
                    )
                    .createScaledBitmap(options.size)
                    .copy(config, false)
            )
        }

        val drawable = if (isApng) {
            ApngDecoder(
                input = inputStream,
                config = ApngDecoder.Config(
                    bitmapConfig = config,
                    width = if (options.size == Size.ORIGINAL) null
                    else options.size.width.pxOrElse { 1 },
                    height = if (options.size == Size.ORIGINAL) null
                    else options.size.height.pxOrElse { 1 }
                )
            ).decodeApng(options.context).getOrNull() ?: bitmapDrawable()
        } else bitmapDrawable()

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

    class Factory : Decoder.Factory {

        override fun create(
            result: SourceResult,
            options: Options,
            imageLoader: ImageLoader
        ): Decoder? = if (isAPNG(result.source.source())) {
            AnimatedPngDecoder(
                source = result.source,
                options = options
            )
        } else null

        private fun isAPNG(source: BufferedSource): Boolean {
            return source.rangeEquals(0, Utils.pngSignature.toByteString())
        }
    }
}