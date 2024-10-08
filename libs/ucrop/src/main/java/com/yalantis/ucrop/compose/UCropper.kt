package com.yalantis.ucrop.compose

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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableFloatState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp

@Composable
fun UCropper(
    imageModel: Any?,
    aspectRatio: Float?,
    modifier: Modifier = Modifier,
    containerModifier: Modifier = Modifier,
    hapticsStrength: Int = 1,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    isOverlayDraggable: Boolean = false,
    rotationAngleState: MutableFloatState = rememberSaveable(imageModel) {
        mutableFloatStateOf(0f)
    },
    croppingTrigger: Boolean,
    onCropped: (Uri) -> Unit,
    onLoadingStateChange: (Boolean) -> Unit = {}
) {
    var rotationAngle by rotationAngleState

    var isLoading by remember(imageModel) {
        mutableStateOf(true)
    }
    var isChangingValues by remember {
        mutableStateOf(false)
    }
    Box(containerModifier) {
        val direction = LocalLayoutDirection.current
        UCrop(
            imageModel = imageModel,
            rotationAngle = rotationAngle,
            aspectRatio = aspectRatio,
            modifier = modifier.fillMaxSize(),
            isOverlayDraggable = isOverlayDraggable,
            croppingTrigger = croppingTrigger,
            onCropped = {
                rotationAngle = 0f
                onCropped(it)
            },
            onLoadingStateChange = {
                onLoadingStateChange(it)
                isLoading = it
            },
            gridLinesCount = if (isChangingValues) 8 else 2,
            bottomPadding = 80.dp + contentPadding.calculateBottomPadding(),
            topPadding = 32.dp + contentPadding.calculateBottomPadding(),
            startPadding = 24.dp + contentPadding.calculateStartPadding(direction),
            endPadding = 24.dp + contentPadding.calculateEndPadding(direction)
        )
        AnimatedVisibility(
            visible = !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
        ) {
            HorizontalWheelSlider(
                value = rotationAngle,
                onValueChange = { rotationAngle = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(contentPadding)
                    .height(64.dp),
                hapticsStrength = hapticsStrength,
                onStart = {
                    isChangingValues = true
                },
                onEnd = {
                    isChangingValues = false
                },
                onFlip = {
                    CropCache.flip(
                        onLoadingStateChange = onLoadingStateChange
                    )
                },
                onRotate90 = {
                    CropCache.rotate90(
                        onLoadingStateChange = onLoadingStateChange,
                        onFinish = {
                            rotationAngle = 0f
                        }
                    )
                }
            )
        }
    }
}