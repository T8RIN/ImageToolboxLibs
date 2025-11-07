package com.t8rin.palette.coders

import com.t8rin.palette.CommonError
import com.t8rin.palette.PALColor
import com.t8rin.palette.PALGroup
import com.t8rin.palette.PALPalette
import org.xml.sax.Attributes
import org.xml.sax.helpers.DefaultHandler
import java.io.InputStream
import java.io.OutputStream
import javax.xml.parsers.SAXParserFactory

/**
 * Autodesk Color Book coder
 */
class AutodeskColorBookCoder : PaletteCoder {

    private class AutodeskXMLHandler : DefaultHandler() {
        val palette = PALPalette()
        private var currentGroup: PALGroup? = null
        private var colorName: String? = null
        private var r: Int? = null
        private var g: Int? = null
        private var b: Int? = null
        private var xmlStack = mutableListOf<String>()
        private var currentChars = StringBuilder()

        override fun startElement(
            uri: String?,
            localName: String,
            qName: String?,
            attributes: Attributes
        ) {
            currentChars.clear()
            val elementName = localName.trim()

            when (elementName.lowercase()) {
                "colorpage" -> {
                    if (xmlStack.lastOrNull()?.lowercase() != "colorbook") {
                        // Invalid structure
                        return
                    }
                    currentGroup = PALGroup()
                }

                "colorentry", "pagecolor" -> {
                    if (xmlStack.lastOrNull()?.lowercase() != "colorpage") {
                        return
                    }
                }

                "colorname" -> {
                    if (xmlStack.lastOrNull()?.lowercase() != "colorentry") {
                        return
                    }
                }

                "red", "green", "blue" -> {
                    if (xmlStack.lastOrNull()?.lowercase() != "rgb8") {
                        return
                    }
                }
            }
            xmlStack.add(elementName)
        }


        override fun characters(ch: CharArray, start: Int, length: Int) {
            currentChars.append(ch, start, length)
        }

        override fun endElement(uri: String?, localName: String, qName: String?) {
            val elementName = localName.trim()
            val content = currentChars.toString().trim()

            when (elementName.lowercase()) {
                "bookname" -> palette.name = content
                "colorname" -> colorName = content
                "red" -> r = content.toIntOrNull()?.coerceIn(0, 255)
                "green" -> g = content.toIntOrNull()?.coerceIn(0, 255)
                "blue" -> b = content.toIntOrNull()?.coerceIn(0, 255)
                "colorentry" -> {
                    if (r != null && g != null && b != null) {
                        val color = PALColor.rgb(
                            r = r!! / 255.0,
                            g = g!! / 255.0,
                            b = b!! / 255.0,
                            name = colorName ?: ""
                        )
                        currentGroup?.colors?.add(color)
                        r = null
                        g = null
                        b = null
                        colorName = null
                    }
                }

                "colorpage" -> {
                    currentGroup?.let { group ->
                        if (group.colors.isNotEmpty()) {
                            group.name = "Color Page ${palette.groups.size + 1}"
                            palette.groups.add(group)
                        }
                    }
                    currentGroup = null
                }
            }

            if (xmlStack.isNotEmpty()) {
                xmlStack.removeAt(xmlStack.size - 1)
            }
            currentChars.clear()
        }
    }

    override fun decode(input: InputStream): PALPalette {
        val handler = AutodeskXMLHandler()
        val factory = SAXParserFactory.newInstance()
        factory.isNamespaceAware = false
        val parser = factory.newSAXParser()
        parser.parse(input, handler)

        if (handler.palette.totalColorCount == 0) {
            throw CommonError.InvalidFormat()
        }

        return handler.palette
    }

    override fun encode(palette: PALPalette, output: OutputStream) {
        var xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
        xml += "<colorBook>\n"

        val name = if (palette.name.isNotEmpty()) palette.name else "Untitled"
        xml += "   <bookName>${name.xmlEscaped()}</bookName>\n"

        xml += "   <majorVersion>1</majorVersion>\n"
        xml += "   <minorVersion>0</minorVersion>\n"

        val allGroups = palette.allGroups
        allGroups.forEach { group ->
            if (group.colors.size <= 1) {
                return@forEach
            }

            // Autodesk color book can only handle a maximum of 10 color entries
            val entries = group.colors.take(10)

            xml += "   <colorPage>\n"

            // Assume that the first color is the page color
            val c = entries[0]
            xml += "      <pageColor>\n"
            xml += encodeColor(c)
            xml += "      </pageColor>\n"

            entries.forEach { color ->
                xml += "      <colorEntry>\n"
                val colorName =
                    if (color.name.isNotEmpty()) color.name else java.util.UUID.randomUUID()
                        .toString()
                xml += "         <colorName>${colorName.xmlEscaped()}</colorName>\n"
                xml += encodeColor(color)
                xml += "      </colorEntry>\n"
            }

            xml += "   </colorPage>\n"
        }

        xml += "</colorBook>\n"

        output.write(xml.toByteArray(java.nio.charset.StandardCharsets.UTF_8))
    }

    private fun encodeColor(color: PALColor): String {
        val rgb = color.toRgb()
        return """         <RGB8>
            <red>${rgb.r255}</red>
            <green>${rgb.g255}</green>
            <blue>${rgb.b255}</blue>
         </RGB8>
"""
    }
}

private fun String.xmlEscaped(): String {
    return this.replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
        .replace("'", "&apos;")
}

