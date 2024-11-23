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
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
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

class EditBoxState(
    scale: Float = 1f,
    rotation: Float = 0f,
    offset: Offset = Offset.Zero,
    isActive: Boolean = false
) {
    var isActive by mutableStateOf(isActive)
        internal set

    fun activate() {
        isActive = true
    }

    fun deactivate() {
        isActive = false
    }

    var scale by mutableFloatStateOf(scale)
        internal set

    var rotation by mutableFloatStateOf(rotation)
        internal set

    var offset by mutableStateOf(offset)
        internal set

    private val IntSize.aspect: Float get() = width / height.toFloat()

    private val _canvasSize = mutableStateOf(IntSize.Zero)

    var canvasSize: IntSize
        get() = _canvasSize.value
        set(value) {
            if (_canvasSize.value != IntSize.Zero && _canvasSize.value != value) {
                val sx = value.width.toFloat() / _canvasSize.value.width
                val sy = value.height.toFloat() / _canvasSize.value.height
                if (_canvasSize.value.aspect < value.aspect) {
                    scale *= minOf(sx, sy)
                    offset *= minOf(sx, sy)
                } else {
                    scale /= minOf(sx, sy)
                    offset /= minOf(sx, sy)
                }
            }
            _canvasSize.value = value
        }
}

@Composable
fun BoxWithConstraintsScope.EditBox(
    onTap: () -> Unit,
    modifier: Modifier = Modifier,
    state: EditBoxState = remember { EditBoxState() },
    content: @Composable BoxScope.() -> Unit
) {
    EditBox(
        modifier = modifier,
        onTap = onTap,
        state = state,
        parentMaxWidth = constraints.maxWidth,
        parentMaxHeight = constraints.maxHeight,
        content = content
    )
}

@Composable
fun EditBox(
    onTap: () -> Unit,
    parentMaxWidth: Int,
    parentMaxHeight: Int,
    modifier: Modifier = Modifier,
    state: EditBoxState = remember { EditBoxState() },
    content: @Composable BoxScope.() -> Unit
) {
    var contentSize by remember {
        mutableStateOf(IntSize.Zero)
    }

    SideEffect {
        IntSize(
            width = parentMaxWidth,
            height = parentMaxHeight
        ).also {
            state.canvasSize = it
        }
    }

    val transformState = rememberTransformableState { zoomChange, offsetChange, rotationChange ->
        state.rotation += rotationChange
        state.scale = (state.scale * zoomChange).fastCoerceIn(0.5f, 10f)
        val panChange = (offsetChange * state.scale).rotateBy(state.rotation)

        val extraWidth = (parentMaxWidth - contentSize.width * state.scale).absoluteValue
        val extraHeight = (parentMaxHeight - contentSize.height * state.scale).absoluteValue

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