package com.t8rin.palette.coders

import com.t8rin.palette.PALColor
import com.t8rin.palette.PALPalette
import com.t8rin.palette.utils.ByteOrder
import com.t8rin.palette.utils.BytesReader
import com.t8rin.palette.utils.BytesWriter
import java.io.InputStream
import java.io.OutputStream

/**
 * Adobe Color Table (ACT) palette coder
 */
class ACTPaletteCoder : PaletteCoder {
    override fun decode(input: InputStream): PALPalette {
        val allData = input.readBytes()
        val reader = BytesReader(allData)
        val result = PALPalette()

        // Read 256 RGB colors (768 bytes)
        repeat(256) {
            val rgb = reader.readData(3)
            val r = rgb[0].toUByte().toInt()
            val g = rgb[1].toUByte().toInt()
            val b = rgb[2].toUByte().toInt()
            val color = PALColor.rgb(
                r = r / 255.0,
                g = g / 255.0,
                b = b / 255.0
            )
            result.colors.add(color)
        }

        var numColors = 256
        // Try to read number of colors (optional)
        try {
            val numColorsBytes = reader.readData(2)
            numColors =
                ((numColorsBytes[0].toUByte().toInt() shl 8) or numColorsBytes[1].toUByte()
                    .toInt()).toShort().toInt()
            if (numColors > 0 && numColors < 256) {
                result.colors = result.colors.take(numColors).toMutableList()
            }
        } catch (e: Exception) {
            // No number of colors field
        }

        // Try to read transparency index (optional)
        try {
            val alphaIndexBytes = reader.readData(2)
            val alphaIndex =
                ((alphaIndexBytes[0].toUByte().toInt() shl 8) or alphaIndexBytes[1].toUByte()
                    .toInt()).toShort()
            if (alphaIndex >= 0 && alphaIndex < result.colors.size) {
                result.colors[alphaIndex.toInt()] = result.colors[alphaIndex.toInt()].withAlpha(0.0)
            }
        } catch (e: Exception) {
            // No transparency index field
        }

        // Try to read color names from extension (non-standard)
        try {
            val remainingData = allData.sliceArray(reader.readPosition.toInt() until allData.size)
            val remainingText = String(remainingData, java.nio.charset.StandardCharsets.UTF_8)
            val nameLine = remainingText.lines().find { it.startsWith("; ACT_NAMES:") }
            if (nameLine != null) {
                val namesStr = nameLine.substring("; ACT_NAMES: ".length).trim()
                val names = namesStr.split("|")
                names.forEachIndexed { index, name ->
                    if (index < result.colors.size && name.isNotEmpty()) {
                        result.colors[index] = result.colors[index].named(name)
                    }
                }
            }
        } catch (e: Exception) {
            // No names extension, continue without names
        }

        return result
    }

    override fun encode(palette: PALPalette, output: OutputStream) {
        val writer = BytesWriter(output)
        val flattenedColors = palette.allColors().map { it.toRgb() }
        val colors = flattenedColors.take(256)
        val maxColors = colors.size

        // Write 256 colors (pad with zeros if needed)
        repeat(256) { index ->
            if (index < maxColors) {
                val c = colors[index]
                writer.writeData(
                    byteArrayOf(
                        (c.rf * 255).toInt().coerceIn(0, 255).toByte(),
                        (c.gf * 255).toInt().coerceIn(0, 255).toByte(),
                        (c.bf * 255).toInt().coerceIn(0, 255).toByte()
                    )
                )
            } else {
                writer.writeData(byteArrayOf(0, 0, 0))
            }
        }

        // Write number of colors if less than 256
        if (maxColors < 256) {
            writer.writeUInt16(maxColors.toUShort(), ByteOrder.BIG_ENDIAN)
            writer.writeUInt16(0xFFFFu, ByteOrder.BIG_ENDIAN)
        }

        // Append color names as a comment extension (non-standard but preserves names)
        val allColorsList = palette.allColors()
        val names = (0 until maxColors).mapNotNull { index ->
            if (index < allColorsList.size && allColorsList[index].name.isNotEmpty()) {
                allColorsList[index].name
            } else null
        }
        if (names.isNotEmpty()) {
            val nameText = "\n; ACT_NAMES: ${names.joinToString("|")}\n"
            output.write(nameText.toByteArray(java.nio.charset.StandardCharsets.UTF_8))
        }
    }
}


