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
}
