package com.t8rin.editbox

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.InfiniteTransition
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp

@Composable
fun BoxWithConstraintsScope.EditBox(
    onTap: () -> Unit,
    modifier: Modifier = Modifier,
    state: EditBoxState = remember { EditBoxState() },
    content: @Composable BoxScope.() -> Unit
) {
    val parentSize by remember(constraints) {
        derivedStateOf {
            IntSize(
                constraints.maxWidth,
                constraints.maxHeight
            )
        }
    }
    EditBox(
        modifier = modifier,
        onTap = onTap,
        state = state,
        parentSize = parentSize,
        content = content
    )
}

@Composable
fun EditBox(
    onTap: () -> Unit,
    parentSize: IntSize,
    modifier: Modifier = Modifier,
    state: EditBoxState = remember { EditBoxState() },
    content: @Composable BoxScope.() -> Unit
) {
    var contentSize by remember {
        mutableStateOf(IntSize.Zero)
    }

    val parentMaxWidth = parentSize.width
    val parentMaxHeight = parentSize.height

    SideEffect {
        state.canvasSize = parentSize
    }

    val transformState = rememberTransformableState { zoomChange, offsetChange, rotationChange ->
        state.applyChanges(
            parentMaxWidth = parentMaxWidth,
            parentMaxHeight = parentMaxHeight,
            contentSize = contentSize,
            zoomChange = zoomChange,
            offsetChange = offsetChange,
            rotationChange = rotationChange
        )
    }

    val tapScale = remember { Animatable(1f) }

    LaunchedEffect(state.isActive) {
        if (state.isActive) {
            tapScale.animateTo(0.95f)
            tapScale.animateTo(1.02f)
            tapScale.animateTo(1f)
        }
    }

    Box(
        modifier = modifier
            .onSizeChanged {
                contentSize = it
            }
            .graphicsLayer(
                scaleX = state.scale,
                scaleY = state.scale,
                rotationZ = state.rotation,
                translationX = state.offset.x,
                translationY = state.offset.y
            )
            .scale(tapScale.value)
            .pointerInput(onTap) {
                detectTapGestures {
                    onTap()
                }
            }
            .transformable(
                state = transformState,
                enabled = state.isActive
            ),
        contentAlignment = Alignment.Center
    ) {
        content()
        val borderAlpha by animateFloatAsState(if (state.isActive) 1f else 0f)
        AnimatedBorder(
            modifier = Modifier.matchParentSize(),
            alpha = borderAlpha,
            scale = state.scale
        )
        Surface(
            color = Color.Transparent,
            modifier = Modifier.matchParentSize()
        ) { }
    }
}

@Composable
internal fun AnimatedBorder(
    modifier: Modifier,
    alpha: Float,
    scale: Float
) {
    val transition: InfiniteTransition = rememberInfiniteTransition()

    // Infinite phase animation for PathEffect
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 80f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1000,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        )
    )

    val pathEffect = PathEffect.dashPathEffect(
        intervals = floatArrayOf(20f, 20f),
        phase = phase
    )

    val colorScheme = MaterialTheme.colorScheme
    Canvas(modifier = modifier) {
        drawRect(
            size = size,
            color = colorScheme.primary.copy(alpha),
            style = Stroke(
                width = 2.dp.toPx() * (1f / scale)
            )
        )
        drawRect(
            size = size,
            color = colorScheme.primaryContainer.copy(alpha),
            style = Stroke(
                width = 2.dp.toPx() * (1f / scale),
                pathEffect = pathEffect
            )
        )
    }
}