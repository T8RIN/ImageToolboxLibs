package com.idapgroup.snowfall.types

import androidx.compose.ui.graphics.painter.Painter

/**
 * Type of flake used for animation.
 */
sealed interface FlakeType {
    data object Snowflakes : FlakeType
    class Custom(val data: List<Painter>) : FlakeType
}