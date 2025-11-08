package com.t8rin.palette

import kotlinx.serialization.Serializable

/**
 * The type of the color (normal, spot, global)
 */
@Serializable
enum class ColorType {
    Global,
    Spot,
    Normal
}