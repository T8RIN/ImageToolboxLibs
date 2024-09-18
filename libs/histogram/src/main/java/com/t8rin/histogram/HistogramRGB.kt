package com.t8rin.histogram

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import coil.imageLoader
import coil.request.ImageRequest
import coil.util.DebugLogger
import ir.ehsannarmani.compose_charts.LineChart
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
fun HistogramRGB(
    imageUri: Uri,
    modifier: Modifier
) {
    val context = LocalContext.current
    var histogramData by remember(imageUri) {
        mutableStateOf(Default)
    }
    val (redData, greenData, blueData, whiteData) = histogramData

    LaunchedEffect(histogramData, imageUri) {
        if (histogramData == Default) {
            val image = context.imageLoader.newBuilder().logger(DebugLogger()).build().execute(
                ImageRequest.Builder(context)
                    .allowHardware(false)
                    .data(imageUri)
                    .size(1024)
                    .build()
            ).drawable?.toBitmap()


            image?.let { bitmap ->
                histogramData = Histogram.generateHistogram(bitmap).map { floats ->
                    floats.map { it.toDouble() }
                }
            }
        }
    }

    AnimatedVisibility(
        visible = histogramData != Default,
        modifier = modifier
    ) {
        val red = remember { Color(0xFFF44336) }
        val green = remember { Color(0xFF4CAF50) }
        val blue = remember { Color(0xFF1978C4) }
        val white = remember { Color.White }
        val alpha = 0.4f
        val topAlpha = 0.8f

        val duration = 300
        val gradientDuration = duration / 2L

        var showRgb by rememberSaveable {
            mutableStateOf(true)
        }

        val rgbData by remember(histogramData) {
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
                        drawStyle = DrawStyle.Stroke(.5.dp),
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
                        drawStyle = DrawStyle.Stroke(.5.dp),
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
                        drawStyle = DrawStyle.Stroke(.5.dp),
                        curvedEdges = true
                    )
                )
            }
        }
        val brightnessData by remember(histogramData) {
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
                        drawStyle = DrawStyle.Stroke(.5.dp),
                        curvedEdges = true
                    )
                )
            }
        }

        LineChart(
            data = if (showRgb) rgbData
            else brightnessData,
            labelHelperProperties = LabelHelperProperties(false),
            labelHelperPadding = 0.dp,
            indicatorProperties = HorizontalIndicatorProperties(false),
            labelProperties = LabelProperties(false),
            popupProperties = PopupProperties(false),
            zeroLineProperties = ZeroLineProperties(false),
            gridProperties = GridProperties(
                xAxisProperties = GridProperties.AxisProperties(
                    style = StrokeStyle.Dashed(),
                    color = SolidColor(MaterialTheme.colorScheme.outline)
                ),
                yAxisProperties = GridProperties.AxisProperties(
                    style = StrokeStyle.Dashed(),
                    color = SolidColor(MaterialTheme.colorScheme.outline)
                )
            ),
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures {
                        showRgb = !showRgb
                    }
                }
        )
    }
}

private val Default: List<List<Double>> by lazy {
    listOf(listOf(0.0, 0.0), listOf(0.0, 0.0), listOf(0.0, 0.0), listOf(0.0, 0.0))
}