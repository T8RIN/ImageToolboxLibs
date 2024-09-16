package com.photoeditor.photoeffect.utils

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Bitmap.Config
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.PorterDuff.Mode
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.widget.ImageView

import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.io.RandomAccessFile
import java.text.SimpleDateFormat
import java.util.Date
import java.util.regex.Pattern

object ImageUtils {

    val OUTPUT_COLLAGE_FOLDER =
        Environment.getExternalStorageDirectory().toString() + "/" + "PhotoCollage"
    private val MIN_OUTPUT_IMAGE_SIZE = 640.0f

    val usedMemorySize: Long
        get() {

            var freeSize = 0L
            var totalSize = 0L
            var usedSize = -1L
            try {
                val info = Runtime.getRuntime()
                freeSize = info.freeMemory()
                totalSize = info.totalMemory()
                usedSize = totalSize - freeSize
            } catch (e: Exception) {
                e.printStackTrace()
            }

            return usedSize

        }

    class MemoryInfo {
        var availMem: Long = 0
        var totalMem: Long = 0
    }

    fun getMemoryInfo(context: Context): MemoryInfo {
        val info = MemoryInfo()
        val actManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memInfo = ActivityManager.MemoryInfo()
        actManager.getMemoryInfo(memInfo)
        info.availMem = memInfo.availMem

        if (Build.VERSION.SDK_INT >= 16) {
            info.totalMem = memInfo.totalMem
        } else {
            try {
                val reader = RandomAccessFile("/proc/meminfo", "r")
                val load = reader.readLine()
                // Get the Number value from the string
                val p = Pattern.compile("(\\d+)")
                val m = p.matcher(load)
                var value: String? = ""
                while (m.find()) {
                    value = m.group(1)
                }
                reader.close()
                info.totalMem = java.lang.Double.parseDouble(value!!).toLong() * 1024
            } catch (ex: Exception) {
                ex.printStackTrace()
            }

        }
        return info
    }

    fun calculateOutputScaleFactor(viewWidth: Int, viewHeight: Int): Float {
        var ratio = Math.min(viewWidth, viewHeight) / MIN_OUTPUT_IMAGE_SIZE
        if (ratio < 1 && ratio > 0) {
            ratio = 1.0f / ratio
        } else {
            ratio = 1f
        }
        return ratio
    }

