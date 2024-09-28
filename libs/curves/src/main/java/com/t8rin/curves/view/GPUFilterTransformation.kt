package com.t8rin.curves.view

import android.content.Context
import android.graphics.Bitmap
import coil.size.Size
import coil.size.pxOrElse
import coil.transform.Transformation
import com.t8rin.curves.utils.aspectRatio
import jp.co.cyberagent.android.gpuimage.GPUImage
import jp.co.cyberagent.android.gpuimage.filter.GPUImageFilter
import kotlin.math.max

internal abstract class GPUFilterTransformation(
    private val context: Context,
) : Transformation {

    /**
     * Create the [GPUImageFilter] to apply to this [Transformation]
     */
    abstract fun createFilter(): GPUImageFilter

    override suspend fun transform(
        input: Bitmap,
        size: Size
    ): Bitmap {
        val gpuImage = GPUImage(context)
        gpuImage.setImage(
            flexibleResize(
                image = input,
                max = max(
                    size.height.pxOrElse { input.height },
                    size.width.pxOrElse { input.width }
                )
            )
        )
        gpuImage.setFilter(createFilter())
        return gpuImage.bitmapWithFilterApplied
    }
}

private fun flexibleResize(
    image: Bitmap,
    max: Int
): Bitmap {
    return runCatching {
        if (image.height >= image.width) {
            val aspectRatio = image.aspectRatio
            val targetWidth = (max * aspectRatio).toInt()
            Bitmap.createScaledBitmap(image, targetWidth, max, true)
        } else {
            val aspectRatio = 1f / image.aspectRatio
            val targetHeight = (max * aspectRatio).toInt()
            Bitmap.createScaledBitmap(image, max, targetHeight, true)
        }
    }.getOrNull() ?: image
}