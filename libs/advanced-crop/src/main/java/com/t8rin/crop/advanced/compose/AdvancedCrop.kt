package com.t8rin.crop.advanced.compose

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
import androidx.compose.runtime.DisposableEffect
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
import androidx.core.graphics.scale
import androidx.core.net.toUri
import coil3.imageLoader
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import coil3.request.allowHardware
import coil3.request.maxBitmapSize
import coil3.size.Precision
import coil3.size.Scale
import coil3.toBitmap
import com.t8rin.crop.advanced.callback.BitmapCropCallback
import com.t8rin.crop.advanced.model.ExifInfo
import com.t8rin.crop.advanced.util.BitmapLoadUtils
import com.t8rin.crop.advanced.util.ImageHeaderParser
import com.t8rin.crop.advanced.view.AdvancedCropView
import com.t8rin.crop.advanced.view.CropImageView
import com.t8rin.crop.advanced.view.OverlayView
import com.t8rin.crop.advanced.view.TransformImageView
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.util.UUID
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.roundToInt
import coil3.size.Size as CoilSize

internal class CropController {
    private val mutex = Mutex()
    private val cacheDirectoryName = UUID.randomUUID().toString()
    private var generation = 0L
    private var currentKey: Any? = null

    var session: CropSession? by mutableStateOf(null)
        private set

    suspend fun loadImage(
        imageModel: Any,
        context: Context,
        onLoadingStateChange: (Boolean) -> Unit
    ) {
        val requestId = mutex.withLock {
            if (currentKey == imageModel && session != null) {
                null
            } else {
                currentKey = imageModel
                session = null
                ++generation
            }
        } ?: return

        onLoadingStateChange(true)
        val previewBitmap = try {
            imageModel.decodePreview(
                context = context,
                maxPreviewSize = BitmapLoadUtils.calculateMaxBitmapSize(context).coerceAtLeast(1)
            )
        } catch (e: CancellationException) {
            throw e
        } catch (_: Throwable) {
            null
        }
        if (previewBitmap == null) {
            if (isCurrent(requestId, imageModel)) {
                onLoadingStateChange(false)
            }
            return
        }

        val appContext = context.applicationContext
        val cropDir = withContext(Dispatchers.IO) {
            File(appContext.cacheDir, "crop/$cacheDirectoryName").apply {
                mkdirs()
                cleanUp()
            }
        }
        val sourceFile = File(cropDir, "source_${UUID.randomUUID()}.png")
        val newSession = CropSession(
            id = requestId,
            key = imageModel,
            previewBitmap = previewBitmap,
            source = DeferredCropSource(
                imageModel = imageModel,
                context = appContext,
                cropDir = cropDir,
                targetFile = sourceFile
            ),
            outputUri = appContext.newCropOutputFile().toUri()
        )

        mutex.withLock {
            if (generation == requestId && currentKey == imageModel) {
                session = newSession
            }
        }
    }

    suspend fun clear() = mutex.withLock {
        invalidate()
    }

    fun invalidate() {
        generation++
        currentKey = null
        session = null
    }

    fun isCurrent(target: CropSession): Boolean = session?.id == target.id

    private suspend fun isCurrent(requestId: Long, imageModel: Any): Boolean = mutex.withLock {
        generation == requestId && currentKey == imageModel
    }

    private fun File.cleanUp() {
        listFiles()?.forEach { file ->
            if (file.isDirectory) {
                file.deleteRecursively()
            } else {
                file.delete()
            }
        }
    }
}

internal data class CropSession(
    val id: Long,
    val key: Any,
    val previewBitmap: Bitmap,
    val source: DeferredCropSource,
    val outputUri: Uri
) {
    fun sourceAspectRatio(isSideways: Boolean): Float {
        val width = previewBitmap.width.toFloat()
        val height = previewBitmap.height.toFloat()
        return if (isSideways) height / width else width / height
    }
}

internal data class PreparedCropSource(
    val uri: Uri,
    val exifInfo: ExifInfo
)

internal class DeferredCropSource(
    private val imageModel: Any,
    private val context: Context,
    private val cropDir: File,
    private val targetFile: File
) {
    private val mutex = Mutex()
    private var preparedSource: PreparedCropSource? = null

    val uri: Uri = targetFile.toUri()

    suspend fun await(): PreparedCropSource? = mutex.withLock {
        preparedSource?.let { return@withLock it }

        try {
            imageModel.prepareCropSource(
                context = context,
                cropDir = cropDir,
                targetFile = targetFile
            )?.also {
                preparedSource = it
            }
        } catch (e: CancellationException) {
            throw e
        } catch (_: Throwable) {
            null
        }
    }
}

