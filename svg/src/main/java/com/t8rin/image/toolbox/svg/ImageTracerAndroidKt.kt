/*
 * ImageToolbox is an image editor for android
 * Copyright (c) 2024 T8RIN (Malik Mukhametzyanov)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * You should have received a copy of the Apache License
 * along with this program.  If not, see <http://www.apache.org/licenses/LICENSE-2.0>.
 */
/*
	ImageTracerAndroid.java
	(Android version with android.graphics. See ImageTracer.java for the desktop version.)
	Simple raster image tracer and vectorizer written in Java. This is a port of imagetracer.js.
	by András Jankovics 2015, 2016
	andras@jankovics.net

 */
@file:Suppress("SpellCheckingInspection", "MemberVisibilityCanBePrivate", "unused")

package com.t8rin.image.toolbox.svg

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.nio.IntBuffer
import java.util.TreeMap
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.pow

object ImageTracerAndroidKt {
    // Loading an image from a file, tracing when loaded, then returning IndexedImage with tracedata in layers
    fun imageToTracedata(
        filename: String,
        options: HashMap<String?, Float>,
        palette: Array<ByteArray>?
    ): IndexedImage {
        val finalOptions = checkoptions(options)
        val imgd = loadImageData(filename)
        return imagedataToTracedata(imgd, finalOptions, palette)
    } // End of imageToTracedata()

    fun imageToTracedata(
        bitmap: Bitmap,
        options: HashMap<String?, Float>,
        palette: Array<ByteArray>?
    ): IndexedImage {
        val finalOptions = checkoptions(options)
        val imgd = loadImageData(bitmap)
        return imagedataToTracedata(imgd, finalOptions, palette)
    } // End of imageToTracedata()

    // Container for the color-indexed image before and tracedata after vectorizing
    class IndexedImage(// array[x][y] of palette colors
        var array: Array<IntArray>, // array[palettelength][4] RGBA color palette
        var palette: Array<ByteArray>?
    ) {
        // Color quantization adds +2 to the original width and height
        var width: Int = array[0].size - 2
        var height: Int = array.size - 2
        var layers: ArrayList<ArrayList<ArrayList<Array<Double?>>>>? = null // tracedata
    }

    // https://developer.mozilla.org/en-US/docs/Web/API/ImageData
    class ImageData(
        var width: Int, var height: Int, // raw byte data: R G B A R G B A ...
        var data: ByteArray
    )


    var versionnumber = "1.1.2"

    // Lookup tables for pathscan
    var pathscan_dir_lookup = byteArrayOf(0, 0, 3, 0, 1, 0, 3, 0, 0, 3, 3, 1, 0, 3, 0, 0)
    var pathscan_holepath_lookup = booleanArrayOf(
        false,
        false,
        false,
        false,
        false,
        false,
        false,
        true,
        false,
        false,
        false,
        true,
        false,
        true,
        true,
        false
    )

    // pathscan_combined_lookup[ arr[py][px] ][ dir ] = [nextarrpypx, nextdir, deltapx, deltapy];
    var pathscan_combined_lookup = arrayOf(
        arrayOf(
            byteArrayOf(-1, -1, -1, -1),
            byteArrayOf(-1, -1, -1, -1),
            byteArrayOf(-1, -1, -1, -1),
            byteArrayOf(-1, -1, -1, -1)
        ),
        arrayOf(
            byteArrayOf(0, 1, 0, -1),
            byteArrayOf(-1, -1, -1, -1),
            byteArrayOf(-1, -1, -1, -1),
            byteArrayOf(0, 2, -1, 0)
        ),
        arrayOf(
            byteArrayOf(-1, -1, -1, -1),
            byteArrayOf(-1, -1, -1, -1),
            byteArrayOf(0, 1, 0, -1),
            byteArrayOf(0, 0, 1, 0)
        ),
        arrayOf(
            byteArrayOf(0, 0, 1, 0),
            byteArrayOf(-1, -1, -1, -1),
            byteArrayOf(0, 2, -1, 0),
            byteArrayOf(-1, -1, -1, -1)
        ),
        arrayOf(
            byteArrayOf(-1, -1, -1, -1),
            byteArrayOf(0, 0, 1, 0),
            byteArrayOf(0, 3, 0, 1),
            byteArrayOf(-1, -1, -1, -1)
        ),
        arrayOf(
            byteArrayOf(13, 3, 0, 1),
            byteArrayOf(13, 2, -1, 0),
            byteArrayOf(7, 1, 0, -1),
            byteArrayOf(7, 0, 1, 0)
        ),
        arrayOf(
            byteArrayOf(-1, -1, -1, -1),
            byteArrayOf(0, 1, 0, -1),
            byteArrayOf(-1, -1, -1, -1),
            byteArrayOf(0, 3, 0, 1)
        ),
        arrayOf(
            byteArrayOf(0, 3, 0, 1),
            byteArrayOf(0, 2, -1, 0),
            byteArrayOf(-1, -1, -1, -1),
            byteArrayOf(-1, -1, -1, -1)
        ),
        arrayOf(
            byteArrayOf(0, 3, 0, 1),
            byteArrayOf(0, 2, -1, 0),
            byteArrayOf(-1, -1, -1, -1),
            byteArrayOf(-1, -1, -1, -1)
        ),
        arrayOf(
            byteArrayOf(-1, -1, -1, -1),
            byteArrayOf(0, 1, 0, -1),
            byteArrayOf(-1, -1, -1, -1),
            byteArrayOf(0, 3, 0, 1)
        ),
        arrayOf(
            byteArrayOf(11, 1, 0, -1),
            byteArrayOf(14, 0, 1, 0),
            byteArrayOf(14, 3, 0, 1),
            byteArrayOf(11, 2, -1, 0)
        ),
        arrayOf(
            byteArrayOf(-1, -1, -1, -1),
            byteArrayOf(0, 0, 1, 0),
            byteArrayOf(0, 3, 0, 1),
            byteArrayOf(-1, -1, -1, -1)
        ),
        arrayOf(
            byteArrayOf(0, 0, 1, 0),
            byteArrayOf(-1, -1, -1, -1),
            byteArrayOf(0, 2, -1, 0),
            byteArrayOf(-1, -1, -1, -1)
        ),
        arrayOf(
            byteArrayOf(-1, -1, -1, -1),
            byteArrayOf(-1, -1, -1, -1),
            byteArrayOf(0, 1, 0, -1),
            byteArrayOf(0, 0, 1, 0)
        ),
        arrayOf(
            byteArrayOf(0, 1, 0, -1),
            byteArrayOf(-1, -1, -1, -1),
            byteArrayOf(-1, -1, -1, -1),
            byteArrayOf(0, 2, -1, 0)
        ),
        arrayOf(
            byteArrayOf(-1, -1, -1, -1),
            byteArrayOf(-1, -1, -1, -1),
            byteArrayOf(-1, -1, -1, -1),
            byteArrayOf(-1, -1, -1, -1)
        )
    )

