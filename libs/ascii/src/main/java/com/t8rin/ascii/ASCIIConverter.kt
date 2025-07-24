@file:Suppress("unused")

package com.t8rin.ascii

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import androidx.core.graphics.createBitmap
import androidx.core.graphics.get
import androidx.core.graphics.scale
import com.t8rin.ascii.models.PixelGrid
import com.t8rin.ascii.utilities.ASCIIMapper
import com.t8rin.ascii.utilities.GradientMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.security.InvalidParameterException
import kotlin.math.min

/**
 * Convert Bitmap into its ASCII equivalent bitmap or String.
 * <p>
 * API endpoints are defined as methods on an interface with annotations providing metadata about
 * the form in which the HTTP call should be made.
 * <p>
 */
class ASCIIConverter {

    /* Grid columns count calculated based on the font size */
    private var columns: Float = 0.0f

    /* Font size of the ASCII text in pixels. */
    private var fontSize: Float = 72.0f

    /* Background color of the bitmap. Default is transparent */
    private var backgroundColor: Int = Color.TRANSPARENT

    /* Luminance of the text */
    private var reversedLuminance: Boolean = false

    /* Enable to disable gray scale of the bitmap */
    private var grayScale: Boolean = false

    /* mapper class to get ASCII-Luminance dictionary map */
    private var mapper: ASCIIMapper = ASCIIMapper()

    /**
     *
     * @param mapper  Customized ASCIIConverter Class
     */
    constructor(mapper: ASCIIMapper) {
        this.mapper = mapper
    }

    /**
     * @param fontSize font size of the ascii text in bitmap
     */
    constructor(fontSize: Float = 72f) {
        this.fontSize = fontSize
    }

    /**
     * @param mapper   Customized ASCIIConverter Class
     * @param fontSize font size of the ascii text in bitmap
     */
    constructor(mapper: ASCIIMapper, fontSize: Int) {
        this.fontSize = fontSize.toFloat()
        this.mapper = mapper
    }

    /**
     * @param map Accepts [GradientMap] of character and its luminance
     */
    constructor(map: GradientMap) {
        this.mapper = ASCIIMapper(map)
        initDefaults()
    }

    private fun initDefaults() {
        setFontSize(18.0f)
        setReversedLuminance(false)
        setGrayScale(false)
        columns = 0.0f
    }

    /**
     * @param grayScale Enable to disable gray scale of the bitmap. Default is false to create colored ASCII Bitmap
     */
    fun setGrayScale(grayScale: Boolean) = apply {
        this.grayScale = grayScale
    }

    /**
     * @param reversedLuminance Reverses the luminance by subtracting from 1. Default is false
     */
    fun setReversedLuminance(reversedLuminance: Boolean) = apply {
        this.reversedLuminance = reversedLuminance
    }

    /**
     * @param fontSize Set the font size in sp
     */
    fun setFontSize(fontSize: Float) = apply {
        this.fontSize = fontSize
    }

    /**
     * @param backgroundColor Adds a background color to ASCII Bitmap. Default is transparent
     */
    fun setBackgroundColor(backgroundColor: Int) = apply {
        this.backgroundColor = backgroundColor
    }


    /**
     * Creates an ASCII String of a bitmap
     * @param originalBitmap bitmap to create ASCII Bitmap
     * @return returns ASCII String
     */
    suspend fun createASCIIString(
        originalBitmap: Bitmap,
        separator: String = " "
    ): String {
        columns = getGridWidth(originalBitmap.width)
        if (columns < 5) {
            throw InvalidParameterException("Columns count is very small. Font size needs to be reduced")
        }

        return createASCIIStringFromBitmap(originalBitmap, separator)
    }

    private suspend fun createASCIIStringFromBitmap(
        bitmap: Bitmap,
        separator: String = " "
    ): String = withContext(Dispatchers.Default) {
        val scaledImage = bitmap.resize(columns.toInt())

        val grid = getGridData(scaledImage)

        buildString {
            grid.blocks.forEach { row ->
                append(
                    row.joinToString(separator) { color ->
                        color?.luma(reversedLuminance)?.let(mapper::mapToAscii).orEmpty()
                    }
                )
                appendLine()
            }
        }
    }

    /**
     * Creates an ASCII String of a bitmap
     * @param originalBitmap bitmap to create ASCII Bitmap
     * @return returns ASCII Bitmap
     */
    suspend fun createASCIIImage(originalBitmap: Bitmap): Bitmap {
        columns = getGridWidth(originalBitmap.width)

        if (columns < 5) {
            throw InvalidParameterException("Columns count is very small. Font size needs to be reduced")
        }

        return createASCIIBitmap(originalBitmap)
    }

    private suspend fun createASCIIBitmap(
        originalBitmap: Bitmap
    ): Bitmap = withContext(Dispatchers.Default) {
        val scaledImage = originalBitmap.resize(columns.toInt())
        val grid = getGridData(scaledImage)

        val newBitmap = createBitmap(originalBitmap.width, originalBitmap.height)
        val canvas = Canvas(newBitmap)

        if (backgroundColor != Color.TRANSPARENT) {
            canvas.drawColor(backgroundColor)
        }

        val paint = Paint().apply {
            color = Color.WHITE
            textSize = fontSize
        }

        canvas.drawBitmap(newBitmap, 0.0f, 0.0f, paint)

        grid.blocks.forEachIndexed { row, blocks ->
            blocks.forEachIndexed { col, color ->
                color?.let {
                    val luminance = color.luma(reversedLuminance)
                    val ascii = mapper.mapToAscii(luminance)

                    if (grayScale) {
                        paint.alpha = (luminance * 255.0f).toInt()
                        paint.color = Color.GRAY
                    } else {
                        paint.color = color
                    }

                    canvas.drawText(ascii, row * fontSize, col * fontSize, paint)
                }
            }
        }

        newBitmap
    }


    /**
     * @param width width of the image
     * @return returns the columns count
     */
    private fun getGridWidth(width: Int): Float {
        return if (columns == 0.0f) {
            width / fontSize
        } else {
            columns
        }
    }

    /**
     * @param bitmap Bitmap for fetching rgba data
     * @return returns grid containing rgba data of bitmap
     */
    private fun getGridData(bitmap: Bitmap): PixelGrid {
        val width = bitmap.width
        val height = bitmap.height

        val grid = PixelGrid(width, height, bitmap.width * bitmap.height)
        for (row in 0 until grid.width) {
            for (col in 0 until grid.height) {
                val pixel = bitmap[row, col]

                grid.add(pixel, row, col)
            }
        }

        return grid
    }

    private fun Bitmap.resize(scaleFactor: Int): Bitmap {
        val bitmap = copy(Bitmap.Config.ARGB_8888, false)
        var outWidth: Int
        var outHeight: Int
        var scaleFactor = scaleFactor

        if (scaleFactor <= 1) return bitmap

        if (scaleFactor > min(bitmap.height, bitmap.width))
            scaleFactor = min(bitmap.height, bitmap.width)

        val ratio = scaleFactor.toFloat() / bitmap.width.toFloat()

        outWidth = scaleFactor
        outHeight = (ratio * bitmap.height).toInt()

        return bitmap.scale(outWidth, outHeight)
    }

    private fun Int.luma(reversed: Boolean): Float {
        val luminance = (0.2126 * (Color.red(this) / 255f))
            .plus(0.7152 * (Color.green(this) / 255f))
            .plus(0.0722 * (Color.blue(this) / 255f))

        return (if (reversed) 1.0 - luminance else luminance).toFloat()
    }
}