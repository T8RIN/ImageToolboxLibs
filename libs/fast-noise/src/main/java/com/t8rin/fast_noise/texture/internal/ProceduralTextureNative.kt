package com.t8rin.fast_noise.texture.internal

import android.graphics.Bitmap

internal object ProceduralTextureNative {

    init {
        System.loadLibrary("fast-noise")
    }

    external fun generate(
        width: Int,
        height: Int,
        textureType: Int,
        seed: Int,
        colors: IntArray,
        parameters: FloatArray
    ): Bitmap?
}

internal object TextureType {
    const val LAVA = 0
    const val CLOUDS = 1
    const val SMOKE = 2
    const val STONE = 3
    const val WOOD = 4
    const val CAMOUFLAGE = 5
    const val PAPER = 6
    const val RUST = 7
    const val FABRIC = 8
    const val TOPOGRAPHY = 9
    const val CELLS = 10
    const val CRACKS = 11
    const val WATER_RIPPLES = 12
    const val FOLIAGE = 13
    const val BRICKS = 14
    const val TERRAIN = 15
    const val ICE = 16
    const val SAND = 17
    const val NEBULA = 18
    const val HONEYCOMB = 19
    const val GRASS = 20
    const val DIRT = 21
    const val LEATHER = 22
    const val CONCRETE = 23
    const val ASPHALT = 24
    const val MOSS = 25
    const val FIRE = 26
    const val AURORA = 27
    const val OIL_SLICK = 28
    const val WATERCOLOR = 29
    const val FLOW = 30
    const val OPAL = 31
    const val DAMASCUS = 32
    const val LIGHTNING = 33
    const val VELVET = 34
    const val INK_MARBLING = 35
    const val HOLOGRAPHIC = 36
    const val BIOLUMINESCENCE = 37
    const val COSMIC_VORTEX = 38
    const val LAVA_LAMP = 39
    const val EVENT_HORIZON = 40
    const val FRACTAL_BLOOM = 41
    const val CHROMATIC_TUNNEL = 42
    const val ECLIPSE_CORONA = 43
    const val STRANGE_ATTRACTOR = 44
    const val FERROFLUID_CROWN = 45
    const val SUPERNOVA = 46
    const val IRIS = 47
    const val PEACOCK_FEATHER = 48
    const val NAUTILUS_SHELL = 49
    const val RINGED_PLANET = 50
}
