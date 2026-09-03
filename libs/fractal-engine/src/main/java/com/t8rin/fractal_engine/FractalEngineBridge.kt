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

    const val API_VERSION = 5

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
    const val TYPE_BURNING_SHIP_JULIA = 106
    const val TYPE_CELTIC_JULIA = 107
    const val TYPE_COLLATZ = 108
    const val TYPE_BUDDHABROT = 109
    const val TYPE_HOPALONG = 110
    const val TYPE_MARTIN = 111
    const val TYPE_GINGERBREADMAN = 112
    const val TYPE_CHIP = 113
    const val TYPE_QUADRUPTWO = 114
    const val TYPE_THREEPLY = 115
    const val TYPE_MANDELBULB = 1001
    const val TYPE_MANDELBOX = 1002
    const val TYPE_MENGER_SPONGE = 1003
    const val TYPE_SIERPINSKI_TETRAHEDRON = 1004
    const val TYPE_QUATERNION_JULIA = 1005
    const val TYPE_OCTAHEDRAL_IFS = 1006
    const val TYPE_ICOSAHEDRAL_IFS = 1007
    const val TYPE_APOLLONIAN_GASKET = 1008
    const val TYPE_KLEINIAN = 1009
    const val TYPE_HYBRID_MANDELBULB_JULIA = 1010
    const val TYPE_QUATERNION_CUBIC = 1011
    const val TYPE_SIERPINSKI_GASKET = 1012
    const val TYPE_PICKOVER = 1013
    const val TYPE_LORENZ = 1014
    const val TYPE_ROSSLER = 1015

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
    const val PARAM_SHOW_FLOOR = 27
    const val PARAM_FLOOR_COLOR_ARGB = 28
    const val REQUIRED_PARAMETER_COUNT = 29
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
        TYPE_BURNING_SHIP_JULIA,
        TYPE_CELTIC_JULIA,
        TYPE_MANDELBULB,
        TYPE_MANDELBOX,
        TYPE_MENGER_SPONGE,
        TYPE_SIERPINSKI_TETRAHEDRON,
        TYPE_QUATERNION_JULIA,
        TYPE_COLLATZ,
        TYPE_BUDDHABROT,
        TYPE_HOPALONG,
        TYPE_MARTIN,
        TYPE_GINGERBREADMAN,
        TYPE_CHIP,
        TYPE_QUADRUPTWO,
        TYPE_THREEPLY,
        TYPE_OCTAHEDRAL_IFS,
        TYPE_ICOSAHEDRAL_IFS,
        TYPE_APOLLONIAN_GASKET,
        TYPE_KLEINIAN,
        TYPE_HYBRID_MANDELBULB_JULIA,
        TYPE_QUATERNION_CUBIC,
        TYPE_SIERPINSKI_GASKET,
        TYPE_PICKOVER,
        TYPE_LORENZ,
        TYPE_ROSSLER
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
     * [parameters] must use the exact public 27-field `PARAM_*` layout above. This overload derives
     * decimal viewport from the legacy `Double` fields; use the typed overload for deep zoom.
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
    ): Int = renderIntoBlocking(
        sessionHandle = sessionHandle,
        bitmap = bitmap,
        typeId = typeId,
        maxIterations = maxIterations,
        parameters = parameters,
        exactViewport = FractalExactViewport.fromDoubles(
            centerX = parameters.getOrElse(PARAM_CENTER_X) { Double.NaN },
            centerY = parameters.getOrElse(PARAM_CENTER_Y) { Double.NaN },
            span = parameters.getOrElse(PARAM_VERTICAL_SPAN) { Double.NaN }
        ),
        palette = palette,
        lyapunovSequence = lyapunovSequence
    )

    /** Renders with decimal viewport coordinates preserved beyond `Double` precision. */
    @JvmStatic
    fun renderIntoBlocking(
        sessionHandle: Long,
        bitmap: Bitmap,
        typeId: Int,
        maxIterations: Int,
        parameters: DoubleArray,
        exactViewport: FractalExactViewport,
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
            exactViewport = exactViewport,
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
                exactCenterX = exactViewport.centerX,
                exactCenterY = exactViewport.centerY,
                exactSpan = exactViewport.span,
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
            ERROR_ALLOCATION -> throw FractalEngineException("Native render allocation failed")
            else -> throw FractalEngineException("Native render failed with code $result")
        }
    }

    internal fun validateArguments(
        bitmapWidth: Int,
        bitmapHeight: Int,
        typeId: Int,
        maxIterations: Int,
        parameters: DoubleArray,
        exactViewport: FractalExactViewport? = null,
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
        exactViewport?.validateAgainst(
            FractalViewport(
                centerX = parameters[PARAM_CENTER_X],
                centerY = parameters[PARAM_CENTER_Y],
                span = parameters[PARAM_VERTICAL_SPAN],
                exact = exactViewport
            )
        )
        require(parameters[PARAM_VIEWPORT_ASPECT_RATIO] in MIN_ASPECT_RATIO..MAX_ASPECT_RATIO) {
            "Viewport aspect ratio is out of range"
        }
        validateFormulaParameters(typeId, parameters)
        require(parameters[PARAM_BAILOUT] in MIN_BAILOUT..MAX_BAILOUT) {
            "Bailout is out of range"
        }
        val supersampling = parameters[PARAM_SUPERSAMPLING].roundToInt()
        require(parameters[PARAM_SUPERSAMPLING] == supersampling.toDouble()) {
            "Supersampling must be an integer"
        }
        require(supersampling in 1..MAX_SUPERSAMPLING) { "Supersampling is out of range" }
        val workUnits = estimatedWorkUnits(
            typeId = typeId,
            pixelCount = bitmapWidth.toLong() * bitmapHeight,
            supersampling = supersampling,
            maxIterations = maxIterations
        )
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
        require(parameters[PARAM_SHOW_FLOOR] == 0.0 || parameters[PARAM_SHOW_FLOOR] == 1.0) {
            "Show floor flag must be 0 or 1"
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
    private const val ERROR_ALLOCATION = -8

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

    private fun validateFormulaParameters(typeId: Int, parameters: DoubleArray) {
        val a = parameters[PARAM_JULIA_REAL]
        val b = parameters[PARAM_JULIA_IMAGINARY]
        val c = parameters[PARAM_POWER]
        val d = parameters[PARAM_PHOENIX_REAL]
        when (typeId) {
            TYPE_HOPALONG -> {
                require(a in -10.0..10.0) { "Hopalong coefficient A is out of range" }
                require(b in -10.0..10.0) { "Hopalong coefficient B is out of range" }
                require(c in -10.0..10.0) { "Hopalong coefficient C is out of range" }
            }
            TYPE_MARTIN -> {
                require(a in -10.0..10.0) { "Martin coefficient A is out of range" }
                require(c in MIN_POWER..MAX_POWER) { "Power is out of range" }
            }
            TYPE_CHIP, TYPE_QUADRUPTWO, TYPE_THREEPLY -> {
                require(a in -100.0..100.0) { "Attractor coefficient A is out of range" }
                require(b in -100.0..100.0) { "Attractor coefficient B is out of range" }
                require(c in -100.0..100.0) { "Attractor coefficient C is out of range" }
            }
            TYPE_OCTAHEDRAL_IFS, TYPE_ICOSAHEDRAL_IFS -> {
                require(a in 0.5..5.0) { "IFS scale is out of range" }
                require(b in 0.5..3.0) { "IFS fold is out of range" }
                require(c in MIN_POWER..MAX_POWER) { "Power is out of range" }
            }
            TYPE_APOLLONIAN_GASKET -> {
                require(a in 0.5..3.0) { "Apollonian scale is out of range" }
                require(b in 0.8..2.0) { "Apollonian fold is out of range" }
                require(c in 0.1..2.0) { "Apollonian minimum radius is out of range" }
            }
            TYPE_KLEINIAN -> {
                require(a in 0.5..3.0) { "Kleinian scale is out of range" }
                require(b in 0.5..2.0) { "Kleinian fold is out of range" }
                require(c in 0.5..3.0) { "Kleinian minimum radius is out of range" }
            }
            TYPE_SIERPINSKI_GASKET -> {
                require(a in 0.5..5.0) { "Sierpinski Gasket scale is out of range" }
                require(b in 0.0..2.0) { "Sierpinski Gasket fold is out of range" }
                require(c in 0.1..2.0) { "Sierpinski Gasket minimum radius is out of range" }
            }
            TYPE_PICKOVER -> {
                require(a in -10.0..10.0) { "Pickover coefficient A is out of range" }
                require(b in -10.0..10.0) { "Pickover coefficient B is out of range" }
                require(c in -10.0..10.0) { "Pickover coefficient C is out of range" }
                require(d in -10.0..10.0) { "Pickover coefficient D is out of range" }
            }
            TYPE_LORENZ -> {
                require(a in 0.1..30.0) { "Lorenz sigma is out of range" }
                require(b in 0.1..100.0) { "Lorenz rho is out of range" }
                require(c in 0.1..10.0) { "Lorenz beta is out of range" }
            }
            TYPE_ROSSLER -> {
                require(a in -1.0..1.0) { "Rossler coefficient A is out of range" }
                require(b in 0.01..2.0) { "Rossler coefficient B is out of range" }
                require(c in 0.1..20.0) { "Rossler coefficient C is out of range" }
            }
            TYPE_HYBRID_MANDELBULB_JULIA -> {
                require(a in -2.0..2.0) { "Hybrid Julia real component is out of range" }
                require(b in -2.0..2.0) { "Hybrid Julia imaginary component is out of range" }
                require(c in MIN_POWER..MAX_POWER) { "Power is out of range" }
            }
            TYPE_QUATERNION_CUBIC -> {
                require(c in MIN_POWER..MAX_POWER) { "Power is out of range" }
                parameters.sliceArray(PARAM_QUATERNION_X..PARAM_QUATERNION_W).forEach {
                    require(it in -2.0..2.0) { "Quaternion constant is out of range" }
                }
            }
            else -> require(c in MIN_POWER..MAX_POWER) { "Power is out of range" }
        }
    }

    internal fun estimatedWorkUnits(
        typeId: Int,
        pixelCount: Long,
        supersampling: Int,
        maxIterations: Int
    ): Long {
        val supersamplingSquared = supersampling.toLong() * supersampling
        val sampledPixels = pixelCount * supersamplingSquared
        return when (typeId) {
            TYPE_BUDDHABROT -> {
                val orbitIterations = maxIterations.coerceAtLeast(32).toLong()
                val desiredSamples = maxOf(8_192L, sampledPixels / 2L)
                val affordableSamples = (MAX_RENDER_WORK_UNITS - pixelCount) / orbitIterations
                val samples = minOf(desiredSamples, affordableSamples)
                pixelCount + samples * orbitIterations
            }
            TYPE_HOPALONG,
            TYPE_MARTIN,
            TYPE_GINGERBREADMAN,
            TYPE_CHIP,
            TYPE_QUADRUPTWO,
            TYPE_THREEPLY -> {
                val base = (maxIterations.toLong() * 128L).coerceIn(24_000L, 1_000_000L)
                val desiredSteps = base * densityResolutionMultiplier(
                    pixelCount = pixelCount,
                    supersamplingSquared = supersamplingSquared
                )
                val steps = minOf(
                    desiredSteps,
                    (MAX_RENDER_WORK_UNITS - pixelCount) / 25L
                )
                pixelCount + steps * 25L
            }
            TYPE_PICKOVER,
            TYPE_LORENZ,
            TYPE_ROSSLER -> {
                val base = (maxIterations.toLong() * 32L).coerceIn(12_000L, 240_000L)
                val desiredSteps = base * densityResolutionMultiplier(
                    pixelCount = pixelCount,
                    supersamplingSquared = supersamplingSquared
                )
                val steps = minOf(
                    desiredSteps,
                    (MAX_RENDER_WORK_UNITS - pixelCount) / 9L
                )
                pixelCount + steps * 9L
            }
            TYPE_MANDELBULB,
            TYPE_MANDELBOX,
            TYPE_MENGER_SPONGE,
            TYPE_SIERPINSKI_TETRAHEDRON,
            TYPE_QUATERNION_JULIA,
            TYPE_OCTAHEDRAL_IFS,
            TYPE_ICOSAHEDRAL_IFS,
            TYPE_APOLLONIAN_GASKET,
            TYPE_KLEINIAN,
            TYPE_HYBRID_MANDELBULB_JULIA,
            TYPE_QUATERNION_CUBIC,
            TYPE_SIERPINSKI_GASKET -> {
                val plan = rayMarchPlan(
                    typeId = typeId,
                    maxIterations = maxIterations,
                    sampledPixels = sampledPixels
                ) ?: return Long.MAX_VALUE
                sampledPixels * (plan.maximumSteps + 6L) * plan.distanceIterations
            }
            else -> sampledPixels * maxIterations
        }
    }

    private fun densityResolutionMultiplier(
        pixelCount: Long,
        supersamplingSquared: Long
    ): Long {
        val sampledPixels = pixelCount * supersamplingSquared
        val ratioCeiling = ceilDiv(sampledPixels, DENSITY_REFERENCE_PIXELS)
        return ceilSqrt(ratioCeiling).coerceAtLeast(1L)
    }

    private fun ceilSqrt(value: Long): Long {
        var lower = 1L
        var upper = value.coerceAtLeast(1L)
        while (lower < upper) {
            val middle = lower + (upper - lower) / 2L
            if (middle >= ceilDiv(value, middle)) {
                upper = middle
            } else {
                lower = middle + 1L
            }
        }
        return lower
    }

    private fun ceilDiv(dividend: Long, divisor: Long): Long =
        dividend / divisor + if (dividend % divisor == 0L) 0L else 1L

    private data class RayMarchPlan(
        val maximumSteps: Long,
        val distanceIterations: Long
    )

    private fun rayMarchPlan(
        typeId: Int,
        maxIterations: Int,
        sampledPixels: Long
    ): RayMarchPlan? {
        val desiredSteps = maxIterations.coerceIn(24, 192).toLong()
        val minimumSteps = 24L
        val (desiredIterations, minimumIterations) = when (typeId) {
            TYPE_MENGER_SPONGE ->
                (maxIterations / 48).coerceIn(3, 7).toLong() to 3L
            TYPE_SIERPINSKI_TETRAHEDRON ->
                (maxIterations / 32).coerceIn(6, 16).toLong() to 6L
            TYPE_OCTAHEDRAL_IFS,
            TYPE_ICOSAHEDRAL_IFS,
            TYPE_APOLLONIAN_GASKET ->
                (maxIterations / 16).coerceIn(4, 18).toLong() to 4L
            TYPE_KLEINIAN ->
                (maxIterations / 16).coerceIn(6, 16).toLong() to 6L
            TYPE_HYBRID_MANDELBULB_JULIA,
            TYPE_QUATERNION_CUBIC ->
                (maxIterations / 16).coerceIn(6, 24).toLong() to 6L
            TYPE_SIERPINSKI_GASKET ->
                (maxIterations / 16).coerceIn(5, 16).toLong() to 5L
            TYPE_MANDELBULB,
            TYPE_MANDELBOX,
            TYPE_QUATERNION_JULIA ->
                (maxIterations / 16).coerceIn(8, 28).toLong() to 8L
            else -> return null
        }
        val allowance = MAX_RENDER_WORK_UNITS / sampledPixels
        if (allowance < (minimumSteps + 6L) * minimumIterations) return null

        var maximumSteps = desiredSteps
        var distanceIterations = desiredIterations
        if ((desiredSteps + 6L) * desiredIterations > allowance) {
            distanceIterations = (allowance / (desiredSteps + 6L))
                .coerceIn(minimumIterations, desiredIterations)
            if ((desiredSteps + 6L) * distanceIterations > allowance) {
                maximumSteps = (allowance / distanceIterations - 6L)
                    .coerceIn(minimumSteps, desiredSteps)
            }
        }
        return RayMarchPlan(maximumSteps, distanceIterations)
    }

    private const val DENSITY_REFERENCE_PIXELS = 640L * 480L
}