    // Gaussian kernels for blur
    var gks = arrayOf(
        doubleArrayOf(0.27901, 0.44198, 0.27901),
        doubleArrayOf(0.135336, 0.228569, 0.272192, 0.228569, 0.135336),
        doubleArrayOf(0.086776, 0.136394, 0.178908, 0.195843, 0.178908, 0.136394, 0.086776),
        doubleArrayOf(
            0.063327,
            0.093095,
            0.122589,
            0.144599,
            0.152781,
            0.144599,
            0.122589,
            0.093095,
            0.063327
        ),
        doubleArrayOf(
            0.049692,
            0.069304,
            0.089767,
            0.107988,
            0.120651,
            0.125194,
            0.120651,
            0.107988,
            0.089767,
            0.069304,
            0.049692
        )
    )

    // Saving a String as a file
    fun saveString(filename: String, str: String?) {
        val file = File(filename)
        // if file doesnt exists, then create it
        if (!file.exists()) {
            file.createNewFile()
        }
        val fw = FileWriter(file.getAbsoluteFile())
        val bw = BufferedWriter(fw)
        bw.write(str)
        bw.close()
    }

    // Loading a file to ImageData, ARGB byte order
    fun loadImageData(filename: String): ImageData {
        val image = BitmapFactory.decodeFile(File(filename).absolutePath)
        return loadImageData(image)
    }

    ////////////////////////////////////////////////////////////
    //
    //  User friendly functions
    //
    ////////////////////////////////////////////////////////////
    fun loadImageData(image: Bitmap): ImageData {
        val width = image.getWidth()
        val height = image.getHeight()
        val ib = IntBuffer.allocate(width * height)
        image.copyPixelsToBuffer(ib)
        val rawdata = ib.array()
        val data = ByteArray(rawdata.size * 4)
        for (i in rawdata.indices) {
            data[i * 4 + 3] = bytetrans((rawdata[i] ushr 24).toByte())
            data[i * 4 + 2] = bytetrans((rawdata[i] ushr 16).toByte())
            data[i * 4 + 1] = bytetrans((rawdata[i] ushr 8).toByte())
            data[i * 4] = bytetrans(rawdata[i].toByte())
        }
        return ImageData(width, height, data)
    }

    // The bitshift method in loadImageData creates signed bytes where -1 -> 255 unsigned ; -128 -> 128 unsigned ;
    // 127 -> 127 unsigned ; 0 -> 0 unsigned ; These will be converted to -128 (representing 0 unsigned) ...
    // 127 (representing 255 unsigned) and tosvgcolorstr will add +128 to create RGB values 0..255
    fun bytetrans(b: Byte): Byte {
        return if (b < 0) {
            (b + 128).toByte()
        } else {
            (b - 128).toByte()
        }
    }

    // Loading an image from a file, tracing when loaded, then returning the SVG String
    fun imageToSVG(
        filename: String,
        options: HashMap<String?, Float>,
        palette: Array<ByteArray>?
    ): String {
        val finalOptions = checkoptions(options)
        val imgd = loadImageData(filename)
        return imagedataToSVG(imgd, finalOptions, palette)
    } // End of imageToSVG()

    fun imageToSVG(
        bitmap: Bitmap,
        options: HashMap<String?, Float>,
        palette: Array<ByteArray>?
    ): String {
        val finalOptions = checkoptions(options)
        val imgd = loadImageData(bitmap)
        return imagedataToSVG(imgd, finalOptions, palette)
    } // End of imageToSVG()

    // Tracing ImageData, then returning the SVG String
    fun imagedataToSVG(
        imgd: ImageData,
        options: HashMap<String?, Float>,
        palette: Array<ByteArray>?
    ): String {
        val finalOptions = checkoptions(options)
        val ii = imagedataToTracedata(imgd, finalOptions, palette)
        return getsvgstring(ii, finalOptions)
    } // End of imagedataToSVG()

    // Tracing ImageData, then returning IndexedImage with tracedata in layers
    fun imagedataToTracedata(
        imgd: ImageData,
        options: HashMap<String?, Float>,
        palette: Array<ByteArray>?
    ): IndexedImage {
        // 1. Color quantization
        val ii = colorquantization(imgd, palette, options)
        // 2. Layer separation and edge detection
        val rawlayers = layering(ii)
        // 3. Batch pathscan
        val bps = batchpathscan(
            rawlayers, floor(
                options["pathomit"]!!.toDouble()
            ).toInt().toFloat()
        )
        // 4. Batch interpollation
        val bis: ArrayList<ArrayList<ArrayList<Array<Double>>>> = batchinternodes(bps)
        // 5. Batch tracing
        ii.layers = batchtracelayers(bis, options["ltres"]!!, options["qtres"]!!)
        return ii
    } // End of imagedataToTracedata()

    // creating options object, setting defaults for missing values
    fun checkoptions(options: HashMap<String?, Float>?): HashMap<String?, Float> {
        val finalOptions = options ?: HashMap()

        // Tracing
        if (!finalOptions.containsKey("ltres")) {
            finalOptions["ltres"] = 1f
        }
        if (!finalOptions.containsKey("qtres")) {
            finalOptions["qtres"] = 1f
        }
        if (!finalOptions.containsKey("pathomit")) {
            finalOptions["pathomit"] = 8f
        }
        // Color quantization
        if (!finalOptions.containsKey("colorsampling")) {
            finalOptions["colorsampling"] = 1f
        }
        if (!finalOptions.containsKey("numberofcolors")) {
            finalOptions["numberofcolors"] = 16f
        }
        if (!finalOptions.containsKey("mincolorratio")) {
            finalOptions["mincolorratio"] = 0.02f
        }
        if (!finalOptions.containsKey("colorquantcycles")) {
            finalOptions["colorquantcycles"] = 3f
        }
        // SVG rendering
        if (!finalOptions.containsKey("scale")) {
            finalOptions["scale"] = 1f
        }
        if (!finalOptions.containsKey("simplifytolerance")) {
            finalOptions["simplifytolerance"] = 0f
        }
        if (!finalOptions.containsKey("roundcoords")) {
            finalOptions["roundcoords"] = 1f
        }
        if (!finalOptions.containsKey("lcpr")) {
            finalOptions["lcpr"] = 0f
        }
        if (!finalOptions.containsKey("qcpr")) {
            finalOptions["qcpr"] = 0f
        }
        if (!finalOptions.containsKey("desc")) {
            finalOptions["desc"] = 1f
        }
        if (!finalOptions.containsKey("viewbox")) {
            finalOptions["viewbox"] = 0f
        }
        // Blur
        if (!finalOptions.containsKey("blurradius")) {
            finalOptions["blurradius"] = 0f
        }
        if (!finalOptions.containsKey("blurdelta")) {
            finalOptions["blurdelta"] = 20f
        }
        return finalOptions
    } // End of checkoptions()

