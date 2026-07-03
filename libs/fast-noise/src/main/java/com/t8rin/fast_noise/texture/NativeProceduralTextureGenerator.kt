package com.t8rin.fast_noise.texture

import android.graphics.Bitmap
import com.t8rin.fast_noise.texture.internal.ProceduralTextureNative

abstract class NativeProceduralTextureGenerator<P : ProceduralTextureParameters>(
    private val textureType: Int
) : ProceduralTextureGenerator<P> {

    final override fun generate(width: Int, height: Int, parameters: P): Bitmap? {
        require(width > 0 && height > 0) { "Texture dimensions must be positive" }
        require(width.toLong() * height <= Int.MAX_VALUE) { "Texture is too large" }

        return ProceduralTextureNative.generate(
            width = width,
            height = height,
            textureType = textureType,
            seed = parameters.seed,
            colors = colors(parameters),
            parameters = values(parameters)
        )
    }

    protected abstract fun colors(parameters: P): IntArray

    protected abstract fun values(parameters: P): FloatArray
}
