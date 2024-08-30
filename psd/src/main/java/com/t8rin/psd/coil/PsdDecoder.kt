/*
 * ImageToolbox is an image editor for android
 * Copyright (c) 2024 T8RIN (Malik Mukhametzyanov)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * You should have received a copy of the Apache License
 * along with this program.  If not, see <http://www.apache.org/licenses/LICENSE-2.0>.
 */

package com.t8rin.psd.coil

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
import com.t8rin.psd.reader.model.Psd
import okio.ByteString.Companion.encodeUtf8


class PsdDecoder private constructor(
    private val source: ImageSource,
    private val options: Options
) : Decoder {

    override suspend fun decode(): DecodeResult? {
        val config = options.config.takeIf {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                it != Bitmap.Config.HARDWARE
            } else true
        } ?: Bitmap.Config.ARGB_8888

        val bitmap: Bitmap = runCatching {
            Psd(source.file().toFile()).image
        }.getOrNull() ?: return null

        val drawable = BitmapDrawable(
            options.context.resources,
            bitmap
                .createScaledBitmap(options.size)
                .copy(config, false)
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
        ): Decoder? = if (isPSD(result)) {
            PsdDecoder(
                source = result.source,
                options = options
            )
        } else null

        private fun isPSD(source: SourceResult): Boolean {
            return source.mimeType == "image/vnd.adobe.photoshop" || source.source.source()
                .rangeEquals(0, "8BPS".encodeUtf8())
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