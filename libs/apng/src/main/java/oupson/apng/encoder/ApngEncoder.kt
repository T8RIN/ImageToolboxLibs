package oupson.apng.encoder

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import oupson.apng.exceptions.BadParameterException
import oupson.apng.exceptions.InvalidFrameSizeException
import oupson.apng.utils.Utils
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.zip.CRC32
import java.util.zip.Deflater
import java.util.zip.DeflaterOutputStream
import kotlin.math.max
import kotlin.math.min

/**
 * A class to write APNG.
 *
 * IDAT encoding par was taken from [PngEncoder by J. David Eisenberg](http://catcode.com/pngencoder/com/keypoint/PngEncoder.java).
 *
 * Usage :
 *  - Instantiate the class.
 *  - Call [writeFrame].
 *  - Call [writeEnd] at the end of the animation.
 * @param outputStream The output stream.
 * @param width Width of the animation
 * @param height Height of the animation
 * @param numberOfFrames The number of frame the animation is composed.
 * @throws IOException If something failed when writing into the output stream.
 */
class ApngEncoder(
    private val outputStream: OutputStream,
    private val width: Int,
    private val height: Int,
    numberOfFrames: Int
) {
    companion object {
        private const val TAG = "ApngEncoder"

        /** Constants for filter (NONE)  */
        const val FILTER_NONE = 0

        /** Constants for filter (SUB)  */
        const val FILTER_SUB = 1

        /** Constants for filter (UP)  */
        const val FILTER_UP = 2

        /** Constants for filter (LAST)  */
        const val FILTER_LAST = 2
    }

    /** Current Frame.  **/
    private var currentFrame = 0

    /**
     * Current sequence of the animation.
     * @see [https://developer.mozilla.org/en-US/docs/Mozilla/Tech/APNG#Chunk_sequence_numbers](Mozilla documentation.)
     **/
    private var currentSeq = 0

    /** CRC.  */
    private var crc = CRC32()

    /** The CRC value.  */
    private var crcValue: Long = 0

    /** The bytes-per-pixel.  */
    private var bytesPerPixel: Int = 0

    /** The compression level.  */
    private var compressionLevel: Int = 0

    /** The filter type.  */
    private var filter: Int = FILTER_NONE

    /** If the alpha channel must be encoded    */
    private var encodeAlpha: Boolean = true

    /** The prior row.  */
    private var priorRow: ByteArray? = null

    /** The left bytes.  */
    private var leftBytes: ByteArray? = null

    /** Number of loop of the animation, zero to infinite **/
    private var repetitionCount: Int = 0

    /** If the first frame should be included in the animation  **/
    private var firstFrameInAnim: Boolean = true

    /** Specify if the encoder will use features such as BlendOp */
    private var optimise: Boolean = true

    /** Used if the animation must be optimised */
    private var bitmapBuffer: Bitmap? = null

    init {
        outputStream.write(Utils.pngSignature)
        writeHeader()
        writeACTL(numberOfFrames)
    }

    /**
     * Set if the alpha channel must be encoded.
     * @param encodeAlpha If the alpha channel must be encoded.
     * @return [ApngEncoder] for chaining.
     */
    fun setEncodeAlpha(encodeAlpha: Boolean): ApngEncoder {
        if (optimise && !encodeAlpha)
            throw BadParameterException("If encodeAlpha is false, then optimise must be false")
        this.encodeAlpha = encodeAlpha
        return this
    }

    /**
     * Return true if the alpha is encoded.
     * @return [Boolean] the value.
     */
    fun isAlphaEncoded(): Boolean {
        return encodeAlpha
    }

    /**
     * Set the filter.
     * @param filter The filter.
     * Values :
     * - [FILTER_NONE]
     * - [FILTER_SUB]
     * - [FILTER_UP]
     * - [FILTER_LAST]
     * @return [ApngEncoder] for chaining.
     */
    @Throws(BadParameterException::class)
    fun setFilter(filter: Int): ApngEncoder {
        if (filter <= FILTER_LAST) {
            this.filter = filter
        } else {
            throw BadParameterException("Invalid filter value")
        }
        return this
    }

    /**
     * Return the filter.
     * @return An [Int] corresponding to the value.
     */
    fun getFilter(): Int {
        return filter
    }

    /**
     * Set the repetition count.
     * @param repetitionCount The number of repetition, zero for infinite.
     * @return [ApngEncoder] for chaining.
     */
    @Suppress("unused")
    fun setRepetitionCount(repetitionCount: Int): ApngEncoder {
        this.repetitionCount = repetitionCount
        return this
    }

    /**
     * Get the repetition count.
     * @return An [Int], the number of repetition, zero for infinite.
     */
    @Suppress("unused")
    fun getRepetitionCount(): Int {
        return this.repetitionCount
    }

    /**
     * Set the compression level.
     * @param compressionLevel A integer between 0 and 9 (not include).
     * @return [ApngEncoder] for chaining.
     */
    @Throws(BadParameterException::class)
    fun setCompressionLevel(compressionLevel: Int): ApngEncoder {
        if (compressionLevel in 0..9) {
            this.compressionLevel = compressionLevel
        } else {
            throw BadParameterException("Invalid compression level : $compressionLevel, expected a number in range 0..9")
        }

        return this
    }

    /**
     * Get compression level.
     * @return [Int] the compression level, an integer in between 0 and 9
     */
    fun getCompressionLevel(): Int {
        return this.compressionLevel
    }

    /**
     * Set if the first frame will be included in the animation.
     * @param firstFrameInAnim A boolean.
     * @return [ApngEncoder] for chaining.
     */
    fun setIsFirstFrameInAnim(firstFrameInAnim: Boolean): ApngEncoder {
        this.firstFrameInAnim = firstFrameInAnim
        return this
    }

    /**
     * Get if the first frame will be included in the animation.
     * @return [Boolean] True if the first frame will be included in the animation.
     */
    @Suppress("unused")
    fun isFirstFrameInAnim(): Boolean {
        return this.firstFrameInAnim
    }

    /**
     * Set if the animation must be optimised (true by default)
     * If set, then the encoder will use features such as Blend Op to reduce the size of the animation
     * @param optimise If the animation must be optimised
     * @return [ApngEncoder] for chaining.
     */
    fun setOptimiseApng(optimise: Boolean): ApngEncoder {
        if (optimise && !encodeAlpha)
            throw BadParameterException("If optimise is set to true, then encodeAlpha must be true")
        this.optimise = optimise
        return this
    }

    /**
     * Return true if the encoder will use features such as Blend Op to reduce the size of the animation
     * @return A [Boolean]
     */
    fun isOptimisingApng(): Boolean {
        return this.optimise
    }

    /**
     * Write a frame into the output stream.
     * @param inputStream An input stream that will be decoded in order to be written in the animation. Not freed.
     * @param delay A delay in ms.
     * @param xOffsets The offset of the left bound of the frame in the animation.
     * @param yOffsets The offset of the top bound of the frame in the animation.
     * @param blendOp See [Utils.BlendOp].
     * @param disposeOp See [Utils.DisposeOp].
     * @throws NullPointerException If the bitmap failed to be decoded
     * @throws InvalidFrameSizeException If the frame size is bigger than the animation size, or the first frame size is not equal to the animation size.
     * @throws IOException If something failed when writing into the output stream.
     */
    @JvmOverloads
    @Throws(
        NullPointerException::class,
        InvalidFrameSizeException::class,
        IOException::class
    )
    fun writeFrame(
        inputStream: InputStream,
        delay: Float = 1000f,
        xOffsets: Int = 0,
        yOffsets: Int = 0,
        blendOp: Utils.BlendOp = Utils.BlendOp.APNG_BLEND_OP_SOURCE,
        disposeOp: Utils.DisposeOp = Utils.DisposeOp.APNG_DISPOSE_OP_NONE
    ) {
        val btm = BitmapFactory.decodeStream(inputStream)!!

        writeFrame(btm, delay, xOffsets, yOffsets, blendOp, disposeOp)
        btm.recycle()
    }

    /**
     * Write a frame into the output stream.
     * @param btm An bitmap that will be written in the animation.
     * @param delay A delay in ms.
     * @param xOffsets The offset of the left bound of the frame in the animation.
     * @param yOffsets The offset of the top bound of the frame in the animation.
     * @param blendOp See [Utils.BlendOp].
     * @param disposeOp See [Utils.DisposeOp].
     * @throws InvalidFrameSizeException If the frame size is bigger than the animation size, or the first frame size is not equal to the animation size.
     * @throws IOException If something failed when writing into the output stream.
     */
    @JvmOverloads
    @Throws(InvalidFrameSizeException::class, IOException::class)
    fun writeFrame(
        btm: Bitmap,
        delay: Float = 1000f,
        xOffsets: Int = 0,
        yOffsets: Int = 0,
        blendOp: Utils.BlendOp = Utils.BlendOp.APNG_BLEND_OP_SOURCE,
        disposeOp: Utils.DisposeOp = Utils.DisposeOp.APNG_DISPOSE_OP_NONE
    ) {
        if (currentFrame == 0) {
            if (btm.width != width || btm.height != height)
                throw InvalidFrameSizeException( // TODO
                    btm.width,
                    btm.height,
                    width,
                    height,
                    currentFrame == 0
                )
        }

        var frameBtm = btm
        var frameXOffsets = xOffsets
        var frameYOffsets = yOffsets
        var frameBlendOp = blendOp

        if (optimise && currentFrame != 0 || (currentFrame == 0 && firstFrameInAnim)) {
            if (bitmapBuffer == null) {
                bitmapBuffer = btm.copy(btm.config, false)
            } else {
                val diff = Utils.getDiffBitmap(bitmapBuffer!!, btm)
                frameBtm = diff.bitmap
                frameXOffsets = diff.offsetX
                frameYOffsets = diff.offsetY
                frameBlendOp = diff.blendOp
                bitmapBuffer = btm.copy(btm.config, false)
            }
        }

        if (frameBtm.width > width || frameBtm.height > height)
            throw InvalidFrameSizeException(
                frameBtm.width,
                frameBtm.height,
                width,
                height,
                currentFrame == 0
            )

        if (firstFrameInAnim || currentFrame != 0)
            writeFCTL(frameBtm, delay, disposeOp, frameBlendOp, frameXOffsets, frameYOffsets)
        writeImageData(frameBtm)
        currentFrame++
    }

    /**
     * Write the end of the animation.
     * @throws IOException If something failed when writing into the output stream.
     */
    @Throws(IOException::class)
    fun writeEnd() {
        // Add IEND body length : 0
        outputStream.write(byteArrayOf(0, 0, 0, 0))
        // Add IEND
        val iend = Utils.IEND
        // Generate crc for IEND
        crc.reset()
        crc.update(iend, 0, iend.size)
        outputStream.write(iend)
        outputStream.write(Utils.uIntToByteArray(crc.value.toInt()))
    }

    /**
     * Write the header into the outputStream.
     * @throws IOException If something failed when writing into the output stream.
     */
    @Throws(IOException::class)
    private fun writeHeader() {
        writeInt4(13) // 4 + 4 + 1 + 1 + 1 + 1 +1

        val header = ByteArrayOutputStream(17) // 4 + 4 + 4 + 1 + 1 + 1 + 1 +1

        header.write(Utils.IHDR)
        header.write(Utils.uIntToByteArray(width))
        header.write(Utils.uIntToByteArray(height))
        header.write(8) // bit depth
        header.write(if (encodeAlpha) 6 else 2) // direct model
        header.write(0) // compression method
        header.write(0) // filter method
        header.write(0) // no interlace

        val headerBytes = header.toByteArray()
        outputStream.write(
            headerBytes
        )
        crc.reset()
        crc.update(headerBytes)
        crcValue = crc.value
        writeInt4(crcValue.toInt())
    }

    /**
     * Write a four-byte integer into the outputStream.
     * @param n The four-byte integer to write.
     * @throws IOException If something failed when writing into the output stream.
     */
    @Throws(IOException::class)
    private fun writeInt4(n: Int) {
        outputStream.write(Utils.uIntToByteArray(n))
    }

    /**
     * Write the animation control chunk into the outputStream.
     * @param num The number of frame the animation contain.
     * @throws IOException If something failed when writing into the output stream.
     */
    @Throws(IOException::class)
    private fun writeACTL(num: Int) {
        // Add length bytes
        outputStream.write(byteArrayOf(0, 0, 0, 0x08))

        val acTL = ByteArrayOutputStream(12) // 4 + 4 + 4

        acTL.write(Utils.acTL) // Add acTL
        acTL.write(Utils.uIntToByteArray(num)) // Add number of frames
        acTL.write(Utils.uIntToByteArray(repetitionCount)) // Number of repeat, 0 to infinite

        val acTLBytes = acTL.toByteArray()

        outputStream.write(acTLBytes)

        // generate crc
        crc.reset()
        crc.update(acTLBytes, 0, acTLBytes.size)
        outputStream.write(Utils.uIntToByteArray(crc.value.toInt()))
    }

    /**
     * Write the frame control chunk into the outputStream.
     * @throws IOException If something failed when writing into the output stream.
     */
    @Throws(IOException::class)
    private fun writeFCTL(
        btm: Bitmap,
        delay: Float,
        disposeOp: Utils.DisposeOp,
        blendOp: Utils.BlendOp,
        xOffsets: Int,
        yOffsets: Int
    ) {
        // Add the length of the chunk body
        outputStream.write(byteArrayOf(0x00, 0x00, 0x00, 0x1A))

        val fcTL = ByteArrayOutputStream(30) // 0x1A + 4

        // Add fcTL
        fcTL.write(Utils.fcTL)
        // Add the frame number
        fcTL.write(Utils.uIntToByteArray(currentSeq++))

        // Add width and height
        fcTL.write(Utils.uIntToByteArray(btm.width))
        fcTL.write(Utils.uIntToByteArray(btm.height))

        // Add offsets
        fcTL.write(Utils.uIntToByteArray(xOffsets))
        fcTL.write(Utils.uIntToByteArray(yOffsets))

        // Set frame delay
        // TODO BETTER FRACTION
        fcTL.write(Utils.uShortToByteArray(delay.toInt().toShort()))
        fcTL.write(Utils.uShortToByteArray(1000.toShort()))

        // Add DisposeOp and BlendOp
        fcTL.write(Utils.encodeDisposeOp(disposeOp))
        fcTL.write(Utils.encodeBlendOp(blendOp))

        val fcTLBytes = fcTL.toByteArray()

        // Create CRC
        crc.reset()
        crc.update(fcTLBytes, 0, fcTLBytes.size)

        // Write all
        outputStream.write(fcTLBytes)
        outputStream.write(Utils.uIntToByteArray(crc.value.toInt()))
    }

    /**
     * Write the image data into the outputStream.
     * This will write one or more PNG "IDAT"/"fdAT" chunks. In order
     * to conserve memory, this method grabs as many rows as will
     * fit into 32K bytes, or the whole image; whichever is less.
     *
     * @param image The frame to encode
     *
     * @return [Boolean] true if no errors; false if error grabbing pixels
     */
    private fun writeImageData(image: Bitmap): Boolean {
        var rowsLeft = image.height  // number of rows remaining to write
        var startRow = 0       // starting row to process this time through
        var nRows: Int              // how many rows to grab at a time

        var scanLines: ByteArray       // the scan lines to be compressed
        var scanPos: Int            // where we are in the scan lines
        var startPos: Int           // where this line's actual pixels start (used for filtering)

        val compressedLines: ByteArray // the resultant compressed lines
        val nCompressed: Int        // how big is the compressed area?

        //int depth;              // color depth ( handle only 8 or 32 )

        bytesPerPixel = if (encodeAlpha) 4 else 3

        val scrunch = Deflater(compressionLevel)
        val outBytes = ByteArrayOutputStream(1024)

        val compBytes = DeflaterOutputStream(outBytes, scrunch)
        try {
            while (rowsLeft > 0) {
                nRows = min(32767 / (image.width * (bytesPerPixel + 1)), rowsLeft)
                nRows = max(nRows, 1)

                val pixels = IntArray(image.width * nRows)

                //pg = new PixelGrabber(image, 0, startRow, width, nRows, pixels, 0, width);
                image.getPixels(pixels, 0, image.width, 0, startRow, image.width, nRows)

                /*
                * Create a data chunk. scanLines adds "nRows" for
                * the filter bytes.
                */
                scanLines = ByteArray(image.width * nRows * bytesPerPixel + nRows)

                if (filter == FILTER_SUB) {
                    leftBytes = ByteArray(16)
                }
                if (filter == FILTER_UP) {
                    priorRow = ByteArray(image.width * bytesPerPixel)
                }

                scanPos = 0
                startPos = 1
                for (i in 0 until image.width * nRows) {
                    if (i % image.width == 0) {
                        scanLines[scanPos++] = filter.toByte()
                        startPos = scanPos
                    }
                    scanLines[scanPos++] = (pixels[i] shr 16 and 0xff).toByte()
                    scanLines[scanPos++] = (pixels[i] shr 8 and 0xff).toByte()
                    scanLines[scanPos++] = (pixels[i] and 0xff).toByte()
                    if (encodeAlpha) {
                        scanLines[scanPos++] = (pixels[i] shr 24 and 0xff).toByte()
                    }
                    if (i % image.width == image.width - 1 && filter != FILTER_NONE) {
                        if (filter == FILTER_SUB) {
                            filterSub(scanLines, startPos, image.width)
                        }
                        if (filter == FILTER_UP) {
                            filterUp(scanLines, startPos, image.width)
                        }
                    }
                }

                /*
                * Write these lines to the output area
                */
                compBytes.write(scanLines, 0, scanPos)

                startRow += nRows
                rowsLeft -= nRows
            }
            compBytes.close()

            /*
            * Write the compressed bytes
            */
            compressedLines = outBytes.toByteArray()
            nCompressed = compressedLines.size

            crc.reset()
            // Add 4 bytes to the length, for the sequence number, if the current frame is not the first frame (and not an IDAT).
            writeInt4(nCompressed + if (currentFrame == 0) 0 else 4)

            // If the current frame is the first frame, write idat for backward compatibility
            if (currentFrame == 0) {
                outputStream.write(Utils.IDAT)
                crc.update(Utils.IDAT)
            } else { // Write a fdAT chunk, containing the current sequence number
                // Add fdat and sequence number
                val fdat = Utils.fdAT + Utils.uIntToByteArray(currentSeq++)

                outputStream.write(fdat)
                crc.update(fdat)
            }
            outputStream.write(compressedLines)
            crc.update(compressedLines, 0, nCompressed)

            crcValue = crc.value
            writeInt4(crcValue.toInt())
            scrunch.finish()
            scrunch.end()
            return true
        } catch (e: IOException) {
            Log.e(TAG, "Error while writing IDAT/fdAT chunks", e)
            return false
        }
    }

    /**
     * Perform "sub" filtering on the given row.
     * Uses temporary array leftBytes to store the original values
     * of the previous pixels.  The array is 16 bytes long, which
     * will easily hold two-byte samples plus two-byte alpha.
     *
     * @param pixels The array holding the scan lines being built
     * @param startPos Starting position within pixels of bytes to be filtered.
     * @param width Width of a scanline in pixels.
     */
    private fun filterSub(pixels: ByteArray, startPos: Int, width: Int) {
        val offset = bytesPerPixel
        val actualStart = startPos + offset
        val nBytes = width * bytesPerPixel
        var leftInsert = offset
        var leftExtract = 0
        var i: Int = actualStart
        while (i < startPos + nBytes) {
            leftBytes!![leftInsert] = pixels[i]
            pixels[i] = ((pixels[i] - leftBytes!![leftExtract]) % 256).toByte()
            leftInsert = (leftInsert + 1) % 0x0f
            leftExtract = (leftExtract + 1) % 0x0f
            i++
        }
    }

    /**
     * Perform "up" filtering on the given row.
     * Side effect: refills the prior row with current row
     *
     * @param pixels The array holding the scan lines being built
     * @param startPos Starting position within pixels of bytes to be filtered.
     * @param width Width of a scanline in pixels.
     */
    private fun filterUp(pixels: ByteArray, startPos: Int, width: Int) {
        var i = 0
        val nBytes: Int = width * bytesPerPixel
        var currentByte: Byte
        while (i < nBytes) {
            currentByte = pixels[startPos + i]
            pixels[startPos + i] = ((pixels[startPos + i] - priorRow!![i]) % 256).toByte()
            priorRow!![i] = currentByte
            i++
        }
    }
}