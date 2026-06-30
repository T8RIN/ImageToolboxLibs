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

package com.t8rin.gif_converter

import android.graphics.Bitmap
import android.graphics.Paint
import androidx.core.graphics.applyCanvas
import androidx.core.graphics.createBitmap
import java.io.IOException
import java.io.OutputStream

class GifEncoder {

    private var width: Int = 0
    private var height: Int = 0
    private var x: Int = 0
    private var y: Int = 0
    private var transparent: Int = -1
    private var repeat: Int = -1
    private var delay: Int = 0
    private var started: Boolean = false
    private var outputStream: OutputStream? = null
    private var dispose: Int = -1
    private var firstFrame: Boolean = true
    private var sizeSet: Boolean = false
    private var sample: Int = 10

    private var nativeHandle: Long = 0

    fun setDelay(ms: Int) = apply {
        delay = ms / 10
    }

    fun setDispose(code: Int) = apply {
        if (code >= 0) {
            dispose = code
        }
    }

    fun setRepeat(iter: Int) = apply {
        if (iter >= 0) {
            repeat = iter
        }
    }

    fun setTransparent(color: Int) = apply {
        transparent = color
    }

    fun setFrameRate(fps: Float) = apply {
        if (fps != 0f) {
            delay = (100 / fps).toInt()
        }
    }

    fun setQuality(quality: Int) = apply {
        sample = quality.coerceAtLeast(1)
    }

    fun setSize(width: Int, height: Int) = apply {
        this.width = width.takeIf { it >= 1 } ?: 320
        this.height = height.takeIf { it >= 1 } ?: 240
        sizeSet = true
    }

    fun setPosition(x: Int, y: Int) = apply {
        this.x = x
        this.y = y
    }

    fun addFrame(image: Bitmap): Boolean {
        if (!started) return false

        if (!sizeSet) {
            setSize(image.width, image.height)
        }

        val frame = if (image.width != width || image.height != height) {
            createBitmap(width, height).applyCanvas {
                drawBitmap(image, 0f, 0f, Paint())
            }
        } else {
            image
        }

        val pixels = IntArray(width * height)
        frame.getPixels(pixels, 0, width, 0, 0, width, height)

        if (nativeHandle == 0L) {
            nativeHandle = nativeStart(width, height, repeat)
            if (nativeHandle == 0L) return false
        }

        val encoded = nativeAddFrame(
            handle = nativeHandle,
            pixels = pixels,
            width = width,
            height = height,
            x = x,
            y = y,
            delay = delay,
            dispose = dispose,
            transparent = transparent,
            quality = sample
        ) ?: return false

        return try {
            val offset = if (firstFrame) 6 else 0
            outputStream!!.write(encoded, offset, encoded.size - offset)
            firstFrame = false
            true
        } catch (_: IOException) {
            false
        }
    }

    fun finish(): Boolean {
        if (!started) return false

        started = false
        var result = true
        try {
            if (nativeHandle == 0L) {
                outputStream!!.write(0x3b)
            } else {
                val encoded = nativeFinish(nativeHandle)
                nativeHandle = 0
                if (encoded == null) {
                    result = false
                } else {
                    outputStream!!.write(encoded)
                }
            }
            outputStream!!.flush()
        } catch (_: IOException) {
            result = false
        } finally {
            if (nativeHandle != 0L) {
                nativeAbort(nativeHandle)
                nativeHandle = 0
            }
            outputStream = null
            firstFrame = true
        }
        return result
    }

    fun start(outputStream: OutputStream): Boolean {
        if (nativeHandle != 0L) {
            nativeAbort(nativeHandle)
            nativeHandle = 0
        }
        firstFrame = true
        this.outputStream = outputStream
        return try {
            outputStream.write(
                byteArrayOf(
                    'G'.code.toByte(),
                    'I'.code.toByte(),
                    'F'.code.toByte(),
                    '8'.code.toByte(),
                    '9'.code.toByte(),
                    'a'.code.toByte()
                )
            )
            started = true
            true
        } catch (_: IOException) {
            started = false
            false
        }
    }

    private external fun nativeStart(width: Int, height: Int, repeat: Int): Long

    private external fun nativeAddFrame(
        handle: Long,
        pixels: IntArray,
        width: Int,
        height: Int,
        x: Int,
        y: Int,
        delay: Int,
        dispose: Int,
        transparent: Int,
        quality: Int
    ): ByteArray?

    private external fun nativeFinish(handle: Long): ByteArray?

    private external fun nativeAbort(handle: Long)

    private companion object {
        init {
            System.loadLibrary("gif_encoder")
        }
    }
}
