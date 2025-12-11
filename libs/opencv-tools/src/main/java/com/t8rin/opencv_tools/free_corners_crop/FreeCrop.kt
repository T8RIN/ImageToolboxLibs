package com.t8rin.opencv_tools.free_corners_crop

import android.graphics.Bitmap
import android.graphics.PointF
import androidx.compose.ui.geometry.Offset
import com.t8rin.opencv_tools.free_corners_crop.model.Quad
import com.t8rin.opencv_tools.free_corners_crop.model.distance
import com.t8rin.opencv_tools.free_corners_crop.model.toOpenCVPoint
import com.t8rin.opencv_tools.utils.OpenCV
import com.t8rin.opencv_tools.utils.toBitmap
import com.t8rin.opencv_tools.utils.toMat
import org.opencv.core.Mat
import org.opencv.core.MatOfPoint2f
import org.opencv.core.Point
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc
import kotlin.math.min

object FreeCrop : OpenCV() {

    fun crop(
        bitmap: Bitmap,
        points: List<Offset>
    ): Bitmap {
        val corners = Quad(
            topLeftCorner = PointF(points[0].x, points[0].y),
            topRightCorner = PointF(points[1].x, points[1].y),
            bottomRightCorner = PointF(points[2].x, points[2].y),
            bottomLeftCorner = PointF(points[3].x, points[3].y)
        )

        val image = bitmap.toMat()

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

        return output.toBitmap()
    }

}