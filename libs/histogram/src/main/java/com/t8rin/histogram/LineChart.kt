package com.t8rin.histogram

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toSize
import ir.ehsannarmani.compose_charts.components.LabelHelper
import ir.ehsannarmani.compose_charts.extensions.drawGridLines
import ir.ehsannarmani.compose_charts.extensions.spaceBetween
import ir.ehsannarmani.compose_charts.extensions.split
import ir.ehsannarmani.compose_charts.models.AnimationMode
import ir.ehsannarmani.compose_charts.models.DividerProperties
import ir.ehsannarmani.compose_charts.models.DotProperties
import ir.ehsannarmani.compose_charts.models.DrawStyle
import ir.ehsannarmani.compose_charts.models.GridProperties
import ir.ehsannarmani.compose_charts.models.HorizontalIndicatorProperties
import ir.ehsannarmani.compose_charts.models.IndicatorPosition
import ir.ehsannarmani.compose_charts.models.LabelHelperProperties
import ir.ehsannarmani.compose_charts.models.LabelProperties
import ir.ehsannarmani.compose_charts.models.Line
import ir.ehsannarmani.compose_charts.models.PopupProperties
import ir.ehsannarmani.compose_charts.models.ZeroLineProperties
import ir.ehsannarmani.compose_charts.utils.calculateOffset
import ir.ehsannarmani.compose_charts.utils.calculateValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.floor
import kotlin.math.pow

private data class Popup(
    val properties: PopupProperties,
    val position: Offset,
    val value: Double
)

