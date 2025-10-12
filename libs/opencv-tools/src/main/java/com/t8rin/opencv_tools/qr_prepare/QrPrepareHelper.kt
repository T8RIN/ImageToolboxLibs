package com.t8rin.opencv_tools.qr_prepare

import android.graphics.Bitmap
import com.t8rin.opencv_tools.utils.OpenCV
import org.opencv.android.Utils
import org.opencv.core.Mat
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc

object QrPrepareHelper : OpenCV() {

    fun prepareQrForDecode(bitmap: Bitmap): Bitmap {
        // 1. Bitmap -> Mat
        val src = Mat()
        Utils.bitmapToMat(bitmap, src)

        // 2. В grayscale
        val gray = Mat()
        Imgproc.cvtColor(src, gray, Imgproc.COLOR_RGBA2GRAY)

        // 3. CLAHE для локального контраста
        val claheMat = Mat()
        val clahe = Imgproc.createCLAHE()
        clahe.clipLimit = 2.0
        clahe.tilesGridSize = Size(8.0, 8.0)
        clahe.apply(gray, claheMat)

        // 4. Глобальная бинаризация через Otsu (полностью заливает модули)
        val binary = Mat()
        Imgproc.threshold(
            claheMat,
            binary,
            0.0,
            255.0,
            Imgproc.THRESH_BINARY or Imgproc.THRESH_OTSU
        )

        // 5. Морфология для устранения дырок
        val kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, Size(3.0, 3.0))
        Imgproc.morphologyEx(binary, binary, Imgproc.MORPH_CLOSE, kernel)

        // 6. Конвертируем в Bitmap
        val result = Bitmap.createBitmap(binary.cols(), binary.rows(), Bitmap.Config.ARGB_8888)
        Imgproc.cvtColor(binary, binary, Imgproc.COLOR_GRAY2RGBA)
        Utils.matToBitmap(binary, result)

        // 7. Освобождаем память
        src.release()
        gray.release()
        claheMat.release()
        binary.release()

        return result
    }

}