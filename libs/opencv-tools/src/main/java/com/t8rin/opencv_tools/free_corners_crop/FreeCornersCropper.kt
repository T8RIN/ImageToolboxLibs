package com.t8rin.opencv_tools.free_corners_crop

import android.graphics.Bitmap
import android.graphics.PointF
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.imageLoader
import coil3.request.ImageRequest
import coil3.request.allowHardware
import coil3.toBitmap
import com.smarttoolfactory.gesture.observePointersCountWithOffset
import com.smarttoolfactory.image.ImageWithConstraints
import com.smarttoolfactory.image.util.coerceIn
import com.t8rin.opencv_tools.free_corners_crop.model.Quad
import com.t8rin.opencv_tools.free_corners_crop.model.distance
import com.t8rin.opencv_tools.free_corners_crop.model.toOpenCVPoint
import com.t8rin.opencv_tools.utils.getMat
import net.engawapg.lib.zoomable.rememberZoomState
import net.engawapg.lib.zoomable.zoomable
import org.opencv.android.OpenCVLoader
import org.opencv.android.Utils
import org.opencv.core.Mat
import org.opencv.core.MatOfPoint2f
import org.opencv.core.Point
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc
import kotlin.math.min
import kotlin.math.roundToInt

@Composable
fun FreeCornersCropper(
    imageModel: Any?,
    croppingTrigger: Boolean,
    onCropped: (Bitmap) -> Unit,
    modifier: Modifier = Modifier,
    handlesSize: Dp = 8.dp,
    frameStrokeWidth: Dp = 1.2.dp,
    coercePointsToImageArea: Boolean = true,
    overlayColor: Color = Color.Black.copy(0.5f),
    contentPadding: PaddingValues = PaddingValues(24.dp),
    containerModifier: Modifier = Modifier,
    onLoadingStateChange: (Boolean) -> Unit = {}
) {
    var bitmap by remember {
        mutableStateOf<Bitmap?>(null)
    }
    val context = LocalContext.current

    LaunchedEffect(imageModel) {
        bitmap = if (imageModel is Bitmap?) imageModel
        else {
            onLoadingStateChange(true)
            context.imageLoader.execute(
                ImageRequest.Builder(context).data(imageModel)
                    .allowHardware(false).build()
            ).image?.toBitmap()
        }
        onLoadingStateChange(false)
    }

    AnimatedContent(
        targetState = bitmap,
        modifier = containerModifier,
        transitionSpec = { fadeIn() togetherWith fadeOut() }
    ) { image ->
        if (image != null) {
            FreeCornersCropper(
                bitmap = image,
                croppingTrigger = croppingTrigger,
                onCropped = onCropped,
                modifier = modifier,
                contentPadding = contentPadding,
                coercePointsToImageArea = coercePointsToImageArea,
                handlesSize = handlesSize,
                frameStrokeWidth = frameStrokeWidth,
                overlayColor = overlayColor
            )
        }
    }
}

