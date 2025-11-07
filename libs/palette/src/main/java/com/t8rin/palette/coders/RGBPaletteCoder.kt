package com.t8rin.palette.coders

import com.t8rin.palette.ColorByteFormat
import com.t8rin.palette.ColorSpace
import com.t8rin.palette.CommonError
import com.t8rin.palette.PALColor
import com.t8rin.palette.PALPalette
import com.t8rin.palette.hexRGB
import com.t8rin.palette.utils.readText
import java.io.InputStream
import java.io.OutputStream

/**
 * RGB text palette coder
 */
class RGBPaletteCoder : PaletteCoder {
    companion object {
        private val regex = Regex("^#?\\s*([a-f0-9]{3,8})\\s*(.*)\\s*", RegexOption.IGNORE_CASE)
    }

    override fun decode(input: InputStream): PALPalette {
        val text = input.readText()
        val lines = text.lines()
        val result = PALPalette()

        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.isEmpty()) continue

            regex.findAll(trimmed).forEach { match ->
                val hex = match.groupValues[1]
                val name = match.groupValues[2].trim()

                try {
                    val color = PALColor(hex, ColorByteFormat.RGB, name)
                    result.colors.add(color)
                } catch (e: Exception) {
                    // Skip invalid color
                }
            }
        }

        if (result.allColors().isEmpty()) {
            throw CommonError.InvalidFormat()
        }

        return result
    }

    override fun encode(palette: PALPalette, output: OutputStream) {
        val flattenedColors = palette.allColors().map {
            if (it.colorSpace == ColorSpace.RGB) it else it.converted(ColorSpace.RGB)
        }
        var result = ""

        flattenedColors.forEach { color ->
            if (result.isNotEmpty()) {
                result += "\r\n"
            }
            result += color.hexRGB(hashmark = true)
            if (color.name.isNotEmpty()) {
                result += " ${color.name}"
            }
        }

        output.write(result.toByteArray(java.nio.charset.StandardCharsets.UTF_8))
    }
}

