package com.t8rin.raw_coder.coil

import android.graphics.Bitmap
import android.graphics.Matrix
import android.os.Build
import androidx.core.graphics.scale
import coil3.Extras
import coil3.ImageLoader
import coil3.asImage
import coil3.decode.DecodeResult
import coil3.decode.Decoder
import coil3.fetch.SourceFetchResult
import coil3.getExtra
import coil3.request.ImageRequest
import coil3.request.Options
import coil3.request.bitmapConfig
import coil3.size.Size
import coil3.size.pxOrElse
import com.t8rin.raw_coder.LibRawBridge
import com.t8rin.raw_coder.LibRawCoder.toBitmap
import com.t8rin.raw_coder.LibRawCoder.toRawInfo
import com.t8rin.raw_coder.RawDecodeMode
import com.t8rin.raw_coder.RawDecodeOptions
import com.t8rin.raw_coder.RawInfo
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import java.io.File
import kotlin.math.roundToInt

class RawDecoder private constructor(
    private val file: File,
    private val rawInfo: RawInfo,
    private val options: Options
) : Decoder {

    override suspend fun decode(): DecodeResult? {
        currentCoroutineContext().ensureActive()
        val request = options.rawDecodeOptions
        val config = requestedConfig()
        val output = LibRawBridge.open(file)?.use { session ->
            val preview = request.mode == RawDecodeMode.EmbeddedPreview &&
                    runCatching { session.unpackThumbnail() }.getOrDefault(false)
            if (!preview) {
                if (!runCatching { session.unpack() }.getOrDefault(false)) return@use null
                currentCoroutineContext().ensureActive()
                val useHalfSize = request.developSettings.halfSize || shouldUseHalfSize()
                if (!runCatching {
                        session.process(
                            settings = request.developSettings,
                            halfSize = useHalfSize,
                            output16Bit = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                config == Bitmap.Config.RGBA_F16
                            } else false
                        )
                    }.getOrDefault(false)) return@use null
                currentCoroutineContext().ensureActive()
            }
            session.output()?.let { it to preview }
        } ?: return null

        currentCoroutineContext().ensureActive()
        var bitmap = output.first.toBitmap(config) ?: return null
        if (request.developSettings.applyOrientation) {
            bitmap = bitmap.applyOrientation(output.first.orientation)
        }
        bitmap = bitmap.resizeToRequest()
        if (bitmap.config != config) {
            val converted = bitmap.copy(config, false)
            if (converted != bitmap) bitmap.recycle()
            bitmap = converted
        }
        currentCoroutineContext().ensureActive()

        val orientedWidth =
            if (rawInfo.orientation in ROTATED_ORIENTATIONS) rawInfo.height else rawInfo.width
        val orientedHeight =
            if (rawInfo.orientation in ROTATED_ORIENTATIONS) rawInfo.width else rawInfo.height
        return DecodeResult(
            image = bitmap.asImage(),
            isSampled = bitmap.width < orientedWidth || bitmap.height < orientedHeight
        )
    }

    private fun requestedConfig(): Bitmap.Config {
        return options.bitmapConfig.takeIf {
            Build.VERSION.SDK_INT < 26 || it != Bitmap.Config.HARDWARE
        }?.takeIf {
            Build.VERSION.SDK_INT >= 26
        } ?: Bitmap.Config.ARGB_8888
    }

    private fun shouldUseHalfSize(): Boolean {
        val (width, height) = targetDimensions(rawInfo.width, rawInfo.height)
        return width * 2 <= rawInfo.width && height * 2 <= rawInfo.height
    }

    private fun Bitmap.resizeToRequest(): Bitmap {
        val (targetWidth, targetHeight) = targetDimensions(width, height)
        if (targetWidth == width && targetHeight == height) return this
        return this.scale(targetWidth, targetHeight).also {
            if (it != this) recycle()
        }
    }

    private fun targetDimensions(sourceWidth: Int, sourceHeight: Int): Pair<Int, Int> {
        if (options.size == Size.ORIGINAL) return sourceWidth to sourceHeight
        val requestedWidth = options.size.width.pxOrElse { sourceWidth }
        val requestedHeight = options.size.height.pxOrElse { sourceHeight }
        val widthScale = requestedWidth.toDouble() / sourceWidth
        val heightScale = requestedHeight.toDouble() / sourceHeight
        val multiplier = when (options.scale) {
            coil3.size.Scale.FILL -> maxOf(widthScale, heightScale)
            coil3.size.Scale.FIT -> minOf(widthScale, heightScale)
        }.coerceIn(0.0, 1.0)
        return (sourceWidth * multiplier).roundToInt().coerceAtLeast(1) to
                (sourceHeight * multiplier).roundToInt().coerceAtLeast(1)
    }

    private fun Bitmap.applyOrientation(orientation: Int): Bitmap {
        val matrix = Matrix()
        when (orientation) {
            3 -> matrix.setRotate(180f)
            5 -> matrix.setRotate(90f)
            6 -> matrix.setRotate(-90f)
            else -> return this
        }
        return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true).also {
            if (it != this) recycle()
        }
    }

    class Factory : Decoder.Factory {

        override fun create(
            result: SourceFetchResult,
            options: Options,
            imageLoader: ImageLoader
        ): Decoder? {
            val file = runCatching { result.source.file().toFile() }.getOrNull() ?: return null
            val info = LibRawBridge.open(file)?.use { session ->
                session.info()?.toRawInfo()
            } ?: return null
            return RawDecoder(file, info, options)
        }
    }

    private companion object {
        val ROTATED_ORIENTATIONS = setOf(5, 6)
    }
}

fun ImageRequest.Builder.rawDecodeOptions(options: RawDecodeOptions) = apply {
    extras[rawDecodeOptionsKey] = options
}

val ImageRequest.rawDecodeOptions: RawDecodeOptions
    get() = getExtra(rawDecodeOptionsKey)

val Options.rawDecodeOptions: RawDecodeOptions
    get() = getExtra(rawDecodeOptionsKey)

val Extras.Key.Companion.rawDecodeOptions: Extras.Key<RawDecodeOptions>
    get() = rawDecodeOptionsKey

private val rawDecodeOptionsKey = Extras.Key(default = RawDecodeOptions())
