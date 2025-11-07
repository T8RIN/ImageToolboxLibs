package com.t8rin.palette.coders

import com.t8rin.palette.ColorSpace
import com.t8rin.palette.CommonError
import com.t8rin.palette.PALColor
import com.t8rin.palette.PALPalette
import org.xml.sax.Attributes
import org.xml.sax.helpers.DefaultHandler
import java.io.InputStream
import java.io.OutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import javax.xml.parsers.SAXParserFactory

/**
 * Swatchbooker coder (decode only)
 */
class SwatchbookerCoder : PaletteCoder {

    private class SwatchbookerXMLHandler : DefaultHandler() {
        val palette = PALPalette()
        private val nodeStack = mutableListOf<Node>()
        private var colorTitle: String? = null
        private var colorID: String? = null
        private var colorMode: String? = null
        private var colorUsage: String? = null
        private var colorComponents = mutableListOf<Double>()
        val colorMaterialMap = mutableMapOf<String, String>()
        val colorMaterialOrdering = mutableListOf<String>()
        private var currentChars = StringBuilder()

        private data class Node(
            val name: String,
            val attrs: Map<String, String>,
            var content: String = ""
        )

        private fun path(): String {
            return nodeStack.joinToString(".") { it.name }
        }

        override fun startElement(
            uri: String?,
            localName: String,
            qName: String?,
            attributes: Attributes
        ) {
            currentChars.clear()
            val elementName = localName.trim()

            if (elementName.isEmpty()) return

            val attrs = mutableMapOf<String, String>()
            for (i in 0 until attributes.length) {
                attrs[attributes.getLocalName(i)] = attributes.getValue(i)
            }

            nodeStack.add(Node(elementName, attrs))

            when {
                elementName == "color" && path() == "SwatchBook.materials" -> {
                    colorTitle = null
                    colorID = null
                    colorMode = null
                    colorUsage = attrs["usage"]
                    colorComponents.clear()
                }

                elementName == "values" && path() == "SwatchBook.materials.color" -> {
                    colorMode = attrs["model"]
                    colorComponents.clear()
                }

                elementName == "swatch" && path() == "SwatchBook.book" -> {
                    attrs["material"]?.let { material ->
                        colorMaterialOrdering.add(material.trim())
                    }
                }
            }
        }

        override fun endElement(uri: String?, localName: String, qName: String?) {
            val elementName = localName.trim()
            if (elementName.isEmpty()) {
                if (nodeStack.isNotEmpty()) {
                    nodeStack.removeAt(nodeStack.size - 1)
                }
                return
            }

            val current = nodeStack.lastOrNull()
            if (current == null || current.name != elementName) {
                if (nodeStack.isNotEmpty()) {
                    nodeStack.removeAt(nodeStack.size - 1)
                }
                return
            }

            val content = currentChars.toString().trim()

            when (path()) {
                "SwatchBook.metadata.dc:title" -> {
                    if (current.attrs["xml:lang"] == null) {
                        palette.name = content
                    }
                }

                "SwatchBook.materials.color.metadata.dc:title" -> {
                    colorTitle = content
                }

                "SwatchBook.materials.color.metadata.dc:identifier" -> {
                    colorID = content
                }

                "SwatchBook.materials.color.values" -> {
                    colorComponents = content.split(" ")
                        .mapNotNull { it.trim().toDoubleOrNull() }
                        .toMutableList()
                }
            }

            if (elementName == "color" && path() == "SwatchBook.materials.color") {
                val colorMode = this.colorMode
                if (colorMode != null) {
                    val name = colorTitle ?: colorID ?: ""

                    val color: PALColor? = when {
                        colorMode == "RGB" && colorComponents.size == 3 -> {
                            try {
                                PALColor.rgb(
                                    r = colorComponents[0],
                                    g = colorComponents[1],
                                    b = colorComponents[2],
                                    name = name
                                )
                            } catch (e: Exception) {
                                null
                            }
                        }

                        colorMode == "Lab" && colorComponents.size == 3 -> {
                            try {
                                PALColor.lab(
                                    l = colorComponents[0],
                                    a = colorComponents[1],
                                    b = colorComponents[2],
                                    name = name
                                )
                            } catch (e: Exception) {
                                null
                            }
                        }

                        colorMode == "GRAY" && colorComponents.size == 1 -> {
                            try {
                                PALColor.gray(
                                    white = colorComponents[0],
                                    name = name
                                )
                            } catch (e: Exception) {
                                null
                            }
                        }

                        colorMode == "CMYK" && colorComponents.size == 4 -> {
                            try {
                                PALColor.cmyk(
                                    c = colorComponents[0],
                                    m = colorComponents[1],
                                    y = colorComponents[2],
                                    k = colorComponents[3],
                                    name = name
                                )
                            } catch (e: Exception) {
                                null
                            }
                        }

                        colorMode == "HSV" && colorComponents.size == 3 -> {
                            try {
                                PALColor.hsb(
                                    hf = colorComponents[0],
                                    sf = colorComponents[1],
                                    bf = colorComponents[2],
                                    name = name
                                )
                            } catch (e: Exception) {
                                null
                            }
                        }

                        colorMode == "HSL" && colorComponents.size == 3 -> {
                            try {
                                PALColor.hsl(
                                    hf = colorComponents[0],
                                    sf = colorComponents[1],
                                    lf = colorComponents[2],
                                    name = name
                                )
                            } catch (e: Exception) {
                                null
                            }
                        }

                        else -> null
                    }

                    if (color != null && colorID != null) {
                        palette.colors.add(color)
                        colorMaterialMap[colorID!!] = color.id
                    }
                }
            }

            if (nodeStack.isNotEmpty()) {
                nodeStack.removeAt(nodeStack.size - 1)
            }
            currentChars.clear()
        }

