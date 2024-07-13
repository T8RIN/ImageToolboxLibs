package com.t8rin.imagetoolbox.app

import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.imageLoader
import coil.request.ImageRequest
import coil.size.Size
import coil.transform.Transformation
import coil.util.DebugLogger
import com.gemalto.jp2.coil.Jpeg2000Decoder
import com.t8rin.logger.makeLog
import io.github.xyzxqs.xlowpoly.LowPoly
import org.beyka.tiffbitmapfactory.TiffDecoder
import kotlin.random.Random

@Composable
fun MainActivity.Jp2Hypothesis() {
    var model by remember {
        mutableStateOf<String>("")
    }


    val imagePicker =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.PickVisualMedia()) {
            model = it?.toString() ?: ""
        }

    val pickImage: () -> Unit = {
        imagePicker.launch(PickVisualMediaRequest())
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
        AsyncImage(
            model = remember(model, fill) {
                ImageRequest.Builder(this@Jp2Hypothesis).data(model).transformations(
                    GenericTransformation { bmp ->
                        val time = System.currentTimeMillis()
                        LowPoly.generateNonNative(
                            input = bmp,
                            threshold = 50,
                            alphaOrPointCount = 30000f,
                            lowPoly = true,
                            fill = fill
                        ).also {
                            makeLog("TIME") {
                                (System.currentTimeMillis() - time).toString() + "ms for nonNative"
                            }
                        } ?: bmp
                    }
                ).build()
            },
            imageLoader = imageLoader,
            modifier = Modifier.weight(1f),
            contentDescription = null
        )

        AsyncImage(
            model = remember(model, fill) {
                ImageRequest.Builder(this@Jp2Hypothesis).data(model).transformations(
                    GenericTransformation { bmp ->
                        val time = System.currentTimeMillis()
                        LowPoly.generate(
                            input = bmp,
                            threshold = 50,
                            alphaOrPointCount = 2f,
                            lowPoly = true,
                            fill = fill
                        ).also {
                            makeLog("TIME") {
                                (System.currentTimeMillis() - time).toString() + "ms for native"
                            }
                        } ?: bmp
                    }
                ).build()
            },
            imageLoader = imageLoader,
            modifier = Modifier.weight(1f),
            contentDescription = null
        )

        Row {
            Button(onClick = pickImage) {
                Text("Pick")
            }
            Switch(checked = fill, onCheckedChange = { fill = it })
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