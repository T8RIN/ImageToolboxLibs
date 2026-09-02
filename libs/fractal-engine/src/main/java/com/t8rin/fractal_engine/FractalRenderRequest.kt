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

import java.math.BigDecimal

data class FractalViewport @JvmOverloads constructor(
    val centerX: Double,
    val centerY: Double,
    /** Visible vertical span. The horizontal span is [span] multiplied by viewport aspect ratio. */
    val span: Double,
    /** Decimal coordinates used by the native perturbation renderer beyond `Double` precision. */
    val exact: FractalExactViewport = FractalExactViewport.fromDoubles(centerX, centerY, span)
)

data class FractalExactViewport(
    val centerX: String,
    val centerY: String,
    val span: String
) {

    internal fun validateAgainst(viewport: FractalViewport) {
        val exactCenterX = centerX.toValidatedDecimal("Exact center X")
        val exactCenterY = centerY.toValidatedDecimal("Exact center Y")
        val exactSpan = span.toValidatedDecimal("Exact vertical span")
        require(exactSpan.signum() > 0) { "Exact vertical span must be positive" }
        require(exactSpan >= MIN_SPAN) { "Exact vertical span must be at least $MIN_SPAN" }
        require(exactCenterX.toDouble() == viewport.centerX) {
            "Exact center X must represent the supplied Double center X"
        }
        require(exactCenterY.toDouble() == viewport.centerY) {
            "Exact center Y must represent the supplied Double center Y"
        }
        require(exactSpan.toDouble() == viewport.span) {
            "Exact vertical span must represent the supplied Double vertical span"
        }
    }

    companion object {
        internal val MIN_SPAN = BigDecimal("1E-300")
        internal const val MAX_DECIMAL_LENGTH = 512

        fun fromDoubles(
            centerX: Double,
            centerY: Double,
            span: Double
        ): FractalExactViewport = FractalExactViewport(
            centerX = centerX.toString(),
            centerY = centerY.toString(),
            span = span.toString()
        )
    }
}

private fun String.toValidatedDecimal(label: String): BigDecimal {
    require(length in 1..FractalExactViewport.MAX_DECIMAL_LENGTH) {
        "$label must contain 1..${FractalExactViewport.MAX_DECIMAL_LENGTH} characters"
    }
    return try {
        toBigDecimal()
    } catch (error: NumberFormatException) {
        throw IllegalArgumentException("$label must be a finite decimal number", error)
    }
}

data class FractalCamera(
    /** Horizontal orbit angle in radians. */
    val yaw: Double = 0.65,
    /** Vertical orbit angle in radians. */
    val pitch: Double = 0.3,
    val distance: Double = 3.5,
    val targetX: Double = 0.0,
    val targetY: Double = 0.0,
    val targetZ: Double = 0.0
)

data class QuaternionConstant(
    val x: Double = -0.2,
    val y: Double = 0.8,
    val z: Double = 0.0,
    val w: Double = 0.0
)

enum class FractalColoring(val stableId: Int) {
    Smooth(FractalEngineBridge.COLORING_SMOOTH),
    Banded(FractalEngineBridge.COLORING_BANDED),
    OrbitTrap(FractalEngineBridge.COLORING_ORBIT_TRAP),
    Angle(FractalEngineBridge.COLORING_ANGLE)
}

data class FractalRenderRequest(
    val type: FractalType,
    val width: Int,
    val height: Int,
    val viewport: FractalViewport = type.defaultViewport,
    val viewportAspectRatio: Double = width.toDouble() / height,
    val maxIterations: Int = DEFAULT_ITERATIONS,
    val power: Double = type.defaultPower,
    val bailout: Double = DEFAULT_BAILOUT,
    val supersampling: Int = 1,
    val coloring: FractalColoring = FractalColoring.Smooth,
    val palette: IntArray = defaultPalette(),
    val paletteCycles: Double = 1.0,
    val paletteOffset: Double = 0.0,
    val insideColor: Int = DEFAULT_INSIDE_COLOR,
    val juliaReal: Double = -0.8,
    val juliaImaginary: Double = 0.156,
    val phoenixReal: Double = -0.5,
    val phoenixImaginary: Double = 0.0,
    val novaRelaxation: Double = 1.0,
    val lyapunovSequence: String = "AB",
    val camera: FractalCamera = type.defaultCamera,
    val quaternionConstant: QuaternionConstant = QuaternionConstant(),
    val fieldOfViewDegrees: Double = 45.0
) {

    internal fun validate() {
        require(width in 1..MAX_OUTPUT_DIMENSION) {
            "Width must be in 1..$MAX_OUTPUT_DIMENSION, was $width"
        }
        require(height in 1..MAX_OUTPUT_DIMENSION) {
            "Height must be in 1..$MAX_OUTPUT_DIMENSION, was $height"
        }
        require(width.toLong() * height <= MAX_OUTPUT_PIXELS) {
            "Output must not exceed $MAX_OUTPUT_PIXELS pixels"
        }
        viewport.exact.validateAgainst(viewport)
        FractalEngineBridge.validateArguments(
            bitmapWidth = width,
            bitmapHeight = height,
            typeId = type.stableId,
            maxIterations = maxIterations,
            parameters = toNativeParameters(),
            palette = palette,
            lyapunovSequence = lyapunovSequence
        )
    }

    internal fun toNativeParameters(): DoubleArray = doubleArrayOf(
        viewport.centerX,
        viewport.centerY,
        viewport.span,
        viewportAspectRatio,
        power,
        bailout,
        supersampling.toDouble(),
        coloring.stableId.toDouble(),
        paletteCycles,
        paletteOffset,
        insideColor.toDouble(),
        juliaReal,
        juliaImaginary,
        phoenixReal,
        phoenixImaginary,
        novaRelaxation,
        camera.yaw,
        camera.pitch,
        camera.distance,
        camera.targetX,
        camera.targetY,
        camera.targetZ,
        quaternionConstant.x,
        quaternionConstant.y,
        quaternionConstant.z,
        quaternionConstant.w,
        fieldOfViewDegrees
    )

    companion object {
        const val DEFAULT_ITERATIONS = 320
        const val DEFAULT_BAILOUT = 4.0
        const val DEFAULT_INSIDE_COLOR = -0x1000000
        const val MAX_OUTPUT_DIMENSION = 8_192
        const val MAX_OUTPUT_PIXELS = 16_777_216L

        fun defaultPalette(): IntArray = intArrayOf(
            0xFF05051A.toInt(),
            0xFF123EAB.toInt(),
            0xFF26BCE1.toInt(),
            0xFFF8E16C.toInt(),
            0xFFF06A24.toInt(),
            0xFF3A0812.toInt()
        )
    }
}
