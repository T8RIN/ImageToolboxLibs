package com.t8rin.curves

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.PointF
import android.view.TextureView
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.t8rin.curves.view.PhotoFilterCurvesControl
import com.t8rin.curves.view.PhotoFilterCurvesControl.CurvesValue
import jp.co.cyberagent.android.gpuimage.GLTextureView
import jp.co.cyberagent.android.gpuimage.GPUImage
import jp.co.cyberagent.android.gpuimage.filter.GPUImageFilter
import jp.co.cyberagent.android.gpuimage.filter.GPUImageToneCurveFilter


@Composable
fun ImageCurvesEditor(
    bitmap: Bitmap?,
    state: ImageCurvesEditorState = remember {
        ImageCurvesEditorState.Default
    },
    imageObtainingTrigger: Boolean,
    onImageObtained: (Bitmap) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    curvesSelectionText: @Composable ColumnScope.(curveType: Int) -> Unit = {},
    colors: ImageCurvesEditorColors = ImageCurvesEditorDefaults.Colors,
    placeControlsAtTheEnd: Boolean = false
) {
    val context = LocalContext.current as Activity

    AnimatedContent(
        targetState = bitmap,
        transitionSpec = { fadeIn() togetherWith fadeOut() }
    ) { image ->
        if (image != null) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = modifier
            ) {
                var imageHeight by remember(image) {
                    mutableFloatStateOf(image.height.toFloat())
                }
                var imageWidth by remember(image) {
                    mutableFloatStateOf(image.width.toFloat())
                }
                var imageOffset by remember(image) {
                    mutableStateOf(Offset.Zero)
                }
                var textureView by remember(image) {
                    mutableStateOf<TextureView?>(null)
                }

                val gpuImage by remember(context, image) {
                    mutableStateOf(
                        GPUImage(context).apply {
                            setImage(image)
                            setFilter(state.buildFilter())
                        }
                    )
                }

                LaunchedEffect(imageObtainingTrigger, gpuImage) {
                    if (imageObtainingTrigger) {
                        onImageObtained(gpuImage.bitmapWithFilterApplied)
                    }
                }

                var controlsPadding by remember {
                    mutableStateOf(0.dp)
                }

                val space = with(LocalDensity.current) {
                    1.dp.toPx()
                }
                AndroidView(
                    modifier = Modifier
                        .padding(contentPadding)
                        .then(
                            if (placeControlsAtTheEnd) Modifier.padding(end = controlsPadding)
                            else Modifier.padding(bottom = controlsPadding)
                        )
                        .aspectRatio(image.safeAspectRatio)
                        .onGloballyPositioned {
                            imageHeight = it.size.height.toFloat()
                            imageWidth = it.size.width.toFloat() - space
                            imageOffset = Offset(
                                x = it.positionInParent().x,
                                y = it.positionInParent().y
                            )
                        }
                        .clip(RoundedCornerShape(2.dp))
                        .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
                        .drawWithContent {
                            drawContent()
                            drawRect(
                                topLeft = Offset(
                                    x = size.width - space,
                                    y = 0f
                                ),
                                color = Color.Transparent,
                                blendMode = BlendMode.Clear
                            )
                        },
                    factory = {
                        GLTextureView(it).apply {
                            textureView = this
                            gpuImage.setGLTextureView(this)
                        }
                    }
                )
                AndroidView(
                    modifier = Modifier.matchParentSize(),
                    factory = {
                        PhotoFilterCurvesControl(
                            context = it,
                            value = state.curvesToolValue
                        ).apply {
                            setColors(
                                lumaCurveColor = colors.lumaCurveColor.toArgb(),
                                redCurveColor = colors.redCurveColor.toArgb(),
                                greenCurveColor = colors.greenCurveColor.toArgb(),
                                blueCurveColor = colors.blueCurveColor.toArgb(),
                                defaultCurveColor = colors.defaultCurveColor.toArgb(),
                                guidelinesColor = colors.guidelinesColor.toArgb()
                            )
                            setActualArea(imageOffset.x, imageOffset.y, imageWidth, imageHeight)
                            setDelegate {
                                gpuImage.setFilter(state.buildFilter())
                            }
                        }
                    },
                    update = {
                        it.setActualArea(imageOffset.x, imageOffset.y, imageWidth, imageHeight)
                        it.setDelegate {
                            gpuImage.setFilter(state.buildFilter())
                        }
                        it.setColors(
                            lumaCurveColor = colors.lumaCurveColor.toArgb(),
                            redCurveColor = colors.redCurveColor.toArgb(),
                            greenCurveColor = colors.greenCurveColor.toArgb(),
                            blueCurveColor = colors.blueCurveColor.toArgb(),
                            defaultCurveColor = colors.defaultCurveColor.toArgb(),
                            guidelinesColor = colors.guidelinesColor.toArgb()
                        )
                    }
                )

                val density = LocalDensity.current
                val controlsModifier = Modifier
                    .align(
                        if (placeControlsAtTheEnd) Alignment.CenterEnd
                        else Alignment.BottomCenter
                    )
                    .onGloballyPositioned {
                        controlsPadding = with(density) {
                            if (placeControlsAtTheEnd) {
                                it.size.width
                            } else {
                                it.size.height
                            }.toDp()
                        }
                    }

                if (placeControlsAtTheEnd) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(
                            space = 8.dp,
                            alignment = Alignment.CenterVertically
                        ),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = controlsModifier
                    ) {
                        val invalidations = remember {
                            mutableStateOf(0)
                        }

                        CurvesSelectionRadioButton(
                            state = state,
                            color = colors.lumaCurveColor,
                            type = PhotoFilterCurvesControl.CurvesToolValue.CurvesTypeLuminance,
                            curvesSelectionText = curvesSelectionText,
                            invalidations = invalidations
                        )
                        CurvesSelectionRadioButton(
                            state = state,
                            color = colors.redCurveColor,
                            type = PhotoFilterCurvesControl.CurvesToolValue.CurvesTypeRed,
                            curvesSelectionText = curvesSelectionText,
                            invalidations = invalidations
                        )
                        CurvesSelectionRadioButton(
                            state = state,
                            color = colors.greenCurveColor,
                            type = PhotoFilterCurvesControl.CurvesToolValue.CurvesTypeGreen,
                            curvesSelectionText = curvesSelectionText,
                            invalidations = invalidations
                        )
                        CurvesSelectionRadioButton(
                            state = state,
                            color = colors.blueCurveColor,
                            type = PhotoFilterCurvesControl.CurvesToolValue.CurvesTypeBlue,
                            curvesSelectionText = curvesSelectionText,
                            invalidations = invalidations
                        )
                    }
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(
                            space = 8.dp,
                            alignment = Alignment.CenterHorizontally
                        ),
                        modifier = controlsModifier
                    ) {
                        val invalidations = remember {
                            mutableStateOf(0)
                        }

                        CurvesSelectionRadioButton(
                            state = state,
                            color = colors.lumaCurveColor,
                            type = PhotoFilterCurvesControl.CurvesToolValue.CurvesTypeLuminance,
                            curvesSelectionText = curvesSelectionText,
                            invalidations = invalidations
                        )
                        CurvesSelectionRadioButton(
                            state = state,
                            color = colors.redCurveColor,
                            type = PhotoFilterCurvesControl.CurvesToolValue.CurvesTypeRed,
                            curvesSelectionText = curvesSelectionText,
                            invalidations = invalidations
                        )
                        CurvesSelectionRadioButton(
                            state = state,
                            color = colors.greenCurveColor,
                            type = PhotoFilterCurvesControl.CurvesToolValue.CurvesTypeGreen,
                            curvesSelectionText = curvesSelectionText,
                            invalidations = invalidations
                        )
                        CurvesSelectionRadioButton(
                            state = state,
                            color = colors.blueCurveColor,
                            type = PhotoFilterCurvesControl.CurvesToolValue.CurvesTypeBlue,
                            curvesSelectionText = curvesSelectionText,
                            invalidations = invalidations
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RowScope.CurvesSelectionRadioButton(
    state: ImageCurvesEditorState,
    color: Color,
    type: Int,
    invalidations: MutableState<Int>,
    curvesSelectionText: @Composable ColumnScope.(type: Int) -> Unit
) {
    CurvesSelectionRadioButtonImpl(
        modifier = Modifier.weight(1f, false),
        state = state,
        color = color,
        type = type,
        invalidations = invalidations,
        curvesSelectionText = curvesSelectionText
    )
}

@Composable
private fun ColumnScope.CurvesSelectionRadioButton(
    state: ImageCurvesEditorState,
    color: Color,
    type: Int,
    invalidations: MutableState<Int>,
    curvesSelectionText: @Composable ColumnScope.(type: Int) -> Unit
) {
    CurvesSelectionRadioButtonImpl(
        modifier = Modifier.weight(1f, false),
        state = state,
        color = color,
        type = type,
        invalidations = invalidations,
        curvesSelectionText = curvesSelectionText
    )
}

@Composable
private fun CurvesSelectionRadioButtonImpl(
    modifier: Modifier,
    state: ImageCurvesEditorState,
    color: Color,
    type: Int,
    invalidations: MutableState<Int>,
    curvesSelectionText: @Composable ColumnScope.(type: Int) -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clickable(
                indication = null,
                interactionSource = interactionSource
            ) {
                state.curvesToolValue.activeType = type
                invalidations.value++
            }
    ) {
        val isSelected by remember(invalidations.value) {
            mutableStateOf(state.curvesToolValue.activeType == type)
        }
        RadioButton(
            selected = isSelected,
            onClick = {
                state.curvesToolValue.activeType = type
                invalidations.value++
            },
            colors = RadioButtonDefaults.colors(
                selectedColor = color,
                unselectedColor = color
            ),
            interactionSource = interactionSource
        )
        CompositionLocalProvider(LocalContentColor provides color) {
            curvesSelectionText(type)
        }
    }
}

private fun ImageCurvesEditorState.buildFilter(): GPUImageFilter = GPUImageToneCurveFilter().apply {
    setRgbCompositeControlPoints(
        curvesToolValue.luminanceCurve.toPoints()
    )
    setRedControlPoints(
        curvesToolValue.redCurve.toPoints()
    )
    setGreenControlPoints(
        curvesToolValue.greenCurve.toPoints()
    )
    setBlueControlPoints(
        curvesToolValue.blueCurve.toPoints()
    )
    updateToneCurveTexture()
}

private fun CurvesValue.toPoints(): Array<PointF> = listOf(
    PointF(0.0f, blacksLevel / 100f),
    PointF(0.25f, shadowsLevel / 100f),
    PointF(0.5f, midtonesLevel / 100f),
    PointF(0.75f, highlightsLevel / 100f),
    PointF(1.0f, whitesLevel / 100f),
).toTypedArray()

private val Bitmap.aspectRatio: Float get() = width / height.toFloat()

private val Bitmap.safeAspectRatio: Float
    get() = aspectRatio
        .coerceAtLeast(0.005f)
        .coerceAtMost(1000f)