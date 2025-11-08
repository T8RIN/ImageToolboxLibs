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
 * Homesite Palette coder
 */
class HPLPaletteCoder : PaletteCoder {
    companion object {
        private val colorRegex = Regex("^\\s*(\\d+)\\s+(\\d+)\\s+(\\d+).*$")
    }

    override fun decode(input: InputStream): Palette {
        val text = input.readText()
        val lines = text.lines()

        if (lines.isEmpty() || !lines[0].contains("Palette") || lines.size < 2 || !lines[1].contains(
                "Version 4.0"
            )
        ) {
            throw CommonError.InvalidFormat()
        }

        val result = Palette.Builder()
        var currentName = ""

        for (line in lines.drop(3)) {
            val trimmed = line.trim()
            if (trimmed.isEmpty()) continue

            if (trimmed.startsWith(";")) {
                // Comment - might be a color name
                val commentText = trimmed.substring(1).trim()
                if (commentText.isNotEmpty()) {
                    currentName = commentText
                }
                continue
            }
            
            colorRegex.find(line)?.let { match ->
                val r = match.groupValues[1].toIntOrNull() ?: return@let
                val g = match.groupValues[2].toIntOrNull() ?: return@let
                val b = match.groupValues[3].toIntOrNull() ?: return@let

                val color = PaletteColor.rgb(
                    r = (r / 255.0).coerceIn(0.0, 1.0),
                    g = (g / 255.0).coerceIn(0.0, 1.0),
                    b = (b / 255.0).coerceIn(0.0, 1.0),
                    name = currentName
                )
                result.colors.add(color)
                currentName = ""
            }
        }

        return result.build()
    }

    override fun encode(palette: Palette, output: OutputStream) {
        var result = "Palette\nVersion 4.0\n-----------\n"

        val colors = palette.allColors().map { color ->
            if (color.colorSpace == ColorSpace.RGB) color else color.converted(ColorSpace.RGB)
        }

        colors.forEach { color ->
            if (color.name.isNotEmpty()) {
                result += "; ${color.name}\n"
            }
            val rgb = color.toRgb()
            val r = ((rgb.rf * 255).coerceIn(0.0, 255.0).toInt())
            val g = ((rgb.gf * 255).coerceIn(0.0, 255.0).toInt())
            val b = ((rgb.bf * 255).coerceIn(0.0, 255.0).toInt())
            result += "$r $g $b\n"
        }

        output.write(result.toByteArray(java.nio.charset.StandardCharsets.UTF_8))
    }
}


