package com.t8rin.palette.coders

import com.t8rin.palette.ColorSpace
import com.t8rin.palette.CommonError
import com.t8rin.palette.PALColor
import com.t8rin.palette.PALPalette
import com.t8rin.palette.utils.ByteOrder
import com.t8rin.palette.utils.BytesReader
import com.t8rin.palette.utils.BytesWriter
import java.io.InputStream
import java.io.OutputStream

/**
 * Affinity Designer Palette (AFPalette) coder
 */
class AFPaletteCoder : PaletteCoder {
    override fun decode(input: InputStream): PALPalette {
        val data = input.readBytes()
        val parser = BytesReader(data)
        val result = PALPalette()

        var hasUnsupportedColorType = false

        // Check BOM 0x414BFF00
        val bom = parser.readUInt32(ByteOrder.LITTLE_ENDIAN)
        if (bom.toInt() != 0x414BFF00) {
            throw CommonError.InvalidBOM()
        }

        // Version
        val version = parser.readUInt32(ByteOrder.LITTLE_ENDIAN)
        if (version.toInt() != 11 && version.toInt() != 10) {
            throw CommonError.InvalidBOM()
        }

        // NClP
        parser.readThroughNextInstanceOfPattern(0x4e, 0x43, 0x6c, 0x50)

        // Filename
        val filenameLen = parser.readUInt32(ByteOrder.LITTLE_ENDIAN).toInt()
        val filename = parser.readStringASCII(filenameLen)
        result.name = filename

        // Colors
        val colors = mutableListOf<PALColor>()

        // VlaP
        parser.readThroughNextInstanceOfPattern(0x56, 0x6c, 0x61, 0x50)
        val colorCount = parser.readUInt32(ByteOrder.LITTLE_ENDIAN).toInt()

        for (index in 0 until colorCount) {
            val curPos = parser.readPosition.toInt()

            try {
                // Find the next color "rloC"
                parser.readThroughNextInstanceOfASCII("rloC")

                // Skip 6 bytes
                parser.seek(6)

                // The color type as a string
                val colorType = parser.readStringASCII(4)

                when (colorType) {
                    "ABGR" -> {
                        // RGB color
                        parser.readThroughNextInstanceOfASCII("Dloc_")
                        val r = parser.readFloat32(ByteOrder.LITTLE_ENDIAN).toDouble()
                        val g = parser.readFloat32(ByteOrder.LITTLE_ENDIAN).toDouble()
                        val b = parser.readFloat32(ByteOrder.LITTLE_ENDIAN).toDouble()
                        val color = PALColor.rgb(r = r, g = g, b = b)
                        colors.add(color)
                    }

                    "ABAL" -> {
                        // Lab color
                        parser.readThroughNextInstanceOfASCII("<loc_")
                        val l = parser.readUInt16(ByteOrder.LITTLE_ENDIAN).toInt()
                        val a = parser.readUInt16(ByteOrder.LITTLE_ENDIAN).toInt()
                        val b = parser.readUInt16(ByteOrder.LITTLE_ENDIAN).toInt()

                        val lab = PALColor.lab(
                            l = l / 65535.0 * 100.0,
                            a = a / 65535.0 * 256.0 - 128.0,
                            b = b / 65535.0 * 256.0 - 128.0
                        )
                        // Convert LAB to RGB
                        colors.add(lab.converted(ColorSpace.RGB))
                    }

                    "KYMC" -> {
                        // CMYK color
                        parser.readThroughNextInstanceOfASCII("Hloc_")
                        val c = parser.readFloat32(ByteOrder.LITTLE_ENDIAN).toDouble()
                        val m = parser.readFloat32(ByteOrder.LITTLE_ENDIAN).toDouble()
                        val y = parser.readFloat32(ByteOrder.LITTLE_ENDIAN).toDouble()
                        val k = parser.readFloat32(ByteOrder.LITTLE_ENDIAN).toDouble()
                        val color = PALColor.cmyk(c = c, m = m, y = y, k = k)
                        colors.add(color)
                    }

                    "ALSH" -> {
                        // HSL color
                        parser.readThroughNextInstanceOfASCII("Dloc_")
                        val h = parser.readFloat32(ByteOrder.LITTLE_ENDIAN).toDouble()
                        val s = parser.readFloat32(ByteOrder.LITTLE_ENDIAN).toDouble()
                        val l = parser.readFloat32(ByteOrder.LITTLE_ENDIAN).toDouble()
                        val color = PALColor.hsl(hf = h, sf = s, lf = l)
                        colors.add(color)
                    }

                    "YARG" -> {
                        // Gray color
                        parser.readThroughNextInstanceOfASCII("<loc_")
                        val g1 = parser.readFloat32(ByteOrder.LITTLE_ENDIAN).toDouble()
                        val color = PALColor.white(white = g1)
                        colors.add(color)
                    }

                    else -> {
                        hasUnsupportedColorType = true
                        throw CommonError.CannotCreateColor()
                    }
                }
            } catch (e: Exception) {
                if (hasUnsupportedColorType) {
                    throw CommonError.UnsupportedPaletteType()
                }
                // Sometimes there are less colors than expected
                parser.seekSet(curPos)
                break
            }
        }

        // Read the color names (VNaP)
        try {
            parser.readThroughNextInstanceOfPattern(0x56, 0x4e, 0x61, 0x50)

            // Unknown offset
            parser.readUInt32(ByteOrder.LITTLE_ENDIAN)

            val nameCount = parser.readUInt32(ByteOrder.LITTLE_ENDIAN).toInt()
            for (index in 0 until minOf(nameCount, colors.size)) {
                val colorNameLen = parser.readUInt32(ByteOrder.LITTLE_ENDIAN).toInt()
                val colorName = parser.readStringUTF8(colorNameLen)
                colors[index].name = colorName
            }
        } catch (e: Exception) {
            // Names may not be present
        }

        result.colors = colors
        return result
    }

