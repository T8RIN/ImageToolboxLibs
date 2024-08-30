package com.t8rin.qoi_coder.coil

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
import com.t8rin.qoi_coder.QOIDecoder
import com.t8rin.qoi_coder.Utils.QOI_MAGIC
import com.t8rin.qoi_coder.Utils.flexibleResize
import okio.BufferedSource
import okio.ByteString.Companion.toByteString

class QoiDecoder private constructor(
    private val source: ImageSource,
    private val options: Options
) : Decoder {

    override suspend fun decode(): DecodeResult? {
        val array = source.source().readByteArray()

        val config = options.config.takeIf {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                it != Bitmap.Config.HARDWARE
            } else true
        } ?: Bitmap.Config.ARGB_8888


        val drawable = BitmapDrawable(
            options.context.resources,
            QOIDecoder(array).decode()
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
        ): Decoder? = if (isQOI(result.source.source())) {
            QoiDecoder(
                source = result.source,
                options = options
            )
        } else null

        private fun isQOI(source: BufferedSource): Boolean {
            return source.rangeEquals(0, QOI_MAGIC.toByteString())
        }
    }
}