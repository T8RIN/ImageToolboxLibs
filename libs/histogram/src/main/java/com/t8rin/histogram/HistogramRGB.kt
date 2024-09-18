package com.t8rin.histogram

import android.graphics.Bitmap
import android.net.Uri
import androidx.annotation.FloatRange
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.graphics.ColorUtils
import androidx.core.graphics.drawable.toBitmap
import coil.imageLoader
import coil.request.ImageRequest
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
    imageUri: Uri?,
    modifier: Modifier,
    bordersColor: Color = MaterialTheme.colorScheme.outline,
    bordersShape: Shape = RoundedCornerShape(2.dp)
) {
    HistogramRGB(
        model = imageUri,
        modifier = modifier,
        bordersColor = bordersColor,
        bordersShape = bordersShape
    )
}

@Composable
fun HistogramRGB(
    model: Any?,
    modifier: Modifier,
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
        ).drawable?.toBitmap()
    }

    HistogramRGB(
        image = image,
        modifier = modifier,
        bordersColor = bordersColor,
        bordersShape = bordersShape
    )
}

@Composable
fun HistogramRGB(
    image: Bitmap?,
    modifier: Modifier,
    bordersColor: Color = MaterialTheme.colorScheme.outline,
    bordersShape: Shape = RoundedCornerShape(2.dp)
) {
    var histogramData by remember(image) {
        mutableStateOf(Default)
    }
    val (redData, greenData, blueData, whiteData) = histogramData

    LaunchedEffect(histogramData, image) {
        if (histogramData == Default) {
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
        val red = Color(0xFFE8362A).harmonizeWithPrimary()
        val green = Color(0xFF24EE2B).harmonizeWithPrimary()
        val blue = Color(0xFF2292EE).harmonizeWithPrimary()
        val white = Color.White.harmonizeWithPrimary(0.1f)
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
                .border(0.5.dp, bordersColor, bordersShape)
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

private fun Color.blend(
    color: Color,
    @FloatRange(from = 0.0, to = 1.0) fraction: Float
): Color = Color(ColorUtils.blendARGB(this.toArgb(), color.toArgb(), fraction))

@Composable
private fun Color.harmonizeWithPrimary(
    @FloatRange(
        from = 0.0,
        to = 1.0
    ) fraction: Float = 0.25f
): Color = blend(MaterialTheme.colorScheme.primary, fraction)