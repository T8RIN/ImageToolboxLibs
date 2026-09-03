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
    val defaultCamera: FractalCamera = FractalCamera(),
    val defaultMaxIterations: Int = FractalRenderRequest.DEFAULT_ITERATIONS,
    val defaultBailout: Double = FractalRenderRequest.DEFAULT_BAILOUT,
    val defaultJuliaReal: Double = -0.8,
    val defaultJuliaImaginary: Double = 0.156,
    val defaultPhoenixReal: Double = -0.5,
    val defaultPhoenixImaginary: Double = 0.0,
    val defaultQuaternionConstant: QuaternionConstant = QuaternionConstant()
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
    BurningShipJulia(
        stableId = FractalEngineBridge.TYPE_BURNING_SHIP_JULIA,
        dimension = FractalDimension.TwoDimensional,
        defaultViewport = FractalViewport(centerX = 0.0, centerY = 0.0, span = 3.0)
    ),
    CelticJulia(
        stableId = FractalEngineBridge.TYPE_CELTIC_JULIA,
        dimension = FractalDimension.TwoDimensional,
        defaultViewport = FractalViewport(centerX = 0.0, centerY = 0.0, span = 3.0)
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
    /** The catalog's pure tetrahedral-fold “Sierpinski Pyramid”; the stable name is retained. */
    SierpinskiTetrahedron(
        stableId = FractalEngineBridge.TYPE_SIERPINSKI_TETRAHEDRON,
        dimension = FractalDimension.ThreeDimensional,
        defaultViewport = FractalViewport(centerX = 0.0, centerY = 0.0, span = 2.0),
        defaultCamera = FractalCamera(yaw = 0.65, pitch = 0.35, distance = 3.8)
    ),
    /** The catalog's quadratic “Julia Set 3D”; the stable name is retained. */
    QuaternionJulia(
        stableId = FractalEngineBridge.TYPE_QUATERNION_JULIA,
        dimension = FractalDimension.ThreeDimensional,
        defaultViewport = FractalViewport(centerX = 0.0, centerY = 0.0, span = 2.0),
        defaultCamera = FractalCamera(yaw = 0.65, pitch = 0.25, distance = 3.4)
    ),
    Collatz(
        stableId = FractalEngineBridge.TYPE_COLLATZ,
        dimension = FractalDimension.TwoDimensional,
        defaultViewport = FractalViewport(centerX = 0.0, centerY = 0.0, span = 4.0),
        defaultMaxIterations = 160,
        defaultBailout = 100.0
    ),
    Buddhabrot(
        stableId = FractalEngineBridge.TYPE_BUDDHABROT,
        dimension = FractalDimension.TwoDimensional,
        defaultViewport = FractalViewport(
            centerX = 0.4,
            centerY = 0.0,
            span = 4.444444444444445
        ),
        defaultMaxIterations = 800,
        defaultBailout = 2.0
    ),
    Hopalong(
        stableId = FractalEngineBridge.TYPE_HOPALONG,
        dimension = FractalDimension.TwoDimensional,
        defaultViewport = FractalViewport(centerX = -0.1, centerY = 0.5, span = 4.2),
        defaultPower = 0.0,
        defaultMaxIterations = 1_000,
        defaultJuliaReal = 0.4,
        defaultJuliaImaginary = 1.0
    ),
    Martin(
        stableId = FractalEngineBridge.TYPE_MARTIN,
        dimension = FractalDimension.TwoDimensional,
        defaultViewport = FractalViewport(
            centerX = Math.PI / 2.0,
            centerY = Math.PI / 2.0,
            span = 145.0
        ),
        defaultMaxIterations = 1_000,
        defaultJuliaReal = Math.PI
    ),
    Gingerbreadman(
        stableId = FractalEngineBridge.TYPE_GINGERBREADMAN,
        dimension = FractalDimension.TwoDimensional,
        defaultViewport = FractalViewport(centerX = 2.5, centerY = 2.5, span = 12.0),
        defaultMaxIterations = 1_000
    ),
    Chip(
        stableId = FractalEngineBridge.TYPE_CHIP,
        dimension = FractalDimension.TwoDimensional,
        defaultViewport = FractalViewport(centerX = -7.2, centerY = -7.9, span = 720.0),
        defaultPower = 1.0,
        defaultMaxIterations = 1_000,
        defaultJuliaReal = -15.0,
        defaultJuliaImaginary = -19.0
    ),
    Quadruptwo(
        stableId = FractalEngineBridge.TYPE_QUADRUPTWO,
        dimension = FractalDimension.TwoDimensional,
        defaultViewport = FractalViewport(centerX = 16.5, centerY = 17.5, span = 200.0),
        defaultPower = 5.0,
        defaultMaxIterations = 1_000,
        defaultJuliaReal = 34.0,
        defaultJuliaImaginary = 1.0
    ),
    Threeply(
        stableId = FractalEngineBridge.TYPE_THREEPLY,
        dimension = FractalDimension.TwoDimensional,
        defaultViewport = FractalViewport(centerX = -34.0, centerY = -21.0, span = 5_500.0),
        defaultPower = -42.0,
        defaultMaxIterations = 1_000,
        defaultJuliaReal = -55.0,
        defaultJuliaImaginary = -1.0
    ),
    OctahedralIFS(
        stableId = FractalEngineBridge.TYPE_OCTAHEDRAL_IFS,
        dimension = FractalDimension.ThreeDimensional,
        defaultViewport = FractalViewport(centerX = 0.0, centerY = 0.0, span = 2.0),
        defaultCamera = FractalCamera(yaw = 0.65, pitch = 0.35, distance = 9.0),
        defaultMaxIterations = 160,
        defaultJuliaReal = 2.0,
        defaultJuliaImaginary = 1.2
    ),
    IcosahedralIFS(
        stableId = FractalEngineBridge.TYPE_ICOSAHEDRAL_IFS,
        dimension = FractalDimension.ThreeDimensional,
        defaultViewport = FractalViewport(centerX = 0.0, centerY = 0.0, span = 2.0),
        defaultCamera = FractalCamera(yaw = 0.65, pitch = 0.35, distance = 9.0),
        defaultMaxIterations = 160,
        defaultJuliaReal = 1.7,
        defaultJuliaImaginary = 1.5
    ),
    ApollonianGasket(
        stableId = FractalEngineBridge.TYPE_APOLLONIAN_GASKET,
        dimension = FractalDimension.ThreeDimensional,
        defaultViewport = FractalViewport(centerX = 0.0, centerY = 0.0, span = 2.0),
        defaultPower = 1.12,
        defaultCamera = FractalCamera(yaw = 0.65, pitch = 0.35, distance = 8.0),
        defaultMaxIterations = 160,
        defaultJuliaReal = 1.3,
        defaultJuliaImaginary = 1.35
    ),
    Kleinian(
        stableId = FractalEngineBridge.TYPE_KLEINIAN,
        dimension = FractalDimension.ThreeDimensional,
        defaultViewport = FractalViewport(centerX = 0.0, centerY = 0.0, span = 2.0),
        defaultPower = 0.5,
        defaultCamera = FractalCamera(yaw = 0.65, pitch = 0.3, distance = 5.0),
        defaultMaxIterations = 192,
        defaultJuliaReal = 1.5,
        defaultJuliaImaginary = 1.0
    ),
    HybridMandelbulbJulia(
        stableId = FractalEngineBridge.TYPE_HYBRID_MANDELBULB_JULIA,
        dimension = FractalDimension.ThreeDimensional,
        defaultViewport = FractalViewport(centerX = 0.0, centerY = 0.0, span = 2.0),
        defaultPower = 8.0,
        defaultCamera = FractalCamera(yaw = 0.65, pitch = 0.3, distance = 4.0),
        defaultMaxIterations = 192,
        defaultJuliaReal = -0.2,
        defaultJuliaImaginary = 0.8
    ),
    QuaternionCubic(
        stableId = FractalEngineBridge.TYPE_QUATERNION_CUBIC,
        dimension = FractalDimension.ThreeDimensional,
        defaultViewport = FractalViewport(centerX = 0.0, centerY = 0.0, span = 2.0),
        defaultCamera = FractalCamera(yaw = 0.65, pitch = 0.3, distance = 3.5),
        defaultMaxIterations = 256,
        defaultQuaternionConstant = QuaternionConstant(x = -0.2, y = 0.6, z = 0.3, w = 0.0)
    ),
    SierpinskiGasket(
        stableId = FractalEngineBridge.TYPE_SIERPINSKI_GASKET,
        dimension = FractalDimension.ThreeDimensional,
        defaultViewport = FractalViewport(centerX = 0.0, centerY = 0.0, span = 2.0),
        defaultPower = 0.5,
        defaultCamera = FractalCamera(yaw = 0.65, pitch = 0.35, distance = 5.0),
        defaultMaxIterations = 128,
        defaultJuliaReal = 1.5,
        defaultJuliaImaginary = 1.0
    ),
    Pickover(
        stableId = FractalEngineBridge.TYPE_PICKOVER,
        dimension = FractalDimension.ThreeDimensional,
        defaultViewport = FractalViewport(centerX = 0.0, centerY = 0.0, span = 2.0),
        defaultPower = -0.65,
        defaultCamera = FractalCamera(yaw = 0.65, pitch = 0.3, distance = 3.0),
        defaultMaxIterations = 1_000,
        defaultJuliaReal = 2.24,
        defaultJuliaImaginary = 0.43,
        defaultPhoenixReal = -2.43
    ),
    Lorenz(
        stableId = FractalEngineBridge.TYPE_LORENZ,
        dimension = FractalDimension.ThreeDimensional,
        defaultViewport = FractalViewport(centerX = 0.0, centerY = 0.0, span = 2.0),
        defaultPower = 8.0 / 3.0,
        defaultCamera = FractalCamera(yaw = 0.65, pitch = 0.25, distance = 5.0),
        defaultMaxIterations = 1_000,
        defaultJuliaReal = 10.0,
        defaultJuliaImaginary = 28.0
    ),
    Rossler(
        stableId = FractalEngineBridge.TYPE_ROSSLER,
        dimension = FractalDimension.ThreeDimensional,
        defaultViewport = FractalViewport(centerX = 0.0, centerY = 0.0, span = 2.0),
        defaultPower = 5.7,
        defaultCamera = FractalCamera(yaw = 0.65, pitch = 0.3, distance = 5.0),
        defaultMaxIterations = 1_000,
        defaultJuliaReal = 0.2,
        defaultJuliaImaginary = 0.2
    );

    companion object {
        fun fromStableId(stableId: Int): FractalType? = entries.firstOrNull {
            it.stableId == stableId
        }
    }
}

enum class FractalDimension {
    TwoDimensional,
    ThreeDimensional
}
