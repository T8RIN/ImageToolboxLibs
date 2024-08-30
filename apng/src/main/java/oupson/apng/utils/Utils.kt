package oupson.apng.utils

import android.graphics.Bitmap
import android.graphics.Color
import oupson.apng.exceptions.BadBitmapsDiffSize
import oupson.apng.utils.Utils.BlendOp.APNG_BLEND_OP_OVER
import oupson.apng.utils.Utils.BlendOp.APNG_BLEND_OP_SOURCE
import oupson.apng.utils.Utils.DisposeOp.APNG_DISPOSE_OP_BACKGROUND
import oupson.apng.utils.Utils.DisposeOp.APNG_DISPOSE_OP_NONE
import oupson.apng.utils.Utils.DisposeOp.APNG_DISPOSE_OP_PREVIOUS
import kotlin.experimental.and


object Utils {

    fun Bitmap.flexibleResize(
        max: Int?
    ): Bitmap {
        val image = this

        if (max == null) return image

        return runCatching {
            if (image.height >= image.width) {
                val aspectRatio = image.width.toDouble() / image.height.toDouble()
                val targetWidth = (max * aspectRatio).toInt()
                Bitmap.createScaledBitmap(image, targetWidth, max, true)
            } else {
                val aspectRatio = image.height.toDouble() / image.width.toDouble()
                val targetHeight = (max * aspectRatio).toInt()
                Bitmap.createScaledBitmap(image, max, targetHeight, true)
            }
        }.getOrNull() ?: image
    }

    /**
     * @param byteArray The PNG
     * @return [Boolean] True if is a png
     */
    fun isPng(byteArray: ByteArray): Boolean {
        return if (byteArray.size == 8)
            byteArray.contentEquals(pngSignature)
        else
            byteArray.copyOfRange(0, 8).contentEquals(pngSignature)
    }

    /**
     * Know if file is an APNG
     * @param byteArray APNG
     * @return True if is an APNG
     */
    fun isApng(byteArray: ByteArray): Boolean {
        if (!isPng(byteArray)) return false
        try {
            for (i in 8 until byteArray.size) {
                val it = byteArray.copyOfRange(i, i + 4)
                // if byteArray contain acTL
                if (it.contentEquals(acTL)) {
                    return true
                } else if (it.contentEquals(IDAT)) {
                    return false
                }
            }
            return false
        } catch (e: Exception) {
            return false
        }
    }

    /**
     * Signature for png / APNG files
     */
    val pngSignature: ByteArray by lazy {
        byteArrayOf(
            0x89.toByte(),
            0x50,
            0x4E,
            0x47,
            0x0D,
            0x0A,
            0x1A,
            0x0A
        )
    }

    /**
     * DisposeOp specifies how the output buffer should be changed at the end of the delay (before rendering the next frame).
     * Values :
     * - [APNG_DISPOSE_OP_NONE] : No disposal is done on this frame before rendering the next; the contents of the output buffer are left as is.
     * - [APNG_DISPOSE_OP_BACKGROUND] : The frame's region of the output buffer is to be cleared to fully transparent black before rendering the next frame.
     * - [APNG_DISPOSE_OP_PREVIOUS] : The frame's region of the output buffer is to be reverted to the previous contents before rendering the next frame.
     */
    enum class DisposeOp {
        APNG_DISPOSE_OP_NONE,
        APNG_DISPOSE_OP_BACKGROUND,
        APNG_DISPOSE_OP_PREVIOUS
    }

    /**
     * Get the int equivalent to the DisposeOp
     * @param disposeOp The DisposeOp
     * @return [Int] An int equivalent to the DisposeOp
     */
    fun encodeDisposeOp(disposeOp: DisposeOp): Int {
        return when (disposeOp) {
            APNG_DISPOSE_OP_NONE -> 0
            APNG_DISPOSE_OP_BACKGROUND -> 1
            APNG_DISPOSE_OP_PREVIOUS -> 2
        }
    }

    /**
     * Get the DisposeOp enum equivalent to the int
     * @param int Int of the DisposeOp
     * @return [DisposeOp] A DisposeOp
     */
    fun decodeDisposeOp(int: Int): DisposeOp {
        return when (int) {
            0 -> APNG_DISPOSE_OP_NONE
            1 -> APNG_DISPOSE_OP_BACKGROUND
            2 -> APNG_DISPOSE_OP_PREVIOUS
            else -> APNG_DISPOSE_OP_NONE
        }
    }

