package com.t8rin.fast_noise.texture

import android.graphics.Bitmap

interface ProceduralTextureParameters {
    val seed: Int
}

fun interface ProceduralTextureGenerator<P : ProceduralTextureParameters> {
    fun generate(
        width: Int,
        height: Int,
        parameters: P
    ): Bitmap?
}
