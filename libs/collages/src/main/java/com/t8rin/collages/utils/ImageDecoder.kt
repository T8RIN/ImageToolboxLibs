package com.t8rin.collages.utils

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import coil3.ImageLoader
import coil3.imageLoader
import coil3.memory.MemoryCache
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import coil3.request.allowHardware
import coil3.toBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal object ImageDecoder {
    var SAMPLER_SIZE = 1024

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

        loader.memoryCache?.get(key)?.image?.toBitmap() ?: loader.execute(
            ImageRequest.Builder(context)
                .allowHardware(false)
                .diskCacheKey(stringKey)
                .memoryCacheKey(key)
                .data(pathName)
                .size(SAMPLER_SIZE)
                .build()
        ).image?.toBitmap()?.apply {
            if (config != Bitmap.Config.ARGB_8888) {
                setConfig(Bitmap.Config.ARGB_8888)
            }
        }
    }

}
