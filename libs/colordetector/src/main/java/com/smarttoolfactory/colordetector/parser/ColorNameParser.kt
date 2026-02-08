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

    private var rbgData: List<RGBData> = emptyList()

    /**
     * Parse name of [Color]
     */
    override fun parseColorName(color: Color): String {
        val rgbArray = ColorUtil.colorToRGBArray(color)

        val red: Int = rgbArray[0]
        val green: Int = rgbArray[1]
        val blue: Int = rgbArray[2]

        var distance: Int = Int.MAX_VALUE

        var colorId = -1

        rbgData.forEachIndexed { index, rgbData ->
            val currentDistance = sqrt(
                (
                        (rgbData.x - red) * (rgbData.x - red) +
                                (rgbData.y - green) * (rgbData.y - green) +
                                (rgbData.z - blue) * (rgbData.z - blue)
                        ).toDouble()
            ).toInt()

            if (currentDistance < distance) {
                distance = currentDistance
                colorId = index
            }
        }

        return if (colorId >= 0) {
            rbgData[colorId].label
        } else ColorItem.Unspecified
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
        return parseAsSequence(name)
            .sortedBy { it.name.length }
            .run {
                find {
                    it.name.equals(
                        other = "Apple Green",
                        ignoreCase = true
                    )
                }?.color ?: firstOrNull()?.color ?: Color.Black
            }
    }

    override suspend fun init(context: Context) = withContext(Dispatchers.IO) {
        rbgData = emptyList()
        try {
            JsonReader(context.assets.open("color_names.json").bufferedReader()).use { reader ->
                reader.beginObject()

                while (reader.hasNext() && isActive) {
                    val rgbArray = HexUtil.hexToRGB(reader.nextName())
                    val label = reader.nextString()

                    rbgData += RGBData(
                        x = rgbArray[0],
                        y = rgbArray[1],
                        z = rgbArray[2],
                        label = label
                    )
                }

                reader.endObject()
            }
        } catch (t: Throwable) {
            t.printStackTrace()
        }
    }

    private fun parseAsSequence(name: String) = rbgData.asSequence().filter {
        it.label.contains(
            other = name,
            ignoreCase = true
        ) || name.contains(
            other = it.label,
            ignoreCase = true
        )
    }.map {
        ColorWithName(
            name = it.label,
            color = Color(
                red = it.x,
                green = it.y,
                blue = it.z
            )
        )
    }

}

data class ColorWithName(
    val color: Color,
    val name: String
)

private data class RGBData(
    val x: Int,
    val y: Int,
    val z: Int,
    val label: String
)