@Composable
internal fun LineChart(
    modifier: Modifier = Modifier,
    data: List<Line>,
    curvedEdges: Boolean = true,
    animationDelay: Long = 300,
    animationMode: AnimationMode = AnimationMode.Together(),
    dividerProperties: DividerProperties = DividerProperties(),
    gridProperties: GridProperties = GridProperties(),
    zeroLineProperties: ZeroLineProperties = ZeroLineProperties(),
    indicatorProperties: HorizontalIndicatorProperties = HorizontalIndicatorProperties(
        textStyle = TextStyle.Default,
        padding = 16.dp
    ),
    labelHelperProperties: LabelHelperProperties = LabelHelperProperties(),
    labelHelperPadding: Dp = 26.dp,
    textMeasurer: TextMeasurer = rememberTextMeasurer(),
    popupProperties: PopupProperties = PopupProperties(
        textStyle = TextStyle.Default.copy(
            color = Color.White,
            fontSize = 12.sp
        )
    ),
    dotsProperties: DotProperties = DotProperties(),
    labelProperties: LabelProperties = LabelProperties(enabled = false),
    maxValue: Double = data.maxOfOrNull { it.values.maxOfOrNull { it } ?: 0.0 } ?: 0.0,
    minValue: Double = if (data.any { it.values.any { it < 0.0 } }) data.minOfOrNull {
        it.values.minOfOrNull { it } ?: 0.0
    } ?: 0.0 else 0.0,
    isMultiply: Boolean = false
) {
    if (data.isNotEmpty()) {
        require(minValue <= data.minOf { it.values.minOf { it } }) {
            "Chart data must be at least $minValue (Specified Min Value)"
        }
        require(maxValue >= data.maxOf { it.values.maxOf { it } }) {
            "Chart data must be at most $maxValue (Specified Max Value)"
        }
        require(data.none { it.values.isEmpty() }) {
            "Chart data should not contain empty values"
        }
    }

    val density = LocalDensity.current
    val scope = rememberCoroutineScope()

    val pathMeasure = remember {
        PathMeasure()
    }

    val popupAnimation = remember {
        Animatable(0f)
    }

    val zeroLineAnimation = remember {
        Animatable(0f)
    }

    val dotAnimators = remember {
        mutableStateListOf<List<Animatable<Float, AnimationVector1D>>>()
    }
    val popups = remember {
        mutableStateListOf<Popup>()
    }
    val popupsOffsetAnimators = remember {
        mutableStateListOf<Pair<Animatable<Float, AnimationVector1D>, Animatable<Float, AnimationVector1D>>>()
    }
    val labelAreaHeight = remember {
        if (labelProperties.enabled) {
            if (labelProperties.labels.isNotEmpty()) {
                labelProperties.labels.maxOf {
                    textMeasurer.measure(
                        it,
                        style = labelProperties.textStyle
                    ).size.height
                } + (labelProperties.padding.value * density.density).toInt()
            } else {
                error("Labels enabled, but there is no label provided to show, disable labels or fill 'labels' parameter in LabelProperties")
            }
        } else {
            0
        }
    }



    LaunchedEffect(Unit) {
        if (zeroLineProperties.enabled) {
            zeroLineAnimation.snapTo(0f)
            zeroLineAnimation.animateTo(1f, animationSpec = zeroLineProperties.animationSpec)
        }
    }

    LaunchedEffect(data) {
        dotAnimators.clear()
        launch {
            data.forEach {
                val animators = mutableListOf<Animatable<Float, AnimationVector1D>>()
                it.values.forEach {
                    animators.add(Animatable(0f))
                }
                dotAnimators.add(animators)
            }
        }
    }

    LaunchedEffect(data) {
        delay(animationDelay)

        val animateStroke: suspend (Line) -> Unit = { line ->
            line.strokeProgress.animateTo(1f, animationSpec = line.strokeAnimationSpec)
        }
        val animateGradient: suspend (Line) -> Unit = { line ->
            delay(line.gradientAnimationDelay)
            line.gradientProgress.animateTo(1f, animationSpec = line.gradientAnimationSpec)
        }
        launch {
            data.forEachIndexed { index, line ->
                when (animationMode) {
                    is AnimationMode.OneByOne -> {
                        animateStroke(line)
                    }

                    is AnimationMode.Together -> {
                        launch {
                            delay(animationMode.delayBuilder(index))
                            animateStroke(line)
                        }
                    }
                }
            }
        }
        launch {
            data.forEachIndexed { index, line ->
                when (animationMode) {
                    is AnimationMode.OneByOne -> {
                        animateGradient(line)
                    }

                    is AnimationMode.Together -> {
                        launch {
                            delay(animationMode.delayBuilder(index))
                            animateGradient(line)
                        }
                    }
                }
            }
        }
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Column(modifier = modifier) {
            if (labelHelperProperties.enabled) {
                LabelHelper(
                    data = data.map { it.label to it.color },
                    textStyle = labelHelperProperties.textStyle
                )
                Spacer(modifier = Modifier.height(labelHelperPadding))
            }
            Row(modifier = Modifier.fillMaxSize()) {
                val paddingBottom = (labelAreaHeight / density.density).dp
                if (indicatorProperties.enabled) {
                    if (indicatorProperties.position == IndicatorPosition.Horizontal.Start) {
                        Indicators(
                            modifier = Modifier.padding(bottom = paddingBottom),
                            indicatorProperties = indicatorProperties,
                            minValue = minValue,
                            maxValue = maxValue
                        )
                        Spacer(modifier = Modifier.width(indicatorProperties.padding))
                    }
                }
                Canvas(modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
                    .pointerInput(data) {
                        detectDragGestures(
                            onDragEnd = {
                                scope.launch {
                                    popupAnimation.animateTo(0f, animationSpec = tween(500))
                                    popups.clear()
                                    popupsOffsetAnimators.clear()
                                }
                            },
                            onDrag = { change, amount ->
                                val _size = size
                                    .toSize()
                                    .copy(height = (size.height - labelAreaHeight).toFloat())
                                popups.clear()
                                data.forEach {
                                    val properties = it.popupProperties ?: popupProperties

                                    if (properties.enabled) {
                                        val positionX =
                                            (change.position.x).coerceIn(
                                                0f,
                                                size.width.toFloat()
                                            )
                                        val fraction = (positionX / size.width)
                                        val popupValue = getPopupValue(
                                            points = it.values,
                                            fraction = fraction.toDouble(),
                                            rounded = it.curvedEdges ?: curvedEdges,
                                            size = _size,
                                            minValue = minValue,
                                            maxValue = maxValue
                                        )
                                        popups.add(
                                            Popup(
                                                position = popupValue.offset,
                                                value = popupValue.calculatedValue,
                                                properties = properties
                                            )
                                        )
                                        // add popup offset animators
                                        if (popupsOffsetAnimators.count() < popups.count()) {
                                            repeat(popups.count() - popupsOffsetAnimators.count()) {
                                                popupsOffsetAnimators.add(
                                                    Animatable(0f) to Animatable(
                                                        0f
                                                    )
                                                )
                                            }
                                        }
                                    }
                                }
                                scope.launch {
                                    // animate popup (alpha)
                                    if (popupAnimation.value != 1f && !popupAnimation.isRunning) {
                                        popupAnimation.animateTo(1f, animationSpec = tween(500))
                                    }
                                }
                            }
                        )
                    }
                ) {
                    val chartAreaHeight = size.height - labelAreaHeight
                    val drawZeroLine = {
                        val zeroY = chartAreaHeight - calculateOffset(
                            minValue = minValue,
                            maxValue = maxValue,
                            total = chartAreaHeight,
                            value = 0f
                        ).toFloat()
                        drawLine(
                            brush = zeroLineProperties.color,
                            start = Offset(x = 0f, y = zeroY),
                            end = Offset(x = size.width * zeroLineAnimation.value, y = zeroY),
                            pathEffect = zeroLineProperties.style.pathEffect,
                            strokeWidth = zeroLineProperties.thickness.toPx()
                        )
                    }

                    if (labelProperties.enabled) {
                        labelProperties.labels.forEachIndexed { index, label ->
                            val measureResult =
                                textMeasurer.measure(label, style = labelProperties.textStyle)
                            drawText(
                                textLayoutResult = measureResult,
                                topLeft = Offset(
                                    (size.width - measureResult.size.width).spaceBetween(
                                        itemCount = labelProperties.labels.count(),
                                        index = index
                                    ),
                                    size.height - labelAreaHeight + labelProperties.padding.toPx()
                                )
                            )
                        }
                    }

                    drawGridLines(
                        dividersProperties = dividerProperties,
                        indicatorPosition = indicatorProperties.position,
                        xAxisProperties = gridProperties.xAxisProperties,
                        yAxisProperties = gridProperties.yAxisProperties,
                        size = size.copy(height = chartAreaHeight),
                        gridEnabled = gridProperties.enabled
                    )
                    if (zeroLineProperties.enabled && zeroLineProperties.zType == ZeroLineProperties.ZType.Under) {
                        drawZeroLine()
                    }
                    data.forEachIndexed { index, line ->
                        val path = getLinePath(
                            dataPoints = line.values.map { it.toFloat() },
                            maxValue = maxValue.toFloat(),
                            minValue = minValue.toFloat(),
                            rounded = line.curvedEdges ?: curvedEdges,
                            size = size.copy(height = chartAreaHeight)
                        )
                        val segmentedPath = Path()
                        pathMeasure.setPath(path, false)
                        pathMeasure.getSegment(
                            0f,
                            pathMeasure.length * line.strokeProgress.value,
                            segmentedPath
                        )
                        var pathEffect: PathEffect? = null
                        val stroke: Float = when (val drawStyle = line.drawStyle) {
                            is DrawStyle.Fill -> {
                                0f
                            }

                            is DrawStyle.Stroke -> {
                                pathEffect = drawStyle.strokeStyle.pathEffect
                                drawStyle.width.toPx()
                            }
                        }
                        drawPath(
                            path = segmentedPath,
                            brush = line.color,
                            style = Stroke(width = stroke, pathEffect = pathEffect),
                            blendMode = if (isMultiply) BlendMode.Lighten
                            else DrawScope.DefaultBlendMode
                        )
                        if (line.firstGradientFillColor != null && line.secondGradientFillColor != null) {
                            drawLineGradient(
                                path = path,
                                color1 = line.firstGradientFillColor!!,
                                color2 = line.secondGradientFillColor!!,
                                progress = line.gradientProgress.value,
                                size = size.copy(height = chartAreaHeight),
                                isMultiply = isMultiply
                            )
                        } else if (line.drawStyle is DrawStyle.Fill) {
                            var fillColor = Color.Unspecified
                            if (line.color is SolidColor) {
                                fillColor = (line.color as SolidColor).value
                            }
                            drawLineGradient(
                                path = path,
                                color1 = fillColor,
                                color2 = fillColor,
                                progress = 1f,
                                size = size.copy(height = chartAreaHeight),
                                isMultiply = isMultiply
                            )
                        }

                        if ((line.dotProperties?.enabled ?: dotsProperties.enabled)) {
                            drawDots(
                                dataPoints = line.values.mapIndexed { mapIndex, value ->
                                    (dotAnimators.getOrNull(
                                        index
                                    )?.getOrNull(mapIndex) ?: Animatable(0f)) to value.toFloat()
                                },
                                properties = line.dotProperties ?: dotsProperties,
                                linePath = segmentedPath,
                                maxValue = maxValue.toFloat(),
                                minValue = minValue.toFloat(),
                                pathMeasure = pathMeasure,
                                scope = scope,
                                size = size.copy(height = chartAreaHeight)
                            )
                        }
                    }
                    if (zeroLineProperties.enabled && zeroLineProperties.zType == ZeroLineProperties.ZType.Above) {
                        drawZeroLine()
                    }
                    popups.forEachIndexed { index, popup ->
                        drawPopup(
                            popup = popup,
                            nextPopup = popups.getOrNull(index + 1),
                            textMeasurer = textMeasurer,
                            scope = scope,
                            progress = popupAnimation.value,
                            offsetAnimator = popupsOffsetAnimators.getOrNull(index)
                        )
                    }
                }
                if (indicatorProperties.enabled) {
                    if (indicatorProperties.position == IndicatorPosition.Horizontal.End) {
                        Spacer(modifier = Modifier.width(indicatorProperties.padding))
                        Indicators(
                            modifier = Modifier.padding(bottom = paddingBottom),
                            indicatorProperties = indicatorProperties,
                            minValue = minValue,
                            maxValue = maxValue
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun Indicators(
    modifier: Modifier = Modifier,
    indicatorProperties: HorizontalIndicatorProperties,
    minValue: Double,
    maxValue: Double
) {
    Column(
        modifier = modifier
            .fillMaxHeight(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        split(
            count = indicatorProperties.count,
            minValue = minValue,
            maxValue = maxValue
        ).forEach {
            BasicText(
                text = indicatorProperties.contentBuilder(it),
                style = indicatorProperties.textStyle
            )
        }
    }
}

private fun DrawScope.drawPopup(
    popup: Popup,
    nextPopup: Popup?,
    textMeasurer: TextMeasurer,
    scope: CoroutineScope,
    progress: Float,
    offsetAnimator: Pair<Animatable<Float, AnimationVector1D>, Animatable<Float, AnimationVector1D>>? = null,
) {
    val offset = popup.position
    val popupProperties = popup.properties
    val measureResult = textMeasurer.measure(
        popupProperties.contentBuilder(popup.value),
        style = popupProperties.textStyle.copy(
            color = popupProperties.textStyle.color.copy(
                alpha = 1f * progress
            )
        )
    )
    var rectSize = measureResult.size.toSize()
    rectSize = rectSize.copy(
        width = (rectSize.width + (popupProperties.contentHorizontalPadding.toPx() * 2)),
        height = (rectSize.height + (popupProperties.contentVerticalPadding.toPx() * 2))
    )

    val conflictDetected =
        ((nextPopup != null) && offset.y in nextPopup.position.y - rectSize.height..nextPopup.position.y + rectSize.height) ||
                (offset.x + rectSize.width) > size.width


    val rectOffset = if (conflictDetected) {
        offset.copy(x = offset.x - rectSize.width)
    } else {
        offset
    }
    offsetAnimator?.also { (x, y) ->
        if (x.value == 0f || y.value == 0f) {
            scope.launch {
                x.snapTo(rectOffset.x)
                y.snapTo(rectOffset.y)
            }
        } else {
            scope.launch {
                x.animateTo(rectOffset.x)
            }
            scope.launch {
                y.animateTo(rectOffset.y)
            }
        }

    }
    if (offsetAnimator != null) {
        val animatedOffset = Offset(
            x = offsetAnimator.first.value,
            y = offsetAnimator.second.value
        )
        val rect = Rect(
            offset = animatedOffset,
            size = rectSize
        )
        drawPath(
            path = Path().apply {
                addRoundRect(
                    RoundRect(
                        rect = rect.copy(
                            top = rect.top,
                            left = rect.left,
                        ),
                        topLeft = CornerRadius(
                            if (conflictDetected) popupProperties.cornerRadius.toPx() else 0f,
                            if (conflictDetected) popupProperties.cornerRadius.toPx() else 0f
                        ),
                        topRight = CornerRadius(
                            if (!conflictDetected) popupProperties.cornerRadius.toPx() else 0f,
                            if (!conflictDetected) popupProperties.cornerRadius.toPx() else 0f
                        ),
                        bottomRight = CornerRadius(
                            popupProperties.cornerRadius.toPx(),
                            popupProperties.cornerRadius.toPx()
                        ),
                        bottomLeft = CornerRadius(
                            popupProperties.cornerRadius.toPx(),
                            popupProperties.cornerRadius.toPx()
                        ),
                    )
                )
            },
            color = popupProperties.containerColor,
            alpha = 1f * progress
        )
        drawText(
            textLayoutResult = measureResult,
            topLeft = animatedOffset.copy(
                x = animatedOffset.x + popupProperties.contentHorizontalPadding.toPx(),
                y = animatedOffset.y + popupProperties.contentVerticalPadding.toPx()
            )
        )
    }
}

internal fun DrawScope.drawDots(
    dataPoints: List<Pair<Animatable<Float, AnimationVector1D>, Float>>,
    properties: DotProperties,
    linePath: Path,
    maxValue: Float,
    minValue: Float,
    pathMeasure: PathMeasure,
    scope: CoroutineScope,
    size: Size? = null,
) {
    val _size = size ?: this.size

    val pathEffect = properties.strokeStyle.pathEffect

    pathMeasure.setPath(linePath, false)
    val lastPosition = pathMeasure.getPosition(pathMeasure.length)
    dataPoints.forEachIndexed { valueIndex, value ->
        val dotOffset = Offset(
            x = _size.width.spaceBetween(
                itemCount = dataPoints.count(),
                index = valueIndex
            ),
            y = (_size.height - calculateOffset(
                maxValue = maxValue.toDouble(),
                minValue = minValue.toDouble(),
                total = _size.height,
                value = value.second
            )).toFloat()

        )
        if (lastPosition != Offset.Unspecified && lastPosition.x >= dotOffset.x - 20 || !properties.animationEnabled) {
            if (!value.first.isRunning && properties.animationEnabled && value.first.value != 1f) {
                scope.launch {
                    value.first.animateTo(1f, animationSpec = properties.animationSpec)
                }
            }

            val radius: Float
            val strokeRadius: Float
            if (properties.animationEnabled) {
                radius =
                    (properties.radius.toPx() + properties.strokeWidth.toPx() / 2) * value.first.value
                strokeRadius = properties.radius.toPx() * value.first.value
            } else {
                radius = properties.radius.toPx() + properties.strokeWidth.toPx() / 2
                strokeRadius = properties.radius.toPx()
            }
            drawCircle(
                brush = properties.strokeColor,
                radius = radius,
                center = dotOffset,
                style = Stroke(width = properties.strokeWidth.toPx(), pathEffect = pathEffect),
            )
            drawCircle(
                brush = properties.color,
                radius = strokeRadius,
                center = dotOffset,
            )
        }
    }
}

internal data class Value(
    val calculatedValue: Double,
    val offset: Offset,
)

internal fun getPopupValue(
    points: List<Double>,
    fraction: Double,
    rounded: Boolean = false,
    size: Size,
    minValue: Double,
    maxValue: Double
): Value {
    val index = fraction * (points.count() - 1)
    val roundedIndex = floor(index).toInt()
    return if (fraction == 1.0) {
        val lastPoint = points.last()
        val offset = Offset(
            x = size.width,
            y = size.height - calculateOffset(
                minValue = minValue,
                maxValue = maxValue,
                total = size.height,
                value = lastPoint.toFloat()
            ).toFloat()
        )
        Value(calculatedValue = points.last(), offset = offset)
    } else {
        if (rounded) {
            val calculateHeight = { value: Double ->
                calculateOffset(
                    maxValue = maxValue,
                    minValue = minValue,
                    total = size.height,
                    value = value.toFloat()
                )
            }
            val x1 = (roundedIndex * (size.width / (points.size - 1)))
            val x2 = ((roundedIndex + 1) * (size.width / (points.size - 1)))
            val y1 = size.height - calculateHeight(points[roundedIndex])
            val y2 = size.height - calculateHeight(points[roundedIndex + 1])
            val cx = (x1 + x2) / 2f

            val areaFraction = roundedIndex.toDouble() / (points.size - 1)

            val t = (fraction - areaFraction) * (points.size - 1)

            val outputY = ((1 - t).pow(3) * (y1) +
                    3 * t * (1 - t).pow(2) * (y1) +
                    3 * (1 - t) * t.pow(2) * (y2) +
                    t.pow(3) * y2).toFloat()
            val outputX = ((1 - t).pow(3) * (x1) +
                    3 * t * (1 - t).pow(2) * (cx) +
                    3 * (1 - t) * t.pow(2) * (cx) +
                    t.pow(3) * x2).toFloat()
            val calculatedValue = calculateValue(
                minValue = minValue,
                maxValue = maxValue,
                total = size.height,
                offset = size.height - outputY
            )

            Value(calculatedValue = calculatedValue, offset = Offset(x = outputX, y = outputY))
        } else {
            val p1 = points[roundedIndex]
            val p2 = points[roundedIndex + 1]
            val calculatedValue = ((p2 - p1) * (index - roundedIndex) + p1)
            val offset = Offset(
                x = (fraction * size.width).toFloat(),
                y = size.height - calculateOffset(
                    minValue = minValue,
                    maxValue = maxValue,
                    total = size.height,
                    value = calculatedValue.toFloat()
                ).toFloat()
            )
            Value(calculatedValue = calculatedValue, offset = offset)
        }
    }
}

internal fun DrawScope.drawLineGradient(
    path: Path,
    color1: Color,
    color2: Color,
    progress: Float,
    isMultiply: Boolean,
    size: Size? = null
) {
    val _size = size ?: this.size
    drawIntoCanvas {
        val p = Path()
        p.addPath(path)
        p.lineTo(_size.width, _size.height)
        p.lineTo(0f, _size.height)
        p.close()
        val paint = Paint()
        if (isMultiply) {
            paint.blendMode = BlendMode.Lighten
        }
        paint.shader = LinearGradientShader(
            Offset(0f, 0f),
            Offset(0f, _size.height),
            listOf(
                color1.copy(alpha = color1.alpha * progress),
                color2.copy(alpha = color2.alpha * progress),
            ),
            tileMode = TileMode.Mirror
        )
        it.drawPath(p, paint)
    }
}

internal fun DrawScope.getLinePath(
    dataPoints: List<Float>,
    maxValue: Float,
    minValue: Float,
    rounded: Boolean = true,
    size: Size? = null
): Path {

    val _size = size ?: this.size
    val path = Path()

    val calculateHeight = { value: Float ->
        calculateOffset(
            maxValue = maxValue.toDouble(),
            minValue = minValue.toDouble(),
            total = _size.height,
            value = value
        )
    }

    path.moveTo(0f, (_size.height - calculateHeight(dataPoints[0])).toFloat())

    for (i in 0 until dataPoints.size - 1) {
        val x1 = (i * (_size.width / (dataPoints.size - 1)))
        val y1 = _size.height - calculateHeight(dataPoints[i]).toFloat()
        val x2 = ((i + 1) * (_size.width / (dataPoints.size - 1)))
        val y2 = _size.height - calculateHeight(dataPoints[i + 1]).toFloat()

        if (rounded) {
            val cx = (x1 + x2) / 2f
            path.cubicTo(x1 = cx, y1 = y1, x2 = cx, y2 = y2, x3 = x2, y3 = y2)
        } else {
            path.cubicTo(x1, y1, x1, y1, (x1 + x2) / 2, (y1 + y2) / 2)
            path.cubicTo((x1 + x2) / 2, (y1 + y2) / 2, x2, y2, x2, y2)
        }
    }
    return path
}