package com.t8rin.collages

import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import com.t8rin.collages.frame.FramePhotoLayout
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
    userInteractionEnabled: Boolean = true
) {
    var previousSize by rememberSaveable {
        mutableIntStateOf(100)
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
            val viewState by rememberSaveable {
                mutableStateOf(Bundle())
            }
            DisposableEffect(viewInstance) {
                viewInstance?.restoreInstanceState(viewState)

                onDispose {
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
                        viewInstance = this
                        previousSize = size
                        setBackgroundColor(backgroundColor)
                        build(size, size, 1f, spacing, cornerRadius)
                    }
                },
                update = {
                    if (previousSize != size || it.mPhotoItems != imagesMapped || needToInvalidate) {
                        needToInvalidate = false
                        it.mPhotoItems = imagesMapped
                        previousSize = size
                        it.build(size, size, 1f, spacing, cornerRadius)
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