    fun saveAndShare(context: Context, image: Bitmap) {
        try {
            val fileName = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date()).replace(
                ":".toRegex(),
                "-"
            ) + ".png"
            val collageFolder = File(OUTPUT_COLLAGE_FOLDER)
            if (!collageFolder.exists()) {
                collageFolder.mkdirs()
            }
            val photoFile = File(collageFolder, fileName)
            image.compress(Bitmap.CompressFormat.PNG, 100, FileOutputStream(photoFile))
            PhotoUtils.addImageToGallery(photoFile.absolutePath, context)
            val share = Intent(Intent.ACTION_SEND)
            share.type = "image/png"
            share.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(File(photoFile.absolutePath)))
            context.startActivity(Intent.createChooser(share, "Share Image"))
        } catch (ex: Exception) {
            ex.printStackTrace()
        }

    }

    fun dpFromPx(context: Context, px: Float): Float {
        return px / context.resources.displayMetrics.density
    }

    fun pxFromDp(context: Context, dp: Float): Float {
        return dp * context.resources.displayMetrics.density
    }

    fun createMatrixToDrawImageInCenterView(
        viewWidth: Float,
        viewHeight: Float,
        imageWidth: Float,
        imageHeight: Float
    ): Matrix {
        val ratioWidth = viewWidth / imageWidth
        val ratioHeight = viewHeight / imageHeight
        val ratio = Math.max(ratioWidth, ratioHeight)
        val dx = (viewWidth - imageWidth) / 2.0f
        val dy = (viewHeight - imageHeight) / 2.0f
        val result = Matrix()
        result.postTranslate(dx, dy)
        result.postScale(ratio, ratio, viewWidth / 2, viewHeight / 2)
        return result
    }

    fun recycleView(iv: View?) {
        if (iv == null) {
            return
        }

        val background = iv.background
        iv.setBackgroundColor(Color.TRANSPARENT)

        if (background != null && background is BitmapDrawable) {
            var bm: Bitmap? = background.bitmap
            if (bm != null && !bm.isRecycled) {
                bm.recycle()
                bm = null
            }
        }
    }

    fun recycleImageView(iv: ImageView?) {
        if (iv == null) {
            return
        }

        val background = iv.background
        val d = iv.drawable
        iv.setBackgroundColor(Color.TRANSPARENT)
        iv.setImageBitmap(null)

        if (background != null && background is BitmapDrawable) {
            var bm: Bitmap? = background.bitmap
            if (bm != null && !bm.isRecycled) {
                bm.recycle()
                bm = null
            }
        }

        if (d != null && d is BitmapDrawable) {
            var bm: Bitmap? = d.bitmap
            if (bm != null && !bm.isRecycled) {
                bm.recycle()
                bm = null
            }
        }
    }

    fun getSizeInBytes(bitmap: Bitmap): Long {
        return (bitmap.rowBytes * bitmap.height).toLong()
    }

    /**
     * @param v
     * @return
     * @throws OutOfMemoryError
     */
    @Deprecated("")
    @Throws(OutOfMemoryError::class)
    fun loadBitmapFromView(v: View): Bitmap {
        try {
            val width = v.measuredWidth
            val height = v.measuredHeight
            val bg = v.background
            v.setBackgroundColor(Color.TRANSPARENT)
            v.layout(0, 0, width, height)
            val returnedBitmap = Bitmap.createBitmap(
                width, height,
                Config.ARGB_8888
            )
            val c = Canvas(returnedBitmap)
            bg?.draw(c)

            v.draw(c)
            if (Build.VERSION.SDK_INT >= 16) {
                v.background = bg
            } else {
                v.setBackgroundDrawable(bg)
            }

            return returnedBitmap
        } catch (err: OutOfMemoryError) {
            err.printStackTrace()
            throw err
        }

    }

    /**
     * @param view
     * @param outputImagePath
     * @throws OutOfMemoryError
     */
    @Deprecated("")
    @Throws(OutOfMemoryError::class)
    fun takeScreen(view: View, outputImagePath: String) {
        try {
            val bitmap = loadBitmapFromView(view)
            val imageFile = File(outputImagePath)
            imageFile.parentFile!!.mkdirs()
            var fout: OutputStream? = null

            fout = FileOutputStream(imageFile)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fout)
            fout.flush()
            fout.close()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (err: OutOfMemoryError) {
            err.printStackTrace()
            throw err
        }

    }

    fun saveBitmap(bitmap: Bitmap, path: String) {
        var fout: OutputStream? = null
        try {
            fout = FileOutputStream(path)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fout)
            fout.flush()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            try {
                fout!!.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }

        }
    }

    fun getImageOrientation(context: Context, imagePath: String): Int {
        var orientation = getOrientationFromExif(imagePath)
        if (orientation < 0) {
            val uri = Uri.fromFile(File(imagePath))
            orientation = getOrientationFromMediaStore(context, uri)
        }

        return orientation
    }

    // private static String getExifTag(ExifInterface exif,String tag){
    // String attribute = exif.getAttribute(tag);
    //
    // return (null != attribute ? attribute : "");
    // }
    private fun getOrientationFromExif(imagePath: String): Int {
        var orientation = -1
        try {
            val exif = ExifInterface(imagePath)
            val exifOrientation = exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )
            // StringBuilder builder = new StringBuilder();
            //
            // builder.append("Date & Time: " +
            // getExifTag(exif,ExifInterface.TAG_DATETIME) + "\n\n");
            // builder.append("Flash: " +
            // getExifTag(exif,ExifInterface.TAG_FLASH) + "\n");
            // builder.append("Focal Length: " +
            // getExifTag(exif,ExifInterface.TAG_FOCAL_LENGTH) + "\n\n");
            // builder.append("GPS Datestamp: " +
            // getExifTag(exif,ExifInterface.TAG_FLASH) + "\n");
            // builder.append("GPS Latitude: " +
            // getExifTag(exif,ExifInterface.TAG_GPS_LATITUDE) + "\n");
            // builder.append("GPS Latitude Ref: " +
            // getExifTag(exif,ExifInterface.TAG_GPS_LATITUDE_REF) + "\n");
            // builder.append("GPS Longitude: " +
            // getExifTag(exif,ExifInterface.TAG_GPS_LONGITUDE) + "\n");
            // builder.append("GPS Longitude Ref: " +
            // getExifTag(exif,ExifInterface.TAG_GPS_LONGITUDE_REF) + "\n");
            // builder.append("GPS Processing Method: " +
            // getExifTag(exif,ExifInterface.TAG_GPS_PROCESSING_METHOD) + "\n");
            // builder.append("GPS Timestamp: " +
            // getExifTag(exif,ExifInterface.TAG_GPS_TIMESTAMP) + "\n\n");
            // builder.append("Image Length: " +
            // getExifTag(exif,ExifInterface.TAG_IMAGE_LENGTH) + "\n");
            // builder.append("Image Width: " +
            // getExifTag(exif,ExifInterface.TAG_IMAGE_WIDTH) + "\n\n");
            // builder.append("Camera Make: " +
            // getExifTag(exif,ExifInterface.TAG_MAKE) + "\n");
            // builder.append("Camera Model: " +
            // getExifTag(exif,ExifInterface.TAG_MODEL) + "\n");
            // builder.append("Camera Orientation: " +
            // getExifTag(exif,ExifInterface.TAG_ORIENTATION) + "\n");
            // builder.append("Camera White Balance: " +
            // getExifTag(exif,ExifInterface.TAG_WHITE_BALANCE) + "\n");
            // builder.append("Camera orientation=" + getExifTag(exif,
            // ExifInterface.TAG_ORIENTATION));
            // ALog.d("ImageUtils.getOrientationFromExif", "exif=" +
            // builder.toString());
            when (exifOrientation) {
                ExifInterface.ORIENTATION_ROTATE_270 -> orientation = 270
                ExifInterface.ORIENTATION_ROTATE_180 -> orientation = 180
                ExifInterface.ORIENTATION_ROTATE_90 -> orientation = 90

                ExifInterface.ORIENTATION_NORMAL -> orientation = 0
                else -> {
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return orientation
    }

    private fun getOrientationFromMediaStore(
        context: Context,
        imageUri: Uri?
    )// (Context context, String imagePath)
            : Int {
        // Uri imageUri = getImageContentUri(context, imagePath);
        if (imageUri == null) {
            return -1
        }

        val projection = arrayOf(MediaStore.Images.ImageColumns.ORIENTATION)
        val cursor = context.contentResolver.query(
            imageUri,
            projection, null, null, null
        )

        var orientation = -1
        if (cursor != null && cursor.moveToFirst()) {
            orientation = cursor.getInt(0)
            cursor.close()
        }

        return orientation
    }

    //
    // private static Uri getImageContentUri(Context context, String imagePath)
    // {
    // String[] projection = new String[] {MediaStore.Images.Media._ID};
    // String selection = MediaStore.Images.Media.DATA + "=? ";
    // String[] selectionArgs = new String[] {imagePath};
    // Cursor cursor = context.getContentResolver().query(IMAGE_PROVIDER_URI,
    // projection,
    // selection, selectionArgs, null);
    //
    // if (cursor != null && cursor.moveToFirst()) {
    // int imageId = cursor.getInt(0);
    // cursor.close();
    //
    // return Uri.withAppendedPath(IMAGE_PROVIDER_URI,
    // Integer.toString(imageId));
    // }
    //
    // if (new File(imagePath).exists()) {
    // ContentValues values = new ContentValues();
    // values.put(MediaStore.Images.Media.DATA, imagePath);
    //
    // return context.getContentResolver().insert(
    // MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
    // }
    //
    // return null;
    // }

    fun fastblur(sentBitmap: Bitmap, radius: Int): Bitmap? {

        // Stack Blur v1.0 from
        // http://www.quasimondo.com/StackBlurForCanvas/StackBlurDemo.html
        //
        // Java Author: Mario Klingemann <mario at quasimondo.com>
        // http://incubator.quasimondo.com
        // created Feburary 29, 2004
        // Android port : Yahel Bouaziz <yahel at kayenko.com>
        // http://www.kayenko.com
        // ported april 5th, 2012
        //

        // This is a compromise between Gaussian Blur and Box blur
        // It creates much better looking blurs than Box Blur, but is
        // 7x faster than my Gaussian Blur implementation.
        //
        // I called it Stack Blur because this describes best how this
        // filter works internally: it creates a kind of moving stack
        // of colors whilst scanning through the image. Thereby it
        // just has to add one new block of color to the right side
        // of the stack and remove the leftmost color. The remaining
        // colors on the topmost layer of the stack are either added on
        // or reduced by one, depending on if they are on the right or
        // on the left side of the stack.
        //
        // If you are using this algorithm in your code please add
        // the following line:
        //
        // Stack Blur Algorithm by Mario Klingemann <mario@quasimondo.com>

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
                pix[yi] = (-0x1000000 and pix[yi] or (dv[rsum] shl 16)
                        or (dv[gsum] shl 8) or dv[bsum])

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

    fun getCircularArea(
        bitmap: Bitmap, circleX: Int,
        circleY: Int, radius: Int
    ): Bitmap {
        val output = Bitmap.createBitmap(
            2 * radius, 2 * radius,
            Config.ARGB_8888
        )

        val rect = Rect()
        rect.top = circleY - radius
        rect.bottom = rect.top + 2 * radius
        rect.left = circleX - radius
        rect.right = rect.left + 2 * radius

        val canvas = Canvas(output)
        val color = -0xbdbdbe
        val paint = Paint()
        paint.isAntiAlias = true
        paint.isFilterBitmap = true
        paint.isDither = true
        canvas.drawARGB(0, 0, 0, 0)
        paint.color = color
        canvas.drawCircle(
            (output.width / 2).toFloat(), (output.height / 2).toFloat(),
            radius.toFloat(), paint
        )
        paint.xfermode = PorterDuffXfermode(Mode.SRC_IN)
        canvas.drawBitmap(
            bitmap, rect, Rect(
                0, 0, output.width,
                output.height
            ), paint
        )

        return output
    }

    fun focus(
        src: Bitmap, filtratedBitmap: Bitmap, rect: Rect,
        isCircle: Boolean
    ): Bitmap {
        val paint = Paint()
        paint.isAntiAlias = true
        paint.isFilterBitmap = true
        paint.isDither = true

        var focusBitmap: Bitmap? = null
        var area: Bitmap? = Bitmap.createBitmap(
            rect.right - rect.left,
            rect.bottom - rect.top,
            Config.ARGB_8888
        )

        val canvas = Canvas(area!!)
        canvas.drawBitmap(
            src, rect,
            Rect(0, 0, area.width, area.height), paint
        )

        if (isCircle) {
            focusBitmap = getCircularBitmap(area)
            area.recycle()
            area = null
        } else {
            focusBitmap = area
            area = null
        }

        val result = Bitmap.createBitmap(
            filtratedBitmap.width,
            filtratedBitmap.height, Config.ARGB_8888
        )
        canvas.setBitmap(result)
        canvas.drawBitmap(
            focusBitmap, Rect(
                0, 0, focusBitmap.width,
                focusBitmap.height
            ), rect, paint
        )

        focusBitmap.recycle()
        focusBitmap = null

        return result
    }

    fun getCircularBitmap(bitmap: Bitmap): Bitmap {

        val output = Bitmap.createBitmap(
            bitmap.width,
            bitmap.height, Config.ARGB_8888
        )

        val canvas = Canvas(output)

        val color = -0xbdbdbe
        val paint = Paint()
        val rect = Rect(0, 0, bitmap.width, bitmap.height)

        var r = 0f

        if (bitmap.width > bitmap.height) {
            r = (bitmap.height / 2).toFloat()
        } else {
            r = (bitmap.width / 2).toFloat()
        }

        paint.isAntiAlias = true
        paint.isFilterBitmap = true
        paint.isDither = true
        canvas.drawARGB(0, 0, 0, 0)
        paint.color = color
        canvas.drawCircle(
            (bitmap.width / 2).toFloat(), (bitmap.height / 2).toFloat(), r,
            paint
        )
        paint.xfermode = PorterDuffXfermode(Mode.SRC_IN)
        canvas.drawBitmap(bitmap, rect, rect, paint)

        return output
    }

}
