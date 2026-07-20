package com.t8rin.raw_coder

import android.graphics.Bitmap
import android.os.Build
import java.io.Closeable
import java.io.File

internal object LibRawBridge {

    fun open(file: File): Session? {
        val handle = runCatching { nativeOpen(file.absolutePath) }.getOrDefault(0L)
        return handle.takeIf { it != 0L }?.let(::Session)
    }

    internal class Session(private var handle: Long) : Closeable {

        fun info(): RawInfo? {
            val packed = handle.takeIf { it != 0L }?.let(::nativeInfo) ?: return null
            if (packed == 0L) return null
            return RawInfo(
                width = (packed and 0xffff).toInt(),
                height = (packed ushr 16 and 0xffff).toInt(),
                orientation = (packed ushr 32 and 0xf).toInt(),
                embeddedPreviewCount = (packed ushr 36 and 0xff).toInt(),
                isDng = packed ushr 44 and 1L != 0L
            )
        }

        fun unpackThumbnail(): Boolean = handle != 0L && nativeUnpackThumbnail(handle)

        fun unpack(): Boolean = handle != 0L && nativeUnpack(handle)

        fun process(
            settings: RawDevelopSettings,
            halfSize: Boolean,
            output16Bit: Boolean
        ): Boolean {
            return handle != 0L && nativeProcess(
                handle = handle,
                cameraWhiteBalance = settings.useCameraWhiteBalance,
                autoWhiteBalance = settings.useAutoWhiteBalance,
                outputColor = settings.outputColorSpace.nativeValue,
                highlight = settings.highlightRecovery,
                quality = settings.quality.nativeValue,
                halfSize = halfSize,
                output16Bit = output16Bit
            )
        }

        fun output(
            config: Bitmap.Config,
            requestedWidth: Int = 0,
            requestedHeight: Int = 0,
            scaleFill: Boolean = false,
            applyOrientation: Boolean = false
        ): NativeRawImage? {
            val output16Bit = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
                    config == Bitmap.Config.RGBA_F16
            val bitmap = nativeOutputBitmap(
                handle = handle,
                output16Bit = output16Bit,
                requestedWidth = requestedWidth,
                requestedHeight = requestedHeight,
                scaleFill = scaleFill,
                applyOrientation = applyOrientation
            ) ?: return null
            return NativeRawImage(
                bitmap = bitmap,
                orientation = nativeOutputOrientation(handle)
            )
        }

        override fun close() {
            val value = handle
            handle = 0L
            if (value != 0L) nativeClose(value)
        }
    }

    internal class NativeRawImage(
        val bitmap: Bitmap,
        val orientation: Int
    )

    private external fun nativeOpen(path: String): Long
    private external fun nativeClose(handle: Long)
    private external fun nativeInfo(handle: Long): Long
    private external fun nativeUnpackThumbnail(handle: Long): Boolean
    private external fun nativeUnpack(handle: Long): Boolean
    private external fun nativeProcess(
        handle: Long,
        cameraWhiteBalance: Boolean,
        autoWhiteBalance: Boolean,
        outputColor: Int,
        highlight: Int,
        quality: Int,
        halfSize: Boolean,
        output16Bit: Boolean
    ): Boolean

    private external fun nativeOutputBitmap(
        handle: Long,
        output16Bit: Boolean,
        requestedWidth: Int,
        requestedHeight: Int,
        scaleFill: Boolean,
        applyOrientation: Boolean
    ): Bitmap?
    private external fun nativeOutputOrientation(handle: Long): Int

    init {
        System.loadLibrary("raw_coder")
    }
}
