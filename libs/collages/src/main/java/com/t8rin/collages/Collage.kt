package com.t8rin.collages

import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.viewinterop.AndroidView
import com.photoeditor.photoeffect.frame.FramePhotoLayout
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
    collageType: CollageType
) {
    var previousSize by remember {
        mutableIntStateOf(100)
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
                    if (it.mPhotoItems.size != imagesMapped.size) {
                        it.mPhotoItems = imagesMapped
                    }
                    if (previousSize != size || it.mPhotoItems != imagesMapped) {
                        it.mPhotoItems = imagesMapped
                        previousSize = size
                        it.build(size, size, 1f, spacing, cornerRadius)
                    }
                    it.setBackgroundColor(backgroundColor)
                    it.setSpace(spacing, cornerRadius)
                }
            )

            LaunchedEffect(viewInstance, collageCreationTrigger) {
                if (collageCreationTrigger) {
                    viewInstance?.createImage()?.let(onCollageCreated)
                }
            }
        }
    }
}