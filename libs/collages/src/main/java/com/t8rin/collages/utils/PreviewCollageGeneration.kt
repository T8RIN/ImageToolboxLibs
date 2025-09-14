package com.t8rin.collages.utils

import android.graphics.Bitmap
import android.net.Uri
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.core.graphics.applyCanvas
import androidx.core.graphics.createBitmap
import androidx.core.graphics.scale
import androidx.core.net.toUri
import com.t8rin.collages.Collage
import com.t8rin.collages.CollageType
import com.t8rin.collages.model.TemplateItem
import com.t8rin.collages.utils.FrameImageUtils.loadFrameImages
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import kotlin.math.pow
import kotlin.math.sqrt

@PreviewDebugApi
@RequiresApi(100)
@Composable
fun PreviewCollageGeneration() {
    fun Bitmap.replaceColor(
        fromColor: Color,
        targetColor: Color,
        tolerance: Float
    ): Bitmap {
        fun Color.distanceFrom(color: Color): Float {
            return sqrt(
                (red - color.red).pow(2) + (green - color.green).pow(2) + (blue - color.blue).pow(
                    2
                )
            )
        }

        val width = width
        val height = height
        val pixels = IntArray(width * height)
        getPixels(pixels, 0, width, 0, 0, width, height)
        for (x in pixels.indices) {
            pixels[x] = if (Color(pixels[x]).distanceFrom(fromColor) <= tolerance) {
                targetColor.toArgb()
            } else pixels[x]
        }
        val result = createBitmap(width, height)
        result.setPixels(pixels, 0, width, 0, 0, width, height)
        return result
    }

    var allFrames: List<TemplateItem> by remember {
        mutableStateOf(emptyList())
    }
    val context = LocalContext.current

    LaunchedEffect(context) {
        allFrames = loadFrameImages(context)
    }

    var previewImageUri by rememberSaveable {
        mutableStateOf<Uri?>(null)
    }

    LaunchedEffect(previewImageUri) {
        if (previewImageUri == null) {
            val file = File(context.cacheDir, "tmp")

            file.outputStream().use {
                createBitmap(200, 200).applyCanvas {
                    drawColor(Color.Black.toArgb())
                }.compress(Bitmap.CompressFormat.PNG, 100, it)
            }

            previewImageUri = file.toUri()
        }
    }

    val scope = rememberCoroutineScope {
        Dispatchers.IO
    }

    if (previewImageUri != null) {
        Box {
            allFrames.forEachIndexed { index, template ->
                val (_, title, _, photoItemList) = template
                val density = LocalDensity.current
                val spacing = with(density) {
                    1.5.dp.toPx()
                }

                var trigger by remember {
                    mutableStateOf(false)
                }

                LaunchedEffect(Unit) {
                    delay(500 + 10L * index)
                    trigger = true
                }

                Collage(
                    images = photoItemList.mapNotNull { previewImageUri },
                    modifier = Modifier.size(64.dp),
                    spacing = spacing,
                    cornerRadius = 0f,
                    onCollageCreated = { image ->
                        scope.launch {
                            val dir = File(context.cacheDir, "frames")
                            dir.mkdirs()
                            val file = File(dir, title)

                            file.createNewFile()

                            file.outputStream().use {
                                image.scale(525, 525, false).replaceColor(
                                    fromColor = Color.Black,
                                    targetColor = Color.Transparent,
                                    tolerance = 0.1f
                                ).compress(Bitmap.CompressFormat.PNG, 100, it)
                            }
                            println("DONE: $title")
                        }
                    },
                    outputScaleRatio = 10f,
                    collageCreationTrigger = trigger,
                    collageType = CollageType(
                        templateItem = template,
                        index = null
                    ),
                    userInteractionEnabled = false
                )
            }
        }
    }
}

@Retention(AnnotationRetention.BINARY)
@RequiresOptIn("This api is not for public use, do no call explicitly")
annotation class PreviewDebugApi