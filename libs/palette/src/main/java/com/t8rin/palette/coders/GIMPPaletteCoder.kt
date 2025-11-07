package com.t8rin.palette.coders

import com.t8rin.palette.ColorSpace
import com.t8rin.palette.CommonError
import com.t8rin.palette.PALColor
import com.t8rin.palette.PALPalette
import com.t8rin.palette.utils.readText
import java.io.InputStream
import java.io.OutputStream

/**
 * GIMP Palette (GPL) coder
 */
class GIMPPaletteCoder : PaletteCoder {
    companion object {
        private val nameRegex = Regex("^Name:\\s*(.*)$", RegexOption.IGNORE_CASE)
        private val colorRegex = Regex("^\\s*(\\d+)\\s+(\\d+)\\s+(\\d+)(.*)$")
    }

    override fun decode(input: InputStream): PALPalette {
        val text = input.readText()
        val lines = text.lines()

        if (lines.isEmpty() || !lines[0].contains("GIMP Palette", ignoreCase = true)) {
            throw CommonError.InvalidFormat()
        }

        val result = PALPalette()

        for (line in lines.drop(1)) {
            val trimmed = line.trim()
            if (trimmed.isEmpty()) continue

            // Check for name
            nameRegex.find(trimmed)?.let { match ->
                result.name = match.groupValues[1].trim()
                continue
            }

            // Check for color
            colorRegex.find(trimmed)?.let { match ->
                val r = match.groupValues[1].toIntOrNull() ?: continue
                val g = match.groupValues[2].toIntOrNull() ?: continue
                val b = match.groupValues[3].toIntOrNull() ?: continue
                val name = match.groupValues[4].trim()

                val color = PALColor.rgb(
                    r = (r / 255.0).coerceIn(0.0, 1.0),
                    g = (g / 255.0).coerceIn(0.0, 1.0),
                    b = (b / 255.0).coerceIn(0.0, 1.0),
                    name = name
                )
                result.colors.add(color)
            }
        }

        return result
    }

    override fun encode(palette: PALPalette, output: OutputStream) {
        val flattenedColors = palette.allColors().map { color ->
            val rgb =
                if (color.colorSpace == ColorSpace.RGB) color else color.converted(ColorSpace.RGB)
            rgb.toRgb()
        }
        var result = "GIMP Palette\n"

        if (palette.name.isNotEmpty()) {
            result += "Name: ${palette.name}\n"
        }

        result += "#Colors: ${flattenedColors.size}\n"

        // We need to preserve original color names, so we'll use the original colors
        val originalColors = palette.allColors()
        flattenedColors.forEachIndexed { index, rgb ->
            val r = ((rgb.rf * 255).coerceIn(0.0, 255.0).toInt())
            val g = ((rgb.gf * 255).coerceIn(0.0, 255.0).toInt())
            val b = ((rgb.bf * 255).coerceIn(0.0, 255.0).toInt())

            result += "$r\t$g\t$b"
            if (index < originalColors.size && originalColors[index].name.isNotEmpty()) {
                result += "\t${originalColors[index].name}"
            }
            result += "\n"
        }

        output.write(result.toByteArray(java.nio.charset.StandardCharsets.UTF_8))
    }
}

