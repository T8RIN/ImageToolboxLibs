package com.t8rin.collages.utils

import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.provider.MediaStore
import android.widget.Toast
import com.t8rin.collages.R
import java.io.IOException
import java.io.InputStream

internal object ImageDecoder {
    var SAMPLER_SIZE = 256

    fun decodeAsset(context: Context, filePath: String): Bitmap? {
        val am = context.assets
        try {
            val `is` = am.open(filePath)
            return decodeStreamToBitmap(`is`)
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return null
    }

    fun decodeResource(context: Context, resId: Int): Bitmap? {
        return decodeSampledBitmapFromResource(
            context.resources, resId,
            SAMPLER_SIZE, SAMPLER_SIZE
        )
    }

    fun decodeUriToBitmap(context: Context, uri: Uri?): Bitmap? {
        if (uri == null) {
            return null
        }
        try {
            val file = FileUtils.getPath(context, uri)
            //ParcelFileDescriptor parcelFileDescriptor = context.getContentResolver().openFileDescriptor(uri, "r");
            //FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = true
            BitmapFactory.decodeFile(file, options)
            //BitmapFactory.decodeFileDescriptor(fileDescriptor, new Rect(), options);
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
            options.inSampleSize = scale
            options.inJustDecodeBounds = false
            return BitmapFactory.decodeFile(file, options)
        } catch (ex: Exception) {
            ex.printStackTrace()
        } catch (e: OutOfMemoryError) {
            throw e
        }

        return null
    }

    fun decodeUriToDrawable(context: Context, uri: Uri): BitmapDrawable? {
        try {
            val file = FileUtils.getPath(context, uri)
            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = true
            BitmapFactory.decodeFile(file, options)
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
            options.inSampleSize = scale
            options.inJustDecodeBounds = false
            val bm = BitmapFactory.decodeFile(file, options)
            return BitmapDrawable(context.resources, bm)
        } catch (ex: Exception) {
            ex.printStackTrace()
        } catch (e: OutOfMemoryError) {
            e.printStackTrace()
            Toast.makeText(
                context,
                context.getString(R.string.waring_out_of_memory),
                Toast.LENGTH_SHORT
            ).show()
        }

        return null
    }

    @Throws(OutOfMemoryError::class)
    fun decodeBlobToBitmap(data: ByteArray?): Bitmap? {
        try {
            if (data != null) {
                val options = BitmapFactory.Options()
                options.inJustDecodeBounds = true
                BitmapFactory.decodeByteArray(data, 0, data.size, options)

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
                options.inSampleSize = scale
                options.inJustDecodeBounds = false
                return BitmapFactory.decodeByteArray(
                    data, 0, data.size,
                    options
                )
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        } catch (err: OutOfMemoryError) {
            err.printStackTrace()
            throw err
        }

        return null
    }

    @Throws(OutOfMemoryError::class)
    fun decodeBlobToDrawable(
        data: ByteArray, reqWidth: Int,
        reqHeight: Int, res: Resources
    ): Drawable? {
        try {
            // First decode with inJustDecodeBounds=true to check dimensions
            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = true
            BitmapFactory.decodeByteArray(data, 0, data.size, options)

            // Calculate inSampleSize
            options.inSampleSize = calculateInSampleSize(
                options, reqWidth,
                reqHeight
            )

            // Decode bitmap with inSampleSize set
            options.inJustDecodeBounds = false
            return BitmapDrawable(
                res, BitmapFactory.decodeByteArray(
                    data,
                    0, data.size, options
                )
            )
        } catch (ex: Exception) {
            ex.printStackTrace()
        } catch (err: OutOfMemoryError) {
            err.printStackTrace()
            throw err
        }

        return null
    }

    @Throws(OutOfMemoryError::class)
    fun decodeBlobToBitmap(
        data: ByteArray, reqWidth: Int,
        reqHeight: Int, res: Resources
    ): Bitmap? {
        try {
            // First decode with inJustDecodeBounds=true to check dimensions
            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = true
            BitmapFactory.decodeByteArray(data, 0, data.size, options)

            // Calculate inSampleSize
            options.inSampleSize = calculateInSampleSize(
                options, reqWidth,
                reqHeight
            )

            // Decode bitmap with inSampleSize set
            options.inJustDecodeBounds = false
            return BitmapFactory.decodeByteArray(data, 0, data.size, options)
        } catch (ex: Exception) {
            ex.printStackTrace()
        } catch (err: OutOfMemoryError) {
            err.printStackTrace()
            throw err
        }

        return null
    }

    @Throws(OutOfMemoryError::class)
    fun decodeBlobToDrawble(data: ByteArray, res: Resources): Drawable? {
        try {
            // decode image size
            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = true
            BitmapFactory.decodeByteArray(data, 0, data.size, options)

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
            val bm = BitmapFactory.decodeByteArray(
                data, 0, data.size,
                options
            )

            return BitmapDrawable(res, bm)
        } catch (ex: Exception) {
            ex.printStackTrace()
        } catch (err: OutOfMemoryError) {
            err.printStackTrace()
            throw err
        }

        return null
    }

    @Throws(OutOfMemoryError::class)
    fun decodeStreamToDrawble(`is`: InputStream, res: Resources): Drawable? {
        try {
            // decode image size
            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = true
            val outPadding = Rect()
            BitmapFactory.decodeStream(`is`, outPadding, options)

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

            return BitmapDrawable(
                res, BitmapFactory.decodeStream(
                    `is`,
                    outPadding, options
                )
            )
        } catch (ex: Exception) {
            ex.printStackTrace()
        } catch (err: OutOfMemoryError) {
            err.printStackTrace()
            throw err
        }

        return null
    }

    @Throws(OutOfMemoryError::class)
    fun decodeFileToBitmap(context: Context, pathName: Uri): Bitmap? {
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

    @Throws(OutOfMemoryError::class)
    fun decodeStreamToBitmap(`is`: InputStream): Bitmap? {
        try {
            // decode image size
            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = true
            val outPadding = Rect()
            BitmapFactory.decodeStream(`is`, outPadding, options)

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

            return BitmapFactory.decodeStream(`is`, outPadding, options)
        } catch (ex: Exception) {
            ex.printStackTrace()
        } catch (err: OutOfMemoryError) {
            err.printStackTrace()
            throw err
        }

        return null
    }

    @Throws(OutOfMemoryError::class)
    fun decodeStreamToBitmap(
        `is`: InputStream, reqWidth: Int,
        reqHeight: Int
    ): Bitmap? {
        try {
            // First decode with inJustDecodeBounds=true to check dimensions
            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = true
            val outPadding = Rect()
            BitmapFactory.decodeStream(`is`, outPadding, options)
            // Calculate inSampleSize
            options.inSampleSize = calculateInSampleSize(
                options, reqWidth,
                reqHeight
            )

            // Decode bitmap with inSampleSize set
            options.inJustDecodeBounds = false
            return BitmapFactory.decodeStream(`is`, outPadding, options)
        } catch (ex: Exception) {
            ex.printStackTrace()
        } catch (err: OutOfMemoryError) {
            err.printStackTrace()
            throw err
        }

        return null
    }

    @Throws(OutOfMemoryError::class)
    fun decodeStreamToDrawable(
        `is`: InputStream, reqWidth: Int,
        reqHeight: Int, res: Resources
    ): Drawable? {
        try {
            // First decode with inJustDecodeBounds=true to check dimensions
            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = true
            val outPadding = Rect()
            BitmapFactory.decodeStream(`is`, outPadding, options)
            // Calculate inSampleSize
            options.inSampleSize = calculateInSampleSize(
                options, reqWidth,
                reqHeight
            )

            // Decode bitmap with inSampleSize set
            options.inJustDecodeBounds = false
            return BitmapDrawable(
                res, BitmapFactory.decodeStream(
                    `is`,
                    outPadding, options
                )
            )
        } catch (ex: Exception) {
            ex.printStackTrace()
        } catch (err: OutOfMemoryError) {
            err.printStackTrace()
            throw err
        }

        return null
    }

    @Throws(OutOfMemoryError::class)
    fun decodeSampledBitmapFromResource(
        res: Resources,
        resId: Int, reqWidth: Int, reqHeight: Int
    ): Bitmap? {
        try {
            // First decode with inJustDecodeBounds=true to check dimensions
            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = true
            BitmapFactory.decodeResource(res, resId, options)

            // Calculate inSampleSize
            options.inSampleSize = calculateInSampleSize(
                options, reqWidth,
                reqHeight
            )

            // Decode bitmap with inSampleSize set
            options.inJustDecodeBounds = false
            return BitmapFactory.decodeResource(res, resId, options)
        } catch (ex: Exception) {
            ex.printStackTrace()
        } catch (err: OutOfMemoryError) {
            err.printStackTrace()
            throw err
        }

        return null
    }

    @Throws(OutOfMemoryError::class)
    fun decodeSampledDrawableFromResource(
        res: Resources,
        resId: Int, reqWidth: Int, reqHeight: Int
    ): Drawable? {
        try {
            // First decode with inJustDecodeBounds=true to check dimensions
            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = true
            BitmapFactory.decodeResource(res, resId, options)

            // Calculate inSampleSize
            options.inSampleSize = calculateInSampleSize(
                options, reqWidth,
                reqHeight
            )

            // Decode bitmap with inSampleSize set
            options.inJustDecodeBounds = false
            return BitmapDrawable(
                res, BitmapFactory.decodeResource(
                    res,
                    resId, options
                )
            )
        } catch (ex: Exception) {
            ex.printStackTrace()
        } catch (e: OutOfMemoryError) {
            e.printStackTrace()
            throw e
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

    fun getRealPathFromURI(activity: Context, contentURI: Uri): String? {
        val cursor = activity.contentResolver.query(contentURI, null, null, null, null)
        // Source is Dropbox or other similar local file path
        if (cursor == null)
            return contentURI.path
        else {
            cursor.moveToFirst()
            val idx = cursor
                .getColumnIndex(MediaStore.Images.ImageColumns.DATA)
            return cursor.getString(idx)
        }
    }
}
