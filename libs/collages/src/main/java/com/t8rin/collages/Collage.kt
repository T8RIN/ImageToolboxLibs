package com.t8rin.collages

import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import com.t8rin.collages.view.FramePhotoLayout
import kotlin.math.min

@Composable
fun Collage(
    images: List<Uri>,
    modifier: Modifier = Modifier,
    spacing: Float = 0f,
    cornerRadius: Float = 0f,
    backgroundColor: Color = Color.White,
    onCollageCreated: (Bitmap) -> Unit,
    collageCreationTrigger: Boolean,
    collageType: CollageType,
    userInteractionEnabled: Boolean = true,
    aspectRatio: Float = 1f,
    outputScaleRatio: Float = 1.5f,
    onImageTap: ((index: Int, uri: Uri?) -> Unit)? = null
) {
    var previousSize by rememberSaveable {
        mutableIntStateOf(100)
    }
    var previousAspect by rememberSaveable {
        mutableFloatStateOf(aspectRatio)
    }
    var previousScale by rememberSaveable {
        mutableFloatStateOf(outputScaleRatio)
    }
    var previousImages by rememberSaveable {
        mutableStateOf(listOf<Uri>())
    }
    var previousCollageType by remember {
        mutableStateOf(collageType)
    }
    var needToInvalidate by remember {
        mutableStateOf(false)
    }
    val imagesMapped by remember(collageType, images) {
        derivedStateOf {
            collageType.templateItem?.photoItemList?.mapIndexed { index, item ->
                item.apply {
                    runCatching {
                        imagePath = images[index]
                    }
                }
            } ?: emptyList()
        }
    }

    LaunchedEffect(imagesMapped, previousImages, images, previousCollageType, collageType) {
        if (images != previousImages || previousCollageType != collageType) {
            needToInvalidate = true
            previousImages = images
            previousCollageType = collageType
        }
    }

    AnimatedVisibility(
        visible = collageType.templateItem != null,
        modifier = modifier,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        BoxWithConstraints {
            val size = this.constraints.run { min(maxWidth, maxHeight) }
            var viewInstance by remember {
                mutableStateOf<FramePhotoLayout?>(null)
            }
            var viewState by rememberSaveable {
                mutableStateOf(Bundle.EMPTY)
            }
            DisposableEffect(viewInstance) {
                viewInstance?.restoreInstanceState(viewState)

                onDispose {
                    viewState = Bundle()
                    viewInstance?.saveInstanceState(viewState)
                }
            }
            SideEffect {
                viewInstance?.setBackgroundColor(backgroundColor)
                viewInstance?.setSpace(spacing, cornerRadius)
            }
            AndroidView(
                factory = {
                    FramePhotoLayout(
                        context = it,
                        mPhotoItems = imagesMapped
                    ).apply {
                        val (width, height) = calculateDimensions(size, constraints, aspectRatio)
                        viewInstance = this
                        previousSize = size
                        previousAspect = aspectRatio
                        previousScale = outputScaleRatio
                        setBackgroundColor(backgroundColor)
                        setOnItemTapListener(onImageTap)
                        build(
                            viewWidth = width,
                            viewHeight = height,
                            outputScaleRatio = outputScaleRatio,
                            space = spacing,
                            corner = cornerRadius
                        )
                    }
                },
                update = {
                    if (previousSize != size || it.mPhotoItems != imagesMapped || needToInvalidate || previousAspect != aspectRatio || previousScale != outputScaleRatio) {
                        needToInvalidate = false
                        it.mPhotoItems = imagesMapped
                        it.setOnItemTapListener(onImageTap)
                        previousSize = size
                        previousAspect = aspectRatio
                        previousScale = outputScaleRatio

                        val (width, height) = calculateDimensions(size, constraints, aspectRatio)
                        it.build(
                            viewWidth = width,
                            viewHeight = height,
                            outputScaleRatio = outputScaleRatio,
                            space = 0f,
                            corner = 0f
                        )
                    }
                }
            )

            if (!userInteractionEnabled) {
                Surface(
                    color = Color.Transparent,
                    modifier = Modifier
                        .matchParentSize()
                        .zIndex(2f)
                ) { }
            }

            LaunchedEffect(viewInstance, collageCreationTrigger) {
                if (collageCreationTrigger) {
                    viewInstance?.createImage()?.let(onCollageCreated)
                }
            }
        }
    }
}

private fun calculateDimensions(
    size: Int,
    constraints: Constraints,
    aspectRatio: Float
): Pair<Int, Int> {
    return if (size == constraints.maxWidth) {
        val targetHeight =
            (size / aspectRatio).toDouble().coerceAtMost(constraints.maxHeight.toDouble()).toInt()
        val targetWidth = (targetHeight * aspectRatio).toInt()
        targetWidth to targetHeight
    } else {
        val targetWidth =
            (size * aspectRatio).toDouble().coerceAtMost(constraints.maxWidth.toDouble()).toInt()
        val targetHeight = (targetWidth / aspectRatio).toInt()
        targetWidth to targetHeight
    }
}