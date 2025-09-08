package com.smarttoolfactory.colordetector

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.palette.graphics.Palette
import com.smarttoolfactory.colordetector.model.ColorData
import com.smarttoolfactory.colordetector.parser.ColorNameParser
import com.smarttoolfactory.colordetector.util.ColorUtil
import com.smarttoolfactory.colordetector.util.ColorUtil.fractionToIntPercent

/**
 * Composable that creates color lists [Palette.Swatch] using **Palette API**
 * @param imageBitmap is the image that colors are generated from
 * should be displayed
 * @param maximumColorCount maximum number of [Palette.Swatch]es that should be generated
 * from [imageBitmap]. Maximum number might not be achieved based on image color composition
 * @param onColorChange callback to notify that user moved and picked a color
 */
@Composable
fun ImageColorPalette(
    modifier: Modifier = Modifier,
    state: ImageColorPaletteState,
    style: TextStyle,
    borderWidth: Dp,
    onEmpty: @Composable ColumnScope.() -> Unit,
    onColorChange: (ColorData) -> Unit
) {
    ColorProfileList(
        modifier = modifier,
        paletteDataList = state.paletteData,
        onColorChange = onColorChange,
        onEmpty = onEmpty,
        borderWidth = borderWidth,
        style = style
    )
}

@Composable
private fun ColorProfileList(
    modifier: Modifier,
    style: TextStyle,
    borderWidth: Dp,
    paletteDataList: List<PaletteData>,
    onEmpty: @Composable ColumnScope.() -> Unit,
    onColorChange: (ColorData) -> Unit
) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        paletteDataList.forEach { paletteData: PaletteData ->
            val percent = paletteData.percent.fractionToIntPercent()
            val colorData = paletteData.colorData

            ColorItemRow(
                style = style,
                modifier = Modifier
                    .then(
                        if (borderWidth > 0.dp) {
                            Modifier.border(
                                width = borderWidth,
                                shape = RoundedCornerShape(50),
                                color = MaterialTheme.colorScheme.outlineVariant
                            )
                        } else Modifier.shadow(1.dp, RoundedCornerShape(50))
                    )
                    .background(
                        MaterialTheme.colorScheme.surfaceColorAtElevation(6.dp),
                        RoundedCornerShape(50)
                    )
                    .fillMaxWidth(),
                colorData = colorData,
                colorModifier = if (borderWidth > 0.dp) {
                    Modifier.border(
                        width = borderWidth,
                        shape = RoundedCornerShape(50),
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                } else Modifier.shadow(1.dp, RoundedCornerShape(50)),
                populationPercent = "$percent%",
                onClick = onColorChange,
                contentColor = MaterialTheme.colorScheme.onSurface
            )
        }
        if (paletteDataList.isEmpty()) onEmpty()
    }
}

data class PaletteData(
    val colorData: ColorData,
    val percent: Float
)

@Composable
fun rememberImageColorPaletteState(
    imageBitmap: ImageBitmap,
    maximumColorCount: Int = 32,
): ImageColorPaletteState {
    return remember(imageBitmap, maximumColorCount) {
        derivedStateOf {
            ImageColorPaletteStateImpl(
                image = imageBitmap,
                maximumColorCount = maximumColorCount
            )
        }
    }.value
}

interface ImageColorPaletteState {
    val image: ImageBitmap
    val maximumColorCount: Int
    val paletteData: List<PaletteData>
}

private class ImageColorPaletteStateImpl(
    override val image: ImageBitmap,
    override val maximumColorCount: Int
) : ImageColorPaletteState {
    override val paletteData: List<PaletteData> = run {
        val paletteData = mutableListOf<PaletteData>()
        val palette = Palette
            .from(image.asAndroidBitmap())
            .maximumColorCount(maximumColorCount)
            .generate()


        val numberOfPixels: Float = palette.swatches.sumOf {
            it.population
        }.toFloat()

        palette.swatches.forEach { paletteSwatch ->
            paletteSwatch?.let { swatch ->
                val color = Color(swatch.rgb)
                val name = ColorNameParser.parseColorName(color)
                val colorData = ColorData(color, name)
                val percent: Float = swatch.population / numberOfPixels
                paletteData.add(PaletteData(colorData = colorData, percent))
            }
        }

        paletteData.distinctBy {
            it.colorData.name
        }.sortedWith(
            compareBy(
                { it.colorData.color.luminance() },
                { ColorUtil.colorToHSV(it.colorData.color)[0] },
            )
        )
    }
}