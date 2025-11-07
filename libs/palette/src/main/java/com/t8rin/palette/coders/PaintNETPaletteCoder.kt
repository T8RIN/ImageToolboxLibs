package com.t8rin.palette.coders

import com.t8rin.palette.ColorByteFormat
import com.t8rin.palette.ColorSpace
import com.t8rin.palette.CommonError
import com.t8rin.palette.PALColor
import com.t8rin.palette.PALPalette
import com.t8rin.palette.hexString
import java.io.InputStream
import java.io.OutputStream

/**
 * Paint.NET palette coder
 */
class PaintNETPaletteCoder : PaletteCoder {
    override fun decode(input: InputStream): PALPalette {
        val allData = input.readBytes()

        // Check for UTF-8 BOM
        val data = if (allData.size >= 3 &&
            allData[0].toInt() == 0xEF &&
            allData[1].toInt() == 0xBB &&
            allData[2].toInt() == 0xBF
        ) {
            allData.drop(3).toByteArray()
        } else {
            allData
        }

        val content = String(data, java.nio.charset.StandardCharsets.UTF_8)
        val result = PALPalette()

        for (line in content.lines()) {
            val trimmed = line.trim()
            if (trimmed.isEmpty() || trimmed.startsWith(";")) {
                continue
            }

            if (trimmed.length != 8) {
                throw CommonError.InvalidFormat()
            }

            // Parse AARRGGBB
            val a = trimmed.substring(0, 2).toIntOrNull(16) ?: continue
            val r = trimmed.substring(2, 4).toIntOrNull(16) ?: continue
            val g = trimmed.substring(4, 6).toIntOrNull(16) ?: continue
            val b = trimmed.substring(6, 8).toIntOrNull(16) ?: continue

            val color = PALColor.rgb(
                r = r / 255.0,
                g = g / 255.0,
                b = b / 255.0,
                a = a / 255.0
            )
            result.colors.add(color)
        }

        return result
    }

    override fun encode(palette: PALPalette, output: OutputStream) {
        val rgbColors = palette.allColors().map { color ->
            if (color.colorSpace == ColorSpace.RGB) color else color.converted(ColorSpace.RGB)
        }

        var content = """; paint.net Palette File
; Lines that start with a semicolon are comments
; Colors are written as 8-digit hexadecimal numbers: aarrggbb

"""
        rgbColors.forEach { color ->
            color.toRgb()
            val hex = color.hexString(ColorByteFormat.ARGB, hashmark = false, uppercase = true)
            content += "$hex\r\n"
        }

        output.write(content.toByteArray(java.nio.charset.StandardCharsets.UTF_8))
    }
}