    ////////////////////////////////////////////////////////////
    //
    //  Vectorizing functions
    //
    ////////////////////////////////////////////////////////////
    // 1. Color quantization repeated "cycles" times, based on K-means clustering
    // https://en.wikipedia.org/wiki/Color_quantization    https://en.wikipedia.org/wiki/K-means_clustering
    fun colorquantization(
        imgd: ImageData,
        palette: Array<ByteArray>?,
        options: HashMap<String?, Float>
    ): IndexedImage {
        var imageData = imgd
        var finalPalette = palette
        val numberofcolors = floor(options["numberofcolors"]!!.toDouble())
            .toInt()
        val minratio = options["mincolorratio"]!!
        val cycles = floor(options["colorquantcycles"]!!.toDouble())
            .toInt()
        // Creating indexed color array arr which has a boundary filled with -1 in every direction
        val arr = Array(imageData.height + 2) { IntArray(imageData.width + 2) }
        for (j in 0 until imageData.height + 2) {
            arr[j][0] = -1
            arr[j][imageData.width + 1] = -1
        }
        for (i in 0 until imageData.width + 2) {
            arr[0][i] = -1
            arr[imageData.height + 1][i] = -1
        }
        var idx: Int
        var cd: Int
        var cdl: Int
        var ci: Int
        var c1: Int
        var c2: Int
        var c3: Int
        var c4: Int

        // Use custom palette if pal is defined or sample or generate custom length palette
        if (finalPalette == null) {
            finalPalette = if (options["colorsampling"] != 0f) {
                samplepalette(numberofcolors, imageData)
            } else {
                generatepalette(numberofcolors)
            }
        }

        // Selective Gaussian blur preprocessing
        if (options["blurradius"]!! > 0) {
            imageData = blur(imageData, options["blurradius"]!!, options["blurdelta"]!!)
        }
        val paletteacc = Array(finalPalette.size) { LongArray(5) }

        // Repeat clustering step "cycles" times
        for (cnt in 0 until cycles) {

            // Average colors from the second iteration
            if (cnt > 0) {
                // averaging paletteacc for palette
                var ratio: Float
                for (k in finalPalette.indices) {
                    // averaging
                    if (paletteacc[k][3] > 0) {
                        finalPalette[k][0] =
                            (-128 + paletteacc[k][0] / paletteacc[k][4]).toByte()
                        finalPalette[k][1] =
                            (-128 + paletteacc[k][1] / paletteacc[k][4]).toByte()
                        finalPalette[k][2] =
                            (-128 + paletteacc[k][2] / paletteacc[k][4]).toByte()
                        finalPalette[k][3] =
                            (-128 + paletteacc[k][3] / paletteacc[k][4]).toByte()
                    }
                    ratio =
                        (paletteacc[k][4].toDouble() / (imageData.width * imageData.height).toDouble()).toFloat()

                    // Randomizing a color, if there are too few pixels and there will be a new cycle
                    if (ratio < minratio && cnt < cycles - 1) {
                        finalPalette[k][0] =
                            (-128 + floor(Math.random() * 255)).toInt().toByte()
                        finalPalette[k][1] =
                            (-128 + floor(Math.random() * 255)).toInt().toByte()
                        finalPalette[k][2] =
                            (-128 + floor(Math.random() * 255)).toInt().toByte()
                        finalPalette[k][3] =
                            (-128 + floor(Math.random() * 255)).toInt().toByte()
                    }
                } // End of palette loop
            } // End of Average colors from the second iteration

            // Reseting palette accumulator for averaging
            for (i in finalPalette.indices) {
                paletteacc[i][0] = 0
                paletteacc[i][1] = 0
                paletteacc[i][2] = 0
                paletteacc[i][3] = 0
                paletteacc[i][4] = 0
            }

            // loop through all pixels
            for (j in 0 until imageData.height) {
                for (i in 0 until imageData.width) {
                    idx = (j * imageData.width + i) * 4

                    // find closest color from palette by measuring (rectilinear) color distance between this pixel and all palette colors
                    cdl = 256 + 256 + 256 + 256
                    ci = 0
                    for (k in finalPalette.indices) {

                        // In my experience, https://en.wikipedia.org/wiki/Rectilinear_distance works better than https://en.wikipedia.org/wiki/Euclidean_distance
                        c1 = abs((finalPalette[k][0] - imageData.data[idx]).toDouble())
                            .toInt()
                        c2 = abs((finalPalette[k][1] - imageData.data[idx + 1]).toDouble())
                            .toInt()
                        c3 = abs((finalPalette[k][2] - imageData.data[idx + 2]).toDouble())
                            .toInt()
                        c4 = abs((finalPalette[k][3] - imageData.data[idx + 3]).toDouble())
                            .toInt()
                        cd =
                            c1 + c2 + c3 + c4 * 4 // weighted alpha seems to help images with transparency

                        // Remember this color if this is the closest yet
                        if (cd < cdl) {
                            cdl = cd
                            ci = k
                        }
                    } // End of palette loop

                    // add to palettacc
                    paletteacc[ci][0] += (128 + imageData.data[idx]).toLong()
                    paletteacc[ci][1] += (128 + imageData.data[idx + 1]).toLong()
                    paletteacc[ci][2] += (128 + imageData.data[idx + 2]).toLong()
                    paletteacc[ci][3] += (128 + imageData.data[idx + 3]).toLong()
                    paletteacc[ci][4]++
                    arr[j + 1][i + 1] = ci
                } // End of i loop
            } // End of j loop
        } // End of Repeat clustering step "cycles" times
        return IndexedImage(arr, finalPalette)
    } // End of colorquantization

