package com.t8rin.imagetoolbox.app

import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.FloatRange
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.graphics.ColorUtils
import androidx.core.graphics.drawable.toBitmap
import coil.compose.AsyncImage
import coil.imageLoader
import coil.request.ImageRequest
import coil.size.Size
import coil.transform.Transformation
import coil.util.DebugLogger
import com.gemalto.jp2.coil.Jpeg2000Decoder
import org.beyka.tiffbitmapfactory.TiffDecoder
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.random.Random

@Composable
fun MainActivity.Jp2Hypothesis() {
    var source by remember {
        mutableStateOf("")
    }

    var target by remember {
        mutableStateOf<String>("")
    }


    val imagePicker =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.PickVisualMedia()) {
            source = it?.toString() ?: ""
        }

    val pickImage: () -> Unit = {
        imagePicker.launch(PickVisualMediaRequest())
    }

    val imagePicker2 =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.PickVisualMedia()) {
            target = it?.toString() ?: ""
        }

    val pickImage2: () -> Unit = {
        imagePicker2.launch(PickVisualMediaRequest())
    }

    var intensity by remember {
        mutableStateOf(1f)
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        val imageLoader = remember {
            imageLoader.newBuilder().components {
                add(TiffDecoder.Factory(this@Jp2Hypothesis))
                add(Jpeg2000Decoder.Factory(this@Jp2Hypothesis))
            }.logger(DebugLogger()).build()
        }
        var fill by remember {
            mutableStateOf(true)
        }
        Row(
            modifier = Modifier.weight(1f)
        ) {
            AsyncImage(
                model = source,
                imageLoader = imageLoader,
                modifier = Modifier.weight(1f),
                contentDescription = null
            )
            AsyncImage(
                model = target,
                imageLoader = imageLoader,
                modifier = Modifier.weight(1f),
                contentDescription = null
            )
        }

        Row(
            modifier = Modifier.weight(1f)
        ) {
            AsyncImage(
                model = remember(source, target, intensity) {
                    ImageRequest.Builder(this@Jp2Hypothesis).allowHardware(false).data(source)
                        .transformations(
                            GenericTransformation { bmp ->
                                val source = bmp.copy(Bitmap.Config.ARGB_8888, true)
                                val target = imageLoader.newBuilder().build().execute(
                                    ImageRequest.Builder(this@Jp2Hypothesis).data(target).build()
                                ).drawable!!.toBitmap().copy(Bitmap.Config.ARGB_8888, true)
                                colorTransfer(target, source, intensity)
//                        val colors = Extractor().extract(
//                            imageLoader.newBuilder().build().execute(
//                                ImageRequest.Builder(this@Jp2Hypothesis).data(model2).build()
//                            ).drawable!!.toBitmap().copy(Bitmap.Config.ARGB_8888, true)
//                        )
//
//                        repeat(newBitmap.width) { x ->
//                            repeat(newBitmap.height) { y ->
//                                val color = newBitmap.getPixel(x, y)
//                                val target = colors.minBy { Color(it).distanceFrom(Color(color)) }
//                                newBitmap.setPixel(x, y, color.blend(target))
//                            }
//                        }
//
//                        newBitmap
                            }
                        ).build()
                },
                imageLoader = imageLoader,
                modifier = Modifier.weight(1f),
                contentDescription = null
            )
            AsyncImage(
                model = remember(source, target) {
                    ImageRequest.Builder(this@Jp2Hypothesis).allowHardware(false).data(source)
                        .transformations(
                            GenericTransformation { bmp ->
                                val source = bmp.copy(Bitmap.Config.ARGB_8888, true)
                                val target = imageLoader.newBuilder().build().execute(
                                    ImageRequest.Builder(this@Jp2Hypothesis).data(target).build()
                                ).drawable!!.toBitmap().copy(Bitmap.Config.ARGB_8888, true)
                                paletteTransfer(target, source)
//                        val colors = Extractor().extract(
//                            imageLoader.newBuilder().build().execute(
//                                ImageRequest.Builder(this@Jp2Hypothesis).data(model2).build()
//                            ).drawable!!.toBitmap().copy(Bitmap.Config.ARGB_8888, true)
//                        )
//
//                        repeat(newBitmap.width) { x ->
//                            repeat(newBitmap.height) { y ->
//                                val color = newBitmap.getPixel(x, y)
//                                val target = colors.minBy { Color(it).distanceFrom(Color(color)) }
//                                newBitmap.setPixel(x, y, color.blend(target))
//                            }
//                        }
//
//                        newBitmap
                            }
                        ).build()
                },
                imageLoader = imageLoader,
                modifier = Modifier.weight(1f),
                contentDescription = null
            )
        }

        Row {
            Button(onClick = pickImage) {
                Text("Source")
            }
            Button(onClick = pickImage2) {
                Text("Target")
            }
            Slider(value = intensity, onValueChange = { intensity = it }, valueRange = 0f..1f)
        }
    }
}

