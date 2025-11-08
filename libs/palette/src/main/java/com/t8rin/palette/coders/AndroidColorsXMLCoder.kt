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
 * Android colors.xml palette coder
 */
class AndroidColorsXMLCoder(
    private val includeAlphaDuringExport: Boolean = true
) : PaletteCoder {

    private class AndroidXMLHandler : DefaultHandler() {
        val palette = PALPalette()
        private var currentElement = ""
        private var currentName: String? = null
        private var isInsideResourcesBlock = false
        private var currentChars = StringBuilder()

        override fun startElement(
            uri: String?,
            localName: String,
            qName: String?,
            attributes: Attributes
        ) {
            currentChars.clear()
            when (localName.lowercase()) {
                "resources" -> isInsideResourcesBlock = true
                "color" -> {
                    currentElement = "color"
                    currentName = attributes.getValue("name")?.xmlDecoded()
                }
            }
        }

        override fun endElement(uri: String?, localName: String, qName: String?) {
            when (localName.lowercase()) {
                "resources" -> isInsideResourcesBlock = false
                "color" -> {
                    if (isInsideResourcesBlock && currentElement == "color") {
                        val colorString = currentChars.toString().trim()
                        val colorName = currentName ?: "color_${palette.colors.size}"
                        try {
                            val color = PALColor(
                                rgbHexString = colorString,
                                format = ColorByteFormat.ARGB,
                                name = colorName
                            )
                            palette.colors.add(color)
                        } catch (e: Exception) {
                            // Skip invalid colors
                        }
                    }
                    currentElement = ""
                    currentName = null
                }
            }
            currentChars.clear()
        }

        override fun characters(ch: CharArray, start: Int, length: Int) {
            currentChars.append(ch, start, length)
        }
    }

    override fun decode(input: InputStream): PALPalette {
        val handler = AndroidXMLHandler()
        val factory = SAXParserFactory.newInstance()
        val parser = factory.newSAXParser()
        parser.parse(input, handler)

        if (handler.palette.colors.isEmpty()) {
            throw CommonError.InvalidFormat()
        }

        return handler.palette
    }

    override fun encode(palette: PALPalette, output: OutputStream) {
        var xml = """<?xml version="1.0" encoding="utf-8"?>
<resources>
"""

        palette.allColors().forEachIndexed { index, color ->
            var name = if (color.name.isNotEmpty()) color.name else "color_$index"
            name = name.replace(" ", "_").xmlEscaped()

            val format = if (includeAlphaDuringExport) ColorByteFormat.ARGB else ColorByteFormat.RGB
            val hex = color.hexString(format, hashmark = true, uppercase = true)

            xml += "   <color name=\"$name\">$hex</color>\n"
        }

        xml += "</resources>\n"

        output.write(xml.toByteArray(java.nio.charset.StandardCharsets.UTF_8))
    }
}