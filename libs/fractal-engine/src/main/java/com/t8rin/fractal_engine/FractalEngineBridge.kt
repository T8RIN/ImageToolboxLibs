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
import kotlin.math.roundToInt

/**
 * Stable primitive/Bitmap facade for optional runtime integration.
 *
 * A session is single-use. Always call [destroySession] in `finally`; [cancel] and
 * [destroySession] are safe to call concurrently with [renderIntoBlocking].
 */
object FractalEngineBridge {

    const val API_VERSION = 1

    const val RESULT_COMPLETED = 0
    const val RESULT_CANCELLED = 1

    const val TYPE_MANDELBROT = 1
    const val TYPE_JULIA = 2
    const val TYPE_BURNING_SHIP = 3
    const val TYPE_TRICORN = 4
    const val TYPE_MULTIBROT = 5
    const val TYPE_MULTICORN = 6
    const val TYPE_CELTIC = 7
    const val TYPE_BUFFALO = 8
    const val TYPE_PERPENDICULAR_BURNING_SHIP = 9
    const val TYPE_PHOENIX = 10
    const val TYPE_NOVA = 11
    const val TYPE_NEWTON = 12
    const val TYPE_MAGNET_I = 101
    const val TYPE_MAGNET_II = 102
    const val TYPE_LYAPUNOV = 103
    const val TYPE_SIERPINSKI_CARPET = 104
    const val TYPE_SIERPINSKI_TRIANGLE = 105
    const val TYPE_MANDELBULB = 1001
    const val TYPE_MANDELBOX = 1002
    const val TYPE_MENGER_SPONGE = 1003
    const val TYPE_SIERPINSKI_TETRAHEDRON = 1004
    const val TYPE_QUATERNION_JULIA = 1005

    const val COLORING_SMOOTH = 0
    const val COLORING_BANDED = 1
    const val COLORING_ORBIT_TRAP = 2
    const val COLORING_ANGLE = 3

    const val PARAM_CENTER_X = 0
    const val PARAM_CENTER_Y = 1
    const val PARAM_VERTICAL_SPAN = 2
    const val PARAM_VIEWPORT_ASPECT_RATIO = 3
    const val PARAM_POWER = 4
    const val PARAM_BAILOUT = 5
    const val PARAM_SUPERSAMPLING = 6
    const val PARAM_COLORING_ID = 7
    const val PARAM_PALETTE_CYCLES = 8
    const val PARAM_PALETTE_OFFSET = 9
    const val PARAM_INSIDE_COLOR_ARGB = 10
    const val PARAM_JULIA_REAL = 11
    const val PARAM_JULIA_IMAGINARY = 12
    const val PARAM_PHOENIX_REAL = 13
    const val PARAM_PHOENIX_IMAGINARY = 14
    const val PARAM_NOVA_RELAXATION = 15
    const val PARAM_CAMERA_YAW = 16
    const val PARAM_CAMERA_PITCH = 17
    const val PARAM_CAMERA_DISTANCE = 18
    const val PARAM_CAMERA_TARGET_X = 19
    const val PARAM_CAMERA_TARGET_Y = 20
    const val PARAM_CAMERA_TARGET_Z = 21
    const val PARAM_QUATERNION_X = 22
    const val PARAM_QUATERNION_Y = 23
    const val PARAM_QUATERNION_Z = 24
    const val PARAM_QUATERNION_W = 25
    const val PARAM_FIELD_OF_VIEW_DEGREES = 26
    const val REQUIRED_PARAMETER_COUNT = 27
    const val MAX_RENDER_WORK_UNITS = 500_000_000L

    private val typeIds = intArrayOf(
        TYPE_MANDELBROT,
        TYPE_JULIA,
        TYPE_BURNING_SHIP,
        TYPE_TRICORN,
        TYPE_MULTIBROT,
        TYPE_MULTICORN,
        TYPE_CELTIC,
        TYPE_BUFFALO,
        TYPE_PERPENDICULAR_BURNING_SHIP,
        TYPE_PHOENIX,
        TYPE_NOVA,
        TYPE_NEWTON,
        TYPE_MAGNET_I,
        TYPE_MAGNET_II,
        TYPE_LYAPUNOV,
        TYPE_SIERPINSKI_CARPET,
        TYPE_SIERPINSKI_TRIANGLE,
        TYPE_MANDELBULB,
        TYPE_MANDELBOX,
        TYPE_MENGER_SPONGE,
        TYPE_SIERPINSKI_TETRAHEDRON,
        TYPE_QUATERNION_JULIA
    )

