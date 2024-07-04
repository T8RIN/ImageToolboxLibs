package com.gemalto.jp2

import android.graphics.Bitmap
import android.util.Log
import com.gemalto.jp2.Utils.J2K_CODESTREAM_MAGIC
import com.gemalto.jp2.Utils.JP2_MAGIC
import com.gemalto.jp2.Utils.JP2_RFC3745_MAGIC
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream

/**
 * JPEG-2000 bitmap decoder. The supported data formats are: JP2 (standard JPEG-2000 file format) and J2K
 * (JPEG-2000 codestream). Only RGB(A) and grayscale(A) colorspaces are supported.
 */
class JP2Decoder {
    class Header {
        var width: Int = 0
        var height: Int = 0
        var hasAlpha: Boolean = false
        var numResolutions: Int = 0
        var numQualityLayers: Int = 0
    }

    private var data: ByteArray? = null
    private var fileName: String? = null
    private var `is`: InputStream? = null
    private var skipResolutions = 0
    private var layersToDecode = 0
    private var premultiplication = true

    /**
     * Decode a JPEG-2000 image from a byte array.
     *
     * @param data the JPEG-2000 image
     */
    constructor(data: ByteArray?) {
        this.data = data
    }

    /**
     * Decode a JPEG-2000 image file.
     *
     * @param fileName the name of the JPEG-2000 file
     */
    constructor(fileName: String?) {
        this.fileName = fileName
    }

    /**
     * Decode a JPEG-2000 image from a stream.
     *
     * @param is the stream containing the JPEG-2000 image<br></br>
     * **Note: the whole content of the stream will be read. The end of image data is not detected.**
     */
    constructor(`is`: InputStream?) {
        this.`is` = `is`
    }

    /**
     * Set the number of highest resolution levels to be discarded. The image resolution is effectively divided
     * by 2 to the power of the number of discarded levels. The reduce factor is limited by the number of stored
     * resolutions in the file. The number of existing resolutions can be detected using the [.readHeader]
     * method.<br></br><br></br>
     *
     *
     * At least one (the lowest) resolution is always decoded, no matter how high a number you set here.<br></br><br></br>
     *
     *
     * Default value: 0 (the image is decoded up to the highest resolution)
     *
     * @param skipResolutions the number of resolutions to skip
     * @return this instance of `JP2Decoder`
     */
    fun setSkipResolutions(skipResolutions: Int): JP2Decoder {
        require(skipResolutions >= 0) { "skipResolutions cannot be a negative number!" }
        this.skipResolutions = skipResolutions
        return this
    }

    /**
     * Set the maximum number of quality layers to decode. If there are less quality layers than the specified number,
     * all the quality layers are decoded. The available number of quality layers can be detected using the
     * [.readHeader] method. Special value 0 indicates all layers.<br></br><br></br>
     *
     *
     * Default value: 0 (all layers are decoded)
     *
     * @param layersToDecode number of quality layers to decode
     * @return this instance of `JP2Decoder`
     */
    fun setLayersToDecode(layersToDecode: Int): JP2Decoder {
        require(layersToDecode >= 0) { "layersToDecode cannot be a negative number!" }
        this.layersToDecode = layersToDecode
        return this
    }

    /**
     * This allows you to turn off alpha pre-multiplication in the output bitmap. Normally Android bitmaps with alpha
     * channel have their RGB component pre-multiplied by the normalized alpha channel. This improves performance when
     * displaying the bitmap, but it leads to loss of precision. This is no problem when you only want to display
     * the bitmap, but it can be a problem when you want to further process the bitmap's raw data.<br></br><br></br>
     *
     *
     * Since API 19 you can turn this pre-multiplication off. The loss of precision doesn't occur then, but the system
     * wont't be able to draw the output bitmap. On API &lt; 19 this setting is ignored.<br></br><br></br>
     *
     *
     * In most cases you should not use this.
     *
     * @return this instance of `JP2Decoder`
     * @see Bitmap.setPremultiplied
     * @see BitmapFactory.Options.inPremultiplied
     */
    fun disableBitmapPremultiplication(): JP2Decoder {
        this.premultiplication = false
        return this
    }

