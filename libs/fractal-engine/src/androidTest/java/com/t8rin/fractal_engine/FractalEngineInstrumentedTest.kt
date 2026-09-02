/*
 * ImageToolbox is an image editor for android
 * Copyright (c) 2026 T8RIN (Malik Mukhametzyanov)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.t8rin.fractal_engine

import android.graphics.Bitmap
import android.graphics.Color
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertFalse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Assert.assertThrows
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FractalEngineInstrumentedTest {

    @Test
    fun nativeMetadataMatchesKotlinContract() {
        assertTrue(FractalEngine.isAvailable)
        assertEquals(FractalEngineBridge.API_VERSION, FractalEngine.apiVersion)
        assertEquals(FractalType.entries.toSet(), FractalEngine.supportedTypes)
    }

    @Test
    fun renderIntoWritesTheProvidedBitmap() = runBlocking {
        val bitmap = Bitmap.createBitmap(96, 64, Bitmap.Config.ARGB_8888)
        val output = FractalEngine.renderInto(
            bitmap = bitmap,
            request = FractalRenderRequest(
                type = FractalType.Mandelbrot,
                width = bitmap.width,
                height = bitmap.height,
                maxIterations = 160
            )
        )

        assertSame(bitmap, output)
        assertFalse(output.isRecycled)
        val pixels = IntArray(output.width * output.height)
        output.getPixels(pixels, 0, output.width, 0, 0, output.width, output.height)
        assertTrue(pixels.toSet().size > 8)
        output.recycle()
    }

    @Test
    fun addedTwoAndThreeDimensionalTypesRender() = runBlocking {
        val addedTypes = FractalType.entries.dropWhile { it != FractalType.MagnetI }
        addedTypes.forEach { type ->
            val bitmap = FractalEngine.render(
                FractalRenderRequest(
                    type = type,
                    width = 48,
                    height = 48,
                    maxIterations = 96
                )
            )
            val pixels = IntArray(bitmap.width * bitmap.height)
            bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
            assertTrue("$type produced a uniform bitmap", pixels.toSet().size > 1)
            bitmap.recycle()
        }
    }

    @Test
    fun threeDimensionalMissRaysUseInsideColorAsBackground() = runBlocking {
        val expectedBackground = Color.rgb(18, 231, 117)
        val bitmap = FractalEngine.render(
            FractalRenderRequest(
                type = FractalType.Mandelbulb,
                width = 64,
                height = 64,
                maxIterations = 96,
                insideColor = expectedBackground
            )
        )

        try {
            listOf(
                0 to 0,
                bitmap.width - 1 to 0,
                0 to bitmap.height - 1,
                bitmap.width - 1 to bitmap.height - 1
            ).forEach { (x, y) ->
                assertEquals(expectedBackground, bitmap.getPixel(x, y))
            }
            assertTrue(bitmap.getPixel(bitmap.width / 2, bitmap.height / 2) != expectedBackground)
        } finally {
            bitmap.recycle()
        }
    }

    @Test
    fun deepZoomAt1e100IsNonUniformAndUsesExactCenter() = runBlocking {
        fun request(exactCenterX: String) = FractalRenderRequest(
            type = FractalType.Mandelbrot,
            width = 32,
            height = 8,
            viewport = FractalViewport(
                centerX = -2.0,
                centerY = 0.0,
                span = 4.0E-100,
                exact = FractalExactViewport(exactCenterX, "0", "4E-100")
            ),
            viewportAspectRatio = 4.0,
            maxIterations = 600
        )

        val centered = FractalEngine.render(request("-2"))
        val shifted = FractalEngine.render(
            request(
                "-1.999999999999999999999999999999999999999999999999999999999999" +
                    "9999999999999999999999999999999999999999"
            )
        )
        try {
            val centeredPixels = IntArray(centered.width * centered.height)
            val shiftedPixels = IntArray(shifted.width * shifted.height)
            centered.getPixels(
                centeredPixels,
                0,
                centered.width,
                0,
                0,
                centered.width,
                centered.height
            )
            shifted.getPixels(
                shiftedPixels,
                0,
                shifted.width,
                0,
                0,
                shifted.width,
                shifted.height
            )
            assertTrue(centeredPixels.toSet().size > 1)
            assertFalse(centeredPixels.contentEquals(shiftedPixels))
        } finally {
            centered.recycle()
            shifted.recycle()
        }
    }

    @Test
    fun preCancelledDeepSessionReturnsCancelled() {
        val request = FractalRenderRequest(
            type = FractalType.Mandelbrot,
            width = 1,
            height = 1,
            viewport = FractalViewport(
                centerX = -2.0,
                centerY = 0.0,
                span = 1.0E-300,
                exact = FractalExactViewport("-2", "0", "1E-300")
            ),
            maxIterations = 16_384
        )
        val bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
        val session = FractalEngineBridge.createSession()
        try {
            FractalEngineBridge.cancel(session)
            assertEquals(
                FractalEngineBridge.RESULT_CANCELLED,
                FractalEngineBridge.renderIntoBlocking(
                    sessionHandle = session,
                    bitmap = bitmap,
                    typeId = request.type.stableId,
                    maxIterations = request.maxIterations,
                    parameters = request.toNativeParameters(),
                    exactViewport = request.viewport.exact,
                    palette = request.palette,
                    lyapunovSequence = request.lyapunovSequence
                )
            )
        } finally {
            FractalEngineBridge.destroySession(session)
            bitmap.recycle()
        }
    }

    @Test
    fun cancellationKeepsCallerOwnedBitmapAlive() = runBlocking {
        val bitmap = Bitmap.createBitmap(768, 768, Bitmap.Config.ARGB_8888)
        withTimeout(3_000) {
            val renderJob = launch {
                FractalEngine.renderInto(
                    bitmap = bitmap,
                    request = FractalRenderRequest(
                        type = FractalType.Mandelbulb,
                        width = bitmap.width,
                        height = bitmap.height,
                        maxIterations = 800
                    )
                )
            }
            delay(10)
            renderJob.cancelAndJoin()
        }
        assertFalse(bitmap.isRecycled)
        bitmap.recycle()
    }

    @Test
    fun premultipliedBitmapRoundTripsTranslucentInsideColor() = runBlocking {
        val expected = Color.argb(128, 64, 128, 192)
        val bitmap = Bitmap.createBitmap(3, 3, Bitmap.Config.ARGB_8888)
        assertTrue(bitmap.isPremultiplied)
        FractalEngine.renderInto(
            bitmap = bitmap,
            request = FractalRenderRequest(
                type = FractalType.Mandelbrot,
                width = bitmap.width,
                height = bitmap.height,
                viewport = FractalViewport(0.0, 0.0, 0.1),
                maxIterations = 1,
                insideColor = expected
            )
        )

        val actual = bitmap.getPixel(1, 1)
        assertTrue(kotlin.math.abs(Color.alpha(actual) - Color.alpha(expected)) <= 1)
        assertTrue(kotlin.math.abs(Color.red(actual) - Color.red(expected)) <= 1)
        assertTrue(kotlin.math.abs(Color.green(actual) - Color.green(expected)) <= 1)
        assertTrue(kotlin.math.abs(Color.blue(actual) - Color.blue(expected)) <= 1)
        bitmap.recycle()
    }

    @Test
    fun nativeSessionsAreSingleUse() {
        val request = FractalRenderRequest(
            type = FractalType.Mandelbrot,
            width = 8,
            height = 8,
            maxIterations = 16
        )
        val bitmap = Bitmap.createBitmap(request.width, request.height, Bitmap.Config.ARGB_8888)
        val session = FractalEngineBridge.createSession()
        try {
            assertEquals(
                FractalEngineBridge.RESULT_COMPLETED,
                FractalEngineBridge.renderIntoBlocking(
                    sessionHandle = session,
                    bitmap = bitmap,
                    typeId = request.type.stableId,
                    maxIterations = request.maxIterations,
                    parameters = request.toNativeParameters(),
                    palette = request.palette,
                    lyapunovSequence = request.lyapunovSequence
                )
            )
            assertThrows(FractalEngineException::class.java) {
                FractalEngineBridge.renderIntoBlocking(
                    sessionHandle = session,
                    bitmap = bitmap,
                    typeId = request.type.stableId,
                    maxIterations = request.maxIterations,
                    parameters = request.toNativeParameters(),
                    palette = request.palette,
                    lyapunovSequence = request.lyapunovSequence
                )
            }
        } finally {
            FractalEngineBridge.destroySession(session)
            bitmap.recycle()
        }
    }
}
