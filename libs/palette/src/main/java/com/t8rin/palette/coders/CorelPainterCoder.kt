package com.t8rin.palette.coders

import com.t8rin.palette.ColorSpace
import com.t8rin.palette.Palette
import com.t8rin.palette.PaletteCoder
import com.t8rin.palette.PaletteCoderException
import com.t8rin.palette.PaletteColor
import com.t8rin.palette.utils.readText
import java.io.InputStream
import java.io.OutputStream

/**
 * Corel Painter Swatch coder
 */
class CorelPainterCoder : PaletteCoder {
    companion object {
        private val regex =
            Regex("[ \\t]*R[ \\t]*:[ \\t]*(\\d*\\.?\\d+)[ \\t,]*G[ \\t]*:[ \\t]*(\\d*\\.?\\d+)[ \\t,]*B[ \\t]*:[ \\t]*(\\d*\\.?\\d+)[ \\t,]*(?:HV[ \\t]*:[ \\t]*(\\d*\\.?\\d+)[ \\t,]*)?(?:SV[ \\t]*:[ \\t]*(\\d*\\.?\\d+)[ \\t,]*)?(?:VV[ \\t]*:[ \\t]*(\\d*\\.?\\d+)[ \\t,]*)?(.*)")
    }

    override fun decode(input: InputStream): Palette {
        val text = input.readText()

        if (!text.startsWith("ROWS ")) {
            throw PaletteCoderException.InvalidFormat()
        }

        val lines = text.lines()
        if (lines.size < 7) {
            throw PaletteCoderException.InvalidFormat()
        }

        if (!lines[0].startsWith("ROWS") ||
            !lines[1].startsWith("COLS") ||
            !lines[2].startsWith("WIDTH") ||
            !lines[3].startsWith("HEIGHT") ||
            !lines[4].startsWith("TEXTHEIGHT") ||
            !lines[5].startsWith("SPACING")
        ) {
            throw PaletteCoderException.InvalidFormat()
        }

        val result = Palette.Builder()

        for (line in lines.drop(6)) {
            val trimmed = line.trim()
            regex.find(trimmed)?.let { match ->
                val r = match.groupValues[1].toIntOrNull() ?: return@let
                val g = match.groupValues[2].toIntOrNull() ?: return@let
                val b = match.groupValues[3].toIntOrNull() ?: return@let
                val name = match.groupValues[7].trim()

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
        var result = """ROWS 12
COLS 22
WIDTH 16
HEIGHT 16
TEXTHEIGHT 0
SPACING 1

"""

        val colors = palette.allColors().map { color ->
            if (color.colorSpace == ColorSpace.RGB) color else color.converted(ColorSpace.RGB)
        }

        colors.forEach { color ->
            val rgb = color.toRgb()
            result += "R: ${rgb.r255}, G:${rgb.g255}, B:${rgb.b255}  HV:0.00, SV:0.00, VV:0.00"
            if (color.name.isNotEmpty()) {
                result += "  ${color.name}"
            }
            result += "\n"
        }

        output.write(result.toByteArray(java.nio.charset.StandardCharsets.UTF_8))
    }
}


