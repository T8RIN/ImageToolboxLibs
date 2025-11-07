package com.t8rin.palette.coders

import com.t8rin.palette.ColorSpace
import com.t8rin.palette.CommonError
import com.t8rin.palette.PALColor
import com.t8rin.palette.PALPalette
import java.io.InputStream
import java.io.OutputStream

/**
 * VGA 24-bit RGB palette coder (3 bytes per color: RRGGBB)
 */
class VGA24BitPaletteCoder : PaletteCoder {
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

            val color = PALColor.rgb(
                r = r / 255.0,
                g = g / 255.0,
                b = b / 255.0
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
            val r = ((rgb.rf * 255).coerceIn(0.0, 255.0).toInt())
            val g = ((rgb.gf * 255).coerceIn(0.0, 255.0).toInt())
            val b = ((rgb.bf * 255).coerceIn(0.0, 255.0).toInt())
            output.write(byteArrayOf(r.toByte(), g.toByte(), b.toByte()))
        }
    }
}


