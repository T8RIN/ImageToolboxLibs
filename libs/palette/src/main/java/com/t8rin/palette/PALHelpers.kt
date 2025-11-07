package com.t8rin.palette

/**
 * Helper functions for creating colors
 */

/**
 * Create a color from RGB components (0.0 ... 1.0)
 */
fun rgbf(
    r: Double,
    g: Double,
    b: Double,
    alpha: Double = 1.0,
    name: String = "",
    colorType: ColorType = ColorType.Global
): PALColor {
    return PALColor.rgb(r, g, b, alpha, name, colorType)
}

/**
 * Create a color from RGB components (0 ... 255)
 */
fun rgb255(
    r: Int,
    g: Int,
    b: Int,
    a: Int = 255,
    name: String = "",
    colorType: ColorType = ColorType.Normal
): PALColor {
    return PALColor.rgb(
        r = r / 255.0,
        g = g / 255.0,
        b = b / 255.0,
        a = a / 255.0,
        name = name,
        colorType = colorType
    )
}

/**
 * Create a color from hex string
 */
fun rgb(
    hexString: String,
    format: ColorByteFormat = ColorByteFormat.RGBA,
    name: String = "",
    colorType: ColorType = ColorType.Normal
): PALColor {
    val cleanHex = hexString.removePrefix("#").uppercase()

    return when (format) {
        ColorByteFormat.RGB -> {
            require(cleanHex.length == 6) { "RGB hex string must be 6 characters" }
            val r = cleanHex.substring(0, 2).toInt(16) / 255.0
            val g = cleanHex.substring(2, 4).toInt(16) / 255.0
            val b = cleanHex.substring(4, 6).toInt(16) / 255.0
            PALColor.rgb(r, g, b, name = name, colorType = colorType)
        }

        ColorByteFormat.RGBA -> {
            require(cleanHex.length == 8) { "RGBA hex string must be 8 characters" }
            val r = cleanHex.substring(0, 2).toInt(16) / 255.0
            val g = cleanHex.substring(2, 4).toInt(16) / 255.0
            val b = cleanHex.substring(4, 6).toInt(16) / 255.0
            val a = cleanHex.substring(6, 8).toInt(16) / 255.0
            PALColor.rgb(r, g, b, a, name, colorType)
        }

        ColorByteFormat.ARGB -> {
            require(cleanHex.length == 8) { "ARGB hex string must be 8 characters" }
            val a = cleanHex.substring(0, 2).toInt(16) / 255.0
            val r = cleanHex.substring(2, 4).toInt(16) / 255.0
            val g = cleanHex.substring(4, 6).toInt(16) / 255.0
            val b = cleanHex.substring(6, 8).toInt(16) / 255.0
            PALColor.rgb(r, g, b, a, name, colorType)
        }

        else -> throw CommonError.InvalidRGBHexString(hexString)
    }
}

/**
 * Extension function to clamp a Double value to unit range [0.0, 1.0]
 */
fun Double.unitClamped(): Double = this.coerceIn(0.0, 1.0)


