package com.t8rin.palette.coders

import com.t8rin.palette.ColorByteFormat
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
 * OpenOffice palette coder
 */
class OpenOfficePaletteCoder : PaletteCoder {

    private class OpenOfficeXMLHandler : DefaultHandler() {
        val palette = PALPalette()
        private var currentChars = StringBuilder()

        override fun startElement(
            uri: String?,
            localName: String,
            qName: String?,
            attributes: Attributes
        ) {
            currentChars.clear()
            if (localName == "draw:color" || qName == "draw:color") {
                val name = attributes.getValue("draw:name")?.xmlDecoded() ?: ""
                val colorString = attributes.getValue("draw:color") ?: ""

                try {
                    val color = PALColor(
                        rgbHexString = colorString,
                        format = ColorByteFormat.RGB,
                        name = name
                    )
                    palette.colors.add(color)
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
        val handler = OpenOfficeXMLHandler()
        val factory = SAXParserFactory.newInstance()
        factory.isNamespaceAware = true
        val parser = factory.newSAXParser()
        parser.parse(input, handler)

        if (handler.palette.totalColorCount == 0) {
            throw CommonError.InvalidFormat()
        }

        return handler.palette
    }

    override fun encode(palette: PALPalette, output: OutputStream) {
        var xml = """<?xml version="1.0" encoding="UTF-8"?>
<office:color-table xmlns:office="http://openoffice.org/2000/office" xmlns:style="http://openoffice.org/2000/style" xmlns:text="http://openoffice.org/2000/text" xmlns:table="http://openoffice.org/2000/table" xmlns:draw="http://openoffice.org/2000/drawing" xmlns:fo="http://www.w3.org/1999/XSL/Format" xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:meta="http://openoffice.org/2000/meta" xmlns:number="http://openoffice.org/2000/datastyle" xmlns:svg="http://www.w3.org/2000/svg" xmlns:chart="http://openoffice.org/2000/chart" xmlns:dr3d="http://openoffice.org/2000/dr3d" xmlns:math="http://www.w3.org/1998/Math/MathML" xmlns:form="http://openoffice.org/2000/form" xmlns:script="http://openoffice.org/2000/script">
"""

        palette.allColors().forEach { color ->
            try {
                val hex = color.hexString(ColorByteFormat.RGB, hashmark = true, uppercase = true)
                xml += "<draw:color draw:name=\"${color.name.xmlEscaped()}\" draw:color=\"$hex\"/>\n"
            } catch (e: Exception) {
                // Skip colors that can't be converted
            }
        }

        xml += "</office:color-table>\n"

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

