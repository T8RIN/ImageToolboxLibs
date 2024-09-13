package com.t8rin.fast_noise

import android.graphics.Bitmap

data object FastNoise {

    init {
        System.loadLibrary("fast-noise")
    }

    external fun generateNoiseImage(
        width: Int,
        height: Int,
        seed: Int = 1337,
        frequency: Float = 0.01f,
        noiseType: Int = 0,
        rotationType3D: Int = 0,
        fractalType: Int = 0,
        fractalOctaves: Int = 3,
        fractalLacunarity: Float = 2f,
        fractalGain: Float = 0.5f,
        fractalWeightedStrength: Float = 0f,
        fractalPingPongStrength: Float = 2f,
        cellularDistanceFunction: Int = 1,
        cellularReturnType: Int = 1,
        cellularJitter: Float = 1f,
        domainWarpType: Int = 0,
        domainWarpAmp: Float = 1f
    ): Bitmap?

}