package com.t8rin.histogram

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
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
    val (blueData, greenData, redData) = histogramData

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
        val red = Color(0xFFF44336)
        val green = Color(0xFF4CAF50)
        val blue = Color(0xFF1978C4)
        val alpha = 0.4f
        val topAlpha = 0.8f
        LineChart(
            data = listOf(
                Line(
                    label = "R",
                    values = redData,
                    color = SolidColor(red),
                    firstGradientFillColor = red.copy(topAlpha),
                    secondGradientFillColor = red.copy(alpha = alpha),
                    strokeAnimationSpec = tween(500, easing = EaseInOutCubic),
                    gradientAnimationDelay = 250,
                    drawStyle = DrawStyle.Stroke(.5.dp),
                    curvedEdges = true
                ),
                Line(
                    label = "G",
                    values = greenData,
                    color = SolidColor(green),
                    firstGradientFillColor = green.copy(topAlpha),
                    secondGradientFillColor = green.copy(alpha = alpha),
                    strokeAnimationSpec = tween(500, easing = EaseInOutCubic),
                    gradientAnimationDelay = 250,
                    drawStyle = DrawStyle.Stroke(.5.dp),
                    curvedEdges = true
                ),
                Line(
                    label = "B",
                    values = blueData,
                    color = SolidColor(blue),
                    firstGradientFillColor = blue.copy(topAlpha),
                    secondGradientFillColor = blue.copy(alpha = alpha),
                    strokeAnimationSpec = tween(500, easing = EaseInOutCubic),
                    gradientAnimationDelay = 250,
                    drawStyle = DrawStyle.Stroke(.5.dp),
                    curvedEdges = true
                )
            ),
            labelHelperProperties = LabelHelperProperties(false),
            labelHelperPadding = 0.dp,
            indicatorProperties = HorizontalIndicatorProperties(false),
            labelProperties = LabelProperties(false),
            popupProperties = PopupProperties(false),
            zeroLineProperties = ZeroLineProperties(false),
            gridProperties = GridProperties(
                xAxisProperties = GridProperties.AxisProperties(
                    style = StrokeStyle.Dashed()
                ),
                yAxisProperties = GridProperties.AxisProperties(
                    style = StrokeStyle.Dashed()
                )
            ),
            modifier = Modifier.fillMaxSize()
        )
    }
}

private val Default: List<List<Double>> by lazy {
    listOf(listOf(0.0, 0.0), listOf(0.0, 0.0), listOf(0.0, 0.0))
}