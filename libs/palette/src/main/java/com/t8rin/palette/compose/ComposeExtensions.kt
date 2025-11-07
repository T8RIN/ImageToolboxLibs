package com.t8rin.palette.compose

import androidx.compose.ui.graphics.Color
import com.t8rin.palette.ColorType
import com.t8rin.palette.PALColor

/**
 * Convert PALColor to Jetpack Compose Color
 */
fun PALColor.toColor(): Color {
    return toComposeColor()
}

/**
 * Create PALColor from Jetpack Compose Color
 */
fun Color.toPALColor(name: String = "", colorType: ColorType = ColorType.Global): PALColor {
    return PALColor.fromComposeColor(this, name, colorType)
}

/**
 * Extension function for Color to get ARGB Int
 */
fun Color.toArgbInt(): Int {
    return value.toInt()
}

/**
 * Create Color from ARGB Int
 */
fun Int.toColor(): Color {
    return Color(this)
}


