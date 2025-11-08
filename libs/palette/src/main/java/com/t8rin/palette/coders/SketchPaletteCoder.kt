package com.t8rin.palette.coders

import com.t8rin.palette.Palette
import com.t8rin.palette.PaletteCoder
import com.t8rin.palette.PaletteColor
import com.t8rin.palette.utils.readText
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream

/**
 * Sketch palette coder
 */
class SketchPaletteCoder : PaletteCoder {
    private val json = Json {
        ignoreUnknownKeys = true
    }

    @Serializable
    private data class SketchColor(
        val red: Double,
        val green: Double,
        val blue: Double,
        val alpha: Double
    )

    @Serializable
    private data class SketchFile(
        val compatibleVersion: String,
        val pluginVersion: String,
        val colors: List<SketchColor>
    )

    override fun decode(input: InputStream): Palette {
        val text = input.readText()

        // Try to decode with names first
        val result = Palette.Builder()
        try {
            val sketchFileWithNames = json.decodeFromString(SketchFileWithNames.serializer(), text)
            result.colors = sketchFileWithNames.colors.map { sketchColor ->
                PaletteColor.rgb(
                    r = sketchColor.red,
                    g = sketchColor.green,
                    b = sketchColor.blue,
                    a = sketchColor.alpha,
                    name = sketchColor.name ?: ""
                )
            }.toMutableList()
        } catch (_: Throwable) {
            // Fall back to old format without names
            val sketchFile = json.decodeFromString(SketchFile.serializer(), text)
            result.colors = sketchFile.colors.map { sketchColor ->
                PaletteColor.rgb(
                    r = sketchColor.red,
                    g = sketchColor.green,
                    b = sketchColor.blue,
                    a = sketchColor.alpha
                )
            }.toMutableList()
        }

        return result.build()
    }

    @Serializable
    private data class SketchColorWithName(
        val name: String? = null,
        val red: Double,
        val green: Double,
        val blue: Double,
        val alpha: Double
    )

    @Serializable
    private data class SketchFileWithNames(
        val compatibleVersion: String,
        val pluginVersion: String,
        val colors: List<SketchColorWithName>
    )

    override fun encode(palette: Palette, output: OutputStream) {
        val colors = palette.allColors().map { color ->
            val rgb = color.toRgb()
            SketchColorWithName(
                name = if (color.name.isNotEmpty()) color.name else null,
                red = rgb.rf,
                green = rgb.gf,
                blue = rgb.bf,
                alpha = rgb.af
            )
        }

        val file = SketchFileWithNames(
            compatibleVersion = "1.4",
            pluginVersion = "1.4",
            colors = colors
        )

        val encoded = json.encodeToString(SketchFileWithNames.serializer(), file)
        output.write(encoded.toByteArray(java.nio.charset.StandardCharsets.UTF_8))
    }
}