    /**
     * BlendOp specifies whether the frame is to be alpha blended into the current output buffer content, or whether it should completely replace its region in the output buffer.
     * Values :
     * - [APNG_BLEND_OP_SOURCE] : All color components of the frame, including alpha, overwrite the current contents of the frame's output buffer region.
     * - [APNG_BLEND_OP_OVER] : The frame should be composited onto the output buffer based on its alpha, using a simple OVER operation as described in the Alpha Channel Processing section of the Extensions to the PNG Specification, Version 1.2.0.
     */
    enum class BlendOp {
        APNG_BLEND_OP_SOURCE,
        APNG_BLEND_OP_OVER
    }

    /**
     * Get the int equivalent to the BlendOp
     * @param blendOp The BlendOp
     * @return [Byte] An int equivalent to the BlendOp
     */
    fun encodeBlendOp(blendOp: BlendOp): Int {
        return when (blendOp) {
            APNG_BLEND_OP_SOURCE -> 0
            APNG_BLEND_OP_OVER -> 1
        }
    }

    /**
     * Get the BlendOp enum equivalent to the int
     * @param int Int of the BlendOp
     * @return [BlendOp] A BlendOp
     */
    fun decodeBlendOp(int: Int): BlendOp {
        return when (int) {
            0 -> APNG_BLEND_OP_SOURCE
            1 -> APNG_BLEND_OP_OVER
            else -> APNG_BLEND_OP_SOURCE
        }
    }

    /**
     * Generate a 4 bytes array from an Int
     * @param i The int
     * @return [Array] 4 Bytes
     */
    fun uShortToArray(i: Int): Array<Byte> {
        return arrayOf((i shr 24).toByte(), (i shr 16).toByte(), (i shr 8).toByte(), i.toByte())
    }

    /**
     * Generate a 4 bytes array from an Int
     * @param i The int
     * @return [ByteArray] 4 Bytes
     */
    fun uIntToByteArray(i: Int): ByteArray {
        return byteArrayOf(
            (i shr 24).toByte(),
            (i shr 16).toByte(),
            (i shr 8).toByte(),
            i.toByte()
        )
    }

    /**
     * Generate a 2 bytes array from an Int
     * @param i The int
     * @return [ByteArray] 2 Bytes
     */
    fun uShortToByteArray(i: Int): ByteArray {
        return byteArrayOf((i shr 8).toByte(), i /*>> 0*/.toByte())
    }

    /**
     * Generate a 2 bytes array from a short
     * @param s The int
     * @return [ByteArray] 2 Bytes
     */
    fun uShortToByteArray(s: Short): ByteArray {
        return byteArrayOf((s.toInt() shr 8 and 0x00FF).toByte(), (s and 0xFF).toByte())
    }

    /**
     * Parse an unsigned int
     * [bytes] The bytes, in big endian order
     * [Int] The parsed int
     */
    fun uIntFromBytesBigEndian(bytes: List<Int>): Int =
        ((bytes[0] and 0xFF) shl 24) or
                ((bytes[1] and 0xFF) shl 16) or
                ((bytes[2] and 0xFF) shl 8) or
                (bytes[3] and 0xFF)

    // TODO DOCUMENT AND TEST
    fun uIntFromBytesBigEndian(bytes: ByteArray, offset: Int = 0): Int =
        ((bytes[offset + 0].toInt() and 0xFF) shl 24) or
                ((bytes[offset + 1].toInt() and 0xFF) shl 16) or
                ((bytes[offset + 2].toInt() and 0xFF) shl 8) or
                (bytes[offset + 3].toInt() and 0xFF)

    /**
     * Parse an unsigned short
     * [bytes] The bytes, in big endian order
     * [Int] The parsed short
     * Because UShort is still experiment, this return an int
     */
    fun uShortFromBytesBigEndian(bytes: List<Int>): Int =
        (((bytes[0] and 0xFF) shl 8) or
                (bytes[1] and 0xFF))

    // TODO DOCUMENT AND TEST
    fun uShortFromBytesBigEndian(bytes: ByteArray, offset: Int = 0): Int =
        (((bytes[offset].toInt() and 0xFF) shl 8) or
                (bytes[offset + 1].toInt() and 0xFF))

