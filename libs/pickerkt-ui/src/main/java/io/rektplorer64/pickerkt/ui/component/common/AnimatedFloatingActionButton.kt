package io.rektplorer64.pickerkt.ui.component.common

import androidx.compose.animation.*
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp


private val ExtendedFabTextPadding = 24.dp
private val FabHeight = 56.dp

@Composable
fun AnimatedFloatingActionButton(
    modifier: Modifier = Modifier,
    text: @Composable () -> Unit,
    onClick: () -> Unit,
    icon: @Composable () -> Unit,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    shape: Shape = RoundedCornerShape(16.dp),
    expanded: Boolean = true,
    containerColor: Color = MaterialTheme.colorScheme.primary,
    contentColor: Color = contentColorFor(containerColor),
    elevation: FloatingActionButtonElevation = FloatingActionButtonDefaults.elevation(),
) {

    val minWidth by animateDpAsState(targetValue = if (expanded) 80.dp else FabHeight)

    val startPadding = ExtendedFabTextPadding / 2
    val endPadding by animateDpAsState(targetValue = (if (expanded) 20.dp else ExtendedFabTextPadding) / 2)

    FloatingActionButton(
        modifier = modifier
            .animateContentSize(tween(durationMillis = 50, easing = LinearEasing))
            .sizeIn(
                minWidth = minWidth,
                minHeight = FabHeight,
            ),
        onClick = onClick,
        interactionSource = interactionSource,
        shape = shape,
        containerColor = containerColor,
        contentColor = contentColor,
        elevation = elevation,
    ) {
        Row(
            modifier = Modifier
                .animateContentSize(tween(durationMillis = 100, easing = LinearEasing))
                .padding(
                    start = startPadding,
                    end = endPadding
                ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            icon()

            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn(tween(200)) + slideInHorizontally(tween(durationMillis = 200)) + expandHorizontally(tween(durationMillis = 200)) { it / 2 },
                exit = fadeOut(tween(200)) + slideOutHorizontally(tween(durationMillis = 200)) + shrinkHorizontally(tween(durationMillis = 200)) { it / 2 }
            ) {
                Row {
                    Spacer(Modifier.width(12.dp))
                    ProvideTextStyle(
                        value = MaterialTheme.typography.labelLarge,
                        content = text,
                    )
                }
            }
        }
    }
}