        override fun characters(ch: CharArray, start: Int, length: Int) {
            currentChars.append(ch, start, length)
        }
    }

    override fun decode(input: InputStream): PALPalette {
        val data = input.readBytes()
        val zipInputStream = ZipInputStream(java.io.ByteArrayInputStream(data))

        var xmlData = ByteArray(0)
        var entry: ZipEntry? = zipInputStream.nextEntry
        while (entry != null) {
            if (entry.name == "swatchbook.xml") {
                xmlData = zipInputStream.readBytes()
                break
            }
            entry = zipInputStream.nextEntry
        }
        zipInputStream.close()

        if (xmlData.isEmpty()) {
            throw CommonError.InvalidFormat()
        }

        val handler = SwatchbookerXMLHandler()
        val factory = SAXParserFactory.newInstance()
        factory.isNamespaceAware = true
        val parser = factory.newSAXParser()
        parser.parse(java.io.ByteArrayInputStream(xmlData), handler)

        if (handler.palette.totalColorCount == 0) {
            throw CommonError.TooFewColors()
        }

        // Re-order colors based on material ordering
        val ordered = mutableListOf<PALColor>()
        val remaining = handler.palette.colors.toMutableList()

        handler.colorMaterialOrdering.forEach { materialID ->
            val colorID = handler.colorMaterialMap[materialID]
            if (colorID != null) {
                val index = remaining.indexOfFirst { it.id == colorID }
                if (index >= 0) {
                    ordered.add(remaining.removeAt(index))
                }
            }
        }

        // Add any remaining colors
        ordered.addAll(remaining)
        handler.palette.colors = ordered

        return handler.palette
    }

    override fun encode(palette: PALPalette, output: OutputStream) {
        val zipOutputStream = ZipOutputStream(output)
        val entry = ZipEntry("swatchbook.xml")
        zipOutputStream.putNextEntry(entry)

        // Generate XML
        val xml = buildString {
            appendLine("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")
            appendLine("<SwatchBook xmlns=\"http://www.selapa.net/swatchbook/1.0/\" xmlns:dc=\"http://purl.org/dc/elements/1.1/\">")

            // Metadata
            appendLine("  <metadata>")
            appendLine("    <dc:title>${escapeXml(palette.name.ifEmpty { "Palette" })}</dc:title>")
            appendLine("  </metadata>")

            // Materials (colors)
            appendLine("  <materials>")
            palette.allColors().forEachIndexed { index, color ->
                appendLine("    <color usage=\"standard\">")
                appendLine("      <metadata>")
                appendLine("        <dc:title>${escapeXml(color.name.ifEmpty { "Color ${index + 1}" })}</dc:title>")
                appendLine("        <dc:identifier>color_$index</dc:identifier>")
                appendLine("      </metadata>")
                appendLine("      <values model=\"${getColorModel(color)}\">")
                appendLine("        ${getColorValues(color)}")
                appendLine("      </values>")
                appendLine("    </color>")
            }
            appendLine("  </materials>")

            // Book (color ordering)
            appendLine("  <book>")
            palette.allColors().forEachIndexed { index, _ ->
                appendLine("    <swatch material=\"color_$index\"/>")
            }
            appendLine("  </book>")

            appendLine("</SwatchBook>")
        }

        zipOutputStream.write(xml.toByteArray(java.nio.charset.StandardCharsets.UTF_8))
        zipOutputStream.closeEntry()
        zipOutputStream.close()
    }

    private fun escapeXml(text: String): String {
        return text
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&apos;")
    }

    private fun getColorModel(color: PALColor): String {
        return when (color.colorSpace) {
            ColorSpace.RGB -> "RGB"
            ColorSpace.CMYK -> "CMYK"
            ColorSpace.LAB -> "Lab"
            ColorSpace.Gray -> "GRAY"
            else -> {
                // Try to detect from components
                when (color.colorComponents.size) {
                    3 -> {
                        // Could be RGB, LAB, or HSB/HSL
                        // Default to RGB
                        "RGB"
                    }

                    4 -> "CMYK"
                    1 -> "GRAY"
                    else -> "RGB"
                }
            }
        }
    }

    private fun getColorValues(color: PALColor): String {
        return when (color.colorSpace) {
            ColorSpace.RGB -> {
                val components = color.colorComponents
                "${components[0]} ${components[1]} ${components[2]}"
            }

            ColorSpace.CMYK -> {
                val components = color.colorComponents
                "${components[0]} ${components[1]} ${components[2]} ${components[3]}"
            }

            ColorSpace.LAB -> {
                val components = color.colorComponents
                "${components[0]} ${components[1]} ${components[2]}"
            }

            ColorSpace.Gray -> {
                "${color.colorComponents[0]}"
            }

            else -> {
                // Convert to RGB
                try {
                    val rgb = color.toRgb()
                    "${rgb.rf} ${rgb.gf} ${rgb.bf}"
                } catch (e: Exception) {
                    "0 0 0"
                }
            }
        }
    }
}

