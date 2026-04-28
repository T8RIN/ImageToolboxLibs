package com.yalantis.ucrop.compose

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import com.yalantis.ucrop.R
import java.util.Locale
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.floor

private const val MaxAngle = 45f
private const val DeltaAngle = 5f
private const val HapticAngleStep = 2.5f

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HorizontalWheelSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    onStart: () -> Unit = {},
    onEnd: (Float) -> Unit = {},
    onRotate90: () -> Unit = {},
    onFlip: () -> Unit = {},
    config: HorizontalWheelSliderConfig,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    val hapticFeedback = LocalHapticFeedback.current
    val density = LocalDensity.current
    val densityValue = density.density
    val onValueChangeState by rememberUpdatedState(onValueChange)
    val onStartState by rememberUpdatedState(onStart)
    val onEndState by rememberUpdatedState(onEnd)
    val degreesTextPaint = remember(density) {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = with(density) { ceil(14.dp.toPx()) }
            color = Color.White.toArgb()
        }
    }

    var rotation by remember { mutableFloatStateOf(value) }
    var isDragging by remember { mutableStateOf(false) }

    LaunchedEffect(value, isDragging) {
        if (!isDragging) {
            rotation = value
        }
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 400.dp)
                .fillMaxWidth()
                .fillMaxHeight(),
            contentAlignment = Alignment.Center
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(densityValue) {
                        var dragStartX = 0f
                        var dragStartRotation = 0f
                        detectDragGestures(
                            onDragStart = { startOffset ->
                                isDragging = true
                                dragStartX = startOffset.x
                                dragStartRotation = rotation
                                onStartState()
                            },
                            onDragEnd = {
                                isDragging = false
                                onEndState(rotation)
                            },
                            onDragCancel = {
                                isDragging = false
                                onEndState(rotation)
                            },
                            onDrag = { change, _ ->
                                change.consume()
                                val currentX = change.position.x
                                val delta = dragStartX - currentX
                                val oldRotation = rotation
                                val clampedRotation =
                                    (dragStartRotation + delta / densityValue / PI.toFloat() / 1.65f)
                                        .coerceIn(-MaxAngle, MaxAngle)

                                if (shouldPerformHapticFeedback(oldRotation, clampedRotation)) {
                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                }
                                if (abs(clampedRotation - oldRotation) > 0.001f) {
                                    val newRotation = if (abs(clampedRotation) < 0.05f) {
                                        0f
                                    } else {
                                        clampedRotation
                                    }
                                    rotation = newRotation
                                    onValueChangeState(newRotation)
                                }
                            }
                        )
                    }
            ) {
                drawRotationWheel(
                    rotation = rotation,
                    centerLineColor = colorScheme.primary,
                    sideLineColor = Color.White,
                    degreesTextPaint = degreesTextPaint
                )
            }

            WheelIconButton(
                imageVector = config.mirrorIcon
                    ?: ImageVector.vectorResource(R.drawable.msg_photo_flip),
                onClick = onFlip,
                onLongClick = {},
                modifier = Modifier.align(Alignment.CenterStart)
            )
            WheelIconButton(
                imageVector = config.rotate90Icon
                    ?: ImageVector.vectorResource(R.drawable.msg_photo_rotate),
                onClick = onRotate90,
                onLongClick = {
                    rotation = 0f
                    onValueChangeState(0f)
                },
                modifier = Modifier.align(Alignment.CenterEnd)
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun WheelIconButton(
    imageVector: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onLongClick: (() -> Unit)? = null
) {
    Box(
        modifier = modifier
            .size(width = 70.dp, height = 64.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = onLongClick
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = imageVector,
                contentDescription = null,
                tint = Color.White
            )
        }
    }
}

private fun DrawScope.drawRotationWheel(
    rotation: Float,
    centerLineColor: Color,
    sideLineColor: Color,
    degreesTextPaint: Paint
) {
    val angle = -rotation * 2f
    val delta = angle % DeltaAngle
    val segments = floor(angle / DeltaAngle).toInt()

    for (i in 0..15) {
        val positiveLineColor = if (i < segments || i == 0 && delta < 0f) {
            centerLineColor
        } else {
            sideLineColor
        }
        drawWheelLine(
            index = i,
            delta = delta,
            isCenter = i == segments || i == 0 && segments == -1,
            lineColor = positiveLineColor,
            centerLineColor = centerLineColor
        )

        if (i != 0) {
            val negativeIndex = -i
            val negativeLineColor = if (negativeIndex > segments) {
                centerLineColor
            } else {
                sideLineColor
            }
            drawWheelLine(
                index = negativeIndex,
                delta = delta,
                isCenter = negativeIndex == segments + 1,
                lineColor = negativeLineColor,
                centerLineColor = centerLineColor
            )
        }
    }

    val centerIndicatorWidth = dp(2.5f)
    val centerIndicatorHeight = dp(22f)
    drawRoundRect(
        color = centerLineColor,
        topLeft = Offset(
            x = (size.width - centerIndicatorWidth) / 2f,
            y = (size.height - centerIndicatorHeight) / 2f
        ),
        size = Size(centerIndicatorWidth, centerIndicatorHeight),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(dp(2f), dp(2f))
    )

    drawIntoCanvas { canvas ->
        val degreesText = rotation.degreesText()
        canvas.nativeCanvas.drawText(
            degreesText,
            (size.width - degreesTextPaint.measureText(degreesText)) / 2f,
            dp(14f),
            degreesTextPaint
        )
    }
}

private fun DrawScope.drawWheelLine(
    index: Int,
    delta: Float,
    isCenter: Boolean,
    lineColor: Color,
    centerLineColor: Color
) {
    val radius = (size.width / 2f - dp(70f)).toInt().toFloat()
    if (radius <= 0f) return

    val angle = 90f - (index * DeltaAngle + delta)
    val value = (radius * cos(Math.toRadians(angle.toDouble())).toFloat()).toInt().toFloat()
    val x = size.width / 2f + value
    val fade = abs(value) / radius
    val alpha = ((1f - fade * fade) * 255f).toInt().coerceIn(0, 255) / 255f
    val width = if (isCenter) 4f else 2f
    val height = if (isCenter) dp(16f) else dp(12f)

    drawRect(
        color = (if (isCenter) centerLineColor else lineColor).copy(alpha = alpha),
        topLeft = Offset(
            x = x - width / 2f,
            y = (size.height - height) / 2f
        ),
        size = Size(width, height)
    )
}

private fun shouldPerformHapticFeedback(oldRotation: Float, newRotation: Float): Boolean {
    return abs(newRotation - MaxAngle) < 0.001f && abs(oldRotation - MaxAngle) >= 0.001f ||
            abs(newRotation + MaxAngle) < 0.001f && abs(oldRotation + MaxAngle) >= 0.001f ||
            floor(oldRotation / HapticAngleStep) != floor(newRotation / HapticAngleStep)
}

private fun Float.degreesText(): String {
    val displayValue = if (abs(this) < 0.1f - 0.001f) abs(this) else this
    return String.format(Locale.getDefault(), "%.1f\u00BA", displayValue)
}

private fun DrawScope.dp(value: Float): Float {
    if (value == 0f) {
        return 0f
    }
    return ceil(value.dp.toPx())
}
