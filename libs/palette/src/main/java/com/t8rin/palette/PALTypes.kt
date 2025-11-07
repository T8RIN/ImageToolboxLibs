package com.t8rin.palette

import kotlinx.serialization.Serializable

/**
 * Color space representation
 */
@Serializable
enum class ColorSpace {
    CMYK,
    RGB,
    LAB,
    Gray
}

/**
 * The type of the color (normal, spot, global)
 */
@Serializable
enum class ColorType {
    Global,
    Spot,
    Normal
}

/**
 * Byte ordering for RGB
 */
enum class ColorByteFormat {
    RGB,
    BGR,
    ARGB,
    RGBA,
    ABGR,
    BGRA
}

/**
 * Cross-platform size structure
 */
data class Size(
    val width: Double,
    val height: Double
)


