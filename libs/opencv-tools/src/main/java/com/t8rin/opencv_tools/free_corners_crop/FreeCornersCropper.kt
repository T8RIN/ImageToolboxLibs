package com.t8rin.opencv_tools.free_corners_crop

import android.graphics.Bitmap
import android.graphics.PointF
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import coil.imageLoader
import coil.request.ImageRequest
import com.smarttoolfactory.image.ImageWithConstraints
import com.smarttoolfactory.image.util.coerceIn
import com.t8rin.opencv_tools.autocrop.getMat
import com.t8rin.opencv_tools.free_corners_crop.model.Quad
import com.t8rin.opencv_tools.free_corners_crop.model.distance
import com.t8rin.opencv_tools.free_corners_crop.model.toOpenCVPoint
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
    contentPadding: PaddingValues = PaddingValues(24.dp),
    containerModifier: Modifier = Modifier
) {
    var bitmap by remember {
        mutableStateOf<Bitmap?>(null)
    }
    val context = LocalContext.current

    LaunchedEffect(imageModel) {
        bitmap = context.imageLoader.execute(
            ImageRequest.Builder(context).data(imageModel).allowHardware(false).build()
        ).drawable?.toBitmap()
    }

    AnimatedContent(
        targetState = bitmap,
        modifier = containerModifier
    ) { image ->
        if (image != null) {
            FreeCornersCropper(
                bitmap = image,
                croppingTrigger = croppingTrigger,
                onCropped = onCropped,
                modifier = modifier,
                contentPadding = contentPadding
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
    contentPadding: PaddingValues = PaddingValues(24.dp)
) {
    LaunchedEffect(Unit) {
        OpenCVLoader.initDebug()
    }
    val density = LocalDensity.current

    val handleRadius = with(density) {
        12.dp.toPx()
    }
    val strokeWidth = with(density) {
        1.dp.toPx()
    }
    val overlayColor = Color.Black.copy(0.5f)

    var touchIndex by remember {
        mutableIntStateOf(-1)
    }

    val colorScheme = MaterialTheme.colorScheme

    val direction = LocalLayoutDirection.current
    val topPadding = with(density) {
        contentPadding.calculateTopPadding().roundToPx()
    }
    val bottomPadding = with(density) {
        contentPadding.calculateBottomPadding().roundToPx()
    }
    val startPadding = with(density) {
        contentPadding.calculateStartPadding(direction).roundToPx()
    }
    val endPadding = with(density) {
        contentPadding.calculateEndPadding(direction).roundToPx()
    }

    val imageBitmap = remember(bitmap) { bitmap.asImageBitmap() }
    ImageWithConstraints(
        modifier = modifier.clipToBounds(),
        imageBitmap = imageBitmap,
        drawImage = false
    ) {
        val imageWidthPx: Float
        val imageHeightPx: Float

        with(density) {
            imageWidthPx = imageWidth.toPx()
            imageHeightPx = imageHeight.toPx()
        }

        val imageWidth = imageWidthPx.roundToInt() - startPadding - endPadding
        val imageHeight = imageHeightPx.roundToInt() - topPadding - bottomPadding

        val internalPadding = with(density) { 16.dp.toPx() }

        var topOffset by remember {
            mutableIntStateOf(0)
        }

        var drawPoints by rememberSaveable(topOffset, stateSaver = OffsetListSaver) {
            mutableStateOf(
                listOf(
                    Offset(
                        startPadding + internalPadding,
                        topPadding + internalPadding + topOffset
                    ),
                    Offset(
                        imageWidthPx - internalPadding - endPadding,
                        topPadding + internalPadding + topOffset
                    ),
                    Offset(
                        imageWidthPx - internalPadding - endPadding,
                        imageHeightPx - internalPadding - bottomPadding + topOffset
                    ),
                    Offset(
                        startPadding + internalPadding,
                        imageHeightPx - internalPadding - bottomPadding + topOffset
                    )
                )
            )
        }

        var canvasWidth by remember {
            mutableIntStateOf(0)
        }
        var canvasHeight by remember {
            mutableIntStateOf(0)
        }

        LaunchedEffect(croppingTrigger) {
            if (croppingTrigger) {
                onCropped(
                    cropImage(
                        bitmap = bitmap,
                        points = drawPoints.map {
                            Offset(
                                x = it.x - (startPadding + internalPadding),
                                y = it.y / canvasHeight * imageHeightPx
                            )
                        }
                    )
                )
            }
        }

        Canvas(
            modifier = Modifier.size(maxWidth, maxHeight)
        ) {

            canvasWidth = size.width.roundToInt()
            canvasHeight = size.height.roundToInt()

            topOffset = (canvasHeight - imageHeightPx.roundToInt()) / 2

            drawImage(
                image = imageBitmap,
                srcSize = IntSize(imageBitmap.width, imageBitmap.height),
                dstSize = IntSize(imageWidth, imageHeight),
                dstOffset = IntOffset(
                    x = (canvasWidth - imageWidth) / 2,
                    y = (canvasHeight - imageHeight) / 2
                )
            )
        }

        Canvas(
            modifier = Modifier
                .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
                .size(maxWidth, maxHeight)
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            touchIndex = -1
                            drawPoints.forEachIndexed { index, drawProperties ->
                                val isTouched =
                                    isTouched(drawProperties, offset, handleRadius)

                                if (isTouched) {
                                    touchIndex = index
                                }
                            }
                        },
                        onDrag = { _, dragAmount: Offset ->
                            val item = drawPoints.getOrNull(touchIndex)
                            item?.let { drawItem ->
                                drawPoints = drawPoints
                                    .toMutableList()
                                    .apply {
                                        this[touchIndex] = drawItem
                                            .plus(dragAmount)
                                            .coerceIn(
                                                horizontalRange = startPadding.toFloat()..(imageWidth + startPadding).toFloat(),
                                                verticalRange = (topPadding + topOffset).toFloat()..(imageHeight + bottomPadding + topOffset).toFloat()
                                            )
                                    }
                            }
                        },
                        onDragEnd = {
                            val item = drawPoints.getOrNull(touchIndex)
                            item?.let { drawItem ->
                                drawPoints = drawPoints
                                    .toMutableList()
                                    .apply {
                                        this[touchIndex] = drawItem
                                    }
                            }
                        }
                    )
                }
        ) {
            val (x, y) = drawPoints[0]
            val (x1, y1) = drawPoints[1]
            val (x2, y2) = drawPoints[2]
            val (x3, y3) = drawPoints[3]
            val framePath = Path().apply {
                moveTo(x, y)
                lineTo(x1, y1)
                lineTo(x2, y2)
                lineTo(x3, y3)
                lineTo(x, y)
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
                brush = SolidColor(colorScheme.surfaceVariant),
                style = Stroke(strokeWidth)
            )

            drawPoints.forEachIndexed { index, drawProperties ->
                if (touchIndex != index) {
                    drawCircle(
                        color = Color.White,
                        center = drawProperties,
                        radius = handleRadius,
                        style = Stroke(strokeWidth * 2)
                    )
                }
            }

            if (touchIndex > -1) {
                drawPoints.getOrNull(touchIndex)?.let { drawProperties ->
                    drawCircle(
                        color = Color.White,
                        center = drawProperties,
                        radius = handleRadius,
                        style = Stroke(strokeWidth * 2)
                    )
                }
            }
        }
    }
}

private fun isTouched(center: Offset, touchPosition: Offset, radius: Float): Boolean {
    return center.minus(touchPosition).getDistanceSquared() < radius * radius * (radius * 0.5f)
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