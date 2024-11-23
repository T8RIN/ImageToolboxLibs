package com.t8rin.editbox

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
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastCoerceIn
import kotlin.math.absoluteValue
import kotlin.math.cos
import kotlin.math.sin


@Composable
fun BoxWithConstraintsScope.EditBox(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onTap: () -> Unit,
    state: EditBoxState = remember { EditBoxState() },
    content: @Composable () -> Unit
) {
    val borderAlpha by animateFloatAsState(if (enabled) 1f else 0f)

    var contentSize by remember {
        mutableStateOf(IntSize.Zero)
    }

    val transformState = rememberTransformableState { zoomChange, offsetChange, rotationChange ->
        state.rotation += rotationChange
        state.scale = (state.scale * zoomChange).fastCoerceIn(0.5f, 10f)
        val panChange = (offsetChange * state.scale).rotateBy(state.rotation)

        val extraWidth = (constraints.maxWidth - contentSize.width * state.scale).absoluteValue
        val extraHeight = (constraints.maxHeight - contentSize.height * state.scale).absoluteValue

        val maxX = extraWidth / 2
        val maxY = extraHeight / 2

        state.offset = Offset(
            x = (state.offset.x + panChange.x).coerceIn(-maxX, maxX),
            y = (state.offset.y + panChange.y).coerceIn(-maxY, maxY),
        )
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
            .transformable(
                state = transformState,
                enabled = enabled
            )
            .then(
                if (!enabled) {
                    Modifier.pointerInput(onTap) {
                        detectTapGestures {
                            onTap()
                        }
                    }
                } else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        content()
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

class EditBoxState(
    scale: Float = 1f,
    rotation: Float = 0f,
    offset: Offset = Offset.Zero
) {
    var scale by mutableFloatStateOf(scale)
        internal set

    var rotation by mutableFloatStateOf(rotation)
        internal set

    var offset by mutableStateOf(offset)
        internal set
}


internal fun Offset.rotateBy(
    angle: Float
): Offset {
    val angleInRadians = ROTATION_CONST * angle
    val newX = x * cos(angleInRadians) - y * sin(angleInRadians)
    val newY = x * sin(angleInRadians) + y * cos(angleInRadians)
    return Offset(newX, newY)
}

internal const val ROTATION_CONST = (Math.PI / 180f).toFloat()

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