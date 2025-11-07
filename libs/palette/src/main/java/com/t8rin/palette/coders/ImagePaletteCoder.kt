package com.t8rin.palette.coders

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
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
        val uniqueColors = mutableSetOf<ColorPixel>()

        // Read first row of pixels
        val width = bitmap.width
        bitmap.height

        for (x in 0 until width) {
            val pixel = bitmap.getPixel(x, 0)
            val a = AndroidColor.alpha(pixel) / 255.0
            val r = AndroidColor.red(pixel) / 255.0
            val g = AndroidColor.green(pixel) / 255.0
            val b = AndroidColor.blue(pixel) / 255.0

            val colorPixel = ColorPixel(r, g, b, a)

            // Check if we already have a similar color
            val existing = uniqueColors.find { existing ->
                kotlin.math.abs(existing.r - colorPixel.r) <= accuracy &&
                        kotlin.math.abs(existing.g - colorPixel.g) <= accuracy &&
                        kotlin.math.abs(existing.b - colorPixel.b) <= accuracy &&
                        kotlin.math.abs(existing.a - colorPixel.a) <= accuracy
            }

            if (existing == null) {
                uniqueColors.add(colorPixel)
            }
        }

        // Convert to PALColor
        uniqueColors.forEach { pixel ->
            val color = PALColor.rgb(
                r = pixel.r,
                g = pixel.g,
                b = pixel.b,
                a = pixel.a
            )
            result.colors.add(color)
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

        val bitmap = Bitmap.createBitmap(bitmapWidth, bitmapHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        colors.forEachIndexed { index, color ->
            val rgb = color.toRgb()
            val androidColor = AndroidColor.argb(
                (rgb.af * 255).toInt().coerceIn(0, 255),
                (rgb.rf * 255).toInt().coerceIn(0, 255),
                (rgb.gf * 255).toInt().coerceIn(0, 255),
                (rgb.bf * 255).toInt().coerceIn(0, 255)
            )

            canvas.drawColor(androidColor)

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

        bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)
    }

    private data class ColorPixel(
        val r: Double,
        val g: Double,
        val b: Double,
        val a: Double
    )
}

