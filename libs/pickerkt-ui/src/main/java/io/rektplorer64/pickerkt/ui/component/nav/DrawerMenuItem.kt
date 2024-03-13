package io.rektplorer64.pickerkt.ui.component.nav

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp

@Composable
fun DrawerMenuItem(
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    enabled: Boolean = true,
    onClick: () -> Unit,
    leadingIcon: @Composable (BoxScope.() -> Unit)? = null,
    trailingIcon: @Composable (BoxScope.() -> Unit)? = null,
    title: String
) {
    val enabledTransition = updateTransition(label = "Enable State", targetState = enabled)
    val composableAlpha by enabledTransition.animateFloat(label = "Alpha") {
        if (it) 1f else 0.6f
    }

    val selectionTransition = updateTransition(label = "Selection", targetState = selected)
    val backgroundAlpha by selectionTransition.animateFloat(label = "Item Alpha") {
        if (it) 1f else 0f
    }
    val contentColor by selectionTransition.animateColor(label = "Content Alpha") {
        if (it) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
    }


    Surface(
        modifier = modifier
            .alpha(composableAlpha)
            .height(56.dp),
        enabled = enabled,
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = backgroundAlpha),
        shape = RoundedCornerShape(50),
        onClick = { onClick() },
        contentColor = contentColor
    ) {
        Row(
            modifier = Modifier
                .padding(start = 16.dp, end = 24.dp)
                .fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CompositionLocalProvider(LocalContentColor provides contentColor) {

                AnimatedVisibility(
                    visible = leadingIcon != null,
                    enter = slideInHorizontally { -it },
                    exit = slideOutVertically { -it }
                ) {
                    Box(
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .sizeIn(maxWidth = 24.dp, maxHeight = 24.dp)
                            .aspectRatio(1f)
                            .size(24.dp),
                        content = {
                            leadingIcon?.invoke(this)
                        },
                        contentAlignment = Alignment.Center
                    )
                }

                Box(
                    modifier = Modifier.weight(1f),
                    content = {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.labelLarge,
                            color = contentColor,
                            modifier = Modifier
                                .align(Alignment.CenterStart)
                                .fillMaxWidth(),
                            maxLines = 1
                        )
                    }
                )
            }

            AnimatedVisibility(
                visible = trailingIcon != null,
                enter = slideInHorizontally { -it },
                exit = slideOutVertically { -it }
            ) {
                ProvideTextStyle(value = MaterialTheme.typography.labelLarge.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)) {
                    Box(
                        content = {
                            trailingIcon?.invoke(this)
                        },
                        contentAlignment = Alignment.Center
                    )
                }
            }

        }
    }
}

