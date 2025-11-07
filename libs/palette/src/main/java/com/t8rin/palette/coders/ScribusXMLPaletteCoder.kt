package com.t8rin.palette.coders

import com.t8rin.palette.ColorByteFormat
import com.t8rin.palette.ColorSpace
import com.t8rin.palette.CommonError
import com.t8rin.palette.PALColor
import com.t8rin.palette.PALPalette
import com.t8rin.palette.hexString
import org.xml.sax.Attributes
import org.xml.sax.helpers.DefaultHandler
import java.io.InputStream
import java.io.OutputStream
import javax.xml.parsers.SAXParserFactory

/**
 * Scribus XML palette coder
 */
class ScribusXMLPaletteCoder : PaletteCoder {

    private class ScribusXMLHandler : DefaultHandler() {
        val palette = PALPalette()
        private var currentChars = StringBuilder()

        override fun startElement(
            uri: String?,
            localName: String,
            qName: String?,
            attributes: Attributes
        ) {
            currentChars.clear()
            val elementName = (qName ?: localName).lowercase()

            if (elementName == "scribuscolors" || elementName == "scolors") {
                val name = attributes.getValue("Name") ?: attributes.getValue("name") ?: ""
                palette.name = name.xmlDecoded()
            } else if (elementName == "color") {
                val name =
                    (attributes.getValue("NAME") ?: attributes.getValue("name") ?: "").xmlDecoded()
                val rgbHex = attributes.getValue("RGB") ?: attributes.getValue("rgb")
                val cmykHex = attributes.getValue("CMYK") ?: attributes.getValue("cmyk")

                try {
                    val color = if (rgbHex != null) {
                        PALColor(rgbHexString = rgbHex, format = ColorByteFormat.RGB, name = name)
                    } else if (cmykHex != null) {
                        // CMYK hex format - parse it
                        PALColor(cmykHexString = cmykHex, name = name)
                    } else {
                        null
                    }

                    if (color != null) {
                        palette.colors.add(color)
                    }
                } catch (e: Exception) {
                    // Skip invalid colors
                }
            }
        }

        override fun characters(ch: CharArray, start: Int, length: Int) {
            currentChars.append(ch, start, length)
        }
    }

    override fun decode(input: InputStream): PALPalette {
        val handler = ScribusXMLHandler()
        val factory = SAXParserFactory.newInstance()
        factory.isNamespaceAware = false
        val parser = factory.newSAXParser()
        parser.parse(input, handler)

        if (handler.palette.colors.isEmpty()) {
            throw CommonError.InvalidFormat()
        }

        return handler.palette
    }

    override fun encode(palette: PALPalette, output: OutputStream) {
        var xml = "<?xml version=\"1.0\"?>\n"
        xml += "<SCRIBUSCOLORS"
        if (palette.name.isNotEmpty()) {
            xml += " Name=\"${palette.name.xmlEscaped()}\""
        }
        xml += " >\n"

        palette.allColors().forEach { color ->
            try {
                if (color.colorSpace == ColorSpace.CMYK) {
                    val cmyk = color.toCmyk()
                    // CMYK hex representation (simplified)
                    val c = (cmyk.c * 255).toInt().coerceIn(0, 255)
                    val m = (cmyk.m * 255).toInt().coerceIn(0, 255)
                    val y = (cmyk.y * 255).toInt().coerceIn(0, 255)
                    val k = (cmyk.k * 255).toInt().coerceIn(0, 255)
                    val hex = String.format("#%02x%02x%02x%02x", c, m, y, k)
                    xml += "<COLOR CMYK=\"$hex\""
                } else {
                    val rgb = color.toRgb()
                    val hex = rgb.hexString(ColorByteFormat.RGB, hashmark = true, uppercase = false)
                    xml += "<COLOR RGB=\"$hex\""
                }

                if (color.name.isNotEmpty()) {
                    xml += " NAME=\"${color.name.xmlEscaped()}\""
                }
                xml += " />\n"
            } catch (e: Exception) {
                // Skip colors that can't be converted
            }
        }

        xml += "</SCRIBUSCOLORS>\n"

        output.write(xml.toByteArray(java.nio.charset.StandardCharsets.UTF_8))
    }
}

private fun String.xmlEscaped(): String {
    return this.replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
        .replace("'", "&apos;")
}

private fun String.xmlDecoded(): String {
    return this.replace("&amp;", "&")
        .replace("&lt;", "<")
        .replace("&gt;", ">")
        .replace("&quot;", "\"")
        .replace("&apos;", "'")
}