    /**
     * @return the decoded image; `null` in case of an error
     */
    fun decode(): Bitmap? {
        var res: IntArray? = null
        if (fileName != null) {
            res = decodeJP2File(fileName!!, skipResolutions, layersToDecode)
        } else {
            if (data == null && `is` != null) {
                data = readInputStream(`is`)
            }
            if (data == null) {
                Log.e(TAG, "Data is null, nothing to decode")
            } else {
                res = decodeJP2ByteArray(data!!, skipResolutions, layersToDecode)
            }
        }
        return nativeToBitmap(res)
    }

    /**
     * Decodes the file header information and returns it in a [Header] object.
     *
     * @return file header information
     */
    fun readHeader(): Header? {
        var res: IntArray? = null
        if (fileName != null) {
            res = readJP2HeaderFile(fileName!!)
        } else {
            if (data == null && `is` != null) {
                data = readInputStream(`is`)
            }
            if (data == null) {
                Log.e(TAG, "Data is null, nothing to decode")
            } else {
                res = readJP2HeaderByteArray(data!!)
            }
        }

        return nativeToHeader(res)
    }

    /*
        Get the decoded data from the native code and create a Bitmap object.
     */
    private fun nativeToBitmap(data: IntArray?): Bitmap? {
        if (data == null || data.size < 3) return null
        val width = data[0]
        val height = data[1]
        val alpha = data[2]

        val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        if (!premultiplication) {
            bmp.isPremultiplied = false
        }
        bmp.setPixels(data, 3, width, 0, 0, width, height)
        bmp.setHasAlpha(alpha != 0)

        return bmp
    }

    private external fun decodeJP2File(filename: String, reduce: Int, layers: Int): IntArray?

    private external fun decodeJP2ByteArray(
        data: ByteArray,
        reduce: Int,
        layers: Int
    ): IntArray?

    private external fun readJP2HeaderFile(filename: String): IntArray?

    private external fun readJP2HeaderByteArray(data: ByteArray): IntArray?

    companion object {
        private const val TAG = "JP2Decoder"

        init {
            System.loadLibrary("openjpeg")
        }

        /**
         * Returns true if the byte array starts with values typical for a JPEG-2000 header.
         *
         * @param data the byte array to check
         * @return `true` if the beginning looks like a JPEG-2000 header; `false` otherwise
         */
        fun isJPEG2000(data: ByteArray?): Boolean {
            if (data == null) return false
            if (startsWith(data, JP2_RFC3745_MAGIC)) return true
            if (startsWith(data, JP2_MAGIC)) return true
            return startsWith(data, J2K_CODESTREAM_MAGIC)
        }

        private fun readInputStream(`in`: InputStream?): ByteArray? {
            //sanity check
            if (`in` == null) {
                Log.e(TAG, "input stream is null!")
                return null
            }

            var out: ByteArrayOutputStream? = null
            try {
                out = ByteArrayOutputStream(`in`.available())
                val buffer = ByteArray(16 * 1024)
                var bytesRead = `in`.read(buffer)
                while (bytesRead >= 0) {
                    out.write(buffer, 0, bytesRead)
                    bytesRead = `in`.read(buffer)
                }
                return out.toByteArray()
            } catch (e: IOException) {
                e.printStackTrace()
                return null
            }
        }

        /*
        Get the header data from the native code
     */
        private fun nativeToHeader(data: IntArray?): Header? {
            if (data == null || data.size < 5) return null
            val ret = Header()
            ret.width = data[0]
            ret.height = data[1]
            ret.hasAlpha = data[2] != 0
            ret.numResolutions = data[3]
            ret.numQualityLayers = data[4]
            return ret
        }

        //does array1 start with contents of array2?
        private fun startsWith(array1: ByteArray, array2: ByteArray): Boolean {
            if (array1.size < array2.size) return false
            for (i in array2.indices) {
                if (array1[i] != array2[i]) return false
            }
            return true
        }
    }
}