    // Generating a palette with numberofcolors, array[numberofcolors][4] where [i][0] = R ; [i][1] = G ; [i][2] = B ; [i][3] = A
    fun generatepalette(numberofcolors: Int): Array<ByteArray> {
        val palette = Array(numberofcolors) { ByteArray(4) }
        if (numberofcolors < 8) {

            // Grayscale
            val graystep = 255.0 / (numberofcolors - 1)
            for (ccnt in 0 until numberofcolors) {
                palette[ccnt][0] = (-128 + Math.round(ccnt * graystep)).toByte()
                palette[ccnt][1] = (-128 + Math.round(ccnt * graystep)).toByte()
                palette[ccnt][2] = (-128 + Math.round(ccnt * graystep)).toByte()
                palette[ccnt][3] = 127.toByte()
            }
        } else {

            // RGB color cube
            val colorqnum =
                floor(
                    numberofcolors.toDouble().pow(1.0 / 3.0)
                ).toInt() // Number of points on each edge on the RGB color cube
            val colorstep =
                floor((255 / (colorqnum - 1)).toDouble()).toInt() // distance between points
            var ccnt = 0
            for (rcnt in 0 until colorqnum) {
                for (gcnt in 0 until colorqnum) {
                    for (bcnt in 0 until colorqnum) {
                        palette[ccnt][0] = (-128 + rcnt * colorstep).toByte()
                        palette[ccnt][1] = (-128 + gcnt * colorstep).toByte()
                        palette[ccnt][2] = (-128 + bcnt * colorstep).toByte()
                        palette[ccnt][3] = 127.toByte()
                        ccnt++
                    } // End of blue loop
                } // End of green loop
            } // End of red loop

            // Rest is random
            for (rcnt in ccnt until numberofcolors) {
                palette[ccnt][0] = (-128 + floor(Math.random() * 255)).toInt().toByte()
                palette[ccnt][1] = (-128 + floor(Math.random() * 255)).toInt().toByte()
                palette[ccnt][2] = (-128 + floor(Math.random() * 255)).toInt().toByte()
                palette[ccnt][3] = (-128 + floor(Math.random() * 255)).toInt().toByte()
            }
        } // End of numberofcolors check
        return palette
    }

    // End of generatepalette()
    fun samplepalette(numberofcolors: Int, imgd: ImageData): Array<ByteArray> {
        var idx: Int
        val palette = Array(numberofcolors) { ByteArray(4) }
        for (i in 0 until numberofcolors) {
            idx = (floor(Math.random() * imgd.data.size / 4) * 4).toInt()
            palette[i][0] = imgd.data[idx]
            palette[i][1] = imgd.data[idx + 1]
            palette[i][2] = imgd.data[idx + 2]
            palette[i][3] = imgd.data[idx + 3]
        }
        return palette
    } // End of samplepalette()

    // 2. Layer separation and edge detection
    // Edge node types ( ▓:light or 1; ░:dark or 0 )
    // 12  ░░  ▓░  ░▓  ▓▓  ░░  ▓░  ░▓  ▓▓  ░░  ▓░  ░▓  ▓▓  ░░  ▓░  ░▓  ▓▓
    // 48  ░░  ░░  ░░  ░░  ░▓  ░▓  ░▓  ░▓  ▓░  ▓░  ▓░  ▓░  ▓▓  ▓▓  ▓▓  ▓▓
    //     0   1   2   3   4   5   6   7   8   9   10  11  12  13  14  15
    //
    fun layering(ii: IndexedImage): Array<Array<IntArray>> {
        // Creating layers for each indexed color in arr
        var `val`: Int
        val aw = ii.array[0].size
        val ah = ii.array.size
        var n1: Int
        var n2: Int
        var n3: Int
        var n4: Int
        var n5: Int
        var n6: Int
        var n7: Int
        var n8: Int
        val layers = Array(
            ii.palette!!.size
        ) { Array(ah) { IntArray(aw) } }

        // Looping through all pixels and calculating edge node type
        for (j in 1 until ah - 1) {
            for (i in 1 until aw - 1) {

                // This pixel's indexed color
                `val` = ii.array[j][i]

                // Are neighbor pixel colors the same?
                n1 = if (ii.array[j - 1][i - 1] == `val`) 1 else 0
                n2 = if (ii.array[j - 1][i] == `val`) 1 else 0
                n3 = if (ii.array[j - 1][i + 1] == `val`) 1 else 0
                n4 = if (ii.array[j][i - 1] == `val`) 1 else 0
                n5 = if (ii.array[j][i + 1] == `val`) 1 else 0
                n6 = if (ii.array[j + 1][i - 1] == `val`) 1 else 0
                n7 = if (ii.array[j + 1][i] == `val`) 1 else 0
                n8 = if (ii.array[j + 1][i + 1] == `val`) 1 else 0

                // this pixel"s type and looking back on previous pixels
                layers[`val`][j + 1][i + 1] = 1 + n5 * 2 + n8 * 4 + n7 * 8
                if (n4 == 0) {
                    layers[`val`][j + 1][i] = 2 + n7 * 4 + n6 * 8
                }
                if (n2 == 0) {
                    layers[`val`][j][i + 1] = n3 * 2 + n5 * 4 + 8
                }
                if (n1 == 0) {
                    layers[`val`][j][i] = n2 * 2 + 4 + n4 * 8
                }
            } // End of i loop
        } // End of j loop
        return layers
    } // End of layering()

