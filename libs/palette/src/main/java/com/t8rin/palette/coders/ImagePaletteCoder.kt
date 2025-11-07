package com.t8rin.palette.coders

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import androidx.core.graphics.createBitmap
import androidx.core.graphics.get
import com.t8rin.palette.CommonError
import com.t8rin.palette.PALColor
import com.t8rin.palette.PALPalette
import java.io.InputStream
import java.io.OutputStream
import android.graphics.Color as AndroidColor

/**
 * Image-based palette coder
 * Note: This requires Android Bitmap API
 */
class ImagePaletteCoder(
    private val accuracy: Double = 0.001
) : PaletteCoder {

    override fun decode(input: InputStream): PALPalette {
        val data = input.readBytes()
        val bitmap = BitmapFactory.decodeByteArray(data, 0, data.size)
            ?: throw CommonError.InvalidFormat()

        val result = PALPalette()
        val colorOrder = mutableListOf<ColorPixel>()

        // Read first row of pixels
        val width = bitmap.width
        bitmap.height

        for (x in 0 until width) {
            val pixel = bitmap[x, 0]
            val a = AndroidColor.alpha(pixel) / 255.0
            val r = AndroidColor.red(pixel) / 255.0
            val g = AndroidColor.green(pixel) / 255.0
            val b = AndroidColor.blue(pixel) / 255.0

            val colorPixel = ColorPixel(r, g, b, a)
            colorOrder.add(colorPixel)
        }

        // Try to read color names from PNG text chunk or extension
        val colorNames = mutableListOf<String>()
        try {
            // Check if there's a JSON extension after PNG data
            val pngEndMarker = byteArrayOf(
                0x49, 0x45, 0x4E, 0x44, 0xAE.toByte(), 0x42, 0x60,
                0x82.toByte()
            ) // IEND chunk
            val pngEndIndex = data.indexOfSlice(pngEndMarker)
            if (pngEndIndex >= 0 && pngEndIndex + 8 < data.size) {
                val extensionData = data.sliceArray(pngEndIndex + 8 until data.size)
                val extensionText = String(extensionData, java.nio.charset.StandardCharsets.UTF_8)
                if (extensionText.startsWith("\n; IMAGE_NAMES: ")) {
                    val namesLine = extensionText.lines().find { it.startsWith("; IMAGE_NAMES:") }
                    if (namesLine != null) {
                        val namesStr = namesLine.substring("; IMAGE_NAMES: ".length).trim()
                        colorNames.addAll(namesStr.split("|"))
                    }
                }
            }
        } catch (e: Exception) {
            // No names extension
        }

        // Convert to PALColor, preserving order and names
        colorOrder.forEachIndexed { index, pixel ->
            val colorName = if (index < colorNames.size) colorNames[index] else ""
            val color = PALColor.rgb(
                r = pixel.r,
                g = pixel.g,
                b = pixel.b,
                a = pixel.a,
                name = colorName
            )
            if (result.colors.none { it.toArgb() == color.toArgb() }) {
                result.colors.add(color)
            }
        }

        return result
    }

    override fun encode(palette: PALPalette, output: OutputStream) {
        val colors = palette.allColors()
        if (colors.isEmpty()) {
            throw CommonError.TooFewColors()
        }

        val swatchWidth = 32
        val swatchHeight = 32
        val bitmapWidth = colors.size * swatchWidth
        val bitmapHeight = swatchHeight

        val bitmap = createBitmap(bitmapWidth, bitmapHeight)
        val canvas = Canvas(bitmap)

        colors.forEachIndexed { index, color ->
            val rgb = color.toRgb()
            val androidColor = AndroidColor.argb(
                (rgb.af * 255).toInt().coerceIn(0, 255),
                (rgb.rf * 255).toInt().coerceIn(0, 255),
                (rgb.gf * 255).toInt().coerceIn(0, 255),
                (rgb.bf * 255).toInt().coerceIn(0, 255)
            )

            val x = index * swatchWidth
            canvas.drawRect(
                x.toFloat(), 0f,
                (x + swatchWidth).toFloat(), swatchHeight.toFloat(),
                android.graphics.Paint().apply {
                    this.color = androidColor
                    style = android.graphics.Paint.Style.FILL
                }
            )
        }

        val pngOutputStream = java.io.ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, pngOutputStream)
        val pngData = pngOutputStream.toByteArray()
        output.write(pngData)

        // Append color names as extension (non-standard but preserves names)
        val names = colors.mapNotNull { it.name.ifEmpty { null } }
        if (names.isNotEmpty()) {
            val nameText = "\n; IMAGE_NAMES: ${names.joinToString("|")}\n"
            output.write(nameText.toByteArray(java.nio.charset.StandardCharsets.UTF_8))
        }
    }

    private data class ColorPixel(
        val r: Double,
        val g: Double,
        val b: Double,
        val a: Double
    )
}

private fun ByteArray.indexOfSlice(slice: ByteArray, startIndex: Int = 0): Int {
    if (slice.isEmpty() || this.isEmpty() || slice.size > this.size) return -1
    outer@ for (i in startIndex..this.size - slice.size) {
        for (j in slice.indices) {
            if (this[i + j] != slice[j]) continue@outer
        }
        return i
    }
    return -1
}