package com.yalantis.ucrop.compose

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.net.Uri
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.net.toFile
import androidx.core.net.toUri
import coil3.imageLoader
import coil3.request.ImageRequest
import coil3.toBitmap
import com.yalantis.ucrop.callback.BitmapCropCallback
import com.yalantis.ucrop.view.CropImageView
import com.yalantis.ucrop.view.OverlayView
import com.yalantis.ucrop.view.UCropView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import kotlin.random.Random

internal object CropCache {
    private var previousKey: Any? = null
    var bitmap by mutableStateOf<Bitmap?>(null)

    var inputUri: Uri by mutableStateOf(Uri.EMPTY)

    var outputUri: Uri by mutableStateOf(Uri.EMPTY)

    suspend fun loadBitmap(
        imageModel: Any?,
        context: Context,
        onLoadingStateChange: (Boolean) -> Unit
    ) {
        onLoadingStateChange(true)
        if (previousKey != imageModel) {
            clear()
            bitmap = null
            bitmap = if (imageModel is Bitmap?) {
                imageModel
            } else context.imageLoader.execute(
                ImageRequest.Builder(context).data(imageModel).build()
            ).image?.toBitmap()

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
        }
        if (bitmap != null) {
            onLoadingStateChange(false)
        }
        previousKey = imageModel
    }

    private var mutex = Mutex()

    fun flip(
        onLoadingStateChange: (Boolean) -> Unit
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            mutex.withLock {
                onLoadingStateChange(true)
                bitmap?.let { image ->
                    val matrix =
                        Matrix().apply { postScale(-1f, 1f, image.width / 2f, image.height / 2f) }
                    Bitmap.createBitmap(image, 0, 0, image.width, image.height, matrix, true)
                        .also { newImage ->
                            inputUri.toFile().outputStream().use {
                                newImage.compress(Bitmap.CompressFormat.PNG, 100, it)
                            }
                            bitmap = newImage
                        }
                }
                onLoadingStateChange(false)
            }
        }
    }

    fun rotate90(
        onLoadingStateChange: (Boolean) -> Unit,
        onFinish: () -> Unit
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            mutex.withLock {
                onLoadingStateChange(true)
                bitmap?.let { image ->
                    val matrix = Matrix().apply { postRotate(-90f) }
                    Bitmap.createBitmap(image, 0, 0, image.width, image.height, matrix, true)
                        .also { newImage ->
                            inputUri.toFile().outputStream().use {
                                newImage.compress(Bitmap.CompressFormat.PNG, 100, it)
                            }
                            bitmap = newImage
                            onFinish()
                        }
                }
                onLoadingStateChange(false)
            }
        }
    }

    fun clear() {
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
    gridLinesCount: Int = 2,
    topPadding: Dp = Dp.Unspecified,
    bottomPadding: Dp = Dp.Unspecified,
    startPadding: Dp = Dp.Unspecified,
    endPadding: Dp = Dp.Unspecified,
    croppingTrigger: Boolean,
    onCropped: (Uri) -> Unit,
    onLoadingStateChange: (Boolean) -> Unit = {}
) {
    val bitmap = CropCache.bitmap
    val context = LocalContext.current as Activity
    val inputUri = CropCache.inputUri
    val outputUri = CropCache.outputUri

    var invalidate by remember {
        mutableIntStateOf(0)
    }

    LaunchedEffect(imageModel) {
        withContext(Dispatchers.IO) {
            CropCache.loadBitmap(
                imageModel = imageModel,
                context = context,
                onLoadingStateChange = onLoadingStateChange
            )
        }
        invalidate++
    }

    DisposableEffect(Unit) {
        onDispose {
            if (!context.isChangingConfigurations) {
                CropCache.clear()
            }
        }
    }
    val colorScheme = MaterialTheme.colorScheme

    AnimatedContent(
        targetState = bitmap to invalidate,
        transitionSpec = {
            fadeIn() togetherWith fadeOut() using SizeTransform(false)
        }
    ) { (image, _) ->
        if (image != null) {
            var viewInstance by remember(image) {
                mutableStateOf<UCropView?>(null)
            }
            AndroidView(
                modifier = modifier,
                factory = { context ->
                    UCropView(context).apply {
                        setPadding(
                            bottomPadding = bottomPadding,
                            topPadding = topPadding,
                            startPadding = startPadding,
                            endPadding = endPadding
                        )
                        setBackgroundColor(Color.Transparent.toArgb())
                        cropImageView.apply {
                            setMaxScaleMultiplier(20f)
                            isRotateEnabled = false
                        }
                        overlayView.apply {
                            setCropGridRowCount(gridLinesCount)
                            setCropGridColumnCount(gridLinesCount)
                        }
                    }.also {
                        viewInstance = it
                    }
                },
                update = {
                    it.cropImageView.apply {
                        setImageUri(inputUri, outputUri)
                        postRotate(-currentAngle)
                        postRotate(rotationAngle)
                        setImageToWrapCropBounds()
                    }
                    it.overlayView.apply {
                        setCropFrameColor(colorScheme.surfaceVariant.toArgb())
                        setCropGridColor(colorScheme.surfaceVariant.toArgb())
                        setCropGridRowCount(gridLinesCount)
                        setCropGridColumnCount(gridLinesCount)
                        freestyleCropMode = if (aspectRatio == null) {
                            if (isOverlayDraggable) OverlayView.FREESTYLE_CROP_MODE_ENABLE
                            else OverlayView.FREESTYLE_CROP_MODE_ENABLE_WITH_PASS_THROUGH
                        } else OverlayView.FREESTYLE_CROP_MODE_DISABLE
                    }
                }
            )
            LaunchedEffect(viewInstance) {
                withContext(Dispatchers.IO) {
                    delay(400)
                    viewInstance?.cropImageView?.apply {
                        while (currentAngle != rotationAngle) {
                            postRotate(-currentAngle)
                            postRotate(rotationAngle)
                            setImageToWrapCropBounds()
                            delay(500)
                        }
                    }
                }
            }
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