    @JvmStatic
    fun isAvailable(): Boolean = NativeFractalEngine.tryLoad()

    @JvmStatic
    fun apiVersion(): Int = NativeFractalEngine.apiVersion()

    @JvmStatic
    fun supportedTypeIds(): IntArray = if (apiVersion() == API_VERSION) {
        typeIds.copyOf()
    } else {
        intArrayOf()
    }

    @JvmStatic
    fun createSession(): Long {
        NativeFractalEngine.requireLoaded()
        return NativeFractalEngine.nativeCreateSession().also { handle ->
            if (handle == 0L) throw FractalEngineException("Unable to allocate render session")
        }
    }

    @JvmStatic
    fun cancel(sessionHandle: Long) {
        if (sessionHandle == 0L || !NativeFractalEngine.tryLoad()) return
        NativeFractalEngine.nativeCancel(sessionHandle)
    }

    @JvmStatic
    fun destroySession(sessionHandle: Long) {
        if (sessionHandle == 0L || !NativeFractalEngine.tryLoad()) return
        NativeFractalEngine.nativeDestroySession(sessionHandle)
    }

    /**
     * Renders synchronously into the exact [bitmap] instance. Dimensions come from the bitmap;
     * [parameters] must use the exact public `PARAM_*` v1 layout above.
     */
    @JvmStatic
    fun renderIntoBlocking(
        sessionHandle: Long,
        bitmap: Bitmap,
        typeId: Int,
        maxIterations: Int,
        parameters: DoubleArray,
        palette: IntArray,
        lyapunovSequence: String
    ): Int {
        require(sessionHandle != 0L) { "Session handle must not be zero" }
        require(!bitmap.isRecycled) { "Bitmap is recycled" }
        require(bitmap.isMutable) { "Bitmap must be mutable" }
        require(bitmap.config == Bitmap.Config.ARGB_8888) { "Bitmap must use ARGB_8888" }
        validateArguments(
            bitmapWidth = bitmap.width,
            bitmapHeight = bitmap.height,
            typeId = typeId,
            maxIterations = maxIterations,
            parameters = parameters,
            palette = palette,
            lyapunovSequence = lyapunovSequence
        )
        NativeFractalEngine.requireLoaded()

        return when (
            val result = NativeFractalEngine.nativeRenderInto(
                sessionHandle = sessionHandle,
                bitmap = bitmap,
                typeId = typeId,
                maxIterations = maxIterations,
                parameters = parameters.copyOf(),
                palette = palette.copyOf(),
                lyapunovSequence = lyapunovSequence
            )
        ) {
            RESULT_COMPLETED, RESULT_CANCELLED -> result
            ERROR_INVALID_SESSION -> throw FractalEngineException("Invalid render session")
            ERROR_BITMAP -> throw FractalEngineException("Native bitmap lock failed")
            ERROR_UNSUPPORTED_TYPE -> throw FractalEngineException("Unsupported fractal type: $typeId")
            ERROR_INVALID_ARGUMENT -> throw FractalEngineException("Native argument validation failed")
            ERROR_SESSION_CONSUMED -> throw FractalEngineException("Render session was already used")
            ERROR_WORK_LIMIT -> throw FractalEngineException("Native render work limit exceeded")
            else -> throw FractalEngineException("Native render failed with code $result")
        }
    }

