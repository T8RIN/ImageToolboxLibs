package com.t8rin.palette.coders

import com.t8rin.palette.ColorByteFormat
import com.t8rin.palette.ColorSpace
import com.t8rin.palette.Palette
import com.t8rin.palette.PaletteCoder
import com.t8rin.palette.PaletteCoderException
import com.t8rin.palette.PaletteColor
import com.t8rin.palette.utils.hexString
import java.io.InputStream
import java.io.OutputStream

/**
 * Paint.NET palette coder
 */
class PaintNETPaletteCoder : PaletteCoder {
    override fun decode(input: InputStream): Palette {
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
        val result = Palette.Builder()

        var currentName = ""
        for (line in content.lines()) {
            val trimmed = line.trim()
            if (trimmed.isEmpty()) {
                continue
            }

            if (trimmed.startsWith(";")) {
                // Comment - might be a color name
                val commentText = trimmed.substring(1).trim()
                if (commentText.startsWith("Palette Name", ignoreCase = true) ||
                    commentText.startsWith("Palette:", ignoreCase = true)
                ) {
                    val name = commentText.substringAfter(":").trim()
                    if (name.isNotBlank()) {
                        result.name = name
                    }
                    continue
                }
                if (commentText.isNotEmpty() && !commentText.contains("paint.net") && !commentText.contains(
                        "Colors are written"
                    ) && !commentText.contains(
                        "Downloaded"
                    ) && !commentText.contains(
                        "Description"
                    ) && !commentText.contains(
                        "Colors"
                    )
                ) {
                    currentName = commentText
                }
                continue
            }

            if (trimmed.length != 8) {
                throw PaletteCoderException.InvalidFormat()
            }

            // Parse AARRGGBB
            val a = trimmed.take(2).toIntOrNull(16) ?: continue
            val r = trimmed.substring(2, 4).toIntOrNull(16) ?: continue
            val g = trimmed.substring(4, 6).toIntOrNull(16) ?: continue
            val b = trimmed.substring(6, 8).toIntOrNull(16) ?: continue

            val color = PaletteColor.rgb(
                r = r / 255.0,
                g = g / 255.0,
                b = b / 255.0,
                a = a / 255.0,
                name = currentName
            )
            result.colors.add(color)
            currentName = ""
        }

        return result.build()
    }

    override fun encode(palette: Palette, output: OutputStream) {
        val rgbColors = palette.allColors().map { color ->
            if (color.colorSpace == ColorSpace.RGB) color else color.converted(ColorSpace.RGB)
        }

        var content = """; paint.net Palette File
; Lines that start with a semicolon are comments
; Colors are written as 8-digit hexadecimal numbers: aarrggbb
"""
        if (palette.name.isNotEmpty()) {
            content += "; Palette Name: ${palette.name}\r\n"
        }
        rgbColors.forEach { color ->
            color.toRgb()
            if (color.name.isNotEmpty()) {
                content += "; ${color.name}\r\n"
            }
            val hex = color.hexString(ColorByteFormat.ARGB, hashmark = false, uppercase = true)
            content += "$hex\r\n"
        }

        output.write(content.toByteArray(java.nio.charset.StandardCharsets.UTF_8))
    }
}


