package com.t8rin.palette.coders

import com.t8rin.palette.ColorSpace
import com.t8rin.palette.CommonError
import com.t8rin.palette.PALColor
import com.t8rin.palette.PALPalette
import com.t8rin.palette.utils.readText
import java.io.InputStream
import java.io.OutputStream
import java.text.DecimalFormat

/**
 * Skencil Palette coder
 */
class SkencilPaletteCoder : PaletteCoder {
    companion object {
        private val colorRegex =
            Regex("^\\s*(\\d*\\.?\\d+|\\d+)\\s+(\\d*\\.?\\d+|\\d+)\\s+(\\d*\\.?\\d+|\\d+)\\s+(.*)$")
        private val formatter = DecimalFormat("0.000000")
    }

    override fun decode(input: InputStream): PALPalette {
        val text = input.readText()
        val lines = text.lines()

        if (lines.isEmpty() || !lines[0].contains("##Sketch RGBPalette 0")) {
            throw CommonError.InvalidFormat()
        }

        val result = PALPalette()

        for (line in lines.drop(1)) {
            colorRegex.find(line)?.let { match ->
                val r = match.groupValues[1].toDoubleOrNull() ?: return@let
                val g = match.groupValues[2].toDoubleOrNull() ?: return@let
                val b = match.groupValues[3].toDoubleOrNull() ?: return@let
                val name = match.groupValues[4].trim()

                val color = PALColor.rgb(
                    r = r.coerceIn(0.0, 1.0),
                    g = g.coerceIn(0.0, 1.0),
                    b = b.coerceIn(0.0, 1.0),
                    name = name
                )
                result.colors.add(color)
            }
        }

        return result
    }

    override fun encode(palette: PALPalette, output: OutputStream) {
        var result = "##Sketch RGBPalette 0\n"

        val colors = palette.allColors().map { color ->
            if (color.colorSpace == ColorSpace.RGB) color else color.converted(ColorSpace.RGB)
        }

        colors.forEach { color ->
            val rgb = color.toRgb()
            val rs = formatter.format(rgb.rf.coerceIn(0.0, 1.0))
            val gs = formatter.format(rgb.gf.coerceIn(0.0, 1.0))
            val bs = formatter.format(rgb.bf.coerceIn(0.0, 1.0))
            result += "$rs $gs $bs"
            if (color.name.isNotEmpty()) {
                result += " ${color.name}"
            }
            result += "\n"
        }

        output.write(result.toByteArray(java.nio.charset.StandardCharsets.UTF_8))
    }
}


