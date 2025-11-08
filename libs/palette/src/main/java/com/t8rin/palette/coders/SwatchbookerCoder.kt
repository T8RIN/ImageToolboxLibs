package com.t8rin.palette.coders

import com.t8rin.palette.ColorSpace
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

class SwatchbookerCoder : PaletteCoder {

    private class SwatchbookerXMLHandler : DefaultHandler() {
        var palette = PALPalette()
        private val nodeStack = mutableListOf<Node>()
        private var colorTitle: String? = null
        private var colorID: String? = null
        private var colorMode: String? = null
        private var colorComponents = mutableListOf<Double>()
        val colorMaterialMap = mutableMapOf<String, String>()
        val colorMaterialOrdering = mutableListOf<String>()
        private var currentChars = StringBuilder()

        private data class Node(
            val name: String,
            val attrs: Map<String, String>,
            var content: String = ""
        )

        private fun attributesToMap(attributes: Attributes): Map<String, String> {
            val map = mutableMapOf<String, String>()
            for (i in 0 until attributes.length) {
                val key = attributes.getQName(i).ifEmpty { attributes.getLocalName(i) }
                map[key] = attributes.getValue(i)
            }
            return map
        }

        override fun startElement(
            uri: String?,
            localName: String,
            qName: String?,
            attributes: Attributes
        ) {
            currentChars.clear()
            val elementName = (qName ?: localName).trim()
            if (elementName.isEmpty()) return
            val attrs = attributesToMap(attributes)
            nodeStack.add(Node(elementName, attrs))
            when {
                elementName.equals(
                    "color",
                    ignoreCase = true
                ) && nodeStack.any { it.name.equals("materials", ignoreCase = true) } -> {
                    colorTitle = null
                    colorID = null
                    colorMode = null
                    colorComponents.clear()
                }

                elementName.equals("values", ignoreCase = true) -> {
                    colorMode = attrs["model"] ?: attrs["MODEL"] ?: attrs["Model"]
                    colorComponents.clear()
                }

                elementName.equals(
                    "swatch",
                    ignoreCase = true
                ) && nodeStack.any { it.name.equals("book", ignoreCase = true) } -> {
                    attrs["material"]?.let { colorMaterialOrdering.add(it.trim()) }
                }
            }
        }

        override fun endElement(uri: String?, localName: String, qName: String?) {
            val elementName = (qName ?: localName).trim()
            if (elementName.isEmpty()) {
                if (nodeStack.isNotEmpty()) nodeStack.removeAt(nodeStack.size - 1)
                return
            }
            nodeStack.lastOrNull() ?: run {
                if (nodeStack.isNotEmpty()) nodeStack.removeAt(nodeStack.size - 1)
                return
            }
            val content = currentChars.toString().trim()
            when {
                elementName.equals("dc:title", ignoreCase = true) -> {
                    if (nodeStack.any { it.name.equals("materials", ignoreCase = true) }) {
                        colorTitle = content
                    } else {
                        palette.name = content
                    }
                }

                elementName.equals("dc:identifier", ignoreCase = true) -> colorID = content
                elementName.equals("values", ignoreCase = true) -> {
                    colorComponents =
                        content.split(Regex("\\s+")).mapNotNull { it.toDoubleOrNull() }
                            .toMutableList()
                }

                elementName.equals(
                    "color",
                    ignoreCase = true
                ) && nodeStack.any { it.name.equals("materials", ignoreCase = true) } -> {
                    val name = colorTitle ?: colorID ?: "Color ${palette.colors.size + 1}"
                    val color: PALColor? = when {
                        colorMode.equals("RGB", true) && colorComponents.size >= 3 -> {
                            val r = colorComponents[0]
                            val g = colorComponents[1]
                            val b = colorComponents[2]
                            val a = if (colorComponents.size >= 4) colorComponents[3] else 1.0
                            PALColor.rgb(r, g, b, a, name)
                        }

                        colorMode.equals("Lab", true) && colorComponents.size >= 3 -> PALColor.lab(
                            colorComponents[0],
                            colorComponents[1],
                            colorComponents[2],
                            1.0,
                            name
                        )

                        colorMode.equals(
                            "CMYK",
                            true
                        ) && colorComponents.size >= 4 -> PALColor.cmyk(
                            colorComponents[0],
                            colorComponents[1],
                            colorComponents[2],
                            colorComponents[3],
                            1.0,
                            name
                        )

                        colorMode.equals(
                            "GRAY",
                            true
                        ) && colorComponents.isNotEmpty() -> PALColor.gray(
                            colorComponents[0],
                            1.0,
                            name
                        )

                        colorMode.equals("HSV", true) && colorComponents.size >= 3 -> PALColor.hsb(
                            colorComponents[0],
                            colorComponents[1],
                            colorComponents[2],
                            1.0,
                            name
                        )

                        colorMode.equals("HSL", true) && colorComponents.size >= 3 -> PALColor.hsl(
                            colorComponents[0],
                            colorComponents[1],
                            colorComponents[2],
                            1.0,
                            name
                        )
                        else -> null
                    }
                    if (color != null) {
                        val id = colorID ?: "color_${palette.colors.size}"
                        palette.colors.add(color)
                        colorMaterialMap[id] = color.id
                    }
                }
            }
            if (nodeStack.isNotEmpty()) nodeStack.removeAt(nodeStack.size - 1)
            currentChars.clear()
        }

