package com.t8rin.palette.coders

import com.t8rin.palette.ColorSpace
import com.t8rin.palette.CommonError
import com.t8rin.palette.PALColor
import com.t8rin.palette.PALPalette
import java.io.InputStream
import java.io.OutputStream

/**
 * VGA 18-bit RGB palette coder (3 bytes per color, 6-bit per channel)
 */
class VGA18BitPaletteCoder : PaletteCoder {
    override fun decode(input: InputStream): PALPalette {
        val data = input.readBytes()

        if (data.size % 3 != 0) {
            throw CommonError.InvalidFormat()
        }

        val result = PALPalette()

        for (i in data.indices step 3) {
            val r = data[i].toUByte().toInt()
            val g = data[i + 1].toUByte().toInt()
            val b = data[i + 2].toUByte().toInt()

            if (r > 63 || g > 63 || b > 63) {
                throw CommonError.InvalidFormat()
            }

            val color = PALColor.rgb(
                r = r / 63.0,
                g = g / 63.0,
                b = b / 63.0
            )
            result.colors.add(color)
        }

        return result
    }

    override fun encode(palette: PALPalette, output: OutputStream) {
        val colors = palette.allColors().map { color ->
            if (color.colorSpace == ColorSpace.RGB) color else color.converted(ColorSpace.RGB)
        }

        colors.forEach { color ->
            val rgb = color.toRgb()
            val r = ((rgb.rf * 63).roundToInt().coerceIn(0, 63))
            val g = ((rgb.gf * 63).roundToInt().coerceIn(0, 63))
            val b = ((rgb.bf * 63).roundToInt().coerceIn(0, 63))
            output.write(byteArrayOf(r.toByte(), g.toByte(), b.toByte()))
        }
    }

    private fun Double.roundToInt(): Int {
        return kotlin.math.round(this).toInt()
    }
}


