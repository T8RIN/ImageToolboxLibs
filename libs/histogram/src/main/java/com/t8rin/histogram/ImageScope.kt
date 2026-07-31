package com.t8rin.histogram

import android.graphics.Bitmap
import com.t8rin.histogram.generator.HistogramGenerator
import kotlinx.coroutines.coroutineScope
import java.nio.ByteBuffer
import java.nio.ByteOrder

enum class ImageScopeType {
    Waveform,
    Vectorscope
}

class ImageScope private constructor(
    val type: ImageScopeType,
    val size: Int,
    val data: ByteBuffer
) {

    companion object {
        const val ChannelCount = 4

        suspend fun from(
            bitmap: Bitmap,
            type: ImageScopeType,
            size: Int = 256
        ): ImageScope = coroutineScope {
            val safeSize = size.coerceIn(64, 512)
            val data = ByteBuffer
                .allocateDirect(safeSize * safeSize * ChannelCount)
                .order(ByteOrder.nativeOrder())
            check(
                HistogramGenerator.generateScope(
                    bitmap = bitmap,
                    type = type.ordinal,
                    size = safeSize,
                    outputBuffer = data
                )
            ) {
                "Unable to generate image scope"
            }
            ImageScope(
                type = type,
                size = safeSize,
                data = data
            )
        }
    }
}
