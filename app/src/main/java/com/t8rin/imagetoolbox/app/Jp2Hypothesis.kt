package com.t8rin.imagetoolbox.app

import android.graphics.Bitmap
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import coil.compose.AsyncImage
import coil.imageLoader
import coil.request.ImageRequest
import coil.size.Size
import coil.transform.Transformation
import coil.util.DebugLogger
import com.gemalto.jp2.coil.Jpeg2000Decoder
import com.t8rin.trickle.Trickle
import com.t8rin.trickle.TrickleUtils.generateShades
import kotlinx.coroutines.delay
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

        val colors by remember(intensity) {
            derivedStateOf {
                generateShades(
                    Color.Cyan.toArgb(),
                    shadeStep = (100 * intensity).toInt().coerceAtLeast(2)
                )
            }
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
                            }
                        ).build()
                },
                imageLoader = imageLoader,
                modifier = Modifier.weight(1f),
                contentDescription = null
            )
            AsyncImage(
                model = remember(source, target, intensity) {
                    ImageRequest.Builder(this@Jp2Hypothesis).allowHardware(false).data(source)
                        .transformations(
                            GenericTransformation { bmp ->
                                val source = bmp.copy(Bitmap.Config.ARGB_8888, true)

                                delay(2000)
                                Log.d("INPUT", colors.joinToString())
                                repeat(source.width) { x ->
                                    repeat(source.height) { y ->
                                        val color = source.getPixel(x, y)
                                        val target =
                                            colors.minBy {
                                                Color(it).distanceFrom(Color(color)).also {
                                                    if (x == source.width / 2 && y == source.height / 2) {
                                                        Log.d("NonNative", it.toString())
                                                    }
                                                }
                                            }
                                        source.setPixel(x, y, target)
                                    }
                                }

                                source
                            }
                        ).build()
                },
                imageLoader = imageLoader,
                modifier = Modifier.weight(1f),
                contentDescription = null
            )
        }

        AsyncImage(
            model = remember(source, intensity, colors) {
                ImageRequest.Builder(this@Jp2Hypothesis).allowHardware(false).data(source)
                    .transformations(
                        GenericTransformation { bmp ->
                            Log.d("INPUT_native", colors.toIntArray().joinToString())
                            Trickle.colorPosterize(bmp, colors.toIntArray())
                        }
                    ).build()
            },
            imageLoader = imageLoader,
            modifier = Modifier.weight(1f),
            contentDescription = null
        )

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

private fun Color.distanceFrom(color: Color): Float {
    return sqrt((red - color.red).pow(2) + (green - color.green).pow(2) + (blue - color.blue).pow(2))
}