        override fun characters(ch: CharArray, start: Int, length: Int) {
            currentChars.append(ch, start, length)
        }
    }

    override fun decode(input: InputStream): PALPalette {
        val data = input.readBytes()
        val zipInputStream = ZipInputStream(data.inputStream())
        var xmlData = ByteArray(0)
        var entry: ZipEntry? = zipInputStream.nextEntry
        while (entry != null) {
            if (entry.name.equals("swatchbook.xml", true)) {
                xmlData = zipInputStream.readBytes()
                break
            }
            entry = zipInputStream.nextEntry
        }
        zipInputStream.close()
        if (xmlData.isEmpty()) return PALPalette()
        val handler = SwatchbookerXMLHandler()
        val factory = SAXParserFactory.newInstance()
        factory.isNamespaceAware = true
        factory.newSAXParser().parse(xmlData.inputStream(), handler)
        val ordered = mutableListOf<PALColor>()
        val remaining = handler.palette.colors.toMutableList()
        handler.colorMaterialOrdering.forEach { materialID ->
            val colorID = handler.colorMaterialMap[materialID]
            if (colorID != null) {
                val idx = remaining.indexOfFirst { it.id == colorID }
                if (idx >= 0) ordered.add(remaining.removeAt(idx))
            }
        }
        ordered.addAll(remaining)
        handler.palette.colors = ordered
        return handler.palette
    }

    override fun encode(palette: PALPalette, output: OutputStream) {
        val zipOutputStream = ZipOutputStream(output)
        zipOutputStream.putNextEntry(ZipEntry("swatchbook.xml"))
        val xml = buildString {
            appendLine("""<?xml version="1.0" encoding="UTF-8"?>""")
            appendLine("""<SwatchBook xmlns="http://www.selapa.net/swatchbook/1.0/" xmlns:dc="http://purl.org/dc/elements/1.1/">""")
            appendLine("  <metadata>")
            appendLine("    <dc:title>${escapeXml(palette.name)}</dc:title>")
            appendLine("  </metadata>")
            appendLine("  <materials>")
            palette.allColors().forEachIndexed { index, color ->
                appendLine("    <color usage=\"standard\">")
                appendLine("      <metadata>")
                appendLine("        <dc:title>${escapeXml(color.name)}</dc:title>")
                appendLine("        <dc:identifier>color_$index</dc:identifier>")
                appendLine("      </metadata>")
                appendLine("      <values model=\"${getColorModel(color)}\">")
                appendLine("        ${getColorValues(color)}")
                appendLine("      </values>")
                appendLine("    </color>")
            }
            appendLine("  </materials>")
            appendLine("  <book>")
            palette.allColors()
                .forEachIndexed { index, _ -> appendLine("    <swatch material=\"color_$index\"/>") }
            appendLine("  </book>")
            appendLine("</SwatchBook>")
        }
        zipOutputStream.write(xml.toByteArray(Charsets.UTF_8))
        zipOutputStream.closeEntry()
        zipOutputStream.close()
    }

    private fun escapeXml(text: String): String =
        text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;")
            .replace("'", "&apos;")

    private fun getColorModel(color: PALColor): String = when (color.colorSpace) {
        ColorSpace.RGB -> "RGB"
        ColorSpace.CMYK -> "CMYK"
        ColorSpace.LAB -> "Lab"
        ColorSpace.Gray -> "GRAY"
    }

    private fun getColorValues(color: PALColor): String {
        val comps = color.colorComponents.toMutableList()
        if (color.colorSpace == ColorSpace.RGB) comps.add(color.alpha)
        return comps.joinToString(" ")
    }
}
