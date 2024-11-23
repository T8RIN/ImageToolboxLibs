package com.t8rin.editbox

import android.util.Log
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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.smarttoolfactory.gesture.pointerMotionEvents
import kotlin.math.abs


@Composable
fun EditBox(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    handleRadius: Dp = 10.dp,
    onTap: () -> Unit,
    state: EditBoxState = remember { EditBoxState() },
    content: @Composable () -> Unit
) {
    val borderAlpha by animateFloatAsState(if (enabled) 1f else 0f)
    DimensionSubcomposeLayout(
        modifier = modifier,
        mainContent = content,
        dependentContent = { intSize: IntSize ->
            val dpSize = with(LocalDensity.current) {
                val rawWidth = intSize.width.toDp()
                val rawHeight = intSize.height.toDp()
                DpSize(rawWidth, rawHeight)
            }

            ScaleEditBoxImpl(
                modifier = Modifier.size(dpSize),
                enabled = enabled,
                borderAlpha = borderAlpha,
                handleRadius = handleRadius,
                dpSize = dpSize,
                onTap = onTap,
                state = state,
                content = content
            )
        }
    )
}

class EditBoxState(
    xScaleInitial: Float,
    yScaleInitial: Float,
    xTranslationInitial: Float,
    yTranslationInitial: Float
) {
    constructor() : this(
        xScaleInitial = 1f,
        yScaleInitial = 1f,
        xTranslationInitial = 0f,
        yTranslationInitial = 0f
    )

    internal val _xScale = mutableFloatStateOf(xScaleInitial)
    val xScale: Float by _xScale

    internal val _yScale = mutableFloatStateOf(yScaleInitial)
    val yScale: Float by _yScale

    internal val _xTranslation = mutableFloatStateOf(xTranslationInitial)
    val xTranslation: Float by _xTranslation

    internal val _yTranslation = mutableFloatStateOf(yTranslationInitial)
    val yTranslation: Float by _yTranslation

    override fun toString(): String {
        return """
            EditBoxState(
                xScale = $xScale,
                yScale = $yScale,
                xTranslation = $xTranslation,
                yTranslation = $yTranslation
            )
        """.trimIndent()
    }
}

