package io.rektplorer64.pickerkt.ui.viewer.content

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.VectorConverter
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.consumeAllChanges
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import coil.compose.AsyncImage
import io.rektplorer64.pickerkt.ui.component.common.CircularHeroIconPlaceholder
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.roundToInt

private const val DEFAULT_INITIAL_SCALE = 1f
private const val DEFAULT_MIN_ZOOM = 1f
private const val DEFAULT_MAX_ZOOM = 3f
private const val DEFAULT_INITIAL_ROTATION = 0f

data class ContentPreviewConfig(
    val initialZoomLevel: Float = DEFAULT_INITIAL_SCALE,
    val minZoomLevel: Float = DEFAULT_MIN_ZOOM,
    val maxZoomLevel: Float = DEFAULT_MAX_ZOOM,
    val initialRotationDegree: Float = DEFAULT_INITIAL_ROTATION,
    val initialPanOffset: Offset = Offset.Zero
)

private val Float.floatingErrorMargin: ClosedRange<Float>
    get() = (this - 0.02f)..(this + 0.02f)

private operator fun Float.compareTo(range: ClosedRange<Float>): Int =
    max(range.start.compareTo(this), this.compareTo(range.endInclusive))

/**
 * Displays an image while allowing user interactions such as pitching in/out, and panning.
 * TODO: Improve the gesture with different aspect ratios or sizes of images.
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ImageContentViewer(
    modifier: Modifier = Modifier,
    coilImageModel: Any?,
    config: ContentPreviewConfig = ContentPreviewConfig(),
    onClick: () -> Unit
) {

    val coroutineScope = rememberCoroutineScope()

    var height by remember { mutableStateOf(0) }
    var width by remember { mutableStateOf(0) }

    var doubleTapZoomOnce by remember { mutableStateOf(false) }
    var zoomLevel by remember { mutableStateOf(config.initialZoomLevel) }
    var rotation by remember { mutableStateOf(config.initialRotationDegree) }
    var panOffset by remember { mutableStateOf(config.initialPanOffset) }

    fun setOffset(newOffset: Offset) {
        if (zoomLevel < 1.05) return

        val newWidth = (width.toFloat() / 4)
        val newHeight = (height.toFloat() / 4)
        panOffset = newOffset.copy(
            x = newOffset.x.coerceIn(-newWidth, newWidth),
            y = newOffset.y.coerceIn(-newHeight, newHeight)
        )
    }

    fun setScale(newScale: Float) {
        zoomLevel = newScale.coerceIn(config.minZoomLevel, config.maxZoomLevel)
    }

    val animatableScale = remember { Animatable(1f) }.also { a ->
        LaunchedEffect(key1 = zoomLevel) {
            if (!a.isRunning) {
                a.snapTo(zoomLevel)
            }
        }
        LaunchedEffect(key1 = a.value) {
            zoomLevel = a.value
        }
    }

    val animatableRotation = remember { Animatable(0f) }.also { a ->
        LaunchedEffect(key1 = rotation) {
            if (!a.isRunning) {
                a.snapTo(rotation)
            }
        }
        LaunchedEffect(key1 = a.value) {
            rotation = a.value
        }
    }

    val animatablePanOffset =
        remember { Animatable(Offset.Zero, typeConverter = Offset.VectorConverter) }.also { a ->
            LaunchedEffect(key1 = panOffset) {
                if (!a.isRunning) {
                    a.snapTo(panOffset)
                }
            }
            LaunchedEffect(key1 = a.value) {
                panOffset = a.value
            }
        }

    fun resetState() {
        coroutineScope.launch {
            launch { animatableScale.animateTo(config.initialZoomLevel) }
            launch { animatableRotation.animateTo(config.initialRotationDegree) }
            launch { animatablePanOffset.animateTo(config.initialPanOffset) }
        }
    }

    val state = rememberTransformableState { zoomChange, panChange, rotationChange ->
        setScale(zoomLevel * zoomChange)
        rotation = (rotation + rotationChange) % 360f
        setOffset(panOffset + panChange)
    }

    Box(
        modifier = modifier
            .pointerInput(zoomLevel) {
                if (zoomLevel >= config.initialZoomLevel.floatingErrorMargin) {
                    detectDragGestures { change, dragAmount ->
                        change.consumeAllChanges()
                        setOffset(
                            panOffset + dragAmount.copy(
                                x = dragAmount.x * (zoomLevel.roundToInt()),
                                y = dragAmount.y * (zoomLevel.roundToInt())
                            )
                        )
                    }
                }
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = {
                        if (!doubleTapZoomOnce) {
                            coroutineScope.launch {
                                animatableScale.animateTo(
                                    (zoomLevel.roundToInt() * 2f).coerceIn(
                                        config.minZoomLevel,
                                        config.maxZoomLevel
                                    )
                                )
                                doubleTapZoomOnce = true
                            }
                        } else {
                            resetState()
                            doubleTapZoomOnce = false
                        }
                    },
                    onPress = {
                        awaitRelease()
                        animatableRotation.animateTo(0f)
                    },
                    onTap = { onClick() },
                    onLongPress = {

                    }
                )
            }
            .transformable(state),
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = coilImageModel,
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.Center)
                .wrapContentSize()
                .onGloballyPositioned {
                    height = it.size.height
                    width = it.size.width
                }
                .graphicsLayer {
                    scaleX = zoomLevel
                    scaleY = zoomLevel
                    rotationZ = rotation
                    translationX = panOffset.x
                    translationY = panOffset.y
                },
            loading = {
                CircularProgressIndicator()
            },
            error = {
                CircularHeroIconPlaceholder(heroIcon = Icons.Outlined.Image) {
                    Text(text = "Failed to load the image")
                }
            }
        )
    }
}