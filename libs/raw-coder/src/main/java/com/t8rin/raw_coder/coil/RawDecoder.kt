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
import okio.BufferedSource
import okio.ByteString.Companion.encodeUtf8
import okio.ByteString.Companion.toByteString
import java.io.File
import kotlin.math.roundToInt

class RawDecoder private constructor(
    private val file: File,
    private val defaultOptions: () -> RawDecodeOptions? = { null },
    private val options: Options
) : Decoder {

    override suspend fun decode(): DecodeResult? {
        currentCoroutineContext().ensureActive()
        val request = defaultOptions() ?: options.rawDecodeOptions
        val config = requestedConfig()
        val output = LibRawBridge.open(file)?.use { session ->
            val rawInfo = session.info()?.toRawInfo() ?: return@use null
            val preview = request.mode == RawDecodeMode.EmbeddedPreview &&
                    runCatching { session.unpackThumbnail() }.getOrDefault(false)
            if (!preview) {
                if (!runCatching { session.unpack() }.getOrDefault(false)) return@use null
                currentCoroutineContext().ensureActive()
                val useHalfSize = request.developSettings.halfSize || shouldUseHalfSize(rawInfo)
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
            session.output()?.let { it to rawInfo }
        } ?: return null

        currentCoroutineContext().ensureActive()
        val (nativeImage, rawInfo) = output
        var bitmap = nativeImage.toBitmap(config) ?: return null
        if (request.developSettings.applyOrientation) {
            bitmap = bitmap.applyOrientation(nativeImage.orientation)
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

    private fun shouldUseHalfSize(rawInfo: RawInfo): Boolean {
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

    class Factory(
        private val defaultOptions: () -> RawDecodeOptions? = { null }
    ) : Decoder.Factory {

        override fun create(
            result: SourceFetchResult,
            options: Options,
            imageLoader: ImageLoader
        ): Decoder? {
            val file = runCatching { result.source.file().toFile() }.getOrNull() ?: return null
            if (!isRaw(result, file)) return null

            return RawDecoder(
                file = file,
                options = options,
                defaultOptions = defaultOptions
            )
        }

        private fun isRaw(result: SourceFetchResult, file: File): Boolean {
            val mimeType = result.mimeType?.substringBefore(';')?.lowercase()
            if (mimeType?.isRawMimeType() == true) return true
            if (file.extension.lowercase() in RAW_EXTENSIONS) return true

            return result.source.source().hasRawSignature()
        }

        private fun String.isRawMimeType(): Boolean {
            if (!startsWith("image/") && this != "application/x-raw") return false
            val subtype = substringAfter('/')
            return subtype == "x-raw" || subtype == "x-dcraw" || RAW_EXTENSIONS.any {
                subtype == it || subtype.endsWith("-$it")
            }
        }

        private fun BufferedSource.hasRawSignature(): Boolean {
            return rangeEquals(0, "FUJIFILMCCD-RAW ".encodeUtf8()) ||
                    rangeEquals(0, "FOVb".encodeUtf8()) ||
                    rangeEquals(0, ORF_LITTLE_ENDIAN_MAGIC.toByteString()) ||
                    rangeEquals(0, ORF_BIG_ENDIAN_MAGIC.toByteString()) ||
                    rangeEquals(0, RW2_MAGIC.toByteString()) ||
                    rangeEquals(0, CR2_MAGIC.toByteString()) ||
                    rangeEquals(4, "ftypcrx ".encodeUtf8())
        }
    }

    private companion object {
        val ROTATED_ORIENTATIONS = setOf(5, 6)
        val ORF_LITTLE_ENDIAN_MAGIC = byteArrayOf(0x49, 0x49, 0x52, 0x4f, 0x08, 0x00, 0x00, 0x00)
        val ORF_BIG_ENDIAN_MAGIC = byteArrayOf(0x4d, 0x4d, 0x4f, 0x52, 0x00, 0x00, 0x00, 0x00)
        val RW2_MAGIC = byteArrayOf(0x49, 0x49, 0x55, 0x00)
        val CR2_MAGIC = byteArrayOf(
            0x49, 0x49, 0x2a, 0x00, 0x10, 0x00, 0x00, 0x00, 0x43, 0x52, 0x02, 0x00
        )
        val RAW_EXTENSIONS = setOf(
            "3fr", "ari", "arw", "bay", "bmq", "cap", "cine", "cr2", "cr3", "crw", "cs1",
            "dc2", "dcr", "dng", "erf", "fff", "hdr", "ia", "iiq", "k25", "kc2", "kdc", "mdc",
            "mef", "mos", "mrw", "nef", "nrw", "orf", "pef", "ptx", "pxn", "qtk", "raf", "raw",
            "rdc", "rw2", "rwl", "rwz", "sr2", "srf", "srw", "sti", "x3f"
        )
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