private suspend fun Any.decodePreview(
    context: Context,
    maxPreviewSize: Int
): Bitmap? {
    if (this is Bitmap) {
        return withContext(Dispatchers.Default) {
            scaledDownTo(maxPreviewSize)
        }
    }
    if (this !is Uri && this !is File && this !is String) return null

    return context.imageLoader.execute(
        ImageRequest.Builder(context)
            .data(this)
            .size(maxPreviewSize)
            .scale(Scale.FIT)
            .precision(Precision.INEXACT)
            .allowHardware(false)
            .memoryCachePolicy(CachePolicy.DISABLED)
            .build()
    ).image?.toBitmap()
}

private fun Bitmap.scaledDownTo(maxSize: Int): Bitmap {
    val largestSide = max(width, height)
    if (largestSide <= maxSize) return this

    val scale = maxSize / largestSide.toFloat()
    return this.scale(
        (width * scale).roundToInt().coerceAtLeast(1),
        (height * scale).roundToInt().coerceAtLeast(1)
    )
}

private suspend fun Any.prepareCropSource(
    context: Context,
    cropDir: File,
    targetFile: File
): PreparedCropSource? = withContext(Dispatchers.IO) {
    val partFile = File(cropDir, "${targetFile.name}.part")
    partFile.delete()
    targetFile.delete()

    val exifInfo = when (this@prepareCropSource) {
        is Bitmap -> {
            if (!writePngTo(partFile)) return@withContext null
            identityExifInfo()
        }

        is Uri -> toSourcePng(context, cropDir, partFile) ?: return@withContext null
        is File -> toUri().toSourcePng(context, cropDir, partFile) ?: return@withContext null
        is String -> toUri().toSourcePng(context, cropDir, partFile) ?: return@withContext null
        else -> return@withContext null
    }

    if (!partFile.isPng() || partFile.length() == 0L) return@withContext null
    if (!partFile.renameTo(targetFile)) {
        partFile.copyTo(targetFile, overwrite = true)
        partFile.delete()
    }
    if (!targetFile.isPng() || targetFile.length() == 0L) return@withContext null

    PreparedCropSource(targetFile.toUri(), exifInfo)
}

private suspend fun Uri.toSourcePng(
    context: Context,
    cropDir: File,
    sourceFile: File
): ExifInfo? {
    val originalFile = copyOriginalTo(context, cropDir) ?: return null

    return try {
        val bitmap = context.imageLoader.execute(
            ImageRequest.Builder(context)
                .data(originalFile.toUri())
                .size(CoilSize.ORIGINAL)
                .maxBitmapSize(CoilSize.ORIGINAL)
                .allowHardware(false)
                .memoryCachePolicy(CachePolicy.DISABLED)
                .build()
        ).image?.toBitmap() ?: return null
        if (!bitmap.writePngTo(sourceFile)) return null

        runCatching {
            ImageHeaderParser.copyIccProfileToPng(
                originalFile.absolutePath,
                sourceFile.absolutePath
            )
        }

        identityExifInfo()
    } finally {
        originalFile.delete()
    }
}

private fun Uri.copyOriginalTo(context: Context, cropDir: File): File? = runCatching {
    val file = File.createTempFile("source_", context.extensionFor(this), cropDir)
    context.contentResolver.openInputStream(this)?.use { input ->
        file.outputStream().use { output ->
            input.copyTo(output)
        }
    } ?: return@runCatching null

    file
}.getOrNull()

private fun Context.newCropOutputFile(): File {
    return File(cacheDir, "crop_results")
        .apply(File::mkdirs)
        .let { File(it, "${UUID.randomUUID()}_out.png") }
}

private fun Bitmap.writePngTo(file: File): Boolean {
    return file.outputStream().use {
        compress(Bitmap.CompressFormat.PNG, 100, it)
    }
}

private fun File.isPng(): Boolean = runCatching {
    inputStream().use { input ->
        val signature = ByteArray(PngSignature.size)
        input.read(signature) == PngSignature.size && signature.contentEquals(PngSignature)
    }
}.getOrDefault(false)

private fun Context.extensionFor(uri: Uri): String {
    val extension = if (uri.scheme == "content") {
        MimeTypeMap.getSingleton()
            .getExtensionFromMimeType(contentResolver.getType(uri))
    } else {
        MimeTypeMap.getFileExtensionFromUrl(uri.toString())
    }?.takeIf { it.isNotBlank() }

    return extension?.let { ".$it" } ?: ".tmp"
}

