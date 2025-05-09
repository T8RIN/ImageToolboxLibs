package com.t8rin.tiff

import android.graphics.Bitmap
import android.os.Build
import androidx.core.graphics.scale
import coil3.ImageLoader
import coil3.asImage
import coil3.decode.DecodeResult
import coil3.decode.Decoder
import coil3.decode.ImageSource
import coil3.fetch.SourceFetchResult
import coil3.request.Options
import coil3.request.bitmapConfig
import coil3.size.Size
import coil3.size.pxOrElse
import okio.BufferedSource
import okio.ByteString.Companion.toByteString
import org.beyka.tiffbitmapfactory.TiffBitmapFactory

class TiffDecoder private constructor(
    private val source: ImageSource,
    private val options: Options
) : Decoder {

    override suspend fun decode(): DecodeResult? {
        val config = options.bitmapConfig.takeIf {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                it != Bitmap.Config.HARDWARE
            } else true
        } ?: Bitmap.Config.ARGB_8888

        val decoded = TiffBitmapFactory.decodeFile(
            source.file().toFile()
        ) ?: return null

        val image = decoded
            .createScaledBitmap(options.size)
            .copy(config, false)
            .asImage()

        return DecodeResult(
            image = image,
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
            result: SourceFetchResult,
            options: Options,
            imageLoader: ImageLoader
        ): Decoder? {
            return if (isTiff(result.source.source())) {
                TiffDecoder(
                    source = result.source,
                    options = options
                )
            } else null
        }

        private fun isTiff(source: BufferedSource): Boolean {
            val magic1 = byteArrayOf(0x49, 0x49, 0x2a, 0x00)
            val magic2 = byteArrayOf(0x4d, 0x4d, 0x00, 0x2a)
            val cr2Magic = byteArrayOf(0x49, 0x49, 0x2a, 0x00, 0x10, 0x00, 0x00, 0x00, 0x43, 0x52)
            val dngMagic = byteArrayOf(0x49, 0x49, 0x2a, 0x00, 0x08)

            if (source.rangeEquals(0, cr2Magic.toByteString())) return false
            if (source.rangeEquals(0, dngMagic.toByteString())) return false
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
            image.scale(targetWidth, max)
        } else {
            val aspectRatio = image.height.toDouble() / image.width.toDouble()
            val targetHeight = (max * aspectRatio).toInt()
            image.scale(max, targetHeight)
        }
    }.getOrNull() ?: image
}