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

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.assertThrows
import org.junit.Test

class FractalModelsTest {

    @Test
    fun stableIdsAndKeysAreUniqueAndRoundTrip() {
        assertEquals(FractalType.entries.size, FractalType.entries.map { it.stableId }.toSet().size)
        assertEquals(FractalType.entries.size, FractalType.entries.map { it.stableKey }.toSet().size)
        FractalType.entries.forEach { type ->
            assertEquals(type, FractalType.fromStableId(type.stableId))
            assertEquals(type, FractalType.fromStableKey(type.stableKey))
        }
    }

    @Test
    fun bridgeAdvertisesEveryTypedFractal() {
        if (FractalEngineBridge.isAvailable()) {
            assertEquals(FractalEngineBridge.API_VERSION, FractalEngineBridge.apiVersion())
            assertEquals(
                FractalType.entries.map { it.stableId }.toSet(),
                FractalEngineBridge.supportedTypeIds().toSet()
            )
        } else {
            assertEquals(0, FractalEngineBridge.apiVersion())
            assertTrue(FractalEngineBridge.supportedTypeIds().isEmpty())
        }
        assertTrue(FractalType.entries.any { it.dimension == FractalDimension.ThreeDimensional })
    }

    @Test
    fun nativeParameterLayoutIsComplete() {
        val request = FractalRenderRequest(
            type = FractalType.Mandelbrot,
            width = 64,
            height = 32
        )
        assertEquals(FractalEngineBridge.REQUIRED_PARAMETER_COUNT, request.toNativeParameters().size)
        assertEquals(
            2.0,
            request.toNativeParameters()[FractalEngineBridge.PARAM_VIEWPORT_ASPECT_RATIO],
            0.0
        )
    }

    @Test
    fun bridgeRejectsTrailingParametersAndExcessiveWork() {
        val request = FractalRenderRequest(
            type = FractalType.Mandelbrot,
            width = 1_000,
            height = 1_000,
            maxIterations = 320
        )
        request.validate()

        assertThrows(IllegalArgumentException::class.java) {
            FractalEngineBridge.validateArguments(
                bitmapWidth = request.width,
                bitmapHeight = request.height,
                typeId = request.type.stableId,
                maxIterations = request.maxIterations,
                parameters = request.toNativeParameters() + 0.0,
                palette = request.palette,
                lyapunovSequence = request.lyapunovSequence
            )
        }
        assertThrows(IllegalArgumentException::class.java) {
            request.copy(width = 2_000, height = 2_000).validate()
        }
    }
}
