package com.t8rin.histogram

import android.graphics.Bitmap
import android.net.Uri
import androidx.annotation.FloatRange
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.core.graphics.ColorUtils
import coil3.imageLoader
import coil3.request.ImageRequest
import coil3.request.allowHardware
import coil3.toBitmap
import ir.ehsannarmani.compose_charts.models.DrawStyle
import ir.ehsannarmani.compose_charts.models.GridProperties
import ir.ehsannarmani.compose_charts.models.HorizontalIndicatorProperties
import ir.ehsannarmani.compose_charts.models.LabelHelperProperties
import ir.ehsannarmani.compose_charts.models.LabelProperties
import ir.ehsannarmani.compose_charts.models.Line
import ir.ehsannarmani.compose_charts.models.PopupProperties
import ir.ehsannarmani.compose_charts.models.StrokeStyle
import ir.ehsannarmani.compose_charts.models.ZeroLineProperties

@Composable
fun ImageHistogram(
    imageUri: Uri?,
    modifier: Modifier,
    initialType: HistogramType = HistogramType.RGB,
    onSwapType: ((HistogramType) -> HistogramType)? = { type ->
        when (type) {
            HistogramType.RGB -> HistogramType.Brightness
            HistogramType.Brightness -> HistogramType.Camera
            HistogramType.Camera -> HistogramType.RGB
        }
    },
    harmonizationColor: Color = MaterialTheme.colorScheme.primary,
    linesThickness: Dp = 0.5.dp,
    bordersColor: Color = MaterialTheme.colorScheme.outline,
    bordersShape: Shape = RoundedCornerShape(2.dp)
) {
    ImageHistogram(
        model = imageUri,
        modifier = modifier,
        bordersColor = bordersColor,
        bordersShape = bordersShape,
        initialType = initialType,
        onSwapType = onSwapType,
        linesThickness = linesThickness,
        harmonizationColor = harmonizationColor
    )
}

@Composable
fun ImageHistogram(
    model: Any?,
    modifier: Modifier,
    initialType: HistogramType = HistogramType.RGB,
    onSwapType: ((HistogramType) -> HistogramType)? = { type ->
        when (type) {
            HistogramType.RGB -> HistogramType.Brightness
            HistogramType.Brightness -> HistogramType.Camera
            HistogramType.Camera -> HistogramType.RGB
        }
    },
    harmonizationColor: Color = MaterialTheme.colorScheme.primary,
    linesThickness: Dp = 0.5.dp,
    bordersColor: Color = MaterialTheme.colorScheme.outline,
    bordersShape: Shape = RoundedCornerShape(2.dp)
) {
    val context = LocalContext.current
    var image by remember {
        mutableStateOf<Bitmap?>(null)
    }
    LaunchedEffect(model) {
        image = context.imageLoader.execute(
            ImageRequest.Builder(context)
                .allowHardware(false)
                .data(model)
                .size(1024)
                .build()
        ).image?.toBitmap()
    }

    ImageHistogram(
        image = image,
        modifier = modifier,
        bordersColor = bordersColor,
        bordersShape = bordersShape,
        initialType = initialType,
        onSwapType = onSwapType,
        linesThickness = linesThickness,
        harmonizationColor = harmonizationColor
    )
}