    // 3. Walking through an edge node array, discarding edge node types 0 and 15 and creating paths from the rest.
    // Walk directions (dir): 0 > ; 1 ^ ; 2 < ; 3 v
    // Edge node types ( ▓:light or 1; ░:dark or 0 )
    // ░░  ▓░  ░▓  ▓▓  ░░  ▓░  ░▓  ▓▓  ░░  ▓░  ░▓  ▓▓  ░░  ▓░  ░▓  ▓▓
    // ░░  ░░  ░░  ░░  ░▓  ░▓  ░▓  ░▓  ▓░  ▓░  ▓░  ▓░  ▓▓  ▓▓  ▓▓  ▓▓
    // 0   1   2   3   4   5   6   7   8   9   10  11  12  13  14  15
    //
    fun pathscan(arr: Array<IntArray>, pathomit: Float): ArrayList<ArrayList<Array<Int?>>> {
        val paths = ArrayList<ArrayList<Array<Int?>>>()
        var thispath: ArrayList<Array<Int?>>
        var px: Int
        var py: Int
        val w = arr[0].size
        val h = arr.size
        var dir: Int
        var pathfinished: Boolean
        var holepath: Boolean
        var lookuprow: ByteArray
        for (j in 0 until h) {
            for (i in 0 until w) {
                if (arr[j][i] != 0 && arr[j][i] != 15) {

                    // Init
                    px = i
                    py = j
                    paths.add(ArrayList())
                    thispath = paths[paths.size - 1]
                    pathfinished = false

                    // fill paths will be drawn, but hole paths are also required to remove unnecessary edge nodes
                    dir = pathscan_dir_lookup[arr[py][px]].toInt()
                    holepath = pathscan_holepath_lookup[arr[py][px]]

                    // Path points loop
                    while (!pathfinished) {

                        // New path point
                        thispath.add(arrayOfNulls(3))
                        thispath[thispath.size - 1][0] = px - 1
                        thispath[thispath.size - 1][1] = py - 1
                        thispath[thispath.size - 1][2] = arr[py][px]

                        // Next: look up the replacement, direction and coordinate changes = clear this cell, turn if required, walk forward
                        lookuprow = pathscan_combined_lookup[arr[py][px]][dir]
                        arr[py][px] = lookuprow[0].toInt()
                        dir = lookuprow[1].toInt()
                        px += lookuprow[2].toInt()
                        py += lookuprow[3].toInt()

                        // Close path
                        if (px - 1 == thispath[0][0] && py - 1 == thispath[0][1]) {
                            pathfinished = true
                            // Discarding 'hole' type paths and paths shorter than pathomit
                            if (holepath || thispath.size < pathomit) {
                                paths.remove(thispath)
                            }
                        }
                    } // End of Path points loop
                } // End of Follow path
            } // End of i loop
        } // End of j loop
        return paths
    } // End of pathscan()

    // 3. Batch pathscan
    fun batchpathscan(
        layers: Array<Array<IntArray>>,
        pathomit: Float
    ): ArrayList<ArrayList<ArrayList<Array<Int?>>>> {
        val bpaths = ArrayList<ArrayList<ArrayList<Array<Int?>>>>()
        for (layer in layers) {
            bpaths.add(pathscan(layer, pathomit))
        }
        return bpaths
    }

    // 4. interpolating between path points for nodes with 8 directions ( East, SouthEast, S, SW, W, NW, N, NE )
    fun internodes(paths: ArrayList<ArrayList<Array<Int?>>>): ArrayList<ArrayList<Array<Double>>> {
        val ins = ArrayList<ArrayList<Array<Double>>>()
        var thisinp: ArrayList<Array<Double>>
        var thispoint: Array<Double>
        val nextpoint = arrayOfNulls<Double>(2)
        var pp1: Array<Int?>
        var pp2: Array<Int?>
        var pp3: Array<Int?>
        var palen: Int
        var nextidx: Int
        var nextidx2: Int

        // paths loop
        for (pacnt in paths.indices) {
            ins.add(ArrayList())
            thisinp = ins[ins.size - 1]
            palen = paths[pacnt].size
            // pathpoints loop
            for (pcnt in 0 until palen) {

                // interpolate between two path points
                nextidx = (pcnt + 1) % palen
                nextidx2 = (pcnt + 2) % palen
                thisinp.add(arrayOf(0.0, 0.0, 0.0))
                thispoint = thisinp[thisinp.size - 1]
                pp1 = paths[pacnt][pcnt]
                pp2 = paths[pacnt][nextidx]
                pp3 = paths[pacnt][nextidx2]
                thispoint[0] = (pp1[0]!! + pp2[0]!!) / 2.0
                thispoint[1] = (pp1[1]!! + pp2[1]!!) / 2.0
                nextpoint[0] = (pp2[0]!! + pp3[0]!!) / 2.0
                nextpoint[1] = (pp2[1]!! + pp3[1]!!) / 2.0

                // line segment direction to the next point
                if (thispoint[0] < nextpoint[0]!!) {
                    if (thispoint[1] < nextpoint[1]!!) {
                        thispoint[2] = 1.0
                    } // SouthEast
                    else if (thispoint[1] > nextpoint[1]!!) {
                        thispoint[2] = 7.0
                    } // NE
                    else {
                        thispoint[2] = 0.0
                    } // E
                } else if (thispoint[0] > nextpoint[0]!!) {
                    if (thispoint[1] < nextpoint[1]!!) {
                        thispoint[2] = 3.0
                    } // SW
                    else if (thispoint[1] > nextpoint[1]!!) {
                        thispoint[2] = 5.0
                    } // NW
                    else {
                        thispoint[2] = 4.0
                    } // W
                } else {
                    if (thispoint[1] < nextpoint[1]!!) {
                        thispoint[2] = 2.0
                    } // S
                    else if (thispoint[1] > nextpoint[1]!!) {
                        thispoint[2] = 6.0
                    } // N
                    else {
                        thispoint[2] = 8.0
                    } // center, this should not happen
                }
            } // End of pathpoints loop
        } // End of paths loop
        return ins
    } // End of internodes()

    // 4. Batch interpollation
    fun batchinternodes(bpaths: ArrayList<ArrayList<ArrayList<Array<Int?>>>>): ArrayList<ArrayList<ArrayList<Array<Double>>>> {
        val binternodes = ArrayList<ArrayList<ArrayList<Array<Double>>>>()
        for (k in bpaths.indices) {
            binternodes.add(internodes(bpaths[k]))
        }
        return binternodes
    }

    fun tracepath(
        path: ArrayList<Array<Double>>,
        ltreshold: Float,
        qtreshold: Float
    ): ArrayList<Array<Double?>> {
        var pcnt = 0
        var seqend: Int
        var segtype1: Double?
        var segtype2: Double?
        val smp = ArrayList<Array<Double?>>()
        //Double [] thissegment;
        val pathlength = path.size
        while (pcnt < pathlength) {
            // 5.1. Find sequences of points with only 2 segment types
            segtype1 = path[pcnt][2]
            segtype2 = -1.0
            seqend = pcnt + 1
            while ((path[seqend][2] == segtype1 || path[seqend][2] == segtype2 || segtype2 == -1.0) && seqend < pathlength - 1) {
                if (path[seqend][2] != segtype1 && segtype2 == -1.0) {
                    segtype2 = path[seqend][2]
                }
                seqend++
            }
            if (seqend == pathlength - 1) {
                seqend = 0
            }

            // 5.2. - 5.6. Split sequence and recursively apply 5.2. - 5.6. to startpoint-splitpoint and splitpoint-endpoint sequences
            smp.addAll(fitseq(path, ltreshold, qtreshold, pcnt, seqend))
            // 5.7. TODO? If splitpoint-endpoint is a spline, try to add new points from the next sequence

            // forward pcnt;
            pcnt = if (seqend > 0) {
                seqend
            } else {
                pathlength
            }
        } // End of pcnt loop
        return smp
    } // End of tracepath()

