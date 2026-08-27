/*
 * ImageToolbox is an image editor for android
 * Copyright (c) 2026 T8RIN (Malik Mukhametzyanov)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package com.t8rin.archive

import java.io.InputStream
import java.io.OutputStream

class BrotliOutputStream(
    private val destination: OutputStream
) : OutputStream() {
    private var handle = BrotliNative.createEncoder()
    private var closed = false

    override fun write(b: Int) = write(byteArrayOf(b.toByte()))

    override fun write(b: ByteArray, off: Int, len: Int) {
        check(!closed) { "Stream is closed" }
        require(off >= 0 && len >= 0 && off + len <= b.size)
        drain(b, off, len, finish = false)
    }

    override fun flush() = destination.flush()

    override fun close() {
        if (closed) return
        try {
            while (!BrotliNative.isEncoderFinished(handle)) {
                drain(EmptyBytes, 0, 0, finish = true)
            }
            destination.flush()
        } finally {
            BrotliNative.destroyEncoder(handle)
            handle = 0
            closed = true
            destination.close()
        }
    }

    private fun drain(input: ByteArray, offset: Int, length: Int, finish: Boolean) {
        var first = true
        do {
            val output = BrotliNative.encode(
                handle = handle,
                input = if (first) input else EmptyBytes,
                offset = if (first) offset else 0,
                length = if (first) length else 0,
                finish = finish
            )
            destination.write(output)
            first = false
        } while (output.size == NativeChunkSize)
    }
}

class BrotliInputStream(
    private val source: InputStream
) : InputStream() {
    private var handle = BrotliNative.createDecoder()
    private var output = EmptyBytes
    private var outputOffset = 0
    private var sourceFinished = false
    private var drainPending = false
    private var closed = false

    override fun read(): Int {
        val byte = ByteArray(1)
        return if (read(byte) < 0) -1 else byte[0].toInt() and 0xFF
    }

    override fun read(b: ByteArray, off: Int, len: Int): Int {
        check(!closed) { "Stream is closed" }
        require(off >= 0 && len >= 0 && off + len <= b.size)
        if (len == 0) return 0
        if (!ensureOutput()) return -1
        val count = minOf(len, output.size - outputOffset)
        output.copyInto(b, off, outputOffset, outputOffset + count)
        outputOffset += count
        return count
    }

    override fun close() {
        if (closed) return
        BrotliNative.destroyDecoder(handle)
        handle = 0
        closed = true
        source.close()
    }

    private fun ensureOutput(): Boolean {
        while (outputOffset >= output.size) {
            if (BrotliNative.isDecoderFinished(handle)) return false
            val input = if (drainPending || sourceFinished) {
                EmptyBytes
            } else {
                ByteArray(NativeChunkSize).let { buffer ->
                    val count = source.read(buffer)
                    if (count < 0) {
                        sourceFinished = true
                        EmptyBytes
                    } else {
                        buffer.copyOf(count)
                    }
                }
            }
            output = BrotliNative.decode(
                handle = handle,
                input = input,
                offset = 0,
                length = input.size,
                finish = sourceFinished
            )
            outputOffset = 0
            drainPending = output.size == NativeChunkSize
        }
        return true
    }
}

private object BrotliNative {
    init {
        System.loadLibrary("archive-brotli")
    }

    external fun createEncoder(): Long
    external fun encode(
        handle: Long,
        input: ByteArray,
        offset: Int,
        length: Int,
        finish: Boolean
    ): ByteArray
    external fun isEncoderFinished(handle: Long): Boolean
    external fun destroyEncoder(handle: Long)

    external fun createDecoder(): Long
    external fun decode(
        handle: Long,
        input: ByteArray,
        offset: Int,
        length: Int,
        finish: Boolean
    ): ByteArray
    external fun isDecoderFinished(handle: Long): Boolean
    external fun destroyDecoder(handle: Long)
}

private val EmptyBytes = ByteArray(0)
private const val NativeChunkSize = 64 * 1024
