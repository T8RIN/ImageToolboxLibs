package com.t8rin.palette

import kotlinx.serialization.Serializable
import java.util.UUID

/**
 * A grouping of colors
 */
@Serializable
data class ColorGroup(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val colors: List<PaletteColor> = mutableListOf()
)