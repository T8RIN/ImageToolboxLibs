package com.t8rin.histogram

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.LinearGradientShader
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
internal fun LineChart(
    modifier: Modifier = Modifier,
    data: List<HistogramLine>,
    gridProperties: HistogramGridProperties = HistogramGridProperties(),
    maxValue: Double = data.maxOfOrNull { line ->
        line.values.maxOfOrNull { it } ?: 0.0
    } ?: 0.0,
    minValue: Double = if (data.any { line -> line.values.any { it < 0.0 } }) {
        data.minOfOrNull { line -> line.values.minOfOrNull { it } ?: 0.0 } ?: 0.0
    } else {
        0.0
    },
    isMultiply: Boolean = false,
    animationDelay: Long = 300
) {
    if (data.isNotEmpty()) {
        require(data.none { line -> line.values.isEmpty() }) {
            "Chart data should not contain empty values"
        }
        require(minValue <= (data.minOfOrNull { line -> line.values.minOfOrNull { it } ?: 0.0 }
            ?: 0.0)) {
            "Chart data must be at least $minValue (Specified Min Value)"
        }
        require(maxValue >= (data.maxOfOrNull { line -> line.values.maxOfOrNull { it } ?: 0.0 }
            ?: 0.0)) {
            "Chart data must be at most $maxValue (Specified Max Value)"
        }
    }

    val pathMeasure = remember {
        PathMeasure()
    }

    LaunchedEffect(data) {
        data.forEach { line ->
            line.strokeProgress.snapTo(0f)
            line.gradientProgress.snapTo(0f)
        }

        delay(animationDelay)

        data.forEach { line ->
            launch {
                line.strokeProgress.animateTo(1f, animationSpec = line.strokeAnimationSpec)
            }
            launch {
                delay(line.gradientAnimationDelay)
                line.gradientProgress.animateTo(1f, animationSpec = line.gradientAnimationSpec)
            }
        }
    }

    Canvas(modifier = modifier) {
        if (gridProperties.enabled) {
            drawHistogramGrid(gridProperties)
        }

        data.forEach { line ->
            val pathData = getHistogramLinePath(
                dataPoints = line.values.map { it.toFloat() },
                maxValue = maxValue.toFloat(),
                minValue = minValue.toFloat(),
                rounded = line.curvedEdges
            )
            val segmentedPath = Path()

            pathMeasure.setPath(pathData.path, false)
            pathMeasure.getSegment(
                startDistance = 0f,
                stopDistance = pathMeasure.length * line.strokeProgress.value,
                destination = segmentedPath
            )

            drawLineGradient(
                path = pathData.path,
                color1 = line.firstGradientFillColor,
                color2 = line.secondGradientFillColor,
                progress = line.gradientProgress.value,
                startOffset = pathData.startOffset,
                endOffset = pathData.endOffset,
                isMultiply = isMultiply
            )
            drawPath(
                path = segmentedPath,
                brush = line.color,
                style = Stroke(
                    width = line.strokeWidth.toPx(),
                    pathEffect = line.strokeStyle.pathEffect
                ),
                blendMode = if (isMultiply) {
                    BlendMode.Lighten
                } else {
                    DrawScope.DefaultBlendMode
                }
            )
        }
    }
}

internal data class HistogramLine(
    val values: List<Double>,
    val color: Brush,
    val firstGradientFillColor: Color,
    val secondGradientFillColor: Color,
    val strokeAnimationSpec: AnimationSpec<Float> = tween(),
    val gradientAnimationSpec: AnimationSpec<Float> = tween(),
    val gradientAnimationDelay: Long = 0L,
    val strokeWidth: Dp = 1.dp,
    val strokeStyle: HistogramStrokeStyle = HistogramStrokeStyle.Solid,
    val curvedEdges: Boolean = true,
    val strokeProgress: Animatable<Float, AnimationVector1D> = Animatable(0f),
    val gradientProgress: Animatable<Float, AnimationVector1D> = Animatable(0f)
)

internal data class HistogramGridProperties(
    val enabled: Boolean = true,
    val xAxisProperties: AxisProperties = AxisProperties(),
    val yAxisProperties: AxisProperties = AxisProperties()
) {
    data class AxisProperties(
        val enabled: Boolean = true,
        val color: Brush = SolidColor(Color.Gray),
        val style: HistogramStrokeStyle = HistogramStrokeStyle.Solid,
        val thickness: Dp = 1.dp
    )
}

internal sealed interface HistogramStrokeStyle {
    val pathEffect: PathEffect?

    data object Solid : HistogramStrokeStyle {
        override val pathEffect: PathEffect? = null
    }

