package com.t8rin.collages.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.core.graphics.drawable.toBitmap
import coil.imageLoader
import coil.request.ImageRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal object ImageDecoder {
    var SAMPLER_SIZE = 256

    @Throws(OutOfMemoryError::class)
    suspend fun decodeFileToBitmap(context: Context, pathName: Uri): Bitmap? =
        withContext(Dispatchers.IO) {
            try {
                context.imageLoader.newBuilder().build().execute(
                    ImageRequest.Builder(context).allowHardware(false).data(pathName).size(1000)
                        .build()
                ).drawable?.toBitmap()
            } catch (ex: Exception) {
                ex.printStackTrace()
                null
            } catch (err: OutOfMemoryError) {
                err.printStackTrace()
                throw err
            }
        }

    @Throws(OutOfMemoryError::class)
    fun decodeFileToBitmapSimple(context: Context, pathName: Uri): Bitmap? {
        try {
            // decode image size
            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = true
            BitmapFactory.decodeStream(
                context.contentResolver.openInputStream(pathName),
                null,
                options
            )
            // Find the correct scale value. It should be the power of 2.
            var width_tmp = options.outWidth
            var height_tmp = options.outHeight
            var scale = 1
            val requiredSize = SAMPLER_SIZE
            while (true) {
                if (width_tmp / 2 <= requiredSize || height_tmp / 2 <= requiredSize)
                    break
                width_tmp /= 2
                height_tmp /= 2
                scale *= 2
            }

            // decode with inSampleSize
            options.inJustDecodeBounds = false
            options.inSampleSize = scale

            return BitmapFactory.decodeStream(
                context.contentResolver.openInputStream(pathName),
                null,
                options
            )
        } catch (ex: Exception) {
            ex.printStackTrace()
        } catch (err: OutOfMemoryError) {
            err.printStackTrace()
            throw err
        }

        return null
    }

    fun calculateInSampleSize(
        options: BitmapFactory.Options,
        reqWidth: Int, reqHeight: Int
    ): Int {
        // Raw height and width of image
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {

            // Calculate ratios of height and width to requested height and
            // width
            val heightRatio = Math.round(height.toFloat() / reqHeight.toFloat())
            val widthRatio = Math.round(width.toFloat() / reqWidth.toFloat())

            // Choose the smallest ratio as inSampleSize value, this will
            // guarantee
            // a final image with both dimensions larger than or equal to the
            // requested height and width.
            inSampleSize = if (heightRatio < widthRatio) heightRatio else widthRatio
        }

        return inSampleSize
    }

}