    // 5.2. - 5.6. recursively fitting a straight or quadratic line segment on this sequence of path nodes,
    // called from tracepath()
    fun fitseq(
        path: ArrayList<Array<Double>>,
        ltreshold: Float,
        qtreshold: Float,
        seqstart: Int,
        seqend: Int
    ): ArrayList<Array<Double?>> {
        var segment = ArrayList<Array<Double?>>()
        val thissegment: Array<Double?>
        val pathlength = path.size

        // return if invalid seqend
        if (seqend > pathlength || seqend < 0) {
            return segment
        }
        var errorpoint = seqstart
        var curvepass = true
        var px: Double
        var py: Double
        var dist2: Double
        var errorval = 0.0
        var tl = (seqend - seqstart).toDouble()
        if (tl < 0) {
            tl += pathlength.toDouble()
        }
        val vx = (path[seqend][0] - path[seqstart][0]) / tl
        val vy = (path[seqend][1] - path[seqstart][1]) / tl

        // 5.2. Fit a straight line on the sequence
        var pcnt = (seqstart + 1) % pathlength
        var pl: Double
        while (pcnt != seqend) {
            pl = (pcnt - seqstart).toDouble()
            if (pl < 0) {
                pl += pathlength.toDouble()
            }
            px = path[seqstart][0] + vx * pl
            py = path[seqstart][1] + vy * pl
            dist2 =
                (path[pcnt][0] - px) * (path[pcnt][0] - px) + (path[pcnt][1] - py) * (path[pcnt][1] - py)
            if (dist2 > ltreshold) {
                curvepass = false
            }
            if (dist2 > errorval) {
                errorpoint = pcnt
                errorval = dist2
            }
            pcnt = (pcnt + 1) % pathlength
        }

        // return straight line if fits
        if (curvepass) {
            segment.add(arrayOfNulls(7))
            thissegment = segment[segment.size - 1]
            thissegment[0] = 1.0
            thissegment[1] = path[seqstart][0]
            thissegment[2] = path[seqstart][1]
            thissegment[3] = path[seqend][0]
            thissegment[4] = path[seqend][1]
            thissegment[5] = 0.0
            thissegment[6] = 0.0
            return segment
        }

        // 5.3. If the straight line fails (an error>ltreshold), find the point with the biggest error
        val fitpoint = errorpoint
        curvepass = true
        errorval = 0.0

        // 5.4. Fit a quadratic spline through this point, measure errors on every point in the sequence
        // helpers and projecting to get control point
        var t = (fitpoint - seqstart) / tl
        var t1 = (1.0 - t) * (1.0 - t)
        var t2 = 2.0 * (1.0 - t) * t
        var t3 = t * t
        val cpx = (t1 * path[seqstart][0] + t3 * path[seqend][0] - path[fitpoint][0]) / -t2
        val cpy = (t1 * path[seqstart][1] + t3 * path[seqend][1] - path[fitpoint][1]) / -t2

        // Check every point
        pcnt = seqstart + 1
        while (pcnt != seqend) {
            t = (pcnt - seqstart) / tl
            t1 = (1.0 - t) * (1.0 - t)
            t2 = 2.0 * (1.0 - t) * t
            t3 = t * t
            px = t1 * path[seqstart][0] + t2 * cpx + t3 * path[seqend][0]
            py = t1 * path[seqstart][1] + t2 * cpy + t3 * path[seqend][1]
            dist2 =
                (path[pcnt][0] - px) * (path[pcnt][0] - px) + (path[pcnt][1] - py) * (path[pcnt][1] - py)
            if (dist2 > qtreshold) {
                curvepass = false
            }
            if (dist2 > errorval) {
                errorpoint = pcnt
                errorval = dist2
            }
            pcnt = (pcnt + 1) % pathlength
        }

        // return spline if fits
        if (curvepass) {
            segment.add(arrayOfNulls(7))
            thissegment = segment[segment.size - 1]
            thissegment[0] = 2.0
            thissegment[1] = path[seqstart][0]
            thissegment[2] = path[seqstart][1]
            thissegment[3] = cpx
            thissegment[4] = cpy
            thissegment[5] = path[seqend][0]
            thissegment[6] = path[seqend][1]
            return segment
        }

        // 5.5. If the spline fails (an error>qtreshold), find the point with the biggest error,
        // set splitpoint = (fitting point + errorpoint)/2
        val splitpoint = (fitpoint + errorpoint) / 2

        // 5.6. Split sequence and recursively apply 5.2. - 5.6. to startpoint-splitpoint and splitpoint-endpoint sequences
        segment = fitseq(path, ltreshold, qtreshold, seqstart, splitpoint)
        segment.addAll(fitseq(path, ltreshold, qtreshold, splitpoint, seqend))
        return segment
    } // End of fitseq()

    // 5. Batch tracing paths
    fun batchtracepaths(
        internodepaths: ArrayList<ArrayList<Array<Double>>>,
        ltres: Float,
        qtres: Float
    ): ArrayList<ArrayList<Array<Double?>>> {
        val btracedpaths = ArrayList<ArrayList<Array<Double?>>>()
        for (k in internodepaths.indices) {
            btracedpaths.add(tracepath(internodepaths[k], ltres, qtres))
        }
        return btracedpaths
    }

    // 5. tracepath() : recursively trying to fit straight and quadratic spline segments on the 8 direction internode path
    // 5.1. Find sequences of points with only 2 segment types
    // 5.2. Fit a straight line on the sequence
    // 5.3. If the straight line fails (an error>ltreshold), find the point with the biggest error
    // 5.4. Fit a quadratic spline through errorpoint (project this to get controlpoint), then measure errors on every point in the sequence
    // 5.5. If the spline fails (an error>qtreshold), find the point with the biggest error, set splitpoint = (fitting point + errorpoint)/2
    // 5.6. Split sequence and recursively apply 5.2. - 5.7. to startpoint-splitpoint and splitpoint-endpoint sequences
    // 5.7. TODO? If splitpoint-endpoint is a spline, try to add new points from the next sequence
    // This returns an SVG Path segment as a double[7] where
    // segment[0] ==1.0 linear  ==2.0 quadratic interpolation
    // segment[1] , segment[2] : x1 , y1
    // segment[3] , segment[4] : x2 , y2 ; middle point of Q curve, endpoint of L line
    // segment[5] , segment[6] : x3 , y3 for Q curve, should be 0.0 , 0.0 for L line
    //
    // path type is discarded, no check for path.size < 3 , which should not happen
    // 5. Batch tracing layers
    fun batchtracelayers(
        binternodes: ArrayList<ArrayList<ArrayList<Array<Double>>>>,
        ltres: Float,
        qtres: Float
    ): ArrayList<ArrayList<ArrayList<Array<Double?>>>> {
        val btbis = ArrayList<ArrayList<ArrayList<Array<Double?>>>>()
        for (k in binternodes.indices) {
            btbis.add(batchtracepaths(binternodes[k], ltres, qtres))
        }
        return btbis
    }

