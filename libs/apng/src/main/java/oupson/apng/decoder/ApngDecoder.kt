package oupson.apng.decoder

import android.content.Context
import android.graphics.*
import android.graphics.drawable.AnimatedImageDrawable
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.annotation.RawRes
import kotlinx.coroutines.*
import oupson.apng.BuildConfig
import oupson.apng.drawable.ApngDrawable
import oupson.apng.exceptions.BadApngException
import oupson.apng.exceptions.BadCRCException
import oupson.apng.utils.Loader
import oupson.apng.utils.Utils
import java.io.*
import java.net.URL
import java.nio.ByteBuffer
import java.util.zip.CRC32

/**
 * An APNG Decoder.
 * Call [decodeApng]
 */
class ApngDecoder(input: InputStream, val config: Config) {
    class Config(
        internal var speed: Float = 1f,
        internal var bitmapConfig: Bitmap.Config = Bitmap.Config.ARGB_8888,
        internal var decodeCoverFrame: Boolean = false
    ) {
        fun getSpeed(): Float = this.speed
        fun setSpeed(speed: Float): Config {
            this.speed = speed
            return this
        }

        fun getBitmapConfig(): Bitmap.Config = this.bitmapConfig
        fun setBitmapConfig(config: Bitmap.Config): Config {
            this.bitmapConfig = config
            return this
        }

        fun isDecodingCoverFrame(): Boolean {
            return this.decodeCoverFrame
        }

        fun setIsDecodingCoverFrame(decodeCoverFrame: Boolean): Config {
            this.decodeCoverFrame = decodeCoverFrame
            return this
        }
    }

    private var inputStream: InputStream? = input
    private var result: Result<Drawable>? = null

