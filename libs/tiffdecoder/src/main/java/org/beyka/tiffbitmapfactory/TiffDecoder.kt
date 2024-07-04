package org.beyka.tiffbitmapfactory

import android.content.Context
import android.graphics.Bitmap
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

class TiffDecoder private constructor(
    private val source: ImageSource,
    private val options: Options,
    private val context: Context
) : Decoder {

    override suspend fun decode(): DecodeResult {
        val config = options.config.takeIf {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                it != Bitmap.Config.HARDWARE
            } else true
        } ?: Bitmap.Config.ARGB_8888

        val drawable = BitmapDrawable(
            context.resources,
            TiffBitmapFactory.decodeFile(
                source.file().toFile()
            )?.createScaledBitmap(options.size)
                ?.copy(config, false)
        )

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
        ): Decoder? {
            return if (isTiff(result.source.source())) {
                TiffDecoder(
                    source = result.source,
                    options = options,
                    context = context
                )
            } else null
        }

        private fun isTiff(source: BufferedSource): Boolean {
            val magic1 = byteArrayOf(0x49, 0x49, 0x2a, 0x00)
            val magic2 = byteArrayOf(0x4d, 0x4d, 0x00, 0x2a)

            if (source.rangeEquals(0, magic1.toByteString())) return true
            return source.rangeEquals(0, magic2.toByteString())
        }
    }
}

private fun Bitmap.flexibleResize(
    max: Int?
): Bitmap {
    val image = this

    if (max == null) return image

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