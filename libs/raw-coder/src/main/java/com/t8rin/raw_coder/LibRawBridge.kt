package com.t8rin.raw_coder

import java.io.Closeable
import java.io.File

internal object LibRawBridge {

    fun open(file: File): Session? {
        val handle = runCatching { nativeOpen(file.absolutePath) }.getOrDefault(0L)
        return handle.takeIf { it != 0L }?.let(::Session)
    }

    internal class Session(private var handle: Long) : Closeable {

        fun info(): IntArray? = handle.takeIf { it != 0L }?.let(::nativeInfo)

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

        fun output(): NativeRawImage? {
            val info = nativeOutputInfo(handle) ?: return null
            if (info.size != 7) return null
            val data = nativeOutputData(handle) ?: return null
            if (data.size != info[5]) return null
            return NativeRawImage(
                type = info[0],
                width = info[1],
                height = info[2],
                colors = info[3],
                bits = info[4],
                data = data,
                orientation = info[6]
            )
        }

        override fun close() {
            val value = handle
            handle = 0L
            if (value != 0L) nativeClose(value)
        }
    }

    internal class NativeRawImage(
        val type: Int,
        val width: Int,
        val height: Int,
        val colors: Int,
        val bits: Int,
        val data: ByteArray,
        val orientation: Int
    )

    private external fun nativeOpen(path: String): Long
    private external fun nativeClose(handle: Long)
    private external fun nativeInfo(handle: Long): IntArray?
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

    private external fun nativeOutputInfo(handle: Long): IntArray?
    private external fun nativeOutputData(handle: Long): ByteArray?

    init {
        System.loadLibrary("raw_coder")
    }
}
