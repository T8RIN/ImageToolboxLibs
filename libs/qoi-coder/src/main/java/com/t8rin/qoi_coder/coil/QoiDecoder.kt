package com.t8rin.qoi_coder.coil

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

        val config = options.bitmapConfig.takeIf {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                it != Bitmap.Config.HARDWARE
            } else true
        } ?: Bitmap.Config.ARGB_8888


        val image = QOIDecoder(array).decode()
            ?.createScaledBitmap(options.size)
            ?.copy(config, false)
            ?.asImage()
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

    class Factory : Decoder.Factory {

        override fun create(
            result: SourceFetchResult,
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