    fun roundtodec(`val`: Float, places: Float): Float {
        return (Math.round(`val` * 10.0.pow(places.toDouble())) / 10.0.pow(places.toDouble())).toFloat()
    }

    // Getting SVG path element string from a traced path
    fun svgpathstring(
        sb: StringBuilder,
        desc: String?,
        segments: ArrayList<Array<Double?>>,
        colorstr: String?,
        options: HashMap<String?, Float>
    ) {
        val scale = options["scale"]!!
        val lcpr = options["lcpr"]!!
        val qcpr = options["qcpr"]!!
        val roundcoords = floor(options["roundcoords"]!!.toDouble())
            .toFloat()
        // Path
        sb.append("<path ").append(desc).append(colorstr).append("d=\"").append("M ").append(
            segments[0][1]!! * scale
        ).append(" ").append(segments[0][2]!! * scale).append(" ")
        if (roundcoords == -1f) {
            for (pcnt in segments.indices) {
                if (segments[pcnt][0] == 1.0) {
                    sb.append("L ").append(segments[pcnt][3]!! * scale).append(" ").append(
                        segments[pcnt][4]!! * scale
                    ).append(" ")
                } else {
                    sb.append("Q ").append(segments[pcnt][3]!! * scale).append(" ").append(
                        segments[pcnt][4]!! * scale
                    ).append(" ").append(segments[pcnt][5]!! * scale).append(" ").append(
                        segments[pcnt][6]!! * scale
                    ).append(" ")
                }
            }
        } else {
            for (pcnt in segments.indices) {
                if (segments[pcnt][0] == 1.0) {
                    sb.append("L ").append(
                        roundtodec(
                            (segments[pcnt][3]!! * scale).toFloat(),
                            roundcoords
                        )
                    ).append(" ")
                        .append(
                            roundtodec(
                                (segments[pcnt][4]!! * scale).toFloat(),
                                roundcoords
                            )
                        ).append(" ")
                } else {
                    sb.append("Q ").append(
                        roundtodec(
                            (segments[pcnt][3]!! * scale).toFloat(),
                            roundcoords
                        )
                    ).append(" ")
                        .append(
                            roundtodec(
                                (segments[pcnt][4]!! * scale).toFloat(),
                                roundcoords
                            )
                        ).append(" ")
                        .append(
                            roundtodec(
                                (segments[pcnt][5]!! * scale).toFloat(),
                                roundcoords
                            )
                        ).append(" ")
                        .append(
                            roundtodec(
                                (segments[pcnt][6]!! * scale).toFloat(),
                                roundcoords
                            )
                        ).append(" ")
                }
            }
        } // End of roundcoords check
        sb.append("Z\" />")

        // Rendering control points
        for (pcnt in segments.indices) {
            if (lcpr > 0 && segments[pcnt][0] == 1.0) {
                sb.append("<circle cx=\"").append(segments[pcnt][3]!! * scale)
                    .append("\" cy=\"").append(
                        segments[pcnt][4]!! * scale
                    ).append("\" r=\"").append(lcpr).append("\" fill=\"white\" stroke-width=\"")
                    .append(lcpr * 0.2).append("\" stroke=\"black\" />")
            }
            if (qcpr > 0 && segments[pcnt][0] == 2.0) {
                sb.append("<circle cx=\"").append(segments[pcnt][3]!! * scale)
                    .append("\" cy=\"").append(
                        segments[pcnt][4]!! * scale
                    ).append("\" r=\"").append(qcpr).append("\" fill=\"cyan\" stroke-width=\"")
                    .append(qcpr * 0.2).append("\" stroke=\"black\" />")
                sb.append("<circle cx=\"").append(segments[pcnt][5]!! * scale)
                    .append("\" cy=\"").append(
                        segments[pcnt][6]!! * scale
                    ).append("\" r=\"").append(qcpr).append("\" fill=\"white\" stroke-width=\"")
                    .append(qcpr * 0.2).append("\" stroke=\"black\" />")
                sb.append("<line x1=\"").append(segments[pcnt][1]!! * scale).append("\" y1=\"")
                    .append(
                        segments[pcnt][2]!! * scale
                    ).append("\" x2=\"").append(segments[pcnt][3]!! * scale).append("\" y2=\"")
                    .append(
                        segments[pcnt][4]!! * scale
                    ).append("\" stroke-width=\"").append(qcpr * 0.2)
                    .append("\" stroke=\"cyan\" />")
                sb.append("<line x1=\"").append(segments[pcnt][3]!! * scale).append("\" y1=\"")
                    .append(
                        segments[pcnt][4]!! * scale
                    ).append("\" x2=\"").append(segments[pcnt][5]!! * scale).append("\" y2=\"")
                    .append(
                        segments[pcnt][6]!! * scale
                    ).append("\" stroke-width=\"").append(qcpr * 0.2)
                    .append("\" stroke=\"cyan\" />")
            } // End of quadratic control points
        }
    } // End of svgpathstring()

