package com.t8rin.opencv_tools.moire

import android.graphics.Bitmap
import com.t8rin.opencv_tools.utils.OpenCV
import com.t8rin.opencv_tools.utils.getMat
import com.t8rin.opencv_tools.utils.toBitmap
import org.opencv.core.Core
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.Scalar
import org.opencv.imgproc.Imgproc
import java.util.Arrays

object Moire : OpenCV() {

    private fun dftDim(image: Mat): Mat {
        println("Finding the optimal dft image size")
        val padded = Mat()
        val pixelRows =
            Core.getOptimalDFTSize(image.rows()) //sets a variable to be the optimum size for dft
        val pixelCols =
            Core.getOptimalDFTSize(image.cols()) //sets a variable to be the optimum size for dft
        println("Optimal dft size found")
        Core.copyMakeBorder(
            image,
            padded,
            0,
            pixelRows - image.rows(),
            0,
            pixelCols - image.cols(),
            Core.BORDER_CONSTANT,
            Scalar.all(0.0)
        ) //modifies the image so that had the optimum length and width for dft
        return padded
    }

    private fun magnitudeFind(complexI: Mat): Mat {
        println("Finding magnitude")
        val planesList: MutableList<Mat?> = ArrayList()
        val mag = Mat()
        Core.split(
            complexI,
            planesList
        ) //splits the image channels into and array of mats and a mat
        Core.magnitude(
            planesList[0],
            planesList[1],
            mag
        ) //finds the magnitudes of the array;
        return mag
    }


    fun remove(image: Bitmap): Bitmap {
        val image: Mat = image.getMat()

        Imgproc.cvtColor(image, image, Imgproc.COLOR_RGBA2BGR)
        Imgproc.cvtColor(image, image, Imgproc.COLOR_BGR2GRAY)

        println("Image loaded")

        val padded = dftDim(image)
        val complexI = Mat()

        padded.convertTo(padded, CvType.CV_32F) //converts the Mat to hold 32 bit signed floats

        val planes = mutableListOf<Mat?>().apply {
            add(padded)
            add(Mat.zeros(padded.size(), CvType.CV_32F))
        }

        Core.merge(
            planes,
            complexI
        ) //merges the arrays so that, while they hold different values, they both are used to create a single image

        println("Mats merged")

        Core.dft(complexI, complexI) //find the dft of the image

        println("Dft computed")

        val magnitude: Mat = magnitudeFind(complexI)

        println("Magnitudes found")

        val size =
            (magnitude.total() * magnitude.channels()).toInt() //calculates the size of the mat magnitude

        val array = FloatArray(size) //initializes a new array for sorting
        for (i in 0..<magnitude.cols()) { //populates array with the values of magnitude
            for (j in 0..<magnitude.rows()) { //iterates through magnitude
                array[i * magnitude.rows() + j] = (magnitude.get(
                    j,
                    i
                )[0]).toFloat() //adds the relevant values in magnitude to array (with casting)
            }
        }
        Arrays.sort(array) //sorts the array
        val percentage =
            array[((array.size - 1) * 0.8).toInt()].toInt() //finds the value at the nth percentile

        //Imgproc.threshold(magnitude, magnitude, percentage, 2, Imgproc.THRESH_BINARY_INV); //changes the top 20% of values to 0
        //System.out.println("Magnitudes thresholded");

        for (i in 0..<magnitude.cols()) { //iterates through the cols in magnitude
            for (j in 0..<magnitude.rows()) { //iterates through the rows in magnitude
                val ar0 = floatArrayOf(0f, 0f)
                val ar1 = floatArrayOf(1f, 1f)
                val arr = FloatArray(2)
                if (magnitude.get(j, i)[0] >= percentage) {  //changes the top n% of values to 0
                    magnitude.put(j, i, ar0)
                } else { //and the rest to 1
                    magnitude.put(j, i, ar1)
                }
                arr[0] = (magnitude.get(j, i)[0] * complexI.get(
                    j,
                    i
                )[0]).toFloat() //multiplies the value in magnitude by the value in the corresponding index in complexI
                arr[1] = (magnitude.get(j, i)[0] * complexI.get(
                    j,
                    i
                )[1]).toFloat() //magnitude holds 0s or 1s only because of the thresholding
                complexI.put(
                    j,
                    i,
                    arr
                ) //the values multiplied by one will remain the same while the values multipled by 0 will become 0
            }
        }

        Core.idft(complexI, complexI) //inverses the transform

        println("Inverse DFT computed")

        val restore = Mat()

        Core.split(
            complexI,
            planes
        ) //splits the array into a mat and an array of mats

        Core.normalize(
            planes[0],
            restore,
            0.0,
            255.0,
            Core.NORM_MINMAX
        ) //normalizes the array so that it can be viewed

        restore.convertTo(restore, CvType.CV_8U) //converted to a byte image

        println("Image processed")

        return restore.toBitmap()
    }

}