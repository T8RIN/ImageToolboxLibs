package com.t8rin.crop.advanced.compose

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.webkit.MimeTypeMap
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
import androidx.core.net.toUri
import coil3.imageLoader
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import coil3.request.allowHardware
import coil3.toBitmap
import com.t8rin.crop.advanced.callback.BitmapCropCallback
import com.t8rin.crop.advanced.model.ExifInfo
import com.t8rin.crop.advanced.task.BitmapCropTask
import com.t8rin.crop.advanced.util.BitmapLoadUtils
import com.t8rin.crop.advanced.util.ImageHeaderParser
import com.t8rin.crop.advanced.view.AdvancedCropView
import com.t8rin.crop.advanced.view.CropImageView
import com.t8rin.crop.advanced.view.OverlayView
import com.t8rin.crop.advanced.view.TransformImageView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID
import kotlin.math.abs
import coil3.size.Size as CoilSize

internal object CropCache {
    private var previousKey: Any? = null

    var inputUri: Uri by mutableStateOf(Uri.EMPTY)

    var outputUri: Uri by mutableStateOf(Uri.EMPTY)

    private val mutex = Mutex()

    suspend fun loadImage(
        imageModel: Any?,
        context: Context,
        onLoadingStateChange: (Boolean) -> Unit
    ) = mutex.withLock {
        onLoadingStateChange(true)
        if (previousKey != imageModel) {
            val protectedFiles = setOfNotNull(
                inputUri.takeIf { it != Uri.EMPTY }?.path,
                outputUri.takeIf { it != Uri.EMPTY }?.path
            )
            clear()
            val preparedFiles = withContext(Dispatchers.IO) {
                val cropDir = File(context.cacheDir, "crop").apply(File::mkdirs)
                cropDir.cleanUp(protectedFiles)
                imageModel.prepareCropFiles(context, cropDir)
            }

            if (preparedFiles != null) {
                val (newInputUri, newOutputUri) = preparedFiles
                inputUri = newInputUri
                outputUri = newOutputUri
                previousKey = imageModel
            }
        }
        if (inputUri == Uri.EMPTY || outputUri == Uri.EMPTY) {
            onLoadingStateChange(false)
        }
    }

    private fun File.cleanUp(protectedFiles: Set<String>) {
        listFiles()
            ?.asSequence()
            ?.filterNot { it.absolutePath in protectedFiles }
            ?.forEach { file ->
                if (file.isDirectory) {
                    file.deleteRecursively()
                } else {
                    file.delete()
                }
            }
    }

    fun flip(
        context: Context,
        onLoadingStateChange: (Boolean) -> Unit
    ) {
        transformSource(
            context = context,
            rotateDegrees = 0,
            flipHorizontally = true,
            onLoadingStateChange = onLoadingStateChange
        )
    }

    fun rotate90(
        context: Context,
        onLoadingStateChange: (Boolean) -> Unit,
        onFinish: () -> Unit
    ) {
        transformSource(
            context = context,
            rotateDegrees = -90,
            flipHorizontally = false,
            onLoadingStateChange = onLoadingStateChange,
            onFinish = onFinish
        )
    }

    fun clear() {
        previousKey = null
        inputUri = Uri.EMPTY
        outputUri = Uri.EMPTY
    }

    private fun transformSource(
        context: Context,
        rotateDegrees: Int,
        flipHorizontally: Boolean,
        onLoadingStateChange: (Boolean) -> Unit,
        onFinish: () -> Unit = {}
    ) {
        kotlinx.coroutines.CoroutineScope(Dispatchers.Main).launch {
            mutex.withLock {
                val currentInputUri = inputUri.takeIf { it != Uri.EMPTY }
                if (currentInputUri == null) {
                    onLoadingStateChange(false)
                    return@withLock
                }

                onLoadingStateChange(true)
                val transformed = withContext(Dispatchers.IO) {
                    runCatching {
                        val cropDir = File(context.cacheDir, "crop").apply(File::mkdirs)
                        val nextInput = File.createTempFile("input_", ".png", cropDir)
                        val nextOutput = File(cropDir, "${UUID.randomUUID()}_out.png")
                        val exifInfo = context.readExifInfo(currentInputUri)
                        val currentInputPath = currentInputUri.path ?: return@runCatching null

                        val transformed = BitmapCropTask.transformCImg(
                            currentInputPath,
                            nextInput.absolutePath,
                            rotateDegrees,
                            flipHorizontally,
                            Bitmap.CompressFormat.PNG.ordinal,
                            100,
                            exifInfo.exifDegrees,
                            exifInfo.exifTranslation
                        )
                        if (!transformed) return@runCatching null

                        ImageHeaderParser.copyIccProfileToPng(
                            currentInputPath,
                            nextInput.absolutePath
                        )
                        nextInput.toUri() to nextOutput.toUri()
                    }.getOrNull()
                }

                if (transformed != null) {
                    inputUri = transformed.first
                    outputUri = transformed.second
                    onFinish()
                } else {
                    onLoadingStateChange(false)
                }
            }
        }
    }
}

private suspend fun Any?.prepareCropFiles(
    context: Context,
    cropDir: File
): Pair<Uri, Uri>? {
    val inputFile = when (this) {
        null -> return null
        is Bitmap -> withContext(Dispatchers.IO) {
            File.createTempFile("input_", ".png", cropDir)
        }.also(::writePngTo)

        is Uri -> toSourcePng(context, cropDir)
        is File -> toUri().toSourcePng(context, cropDir)
        is String -> toUri().toSourcePng(context, cropDir)
        else -> return null
    } ?: return null

    val outputFile = File(cropDir, "${UUID.randomUUID()}_out.png")
    return inputFile.toUri() to outputFile.toUri()
}

