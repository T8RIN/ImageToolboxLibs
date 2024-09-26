package com.t8rin.imagetoolbox.app

import android.graphics.Bitmap
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import androidx.core.net.toUri
import coil.compose.AsyncImage
import coil.imageLoader
import coil.request.ImageRequest
import coil.size.Size
import coil.transform.Transformation
import coil.util.DebugLogger
import com.gemalto.jp2.coil.Jpeg2000Decoder
import com.t8rin.awebp.coil.AnimatedWebPDecoder
import com.t8rin.awebp.decoder.AnimatedWebpDecoder
import com.t8rin.djvu_coder.coil.DjvuDecoder
import com.t8rin.opencv_tools.spot_heal.SpotHealer
import com.t8rin.psd.coil.PsdDecoder
import com.t8rin.qoi_coder.coil.QoiDecoder
import com.t8rin.tiff.TiffDecoder
import kotlinx.coroutines.flow.onCompletion
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
        mutableFloatStateOf(1f)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        val imageLoader = remember {
            imageLoader.newBuilder().components {
                //add(AnimatedAVIFDecoder.Factory())
                add(DjvuDecoder.Factory())
                add(AnimatedWebPDecoder.Factory())
                add(TiffDecoder.Factory())
                add(Jpeg2000Decoder.Factory())
                add(QoiDecoder.Factory())
                add(PsdDecoder.Factory())
            }.logger(DebugLogger()).build()
        }

        Row(
            modifier = Modifier.weight(1f)
        ) {
            AsyncImage(
                model = ImageRequest.Builder(this@Jp2Hypothesis).allowHardware(false)
                    .transformations(
                        listOf(
                            GenericTransformation { bmp ->
                                SpotHealer.heal(
                                    image = bmp,
                                    mask = imageLoader.execute(
                                        ImageRequest.Builder(this@Jp2Hypothesis)
                                            .data(target)
                                            .allowHardware(false)
                                            .build()
                                    ).drawable?.toBitmap()!!,
                                    radius = 2f,
                                    type = SpotHealer.Type.TELEA
                                )
                            }
                        )
                    ).data(source).build(),
                imageLoader = imageLoader,
                contentScale = ContentScale.Fit,
                modifier = Modifier.weight(1f),
                contentDescription = null
            )
            var model by remember {
                mutableStateOf<Any?>(Unit)
            }
            LaunchedEffect(target) {
                if (target.isEmpty()) return@LaunchedEffect
                AnimatedWebpDecoder(
                    context = this@Jp2Hypothesis,
                    sourceUri = target.toUri(),
                    coroutineScope = this
                ).frames().onCompletion {
                    model = R.drawable.test
                }.collect {
                    model = it
                }
            }
            AsyncImage(
                model = model,
                imageLoader = imageLoader,
                modifier = Modifier.weight(1f),
                contentDescription = null
            )
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