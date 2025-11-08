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