    override fun encode(palette: PALPalette, output: OutputStream) {
        val writer = BytesWriter(output)
        val allColors = palette.allColors()
        val colorCount = allColors.size

        if (colorCount == 0) {
            throw CommonError.TooFewColors()
        }

        // BOM 0x414BFF00 (4 bytes, little endian)
        writer.writeUInt32(0x414BFF00u, ByteOrder.LITTLE_ENDIAN)

        // Version 11 (4 bytes, little endian)
        writer.writeUInt32(11u, ByteOrder.LITTLE_ENDIAN)

        // NClP chunk marker (0x4E436C50 = "NClP" in ASCII, but reversed)
        writer.writePattern(0x50, 0x6C, 0x43, 0x4E)

        // Filename length and name
        val filename = palette.name.ifEmpty { "Palette" }
        writer.writeStringASCIIWithLength(filename, ByteOrder.LITTLE_ENDIAN)

        // VlaP chunk marker (0x566C6150 = "VlaP" reversed)
        writer.writePattern(0x50, 0x61, 0x6C, 0x56)

        // Color count (4 bytes, little endian)
        writer.writeUInt32(colorCount.toUInt(), ByteOrder.LITTLE_ENDIAN)

        // Write colors - all as RGB (ABGR) for simplicity
        allColors.forEach { color ->
            // Color marker "rloC" = "Color" backwards in ASCII: 0x72 0x6C 0x6F 0x43
            // But we need to write it as "rloC" which is: 0x72 0x6C 0x6F 0x43
            writer.writePattern(0x72, 0x6C, 0x6F, 0x43)

            // Skip 6 bytes (unknown data)
            writer.writePattern(0x00, 0x00, 0x00, 0x00, 0x00, 0x00)

            // Color type "ABGR" (RGB) - in ASCII: 0x41 0x42 0x47 0x52
            writer.writePattern(0x41, 0x42, 0x47, 0x52)

            // Marker "Dloc_" - "Color" backwards: 0x44 0x6C 0x6F 0x63 0x5F
            writer.writePattern(0x44, 0x6C, 0x6F, 0x63, 0x5F)

            // Write RGB values as float32 (little endian)
            val rgb = color.toRgb()
            writer.writeFloat32(rgb.rf.toFloat(), ByteOrder.LITTLE_ENDIAN)
            writer.writeFloat32(rgb.gf.toFloat(), ByteOrder.LITTLE_ENDIAN)
            writer.writeFloat32(rgb.bf.toFloat(), ByteOrder.LITTLE_ENDIAN)
        }

        // VNaP chunk marker (0x564E6150 = "VNaP" reversed)
        writer.writePattern(0x50, 0x61, 0x4E, 0x56)

        // Unknown offset (4 bytes, little endian) - typically 0
        writer.writeUInt32(0u, ByteOrder.LITTLE_ENDIAN)

        // Name count (4 bytes, little endian)
        writer.writeUInt32(colorCount.toUInt(), ByteOrder.LITTLE_ENDIAN)

        // Write color names
        allColors.forEach { color ->
            val colorName = color.name.ifEmpty { "Color" }
            writer.writeStringUTF8WithLength(colorName, ByteOrder.LITTLE_ENDIAN)
        }
    }
}

