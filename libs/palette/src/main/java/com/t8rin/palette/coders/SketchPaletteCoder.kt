package com.t8rin.palette.coders

import com.t8rin.palette.PALColor
import com.t8rin.palette.PALPalette
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

    override fun decode(input: InputStream): PALPalette {
        val text = input.readText()
        val sketchFile = json.decodeFromString(SketchFile.serializer(), text)

        val result = PALPalette()
        result.colors = sketchFile.colors.map { sketchColor ->
            PALColor.rgb(
                r = sketchColor.red,
                g = sketchColor.green,
                b = sketchColor.blue,
                a = sketchColor.alpha
            )
        }.toMutableList()

        return result
    }

    override fun encode(palette: PALPalette, output: OutputStream) {
        val colors = palette.allColors().map { color ->
            val rgb = color.toRgb()
            SketchColor(
                red = rgb.rf,
                green = rgb.gf,
                blue = rgb.bf,
                alpha = rgb.af
            )
        }

        val file = SketchFile(
            compatibleVersion = "1.4",
            pluginVersion = "1.4",
            colors = colors
        )

        val encoded = json.encodeToString(SketchFile.serializer(), file)
        output.write(encoded.toByteArray(java.nio.charset.StandardCharsets.UTF_8))
    }
}