@Composable
private fun ScaleEditBoxImpl(
    modifier: Modifier = Modifier,
    enabled: Boolean,
    handleRadius: Dp,
    borderAlpha: Float,
    dpSize: DpSize,
    onTap: () -> Unit,
    state: EditBoxState,
    content: @Composable () -> Unit
) {
    val size = with(LocalDensity.current) {
        dpSize.toSize()
    }

    val xTranslation = state._xTranslation.floatValue * size.width
    val yTranslation = state._yTranslation.floatValue * size.height

    var xScale by remember { mutableStateOf(1f) }
    var yScale by remember { mutableStateOf(1f) }

    var angle by remember { mutableFloatStateOf(0f) }

    var touchRegion by remember(enabled) { mutableStateOf(TouchRegion.None) }

    val touchRegionRadius: Float
    val minDimension: Float

    with(LocalDensity.current) {
        touchRegionRadius = handleRadius.toPx()
        val minPx = 48.dp.toPx()
        // Minimum size 48 dp + diameter of a handle or width of 2 handles
        minDimension = (minPx + touchRegionRadius * 2)
            .coerceAtLeast(touchRegionRadius * 4)
    }

    // This is the real position of touch in screen, when scaled
    // right bottom corner of 1000x1000px Composable is always (1000,1000)
    // but we need screen coordinated to set draw rectangle after scaling and translating
    // rectDraw is drawn based on touch position on screen
    var positionActual by remember { mutableStateOf(Offset.Zero) }

    // Touch position for edge of the rectangle, used for not jumping to edge of rectangle
    // when user moves a handle. We set positionActual as position of selected handle
    // and using this distance as offset to not have a jump from touch position

    var distanceToEdgeFromTouch by remember { mutableStateOf(Offset.Zero) }

    Box(modifier) {
        val rectBounds by remember {
            val newSize = Size(
                width = size.width,
                height = size.height
            )
            val offset = Offset(
                state.xTranslation * newSize.width,
                state.yTranslation * newSize.height
            )
            mutableStateOf(
                Rect(
                    offset = offset,
                    size = newSize
                )
            )
        }

        var rectDraw by remember { mutableStateOf(rectBounds.copy()) }

        var rectTemp by remember { mutableStateOf(rectBounds.copy()) }

        val editModifier =
            Modifier
                .graphicsLayer {
                    translationX = xTranslation
                    translationY = yTranslation
                    scaleX = xScale
                    scaleY = yScale
                    rotationZ = angle
                }
                .then(
                    if (enabled) {
                        Modifier.pointerMotionEvents(
                            onDown = { change: PointerInputChange ->

                                if (enabled) {
                                    rectTemp = rectDraw.copy()

                                    val position = change.position

                                    val scaledX =
                                        rectDraw.left + position.x * rectDraw.width / rectBounds.width
                                    val scaledY =
                                        rectDraw.top + position.y * rectDraw.height / rectBounds.height

                                    positionActual = Offset(scaledX, scaledY)

                                    touchRegion = getTouchRegion(
                                        position = positionActual,
                                        rect = rectDraw,
                                        threshold = touchRegionRadius * 2
                                    )

                                    // This is the difference between touch position and edge
                                    // This is required for not moving edge of draw rect to touch position on move
                                    when (touchRegion) {
                                        TouchRegion.TopLeft -> {
                                            distanceToEdgeFromTouch =
                                                rectTemp.topLeft - positionActual
                                        }

                                        TouchRegion.TopRight -> {
                                            distanceToEdgeFromTouch =
                                                rectTemp.topRight - positionActual
                                        }

                                        TouchRegion.BottomLeft -> {
                                            distanceToEdgeFromTouch =
                                                rectTemp.bottomLeft - positionActual
                                        }

                                        TouchRegion.BottomRight -> {
                                            distanceToEdgeFromTouch =
                                                rectTemp.bottomRight - positionActual
                                        }

                                        else -> {
                                            distanceToEdgeFromTouch = Offset.Zero
                                        }
                                    }
                                }

                            },
                            onMove = { change: PointerInputChange ->

                                if (!enabled) return@pointerMotionEvents

                                val position = change.position
                                val scaledX =
                                    rectDraw.left + position.x * rectDraw.width / rectBounds.width +
                                            distanceToEdgeFromTouch.x
                                val scaledY =
                                    rectDraw.top + position.y * rectDraw.height / rectBounds.height +
                                            distanceToEdgeFromTouch.y


                                when (touchRegion) {
                                    TouchRegion.TopLeft -> {

                                        // Set position of top left while moving with top left handle and
                                        // limit position to not intersect other handles
                                        positionActual = Offset(
                                            scaledX.coerceAtMost(rectTemp.right - minDimension),
                                            scaledY.coerceAtMost(rectTemp.bottom - minDimension)
                                        )

                                        rectDraw = Rect(
                                            left = positionActual.x,
                                            top = positionActual.y,
                                            right = rectTemp.right,
                                            bottom = rectTemp.bottom
                                        )

                                        // TransformOrion is center of composable, by changing
                                        // it to 0,0 only for translation we move the composable
                                        // from left and top
                                        val horizontalCenter =
                                            (rectDraw.width - rectBounds.width) / 2
                                        val verticalCenter =
                                            (rectDraw.height - rectBounds.height) / 2

                                        // Scale is ratio of
                                        // rectangle drawn(It's the outer rectangle that covers handles)
                                        // to initial rectangle of this Composable
                                        xScale = rectDraw.width / rectBounds.width
                                        yScale = rectDraw.height / rectBounds.height

                                        state._xTranslation.floatValue =
                                            (positionActual.x + horizontalCenter) / size.width
                                        state._yTranslation.floatValue =
                                            (positionActual.y + verticalCenter) / size.height
                                    }

                                    TouchRegion.BottomLeft -> {

                                        // Set position of top left while moving with bottom left handle and
                                        // limit position to not intersect other handles
                                        positionActual = Offset(
                                            scaledX.coerceAtMost(rectTemp.right - minDimension),
                                            scaledY.coerceAtLeast(rectTemp.top + minDimension)
                                        )

                                        rectDraw = Rect(
                                            left = positionActual.x,
                                            top = rectTemp.top,
                                            right = rectTemp.right,
                                            bottom = positionActual.y,
                                        )

                                        // TransformOrion is center of composable, by changing
                                        // it to 0,0 only for translation we move the composable
                                        // from left and bottom
                                        val horizontalCenter =
                                            (rectDraw.width - rectBounds.width) / 2
                                        val verticalCenter =
                                            (rectDraw.height - rectBounds.height) / 2

                                        // Scale is ratio of
                                        // rectangle drawn(It's the outer rectangle that covers handles)
                                        // to initial rectangle of this Composable
                                        xScale = rectDraw.width / rectBounds.width
                                        yScale = rectDraw.height / rectBounds.height

                                        state._xTranslation.floatValue =
                                            (positionActual.x + horizontalCenter) / size.width
                                        state._yTranslation.floatValue =
                                            (positionActual.y - rectDraw.height + verticalCenter) / size.height
                                    }

                                    TouchRegion.TopRight -> {

                                        // Set position of top left while moving with top right handle and
                                        // limit position to not intersect other handles
                                        positionActual = Offset(
                                            scaledX.coerceAtLeast(rectTemp.left + minDimension),
                                            scaledY.coerceAtMost(rectTemp.bottom - minDimension)
                                        )

                                        rectDraw = Rect(
                                            left = rectTemp.left,
                                            top = positionActual.y,
                                            right = positionActual.x,
                                            bottom = rectTemp.bottom,
                                        )

                                        // TransformOrion is center of composable, by changing
                                        // it to 0,0 only for translation we move the composable
                                        // from top and right
                                        val horizontalCenter =
                                            (rectDraw.width - rectBounds.width) / 2
                                        val verticalCenter =
                                            (rectDraw.height - rectBounds.height) / 2

                                        // Scale is ratio of
                                        // rectangle drawn(It's the outer rectangle that covers handles)
                                        // to initial rectangle of this Composable
                                        xScale = rectDraw.width / rectBounds.width
                                        yScale = rectDraw.height / rectBounds.height

                                        state._xTranslation.floatValue =
                                            (positionActual.x - rectDraw.width + horizontalCenter) / size.width
                                        state._yTranslation.floatValue =
                                            (positionActual.y + verticalCenter) / size.height
                                    }

                                    TouchRegion.BottomRight -> {

                                        // Set position of top left while moving with bottom right handle and
                                        // limit position to not intersect other handles
                                        positionActual = Offset(
                                            scaledX.coerceAtLeast(rectTemp.left + minDimension),
                                            scaledY.coerceAtLeast(rectTemp.top + minDimension)
                                        )

                                        rectDraw = Rect(
                                            left = rectTemp.left,
                                            top = rectTemp.top,
                                            right = positionActual.x,
                                            bottom = positionActual.y,
                                        )

                                        // TransformOrion is center of composable, by changing
                                        // it to 0,0 only for translation we move the composable
                                        // from right and bottom
                                        val horizontalCenter =
                                            (rectDraw.width - rectBounds.width) / 2
                                        val verticalCenter =
                                            (rectDraw.height - rectBounds.height) / 2

                                        // Scale is ratio of
                                        // rectangle drawn(It's the outer rectangle that covers handles)
                                        // to initial rectangle of this Composable
                                        xScale = rectDraw.width / rectBounds.width
                                        yScale = rectDraw.height / rectBounds.height

                                        state._xTranslation.floatValue =
                                            (positionActual.x - rectDraw.width + horizontalCenter) / size.width
                                        state._yTranslation.floatValue =
                                            (positionActual.y - rectDraw.height + verticalCenter) / size.height
                                    }

                                    TouchRegion.Inside -> {
                                        val drag = change.positionChange()

                                        val scaledDragX = drag.x * rectDraw.width / rectBounds.width
                                        val scaledDragY =
                                            drag.y * rectDraw.height / rectBounds.height

                                        state._xTranslation.floatValue += scaledDragX / size.width
                                        state._yTranslation.floatValue += scaledDragY / size.height

                                        rectDraw = rectDraw.translate(scaledDragX, scaledDragY)
                                    }

                                    else -> Unit
                                }

                                if (touchRegion != TouchRegion.None) {
                                    change.consume()
                                }
                            },
                            onUp = {
                                touchRegion = TouchRegion.None
                                rectTemp = rectDraw.copy()
                            }
                        )
                    } else {
                        Modifier.pointerInput(onTap) {
                            detectTapGestures {
                                onTap()
                            }
                        }
                    }
                )
                // Padding is required to keep touch position of handles inside composable
                // without padding only 1 quarter of corner handles are in composable
                // padding is scaled because scale change also changes padding dimensions
                .padding(
                    horizontal = handleRadius / abs(xScale),
                    vertical = handleRadius / abs(yScale)
                )

        Box(
            modifier = editModifier
                .size(dpSize),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .graphicsLayer {
                        scaleX = 1 / xScale
                        scaleY = 1 / yScale
                    }
                    .scale(minOf(xScale, yScale))
            ) {
                content()
            }
        }

        EditBoxImpl(
            modifier = Modifier
                .size(dpSize),
            radius = touchRegionRadius,
            rectDraw = rectDraw,
            borderAlpha = borderAlpha
        )
    }
}

