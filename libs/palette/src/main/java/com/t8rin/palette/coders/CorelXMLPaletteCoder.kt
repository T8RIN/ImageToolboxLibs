package com.t8rin.palette.coders

import com.t8rin.palette.ColorSpace
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
 * CorelDraw XML palette coder
 */
class CorelXMLPaletteCoder : PaletteCoder {

    private class CorelXMLHandler : DefaultHandler() {
        val palette = PALPalette()
        private var currentGroup = PALGroup()
        private var isInColorsSection = false
        private var isInColorspaceSection = false
        private val colorspaces = mutableListOf<Colorspace>()
        private var currentChars = StringBuilder()

        private class Colorspace(val name: String) {
            val colors = mutableListOf<PALColor>()
        }

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

                "colorspaces" -> isInColorspaceSection = true
                "cs" -> {
                    val name = attributes.getValue("name")?.xmlDecoded() ?: ""
                    colorspaces.add(Colorspace(name.lowercase()))
                }

                "colors" -> isInColorsSection = true
                "page" -> {
                    val name = attributes.getValue("name")?.xmlDecoded() ?: ""
                    currentGroup = PALGroup(name = name)
                }

                "color" -> {
                    val cs = attributes.getValue("cs")?.lowercase()
                    val name = attributes.getValue("name")?.xmlDecoded() ?: ""
                    val tints = attributes.getValue("tints") ?: ""
                    val components = tints.split(",").mapNotNull { it.trim().toDoubleOrNull() }

                    val color: PALColor? = when (cs) {
                        "cmyk" -> {
                            if (components.size >= 4) {
                                try {
                                    PALColor.cmyk(
                                        c = components[0],
                                        m = components[1],
                                        y = components[2],
                                        k = components[3],
                                        name = name
                                    )
                                } catch (e: Exception) {
                                    null
                                }
                            } else null
                        }

                        "rgb" -> {
                            if (components.size >= 3) {
                                try {
                                    PALColor.rgb(
                                        r = components[0],
                                        g = components[1],
                                        b = components[2],
                                        name = name
                                    )
                                } catch (e: Exception) {
                                    null
                                }
                            } else null
                        }

                        "lab" -> {
                            if (components.size >= 3) {
                                try {
                                    // Convert from XML ranges to LAB ranges
                                    val l = components[0] * 100.0  // 0 -> 100
                                    val a = components[1] * 256.0 - 128.0  // -128 -> 128
                                    val b = components[2] * 256.0 - 128.0  // -128 -> 128
                                    PALColor.lab(l = l, a = a, b = b, name = name)
                                } catch (e: Exception) {
                                    null
                                }
                            } else null
                        }

                        "gray" -> {
                            if (components.isNotEmpty()) {
                                try {
                                    PALColor.gray(white = components[0], name = name)
                                } catch (e: Exception) {
                                    null
                                }
                            } else null
                        }

                        else -> {
                            if (isInColorsSection && cs != null) {
                                // Try to find in colorspaces
                                colorspaces.find { it.name == cs }?.colors?.firstOrNull()
                                    ?.let { existingColor ->
                                        try {
                                            PALColor(
                                                name = name,
                                                colorSpace = existingColor.colorSpace,
                                                colorComponents = existingColor.colorComponents,
                                                alpha = existingColor.alpha,
                                                colorType = existingColor.colorType
                                            )
                                        } catch (e: Exception) {
                                            null
                                        }
                                    }
                            } else null
                        }
                    }

                    if (color != null) {
                        if (isInColorspaceSection) {
                            colorspaces.lastOrNull()?.colors?.add(color)
                        } else {
                            currentGroup.colors.add(color)
                        }
                    }
                }
            }
        }

        override fun endElement(uri: String?, localName: String, qName: String?) {
            when (localName.lowercase()) {
                "page" -> {
                    // If page name is empty or this is the first page with colors, add to main palette
                    if (currentGroup.colors.isNotEmpty()) {
                        if (currentGroup.name.isEmpty() && palette.colors.isEmpty() && palette.groups.isEmpty()) {
                            // First page with empty name goes to main palette
                            palette.colors.addAll(currentGroup.colors)
                        } else if (currentGroup.name.isNotEmpty()) {
                            // Named page goes to groups
                            palette.groups.add(currentGroup)
                        } else {
                            // Empty name but not first - add to main palette anyway
                            palette.colors.addAll(currentGroup.colors)
                        }
                    }
                    currentGroup = PALGroup()
                }

                "colors" -> isInColorsSection = false
                "colorspaces" -> isInColorspaceSection = false
            }
            currentChars.clear()
        }

        override fun characters(ch: CharArray, start: Int, length: Int) {
            currentChars.append(ch, start, length)
        }
    }

    override fun decode(input: InputStream): PALPalette {
        val handler = CorelXMLHandler()
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
        var xml = "<?xml version=\"1.0\"?>\n"
        xml += "<palette guid=\"${java.util.UUID.randomUUID()}\""
        if (palette.name.isNotEmpty()) {
            xml += " name=\"${palette.name.xmlEscaped()}\""
        }
        xml += ">\n"
        xml += "<colors>\n"

        // Global colors
        if (palette.colors.isNotEmpty()) {
            xml += pageData(name = "", colors = palette.colors)
        }

        // Group colors
        palette.groups.forEach { group ->
            xml += pageData(name = group.name, colors = group.colors)
        }

        xml += "</colors>\n"
        xml += "</palette>\n"

        output.write(xml.toByteArray(java.nio.charset.StandardCharsets.UTF_8))
    }

    private fun pageData(name: String, colors: List<PALColor>): String {
        var result = "<page>"
        colors.forEach { color ->
            result += "<color"
            if (color.name.isNotEmpty()) {
                result += " name=\"${color.name.xmlEscaped()}\""
            }

            val (cs, tints) = when (color.colorSpace) {
                ColorSpace.CMYK -> {
                    "CMYK" to color.colorComponents.joinToString(",") { "%.6f".format(it) }
                }

                ColorSpace.RGB -> {
                    "RGB" to color.colorComponents.joinToString(",") { "%.6f".format(it) }
                }

                ColorSpace.Gray -> {
                    "GRAY" to color.colorComponents.joinToString(",") { "%.6f".format(it) }
                }

                ColorSpace.LAB -> {
                    // Map from LAB to XML specification
                    if (color.colorComponents.size < 3) {
                        "LAB" to ""
                    } else {
                        "LAB" to listOf(
                            color.colorComponents[0] / 100.0,  // 0…100 -> 0.0…1.0
                            (color.colorComponents[1] + 128.0) / 256.0,  // -128…128 -> 0.0…1.0
                            (color.colorComponents[2] + 128.0) / 256.0   // -128…128 -> 0.0…1.0
                        ).joinToString(",") { "%.6f".format(it) }
                    }
                }
            }

            result += " cs=\"$cs\""
            if (tints.isNotEmpty()) {
                result += " tints=\"$tints\""
            }
            result += "/>\n"
        }
        result += "</page>\n"
        return result
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

