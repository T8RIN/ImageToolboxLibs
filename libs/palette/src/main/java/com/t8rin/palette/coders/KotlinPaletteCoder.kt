package com.t8rin.palette.coders

import com.t8rin.palette.ColorSpace
import com.t8rin.palette.CommonError
import com.t8rin.palette.PALColor
import com.t8rin.palette.PALPalette
import java.io.InputStream
import java.io.OutputStream

/**
 * Kotlin/Jetpack Compose code generator (encode only)
 */
class KotlinPaletteCoder : PaletteCoder {
    override fun decode(input: InputStream): PALPalette {
        throw CommonError.NotImplemented()
    }

    private fun sanitizeName(name: String): String {
        // Remove invalid characters and make it a valid Kotlin identifier
        return name
            .replace(Regex("[^a-zA-Z0-9_]"), "_")
            .replace(Regex("^[0-9]"), "_$0") // Can't start with digit
            .takeIf { it.isNotEmpty() } ?: "color"
    }

    private fun formatColor(rgb: PALColor.RGB): String {
        val r = (rgb.rf * 255).toInt().coerceIn(0, 255)
        val g = (rgb.gf * 255).toInt().coerceIn(0, 255)
        val b = (rgb.bf * 255).toInt().coerceIn(0, 255)
        val a = (rgb.af * 255).toInt().coerceIn(0, 255)

        // Format: Color(0xAARRGGBB)
        val argb = (a shl 24) or (r shl 16) or (g shl 8) or b
        return "Color(0x${argb.toUInt().toString(16).uppercase().padStart(8, '0')})"
    }

    override fun encode(palette: PALPalette, output: OutputStream) {
        val packageName = if (palette.name.isNotEmpty()) {
            palette.name.lowercase()
                .replace(Regex("[^a-z0-9]"), "")
                .takeIf { it.isNotEmpty() && it[0].isLetter() }
                ?: "palette"
        } else {
            "palette"
        }

        var result = "package $packageName\n\n"
        result += "import androidx.compose.ui.graphics.Color\n\n"
        result += "/**\n"
        result += " * Exported palette: ${palette.name.ifEmpty { "Untitled" }}\n"
        result += " * Total colors: ${palette.totalColorCount}\n"
        result += " */\n"
        result += "object ExportedPalette {\n\n"

        // Generate individual color constants with names
        val allColorsList = palette.allColors()
        if (allColorsList.isNotEmpty()) {
            result += "    // Individual color constants\n"
            allColorsList.forEachIndexed { index, color ->
                try {
                    val converted =
                        if (color.colorSpace == ColorSpace.RGB) color else color.converted(
                            ColorSpace.RGB
                        )
                    val rgb = converted.toRgb()
                    val colorName = if (color.name.isNotEmpty()) {
                        sanitizeName(color.name)
                    } else {
                        "color$index"
                    }
                    result += "    val $colorName: Color = ${formatColor(rgb)}\n"
                } catch (e: Exception) {
                    // Skip invalid colors
                }
            }
            result += "\n"
        }

        // Generate groups
        palette.allGroups.forEachIndexed { groupIndex, group ->
            if (group.colors.isEmpty()) return@forEachIndexed

            val groupName = if (group.name.isNotEmpty() && group.name != "global") {
                sanitizeName(group.name)
            } else {
                "group$groupIndex"
            }

            result += "    // Group: ${group.name}\n"
            result += "    val $groupName: List<Color> = listOf(\n"

            group.colors.forEachIndexed { index, color ->
                try {
                    val converted =
                        if (color.colorSpace == ColorSpace.RGB) color else color.converted(
                            ColorSpace.RGB
                        )
                    val rgb = converted.toRgb()
                    val indent = "        "
                    result += "$indent${formatColor(rgb)}"

                    if (index < group.colors.size - 1) {
                        result += ","
                    }
                    result += "\n"
                } catch (e: Exception) {
                    // Skip invalid colors
                }
            }

            result += "    )\n\n"
        }

        // Generate allColors list
        val allColors = allColorsList.mapNotNull { color ->
            try {
                val converted =
                    if (color.colorSpace == ColorSpace.RGB) color else color.converted(ColorSpace.RGB)
                converted.toRgb()
            } catch (e: Exception) {
                null
            }
        }

        if (allColors.isNotEmpty()) {
            result += "    /**\n"
            result += "     * All colors from all groups\n"
            result += "     */\n"
            result += "    val allColors: List<Color> = listOf(\n"

            allColors.forEachIndexed { index, rgb ->
                val indent = "        "
                result += "$indent${formatColor(rgb)}"

                if (index < allColors.size - 1) {
                    result += ","
                }
                result += "\n"
            }

            result += "    )\n"
        }

        result += "}\n"

        output.write(result.toByteArray(java.nio.charset.StandardCharsets.UTF_8))
    }
}

