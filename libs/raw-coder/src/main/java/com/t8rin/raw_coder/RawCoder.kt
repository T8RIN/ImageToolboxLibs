package com.t8rin.raw_coder

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Build
import android.util.Half
import androidx.annotation.RequiresApi
import androidx.core.graphics.createBitmap
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder

interface RawCoder {
    fun probe(file: File): RawInfo?
    fun decode(file: File, options: RawDecodeOptions = RawDecodeOptions()): RawImage?
}

object LibRawCoder : RawCoder {

    override fun probe(file: File): RawInfo? = LibRawBridge.open(file)?.use { session ->
        session.info()?.toRawInfo()
    }

    override fun decode(file: File, options: RawDecodeOptions): RawImage? {
        return LibRawBridge.open(file)?.use { session ->
            val info = session.info()?.toRawInfo() ?: return@use null
            val preview = options.mode == RawDecodeMode.EmbeddedPreview && session.unpackThumbnail()
            if (!preview) {
                if (!session.unpack()) return@use null
                if (!session.process(
                        options.developSettings,
                        options.developSettings.halfSize,
                        false
                    )
                ) {
                    return@use null
                }
            }
            val output = session.output() ?: return@use null
            val bitmap = output.toBitmap(Bitmap.Config.ARGB_8888) ?: return@use null
            RawImage(bitmap, preview, output.orientation.takeIf { it != 0 } ?: info.orientation)
        }
    }

    internal fun IntArray.toRawInfo(): RawInfo? {
        if (size != 5 || this[0] <= 0 || this[1] <= 0) return null
        return RawInfo(
            width = this[0],
            height = this[1],
            orientation = this[2],
            embeddedPreviewCount = this[3],
            isDng = this[4] != 0
        )
    }

    internal fun LibRawBridge.NativeRawImage.toBitmap(config: Bitmap.Config): Bitmap? {
        if (type == LIBRAW_IMAGE_JPEG) {
            return BitmapFactory.decodeByteArray(data, 0, data.size)
        }
        if (type != LIBRAW_IMAGE_BITMAP || width <= 0 || height <= 0 ||
            colors != 1 && colors !in 3..4
        ) {
            return null
        }
        val pixelCount = width.toLong() * height
        if (pixelCount !in 1..MAX_PIXELS) return null
        return if (bits > 8 && Build.VERSION.SDK_INT >= 26 && config == Bitmap.Config.RGBA_F16) {
            toF16Bitmap(pixelCount.toInt())
        } else {
            toArgbBitmap(pixelCount.toInt(), bits > 8)
        }
    }

    private fun LibRawBridge.NativeRawImage.toArgbBitmap(
        pixelCount: Int,
        sixteenBit: Boolean
    ): Bitmap? {
        val sampleBytes = if (sixteenBit) 2 else 1
        val required = pixelCount.toLong() * colors * sampleBytes
        if (required > data.size) return null
        val input = ByteBuffer.wrap(data).order(ByteOrder.nativeOrder())
        val pixels = IntArray(pixelCount)
        for (index in pixels.indices) {
            fun channel(component: Int): Int {
                if (component >= colors) return if (component == 3) 255 else 0
                val offset = (index * colors + component) * sampleBytes
                return if (sixteenBit) (input.getShort(offset).toInt() and 0xffff) ushr 8
                else data[offset].toInt() and 0xff
            }

            val red = channel(0)
            val green = channel(if (colors == 1) 0 else 1)
            val blue = channel(if (colors == 1) 0 else 2)
            val alpha = 255
            pixels[index] = Color.argb(alpha, red, green, blue)
        }
        return Bitmap.createBitmap(pixels, width, height, Bitmap.Config.ARGB_8888)
    }

    @SuppressLint("HalfFloat")
    @RequiresApi(Build.VERSION_CODES.O)
    private fun LibRawBridge.NativeRawImage.toF16Bitmap(pixelCount: Int): Bitmap? {
        val required = pixelCount.toLong() * colors * 2
        if (required > data.size) return null
        val input = ByteBuffer.wrap(data).order(ByteOrder.nativeOrder())
        val output = ByteBuffer.allocateDirect(pixelCount * 8).order(ByteOrder.nativeOrder())
        for (index in 0 until pixelCount) {
            fun channel(component: Int): Float {
                if (component >= colors) return if (component == 3) 1f else 0f
                val value = input.getShort((index * colors + component) * 2).toInt() and 0xffff
                return value / 65535f
            }
            output.putShort(Half.toHalf(channel(0)))
            output.putShort(Half.toHalf(channel(1)))
            output.putShort(Half.toHalf(channel(2)))
            output.putShort(Half.toHalf(1f))
        }
        output.rewind()
        return createBitmap(width, height, Bitmap.Config.RGBA_F16).apply {
            copyPixelsFromBuffer(output)
        }
    }

    private const val LIBRAW_IMAGE_JPEG = 1
    private const val LIBRAW_IMAGE_BITMAP = 2
    private const val MAX_PIXELS = 100_000_000L
}