@Composable
fun FreeCornersCropper(
    bitmap: Bitmap,
    croppingTrigger: Boolean,
    onCropped: (Bitmap) -> Unit,
    modifier: Modifier = Modifier,
    handlesSize: Dp = 8.dp,
    frameStrokeWidth: Dp = 1.2.dp,
    coercePointsToImageArea: Boolean = true,
    overlayColor: Color = Color.Black.copy(0.5f),
    contentPadding: PaddingValues = PaddingValues(24.dp)
) {
    LaunchedEffect(Unit) {
        OpenCVLoader.initDebug()
    }
    val density = LocalDensity.current

    val handleRadiusPx = with(density) {
        handlesSize.toPx()
    }
    val frameStrokeWidthPx = with(density) {
        frameStrokeWidth.toPx()
    }

    val touchIndex = remember {
        mutableIntStateOf(-1)
    }
    var globalTouchPointersCount by remember { mutableIntStateOf(0) }

    val colorScheme = MaterialTheme.colorScheme

    val imageBitmap = remember(bitmap) { bitmap.asImageBitmap() }
    ImageWithConstraints(
        modifier = modifier
            .clipToBounds()
            .observePointersCountWithOffset { size, _ ->
                globalTouchPointersCount = size
            }
            .zoomable(
                zoomState = rememberZoomState(maxScale = 10f),
                zoomEnabled = globalTouchPointersCount >= 2
            ),
        imageBitmap = imageBitmap,
        drawImage = false
    ) {
        var imageWidth by remember {
            mutableIntStateOf(bitmap.width)
        }

        var imageHeight by remember {
            mutableIntStateOf(bitmap.height)
        }

        val internalPaddingDp = 16.dp
        val internalPadding = with(density) { internalPaddingDp.toPx() }

        var topOffset by remember {
            mutableIntStateOf(0)
        }

        var startOffset by remember {
            mutableIntStateOf(0)
        }

        val drawPoints = rememberSaveable(
            topOffset,
            startOffset,
            imageWidth,
            imageHeight,
            contentPadding,
            stateSaver = OffsetListSaver
        ) {
            mutableStateOf(
                listOf(
                    Offset(
                        x = internalPadding + startOffset,
                        y = internalPadding + topOffset
                    ),
                    Offset(
                        x = imageWidth - internalPadding + startOffset,
                        y = internalPadding + topOffset
                    ),
                    Offset(
                        x = imageWidth - internalPadding + startOffset,
                        y = imageHeight - internalPadding + topOffset
                    ),
                    Offset(
                        x = internalPadding + startOffset,
                        y = imageHeight - internalPadding + topOffset
                    )
                )
            )
        }

        val pointScales = List(drawPoints.value.size) {
            animateFloatAsState(if (it == touchIndex.intValue) 1.4f else 1f)
        }

        LaunchedEffect(croppingTrigger) {
            if (croppingTrigger) {
                val widthScale = bitmap.width.toFloat() / imageWidth
                val heightScale = bitmap.height.toFloat() / imageHeight
                onCropped(
                    cropImage(
                        bitmap = bitmap,
                        points = drawPoints.value.map {
                            Offset(
                                x = ((it.x - startOffset) * widthScale).roundToInt()
                                    .coerceIn(0, bitmap.width).toFloat(),
                                y = ((it.y - topOffset) * heightScale).roundToInt()
                                    .coerceIn(0, bitmap.height).toFloat()
                            )
                        }
                    )
                )
            }
        }

        Image(
            bitmap = imageBitmap,
            contentDescription = null,
            modifier = Modifier
                .padding(internalPaddingDp)
                .padding(contentPadding)
                .aspectRatio(bitmap.width / bitmap.height.toFloat())
                .onGloballyPositioned {
                    topOffset = it.positionInParent().y.toInt()
                    startOffset = it.positionInParent().x.toInt()
                    imageWidth = it.size.width
                    imageHeight = it.size.height
                },
            contentScale = ContentScale.FillBounds
        )

        fun Offset.coerceToImageBounds(): Offset = coerceIn(
            horizontalRange = (startOffset).toFloat()..((imageWidth + startOffset).toFloat()),
            verticalRange = (topOffset).toFloat()..((imageHeight + topOffset).toFloat())
        )

        LaunchedEffect(coercePointsToImageArea) {
            drawPoints.value = drawPoints.value.map {
                it.coerceToImageBounds()
            }
        }

        Canvas(
            modifier = Modifier
                .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
                .size(maxWidth, maxHeight)
                .pointerInput(contentPadding, coercePointsToImageArea, handleRadiusPx, bitmap) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            touchIndex.intValue = -1
                            drawPoints.value.forEachIndexed { index, drawProperties ->
                                val isTouched = isTouched(
                                    center = drawProperties,
                                    touchPosition = offset,
                                    radius = handleRadiusPx
                                )

                                if (isTouched) {
                                    touchIndex.intValue = index
                                }
                            }
                        },
                        onDrag = { _, dragAmount: Offset ->
                            drawPoints.value
                                .getOrNull(touchIndex.intValue)
                                ?.let { point ->
                                    drawPoints.value = drawPoints.value
                                        .toMutableList()
                                        .apply {
                                            this[touchIndex.intValue] = point
                                                .plus(dragAmount)
                                                .let {
                                                    if (coercePointsToImageArea) {
                                                        it.coerceToImageBounds()
                                                    } else it
                                                }
                                        }
                                }
                        },
                        onDragEnd = {
                            drawPoints.value
                                .getOrNull(touchIndex.intValue)
                                ?.let { point ->
                                    drawPoints.value = drawPoints.value
                                        .toMutableList()
                                        .apply {
                                            this[touchIndex.intValue] = point
                                        }
                                }
                            touchIndex.intValue = -1
                        }
                    )
                }
        ) {
            val (x, y) = drawPoints.value[0]
            val (x1, y1) = drawPoints.value[1]
            val (x2, y2) = drawPoints.value[2]
            val (x3, y3) = drawPoints.value[3]

            val framePath = Path().apply {
                moveTo(x, y)
                lineTo(x1, y1)
                lineTo(x2, y2)
                lineTo(x3, y3)
                close()
            }
            drawRect(overlayColor)

            drawPath(
                path = framePath,
                brush = SolidColor(Color.Transparent),
                blendMode = BlendMode.Clear
            )

            drawPath(
                path = framePath,
                brush = SolidColor(colorScheme.primaryContainer),
                style = Stroke(frameStrokeWidthPx)
            )

            drawPoints.value.forEachIndexed { index, point ->
                val scale = pointScales[index].value

                drawCircle(
                    color = colorScheme.primary,
                    center = point,
                    radius = handleRadiusPx * scale
                )
                drawCircle(
                    color = colorScheme.primaryContainer,
                    center = point,
                    radius = handleRadiusPx * 0.8f * scale
                )
            }
        }
    }
}

