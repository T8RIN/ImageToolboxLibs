package com.t8rin.palette.coders

import com.t8rin.palette.ColorByteFormat
import com.t8rin.palette.Palette
import com.t8rin.palette.PaletteCoder
import com.t8rin.palette.PaletteCoderException
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
 * Android colors.xml palette coder
 */
class AndroidColorsXMLCoder(
    private val includeAlphaDuringExport: Boolean = true
) : PaletteCoder {

    private class AndroidXMLHandler : DefaultHandler() {
        val palette = Palette.Builder()
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
                            val color = PaletteColor(
                                rgbHexString = colorString,
                                format = ColorByteFormat.ARGB,
                                name = colorName
                            )
                            palette.colors.add(color)
                        } catch (_: Throwable) {
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

    override fun decode(input: InputStream): Palette {
        val handler = AndroidXMLHandler()
        val factory = SAXParserFactory.newInstance()
        val parser = factory.newSAXParser()
        parser.parse(input, handler)

        if (handler.palette.colors.isEmpty()) {
            throw PaletteCoderException.InvalidFormat()
        }

        return handler.palette.build()
    }

    override fun encode(palette: Palette, output: OutputStream) {
        var xml = """<?xml version="1.0" encoding="utf-8"?>
<resources>
"""

        palette.allColors().forEachIndexed { index, color ->
            var name = color.name.ifEmpty { "color_$index" }
            name = name.replace(" ", "_").xmlEscaped()

            val format = if (includeAlphaDuringExport) ColorByteFormat.ARGB else ColorByteFormat.RGB
            val hex = color.hexString(format, hashmark = true, uppercase = true)

            xml += "   <color name=\"$name\">$hex</color>\n"
        }

        xml += "</resources>\n"

        output.write(xml.toByteArray(java.nio.charset.StandardCharsets.UTF_8))
    }
}