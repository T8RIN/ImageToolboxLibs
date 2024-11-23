package com.t8rin.editbox

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

internal fun getTouchRegion(
    position: Offset,
    rect: Rect,
    threshold: Float
): TouchRegion {

    // Instead of using square check for power of 2 of threshold
    val target2 = threshold*threshold
    return when {

        inDistanceSquared(
            position,
            rect.topLeft,
            target2
        ) -> TouchRegion.TopLeft
        inDistanceSquared(
            position,
            rect.topRight,
            target2
        ) -> TouchRegion.TopRight
        inDistanceSquared(
            position,
            rect.bottomLeft,
            target2
        ) -> TouchRegion.BottomLeft
        inDistanceSquared(
            position,
            rect.bottomRight,
            target2
        ) -> TouchRegion.BottomRight
        rect.contains(offset = position) -> TouchRegion.Inside
        else -> TouchRegion.None
    }
}

/**
 * Check if [target] which is power of 2 of actual value to not use square to make this
 * operation cheaper
 */
internal fun inDistanceSquared(offset1: Offset, offset2: Offset, target: Float): Boolean {
    val x1 = offset1.x
    val y1 = offset1.y

    val x2 = offset2.x
    val y2 = offset2.y

    val distance = ((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1))
    return distance < target
}

internal fun DrawScope.drawBorderCircle(
    radius: Float,
    center: Offset,
    colorScheme: ColorScheme,
    borderAlpha: Float,
    scale: Float = 1f
) {
    drawCircle(
        color = colorScheme.primary.copy(borderAlpha),
        center = center,
        radius = radius * scale
    )
    drawCircle(
        color = colorScheme.primaryContainer.copy(borderAlpha),
        center = center,
        radius = radius * 0.8f * scale
    )
}

