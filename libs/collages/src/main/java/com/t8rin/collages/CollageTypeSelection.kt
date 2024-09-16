package com.t8rin.collages

import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Surface
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import androidx.core.graphics.applyCanvas
import androidx.core.net.toUri
import com.photoeditor.photoeffect.frame.FramePhotoLayout
import com.photoeditor.photoeffect.model.TemplateItem
import com.photoeditor.photoeffect.utils.FrameImageUtils.loadFrameImages
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.File

@Composable
fun CollageTypeSelection(
    imagesCount: Int,
    value: CollageType,
    onValueChange: (CollageType) -> Unit,
    modifier: Modifier = Modifier,
    previewBackgroundColor: Color = MaterialTheme.colorScheme.surface,
    previewBorderColor: Color = MaterialTheme.colorScheme.secondary,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp)
) {
    var allFrames: List<TemplateItem> by remember {
        mutableStateOf(emptyList())
    }
    val context = LocalContext.current

    LaunchedEffect(context) {
        allFrames = loadFrameImages(context)
    }

    val availableFrames by remember(allFrames, imagesCount) {
        derivedStateOf {
            allFrames.filter {
                it.photoItemList.size == imagesCount
            }
        }
    }

    var previewImageUri by remember(previewBackgroundColor) {
        mutableStateOf<Uri?>(null)
    }

    LaunchedEffect(previewImageUri) {
        if (previewImageUri == null) {
            val file = File(context.cacheDir, "tmp")

            file.outputStream().use {
                Bitmap.createBitmap(200, 200, Bitmap.Config.ARGB_8888).applyCanvas {
                    drawColor(previewBackgroundColor.toArgb())
                }.compress(Bitmap.CompressFormat.PNG, 100, it)
            }

            previewImageUri = file.toUri()
        }
    }

    LaunchedEffect(availableFrames) {
        if (availableFrames.isNotEmpty()) {
            onValueChange(
                CollageType(availableFrames.first())
            )
        }
    }

    AnimatedVisibility(
        visible = availableFrames.size > 1 && previewImageUri != null
    ) {
        LazyRow(
            modifier = modifier,
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = contentPadding
        ) {
            items(availableFrames) { templateItem ->
                val density = LocalDensity.current
                val spacing = with(density) {
                    1.5.dp.toPx()
                }
                val size = with(density) {
                    64.dp.roundToPx()
                }

                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .scale(
                            animateFloatAsState(
                                if (value.templateItem == templateItem) 0.8f
                                else 1f
                            ).value
                        )
                        .clickable {
                            onValueChange(CollageType(templateItem))
                        }
                ) {
                    var viewInstance by remember {
                        mutableStateOf<FramePhotoLayout?>(null)
                    }
                    AndroidView(
                        factory = { ctx ->
                            FramePhotoLayout(
                                context = ctx,
                                mPhotoItems = templateItem.photoItemList.onEach {
                                    it.imagePath = previewImageUri
                                }
                            ).apply {
                                setBackgroundColor(previewBorderColor)
                                build(size, size, 1f, spacing, 0f)
                                viewInstance = this
                            }
                        }
                    )
                    LaunchedEffect(
                        templateItem,
                        previewImageUri,
                        viewInstance,
                        previewBorderColor,
                        spacing,
                        size
                    ) {
                        delay(500)
                        withContext(Dispatchers.Main.immediate) {
                            viewInstance?.mPhotoItems = templateItem.photoItemList.onEach {
                                it.imagePath = previewImageUri
                            }
                            viewInstance?.setBackgroundColor(previewBorderColor)
                            viewInstance?.build(size, size, 1f, spacing, 0f)
                        }
                    }
                    Surface(
                        color = Color.Transparent,
                        modifier = Modifier
                            .matchParentSize()
                            .zIndex(2f)
                    ) { }
                }
            }
        }
    }

}