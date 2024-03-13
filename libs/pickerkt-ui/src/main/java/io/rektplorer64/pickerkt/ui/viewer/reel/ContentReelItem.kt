package io.rektplorer64.pickerkt.ui.viewer.reel

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.rektplorer64.pickerkt.ui.component.common.CheckMark
import io.rektplorer64.pickerkt.ui.component.common.CheckMarkSize

val FocusedReelItemHeight = 72.dp
val UnfocusedReelItemHeight = 56.dp

@Composable
fun ContentReelItem(
    modifier: Modifier = Modifier,
    highlighted: Boolean,
    onClick: () -> Unit,
    selectionIndex: Int = -1,
    content: @Composable BoxScope.() -> Unit
) {

    val transition = updateTransition(targetState = highlighted, label = "Highlighted")

    val aspectRatio by transition.animateFloat(label = "Aspect Ratio") {
        if (it) 3 / 2f else 1f
    }

    val height by transition.animateDp(label = "Image Height") {
        if (it) FocusedReelItemHeight else UnfocusedReelItemHeight
    }

    val borderColor by transition.animateColor(label = "Border Color") {
        if (it) MaterialTheme.colorScheme.onSecondaryContainer else Color.Transparent
    }
    val borderWidth by transition.animateDp(label = "Border Width") { if (it) 4.dp else 0.dp }
    val shapeCorner by transition.animateDp(label = "Shape Corner") { if (it) 8.dp else 0.dp }
    val checkMarkPadding by transition.animateDp(label = "Check Mark padding") { if (it) 8.dp else 0.dp }

    val shape = RoundedCornerShape(shapeCorner)

    Surface(
        modifier = modifier
            .clickable(onClick = onClick)
            .border(
                width = borderWidth,
                color = borderColor,
                shape = shape
            )
            .shadow(elevation = shapeCorner, shape = shape, clip = false)
            //.animateItemPlacement()
            .clip(shape),
        color = MaterialTheme.colorScheme.secondaryContainer
    ) {
        Box {
            Box(
                modifier = Modifier
                    .height(height)
                    .aspectRatio(aspectRatio),
                content = content
            )

            val isSelected = selectionIndex >= 0
            AnimatedVisibility(
                visible = isSelected,
                modifier = Modifier.align(if (highlighted) Alignment.TopStart else Alignment.Center)
            ) {
                CheckMark(
                    modifier = Modifier.padding(checkMarkPadding),
                    label = if (isSelected) (selectionIndex + 1).toString() else "",
                    size = CheckMarkSize.Mini,
                    selected = isSelected
                )
            }
        }
    }
}