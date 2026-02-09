package com.smarttoolfactory.colordetector.parser

import android.content.Context
import android.util.JsonReader
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.smarttoolfactory.colordetector.model.ColorItem
import com.smarttoolfactory.colordetector.util.ColorUtil
import com.smarttoolfactory.colordetector.util.HexUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import kotlin.math.sqrt

@Composable
fun rememberColorParser(): ColorNameParser = ColorNameParser

interface ColorNameParser {
    fun parseColorName(color: Color): String
    fun parseColorFromName(name: String): List<ColorWithName>
    fun parseColorFromNameSingle(name: String): Color
    suspend fun init(context: Context)

    companion object : ColorNameParser by ColorNameParserImpl
}

/**
 * Parses color name from [Color]
 */
private object ColorNameParserImpl : ColorNameParser {

    private val colorNames: MutableMap<String, ColorWithName> = mutableMapOf()

    /**
     * Parse name of [Color]
     */
    override fun parseColorName(color: Color): String {
        val hex = ColorUtil.colorToHex(color).uppercase().replace("#", "#FF")
        return colorNames[hex]?.name ?: run {
            val red = color.red
            val green = color.green
            val blue = color.blue

            colorNames.values.minByOrNull { color ->
                sqrt(
                    (color.red - red) * (color.red - red) +
                            (color.green - green) * (color.green - green) +
                            (color.blue - blue) * (color.blue - blue)
                )
            }?.name ?: ColorItem.Unspecified
        }
    }

    override fun parseColorFromName(
        name: String
    ): List<ColorWithName> = parseAsSequence(name).toList().ifEmpty {
        listOf(
            ColorWithName(
                color = Color.Black,
                name = "Black"
            )
        )
    }

    override fun parseColorFromNameSingle(name: String): Color {
        val normalizedName = name.trim().lowercase()
        val values = colorNames.values

        return values
            .firstOrNull { color ->
                color.name.lowercase() == normalizedName
            }
            ?.let { color ->
                Color(
                    red = color.red,
                    green = color.green,
                    blue = color.blue
                )
            }
            ?: values
                .firstOrNull { color ->
                    color.name.lowercase().contains(normalizedName) ||
                            normalizedName.contains(color.name.lowercase())
                }
                ?.let { color ->
                    Color(
                        red = color.red,
                        green = color.green,
                        blue = color.blue
                    )
                }
            ?: Color.Black
    }

    override suspend fun init(context: Context) = withContext(Dispatchers.IO) {
        try {
            JsonReader(context.assets.open("color_names.json").bufferedReader()).use { reader ->
                reader.beginObject()

                while (reader.hasNext() && isActive) {
                    val hex = reader.nextName()
                    val name = reader.nextString()
                    val color = HexUtil.hexToColor(hex)

                    colorNames[hex] = ColorWithName(
                        color = color,
                        name = name
                    )
                }

                reader.endObject()
            }
        } catch (t: Throwable) {
            t.printStackTrace()
        }
    }

    private fun parseAsSequence(name: String) = colorNames.values.asSequence().filter {
        it.name.contains(
            other = name,
            ignoreCase = true
        ) || name.contains(
            other = it.name,
            ignoreCase = true
        )
    }

}

data class ColorWithName(
    val color: Color,
    val name: String
) {
    val red = color.red
    val green = color.green
    val blue = color.blue
}