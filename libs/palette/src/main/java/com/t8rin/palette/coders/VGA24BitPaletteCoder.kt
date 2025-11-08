package com.t8rin.palette.coders

import com.t8rin.palette.ColorSpace
import com.t8rin.palette.CommonError
import com.t8rin.palette.Palette
import com.t8rin.palette.PaletteCoder
import com.t8rin.palette.PaletteColor
import java.io.InputStream
import java.io.OutputStream

/**
 * VGA 24-bit RGB palette coder (3 bytes per color: RRGGBB)
 */
class VGA24BitPaletteCoder : PaletteCoder {
    override fun decode(input: InputStream): Palette {
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
        } catch (_: Throwable) {
            // Not a text header, use binary data as-is
            data = allData
        }

        if (data.size % 3 != 0) {
            throw CommonError.InvalidFormat()
        }

        val result = Palette.Builder()

        for (i in data.indices step 3) {
            val r = data[i].toUByte().toInt()
            val g = data[i + 1].toUByte().toInt()
            val b = data[i + 2].toUByte().toInt()

            val colorIndex = i / 3
            val colorName = if (colorIndex < names.size) names[colorIndex] else ""
            val color = PaletteColor.rgb(
                r = r / 255.0,
                g = g / 255.0,
                b = b / 255.0,
                name = colorName
            )
            result.colors.add(color)
        }

        return result.build()
    }

    override fun encode(palette: Palette, output: OutputStream) {
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
            val r = ((rgb.rf * 255).coerceIn(0.0, 255.0).toInt())
            val g = ((rgb.gf * 255).coerceIn(0.0, 255.0).toInt())
            val b = ((rgb.bf * 255).coerceIn(0.0, 255.0).toInt())
            output.write(byteArrayOf(r.toByte(), g.toByte(), b.toByte()))
        }
    }
}


