package com.t8rin.collages.utils

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.core.graphics.drawable.toBitmap
import coil.ImageLoader
import coil.imageLoader
import coil.memory.MemoryCache
import coil.request.CachePolicy
import coil.request.ImageRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal object ImageDecoder {
    var SAMPLER_SIZE = 512

    private var imageLoader: ImageLoader? = null

    suspend fun decodeFileToBitmap(
        context: Context,
        pathName: Uri
    ): Bitmap? = withContext(Dispatchers.IO) {
        val loader = imageLoader ?: context.imageLoader
            .newBuilder()
            .diskCachePolicy(CachePolicy.ENABLED)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .build()

        imageLoader = loader

        val stringKey = pathName.toString() + SAMPLER_SIZE + "ImageDecoder"
        val key = MemoryCache.Key(stringKey)

        loader.memoryCache?.get(key)?.bitmap ?: loader.execute(
            ImageRequest.Builder(context)
                .allowHardware(false)
                .diskCacheKey(stringKey)
                .memoryCacheKey(key)
                .data(pathName)
                .size(SAMPLER_SIZE)
                .build()
        ).drawable?.toBitmap()?.apply {
            if (config != Bitmap.Config.ARGB_8888) {
                config = Bitmap.Config.ARGB_8888
            }
        }
    }

}