    /**
     * Decode Apng and return a Drawable who can be an [ApngDrawable] if it end successfully. Can also be an [android.graphics.drawable.AnimatedImageDrawable].
     * @param context Context needed for the animation drawable
     * @param inStream Input Stream to decode. Will be closed at the end.
     * @param config Decoder configuration
     * @return [ApngDrawable] if successful and an [AnimatedImageDrawable] if the image decoded is not an APNG but a gif. If it is not an animated image, it is a [Drawable].
     */
    suspend fun decodeApng(
        context: Context
    ): Result<Drawable> =
        kotlin.runCatching {
            withContext(Dispatchers.Default) {
                val inputStream = BufferedInputStream(inputStream)
                val bytes = ByteArray(8)
                inputStream.mark(8)

                withContext(Dispatchers.IO) {
                    inputStream.read(bytes)
                }

                if (Utils.isPng(bytes)) {
                    var png: ByteArrayOutputStream? = null
                    var cover: ByteArrayOutputStream? = null
                    var delay = -1f
                    var yOffset = -1
                    var xOffset = -1
                    var plte: ByteArray? = null
                    var tnrs: ByteArray? = null
                    var maxWidth = 0
                    var maxHeight = 0
                    var blendOp: Utils.Companion.BlendOp =
                        Utils.Companion.BlendOp.APNG_BLEND_OP_SOURCE
                    var disposeOp: Utils.Companion.DisposeOp =
                        Utils.Companion.DisposeOp.APNG_DISPOSE_OP_NONE

                    var ihdrOfApng = ByteArray(0)

                    var isApng = false

                    val drawable = ApngDrawable().apply {
                        isOneShot = false
                    }

                    var buffer: Bitmap? = null

                    var byteRead: Int
                    val lengthChunk = ByteArray(4)
                    do {
                        val length: Int
                        val chunk: ByteArray
                        if (withContext(Dispatchers.IO) {
                                byteRead = inputStream.read(lengthChunk)

                                if (byteRead != -1) {
                                    length = Utils.uIntFromBytesBigEndian(lengthChunk)

                                    chunk = ByteArray(length + 8)
                                    byteRead = inputStream.read(chunk)
                                    false
                                } else {
                                    chunk = ByteArray(0)
                                    true
                                }
                            }) {
                            break
                        }

                        val byteArray = lengthChunk.plus(chunk)
                        val chunkCRC = Utils.uIntFromBytesBigEndian(byteArray, byteArray.size - 4)
                        val crc = CRC32()
                        crc.update(byteArray, 4, byteArray.size - 8)
                        if (chunkCRC == crc.value.toInt()) {
                            val name = byteArray.copyOfRange(4, 8)
                            when {
                                name.contentEquals(Utils.fcTL) -> {
                                    if (png == null) {
                                        if (config.decodeCoverFrame) {
                                            drawable.coverFrame = cover?.let {
                                                it.write(zeroLength)
                                                // Generate crc for IEND
                                                val crC32 = CRC32()
                                                crC32.update(Utils.IEND, 0, Utils.IEND.size)
                                                it.write(Utils.IEND)
                                                it.write(Utils.uIntToByteArray(crC32.value.toInt()))

                                                val pngBytes = it.toByteArray()
                                                BitmapFactory.decodeByteArray(
                                                    pngBytes,
                                                    0,
                                                    pngBytes.size
                                                )
                                            }
                                        }
                                        cover?.close()
                                        cover = null
                                    } else {
                                        // Add IEND body length : 0
                                        png.write(zeroLength)
                                        // Add IEND
                                        // Generate crc for IEND
                                        val crC32 = CRC32()
                                        crC32.update(Utils.IEND, 0, Utils.IEND.size)
                                        png.write(Utils.IEND)
                                        png.write(Utils.uIntToByteArray(crC32.value.toInt()))

                                        val btm = Bitmap.createBitmap(
                                            maxWidth,
                                            maxHeight,
                                            Bitmap.Config.ARGB_8888
                                        )

                                        val pngBytes = png.toByteArray()
                                        val decoded = BitmapFactory.decodeByteArray(
                                            pngBytes,
                                            0,
                                            pngBytes.size
                                        )
                                        val canvas = Canvas(btm)
                                        canvas.drawBitmap(buffer!!, 0f, 0f, null)

                                        if (blendOp == Utils.Companion.BlendOp.APNG_BLEND_OP_SOURCE) {
                                            canvas.drawRect(
                                                xOffset.toFloat(),
                                                yOffset.toFloat(),
                                                xOffset + decoded.width.toFloat(),
                                                yOffset + decoded.height.toFloat(),
                                                clearPaint
                                            )
                                        }

                                        canvas.drawBitmap(
                                            decoded,
                                            xOffset.toFloat(),
                                            yOffset.toFloat(),
                                            null
                                        )

                                        drawable.addFrame(
                                            BitmapDrawable(
                                                context.resources,
                                                if (btm.config != config.bitmapConfig) {
                                                    if (BuildConfig.DEBUG)
                                                        Log.v(
                                                            TAG,
                                                            "Bitmap Config : ${btm.config}, Config : $config"
                                                        )
                                                    btm.copy(config.bitmapConfig, btm.isMutable)
                                                } else {
                                                    btm
                                                }
                                            ),
                                            (delay / config.speed).toInt()
                                        )

                                        when (disposeOp) {
                                            Utils.Companion.DisposeOp.APNG_DISPOSE_OP_PREVIOUS -> {
                                                //Do nothings
                                            }
                                            // Add current frame to bitmap buffer
                                            // APNG_DISPOSE_OP_BACKGROUND: the frame's region of the output buffer is to be cleared to fully transparent black before rendering the next frame.
                                            Utils.Companion.DisposeOp.APNG_DISPOSE_OP_BACKGROUND -> {
                                                val res = Bitmap.createBitmap(
                                                    maxWidth,
                                                    maxHeight,
                                                    Bitmap.Config.ARGB_8888
                                                )
                                                val can = Canvas(res)
                                                can.drawBitmap(btm, 0f, 0f, null)
                                                can.drawRect(
                                                    xOffset.toFloat(),
                                                    yOffset.toFloat(),
                                                    xOffset + decoded.width.toFloat(),
                                                    yOffset + decoded.height.toFloat(),
                                                    clearPaint
                                                )
                                                buffer = res
                                            }
                                            else -> buffer = btm
                                        }
                                    }

                                    png?.close()
                                    png = ByteArrayOutputStream(4096)

                                    // Parse Frame ConTroL chunk
                                    // Get the width of the png
                                    val width = Utils.uIntFromBytesBigEndian(
                                        byteArray, 12
                                    )
                                    // Get the height of the png
                                    val height = Utils.uIntFromBytesBigEndian(
                                        byteArray, 16
                                    )

                                    /*
                             * The `delay_num` and `delay_den` parameters together specify a fraction indicating the time to display the current frame, in seconds.
                             * If the the value of the numerator is 0 the decoder should render the next frame as quickly as possible, though viewers may impose a reasonable lower bound.
                             */
                                    // Get delay numerator
                                    val delayNum = Utils.uShortFromBytesBigEndian(
                                        byteArray, 28
                                    ).toFloat()
                                    // Get delay denominator
                                    var delayDen = Utils.uShortFromBytesBigEndian(
                                        byteArray, 30
                                    ).toFloat()

                                    // If the denominator is 0, it is to be treated as if it were 100 (that is, `delay_num` then specifies 1/100ths of a second).
                                    if (delayDen == 0f) {
                                        delayDen = 100f
                                    }

                                    delay = (delayNum / delayDen * 1000)

                                    // Get x and y offsets
                                    xOffset = Utils.uIntFromBytesBigEndian(
                                        byteArray, 20
                                    )
                                    yOffset = Utils.uIntFromBytesBigEndian(
                                        byteArray, 24
                                    )
                                    blendOp = Utils.decodeBlendOp(byteArray[33].toInt())
                                    disposeOp = Utils.decodeDisposeOp(byteArray[32].toInt())

                                    if (xOffset + width > maxWidth) {
                                        throw BadApngException("`xOffset` + `width` must be <= `IHDR` width")
                                    } else if (yOffset + height > maxHeight) {
                                        throw BadApngException("`yOffset` + `height` must be <= `IHDR` height")
                                    }

                                    png.write(Utils.pngSignature)
                                    png.write(
                                        generateIhdr(
                                            ihdrOfApng,
                                            width,
                                            height
                                        )
                                    )
                                    plte?.let {
                                        png.write(it)
                                    }
                                    tnrs?.let {
                                        png.write(it)
                                    }

                                }
                                name.contentEquals(Utils.IEND) -> {
                                    if (isApng && png != null) {
                                        png.write(zeroLength)
                                        // Add IEND
                                        // Generate crc for IEND
                                        val crC32 = CRC32()
                                        crC32.update(Utils.IEND, 0, Utils.IEND.size)
                                        png.write(Utils.IEND)
                                        png.write(Utils.uIntToByteArray(crC32.value.toInt()))

                                        val btm = Bitmap.createBitmap(
                                            maxWidth,
                                            maxHeight,
                                            Bitmap.Config.ARGB_8888
                                        )

                                        val pngBytes = png.toByteArray()
                                        png.close()
                                        val decoded = BitmapFactory.decodeByteArray(
                                            pngBytes,
                                            0,
                                            pngBytes.size
                                        )
                                        val canvas = Canvas(btm)
                                        canvas.drawBitmap(buffer!!, 0f, 0f, null)

                                        if (blendOp == Utils.Companion.BlendOp.APNG_BLEND_OP_SOURCE) {
                                            canvas.drawRect(
                                                xOffset.toFloat(),
                                                yOffset.toFloat(),
                                                xOffset + decoded.width.toFloat(),
                                                yOffset + decoded.height.toFloat(),
                                                clearPaint
                                            )
                                        }

                                        canvas.drawBitmap(
                                            decoded,
                                            xOffset.toFloat(),
                                            yOffset.toFloat(),
                                            null
                                        )
                                        drawable.addFrame(
                                            BitmapDrawable(
                                                context.resources,
                                                if (btm.config != config.bitmapConfig) {
                                                    if (BuildConfig.DEBUG)
                                                        Log.v(
                                                            TAG,
                                                            "Bitmap Config : ${btm.config}, Config : $config"
                                                        )
                                                    btm.copy(config.bitmapConfig, btm.isMutable)
                                                } else {
                                                    btm
                                                }
                                            ),
                                            (delay / config.speed).toInt()
                                        )

                                        when (disposeOp) {
                                            Utils.Companion.DisposeOp.APNG_DISPOSE_OP_PREVIOUS -> {
                                                //Do nothings
                                            }
                                            // Add current frame to bitmap buffer
                                            // APNG_DISPOSE_OP_BACKGROUND: the frame's region of the output buffer is to be cleared to fully transparent black before rendering the next frame.
                                            Utils.Companion.DisposeOp.APNG_DISPOSE_OP_BACKGROUND -> {
                                                val res = Bitmap.createBitmap(
                                                    maxWidth,
                                                    maxHeight,
                                                    Bitmap.Config.ARGB_8888
                                                )
                                                val can = Canvas(res)
                                                can.drawBitmap(btm, 0f, 0f, null)
                                                can.drawRect(
                                                    xOffset.toFloat(),
                                                    yOffset.toFloat(),
                                                    xOffset + decoded.width.toFloat(),
                                                    yOffset + decoded.height.toFloat(),
                                                    clearPaint
                                                )
                                                buffer = res
                                            }
                                            else -> buffer = btm
                                        }
                                    } else {
                                        cover?.let {
                                            it.write(zeroLength)
                                            // Add IEND
                                            // Generate crc for IEND
                                            val crC32 = CRC32()
                                            crC32.update(Utils.IEND, 0, Utils.IEND.size)
                                            it.write(Utils.IEND)
                                            it.write(Utils.uIntToByteArray(crC32.value.toInt()))

                                            withContext(Dispatchers.IO) {
                                                inputStream.close()
                                            }

                                            val pngBytes = it.toByteArray()
                                            it.close()

                                            return@withContext BitmapDrawable(
                                                context.resources,
                                                BitmapFactory.decodeByteArray(
                                                    pngBytes,
                                                    0,
                                                    pngBytes.size
                                                )
                                            )
                                        }
                                    }
                                }
                                name.contentEquals(Utils.IDAT) -> {
                                    val w = if (png == null) {
                                        if (isApng && !config.decodeCoverFrame) {
                                            if (BuildConfig.DEBUG)
                                                Log.d(TAG, "Ignoring cover frame")
                                            continue
                                        }
                                        if (cover == null) {
                                            cover = ByteArrayOutputStream()
                                            cover.write(Utils.pngSignature)
                                            cover.write(
                                                generateIhdr(
                                                    ihdrOfApng,
                                                    maxWidth,
                                                    maxHeight
                                                )
                                            )
                                        }
                                        cover
                                    } else {
                                        png
                                    }

                                    // Find the chunk length
                                    val bodySize =
                                        Utils.uIntFromBytesBigEndian(
                                            byteArray, 0
                                        )
                                    w.write(byteArray.copyOfRange(0, 4))

                                    val body = ByteArray(4 + bodySize)

                                    System.arraycopy(Utils.IDAT, 0, body, 0, 4)

                                    // Get image bytes
                                    System.arraycopy(byteArray, 8, body, 4, bodySize)

                                    val crC32 = CRC32()
                                    crC32.update(body, 0, body.size)
                                    w.write(body)
                                    w.write(Utils.uIntToByteArray(crC32.value.toInt()))
                                }
                                name.contentEquals(Utils.fdAT) -> {
                                    // Find the chunk length
                                    val bodySize = Utils.uIntFromBytesBigEndian(byteArray, 0)
                                    png?.write(Utils.uIntToByteArray(bodySize - 4))

                                    val body = ByteArray(bodySize)
                                    System.arraycopy(Utils.IDAT, 0, body, 0, 4)

                                    // Get image bytes
                                    System.arraycopy(byteArray, 12, body, 4, bodySize - 4)

                                    val crC32 = CRC32()
                                    crC32.update(body, 0, body.size)
                                    png?.write(body)
                                    png?.write(Utils.uIntToByteArray(crC32.value.toInt()))
                                }
                                name.contentEquals(Utils.plte) -> {
                                    plte = byteArray
                                }
                                name.contentEquals(Utils.tnrs) -> {
                                    tnrs = byteArray
                                }
                                name.contentEquals(Utils.IHDR) -> {
                                    // Get length of the body of the chunk
                                    val bodySize = Utils.uIntFromBytesBigEndian(byteArray, 0)
                                    // Get the width of the png
                                    maxWidth = Utils.uIntFromBytesBigEndian(byteArray, 8)
                                    // Get the height of the png
                                    maxHeight = Utils.uIntFromBytesBigEndian(byteArray, 12)
                                    ihdrOfApng = byteArray.copyOfRange(4 + 4, 4 + bodySize + 4)

                                    buffer = Bitmap.createBitmap(
                                        maxWidth,
                                        maxHeight,
                                        Bitmap.Config.ARGB_8888
                                    )
                                }
                                name.contentEquals(Utils.acTL) -> { // TODO GET NBR REPETITIONS
                                    isApng = true
                                }
                            }
                        } else throw BadCRCException()
                    } while (byteRead != -1 && isActive)
                    withContext(Dispatchers.IO) {
                        inputStream.close()
                    }
                    drawable
                } else {
                    if (BuildConfig.DEBUG)
                        Log.i(TAG, "Decoding non APNG stream")
                    inputStream.reset()

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        val bytesRead: ByteArray
                        withContext(Dispatchers.IO) {
                            bytesRead = inputStream.readBytes()
                            inputStream.close()
                        }
                        val buf = ByteBuffer.wrap(bytesRead)
                        val source = ImageDecoder.createSource(buf)
                        withContext(Dispatchers.IO) {
                            ImageDecoder.decodeDrawable(source)
                        }
                    } else {
                        val drawable = Drawable.createFromStream(
                            inputStream,
                            null
                        )
                        withContext(Dispatchers.IO) {
                            inputStream.close()
                        }
                        drawable!!
                    }
                }
            }
        }

    suspend fun getDecoded(context: Context): Result<Drawable> {
        if (result == null) {
            result = decodeApng(context)

            kotlin.runCatching {
                withContext(Dispatchers.IO) {
                    inputStream?.close()
                }
            }.onFailure {
                this.result = Result.failure(it)
            }

            inputStream = null
        }

        return result ?: Result.failure(NullPointerException("result is null"))
    }

    /**
     * Decode Apng and return a Drawable who can be an [ApngDrawable] if it end successfully. Can also be an [android.graphics.drawable.AnimatedImageDrawable].
     * @param context Context needed for animation drawable.
     * @param file File to decode.
     * @param config Decoder configuration
     * @return [ApngDrawable] if successful and an [AnimatedImageDrawable] if the image decoded is not an APNG but a gif. If it is not an animated image, it is a [Drawable].
     */
    // TODO DOC
    constructor(file: File, config: Config = Config()) : this(FileInputStream(file), config)

    /**
     * Decode Apng and return a Drawable who can be an [ApngDrawable] if it end successfully. Can also be an [android.graphics.drawable.AnimatedImageDrawable].
     * @param context Context is needed for contentResolver and animation drawable.
     * @param uri Uri to open.
     * @param config Decoder configuration
     * @return [ApngDrawable] if successful and an [AnimatedImageDrawable] if the image decoded is not an APNG but a gif.
     */
    // TODO DOC + better
    constructor(
        context: Context,
        uri: Uri,
        config: Config = Config()
    ) : this(context.contentResolver.openInputStream(uri)!!, config)

    /**
     * Decode Apng and return a Drawable who can be an [ApngDrawable] if it end successfully. Can also be an [android.graphics.drawable.AnimatedImageDrawable].
     * @param context Context is needed for contentResolver and animation drawable.
     * @param res Resource to decode.
     * @param config Decoder configuration
     * @return [ApngDrawable] if successful and an [AnimatedImageDrawable] if the image decoded is not an APNG but a gif.
     */
    // TODO DOC
    constructor(
        context: Context,
        @RawRes res: Int,
        config: Config = Config()
    ) : this(context.resources.openRawResource(res), config)


    /**
     * Generate a correct IHDR from the IHDR chunk of the APNG.
     * @param ihdrOfApng The IHDR of the APNG.
     * @param width The width of the frame.
     * @param height The height of the frame.
     * @return [ByteArray] The generated IHDR.
     */
    private fun generateIhdr(ihdrOfApng: ByteArray, width: Int, height: Int): ByteArray {
        val ihdr =
            ByteArray(0xD + 4 + 4 + 4) // 0xD (IHDR body length) + 4 (0x0, 0x0, 0x0, 0xD : the chunk length) + 4 : IHDR + 4 : CRC

        // Add chunk body length
        System.arraycopy(Utils.uIntToByteArray(0xD), 0, ihdr, 0, 4)

        // We need a body var to know body length and generate crc
        val ihdrBody = ByteArray(0xD + 4) // 0xD (IHDR body length) + 4 : IHDR

        // Add IHDR
        System.arraycopy(Utils.IHDR, 0, ihdrBody, 0, 4)

        // Add the max width and height
        System.arraycopy(Utils.uIntToByteArray(width), 0, ihdrBody, 4, 4)
        System.arraycopy(Utils.uIntToByteArray(height), 0, ihdrBody, 8, 4)

        // Add complicated stuff like depth color ...
        // If you want correct png you need same parameters.
        System.arraycopy(ihdrOfApng, 8, ihdrBody, 12, 5)

        // Generate CRC
        val crC32 = CRC32()
        crC32.update(ihdrBody, 0, 0xD + 4)

        System.arraycopy(ihdrBody, 0, ihdr, 4, 0xD + 4)
        System.arraycopy(Utils.uIntToByteArray(crC32.value.toInt()), 0, ihdr, 0xD + 4 + 4, 4)
        return ihdr
    }

    companion object {
        private const val TAG = "ApngDecoder"
        private val zeroLength = byteArrayOf(0x00, 0x00, 0x00, 0x00)

        // Paint used to clear the buffer
        private val clearPaint: Paint by lazy {
            Paint().apply {
                xfermode = PorterDuffXfermode(
                    PorterDuff.Mode.CLEAR
                )
            }
        }


        /**
         * Decode Apng and return a Drawable who can be an [ApngDrawable] if it end successfully. Can also be an [android.graphics.drawable.AnimatedImageDrawable].
         * @param context Context is needed for contentResolver and animation drawable.
         * @param url URL to decode.
         * @param config Decoder configuration
         * @return [ApngDrawable] if successful and an [AnimatedImageDrawable] if the image decoded is not an APNG but a gif.
         */
        @Suppress("unused")
        @JvmStatic
        suspend fun constructFromUrl(
            url: URL,
            config: Config = Config()
        ): Result<ApngDecoder> =
            kotlin.runCatching {
                withContext(Dispatchers.IO) {
                    ApngDecoder(
                        ByteArrayInputStream(Loader.load(url)),
                        config
                    )
                }
            }
    }
}