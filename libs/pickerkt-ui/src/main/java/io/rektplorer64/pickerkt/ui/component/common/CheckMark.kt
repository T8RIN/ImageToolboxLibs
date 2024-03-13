package io.rektplorer64.pickerkt.ui.component.common

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.rektplorer64.pickerkt.ui.common.compose.animation.slideContentTransitionSpec

@Composable
fun CheckMarkButton(
    modifier: Modifier = Modifier,
    selected: Boolean = true,
    borderColor: Color = LocalContentColor.current,
    label: String,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clickable(
                indication = rememberRipple(bounded = false),
                interactionSource = remember { MutableInteractionSource() },
                onClick = onClick
            )
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        CheckMark(label = label, selected = selected, borderColor = borderColor)
    }
}

enum class CheckMarkSize {
    Default,
    Mini
}

@Composable
@OptIn(ExperimentalAnimationApi::class)
fun CheckMark(
    modifier: Modifier = Modifier,
    selected: Boolean = true,
    size: CheckMarkSize = CheckMarkSize.Default,
    borderColor: Color = LocalContentColor.current,
    label: String
) {

    val transition = updateTransition(targetState = selected, label = "Selection")

    val borderAlpha by transition.animateFloat(
        label = "Border Alpha",
        transitionSpec = { tween() }
    ) {
        if (it) 0.3F else 0.8F
    }
    val borderWidth by transition.animateDp(
        label = "Border Width",
        transitionSpec = { tween() }
    ) {
        if (it) 1.dp else 4.dp
    }
    val fillColorAlpha by transition.animateFloat(
        label = "Fill Color Alpha",
        transitionSpec = { tween() }
    ) {
        if (it) 1F else 0F
    }

    val contentAlpha by transition.animateFloat(
        label = "Content Alpha",
        transitionSpec = { tween() }
    ) {
        if (it) 1F else 0F
    }

    val sizeTransition = updateTransition(targetState = size, label = "Size")


    val checkMarkSize by sizeTransition.animateDp(label = "Size In Dp") {
        when (it) {
            CheckMarkSize.Default -> 32.dp
            CheckMarkSize.Mini -> 24.dp
        }
    }

    val fontSize by sizeTransition.animateFloat(label = "Font Size") {
        when (it) {
            CheckMarkSize.Default -> 16f
            CheckMarkSize.Mini -> 12f
        }
    }

    val backgroundColor = MaterialTheme.colorScheme.primary
    val contentColor by animateColorAsState(targetValue = contentColorFor(backgroundColor))

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(checkMarkSize)
            .border(
                width = borderWidth,
                color = borderColor.copy(alpha = borderAlpha),
                shape = CircleShape
            )
            .background(
                color = backgroundColor.copy(alpha = fillColorAlpha),
                shape = CircleShape
            )
    ) {
        AnimatedContent(
            targetState = label,
            transitionSpec = {
                slideContentTransitionSpec<String, Int> { it.toIntOrNull() ?: 0 }.invoke(this)
            }
        ) {
            Text(
                text = it,
                fontWeight = FontWeight.Bold,
                color = contentColor.copy(alpha = contentAlpha),
                modifier = Modifier.padding(2.dp),
                fontSize = fontSize.sp
            )
        }
    }
}