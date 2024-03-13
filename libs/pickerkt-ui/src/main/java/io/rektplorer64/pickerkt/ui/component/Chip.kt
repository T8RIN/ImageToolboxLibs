package io.rektplorer64.pickerkt.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColor
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

object ChipDefaults {
    val Shape = RoundedCornerShape(8.dp)
    val ChipGroupHorizontalSpacing = 4.dp
    val ChipGroupVerticalSpacing = 4.dp
}

enum class ChipSize {
    Default,
    Compact
}

@Composable
fun Chip(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    selected: Boolean = false,
    color: Color = MaterialTheme.colorScheme.surface,
    onClick: () -> Unit = {},
    size: ChipSize = ChipSize.Default,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    iconSize: Dp = 18.dp,
    label: @Composable () -> Unit,
) {
    val selectionTransition = updateTransition(targetState = selected, label = "Selection")
    val borderWidth by selectionTransition.animateDp(label = "Border Width") {
        if (it) 0.dp else 1.dp
    }

    val surfaceColor by selectionTransition.animateColor(label = "Surface Color") {
        if (it) MaterialTheme.colorScheme.secondaryContainer else color
    }

    val borderOpacity by selectionTransition.animateFloat(label = "Border Opacity") {
        if (it) 0f else 1f
    }

    val height by animateDpAsState(
        targetValue = when (size) {
            ChipSize.Default -> 36.dp
            ChipSize.Compact -> 26.dp
        }
    )

    Surface(
        shape = ChipDefaults.Shape,
        color = surfaceColor,
        contentColor = MaterialTheme.colorScheme.contentColorFor(surfaceColor),
        modifier = modifier
            .animateContentSize()
            .height(height)
            .border(
                width = borderWidth,
                color = MaterialTheme.colorScheme.outline.copy(alpha = borderOpacity),
                shape = ChipDefaults.Shape
            )
            .clip(ChipDefaults.Shape)
            .clickable(
                enabled = enabled,
                indication = LocalIndication.current,
                interactionSource = remember { MutableInteractionSource() },
                onClick = onClick
            ),
        tonalElevation = 1.dp
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.wrapContentWidth()
        ) {
            AnimatedVisibility(visible = leadingIcon != null) {
                Box(
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .size(iconSize),
                    contentAlignment = Alignment.Center
                ) {
                    leadingIcon?.invoke()
                }
            }

            val textEndPadding by animateDpAsState(
                targetValue = when(size) {
                    ChipSize.Default -> if (trailingIcon != null) 8.dp else 16.dp
                    ChipSize.Compact -> if (trailingIcon != null) 4.dp else 8.dp
                }
            )

            Box(modifier = Modifier.padding(start = 8.dp, end = textEndPadding)) {
                ProvideTextStyle(
                    value = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                    ),
                    content = label
                )
            }

            AnimatedVisibility(visible = trailingIcon != null) {
                Box(
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .size(iconSize),
                    contentAlignment = Alignment.Center
                ) {
                    trailingIcon?.invoke()
                }
            }
        }
    }
}