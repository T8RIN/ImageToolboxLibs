package com.photoeditor.photoeffect.utils

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Bitmap.Config
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PointF
import android.graphics.PorterDuff.Mode
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.media.ExifInterface
import android.provider.MediaStore
import java.io.File
import java.io.IOException

object PhotoUtils {
    val FLIP_VERTICAL = 1
    val FLIP_HORIZONTAL = 2
    val DRAWABLE_PREFIX = "drawable://"
    val ASSET_PREFIX = "file:///android_asset/"

    fun addImageToGallery(filePath: String, context: Context) {
        val values = ContentValues()

        values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis())
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/png")
        values.put(MediaStore.MediaColumns.DATA, filePath)

        context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
    }

    fun fillBackgroundColorToImage(bitmap: Bitmap, color: Int): Bitmap {
        val result = Bitmap.createBitmap(bitmap.width, bitmap.height, bitmap.config)
        val canvas = Canvas(result)
        canvas.drawColor(color)
        canvas.drawBitmap(bitmap, 0f, 0f, Paint())
        return result
    }

    @JvmOverloads
    fun rotateImage(src: Bitmap?, degs: Float, flip: Boolean = false): Bitmap? {
        if (degs == 0f) return src
        var bm: Bitmap? = null
        if (src != null) {
            val matrix = Matrix()
            matrix.postRotate(degs)
            if (flip) {
                matrix.postScale(1f, -1f)
            }
            bm = Bitmap.createBitmap(src, 0, 0, src.width, src.height, matrix, true)
        }
        return bm
    }

    fun getCameraPhotoOrientation(context: Context, imagePath: String): Int {
        var rotate = 90
        try {
            val imageFile = File(imagePath)
            val exif = ExifInterface(imageFile.absolutePath)
            val orientation = exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_270 -> rotate = 270
                ExifInterface.ORIENTATION_ROTATE_180 -> rotate = 180
                ExifInterface.ORIENTATION_ROTATE_90 -> rotate = 90
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return rotate
    }

    fun decodePNGImage(context: Context, uri: String): Bitmap? {
        if (uri.startsWith(DRAWABLE_PREFIX)) {
            try {
                val resId = Integer.parseInt(uri.substring(DRAWABLE_PREFIX.length))
                return BitmapFactory.decodeResource(context.resources, resId)
            } catch (ex: Exception) {
                ex.printStackTrace()
            }

        } else if (uri.startsWith(ASSET_PREFIX)) {
            val path = uri.substring(ASSET_PREFIX.length)
            try {
                val `is` = context.assets.open(path)
                return BitmapFactory.decodeStream(`is`)
            } catch (e: IOException) {
                e.printStackTrace()
            }

        } else {
            return BitmapFactory.decodeFile(uri)
        }

        return null
    }

    /**
     * @param src
     * @param type is FLIP_VERTICAL or FLIP_HORIZONTAL
     * @return flipped image
     */
    fun flip(src: Bitmap, type: Int): Bitmap? {
        // create new matrix for transformation
        val matrix = Matrix()
        // if vertical
        if (type == FLIP_VERTICAL) {
            // y = y * -1
            matrix.preScale(1.0f, -1.0f)
        } else if (type == FLIP_HORIZONTAL) {
            // x = x * -1
            matrix.preScale(-1.0f, 1.0f)
            // unknown type
        } else {
            return null
        }// if horizonal

        // return transformed image
        return Bitmap.createBitmap(src, 0, 0, src.width, src.height, matrix, true)
    }

    fun blurImage(bitmap: Bitmap, radius: Float): Bitmap? {
        //        StackBlurManager stackBlurManager = new StackBlurManager(bitmap);
        //        return stackBlurManager.processNatively((int) radius);

        return null
    }

    /**
     * @param sentBitmap
     * @param radius
     * @return blurred image
     */
    @Deprecated(" ")
    fun fastblur(sentBitmap: Bitmap, radius: Int): Bitmap? {
        val bitmap = sentBitmap.copy(sentBitmap.config, true)

        if (radius < 1) {
            return null
        }

        val w = bitmap.width
        val h = bitmap.height

        val pix = IntArray(w * h)
        if (bitmap.isRecycled)
            return null
        bitmap.getPixels(pix, 0, w, 0, 0, w, h)

        val wm = w - 1
        val hm = h - 1
        val wh = w * h
        val div = radius + radius + 1

        val r = IntArray(wh)
        val g = IntArray(wh)
        val b = IntArray(wh)
        var rsum: Int
        var gsum: Int
        var bsum: Int
        var x: Int
        var y: Int
        var i: Int
        var p: Int
        var yp: Int
        var yi: Int
        var yw: Int
        val vmin = IntArray(Math.max(w, h))

        var divsum = div + 1 shr 1
        divsum *= divsum
        val dv = IntArray(256 * divsum)
        i = 0
        while (i < 256 * divsum) {
            dv[i] = i / divsum
            i++
        }

        yi = 0
        yw = yi

        val stack = Array(div) { IntArray(3) }
        var stackpointer: Int
        var stackstart: Int
        var sir: IntArray
        var rbs: Int
        val r1 = radius + 1
        var routsum: Int
        var goutsum: Int
        var boutsum: Int
        var rinsum: Int
        var ginsum: Int
        var binsum: Int

        y = 0
        while (y < h) {
            bsum = 0
            gsum = bsum
            rsum = gsum
            boutsum = rsum
            goutsum = boutsum
            routsum = goutsum
            binsum = routsum
            ginsum = binsum
            rinsum = ginsum
            i = -radius
            while (i <= radius) {
                p = pix[yi + Math.min(wm, Math.max(i, 0))]
                sir = stack[i + radius]
                sir[0] = p and 0xff0000 shr 16
                sir[1] = p and 0x00ff00 shr 8
                sir[2] = p and 0x0000ff
                rbs = r1 - Math.abs(i)
                rsum += sir[0] * rbs
                gsum += sir[1] * rbs
                bsum += sir[2] * rbs
                if (i > 0) {
                    rinsum += sir[0]
                    ginsum += sir[1]
                    binsum += sir[2]
                } else {
                    routsum += sir[0]
                    goutsum += sir[1]
                    boutsum += sir[2]
                }
                i++
            }
            stackpointer = radius

            x = 0
            while (x < w) {

                r[yi] = dv[rsum]
                g[yi] = dv[gsum]
                b[yi] = dv[bsum]

                rsum -= routsum
                gsum -= goutsum
                bsum -= boutsum

                stackstart = stackpointer - radius + div
                sir = stack[stackstart % div]

                routsum -= sir[0]
                goutsum -= sir[1]
                boutsum -= sir[2]

                if (y == 0) {
                    vmin[x] = Math.min(x + radius + 1, wm)
                }
                p = pix[yw + vmin[x]]

                sir[0] = p and 0xff0000 shr 16
                sir[1] = p and 0x00ff00 shr 8
                sir[2] = p and 0x0000ff

                rinsum += sir[0]
                ginsum += sir[1]
                binsum += sir[2]

                rsum += rinsum
                gsum += ginsum
                bsum += binsum

                stackpointer = (stackpointer + 1) % div
                sir = stack[stackpointer % div]

                routsum += sir[0]
                goutsum += sir[1]
                boutsum += sir[2]

                rinsum -= sir[0]
                ginsum -= sir[1]
                binsum -= sir[2]

                yi++
                x++
            }
            yw += w
            y++
        }
        x = 0
        while (x < w) {
            bsum = 0
            gsum = bsum
            rsum = gsum
            boutsum = rsum
            goutsum = boutsum
            routsum = goutsum
            binsum = routsum
            ginsum = binsum
            rinsum = ginsum
            yp = -radius * w
            i = -radius
            while (i <= radius) {
                yi = Math.max(0, yp) + x

                sir = stack[i + radius]

                sir[0] = r[yi]
                sir[1] = g[yi]
                sir[2] = b[yi]

                rbs = r1 - Math.abs(i)

                rsum += r[yi] * rbs
                gsum += g[yi] * rbs
                bsum += b[yi] * rbs

                if (i > 0) {
                    rinsum += sir[0]
                    ginsum += sir[1]
                    binsum += sir[2]
                } else {
                    routsum += sir[0]
                    goutsum += sir[1]
                    boutsum += sir[2]
                }

                if (i < hm) {
                    yp += w
                }
                i++
            }
            yi = x
            stackpointer = radius
            y = 0
            while (y < h) {
                // Preserve alpha channel: ( 0xff000000 & pix[yi] )
                pix[yi] =
                    -0x1000000 and pix[yi] or (dv[rsum] shl 16) or (dv[gsum] shl 8) or dv[bsum]

                rsum -= routsum
                gsum -= goutsum
                bsum -= boutsum

                stackstart = stackpointer - radius + div
                sir = stack[stackstart % div]

                routsum -= sir[0]
                goutsum -= sir[1]
                boutsum -= sir[2]

                if (x == 0) {
                    vmin[y] = Math.min(y + r1, hm) * w
                }
                p = x + vmin[y]

                sir[0] = r[p]
                sir[1] = g[p]
                sir[2] = b[p]

                rinsum += sir[0]
                ginsum += sir[1]
                binsum += sir[2]

                rsum += rinsum
                gsum += ginsum
                bsum += binsum

                stackpointer = (stackpointer + 1) % div
                sir = stack[stackpointer]

                routsum += sir[0]
                goutsum += sir[1]
                boutsum += sir[2]

                rinsum -= sir[0]
                ginsum -= sir[1]
                binsum -= sir[2]

                yi += w
                y++
            }
            x++
        }

        bitmap.setPixels(pix, 0, w, 0, 0, w, h)

        return bitmap
    }

    fun calculateScaleRatio(
        imageWidth: Int,
        imageHeight: Int,
        viewWidth: Int,
        viewHeight: Int
    ): Float {
        val ratioWidth = imageWidth.toFloat() / viewWidth
        val ratioHeight = imageHeight.toFloat() / viewHeight
        return Math.max(ratioWidth, ratioHeight)
    }

    fun calculateThumbnailSize(
        imageWidth: Int,
        imageHeight: Int,
        viewWidth: Int,
        viewHeight: Int
    ): IntArray {
        val size = IntArray(2)
        val ratioWidth = imageWidth.toFloat() / viewWidth
        val ratioHeight = imageHeight.toFloat() / viewHeight
        val ratio = Math.max(ratioWidth, ratioHeight)
        if (ratio == ratioWidth) {
            size[0] = viewWidth
            size[1] = (imageHeight / ratio).toInt()
        } else {
            size[0] = (imageWidth / ratio).toInt()
            size[1] = viewHeight
        }

        return size
    }

    /**
     * Remove all transparent color borders
     *
     * @param bitmap
     * @return
     */
    fun cleanImage(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        var top = 0
        var left = 0
        var right = width
        var bottom = height
        for (idx in 0 until width)
            if (!isTransparentColumn(bitmap, idx)) {
                left = idx
                break
            }
        for (idx in width - 1 downTo left + 1)
            if (!isTransparentColumn(bitmap, idx)) {
                right = idx
                break
            }
        for (idx in 0 until height)
            if (!isTransparentRow(bitmap, idx)) {
                top = idx
                break
            }
        for (idx in height - 1 downTo top + 1)
            if (!isTransparentRow(bitmap, idx)) {
                bottom = idx
                break
            }
        return Bitmap.createBitmap(bitmap, left, top, right - left + 1, bottom - top + 1)
    }

    private fun isTransparentRow(bitmap: Bitmap, row: Int): Boolean {
        for (idx in 0 until bitmap.width)
            if (bitmap.getPixel(idx, row) != Color.TRANSPARENT) {
                return false
            }
        return true
    }

    private fun isTransparentColumn(bitmap: Bitmap, column: Int): Boolean {
        for (idx in 0 until bitmap.height)
            if (bitmap.getPixel(column, idx) != Color.TRANSPARENT) {
                return false
            }
        return true
    }

    @Throws(OutOfMemoryError::class)
    fun transparentPadding(image: Bitmap, ratioWidthPerHeight: Float): Bitmap {
        try {
            val width = image.width
            val height = image.height
            var x = 0
            var y = 0
            // desfault
            var destWidth = width
            var destHeight = (width / ratioWidthPerHeight).toInt()
            y = (destHeight - height) / 2
            // else
            if (y < 0) {
                destHeight = height
                destWidth = (height * ratioWidthPerHeight).toInt()
                x = Math.max((destWidth - width) / 2, 0)
                y = 0
            }

            val result = Bitmap.createBitmap(destWidth, destHeight, Config.ARGB_8888)
            val canvas = Canvas(result)
            canvas.drawARGB(0x00, 0x00, 0x00, 0x00)
            canvas.drawBitmap(image, x.toFloat(), y.toFloat(), Paint())
            return result
        } catch (err: OutOfMemoryError) {
            throw err
        }

    }

    fun createBitmapMask(
        pointList: ArrayList<PointF>, imageWidth: Int,
        imageHeight: Int
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(imageWidth, imageHeight, Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawARGB(0x00, 0x00, 0x00, 0x00)

        val paint = Paint()
        paint.isAntiAlias = true
        paint.style = Paint.Style.FILL_AND_STROKE
        paint.color = Color.BLACK

        val path = Path()
        for (i in pointList.indices) {
            if (i == 0) {
                path.moveTo(pointList[i].x, pointList[i].y)
            } else {
                path.lineTo(pointList[i].x, pointList[i].y)
            }
        }

        canvas.drawPath(path, paint)
        canvas.clipPath(path)

        return bitmap

    }

    fun cropImage(mainImage: Bitmap, mask: Bitmap, m: Matrix): Bitmap {
        val canvas = Canvas()
        val result = Bitmap.createBitmap(mainImage.width, mainImage.height, Config.ARGB_8888)

        canvas.setBitmap(result)
        val paint = Paint()
        paint.isFilterBitmap = true
        paint.isAntiAlias = true

        canvas.drawBitmap(mainImage, m, paint)
        paint.xfermode = PorterDuffXfermode(Mode.DST_IN)
        canvas.drawBitmap(mask, 0f, 0f, paint)
        paint.xfermode = null

        return result
    }

    fun cropImage(mainImage: Bitmap, mask: Bitmap): Bitmap {
        val canvas = Canvas()
        val result = Bitmap.createBitmap(mainImage.width, mainImage.height, Config.ARGB_8888)

        canvas.setBitmap(result)
        val paint = Paint()
        paint.isFilterBitmap = true
        paint.isAntiAlias = true

        canvas.drawBitmap(mainImage, 0f, 0f, paint)
        paint.xfermode = PorterDuffXfermode(Mode.DST_IN)
        canvas.drawBitmap(mask, 0f, 0f, paint)
        paint.xfermode = null

        return result
    }

    fun getCircularBitmap(bitmap: Bitmap): Bitmap {
        val output: Bitmap

        if (bitmap.width > bitmap.height) {
            output = Bitmap.createBitmap(bitmap.height, bitmap.height, Config.ARGB_8888)
        } else {
            output = Bitmap.createBitmap(bitmap.width, bitmap.width, Config.ARGB_8888)
        }

        val canvas = Canvas(output)

        val paint = Paint()
        val rect = Rect(0, 0, bitmap.width, bitmap.height)

        var r = 0f

        if (bitmap.width > bitmap.height) {
            r = (bitmap.height / 2).toFloat()
        } else {
            r = (bitmap.width / 2).toFloat()
        }

        paint.isAntiAlias = true
        canvas.drawARGB(0, 0, 0, 0)
        paint.color = Color.BLACK
        canvas.drawCircle(r, r, r, paint)
        paint.xfermode = PorterDuffXfermode(Mode.SRC_IN)
        canvas.drawBitmap(bitmap, rect, rect, paint)
        return output
    }
}