    class Dashed(
        intervals: FloatArray,
        phase: Float = 0f
    ) : HistogramStrokeStyle {
        override val pathEffect: PathEffect = PathEffect.dashPathEffect(intervals, phase)
    }
}

private data class HistogramPathData(
    val path: Path,
    val startOffset: Float,
    val endOffset: Float
)


private fun DrawScope.drawHistogramGrid(
    properties: HistogramGridProperties
) {
    drawAxisGridLines(
        properties = properties.xAxisProperties,
        count = HistogramGridDefaults.VERTICAL_LINES,
        drawLine = { position, axisProperties ->
            val x = size.width * position
            drawLine(
                brush = axisProperties.color,
                start = Offset(x = x, y = 0f),
                end = Offset(x = x, y = size.height),
                strokeWidth = axisProperties.thickness.toPx(),
                pathEffect = axisProperties.style.pathEffect
            )
        }
    )
    drawAxisGridLines(
        properties = properties.yAxisProperties,
        count = HistogramGridDefaults.HORIZONTAL_LINES,
        drawLine = { position, axisProperties ->
            val y = size.height * position
            drawLine(
                brush = axisProperties.color,
                start = Offset(x = 0f, y = y),
                end = Offset(x = size.width, y = y),
                strokeWidth = axisProperties.thickness.toPx(),
                pathEffect = axisProperties.style.pathEffect
            )
        }
    )
}

private fun DrawScope.drawAxisGridLines(
    properties: HistogramGridProperties.AxisProperties,
    count: Int,
    drawLine: DrawScope.(Float, HistogramGridProperties.AxisProperties) -> Unit
) {
    if (!properties.enabled || count <= 0) return

    repeat(count + 1) { index ->
        drawLine(index.toFloat() / count, properties)
    }
}

private fun DrawScope.getHistogramLinePath(
    dataPoints: List<Float>,
    maxValue: Float,
    minValue: Float,
    rounded: Boolean
): HistogramPathData {
    val path = Path()
    if (dataPoints.isEmpty()) {
        return HistogramPathData(
            path = path,
            startOffset = 0f,
            endOffset = size.width
        )
    }

    if (dataPoints.size == 1) {
        val y = size.height - calculateHeightOffset(
            maxValue = maxValue,
            minValue = minValue,
            total = size.height,
            value = dataPoints.first()
        )
        path.moveTo(0f, y)
        path.lineTo(size.width, y)
        return HistogramPathData(
            path = path,
            startOffset = 0f,
            endOffset = size.width
        )
    }

    val xStep = size.width / (dataPoints.size - 1)
    var previousX = 0f
    var previousY = size.height - calculateHeightOffset(
        maxValue = maxValue,
        minValue = minValue,
        total = size.height,
        value = dataPoints.first()
    )

    path.moveTo(previousX, previousY)

    for (index in 1 until dataPoints.size) {
        val x = index * xStep
        val y = size.height - calculateHeightOffset(
            maxValue = maxValue,
            minValue = minValue,
            total = size.height,
            value = dataPoints[index]
        )

        if (rounded) {
            val centerX = (previousX + x) / 2f
            path.cubicTo(
                x1 = centerX,
                y1 = previousY,
                x2 = centerX,
                y2 = y,
                x3 = x,
                y3 = y
            )
        } else {
            path.lineTo(x, y)
        }

        previousX = x
        previousY = y
    }

    return HistogramPathData(
        path = path,
        startOffset = 0f,
        endOffset = size.width
    )
}

private fun DrawScope.drawLineGradient(
    path: Path,
    color1: Color,
    color2: Color,
    progress: Float,
    startOffset: Float,
    endOffset: Float,
    isMultiply: Boolean
) {
    drawIntoCanvas { canvas ->
        val fillPath = Path().apply {
            addPath(path)
            lineTo(endOffset, size.height)
            lineTo(startOffset, size.height)
            close()
        }
        val paint = Paint().apply {
            if (isMultiply) {
                blendMode = BlendMode.Lighten
            }
            shader = LinearGradientShader(
                from = Offset(0f, 0f),
                to = Offset(0f, size.height),
                colors = listOf(
                    color1.copy(alpha = color1.alpha * progress),
                    color2
                ),
                tileMode = TileMode.Mirror
            )
        }

        canvas.drawPath(fillPath, paint)
    }
}

private fun calculateHeightOffset(
    maxValue: Float,
    minValue: Float,
    total: Float,
    value: Float
): Float {
    val range = maxValue - minValue
    if (range == 0f) return 0f

    return ((value - minValue) / range) * total
}

private object HistogramGridDefaults {
    const val VERTICAL_LINES = 8
    const val HORIZONTAL_LINES = 4
}