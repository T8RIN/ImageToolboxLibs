package com.t8rin.palette.coders

import com.t8rin.palette.ColorSpace
import com.t8rin.palette.ColorType
import com.t8rin.palette.CommonError
import com.t8rin.palette.PALColor
import com.t8rin.palette.PALGroup
import com.t8rin.palette.PALPalette
import com.t8rin.palette.utils.ByteOrder
import com.t8rin.palette.utils.BytesReader
import com.t8rin.palette.utils.BytesWriter
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.io.OutputStream

/**
 * ColorPaletteCodable binary format (DCP) coder
 */
class DCPPaletteCoder : PaletteCoder {
    companion object {
        const val BOM: UShort = 32156u
        const val VERSION: UShort = 1u
        const val GROUP_IDENTIFIER: UByte = 0xEAu
        const val COLOR_IDENTIFIER: UByte = 0xC0u
    }

    override fun decode(input: InputStream): PALPalette {
        // Read all data first for seek support
        val data = input.readBytes()
        val parser = BytesReader(ByteArrayInputStream(data))
        val result = PALPalette()

        // Read BOM
        if (parser.readUInt16(ByteOrder.LITTLE_ENDIAN) != BOM) {
            throw CommonError.InvalidBOM()
        }

        // Read version
        if (parser.readUInt16(ByteOrder.LITTLE_ENDIAN) != VERSION) {
            throw CommonError.InvalidBOM()
        }

        // Palette name
        result.name = parser.readPascalStringUTF16(ByteOrder.LITTLE_ENDIAN)

        // Read the expected number of groups
        val expectedGroupCount = parser.readUInt16(ByteOrder.LITTLE_ENDIAN).toInt()

        // Read in the groups
        val groups = mutableListOf<PALGroup>()
        for (i in 0 until expectedGroupCount) {
            // Read a group identifier tag
            if (parser.readByte() != GROUP_IDENTIFIER.toByte()) {
                throw CommonError.InvalidBOM()
            }

            // Read the group name
            val groupName = parser.readPascalStringUTF16(ByteOrder.LITTLE_ENDIAN)

            // Read the expected number of colors
            val expectedColorCount = parser.readUInt16(ByteOrder.LITTLE_ENDIAN).toInt()

            // The groups colors
            val colors = mutableListOf<PALColor>()
            for (j in 0 until expectedColorCount) {
                colors.add(parser.readColor())
            }

            groups.add(PALGroup(colors = colors, name = groupName))
        }

        // First group is always the 'global' colors
        if (groups.isEmpty()) {
            throw CommonError.InvalidFormat()
        }

        result.colors = groups[0].colors
        result.groups = groups.drop(1).toMutableList()
        return result
    }

    override fun encode(palette: PALPalette, output: OutputStream) {
        val writer = BytesWriter(output)

        // Expected BOM
        writer.writeUInt16(BOM, ByteOrder.LITTLE_ENDIAN)

        // Version
        writer.writeUInt16(VERSION, ByteOrder.LITTLE_ENDIAN)

        // Write the palette name
        writer.writePascalStringUTF16(palette.name, ByteOrder.LITTLE_ENDIAN)

        // Write the number of groups (global colors + groups)
        val allGroups = palette.allGroups
        writer.writeUInt16(allGroups.size.toUShort(), ByteOrder.LITTLE_ENDIAN)

        allGroups.forEach { group ->
            // Write a group identifier tag
            writer.writeByte(GROUP_IDENTIFIER.toByte())

            // Write the group name
            writer.writePascalStringUTF16(group.name, ByteOrder.LITTLE_ENDIAN)

            // Write the number of colors in the group
            writer.writeUInt16(group.colors.size.toUShort(), ByteOrder.LITTLE_ENDIAN)

            group.colors.forEach { color ->
                writer.writeColor(color)
            }
        }
    }
}

private fun BytesReader.readColor(): PALColor {
    // Read a color identifier tag
    if (readByte() != DCPPaletteCoder.COLOR_IDENTIFIER.toByte()) {
        throw CommonError.InvalidBOM()
    }

    // Read the color name
    val colorName = readPascalStringUTF16(ByteOrder.LITTLE_ENDIAN)

    val colorspaceID = readUInt8()

    val colorSpace: ColorSpace
    val components: List<Double>

    when (colorspaceID.toInt()) {
        1 -> {
            // CMYK
            colorSpace = ColorSpace.CMYK
            components = listOf(
                readFloat32(ByteOrder.LITTLE_ENDIAN).toDouble(),
                readFloat32(ByteOrder.LITTLE_ENDIAN).toDouble(),
                readFloat32(ByteOrder.LITTLE_ENDIAN).toDouble(),
                readFloat32(ByteOrder.LITTLE_ENDIAN).toDouble()
            )
        }

        2 -> {
            // RGB
            colorSpace = ColorSpace.RGB
            components = listOf(
                readFloat32(ByteOrder.LITTLE_ENDIAN).toDouble(),
                readFloat32(ByteOrder.LITTLE_ENDIAN).toDouble(),
                readFloat32(ByteOrder.LITTLE_ENDIAN).toDouble()
            )
        }

        3 -> {
            // LAB
            colorSpace = ColorSpace.LAB
            components = listOf(
                readFloat32(ByteOrder.LITTLE_ENDIAN).toDouble(),
                readFloat32(ByteOrder.LITTLE_ENDIAN).toDouble(),
                readFloat32(ByteOrder.LITTLE_ENDIAN).toDouble()
            )
        }

        4 -> {
            // Gray
            colorSpace = ColorSpace.Gray
            components = listOf(readFloat32(ByteOrder.LITTLE_ENDIAN).toDouble())
        }

        else -> throw CommonError.InvalidFormat()
    }

    // Alpha component
    val alpha = readFloat32(ByteOrder.LITTLE_ENDIAN).toDouble()

    // Color type
    val type = readUInt8()
    val colorType: ColorType = when (type.toInt()) {
        1 -> ColorType.Global
        2 -> ColorType.Spot
        3 -> ColorType.Normal
        else -> throw CommonError.InvalidFormat()
    }

    return PALColor(
        name = colorName,
        colorType = colorType,
        colorSpace = colorSpace,
        colorComponents = components,
        alpha = alpha
    )
}

private fun BytesWriter.writeColor(color: PALColor) {
    // Write a color identifier tag
    writeByte(DCPPaletteCoder.COLOR_IDENTIFIER.toByte())

    // Color name
    writePascalStringUTF16(color.name, ByteOrder.LITTLE_ENDIAN)

    // Write the colorspace identifier
    val colorspaceID: UByte = when (color.colorSpace) {
        ColorSpace.CMYK -> 1u
        ColorSpace.RGB -> 2u
        ColorSpace.LAB -> 3u
        ColorSpace.Gray -> 4u
    }
    writeUInt8(colorspaceID)

    // Write the color components
    color.colorComponents.forEach { comp ->
        writeFloat32(comp.toFloat(), ByteOrder.LITTLE_ENDIAN)
    }

    // Color alpha
    writeFloat32(color.alpha.toFloat(), ByteOrder.LITTLE_ENDIAN)

    // Color type
    val type: UByte = when (color.colorType) {
        ColorType.Global -> 1u
        ColorType.Spot -> 2u
        ColorType.Normal -> 3u
    }
    writeUInt8(type)
}

