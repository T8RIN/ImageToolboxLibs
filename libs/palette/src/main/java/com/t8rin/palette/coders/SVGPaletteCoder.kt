package com.t8rin.palette.coders

import com.t8rin.palette.CommonError
import com.t8rin.palette.PALColor
import com.t8rin.palette.PALPalette
import com.t8rin.palette.hexRGB
import java.io.InputStream
import java.io.OutputStream
import java.text.DecimalFormat

/**
 * SVG palette coder (encode only)
 */
class SVGPaletteCoder(
    private val swatchSize: Size = Size(width = 40.0, height = 40.0),
    private val maxExportWidth: Double = 600.0,
    private val edgeInset: EdgeInsets = EdgeInsets(top = 4.0, left = 4.0, bottom = 4.0, right = 4.0)
) : PaletteCoder {

    private val formatter = DecimalFormat("#.###").apply {
        decimalFormatSymbols = java.text.DecimalFormatSymbols(java.util.Locale.US)
    }

    override fun decode(input: InputStream): PALPalette {
        throw CommonError.NotImplemented()
    }

    override fun encode(palette: PALPalette, output: OutputStream) {
        var xOffset = edgeInset.left
        var yOffset = edgeInset.top

        fun exportGrouping(colors: List<PALColor>): String {
            var result = ""
            colors.forEach { color ->
                val rgb = color.toRgb()
                val hex = rgb.hexRGB(hashmark = true, uppercase = true)

                result += "      <rect x=\"${formatter.format(xOffset)}\" y=\"${
                    formatter.format(
                        yOffset
                    )
                }\" "
                result += "width=\"${formatter.format(swatchSize.width)}\" height=\"${
                    formatter.format(
                        swatchSize.height
                    )
                }\" "
                result += "fill=\"$hex\" fill-opacity=\"${formatter.format(color.alpha)}\""
                result += " />\n"

                xOffset += swatchSize.width + 1
                if (xOffset + swatchSize.width + edgeInset.right > maxExportWidth) {
                    yOffset += swatchSize.height + 1
                    xOffset = edgeInset.left
                }
            }
            return result
        }

        var colorsXml = ""
        // Global colors first
        colorsXml += exportGrouping(palette.colors)

        palette.groups.forEach { group ->
            xOffset = edgeInset.left
            colorsXml += exportGrouping(group.colors)

            if (group.name.isNotEmpty()) {
                yOffset += swatchSize.height + 10
                colorsXml += "      <text x='5' y='${formatter.format(yOffset)}' font-size='8' alignment-baseline='middle'>${group.name.xmlEscaped()}</text>\n\n"
            }

            yOffset += 10
        }

        yOffset += edgeInset.bottom

        val result = """<?xml version="1.0" encoding="utf-8"?>
	<svg xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" version="1.1" viewBox="0 0 ${
            formatter.format(
                maxExportWidth
            )
        } ${formatter.format(yOffset + swatchSize.height)}" xml:space="preserve">

"""
        output.write(result.toByteArray(java.nio.charset.StandardCharsets.UTF_8))
        output.write(colorsXml.toByteArray(java.nio.charset.StandardCharsets.UTF_8))
        output.write("</svg>\n".toByteArray(java.nio.charset.StandardCharsets.UTF_8))
    }
}

private fun String.xmlEscaped(): String {
    return this.replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
        .replace("'", "&apos;")
}

data class Size(val width: Double, val height: Double)
data class EdgeInsets(val top: Double, val left: Double, val bottom: Double, val right: Double)