    internal fun validateArguments(
        bitmapWidth: Int,
        bitmapHeight: Int,
        typeId: Int,
        maxIterations: Int,
        parameters: DoubleArray,
        palette: IntArray,
        lyapunovSequence: String
    ) {
        require(bitmapWidth in 1..FractalRenderRequest.MAX_OUTPUT_DIMENSION)
        require(bitmapHeight in 1..FractalRenderRequest.MAX_OUTPUT_DIMENSION)
        require(bitmapWidth.toLong() * bitmapHeight <= FractalRenderRequest.MAX_OUTPUT_PIXELS)
        require(typeId in typeIds) { "Unsupported fractal type: $typeId" }
        require(maxIterations in 1..MAX_ITERATIONS) {
            "Iterations must be in 1..$MAX_ITERATIONS, was $maxIterations"
        }
        require(parameters.size == REQUIRED_PARAMETER_COUNT) {
            "Expected exactly $REQUIRED_PARAMETER_COUNT parameters, got ${parameters.size}"
        }
        require(parameters.all(Double::isFinite)) { "Parameters must be finite" }
        require(parameters[PARAM_VERTICAL_SPAN] > 0.0) { "Vertical span must be positive" }
        require(parameters[PARAM_VIEWPORT_ASPECT_RATIO] in MIN_ASPECT_RATIO..MAX_ASPECT_RATIO) {
            "Viewport aspect ratio is out of range"
        }
        require(parameters[PARAM_POWER] in MIN_POWER..MAX_POWER) { "Power is out of range" }
        require(parameters[PARAM_BAILOUT] in MIN_BAILOUT..MAX_BAILOUT) {
            "Bailout is out of range"
        }
        val supersampling = parameters[PARAM_SUPERSAMPLING].roundToInt()
        require(parameters[PARAM_SUPERSAMPLING] == supersampling.toDouble()) {
            "Supersampling must be an integer"
        }
        require(supersampling in 1..MAX_SUPERSAMPLING) { "Supersampling is out of range" }
        val workUnits = bitmapWidth.toLong() * bitmapHeight * supersampling * supersampling *
            maxIterations
        require(workUnits <= MAX_RENDER_WORK_UNITS) {
            "Render requires $workUnits work units, limit is $MAX_RENDER_WORK_UNITS"
        }
        val coloringId = parameters[PARAM_COLORING_ID].roundToInt()
        require(parameters[PARAM_COLORING_ID] == coloringId.toDouble()) {
            "Coloring id must be an integer"
        }
        require(coloringId in COLORING_SMOOTH..COLORING_ANGLE) { "Unknown coloring id" }
        require(parameters[PARAM_PALETTE_CYCLES] in MIN_PALETTE_CYCLES..MAX_PALETTE_CYCLES) {
            "Palette cycles are out of range"
        }
        require(parameters[PARAM_NOVA_RELAXATION] in MIN_NOVA_RELAXATION..MAX_NOVA_RELAXATION) {
            "Nova relaxation is out of range"
        }
        require(parameters[PARAM_CAMERA_DISTANCE] > 0.0) { "Camera distance must be positive" }
        require(parameters[PARAM_FIELD_OF_VIEW_DEGREES] in MIN_FIELD_OF_VIEW..MAX_FIELD_OF_VIEW) {
            "Field of view is out of range"
        }
        require(palette.size in 2..MAX_PALETTE_COLORS) {
            "Palette must contain 2..$MAX_PALETTE_COLORS colors"
        }
        require(lyapunovSequence.length in 1..MAX_LYAPUNOV_SEQUENCE_LENGTH) {
            "Lyapunov sequence length is out of range"
        }
        require(lyapunovSequence.all { it == 'A' || it == 'B' }) {
            "Lyapunov sequence may only contain A and B"
        }
    }

    private const val ERROR_INVALID_SESSION = -1
    private const val ERROR_BITMAP = -2
    private const val ERROR_UNSUPPORTED_TYPE = -3
    private const val ERROR_INVALID_ARGUMENT = -4
    private const val ERROR_SESSION_CONSUMED = -5
    private const val ERROR_WORK_LIMIT = -7

    private const val MAX_ITERATIONS = 16_384
    private const val MIN_ASPECT_RATIO = 1.0 / 8_192.0
    private const val MAX_ASPECT_RATIO = 8_192.0
    private const val MIN_POWER = 2.0
    private const val MAX_POWER = 16.0
    private const val MIN_BAILOUT = 2.0
    private const val MAX_BAILOUT = 1.0E12
    private const val MAX_SUPERSAMPLING = 4
    private const val MIN_PALETTE_CYCLES = 0.01
    private const val MAX_PALETTE_CYCLES = 64.0
    private const val MIN_NOVA_RELAXATION = 0.01
    private const val MAX_NOVA_RELAXATION = 4.0
    private const val MIN_FIELD_OF_VIEW = 10.0
    private const val MAX_FIELD_OF_VIEW = 120.0
    private const val MAX_PALETTE_COLORS = 64
    private const val MAX_LYAPUNOV_SEQUENCE_LENGTH = 64
}