private fun isTouched(center: Offset, touchPosition: Offset, radius: Float): Boolean {
    return center.minus(touchPosition).getDistanceSquared() < radius * radius * radius
}

private val OffsetListSaver: Saver<List<Offset>, String> = Saver(
    save = { list ->
        list.joinToString(",") { (x, y) ->
            "$x:$y"
        }
    },
    restore = { string ->
        string.split(",").map { o ->
            val (x, y) = o.split(":").map { it.toFloat() }
            Offset(x, y)
        }
    }
)

private fun cropImage(
    bitmap: Bitmap,
    points: List<Offset>
): Bitmap {
    val corners = Quad(
        topLeftCorner = PointF(points[0].x, points[0].y),
        topRightCorner = PointF(points[1].x, points[1].y),
        bottomRightCorner = PointF(points[2].x, points[2].y),
        bottomLeftCorner = PointF(points[3].x, points[3].y)
    )

    val image = bitmap.getMat()

    // convert top left, top right, bottom right, and bottom left document corners from
    // Android points to OpenCV points
    val tLC = corners.topLeftCorner.toOpenCVPoint()
    val tRC = corners.topRightCorner.toOpenCVPoint()
    val bRC = corners.bottomRightCorner.toOpenCVPoint()
    val bLC = corners.bottomLeftCorner.toOpenCVPoint()

    val width = min(tLC.distance(tRC), bLC.distance(bRC))
    val height = min(tLC.distance(bLC), tRC.distance(bRC))

    // create empty image matrix with cropped and warped document width and height
    val croppedImage = MatOfPoint2f(
        Point(0.0, 0.0),
        Point(width, 0.0),
        Point(width, height),
        Point(0.0, height),
    )

    val output = Mat()
    Imgproc.warpPerspective(
        image,
        output,
        Imgproc.getPerspectiveTransform(
            MatOfPoint2f(tLC, tRC, bRC, bLC),
            croppedImage
        ),
        Size(width, height)
    )

    // convert output image matrix to bitmap
    val croppedBitmap = Bitmap.createBitmap(
        output.cols(),
        output.rows(),
        Bitmap.Config.ARGB_8888
    )
    Utils.matToBitmap(output, croppedBitmap)

    return croppedBitmap
}