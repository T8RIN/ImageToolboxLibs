package com.t8rin.djvu_coder.coil

import android.graphics.Bitmap
import android.os.Build
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
import com.t8rin.djvu_coder.DJVUDecoder
import com.t8rin.djvu_coder.Utils.DJVU_MAGIC
import com.t8rin.djvu_coder.Utils.flexibleResize
import okio.BufferedSource
import okio.ByteString.Companion.toByteString

class DjvuDecoder private constructor(
    private val source: ImageSource,
    private val options: Options,
    private val page: Int
) : Decoder {

    override suspend fun decode(): DecodeResult? {
        val file = source.file().toFile()

        val config = options.bitmapConfig.takeIf {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                it != Bitmap.Config.HARDWARE
            } else true
        } ?: Bitmap.Config.ARGB_8888


        val image = DJVUDecoder(file).decode(page)
            ?.createScaledBitmap(options.size)
            ?.copy(config, false)?.asImage()
            ?: return null

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

    class Factory(
        val page: Int = 0
    ) : Decoder.Factory {

        override fun create(
            result: SourceFetchResult,
            options: Options,
            imageLoader: ImageLoader
        ): Decoder? = if (isDJVU(result.source.source())) {
            DjvuDecoder(
                source = result.source,
                options = options,
                page = page
            )
        } else null

        private fun isDJVU(source: BufferedSource): Boolean {
            return source.rangeEquals(0, DJVU_MAGIC.toByteString())
        }
    }
}