class GenericTransformation(
    val key: Any? = Random.nextInt(),
    val action: suspend (Bitmap, Size) -> Bitmap
) : Transformation {

    constructor(
        key: Any? = Random.nextInt(),
        action: suspend (Bitmap) -> Bitmap
    ) : this(
        key, { t, _ -> action(t) }
    )

    override val cacheKey: String
        get() = (action to key).hashCode().toString()

    override suspend fun transform(
        input: Bitmap,
        size: Size
    ): Bitmap = action(input, size)
}

fun Int.blend(
    color: Int,
    @FloatRange(from = 0.0, to = 1.0) fraction: Float = 0.5f
): Int = ColorUtils.blendARGB(this, color, fraction)

private fun Color.distanceFrom(color: Color): Float {
    return sqrt((red - color.red).pow(2) + (green - color.green).pow(2) + (blue - color.blue).pow(2))
}

fun paletteTransfer(source: Bitmap, target: Bitmap): Bitmap {
    val result = Bitmap.createBitmap(target.width, target.height, target.config)

    // Вычисление среднего и стандартного отклонения для источника
    val sourceMeanStd = calculateMeanAndStd(source)
    val sourceMean = sourceMeanStd.first
    val sourceStd = sourceMeanStd.second

    // Вычисление среднего и стандартного отклонения для цели
    val targetMeanStd = calculateMeanAndStd(target)
    val targetMean = targetMeanStd.first
    val targetStd = targetMeanStd.second

    // Применение цветового преобразования
    for (x in 0 until target.width) {
        for (y in 0 until target.height) {
            val targetPixel = target.getPixel(x, y)

            val r = android.graphics.Color.red(targetPixel)
            val g = android.graphics.Color.green(targetPixel)
            val b = android.graphics.Color.blue(targetPixel)

            val newR = ((r - targetMean[0]) * (sourceStd[0] / targetStd[0]) + sourceMean[0]).toInt()
                .coerceIn(0, 255)
            val newG = ((g - targetMean[1]) * (sourceStd[1] / targetStd[1]) + sourceMean[1]).toInt()
                .coerceIn(0, 255)
            val newB = ((b - targetMean[2]) * (sourceStd[2] / targetStd[2]) + sourceMean[2]).toInt()
                .coerceIn(0, 255)

            result.setPixel(x, y, android.graphics.Color.rgb(newR, newG, newB))
        }
    }

    return result
}

private fun calculateMeanAndStd(bitmap: Bitmap): Pair<FloatArray, FloatArray> {
    val mean = FloatArray(3)
    val std = FloatArray(3)
    val pixelCount = bitmap.width * bitmap.height

    // Вычисление среднего значения
    for (x in 0 until bitmap.width) {
        for (y in 0 until bitmap.height) {
            val pixel = bitmap.getPixel(x, y)
            mean[0] += android.graphics.Color.red(pixel).toFloat()
            mean[1] += android.graphics.Color.green(pixel).toFloat()
            mean[2] += android.graphics.Color.blue(pixel).toFloat()
        }
    }
    mean[0] /= pixelCount.toFloat()
    mean[1] /= pixelCount.toFloat()
    mean[2] /= pixelCount.toFloat()

    // Вычисление стандартного отклонения
    for (x in 0 until bitmap.width) {
        for (y in 0 until bitmap.height) {
            val pixel = bitmap.getPixel(x, y)
            std[0] += (android.graphics.Color.red(pixel) - mean[0]).toDouble().pow(2.0).toFloat()
            std[1] += (android.graphics.Color.green(pixel) - mean[1]).toDouble().pow(2.0).toFloat()
            std[2] += (android.graphics.Color.blue(pixel) - mean[2]).toDouble().pow(2.0).toFloat()
        }
    }
    std[0] = sqrt(std[0] / pixelCount)
    std[1] = sqrt(std[1] / pixelCount)
    std[2] = sqrt(std[2] / pixelCount)

    return Pair(mean, std)
}