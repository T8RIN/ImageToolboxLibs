package com.t8rin.palette.coders

import com.t8rin.palette.ColorSpace
import com.t8rin.palette.CommonError
import com.t8rin.palette.Palette
import com.t8rin.palette.PaletteCoder
import com.t8rin.palette.PaletteColor
import com.t8rin.palette.utils.readText
import java.io.InputStream
import java.io.OutputStream

/**
 * KOffice Palette coder
 */
class KOfficePaletteCoder : PaletteCoder {
    companion object {
        private val colorRegex = Regex("^\\s*(\\d+)\\s+(\\d+)\\s+(\\d+)(.*)$")
    }

    override fun decode(input: InputStream): Palette {
        val text = input.readText()
        val lines = text.lines()

        if (lines.isEmpty() || !lines[0].contains("KDE RGB Palette")) {
            throw CommonError.InvalidFormat()
        }

        val result = Palette.Builder()

        for (line in lines.drop(1)) {
            colorRegex.find(line)?.let { match ->
                val r = match.groupValues[1].toIntOrNull() ?: return@let
                val g = match.groupValues[2].toIntOrNull() ?: return@let
                val b = match.groupValues[3].toIntOrNull() ?: return@let
                val name = match.groupValues[4].trim()

                val color = PaletteColor.rgb(
                    r = (r / 255.0).coerceIn(0.0, 1.0),
                    g = (g / 255.0).coerceIn(0.0, 1.0),
                    b = (b / 255.0).coerceIn(0.0, 1.0),
                    name = name
                )
                result.colors.add(color)
            }
        }

        return result.build()
    }

    override fun encode(palette: Palette, output: OutputStream) {
        var result = "KDE RGB Palette\n"

        val colors = palette.allColors().map { color ->
            if (color.colorSpace == ColorSpace.RGB) color else color.converted(ColorSpace.RGB)
        }

        colors.forEach { color ->
            val rgb = color.toRgb()
            val r = ((rgb.rf * 255).coerceIn(0.0, 255.0).toInt())
            val g = ((rgb.gf * 255).coerceIn(0.0, 255.0).toInt())
            val b = ((rgb.bf * 255).coerceIn(0.0, 255.0).toInt())
            result += "$r\t$g\t$b"
            if (color.name.isNotEmpty()) {
                result += "\t${color.name}"
            }
            result += "\n"
        }

        output.write(result.toByteArray(java.nio.charset.StandardCharsets.UTF_8))
    }
}


