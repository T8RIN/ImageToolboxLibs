package com.yalantis.ucrop.compose

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.vector.ImageVector

@Immutable
data class HorizontalWheelSliderConfig(
    val mirrorIcon: ImageVector? = null,
    val rotate90Icon: ImageVector? = null
)