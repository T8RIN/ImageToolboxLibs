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

/**
 * Fractal identifiers are part of the public wire format. Existing values must never be changed
 * or reused because [FractalEngineBridge] is intentionally callable through reflection.
 */
enum class FractalType(
    val stableId: Int,
    val dimension: FractalDimension,
    val defaultViewport: FractalViewport,
    val defaultPower: Double = 2.0,
    val defaultCamera: FractalCamera = FractalCamera()
) {
    Mandelbrot(
        stableId = FractalEngineBridge.TYPE_MANDELBROT,
        dimension = FractalDimension.TwoDimensional,
        defaultViewport = FractalViewport(centerX = -0.5, centerY = 0.0, span = 3.0)
    ),
    Julia(
        stableId = FractalEngineBridge.TYPE_JULIA,
        dimension = FractalDimension.TwoDimensional,
        defaultViewport = FractalViewport(centerX = 0.0, centerY = 0.0, span = 3.0)
    ),
    BurningShip(
        stableId = FractalEngineBridge.TYPE_BURNING_SHIP,
        dimension = FractalDimension.TwoDimensional,
        defaultViewport = FractalViewport(centerX = -0.45, centerY = -0.5, span = 2.4)
    ),
    Tricorn(
        stableId = FractalEngineBridge.TYPE_TRICORN,
        dimension = FractalDimension.TwoDimensional,
        defaultViewport = FractalViewport(centerX = 0.0, centerY = 0.0, span = 3.2)
    ),
    Multibrot(
        stableId = FractalEngineBridge.TYPE_MULTIBROT,
        dimension = FractalDimension.TwoDimensional,
        defaultViewport = FractalViewport(centerX = 0.0, centerY = 0.0, span = 3.0),
        defaultPower = 3.0
    ),
    Multicorn(
        stableId = FractalEngineBridge.TYPE_MULTICORN,
        dimension = FractalDimension.TwoDimensional,
        defaultViewport = FractalViewport(centerX = 0.0, centerY = 0.0, span = 3.2),
        defaultPower = 3.0
    ),
    Celtic(
        stableId = FractalEngineBridge.TYPE_CELTIC,
        dimension = FractalDimension.TwoDimensional,
        defaultViewport = FractalViewport(centerX = -0.4, centerY = 0.0, span = 3.0)
    ),
    Buffalo(
        stableId = FractalEngineBridge.TYPE_BUFFALO,
        dimension = FractalDimension.TwoDimensional,
        defaultViewport = FractalViewport(centerX = -0.5, centerY = 0.0, span = 3.0)
    ),
    PerpendicularBurningShip(
        stableId = FractalEngineBridge.TYPE_PERPENDICULAR_BURNING_SHIP,
        dimension = FractalDimension.TwoDimensional,
        defaultViewport = FractalViewport(centerX = -0.5, centerY = -0.25, span = 3.0)
    ),
    Phoenix(
        stableId = FractalEngineBridge.TYPE_PHOENIX,
        dimension = FractalDimension.TwoDimensional,
        defaultViewport = FractalViewport(centerX = 0.0, centerY = 0.0, span = 3.2)
    ),
    Nova(
        stableId = FractalEngineBridge.TYPE_NOVA,
        dimension = FractalDimension.TwoDimensional,
        defaultViewport = FractalViewport(centerX = 0.0, centerY = 0.0, span = 4.0),
        defaultPower = 3.0
    ),
    Newton(
        stableId = FractalEngineBridge.TYPE_NEWTON,
        dimension = FractalDimension.TwoDimensional,
        defaultViewport = FractalViewport(centerX = 0.0, centerY = 0.0, span = 4.0),
        defaultPower = 3.0
    ),
    MagnetI(
        stableId = FractalEngineBridge.TYPE_MAGNET_I,
        dimension = FractalDimension.TwoDimensional,
        defaultViewport = FractalViewport(centerX = 0.5, centerY = 0.0, span = 4.5)
    ),
    MagnetII(
        stableId = FractalEngineBridge.TYPE_MAGNET_II,
        dimension = FractalDimension.TwoDimensional,
        defaultViewport = FractalViewport(centerX = 0.5, centerY = 0.0, span = 4.5)
    ),
    Lyapunov(
        stableId = FractalEngineBridge.TYPE_LYAPUNOV,
        dimension = FractalDimension.TwoDimensional,
        defaultViewport = FractalViewport(centerX = 3.0, centerY = 3.0, span = 2.2)
    ),
    SierpinskiCarpet(
        stableId = FractalEngineBridge.TYPE_SIERPINSKI_CARPET,
        dimension = FractalDimension.TwoDimensional,
        defaultViewport = FractalViewport(centerX = 0.0, centerY = 0.0, span = 2.2)
    ),
    SierpinskiTriangle(
        stableId = FractalEngineBridge.TYPE_SIERPINSKI_TRIANGLE,
        dimension = FractalDimension.TwoDimensional,
        defaultViewport = FractalViewport(centerX = 0.0, centerY = 0.0, span = 2.2)
    ),
    Mandelbulb(
        stableId = FractalEngineBridge.TYPE_MANDELBULB,
        dimension = FractalDimension.ThreeDimensional,
        defaultViewport = FractalViewport(centerX = 0.0, centerY = 0.0, span = 2.0),
        defaultPower = 8.0,
        defaultCamera = FractalCamera(yaw = 0.65, pitch = 0.3, distance = 3.2)
    ),
    Mandelbox(
        stableId = FractalEngineBridge.TYPE_MANDELBOX,
        dimension = FractalDimension.ThreeDimensional,
        defaultViewport = FractalViewport(centerX = 0.0, centerY = 0.0, span = 2.0),
        defaultCamera = FractalCamera(yaw = 0.7, pitch = 0.35, distance = 4.5)
    ),
    MengerSponge(
        stableId = FractalEngineBridge.TYPE_MENGER_SPONGE,
        dimension = FractalDimension.ThreeDimensional,
        defaultViewport = FractalViewport(centerX = 0.0, centerY = 0.0, span = 2.0),
        defaultCamera = FractalCamera(yaw = 0.7, pitch = 0.45, distance = 3.6)
    ),
    SierpinskiTetrahedron(
        stableId = FractalEngineBridge.TYPE_SIERPINSKI_TETRAHEDRON,
        dimension = FractalDimension.ThreeDimensional,
        defaultViewport = FractalViewport(centerX = 0.0, centerY = 0.0, span = 2.0),
        defaultCamera = FractalCamera(yaw = 0.65, pitch = 0.35, distance = 3.8)
    ),
    QuaternionJulia(
        stableId = FractalEngineBridge.TYPE_QUATERNION_JULIA,
        dimension = FractalDimension.ThreeDimensional,
        defaultViewport = FractalViewport(centerX = 0.0, centerY = 0.0, span = 2.0),
        defaultCamera = FractalCamera(yaw = 0.65, pitch = 0.25, distance = 3.4)
    );

    val stableKey: String
        get() = when (this) {
            Mandelbrot -> "mandelbrot"
            Julia -> "julia"
            BurningShip -> "burning_ship"
            Tricorn -> "tricorn"
            Multibrot -> "multibrot"
            Multicorn -> "multicorn"
            Celtic -> "celtic"
            Buffalo -> "buffalo"
            PerpendicularBurningShip -> "perpendicular_burning_ship"
            Phoenix -> "phoenix"
            Nova -> "nova"
            Newton -> "newton"
            MagnetI -> "magnet_i"
            MagnetII -> "magnet_ii"
            Lyapunov -> "lyapunov"
            SierpinskiCarpet -> "sierpinski_carpet"
            SierpinskiTriangle -> "sierpinski_triangle"
            Mandelbulb -> "mandelbulb"
            Mandelbox -> "mandelbox"
            MengerSponge -> "menger_sponge"
            SierpinskiTetrahedron -> "sierpinski_tetrahedron"
            QuaternionJulia -> "quaternion_julia"
        }

    companion object {
        fun fromStableId(stableId: Int): FractalType? = entries.firstOrNull {
            it.stableId == stableId
        }

        fun fromStableKey(stableKey: String): FractalType? = entries.firstOrNull {
            it.stableKey == stableKey
        }
    }
}

enum class FractalDimension {
    TwoDimensional,
    ThreeDimensional
}