    // Converting tracedata to an SVG string, paths are drawn according to a Z-index
    // the optional lcpr and qcpr are linear and quadratic control point radiuses
    fun getsvgstring(ii: IndexedImage, options: HashMap<String?, Float>): String {
        val finalOptions = checkoptions(options)
        // SVG start
        val w = (ii.width * finalOptions["scale"]!!).toInt()
        val h = (ii.height * finalOptions["scale"]!!).toInt()
        val viewboxorviewport =
            if (finalOptions["viewbox"] != 0f) "viewBox=\"0 0 $w $h\" " else "width=\"$w\" height=\"$h\" "
        val svgstr =
            StringBuilder("<svg " + viewboxorviewport + "version=\"1.1\" xmlns=\"http://www.w3.org/2000/svg\" ")
        if (finalOptions["desc"] != 0f) {
            svgstr.append("desc=\"Created with ImageTracerAndroid.java version $versionnumber\" ")
        }
        svgstr.append(">")

        // creating Z-index
        val zindex = TreeMap<Double, Array<Int?>>()
        var label: Double
        // Layer loop
        for (k in ii.layers!!.indices) {

            // Path loop
            for (pcnt in ii.layers!![k].indices) {

                // Label (Z-index key) is the startpoint of the path, linearized
                label = ii.layers!![k][pcnt][0][2]!! * w + ii.layers!![k][pcnt][0][1]!!
                // Creating new list if required
                if (!zindex.containsKey(label)) {
                    zindex[label] = arrayOfNulls(2)
                }
                // Adding layer and path number to list
                zindex[label]!![0] = k
                zindex[label]!![1] = pcnt
            } // End of path loop
        } // End of layer loop

        // Sorting Z-index is not required, TreeMap is sorted automatically

        // Drawing
        // Z-index loop
        var thisdesc: String
        for ((_, value) in zindex) {
            thisdesc = if (finalOptions["desc"] != 0f) {
                "desc=\"l " + value[0] + " p " + value[1] + "\" "
            } else {
                ""
            }
            svgpathstring(
                svgstr,
                thisdesc,
                ii.layers!![value[0]!!][value[1]!!],
                tosvgcolorstr(ii.palette!![value[0]!!]),
                finalOptions
            )
        }

        // SVG End
        svgstr.append("</svg>")
        return svgstr.toString()
    } // End of getsvgstring()

    ////////////////////////////////////////////////////////////
    //
    //  SVG Drawing functions
    //
    ////////////////////////////////////////////////////////////
    fun tosvgcolorstr(c: ByteArray): String {
        return "fill=\"rgb(" + (c[0] + 128) + "," + (c[1] + 128) + "," + (c[2] + 128) + ")\" stroke=\"rgb(" + (c[0] + 128) + "," + (c[1] + 128) + "," + (c[2] + 128) + ")\" stroke-width=\"1\" opacity=\"" + (c[3] + 128) / 255.0 + "\" "
    }

    // Selective Gaussian blur for preprocessing
    fun blur(imgd: ImageData, rad: Float, del: Float): ImageData {
        var i: Int
        var k: Int
        var d: Int
        var idx: Int
        var racc: Double
        var gacc: Double
        var bacc: Double
        var aacc: Double
        var wacc: Double
        val imgd2 = ImageData(imgd.width, imgd.height, ByteArray(imgd.width * imgd.height * 4))

        // radius and delta limits, this kernel
        var radius = floor(rad.toDouble()).toInt()
        if (radius < 1) {
            return imgd
        }
        if (radius > 5) {
            radius = 5
        }
        var delta = abs(del.toDouble()).toInt()
        if (delta > 1024) {
            delta = 1024
        }
        val thisgk = gks[radius - 1]

        // loop through all pixels, horizontal blur
        var j = 0
        while (j < imgd.height) {
            i = 0
            while (i < imgd.width) {
                racc = 0.0
                gacc = 0.0
                bacc = 0.0
                aacc = 0.0
                wacc = 0.0
                // gauss kernel loop
                k = -radius
                while (k < radius + 1) {

                    // add weighted color values
                    if (i + k > 0 && i + k < imgd.width) {
                        idx = (j * imgd.width + i + k) * 4
                        racc += imgd.data[idx] * thisgk[k + radius]
                        gacc += imgd.data[idx + 1] * thisgk[k + radius]
                        bacc += imgd.data[idx + 2] * thisgk[k + radius]
                        aacc += imgd.data[idx + 3] * thisgk[k + radius]
                        wacc += thisgk[k + radius]
                    }
                    k++
                }
                // The new pixel
                idx = (j * imgd.width + i) * 4
                imgd2.data[idx] = floor(racc / wacc).toInt().toByte()
                imgd2.data[idx + 1] = floor(gacc / wacc).toInt().toByte()
                imgd2.data[idx + 2] = floor(bacc / wacc).toInt().toByte()
                imgd2.data[idx + 3] = floor(aacc / wacc).toInt().toByte()
                i++
            }
            j++
        }

        // copying the half blurred imgd2
        val himgd = imgd2.data.clone()

        // loop through all pixels, vertical blur
        j = 0
        while (j < imgd.height) {
            i = 0
            while (i < imgd.width) {
                racc = 0.0
                gacc = 0.0
                bacc = 0.0
                aacc = 0.0
                wacc = 0.0
                // gauss kernel loop
                k = -radius
                while (k < radius + 1) {

                    // add weighted color values
                    if (j + k > 0 && j + k < imgd.height) {
                        idx = ((j + k) * imgd.width + i) * 4
                        racc += himgd[idx] * thisgk[k + radius]
                        gacc += himgd[idx + 1] * thisgk[k + radius]
                        bacc += himgd[idx + 2] * thisgk[k + radius]
                        aacc += himgd[idx + 3] * thisgk[k + radius]
                        wacc += thisgk[k + radius]
                    }
                    k++
                }
                // The new pixel
                idx = (j * imgd.width + i) * 4
                imgd2.data[idx] = floor(racc / wacc).toInt().toByte()
                imgd2.data[idx + 1] = floor(gacc / wacc).toInt().toByte()
                imgd2.data[idx + 2] = floor(bacc / wacc).toInt().toByte()
                imgd2.data[idx + 3] = floor(aacc / wacc).toInt().toByte()
                i++
            }
            j++
        }

        // Selective blur: loop through all pixels
        j = 0
        while (j < imgd.height) {
            i = 0
            while (i < imgd.width) {
                idx = (j * imgd.width + i) * 4
                // d is the difference between the blurred and the original pixel
                d = (abs((imgd2.data[idx] - imgd.data[idx]).toDouble()) + abs(
                    (imgd2.data[idx + 1] - imgd.data[idx + 1]).toDouble()
                ) + abs((imgd2.data[idx + 2] - imgd.data[idx + 2]).toDouble()) + abs(
                    (imgd2.data[idx + 3] - imgd.data[idx + 3]).toDouble()
                )).toInt()
                // selective blur: if d>delta, put the original pixel back
                if (d > delta) {
                    imgd2.data[idx] = imgd.data[idx]
                    imgd2.data[idx + 1] = imgd.data[idx + 1]
                    imgd2.data[idx + 2] = imgd.data[idx + 2]
                    imgd2.data[idx + 3] = imgd.data[idx + 3]
                }
                i++
            }
            j++
        }
        return imgd2
    } // End of blur()
} // End of ImageTracerAndroid class