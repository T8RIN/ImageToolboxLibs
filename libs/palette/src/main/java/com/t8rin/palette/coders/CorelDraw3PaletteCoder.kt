package com.t8rin.palette.coders

import com.t8rin.palette.ColorSpace
import com.t8rin.palette.CommonError
import com.t8rin.palette.PALColor
import com.t8rin.palette.PALPalette
import com.t8rin.palette.utils.readText
import java.io.InputStream
import java.io.OutputStream

/**
 * Corel Draw V3 Palette coder
 */
class CorelDraw3PaletteCoder : PaletteCoder {
    companion object {
        private val regex =
            Regex("\"(.*)\"[ \\t]*(\\d*\\.?\\d+)[ \\t]*(\\d*\\.?\\d+)[ \\t]*(\\d*\\.?\\d+)[ \\t]*(\\d*\\.?\\d+)[ \\t]*")
    }

    override fun decode(input: InputStream): PALPalette {
        val text = input.readText()
        val result = PALPalette()

        text.lines().forEach { line ->
            val trimmed = line.trim()
            regex.find(trimmed)?.let { match ->
                val name = match.groupValues[1]
                val cyan = match.groupValues[2].toDoubleOrNull() ?: return@let
                val magenta = match.groupValues[3].toDoubleOrNull() ?: return@let
                val yellow = match.groupValues[4].toDoubleOrNull() ?: return@let
                val black = match.groupValues[5].toDoubleOrNull() ?: return@let

                val color = PALColor.cmyk(
                    c = (cyan / 100.0).coerceIn(0.0, 1.0),
                    m = (magenta / 100.0).coerceIn(0.0, 1.0),
                    y = (yellow / 100.0).coerceIn(0.0, 1.0),
                    k = (black / 100.0).coerceIn(0.0, 1.0),
                    name = name
                )
                result.colors.add(color)
            }
        }

        if (result.allColors().isEmpty()) {
            throw CommonError.InvalidFormat()
        }

        return result
    }

    override fun encode(palette: PALPalette, output: OutputStream) {
        var result = ""

        val colors = palette.allColors().map { color ->
            if (color.colorSpace == ColorSpace.CMYK) color else color.converted(ColorSpace.CMYK)
        }

        colors.forEach { color ->
            val cmyk = color.toCmyk()
            val ci = ((cmyk.cf * 100).roundToInt())
            val mi = ((cmyk.mf * 100).roundToInt())
            val yi = ((cmyk.yf * 100).roundToInt())
            val ki = ((cmyk.kf * 100).roundToInt())

            result += "\"${color.name}\"    $ci    $mi    $yi    $ki\r\n"
        }

        output.write(result.toByteArray(java.nio.charset.StandardCharsets.UTF_8))
    }

    private fun Double.roundToInt(): Int {
        return kotlin.math.round(this).toInt()
    }
}


