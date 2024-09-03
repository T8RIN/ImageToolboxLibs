package com.t8rin.djvu_coder.coil

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
import com.t8rin.djvu_coder.DJVUDecoder
import com.t8rin.djvu_coder.Utils.DJVU_MAGIC
import com.t8rin.djvu_coder.Utils.flexibleResize
import okio.BufferedSource
import okio.ByteString.Companion.toByteString

class DjvuDecoder private constructor(
    private val source: ImageSource,
    private val options: Options
) : Decoder {

    override suspend fun decode(): DecodeResult? {
        val file = source.file().toFile()

        val config = options.config.takeIf {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                it != Bitmap.Config.HARDWARE
            } else true
        } ?: Bitmap.Config.ARGB_8888


        val drawable = BitmapDrawable(
            options.context.resources,
            DJVUDecoder(file).decode(0)
                ?.createScaledBitmap(options.size)
                ?.copy(config, false)
                ?: return null
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

    class Factory : Decoder.Factory {

        override fun create(
            result: SourceResult,
            options: Options,
            imageLoader: ImageLoader
        ): Decoder? = if (isDJVU(result.source.source())) {
            DjvuDecoder(
                source = result.source,
                options = options
            )
        } else null

        private fun isDJVU(source: BufferedSource): Boolean {
            return source.rangeEquals(0, DJVU_MAGIC.toByteString())
        }
    }
}