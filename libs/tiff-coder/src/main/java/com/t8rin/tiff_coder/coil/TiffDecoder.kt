@file:Suppress("DEPRECATION")

package com.t8rin.tiff_coder.coil

import android.graphics.Bitmap
import android.os.Build
import androidx.core.graphics.scale
import coil3.Extras
import coil3.ImageLoader
import coil3.asImage
import coil3.decode.DecodeResult
import coil3.decode.DecodeUtils
import coil3.decode.Decoder
import coil3.fetch.SourceFetchResult
import coil3.getExtra
import coil3.request.ImageRequest
import coil3.request.Options
import coil3.request.bitmapConfig
import coil3.size.Size
import coil3.size.pxOrElse
import com.t8rin.tiff_coder.TiffCoder
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.runInterruptible
import org.beyka.tiffbitmapfactory.Orientation
import org.beyka.tiffbitmapfactory.TiffBitmapFactory
import java.io.File
import kotlin.math.roundToInt

class TiffDecoder private constructor(
    private val file: File,
    private val options: Options
) : Decoder {

    override suspend fun decode(): DecodeResult? {
        currentCoroutineContext().ensureActive()
        val bounds = readBounds() ?: return null
        val sourceWidth = if (bounds.orientation.isRotated()) bounds.height else bounds.width
        val sourceHeight = if (bounds.orientation.isRotated()) bounds.width else bounds.height
        val (targetWidth, targetHeight) = targetDimensions(sourceWidth, sourceHeight)
        val sampleSize = DecodeUtils.calculateInSampleSize(
            srcWidth = sourceWidth,
            srcHeight = sourceHeight,
            dstWidth = targetWidth,
            dstHeight = targetHeight,
            scale = options.scale
        )
        val nativeOptions = TiffBitmapFactory.Options().apply {
            inDirectoryNumber = options.tiffPage.coerceIn(0, bounds.pageCount - 1)
            inSampleSize = sampleSize
            inUseOrientationTag = true
            inThrowException = false
            inAvailableMemory = TiffCoder.MAX_DECODE_BYTES
            inPreferredConfig = TiffBitmapFactory.ImageConfig.ARGB_8888
        }
        var bitmap = runInterruptible {
            runCatching { TiffBitmapFactory.decodeFile(file, nativeOptions) }.getOrNull()
        } ?: return null
        currentCoroutineContext().ensureActive()
        if (bitmap.width != targetWidth || bitmap.height != targetHeight) {
            val resized = bitmap.scale(targetWidth, targetHeight)
            if (resized != bitmap) bitmap.recycle()
            bitmap = resized
        }
        val config = requestedConfig()
        if (bitmap.config != config) {
            val converted = bitmap.copy(config, false)
            if (converted != bitmap) bitmap.recycle()
            bitmap = converted
        }
        return DecodeResult(
            image = bitmap.asImage(),
            isSampled = bitmap.width < sourceWidth || bitmap.height < sourceHeight
        )
    }

    private fun readBounds(): Bounds? {
        val page = options.tiffPage.coerceAtLeast(0)
        val pageCount = TiffCoder.pageCount(file)
        if (page >= pageCount) return null
        val nativeOptions = TiffBitmapFactory.Options().apply {
            inDirectoryNumber = page
            inJustDecodeBounds = true
            inThrowException = false
            inAvailableMemory = TiffCoder.MAX_DECODE_BYTES
        }
        runCatching { TiffBitmapFactory.decodeFile(file, nativeOptions) }
        val width = nativeOptions.outWidth
        val height = nativeOptions.outHeight
        val pixels = width.toLong() * height
        if (width <= 0 || height <= 0 || pixels <= 0 || pixels > MAX_PIXELS
        ) return null
        return Bounds(width, height, pageCount, nativeOptions.outImageOrientation)
    }

    private fun targetDimensions(sourceWidth: Int, sourceHeight: Int): Pair<Int, Int> {
        if (options.size == Size.ORIGINAL) return sourceWidth to sourceHeight
        val requestedWidth = options.size.width.pxOrElse { sourceWidth }
        val requestedHeight = options.size.height.pxOrElse { sourceHeight }
        val multiplier = when (options.scale) {
            coil3.size.Scale.FILL -> maxOf(
                requestedWidth.toDouble() / sourceWidth,
                requestedHeight.toDouble() / sourceHeight
            )

            coil3.size.Scale.FIT -> minOf(
                requestedWidth.toDouble() / sourceWidth,
                requestedHeight.toDouble() / sourceHeight
            )
        }.coerceIn(0.0, 1.0)
        return (sourceWidth * multiplier).roundToInt().coerceAtLeast(1) to
                (sourceHeight * multiplier).roundToInt().coerceAtLeast(1)
    }

    private fun requestedConfig(): Bitmap.Config {
        return options.bitmapConfig.takeIf {
            Build.VERSION.SDK_INT < 26 || it != Bitmap.Config.HARDWARE
        }?.takeIf {
            Build.VERSION.SDK_INT >= 26
        } ?: Bitmap.Config.ARGB_8888
    }

    class Factory : Decoder.Factory {
        override fun create(
            result: SourceFetchResult,
            options: Options,
            imageLoader: ImageLoader
        ): Decoder? {
            val file = runCatching { result.source.file().toFile() }.getOrNull() ?: return null
            return if (TiffCoder.isTiff(file)) TiffDecoder(file, options) else null
        }
    }

    private data class Bounds(
        val width: Int,
        val height: Int,
        val pageCount: Int,
        val orientation: Orientation
    )

    private fun Orientation.isRotated(): Boolean = this == Orientation.LEFT_TOP ||
            this == Orientation.RIGHT_TOP || this == Orientation.RIGHT_BOT ||
            this == Orientation.LEFT_BOT

    private companion object {
        const val MAX_PIXELS = 100_000_000L
    }
}

fun ImageRequest.Builder.tiffPage(page: Int) = apply {
    extras[tiffPageKey] = page.coerceAtLeast(0)
}

val ImageRequest.tiffPage: Int
    get() = getExtra(tiffPageKey)

val Options.tiffPage: Int
    get() = getExtra(tiffPageKey)

val Extras.Key.Companion.tiffPage: Extras.Key<Int>
    get() = tiffPageKey

private val tiffPageKey = Extras.Key(default = 0)
