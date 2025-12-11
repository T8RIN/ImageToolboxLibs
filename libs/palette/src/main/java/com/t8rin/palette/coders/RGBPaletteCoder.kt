package com.t8rin.palette.coders

import com.t8rin.palette.ColorByteFormat
import com.t8rin.palette.ColorSpace
import com.t8rin.palette.Palette
import com.t8rin.palette.PaletteCoder
import com.t8rin.palette.PaletteCoderException
import com.t8rin.palette.PaletteColor
import com.t8rin.palette.utils.hexString
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

    override fun decode(input: InputStream): Palette {
        val text = input.readText()
        val lines = text.lines()
        val result = Palette.Builder()

        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.isEmpty()) continue

            regex.findAll(trimmed).forEach { match ->
                val hex = match.groupValues[1]
                val name = match.groupValues[2].trim()

                try {
                    val color = PaletteColor(hex, ColorByteFormat.RGB, name)
                    result.colors.add(color)
                } catch (_: Throwable) {
                    // Skip invalid color
                }
            }
        }

        val palette = result.build()

        if (palette.allColors().isEmpty()) {
            throw PaletteCoderException.InvalidFormat()
        }

        return palette
    }

    override fun encode(palette: Palette, output: OutputStream) {
        val flattenedColors = palette.allColors().map {
            if (it.colorSpace == ColorSpace.RGB) it else it.converted(ColorSpace.RGB)
        }
        var result = ""

        flattenedColors.forEach { color ->
            if (result.isNotEmpty()) {
                result += "\r\n"
            }
            result += color.hexString(
                format = ColorByteFormat.RGB,
                hashmark = true,
                uppercase = false
            )
            if (color.name.isNotEmpty()) {
                result += " ${color.name}"
            }
        }

        output.write(result.toByteArray(java.nio.charset.StandardCharsets.UTF_8))
    }
}

