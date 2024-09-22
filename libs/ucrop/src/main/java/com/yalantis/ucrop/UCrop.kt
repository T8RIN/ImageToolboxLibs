package com.yalantis.ucrop

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.compose.animation.AnimatedContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.graphics.drawable.toBitmap
import androidx.core.net.toUri
import coil.imageLoader
import coil.request.ImageRequest
import com.yalantis.ucrop.callback.BitmapCropCallback
import com.yalantis.ucrop.view.CropImageView
import com.yalantis.ucrop.view.OverlayView
import com.yalantis.ucrop.view.UCropView
import java.io.File
import java.io.FileOutputStream
import kotlin.random.Random

private object CropCache {
    private var previousKey: Any? = null
    var bitmap by mutableStateOf<Bitmap?>(null)

    var inputUri: Uri by mutableStateOf(Uri.EMPTY)

    var outputUri: Uri by mutableStateOf(Uri.EMPTY)

    suspend fun loadBitmap(
        imageModel: Any?,
        context: Context,
        onLoadingStateChange: (Boolean) -> Unit
    ) {
        if (previousKey != imageModel) {
            onLoadingStateChange(true)
            bitmap = null
            bitmap = if (imageModel is Bitmap?) {
                imageModel
            } else context.imageLoader.execute(
                ImageRequest.Builder(context).data(imageModel).build()
            ).drawable?.toBitmap()

            File(context.cacheDir, "crop").apply {
                deleteRecursively()
                mkdirs()
            }
            val file = File(context.cacheDir, "crop/${Random.nextInt()}input.png")
            runCatching {
                FileOutputStream(file).use { os ->
                    bitmap!!.compress(Bitmap.CompressFormat.PNG, 100, os)
                }
            }

            val file1 = File(context.cacheDir, "crop/${Random.nextInt()}out.png")

            inputUri = file.toUri()
            outputUri = file1.toUri()
            onLoadingStateChange(false)
        }
        previousKey = imageModel
    }

    fun clear() {
        bitmap?.recycle()
        bitmap = null
        previousKey = null
        inputUri = Uri.EMPTY
        outputUri = Uri.EMPTY
    }
}

@Composable
fun UCrop(
    imageModel: Any?,
    rotationAngle: Float,
    aspectRatio: Float?,
    modifier: Modifier = Modifier,
    isOverlayDraggable: Boolean = false,
    croppingTrigger: Boolean,
    onCropped: (Uri) -> Unit,
    onLoadingStateChange: (Boolean) -> Unit = {}
) {
    val bitmap = CropCache.bitmap
    val context = LocalContext.current as Activity
    val inputUri = CropCache.inputUri
    val outputUri = CropCache.outputUri

    LaunchedEffect(imageModel) {
        CropCache.loadBitmap(
            imageModel = imageModel,
            context = context,
            onLoadingStateChange = onLoadingStateChange
        )
    }

    DisposableEffect(Unit) {
        onDispose {
            if (!context.isChangingConfigurations) {
                CropCache.clear()
            }
        }
    }

    AnimatedContent(bitmap) { image ->
        if (image != null) {
            var viewInstance by remember(image) {
                mutableStateOf<UCropView?>(null)
            }
            AndroidView(
                modifier = modifier,
                factory = { context ->
                    UCropView(context).apply {
                        setBackgroundColor(Color.Transparent.toArgb())
                        cropImageView.apply {
                            setMaxScaleMultiplier(20f)
                            isRotateEnabled = false
                        }
                    }.also {
                        viewInstance = it
                    }
                },
                update = {
                    it.cropImageView.apply {
                        setImageUri(inputUri, outputUri)
                        runCatching {
                            postRotate(-currentAngle)
                            postRotate(rotationAngle)
                            setImageToWrapCropBounds()
                        }
                    }
                    it.overlayView.apply {
                        freestyleCropMode = if (aspectRatio == null) {
                            if (isOverlayDraggable) OverlayView.FREESTYLE_CROP_MODE_ENABLE
                            else OverlayView.FREESTYLE_CROP_MODE_ENABLE_WITH_PASS_THROUGH
                        } else OverlayView.FREESTYLE_CROP_MODE_DISABLE
                    }
                }
            )
            LaunchedEffect(aspectRatio) {
                viewInstance?.apply {
                    cropImageView.apply {
                        targetAspectRatio = aspectRatio ?: CropImageView.SOURCE_IMAGE_ASPECT_RATIO
                    }
                }
            }
            LaunchedEffect(croppingTrigger) {
                if (croppingTrigger) {
                    viewInstance?.cropImageView?.cropAndSaveImage(
                        Bitmap.CompressFormat.PNG, 100, object : BitmapCropCallback {

                            override fun onBitmapCropped(
                                resultUri: Uri,
                                offsetX: Int,
                                offsetY: Int,
                                imageWidth: Int,
                                imageHeight: Int
                            ) {
                                Log.d("COCK", "cropped $resultUri")
                                onCropped(resultUri)
                            }

                            override fun onCropFailure(t: Throwable) = Unit

                        }
                    )
                }
            }
        }
    }
}