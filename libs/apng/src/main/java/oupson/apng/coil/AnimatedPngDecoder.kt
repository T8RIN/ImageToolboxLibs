package oupson.apng.coil

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
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
import java.io.ByteArrayInputStream

class AnimatedPngDecoder private constructor(
    private val source: ImageSource,
    private val options: Options,
    private val context: Context
) : Decoder {

    override suspend fun decode(): DecodeResult {
        val array = source.source().readByteArray()
        val inputStream = ByteArrayInputStream(array)

        val drawable: ApngDrawable? = ApngDecoder(
            input = inputStream,
            config = ApngDecoder.Config(
                bitmapConfig = options.config
            )
        ).decodeApng(context).getOrNull() as? ApngDrawable

        return if (drawable?.numberOfFrames == 1 || drawable == null) {
            DecodeResult(
                drawable = BitmapDrawable(
                    context.resources,
                    BitmapFactory.decodeByteArray(
                        array, 0, array.size
                    ).apply {
                        config = options.config
                    }.createScaledBitmap(options.size)
                ),
                isSampled = true
            )
        } else {
            DecodeResult(
                drawable = drawable,
                isSampled = false
            )
        }
    }

    private fun Bitmap.createScaledBitmap(
        size: Size
    ): Bitmap {
        if (size == Size.ORIGINAL) return this

        return flexibleResize(maxOf(size.width.pxOrElse { 1 }, size.height.pxOrElse { 1 }))
    }

    private fun Bitmap.flexibleResize(
        max: Int
    ): Bitmap {
        val image = this

        return runCatching {
            if (image.height >= image.width) {
                val aspectRatio = image.width.toDouble() / image.height.toDouble()
                val targetWidth = (max * aspectRatio).toInt()
                Bitmap.createScaledBitmap(image, targetWidth, max, true)
            } else {
                val aspectRatio = image.height.toDouble() / image.width.toDouble()
                val targetHeight = (max * aspectRatio).toInt()
                Bitmap.createScaledBitmap(image, max, targetHeight, true)
            }
        }.getOrNull() ?: image
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