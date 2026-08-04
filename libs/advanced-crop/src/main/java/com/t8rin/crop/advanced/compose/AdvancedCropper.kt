package com.t8rin.crop.advanced.compose

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableFloatState
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import com.t8rin.crop.advanced.view.AdvancedCropView
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun AdvancedCropper(
    imageModel: Any?,
    aspectRatio: Float?,
    modifier: Modifier = Modifier,
    containerModifier: Modifier = Modifier,
    sliderConfig: HorizontalWheelSliderConfig = HorizontalWheelSliderConfig(),
    contentPadding: PaddingValues = PaddingValues(0.dp),
    isOverlayDraggable: Boolean = false,
    state: AdvancedCropperState = rememberAdvancedCropperState(),
    rotationAngleState: MutableFloatState = rememberSaveable(imageModel) {
        mutableFloatStateOf(0f)
    },
    croppingTrigger: Boolean,
    onCropped: (Uri) -> Unit,
    gridColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    handlesColor: Color = MaterialTheme.colorScheme.primaryFixed,
    oneFingerZoom: Boolean = true,
    onZoomChange: (Float) -> Unit = {},
    onLoadingStateChange: (Boolean) -> Unit = {},
    onTransformationCommitted: () -> Unit = {}
) {
    var rotationAngle by rotationAngleState
    val cropController = remember { CropController() }
    val scope = rememberCoroutineScope()
    val currentOnTransformationCommitted by rememberUpdatedState(onTransformationCommitted)
    var finishTransformationJob by remember { mutableStateOf<Job?>(null) }
    var cropView by remember(imageModel) {
        mutableStateOf<AdvancedCropView?>(null)
    }

    var isLoading by remember(imageModel) {
        mutableStateOf(true)
    }
    var isChangingValues by remember {
        mutableStateOf(false)
    }
    var wrapCropBoundsTrigger by remember {
        mutableIntStateOf(0)
    }
    var wrapCropBoundsAnimated by remember {
        mutableStateOf(false)
    }
    var resetZoomTrigger by remember {
        mutableIntStateOf(0)
    }
    var sourceRotationDegrees by rememberSaveable(imageModel) {
        mutableIntStateOf(0)
    }
    var isFlippedHorizontally by rememberSaveable(imageModel) {
        mutableStateOf(false)
    }
    SideEffect {
        state.attach(
            imageKey = imageModel,
            captureSnapshot = {
                AdvancedCropSnapshot(
                    rotationAngle = rotationAngle,
                    sourceRotationDegrees = sourceRotationDegrees,
                    isFlippedHorizontally = isFlippedHorizontally,
                    viewState = cropView?.takeUnless { isLoading }?.captureState()
                )
            },
            restoreSnapshot = { snapshot ->
                rotationAngle = snapshot.rotationAngle
                sourceRotationDegrees = snapshot.sourceRotationDegrees
                isFlippedHorizontally = snapshot.isFlippedHorizontally
                snapshot.viewState?.let { viewState ->
                    cropView?.restoreState(viewState)
                    scope.launch {
                        withFrameNanos { }
                        withFrameNanos { }
                        cropView?.restoreState(viewState)
                    }
                }
            }
        )
    }
    DisposableEffect(state) {
        onDispose {
            finishTransformationJob?.cancel()
            state.prepareForReattachment()
            state.detach()
        }
    }
    LaunchedEffect(isLoading, onLoadingStateChange) {
        onLoadingStateChange(isLoading)
        if (!isLoading) {
            withFrameNanos { }
            state.onViewReady()
        }
    }
    LaunchedEffect(state, state.resetVersion) {
        if (state.consumeReset()) {
            finishTransformationJob?.cancel()
            rotationAngle = 0f
            sourceRotationDegrees = 0
            isFlippedHorizontally = false
            resetZoomTrigger++
        }
    }
    fun beginTransformation() {
        finishTransformationJob?.cancel()
        state.beginTransformation()
    }

    fun commitTransformation() {
        finishTransformationJob?.cancel()
        if (state.endTransformation()) {
            currentOnTransformationCommitted()
        }
    }

    fun scheduleTransformationCommit() {
        finishTransformationJob?.cancel()
        finishTransformationJob = scope.launch {
            delay(350)
            commitTransformation()
        }
    }
    Box(containerModifier) {
        val direction = LocalLayoutDirection.current
        AdvancedCropImpl(
            controller = cropController,
            imageModel = imageModel,
            rotationAngle = rotationAngle,
            sourceRotationDegrees = sourceRotationDegrees,
            isFlippedHorizontally = isFlippedHorizontally,
            aspectRatio = aspectRatio,
            modifier = modifier.fillMaxSize(),
            isOverlayDraggable = isOverlayDraggable,
            isChangingValues = isChangingValues,
            wrapCropBoundsTrigger = wrapCropBoundsTrigger,
            wrapCropBoundsAnimated = wrapCropBoundsAnimated,
            resetZoomTrigger = resetZoomTrigger,
            croppingTrigger = croppingTrigger,
            onCropped = {
                rotationAngle = 0f
                onCropped(it)
            },
            onLoadingStateChange = {
                isLoading = it
            },
            onZoomChange = onZoomChange,
            oneFingerZoom = oneFingerZoom,
            gridLinesCount = if (isChangingValues) 8 else 2,
            bottomPadding = 80.dp + contentPadding.calculateBottomPadding(),
            topPadding = 32.dp + contentPadding.calculateTopPadding(),
            startPadding = 24.dp + contentPadding.calculateStartPadding(direction),
            endPadding = 24.dp + contentPadding.calculateEndPadding(direction),
            gridColor = gridColor,
            handlesColor = handlesColor,
            onViewChanged = { cropView = it },
            onTransformationStart = ::beginTransformation,
            onTransformationEnd = ::commitTransformation
        )
        AnimatedVisibility(
            visible = !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
        ) {
            HorizontalWheelSlider(
                value = rotationAngle,
                onValueChange = {
                    beginTransformation()
                    rotationAngle = it
                    wrapCropBoundsAnimated = false
                    wrapCropBoundsTrigger++
                    scheduleTransformationCommit()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(contentPadding)
                    .height(64.dp),
                config = sliderConfig,
                onStart = {
                    beginTransformation()
                    isChangingValues = true
                },
                onEnd = {
                    isChangingValues = false
                    wrapCropBoundsAnimated = true
                    wrapCropBoundsTrigger++
                    scheduleTransformationCommit()
                },
                onFlip = {
                    beginTransformation()
                    sourceRotationDegrees = normalizeCropRotation(-sourceRotationDegrees)
                    isFlippedHorizontally = !isFlippedHorizontally
                    scheduleTransformationCommit()
                },
                onRotate90 = {
                    beginTransformation()
                    sourceRotationDegrees = normalizeCropRotation(sourceRotationDegrees - 90)
                    rotationAngle = 0f
                    scheduleTransformationCommit()
                },
                onReset = {
                    beginTransformation()
                    sourceRotationDegrees = 0
                    rotationAngle = 0f
                    resetZoomTrigger++
                    scheduleTransformationCommit()
                }
            )
        }
    }
}
