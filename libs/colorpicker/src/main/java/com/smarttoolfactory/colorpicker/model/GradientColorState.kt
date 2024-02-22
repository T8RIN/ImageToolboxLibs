package com.smarttoolfactory.colorpicker.model

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpSize
import com.smarttoolfactory.colorpicker.selector.gradient.GradientType
import com.smarttoolfactory.colorpicker.ui.GradientAngle
import com.smarttoolfactory.colorpicker.ui.GradientOffset
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

@Composable
fun rememberGradientColorState(
    size: DpSize = DpSize.Zero
): GradientColorState {

    val density = LocalDensity.current

    return remember {
        val sizePx = if (size == DpSize.Zero) {
            Size.Zero
        } else {
            with(density) {
                Size(
                    size.width.toPx(),
                    size.height.toPx()
                )
            }
        }
        GradientColorState(sizePx)
    }
}

/**
 * Gradient and color state for setting and getting gradient color
 * with [gradientType] such as Linear, Radial or Sweep, [tileMode]s, and [colorStops].
 * * Linear gradient uses [gradientOffset] to set offset or angle.
 * * Radial gradient uses [centerFriction] and [radiusFriction]
 */
class GradientColorState(size: Size = Size.Zero) {

    var size by mutableStateOf(size)

    val brush: ShaderBrush
        get() {

            val colorStops = if (colorStops.size == 1) {
                listOf(colorStops.first(), colorStops.first()).toTypedArray()
            } else {
                colorStops.toTypedArray()
            }

            val brush: ShaderBrush = when (gradientType) {
                GradientType.Linear -> {
                    val angleRad = linearGradientAngle / 180f * PI
                    val x = cos(angleRad).toFloat()
                    val y = sin(angleRad).toFloat()

                    val radius = sqrt(size.width.pow(2) + size.height.pow(2)) / 2f
                    val offset = size.center + Offset(x * radius, y * radius)

                    val exactOffset = Offset(
                        x = min(offset.x.coerceAtLeast(0f), size.width),
                        y = size.height - min(offset.y.coerceAtLeast(0f), size.height)
                    )
                    Brush.linearGradient(
                        colorStops = colorStops,
                        start = Offset(size.width, size.height) - exactOffset,
                        end = exactOffset,
                        tileMode = tileMode
                    )
                }

                GradientType.Radial -> Brush.radialGradient(
                    colorStops = colorStops,
                    center = Offset(
                        x = size.width * centerFriction.x,
                        y = size.height * centerFriction.y
                    ),
                    radius = ((size.width.coerceAtLeast(size.height)) / 2 * radiusFriction)
                        .coerceAtLeast(0.01f),
                    tileMode = tileMode
                )

                GradientType.Sweep -> Brush.sweepGradient(
                    colorStops = colorStops,
                    center = Offset(
                        x = size.width * centerFriction.x,
                        y = size.height * centerFriction.y
                    ),
                )
            } as ShaderBrush

            return brush
        }

    var gradientType: GradientType by mutableStateOf(GradientType.Linear)
    var colorStops = mutableStateListOf(
        0.0f to Color(0xff00ffa6),
        0.5f to Color(0xff79ff00),
        1.0f to Color(0xff1f5308)
    )
    var tileMode by mutableStateOf(TileMode.Clamp)

    var linearGradientAngle by mutableFloatStateOf(0f)
    var gradientOffset by mutableStateOf(GradientOffset(GradientAngle.CW0))

    var centerFriction by mutableStateOf(Offset(.5f, .5f))
    var radiusFriction by mutableFloatStateOf(.5f)
}
