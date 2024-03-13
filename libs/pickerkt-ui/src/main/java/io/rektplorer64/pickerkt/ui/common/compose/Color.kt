package io.rektplorer64.pickerkt.ui.common.compose

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ExperimentalGraphicsApi

fun Color.lighten(amount: Int): Color {
    val hsl = toHsl()
    val lightenColor = hsl.copy(lightness = hsl.lightness + amount)
    return lightenColor.toColor(alpha = alpha)
}

fun Color.darken(amount: Int): Color {
    val hsl = toHsl()
    val lightenColor = hsl.copy(lightness = hsl.lightness - amount)
    return lightenColor.toColor(alpha = alpha)
}

fun Color.lighten(fraction: Float): Color {
    val hsl = toHsl()
    val lightenColor = hsl.copy(lightness = hsl.lightness + (fraction * hsl.lightness))
    return lightenColor.toColor(alpha = alpha)
}

fun Color.darken(fraction: Float): Color {
    val hsl = toHsl()
    val lightenColor = hsl.copy(lightness = hsl.lightness - (fraction * hsl.lightness))
    return lightenColor.toColor(alpha = alpha)
}

val Color.isDark: Boolean
    get() {
        val yiq = ((red * 299) + (green * 587) + (blue * 114)) / 1000F
        return yiq < 128
    }

val Color.isLight: Boolean
    get() = !isDark

class HslColor(
    hue: Float,
    saturation: Float,
    lightness: Float
) {

    val hue: Float = hue.coerceIn(0f..1f)
    val saturation: Float = saturation.coerceIn(0f..1f)
    val lightness: Float = lightness.coerceIn(0f..1f)

    @OptIn(ExperimentalGraphicsApi::class)
    fun toColor(alpha: Float = 1f): Color {
        return Color.hsl(
            hue = hue * 360,
            saturation = saturation,
            lightness = lightness,
            alpha = alpha
        )
    }

    fun copy(
        hue: Float = this.hue,
        saturation: Float = this.saturation,
        lightness: Float = this.lightness
    ): HslColor {
        return HslColor(
            hue = hue,
            saturation = saturation,
            lightness = lightness
        )
    }

    override fun toString(): String {
        return "HslColor(hue=$hue, saturation=$saturation, lightness=$lightness)"
    }
}

/**
 * Converts an RGB color value to HSL. Conversion formula
 * adapted from http://en.wikipedia.org/wiki/HSL_color_space.
 * returns h, s, and l in the set [0, 1].
 *
 * @return the HSL representation
 */
fun Color.toHsl(): HslColor {

    val colorList = listOf(red, green, blue)
    val max = colorList.maxOrNull()!!
    val min = colorList.minOrNull()!!

    var hue: Float
    val saturation: Float
    val lightness: Float = (max + min) / 2.0f
    if (max == min) {
        saturation = 0.0f
        hue = saturation
    } else {
        val delta = max - min
        saturation = if (lightness > 0.5f) delta / (2.0f - max - min) else delta / (max + min)
        hue = if (red > green && red > blue) {
            (green - blue) / delta + (if (green < blue) 6.0f else 0.0f)
        } else if (green > blue) {
            (blue - red) / delta + 2.0f
        } else {
            (red - green) / delta + 4.0f
        }
        hue /= 6.0f
    }
    return HslColor(hue = hue, saturation = saturation, lightness = lightness)
}