private fun Bitmap.writePngTo(file: File) {
    file.outputStream().use {
        compress(Bitmap.CompressFormat.PNG, 100, it)
    }
}

private suspend fun Uri.toSourcePng(context: Context, cropDir: File): File? = runCatching {
    val originalFile = copyOriginalTo(context, cropDir)
    val sourceFile = File.createTempFile("source_", ".png", cropDir)

    if (originalFile?.isPng() == true) {
        originalFile.copyTo(sourceFile, overwrite = true)
    } else {
        val bitmap = context.imageLoader.execute(
            ImageRequest.Builder(context)
                .data(originalFile?.toUri() ?: this@toSourcePng)
                .size(CoilSize.ORIGINAL)
                .allowHardware(false)
                .memoryCachePolicy(CachePolicy.DISABLED)
                .build()
        ).image?.toBitmap() ?: return@runCatching null

        bitmap.writePngTo(sourceFile)
        bitmap.recycleIfNeeded()
    }

    originalFile?.let { original ->
        runCatching {
            ImageHeaderParser.copyIccProfileToPng(
                original.absolutePath,
                sourceFile.absolutePath
            )
        }
    }

    sourceFile
}.getOrNull()

private fun Uri.copyOriginalTo(context: Context, cropDir: File): File? = runCatching {
    val file = File.createTempFile("source_", context.extensionFor(this), cropDir)
    context.contentResolver.openInputStream(this)?.use { input ->
        file.outputStream().use { output ->
            input.copyTo(output)
        }
    } ?: return@runCatching null

    file
}.getOrNull()

private fun File.isPng(): Boolean = runCatching {
    inputStream().use { input ->
        val signature = ByteArray(PngSignature.size)
        input.read(signature) == PngSignature.size && signature.contentEquals(PngSignature)
    }
}.getOrDefault(false)

private fun Bitmap.recycleIfNeeded() {
    if (!isRecycled) {
        recycle()
    }
}

private fun Context.extensionFor(uri: Uri): String {
    val extension = if (uri.scheme == "content") {
        MimeTypeMap.getSingleton()
            .getExtensionFromMimeType(contentResolver.getType(uri))
    } else {
        MimeTypeMap.getFileExtensionFromUrl(uri.toString())
    }?.takeIf { it.isNotBlank() }

    return extension?.let { ".$it" } ?: ".tmp"
}

private fun Context.readExifInfo(uri: Uri): ExifInfo {
    val exifOrientation = BitmapLoadUtils.getExifOrientation(this, uri)
    return ExifInfo(
        exifOrientation,
        BitmapLoadUtils.exifToDegrees(exifOrientation),
        BitmapLoadUtils.exifToTranslation(exifOrientation)
    )
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
    gridColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    handlesColor: Color = MaterialTheme.colorScheme.primaryFixed,
    oneFingerZoom: Boolean = true,
    onCropped: (Uri) -> Unit,
    onZoomChange: (Float) -> Unit = {},
    onLoadingStateChange: (Boolean) -> Unit = {}
) {
    val context = LocalContext.current as Activity
    val inputUri = CropCache.inputUri
    val outputUri = CropCache.outputUri

    var invalidate by remember {
        mutableIntStateOf(0)
    }

    val imageModel by rememberUpdatedState(imageModel)
    val onZoomChange by rememberUpdatedState(onZoomChange)

    LaunchedEffect(imageModel) {
        CropCache.loadImage(
            imageModel = imageModel,
            context = context,
            onLoadingStateChange = onLoadingStateChange
        )
        invalidate++
    }

    AnimatedContent(
        targetState = (inputUri to outputUri) to invalidate,
        transitionSpec = {
            fadeIn() togetherWith fadeOut() using SizeTransform(false)
        }
    ) { (uris, _) ->
        val targetInputUri = uris.first
        val targetOutputUri = uris.second
        if (targetInputUri != Uri.EMPTY && targetOutputUri != Uri.EMPTY) {
            var viewInstance by remember(targetInputUri, targetOutputUri) {
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
                            isOneFingerZoomEnabled = oneFingerZoom
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

                                    override fun onScale(currentScale: Float) =
                                        onZoomChange(currentZoom)
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
                        isOneFingerZoomEnabled = oneFingerZoom
                        val shouldUpdateImage = it.imageInputUri != targetInputUri ||
                                it.imageOutputUri != targetOutputUri
                        if (shouldUpdateImage) {
                            it.imageInputUri = targetInputUri
                            it.imageOutputUri = targetOutputUri
                            setImageUri(targetInputUri, targetOutputUri)
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
                        setCropFrameColor(gridColor.toArgb())
                        setCropGridColor(gridColor.toArgb())
                        setCropGridCornerColor(handlesColor.toArgb())
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

                            override fun onCropFailure(t: Throwable) {
                                onLoadingStateChange(false)
                            }
                        }
                    )
                }
            }
        }
    }
}

private val PngSignature = byteArrayOf(
    0x89.toByte(),
    0x50,
    0x4E,
    0x47,
    0x0D,
    0x0A,
    0x1A,
    0x0A
)
