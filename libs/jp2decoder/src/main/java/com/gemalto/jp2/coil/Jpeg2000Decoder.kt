package com.gemalto.jp2.coil

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
import com.gemalto.jp2.JP2Decoder
import com.gemalto.jp2.Utils.J2K_CODESTREAM_MAGIC
import com.gemalto.jp2.Utils.JP2_MAGIC
import com.gemalto.jp2.Utils.JP2_RFC3745_MAGIC
import com.gemalto.jp2.Utils.flexibleResize
import okio.BufferedSource
import okio.ByteString.Companion.toByteString

class Jpeg2000Decoder private constructor(
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


        val image = JP2Decoder(array).decode()
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
        ): Decoder? = if (isJP2(result.source.source())) {
            Jpeg2000Decoder(
                source = result.source,
                options = options
            )
        } else null

        private fun isJP2(source: BufferedSource): Boolean {
            if (source.rangeEquals(0, JP2_RFC3745_MAGIC.toByteString())) return true
            if (source.rangeEquals(0, JP2_MAGIC.toByteString())) return true
            return source.rangeEquals(0, J2K_CODESTREAM_MAGIC.toByteString())
        }
    }
}