@Composable
internal fun EditBoxImpl(
    modifier: Modifier,
    radius: Float,
    rectDraw: Rect,
    borderAlpha: Float
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

    // This rect what we get after offsetting inside padding area to draw actual composable
    val rect = Rect(
        topLeft = Offset(
            rectDraw.topLeft.x + radius,
            rectDraw.topLeft.y + radius
        ),
        bottomRight = Offset(
            rectDraw.bottomRight.x - radius,
            rectDraw.bottomRight.y - radius
        )
    )

    val colorScheme = MaterialTheme.colorScheme
    Canvas(modifier = modifier) {
        drawRect(
            topLeft = rect.topLeft,
            size = rect.size,
            color = colorScheme.primary.copy(borderAlpha),
            style = Stroke(
                width = 2.dp.toPx()
            )
        )
        drawRect(
            topLeft = rect.topLeft,
            size = rect.size,
            color = colorScheme.primaryContainer.copy(borderAlpha),
            style = Stroke(
                width = 2.dp.toPx(),
                pathEffect = pathEffect
            )
        )

        drawBorderCircle(
            radius = radius,
            center = rect.topLeft,
            colorScheme = colorScheme,
            borderAlpha = borderAlpha
        )
        drawBorderCircle(
            radius = radius,
            center = rect.topRight,
            colorScheme = colorScheme,
            borderAlpha = borderAlpha
        )
        drawBorderCircle(
            radius = radius,
            center = rect.bottomLeft,
            colorScheme = colorScheme,
            borderAlpha = borderAlpha
        )
        drawBorderCircle(
            radius = radius,
            center = rect.bottomRight,
            colorScheme = colorScheme,
            borderAlpha = borderAlpha
        )
    }
}