    val fcTL: ByteArray by lazy { byteArrayOf(0x66, 0x63, 0x54, 0x4c) }
    val IEND: ByteArray by lazy { byteArrayOf(0x49, 0x45, 0x4e, 0x44) }
    val IDAT: ByteArray by lazy { byteArrayOf(0x49, 0x44, 0x41, 0x54) }
    val fdAT: ByteArray by lazy { byteArrayOf(0x66, 0x64, 0x41, 0x54) }
    val plte: ByteArray by lazy { byteArrayOf(0x50, 0x4c, 0x54, 0x45) }
    val tnrs: ByteArray by lazy { byteArrayOf(0x74, 0x52, 0x4e, 0x53) }
    val IHDR: ByteArray by lazy { byteArrayOf(0x49, 0x48, 0x44, 0x52) }
    val acTL: ByteArray by lazy { byteArrayOf(0x61, 0x63, 0x54, 0x4c) }


    /**
     * A class that contain the difference between two bitmaps
     * @property bitmap A resized [Bitmap] that contain the difference between two bitmaps
     * @property offsetX the x offset
     * @property offsetY the y offset
     * @property blendOp a [BlendOp]
     */
    data class DiffResult(
        val bitmap: Bitmap,
        val offsetX: Int,
        val offsetY: Int,
        val blendOp: BlendOp
    )

    /**
     * Get the difference between two bitmaps
     * @param firstBitmap A [Bitmap], the first bitmap
     * @param secondBitmap A [Bitmap], a second bitmap
     * @return [DiffResult], the difference between the second and the first bitmap
     */
    @Throws(BadBitmapsDiffSize::class)
    fun getDiffBitmap(firstBitmap: Bitmap, secondBitmap: Bitmap): DiffResult {
        if (firstBitmap.width < secondBitmap.width || firstBitmap.height < secondBitmap.height) {
            throw BadBitmapsDiffSize(
                firstBitmap.width,
                firstBitmap.height,
                firstBitmap.width,
                firstBitmap.height
            )
        }

        val resultBtm = Bitmap.createBitmap(
            secondBitmap.width,
            secondBitmap.height,
            Bitmap.Config.ARGB_8888
        )

        var offsetX = resultBtm.width + 1
        var offsetY = resultBtm.height + 1

        var lastX = 0
        var lastY = 0

        // Find if the image contain transparent pixels, if true, then transparent pixels must replace the pixels in the buffer
        val blendOp =
            if (containTransparency(secondBitmap)) APNG_BLEND_OP_SOURCE else APNG_BLEND_OP_OVER

        for (y in 0 until secondBitmap.height) {
            for (x in 0 until secondBitmap.width) {
                val btmPixel = secondBitmap.getPixel(x, y)
                if (firstBitmap.getPixel(
                        x,
                        y
                    ) == btmPixel
                ) { // Similar pixels could be forgotten
                    if (blendOp == APNG_BLEND_OP_OVER)
                        resultBtm.setPixel(x, y, Color.TRANSPARENT)
                    else
                        resultBtm.setPixel(x, y, firstBitmap.getPixel(x, y))
                } else { // Otherwise, track image bounds
                    resultBtm.setPixel(x, y, btmPixel)
                    if (x < offsetX)
                        offsetX = x
                    if (y < offsetY)
                        offsetY = y
                    if (x > lastX)
                        lastX = x
                    if (y > lastY)
                        lastY = y
                }
            }
        }

        lastX++
        lastY++

        val newWidth = lastX - offsetX
        val newHeight = lastY - offsetY

        // Resize bitmap
        val resizedResultBtm =
            Bitmap.createBitmap(resultBtm, offsetX, offsetY, newWidth, newHeight)

        return DiffResult(resizedResultBtm, offsetX, offsetY, blendOp)
    }

    /**
     * This function return true if the bitmap contain transparent pixels
     * @param btm A [Bitmap]
     * @return [Boolean] true if if the bitmap contain transparent pixels
     */
    fun containTransparency(btm: Bitmap): Boolean {
        var result = false
        var y = 0
        var x = 0
        while (y < btm.height && !result) {
            if (btm.getPixel(x, y) == Color.TRANSPARENT) {
                result = true
            }

            x++
            if (x == btm.width) {
                x = 0
                y++
            }
        }
        return result
    }

    suspend fun <T, U> Result<T>.mapResult(block: suspend (T) -> Result<U>): Result<U> {
        return this.fold(
            onSuccess = {
                block.invoke(it)
            },
            onFailure = { Result.failure(it) }
        )
    }
}