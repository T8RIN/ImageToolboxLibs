package com.t8rin.crop.advanced.compose

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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
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
import com.t8rin.crop.advanced.callback.BitmapCropCallback
import com.t8rin.crop.advanced.view.AdvancedCropView
import com.t8rin.crop.advanced.view.CropImageView
import com.t8rin.crop.advanced.view.OverlayView
import com.t8rin.crop.advanced.view.TransformImageView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import kotlin.math.abs
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
            val loadedBitmap = when (imageModel) {
                null -> null
                is Bitmap -> imageModel
                else -> withContext(Dispatchers.IO) {
                    context.imageLoader.execute(
                        ImageRequest.Builder(context).data(imageModel).build()
                    ).image?.toBitmap()
                }
            }

            if (loadedBitmap != null) {
                val (newInputUri, newOutputUri) = withContext(Dispatchers.IO) {
                    File(context.cacheDir, "crop").apply {
                        deleteRecursively()
                        mkdirs()
                    }
                    val file = File(context.cacheDir, "crop/${Random.nextInt()}input.png")
                    FileOutputStream(file).use { os ->
                        loadedBitmap.compress(Bitmap.CompressFormat.PNG, 100, os)
                    }

                    val file1 = File(context.cacheDir, "crop/${Random.nextInt()}out.png")
                    file.toUri() to file1.toUri()
                }

                inputUri = newInputUri
                outputUri = newOutputUri
            }

            bitmap = loadedBitmap
            previousKey = if (loadedBitmap != null) imageModel else null
        }
        if (bitmap == null) {
            onLoadingStateChange(false)
        }
    }

    private var mutex = Mutex()

    fun flip(
        onLoadingStateChange: (Boolean) -> Unit
    ) {
        CoroutineScope(Dispatchers.Main).launch {
            mutex.withLock {
                onLoadingStateChange(true)
                val currentBitmap = bitmap
                val currentInputUri = inputUri
                val newImage = if (currentBitmap != null && currentInputUri != Uri.EMPTY) {
                    withContext(Dispatchers.IO) {
                        val matrix = Matrix().apply {
                            postScale(
                                -1f,
                                1f,
                                currentBitmap.width / 2f,
                                currentBitmap.height / 2f
                            )
                        }
                        Bitmap.createBitmap(
                            currentBitmap,
                            0,
                            0,
                            currentBitmap.width,
                            currentBitmap.height,
                            matrix,
                            true
                        ).also { flippedImage ->
                            currentInputUri.toFile().outputStream().use {
                                flippedImage.compress(Bitmap.CompressFormat.PNG, 100, it)
                            }
                        }
                    }
                } else {
                    null
                }
                if (newImage != null) {
                    bitmap = newImage
                }
                onLoadingStateChange(false)
            }
        }
    }

    fun rotate90(
        onLoadingStateChange: (Boolean) -> Unit,
        onFinish: () -> Unit
    ) {
        CoroutineScope(Dispatchers.Main).launch {
            mutex.withLock {
                onLoadingStateChange(true)
                val currentBitmap = bitmap
                val currentInputUri = inputUri
                val newImage = if (currentBitmap != null && currentInputUri != Uri.EMPTY) {
                    withContext(Dispatchers.IO) {
                        val matrix = Matrix().apply { postRotate(-90f) }
                        Bitmap.createBitmap(
                            currentBitmap,
                            0,
                            0,
                            currentBitmap.width,
                            currentBitmap.height,
                            matrix,
                            true
                        ).also { rotatedImage ->
                            currentInputUri.toFile().outputStream().use {
                                rotatedImage.compress(Bitmap.CompressFormat.PNG, 100, it)
                            }
                        }
                    }
                } else {
                    null
                }
                if (newImage != null) {
                    bitmap = newImage
                    onFinish()
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
fun AdvancedCrop(
    imageModel: Any?,
    rotationAngle: Float,
    aspectRatio: Float?,
    modifier: Modifier = Modifier,
    isOverlayDraggable: Boolean = false,
    isChangingValues: Boolean = false,
    wrapCropBoundsTrigger: Int = 0,
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

    val imageModel by rememberUpdatedState(imageModel)

    LaunchedEffect(imageModel) {
        CropCache.loadBitmap(
            imageModel = imageModel,
            context = context,
            onLoadingStateChange = onLoadingStateChange
        )
        invalidate++
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
                mutableStateOf<AdvancedCropView?>(null)
            }
            AndroidView(
                modifier = modifier,
                factory = { context ->
                    AdvancedCropView(context).apply {
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
                            targetAspectRatio =
                                aspectRatio ?: CropImageView.SOURCE_IMAGE_ASPECT_RATIO
                            setTransformImageListener(
                                object : TransformImageView.TransformImageListener {
                                    override fun onLoadComplete() {
                                        onLoadingStateChange(false)
                                    }

                                    override fun onLoadFailure(e: Exception) {
                                        onLoadingStateChange(false)
                                    }

                                    override fun onRotate(currentAngle: Float) = Unit

                                    override fun onScale(currentScale: Float) = Unit
                                }
                            )
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
                        val isImageReady = inputUri != Uri.EMPTY && outputUri != Uri.EMPTY
                        val shouldUpdateImage = it.imageInputUri != inputUri ||
                                it.imageOutputUri != outputUri
                        if (isImageReady && shouldUpdateImage) {
                            it.imageInputUri = inputUri
                            it.imageOutputUri = outputUri
                            setImageUri(inputUri, outputUri)
                        }
                        if (abs(currentAngle - rotationAngle) > 0.01f) {
                            if (isChangingValues) {
                                cancelAllAnimations()
                            }
                            postRotate(-currentAngle)
                            postRotate(rotationAngle)
                            if (!isChangingValues) {
                                setImageToWrapCropBounds()
                            }
                        }
                    }
                    it.overlayView.apply {
                        setCropFrameColor(colorScheme.surfaceVariant.toArgb())
                        setCropGridColor(colorScheme.surfaceVariant.toArgb())
                        setCropGridRowCount(gridLinesCount)
                        setCropGridColumnCount(gridLinesCount)
                        freestyleCropMode = if (aspectRatio == null) {
                            if (isOverlayDraggable) {
                                OverlayView.FREESTYLE_CROP_MODE_ENABLE
                            } else {
                                OverlayView.FREESTYLE_CROP_MODE_ENABLE_WITH_PASS_THROUGH
                            }
                        } else {
                            OverlayView.FREESTYLE_CROP_MODE_DISABLE
                        }
                    }
                }
            )
            LaunchedEffect(viewInstance, wrapCropBoundsTrigger) {
                if (wrapCropBoundsTrigger > 0) {
                    viewInstance?.cropImageView?.apply {
                        cancelAllAnimations()
                        setImageToWrapCropBounds()
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
                        Bitmap.CompressFormat.PNG,
                        100,
                        object : BitmapCropCallback {
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