private fun identityExifInfo() = ExifInfo(0, 0, 1)

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
    val controller = remember { CropController() }
    AdvancedCropImpl(
        controller = controller,
        imageModel = imageModel,
        rotationAngle = rotationAngle,
        sourceRotationDegrees = 0,
        isFlippedHorizontally = false,
        aspectRatio = aspectRatio,
        modifier = modifier,
        isOverlayDraggable = isOverlayDraggable,
        isChangingValues = isChangingValues,
        wrapCropBoundsTrigger = wrapCropBoundsTrigger,
        gridLinesCount = gridLinesCount,
        topPadding = topPadding,
        bottomPadding = bottomPadding,
        startPadding = startPadding,
        endPadding = endPadding,
        croppingTrigger = croppingTrigger,
        gridColor = gridColor,
        handlesColor = handlesColor,
        oneFingerZoom = oneFingerZoom,
        onCropped = onCropped,
        onZoomChange = onZoomChange,
        onLoadingStateChange = onLoadingStateChange
    )
}

@Composable
internal fun AdvancedCropImpl(
    controller: CropController,
    imageModel: Any?,
    rotationAngle: Float,
    sourceRotationDegrees: Int,
    isFlippedHorizontally: Boolean,
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
    val context = LocalContext.current
    val session = controller.session
    val isSourceSideways = abs(sourceRotationDegrees) % 180 == 90

    val onCropped by rememberUpdatedState(onCropped)
    val currentImageModel by rememberUpdatedState(imageModel)
    val onZoomChange by rememberUpdatedState(onZoomChange)
    val onLoadingStateChange by rememberUpdatedState(onLoadingStateChange)

    DisposableEffect(controller) {
        onDispose {
            controller.invalidate()
        }
    }

    LaunchedEffect(imageModel) {
        if (imageModel == null) {
            controller.clear()
            onLoadingStateChange(false)
        } else {
            controller.loadImage(
                imageModel = imageModel,
                context = context,
                onLoadingStateChange = onLoadingStateChange
            )
        }
    }

    AnimatedContent(
        targetState = session,
        transitionSpec = {
            fadeIn() togetherWith fadeOut() using SizeTransform(false)
        }
    ) { targetSession ->
        if (targetSession != null) {
            var viewInstance by remember(targetSession.id) {
                mutableStateOf<AdvancedCropView?>(null)
            }
            var viewLoadVersion by remember(targetSession.id) {
                mutableIntStateOf(0)
            }
            var isCropping by remember(targetSession.id) {
                mutableStateOf(false)
            }
            val currentViewLoadVersion = viewLoadVersion
            val currentIsCropping = isCropping
            AndroidView(
                modifier = modifier,
                factory = { viewContext ->
                    AdvancedCropView(viewContext).apply {
                        imageInputUri = targetSession.source.uri
                        imageOutputUri = targetSession.outputUri
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
                            setSourceRotationDegrees(sourceRotationDegrees)
                            setImageFlipHorizontally(isFlippedHorizontally)
                            targetAspectRatio = aspectRatio
                                ?: targetSession.sourceAspectRatio(isSourceSideways)
                            setTransformImageListener(
                                object : TransformImageView.TransformImageListener {
                                    override fun onLoadComplete() {
                                        if (controller.isCurrent(targetSession) &&
                                            currentImageModel == targetSession.key &&
                                            !isCropping
                                        ) {
                                            viewLoadVersion++
                                            onLoadingStateChange(false)
                                        }
                                    }

                                    override fun onLoadFailure(e: Exception) {
                                        if (controller.isCurrent(targetSession) &&
                                            currentImageModel == targetSession.key &&
                                            !isCropping
                                        ) {
                                            onLoadingStateChange(false)
                                        }
                                    }

                                    override fun onRotate(currentAngle: Float) = Unit

                                    override fun onScale(currentScale: Float) {
                                        if (controller.isCurrent(targetSession) &&
                                            currentImageModel == targetSession.key &&
                                            !isCropping
                                        ) {
                                            onZoomChange(currentZoom)
                                        }
                                    }
                                }
                            )
                            setImageBitmap(
                                targetSession.previewBitmap,
                                targetSession.source.uri,
                                targetSession.outputUri
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
                        isEnabled = !currentIsCropping
                        isOneFingerZoomEnabled = oneFingerZoom
                        if (currentViewLoadVersion > 0 && !currentIsCropping) {
                            applyTargetTransform(
                                rotationAngle = rotationAngle,
                                sourceRotationDegrees = sourceRotationDegrees,
                                isFlippedHorizontally = isFlippedHorizontally,
                                isChangingValues = isChangingValues
                            )
                        }
                    }
                    it.overlayView.apply {
                        isEnabled = !currentIsCropping
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
            LaunchedEffect(viewInstance, aspectRatio, isSourceSideways) {
                viewInstance?.cropImageView?.apply {
                    cancelAllAnimations()
                    targetAspectRatio = aspectRatio
                        ?: targetSession.sourceAspectRatio(isSourceSideways)
                }
            }
            LaunchedEffect(viewInstance, croppingTrigger, currentViewLoadVersion) {
                if (!croppingTrigger || currentViewLoadVersion == 0 || isCropping) {
                    return@LaunchedEffect
                }

                val cropImageView = viewInstance?.cropImageView
                if (cropImageView == null) {
                    onLoadingStateChange(false)
                    return@LaunchedEffect
                }
                isCropping = true
                cropImageView.prepareForCrop()
                cropImageView.isEnabled = false
                viewInstance?.overlayView?.isEnabled = false
                onLoadingStateChange(true)
                val preparedSource = try {
                    targetSession.source.await()
                } catch (e: CancellationException) {
                    if (controller.isCurrent(targetSession) &&
                        currentImageModel == targetSession.key &&
                        viewInstance?.cropImageView === cropImageView
                    ) {
                        isCropping = false
                        cropImageView.isEnabled = true
                        viewInstance?.overlayView?.isEnabled = true
                        onLoadingStateChange(false)
                    }
                    throw e
                }
                if (!controller.isCurrent(targetSession) ||
                    currentImageModel != targetSession.key ||
                    viewInstance?.cropImageView !== cropImageView
                ) {
                    return@LaunchedEffect
                }
                if (preparedSource == null) {
                    isCropping = false
                    cropImageView.isEnabled = true
                    viewInstance?.overlayView?.isEnabled = true
                    onLoadingStateChange(false)
                    return@LaunchedEffect
                }

                cropImageView.setCropSource(
                    preparedSource.uri,
                    targetSession.outputUri,
                    preparedSource.exifInfo
                )
                cropImageView.cropAndSaveImage(
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
                            if (!controller.isCurrent(targetSession) ||
                                currentImageModel != targetSession.key ||
                                viewInstance?.cropImageView !== cropImageView
                            ) return

                            val resultFile = resultUri.path?.let(::File)
                            if (resultFile?.exists() == true && resultFile.length() > 0L) {
                                isCropping = false
                                cropImageView.isEnabled = true
                                viewInstance?.overlayView?.isEnabled = true
                                onLoadingStateChange(false)
                                onCropped(resultUri)
                            } else {
                                onCropFailure(
                                    IOException("Output file was not created: ${resultUri.path}")
                                )
                            }
                        }

                        override fun onCropFailure(t: Throwable) {
                            if (controller.isCurrent(targetSession) &&
                                currentImageModel == targetSession.key &&
                                viewInstance?.cropImageView === cropImageView
                            ) {
                                isCropping = false
                                cropImageView.isEnabled = true
                                viewInstance?.overlayView?.isEnabled = true
                                onLoadingStateChange(false)
                            }
                        }
                    }
                )
            }
        }
    }
}

private fun CropImageView.applyTargetTransform(
    rotationAngle: Float,
    sourceRotationDegrees: Int,
    isFlippedHorizontally: Boolean,
    isChangingValues: Boolean
) {
    val isFlipOrderingChanged = isImageFlipHorizontally != isFlippedHorizontally &&
            getSourceRotationDegrees() != sourceRotationDegrees
    setSourceRotationDegrees(sourceRotationDegrees)
    setImageFlipHorizontally(isFlippedHorizontally)

    val targetAngle = normalizeCropAngle(rotationAngle + sourceRotationDegrees)
    val deltaAngle = normalizeCropAngle(targetAngle - currentAngle)
    if (abs(deltaAngle) > 0.01f) {
        cancelAllAnimations()
        if (isFlipOrderingChanged) {
            postRotateAroundImageCenter(deltaAngle)
        } else {
            postRotate(deltaAngle)
        }
        if (!isChangingValues) {
            setImageToWrapCropBounds()
        }
    }
}

internal fun normalizeCropRotation(degrees: Int): Int {
    return ((degrees + 180) % 360 + 360) % 360 - 180
}

private fun normalizeCropAngle(degrees: Float): Float {
    return ((degrees + 180f) % 360f + 360f) % 360f - 180f
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
