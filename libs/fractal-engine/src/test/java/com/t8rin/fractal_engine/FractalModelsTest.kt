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
    fun stableIdsAreUniqueAndRoundTrip() {
        assertEquals(FractalType.entries.size, FractalType.entries.map { it.stableId }.toSet().size)
        FractalType.entries.forEach { type ->
            assertEquals(type, FractalType.fromStableId(type.stableId))
        }
    }

    @Test
    fun additiveJuliaVariantIdsAndApiContractStayStable() {
        assertEquals(106, FractalType.BurningShipJulia.stableId)
        assertEquals("BurningShipJulia", FractalType.BurningShipJulia.name)
        assertEquals(107, FractalType.CelticJulia.stableId)
        assertEquals("CelticJulia", FractalType.CelticJulia.name)
        assertEquals(108, FractalType.Collatz.stableId)
        assertEquals(115, FractalType.Threeply.stableId)
        assertEquals(1006, FractalType.OctahedralIFS.stableId)
        assertEquals(1012, FractalType.SierpinskiGasket.stableId)
        assertEquals(1015, FractalType.Rossler.stableId)
        assertEquals(116, FractalType.Clifford.stableId)
        assertEquals(130, FractalType.HilbertCurve.stableId)
        assertEquals(131, FractalType.BarnsleyMandelbrot.stableId)
        assertEquals(140, FractalType.Thorn.stableId)
        assertEquals(141, FractalType.BarnsleyII.stableId)
        assertEquals(150, FractalType.Tetration.stableId)
        assertEquals(1004, FractalType.SierpinskiTetrahedron.stableId)
        assertEquals("SierpinskiTetrahedron", FractalType.SierpinskiTetrahedron.name)
        assertEquals(77, FractalType.entries.size)
        assertEquals(9, FractalEngineBridge.API_VERSION)
        assertEquals(30, FractalEngineBridge.REQUIRED_PARAMETER_COUNT)
    }

    @Test
    fun formulaSpecificDefaultsReachTheUnchangedWireLayout() {
        val threeply = FractalRenderRequest(FractalType.Threeply, 32, 32)
        assertEquals(-55.0, threeply.juliaReal, 0.0)
        assertEquals(-1.0, threeply.juliaImaginary, 0.0)
        assertEquals(-42.0, threeply.power, 0.0)
        threeply.validate()

        val pickover = FractalRenderRequest(FractalType.Pickover, 32, 32)
        assertEquals(2.24, pickover.juliaReal, 0.0)
        assertEquals(0.43, pickover.juliaImaginary, 0.0)
        assertEquals(-0.65, pickover.power, 0.0)
        assertEquals(-2.43, pickover.phoenixReal, 0.0)
        pickover.validate()

        val cubic = FractalRenderRequest(FractalType.QuaternionCubic, 32, 32)
        assertEquals(QuaternionConstant(-0.2, 0.6, 0.3, 0.0), cubic.quaternionConstant)
        cubic.validate()

        val clifford = FractalRenderRequest(FractalType.Clifford, 32, 32)
        assertEquals(-1.4, clifford.juliaReal, 0.0)
        assertEquals(1.6, clifford.juliaImaginary, 0.0)
        assertEquals(1.0, clifford.power, 0.0)
        assertEquals(0.7, clifford.phoenixReal, 0.0)
        clifford.validate()

        val pythagorasTree = FractalRenderRequest(FractalType.PythagorasTree, 32, 32)
        assertEquals(1.25, pythagorasTree.viewport.centerY, 0.0)
        assertEquals(5.0, pythagorasTree.viewport.span, 0.0)
        pythagorasTree.validate()

        val thorn = FractalRenderRequest(FractalType.Thorn, 32, 32)
        assertEquals(0.1, thorn.juliaReal, 0.0)
        assertEquals(-0.1, thorn.juliaImaginary, 0.0)
        assertEquals(10_000.0, thorn.bailout, 0.0)
        thorn.validate()

        val barnsleyII = FractalRenderRequest(FractalType.BarnsleyII, 32, 32)
        assertEquals(1.1, barnsleyII.juliaReal, 0.0)
        assertEquals(0.6, barnsleyII.juliaImaginary, 0.0)
        barnsleyII.validate()
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
        assertEquals("-0.5", request.viewport.exact.centerX)
        assertEquals("3.0", request.viewport.exact.span)
        assertEquals(
            0.0,
            request.toNativeParameters()[FractalEngineBridge.PARAM_SHOW_FLOOR],
            0.0
        )
        assertEquals(
            FractalRenderRequest.DEFAULT_FLOOR_PRIMARY_COLOR.toDouble(),
            request.toNativeParameters()[FractalEngineBridge.PARAM_FLOOR_PRIMARY_COLOR_ARGB],
            0.0
        )
        assertEquals(
            FractalRenderRequest.DEFAULT_FLOOR_SECONDARY_COLOR.toDouble(),
            request.toNativeParameters()[FractalEngineBridge.PARAM_FLOOR_SECONDARY_COLOR_ARGB],
            0.0
        )

        val floorRequest = request.copy(
            showFloor = true,
            floorPrimaryColor = 0xFF00FF00.toInt(),
            floorSecondaryColor = 0xFFFF00FF.toInt()
        )
        assertEquals(
            1.0,
            floorRequest.toNativeParameters()[FractalEngineBridge.PARAM_SHOW_FLOOR],
            0.0
        )
        assertEquals(
            0xFF00FF00.toInt().toDouble(),
            floorRequest.toNativeParameters()[FractalEngineBridge.PARAM_FLOOR_PRIMARY_COLOR_ARGB],
            0.0
        )
        assertEquals(
            0xFFFF00FF.toInt().toDouble(),
            floorRequest.toNativeParameters()[FractalEngineBridge.PARAM_FLOOR_SECONDARY_COLOR_ARGB],
            0.0
        )
    }

    @Test
    fun exactViewportValidatesDeepCoordinatesAndDoubleConsistency() {
        FractalViewport::class.java.getConstructor(
            Double::class.javaPrimitiveType,
            Double::class.javaPrimitiveType,
            Double::class.javaPrimitiveType
        )
        FractalRenderRequest(
            type = FractalType.Mandelbrot,
            width = 16,
            height = 8,
            viewport = FractalViewport(
                centerX = -2.0,
                centerY = 0.0,
                span = 4.0E-100,
                exact = FractalExactViewport(
                    centerX = "-2.0000000000000000000000000000000000000001",
                    centerY = "0",
                    span = "4E-100"
                )
            ),
            maxIterations = 600
        ).validate()

        assertThrows(IllegalArgumentException::class.java) {
            FractalRenderRequest(
                type = FractalType.Mandelbrot,
                width = 1,
                height = 1,
                viewport = FractalViewport(
                    centerX = -2.0,
                    centerY = 0.0,
                    span = 1.0E-301,
                    exact = FractalExactViewport("-2", "0", "1E-301")
                )
            ).validate()
        }
        assertThrows(IllegalArgumentException::class.java) {
            FractalRenderRequest(
                type = FractalType.Mandelbrot,
                width = 1,
                height = 1,
                viewport = FractalViewport(
                    centerX = -0.5,
                    centerY = 0.0,
                    span = 3.0,
                    exact = FractalExactViewport("-0.4", "0", "3")
                )
            ).validate()
        }
    }

    @Test
    fun bridgeRejectsTrailingParametersAndExcessiveBudgetedWork() {
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
        request.copy(width = 4_096, height = 4_096).validate()
        assertThrows(IllegalArgumentException::class.java) {
            request.copy(width = 8_192, height = 8_192).validate()
        }

        FractalRenderRequest(
            type = FractalType.Buddhabrot,
            width = 4_096,
            height = 4_096,
            maxIterations = 1_000
        ).validate()

        assertThrows(IllegalArgumentException::class.java) {
            FractalRenderRequest(
                type = FractalType.Lorenz,
                width = 32,
                height = 32,
                power = -1.0
            ).validate()
        }
    }

    @Test
    fun densityWorkPlansAreResolutionAwareAndExact() {
        assertEquals(
            5_222_400L,
            FractalEngineBridge.estimatedWorkUnits(
                typeId = FractalType.Buddhabrot.stableId,
                pixelCount = 640L * 480L,
                supersampling = 1,
                maxIterations = 16
            )
        )
        assertEquals(
            500_000_000L,
            FractalEngineBridge.estimatedWorkUnits(
                typeId = FractalType.Buddhabrot.stableId,
                pixelCount = 1_920L * 1_080L,
                supersampling = 1,
                maxIterations = 800
            )
        )
        assertEquals(
            1_331_200L,
            FractalEngineBridge.estimatedWorkUnits(
                typeId = FractalType.Hopalong.stableId,
                pixelCount = 640L * 480L,
                supersampling = 1,
                maxIterations = 320
            )
        )
        assertEquals(
            5_241_600L,
            FractalEngineBridge.estimatedWorkUnits(
                typeId = FractalType.Lorenz.stableId,
                pixelCount = 1_920L * 1_080L,
                supersampling = 4,
                maxIterations = 1_000
            )
        )
    }

    @Test
    fun rayMarchWorkPlanAcceptsAppSizeAndRejectsFourK() {
        FractalType.entries
            .filter { it.dimension == FractalDimension.ThreeDimensional }
            .filterNot { it in setOf(FractalType.Pickover, FractalType.Lorenz, FractalType.Rossler) }
            .forEach { type ->
                FractalRenderRequest(type = type, width = 1_080, height = 1_080).validate()
                assertThrows(
                    "$type must reject a 4K ray-marched render",
                    IllegalArgumentException::class.java
                ) {
                    FractalRenderRequest(
                        type = type,
                        width = 3_840,
                        height = 2_160
                    ).validate()
                }
            }
    }

    @Test
    fun maximumOutputDimensionsAreAcceptedWithinWorkBudget() {
        FractalRenderRequest(
            type = FractalType.Mandelbrot,
            width = 8_192,
            height = 8_192,
            maxIterations = 128
        ).validate()
    }
}
