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
        val allData = input.readBytes()

        // Check if there's a text header with names
        var data = allData
        val names = mutableListOf<String>()
        try {
            val text = String(allData, java.nio.charset.StandardCharsets.UTF_8)
            if (text.startsWith("; Names: ")) {
                val firstNewline = text.indexOf('\n')
                if (firstNewline > 0) {
                    val nameLine = text.substring(0, firstNewline)
                    val namesStr = nameLine.substring("; Names: ".length)
                    names.addAll(namesStr.split(", ").map { it.trim() })
                    data = allData.sliceArray(firstNewline + 1 until allData.size)
                }
            }
        } catch (e: Exception) {
            // Not a text header, use binary data as-is
            data = allData
        }

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

            val colorIndex = i / 3
            val colorName = if (colorIndex < names.size) names[colorIndex] else ""
            val color = PALColor.rgb(
                r = r / 63.0,
                g = g / 63.0,
                b = b / 63.0,
                name = colorName
            )
            result.colors.add(color)
        }

        return result
    }

    override fun encode(palette: PALPalette, output: OutputStream) {
        val colors = palette.allColors().map { color ->
            if (color.colorSpace == ColorSpace.RGB) color else color.converted(ColorSpace.RGB)
        }

        // VGA format doesn't support names, but we can write them as a comment header
        val names = colors.mapNotNull { if (it.name.isNotEmpty()) it.name else null }
        if (names.isNotEmpty()) {
            val nameHeader = "; Names: ${names.joinToString(", ")}\n"
            output.write(nameHeader.toByteArray(java.nio.charset.StandardCharsets.UTF_8))
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


