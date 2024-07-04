package com.gemalto.jp2

import android.graphics.Bitmap
import android.util.Log
import androidx.annotation.IntDef
import java.io.IOException
import java.io.OutputStream
import java.util.Collections
import kotlin.math.min
import kotlin.math.sign

/**
 * JPEG-2000 bitmap encoder. Output properties:
 *
 *  * file format: JP2 (standard JPEG-2000 file format) or J2K (JPEG-2000 code stream)
 *  * colors pace: RGB or RGBA (depending on the [hasAlpha()][Bitmap.hasAlpha] value of the input bitmap)
 *  * precision: 8 bits per channel
 *  * image quality: can be set by visual quality or compression ratio; or lossless
 *
 */

class JP2Encoder
/**
 * Creates a new instance of the JPEG-2000 encoder.
 * @param bmp the bitmap to encode
 */
constructor(private val bmp: Bitmap) {
    @IntDef(*[FORMAT_J2K, FORMAT_JP2])
    annotation class OutputFormat

    private var numResolutions = DEFAULT_NUM_RESOLUTIONS
    private var compressionRatios: FloatArray? = null
    private var qualityValues: FloatArray? = null
    private var outputFormat = FORMAT_JP2

    //maximum resolutions possible to create with the given image dimensions [ = floor(log2(min_image_dimension)) + 1]
    private val maxResolutions: Int

    init {
        maxResolutions = min(
            (log2RoundedDown(
                min(
                    bmp.width.toDouble(),
                    bmp.height.toDouble()
                ).toInt()
            ) + 1).toDouble(), MAX_RESOLUTIONS_GLOBAL.toDouble()
        ).toInt()
        if (numResolutions > maxResolutions) numResolutions = maxResolutions
        if (BuildConfig.DEBUG) {
            Log.d(
                TAG,
                String.format(
                    "openjpeg encode: image size = %d x %d, maxResolutions = %d",
                    bmp.width,
                    bmp.height,
                    maxResolutions
                )
            )
        }
    }


    /**
     * Set the number of resolutions. It corresponds to the number of DWT decompositions +1.
     * Minimum number of resolutions is 1. Maximum is floor(log2(min_image_dimension)) + 1.<br></br><br></br>
     *
     * Some software might be able to take advantage of this and decode only smaller resolution
     * when appropriate. (This library is one such software. See [JP2Decoder.setSkipResolutions]).<br></br><br></br>
     *
     * Default value: 6 if the image dimensions are at least 32x32. Otherwise the maximum supported
     * number of resolutions.
     * @param numResolutions number of resolutions
     * @return this `JP2Encoder` instance
     */
    fun setNumResolutions(numResolutions: Int): JP2Encoder {
        require(!(numResolutions < MIN_RESOLUTIONS || numResolutions > maxResolutions)) { "Maximum number of resolutions for this image is between $MIN_RESOLUTIONS and $maxResolutions" }
        this.numResolutions = numResolutions
        return this
    }

    /**
     * Set compression ratio. The value is a factor of compression, thus 20 means 20 times compressed
     * (measured against the raw, uncompressed image size). 1 indicates lossless compression<br></br><br></br>
     *
     * This option produces a predictable image size, but the visual image quality will depend on how
     * "compressible" the original image is. If you want to get predictable visual quality (but
     * unpredictable size), use [.setVisualQuality].<br></br><br></br>
     *
     * You can set multiple compression ratios - this will produce an image with multiple quality layers.
     * Some software might be able to take advantage of this and decode only lesser quality layer
     * when appropriate. (This library is one such software. See [JP2Decoder.setLayersToDecode].)<br></br><br></br>
     *
     * Default value: a single lossless quality layer.<br></br><br></br>
     *
     * **Note: [.setCompressionRatio] and [.setVisualQuality]
     * cannot be used together.**
     * @param compressionRatios compression ratios
     * @return this `JP2Encoder` instance
     */
    fun setCompressionRatio(vararg compressionRatios: Float): JP2Encoder {
        if (compressionRatios.isEmpty()) {
            this.compressionRatios = null
            return this
        }

        //check for invalid values
        for (compressionRatio in compressionRatios) {
            require(!(compressionRatio < 1)) { "compression ratio must be at least 1" }
        }

        //check for conflicting settings
        require(qualityValues == null) { "setCompressionRatios and setQualityValues must not be used together!" }

        //store the values
        this.compressionRatios = sort(compressionRatios, false, 1f)!!
        return this
    }

    /**
     * Set image quality. The value is a [PSNR](https://en.wikipedia.org/wiki/Peak_signal-to-noise_ratio),
     * measured in dB. Higher PSNR means higher quality. A special value 0 indicates lossless quality.<br></br><br></br>
     *
     * As for reasonable values: 20 is extremely aggressive compression, 60-70 is close to lossless.
     * For "normal" compression you might want to aim at 30-50, depending on your needs.<br></br><br></br>
     *
     * This option produces predictable visual image quality, but the file size will depend on how
     * "compressible" the original image is. If you want to get predictable size (but
     * unpredictable visual quality), use [.setCompressionRatio].<br></br><br></br>
     *
     * You can set multiple quality values - this will produce an image with multiple quality layers.
     * Some software might be able to take advantage of this and decode only lesser quality layer
     * when appropriate. (This library is one such software. See [JP2Decoder.setLayersToDecode].)<br></br><br></br>
     *
     * Default value: a single lossless quality layer.<br></br><br></br>
     *
     * **Note: [.setVisualQuality] and [.setCompressionRatio] cannot be used together.**
     * @param qualityValues quality layer PSNR values
     * @return this `JP2Encoder` instance
     */
    fun setVisualQuality(vararg qualityValues: Float): JP2Encoder {
        if (qualityValues.isEmpty()) {
            this.qualityValues = null
            return this
        }

        //check for invalid values
        for (qualityValue in qualityValues) {
            require(!(qualityValue < 0)) { "quality values must not be negative" }
        }

        //check for conflicting settings
        require(compressionRatios == null) { "setCompressionRatios and setQualityValues must not be used together!" }

        //sort the values and filter out duplicates
        //store the values
        this.qualityValues = sort(qualityValues, true, 0f)!!
        return this
    }

    /**
     * Sets the output file format. The default value is [.FORMAT_JP2].
     * @param outputFormat [.FORMAT_J2K] or [.FORMAT_JP2]
     * @return this `JP2Encoder` instance
     */
    fun setOutputFormat(@OutputFormat outputFormat: Int): JP2Encoder {
        require(!(outputFormat != FORMAT_J2K && outputFormat != FORMAT_JP2)) { "output format must be FORMAT_JP2 or FORMAT_J2K!" }
        this.outputFormat = outputFormat
        return this
    }

    /**
     * Encode to JPEG-2000, return the result as a byte array.
     * @return the JPEG-2000 encoded data
     */
    fun encode(): ByteArray {
        return encodeInternal(bmp)
    }

    /**
     * Encode to JPEG-2000, store the result into a file.
     * @param fileName the name of the output file
     * @return `true` if the image was successfully converted and stored; `false` otherwise
     */
    fun encode(fileName: String): Boolean {
        return encodeInternal(bmp, fileName)
    }

    /**
     * Encode to JPEG-2000, write the result into an [OutputStream].
     * @param out the stream into which the result will be written
     * @return the number of bytes written; 0 in case of a conversion error
     * @throws IOException if there's an error writing the result into the output stream
     */
    fun encode(out: OutputStream): Int {
        val data = encodeInternal(bmp)
        out.write(data)
        return data.size
    }

    private fun encodeInternal(bmp: Bitmap): ByteArray {
        val pixels = IntArray(bmp.width * bmp.height)
        bmp.getPixels(pixels, 0, bmp.width, 0, 0, bmp.width, bmp.height)
        /* debug */
        var start: Long = 0
        /* debug */
        if (BuildConfig.DEBUG) start = System.currentTimeMillis()
        val ret = encodeJP2ByteArray(
            pixels,
            bmp.hasAlpha(),
            bmp.width,
            bmp.height,
            outputFormat,
            numResolutions,
            compressionRatios,
            qualityValues
        )
        /* debug */
        if (BuildConfig.DEBUG) Log.d(
            TAG,
            "converting to JP2: " + (System.currentTimeMillis() - start) + " ms"
        )
        return ret
    }

    private fun encodeInternal(bmp: Bitmap, fileName: String): Boolean {
        val pixels = IntArray(bmp.width * bmp.height)
        bmp.getPixels(pixels, 0, bmp.width, 0, 0, bmp.width, bmp.height)
        /* debug */
        var start: Long = 0
        /* debug */
        if (BuildConfig.DEBUG) start = System.currentTimeMillis()
        val ret = encodeJP2File(
            fileName,
            pixels,
            bmp.hasAlpha(),
            bmp.width,
            bmp.height,
            outputFormat,
            numResolutions,
            compressionRatios,
            qualityValues
        )
        /* debug */
        if (BuildConfig.DEBUG) Log.d(
            TAG,
            "converting to JP2: " + (System.currentTimeMillis() - start) + " ms"
        )
        return ret == EXIT_SUCCESS
    }

    private fun sort(array: FloatArray?, ascending: Boolean, losslessValue: Float): FloatArray? {
        if (array == null || array.isEmpty()) return null
        val list: MutableList<Float> = ArrayList()
        for (value in array) {
            //filter out duplicates
            if (!list.contains(value)) list.add(value)
        }
        //sort the list
        Collections.sort(
            list,
            java.util.Comparator { o1, o2 -> //lossless value must always come last
                if (o1 == losslessValue && o2 != losslessValue) return@Comparator 1
                if (o2 == losslessValue && o1 != losslessValue) return@Comparator -1
                sign((if (ascending) o1 - o2 else o2 - o1).toDouble()).toInt()
            })
        //copy from list back to array
        val ret = FloatArray(list.size)
        for (i in ret.indices) ret[i] = list[i]
        return ret
    }

    private fun log2RoundedDown(n: Int): Int {
        //returns log2(n) rounded down to the nearest integer.
        //naive implementation, but should be fast enough for our purposes.
        //only test until MAX_RESOLUTIONS_GLOBAL, we don't care about higher results anyway
        for (i in 0 until MAX_RESOLUTIONS_GLOBAL) {
            if ((1 shl i) > n) return i - 1
        }
        return MAX_RESOLUTIONS_GLOBAL
    }

    private external fun encodeJP2File(
        filename: String,
        pixels: IntArray,
        hasAlpha: Boolean,
        width: Int,
        height: Int,
        fileFormat: Int,
        numResolutions: Int,
        compressionRatios: FloatArray?,
        qualityValues: FloatArray?
    ): Int

    private external fun encodeJP2ByteArray(
        pixels: IntArray,
        hasAlpha: Boolean,
        width: Int,
        height: Int,
        fileFormat: Int,
        numResolutions: Int,
        compressionRatios: FloatArray?,
        qualityValues: FloatArray?
    ): ByteArray

    companion object {
        private const val TAG = "JP2Encoder"

        private const val EXIT_SUCCESS = 0
        private const val EXIT_FAILURE = 1

        private const val DEFAULT_NUM_RESOLUTIONS = 6

        //TODO in case of update to a newer version of OpenJPEG, check if it still throws error in case of too high resolution number
        //minimum resolutions supported by OpenJPEG 2.3.0
        private const val MIN_RESOLUTIONS = 1

        //maximum resolutions supported by OpenJPEG 2.3.0
        private const val MAX_RESOLUTIONS_GLOBAL = 32

        /** JPEG 2000 codestream format  */
        const val FORMAT_J2K: Int = 0

        /** The standard JPEG-2000 file format  */
        const val FORMAT_JP2: Int = 1

        init {
            System.loadLibrary("openjpeg")
        }
    }
}
