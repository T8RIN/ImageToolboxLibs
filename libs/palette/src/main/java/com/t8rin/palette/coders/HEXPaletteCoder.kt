package com.t8rin.palette.coders

import com.t8rin.palette.ColorByteFormat
import com.t8rin.palette.ColorSpace
import com.t8rin.palette.CommonError
import com.t8rin.palette.PALColor
import com.t8rin.palette.PALPalette
import com.t8rin.palette.hexString
import com.t8rin.palette.utils.readText
import java.io.InputStream
import java.io.OutputStream

/**
 * Hex RGBA palette coder
 */
class HEXPaletteCoder : PaletteCoder {
    companion object {
        private val validHexChars = "#0123456789abcdefABCDEF"
    }

    override fun decode(input: InputStream): PALPalette {
        val text = input.readText()
        val lines = text.lines()
        val result = PALPalette()

        var currentName = ""
        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.isEmpty()) continue

            if (trimmed.firstOrNull() == ';') {
                // Comment - might be a color name
                val commentText = trimmed.substring(1).trim()
                if (commentText.isNotEmpty()) {
                    currentName = commentText
                }
                continue
            }

            var current = ""
            for (char in line) {
                if (validHexChars.contains(char)) {
                    current += char
                } else {
                    if (current.isNotEmpty()) {
                        try {
                            val color = PALColor(current, ColorByteFormat.RGBA, name = currentName)
                            result.colors.add(color)
                            currentName = ""
                        } catch (e: Exception) {
                            // Skip invalid hex
                        }
                        current = ""
                    }
                }
            }

            if (current.isNotEmpty()) {
                try {
                    val color = PALColor(current, ColorByteFormat.RGBA, name = currentName)
                    result.colors.add(color)
                    currentName = ""
                } catch (e: Exception) {
                    // Skip invalid hex
                }
            }
        }

        if (result.colors.isEmpty()) {
            throw CommonError.InvalidFormat()
        }

        return result
    }

    override fun encode(palette: PALPalette, output: OutputStream) {
        val rgbColors = palette.allColors().map {
            if (it.colorSpace == ColorSpace.RGB) it else it.converted(ColorSpace.RGB)
        }
        var content = ""

        rgbColors.forEach { color ->
            val rgb = color.toRgb()
            val format = if (rgb.af < 1.0) ColorByteFormat.RGBA else ColorByteFormat.RGB
            val hex = color.hexString(format, hashmark = true, uppercase = false)
            if (color.name.isNotEmpty()) {
                content += "; ${color.name}\n"
            }
            content += "$hex\n"
        }

        output.write(content.toByteArray(java.nio.charset.StandardCharsets.UTF_8))
    }
}