@Composable
fun ImageHistogram(
    image: Bitmap?,
    modifier: Modifier,
    initialType: HistogramType = HistogramType.RGB,
    onSwapType: ((HistogramType) -> HistogramType)? = { type ->
        when (type) {
            HistogramType.RGB -> HistogramType.Brightness
            HistogramType.Brightness -> HistogramType.Camera
            HistogramType.Camera -> HistogramType.RGB
        }
    },
    harmonizationColor: Color = MaterialTheme.colorScheme.primary,
    linesThickness: Dp = 0.5.dp,
    bordersColor: Color = MaterialTheme.colorScheme.outline,
    bordersShape: Shape = RoundedCornerShape(2.dp)
) {
    var histogramType by rememberSaveable(initialType, onSwapType) {
        mutableStateOf(initialType)
    }
    var histogram by remember(image) {
        mutableStateOf(Histogram.Empty)
    }
    val (redData, greenData, blueData, whiteData) = histogram

    LaunchedEffect(histogram, image) {
        if (histogram == Histogram.Empty) {
            image?.let { bitmap ->
                histogram = Histogram.from(bitmap)
            }
        }
    }

    AnimatedVisibility(
        visible = histogram != Histogram.Empty,
        modifier = modifier
    ) {
        val red = if (histogramType != HistogramType.Camera) {
            Color(0xFFE51515).blend(harmonizationColor)
        } else Color.Red

        val green = if (histogramType != HistogramType.Camera) {
            Color(0xFF16D72F).blend(harmonizationColor)
        } else Color.Green

        val blue = if (histogramType != HistogramType.Camera) {
            Color(0xFF0067FF).blend(harmonizationColor)
        } else Color.Blue

        val white = Color.White.blend(harmonizationColor, 0.1f)
        val alpha = if (histogramType != HistogramType.Camera) 0.4f else 1f
        val topAlpha = if (histogramType != HistogramType.Camera) 0.8f else 1f

        val duration = 300
        val gradientDuration = duration / 2L

        val rgbData by remember(
            histogram,
            linesThickness,
            histogramType,
            red, green, blue
        ) {
            derivedStateOf {
                listOf(
                    Line(
                        label = "R",
                        values = redData,
                        color = SolidColor(red),
                        firstGradientFillColor = red.copy(topAlpha),
                        secondGradientFillColor = red.copy(alpha = alpha),
                        strokeAnimationSpec = tween(duration, easing = EaseInOutCubic),
                        gradientAnimationDelay = gradientDuration,
                        drawStyle = DrawStyle.Stroke(linesThickness),
                        curvedEdges = true
                    ),
                    Line(
                        label = "G",
                        values = greenData,
                        color = SolidColor(green),
                        firstGradientFillColor = green.copy(topAlpha),
                        secondGradientFillColor = green.copy(alpha = alpha),
                        strokeAnimationSpec = tween(duration, easing = EaseInOutCubic),
                        gradientAnimationDelay = gradientDuration,
                        drawStyle = DrawStyle.Stroke(linesThickness),
                        curvedEdges = true
                    ),
                    Line(
                        label = "B",
                        values = blueData,
                        color = SolidColor(blue),
                        firstGradientFillColor = blue.copy(topAlpha),
                        secondGradientFillColor = blue.copy(alpha = alpha),
                        strokeAnimationSpec = tween(duration, easing = EaseInOutCubic),
                        gradientAnimationDelay = gradientDuration,
                        drawStyle = DrawStyle.Stroke(linesThickness),
                        curvedEdges = true
                    )
                )
            }
        }
        val brightnessData by remember(histogram, histogramType, white) {
            derivedStateOf {
                listOf(
                    Line(
                        label = "W",
                        values = whiteData,
                        color = SolidColor(white),
                        firstGradientFillColor = white.copy(topAlpha),
                        secondGradientFillColor = white.copy(alpha = alpha),
                        strokeAnimationSpec = tween(duration, easing = EaseInOutCubic),
                        gradientAnimationDelay = gradientDuration,
                        drawStyle = DrawStyle.Stroke(linesThickness),
                        curvedEdges = true
                    )
                )
            }
        }

        AnimatedContent(
            targetState = histogramType,
            transitionSpec = {
                val spec1 = tween<IntOffset>(750)
                val spec2 = tween<Float>(750)
                if (targetState > initialState) {
                    slideInVertically(spec1) { height -> height / 3 } + fadeIn(spec2) togetherWith
                            slideOutVertically(spec1) { height -> -height / 3 } + fadeOut(spec2)
                } else {
                    slideInVertically(spec1) { height -> -height / 3 } + fadeIn(spec2) togetherWith
                            slideOutVertically(spec1) { height -> height / 3 } + fadeOut(spec2)
                }.using(
                    SizeTransform(clip = false)
                )
            }
        ) { type ->
            Box {
                val data = when (type) {
                    HistogramType.RGB -> rgbData
                    HistogramType.Brightness -> brightnessData
                    HistogramType.Camera -> rgbData
                }
                LineChart(
                    data = data,
                    labelHelperProperties = LabelHelperProperties(false),
                    labelHelperPadding = 0.dp,
                    indicatorProperties = HorizontalIndicatorProperties(false),
                    labelProperties = LabelProperties(false),
                    popupProperties = PopupProperties(false),
                    zeroLineProperties = ZeroLineProperties(false),
                    gridProperties = GridProperties(
                        xAxisProperties = GridProperties.AxisProperties(
                            style = StrokeStyle.Dashed(floatArrayOf(1f, 1f), 1f),
                            color = SolidColor(bordersColor.copy(0.5f))
                        ),
                        yAxisProperties = GridProperties.AxisProperties(
                            style = StrokeStyle.Dashed(floatArrayOf(1f, 1f), 1f),
                            color = SolidColor(bordersColor.copy(0.5f))
                        )
                    ),
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(bordersShape)
                        .border(linesThickness, bordersColor, bordersShape)
                        .then(
                            if (onSwapType != null) {
                                Modifier.pointerInput(Unit) {
                                    detectTapGestures {
                                        histogramType = onSwapType(type)
                                    }
                                }
                            } else Modifier
                        ),
                    isMultiply = histogramType == HistogramType.Camera,
                    maxValue = remember(data) {
                        derivedStateOf {
                            data.maxOfOrNull { line -> line.values.maxOfOrNull { it } ?: 0.0 }
                                ?: 0.0
                        }
                    }.value,
                    minValue = remember(data) {
                        derivedStateOf {
                            if (data.any { line -> line.values.any { it < 0.0 } }) data.minOfOrNull { line ->
                                line.values.minOfOrNull { it } ?: 0.0
                            } ?: 0.0 else 0.0
                        }
                    }.value
                )

                if (onSwapType == null) {
                    Surface(
                        modifier = Modifier.matchParentSize(),
                        color = Color.Transparent
                    ) { }
                }
            }
        }
    }
}

private fun Color.blend(
    color: Color,
    @FloatRange(from = 0.0, to = 1.0) fraction: Float = 0.25f
): Color = Color(ColorUtils.blendARGB(this.toArgb(), color.toArgb(), fraction))