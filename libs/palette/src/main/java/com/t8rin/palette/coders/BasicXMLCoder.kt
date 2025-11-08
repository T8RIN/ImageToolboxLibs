package com.t8rin.palette.coders

import com.t8rin.palette.ColorByteFormat
import com.t8rin.palette.CommonError
import com.t8rin.palette.Palette
import com.t8rin.palette.PaletteCoder
import com.t8rin.palette.PaletteColor
import com.t8rin.palette.utils.hexString
import com.t8rin.palette.utils.xmlDecoded
import com.t8rin.palette.utils.xmlEscaped
import org.xml.sax.Attributes
import org.xml.sax.helpers.DefaultHandler
import java.io.InputStream
import java.io.OutputStream
import javax.xml.parsers.SAXParserFactory

/**
 * Basic XML palette coder
 */
class BasicXMLCoder : PaletteCoder {

    private class BasicXMLHandler : DefaultHandler() {
        val palette = Palette.Builder()
        private var currentChars = StringBuilder()

        override fun startElement(
            uri: String?,
            localName: String,
            qName: String?,
            attributes: Attributes
        ) {
            currentChars.clear()
            when (localName.lowercase()) {
                "palette" -> {
                    val name = attributes.getValue("name")?.xmlDecoded()
                    if (name != null) {
                        palette.name = name
                    }
                }

                "color" -> {
                    val name = attributes.getValue("name")?.xmlDecoded() ?: ""
                    val hex = attributes.getValue("hex")
                    val r = attributes.getValue("r")?.toIntOrNull()
                    val g = attributes.getValue("g")?.toIntOrNull()
                    val b = attributes.getValue("b")?.toIntOrNull()
                    val a = attributes.getValue("a")?.toIntOrNull() ?: 255

                    try {
                        val color = if (hex != null) {
                            PaletteColor(
                                rgbHexString = hex,
                                format = ColorByteFormat.RGBA,
                                name = name
                            )
                        } else if (r != null && g != null && b != null) {
                            PaletteColor.rgb(
                                r = r / 255.0,
                                g = g / 255.0,
                                b = b / 255.0,
                                a = a / 255.0,
                                name = name
                            )
                        } else {
                            null
                        }

                        if (color != null) {
                            palette.colors.add(color)
                        }
                    } catch (_: Throwable) {
                        // Skip invalid colors
                    }
                }
            }
        }

        override fun characters(ch: CharArray, start: Int, length: Int) {
            currentChars.append(ch, start, length)
        }
    }

    override fun decode(input: InputStream): Palette {
        val handler = BasicXMLHandler()
        val factory = SAXParserFactory.newInstance()
        val parser = factory.newSAXParser()
        parser.parse(input, handler)

        val palette = handler.palette.build()

        if (palette.totalColorCount == 0) {
            throw CommonError.InvalidFormat()
        }

        return palette
    }

    override fun encode(palette: Palette, output: OutputStream) {
        var xml = "<?xml version=\"1.0\"?>\n"
        xml += "<palette"
        if (palette.name.isNotEmpty()) {
            xml += " name=\"${palette.name.xmlEscaped()}\""
        }
        xml += ">\n"

        palette.allColors().forEach { color ->
            val rgb = color.toRgb()
            val hex = rgb.hexString(ColorByteFormat.RGBA, hashmark = false, uppercase = false)

            xml += "<color"
            if (color.name.isNotEmpty()) {
                xml += " name=\"${color.name.xmlEscaped()}\""
            }
            xml += " hex=\"$hex\""
            xml += " r=\"${rgb.r255}\" g=\"${rgb.g255}\" b=\"${rgb.b255}\" a=\"${rgb.a255}\""
            xml += " />\n"
        }

        xml += "</palette>\n"

        output.write(xml.toByteArray(java.nio.charset.StandardCharsets.UTF_8))
    }
}