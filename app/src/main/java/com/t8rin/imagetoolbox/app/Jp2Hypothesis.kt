package com.t8rin.imagetoolbox.app

import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import coil3.compose.AsyncImage
import coil3.imageLoader
import coil3.request.ImageRequest
import coil3.request.allowHardware
import coil3.request.transformations
import coil3.size.Size
import coil3.transform.Transformation
import coil3.util.DebugLogger
import com.gemalto.jp2.coil.Jpeg2000Decoder
import com.t8rin.awebp.coil.AnimatedWebPDecoder
import com.t8rin.awebp.decoder.AnimatedWebpDecoder
import com.t8rin.djvu_coder.coil.DjvuDecoder
import com.t8rin.opencv_tools.auto_straight.AutoStraighten
import com.t8rin.opencv_tools.auto_straight.AutoStraighten.Corners
import com.t8rin.opencv_tools.auto_straight.AutoStraighten.Mode
import com.t8rin.opencv_tools.auto_straight.AutoStraighten.PointD
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

    var pos by remember {
        mutableIntStateOf(0)
    }

    val imagePicker =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.OpenDocument()) {
            source = it?.toString() ?: ""
        }

    val pickImage: () -> Unit = {
        imagePicker.launch(arrayOf("image/*"))
    }

    val imagePicker2 =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.OpenDocument()) {
            target = it?.toString() ?: ""
        }

    val pickImage2: () -> Unit = {
        imagePicker2.launch(arrayOf("image/*"))
    }

    var intensity by remember {
        mutableFloatStateOf(0f)
    }
    var intensity2 by remember {
        mutableFloatStateOf(0f)
    }
    var intensity3 by remember {
        mutableFloatStateOf(0f)
    }
    var intensity4 by remember {
        mutableFloatStateOf(0f)
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
                            GenericTransformation(
                                listOf(intensity, intensity2, intensity3, intensity4, pos)
                            ) { bmp ->
//                                LensCorrection.undistort(
//                                    bmp,
//                                    lensDataJson = """
//                                        {
//                                          "name": "Apple_iPhone 15pro_24mm__4k_16by9_3840x2160-60.05fps",
//                                          "note": "",
//                                          "calibrated_by": "Lucas",
//                                          "camera_brand": "Apple",
//                                          "camera_model": "iPhone 15pro",
//                                          "lens_model": "24mm",
//                                          "camera_setting": "",
//                                          "calib_dimension": {
//                                            "w": 3840,
//                                            "h": 2160
//                                          },
//                                          "orig_dimension": {
//                                            "w": 3840,
//                                            "h": 2160
//                                          },
//                                          "output_dimension": {
//                                            "w": 3840,
//                                            "h": 2160
//                                          },
//                                          "frame_readout_time": null,
//                                          "gyro_lpf": null,
//                                          "input_horizontal_stretch": 1.0,
//                                          "input_vertical_stretch": 1.0,
//                                          "num_images": 15,
//                                          "fps": 60.046101,
//                                          "crop": null,
//                                          "official": false,
//                                          "asymmetrical": false,
//                                          "fisheye_params": {
//                                            "RMS_error": 0.8832859526688158,
//                                            "camera_matrix": [
//                                              [
//                                                2607.333657684296,
//                                                0.0,
//                                                1915.37813609749
//                                              ],
//                                              [
//                                                0.0,
//                                                2602.093328903683,
//                                                1088.1412038298402
//                                              ],
//                                              [
//                                                0.0,
//                                                0.0,
//                                                1.0
//                                              ]
//                                            ],
//                                            "distortion_coeffs": [
//                                              0.5090054847934529,
//                                              -0.02921177076421695,
//                                              -2.5373092536413333,
//                                              5.569492679404599
//                                            ],
//                                            "radial_distortion_limit": null
//                                          },
//                                          "identifier": "",
//                                          "calibrator_version": "1.5.2",
//                                          "date": "2023-10-01",
//                                          "compatible_settings": [],
//                                          "sync_settings": null,
//                                          "distortion_model": null,
//                                          "digital_lens": null,
//                                          "digital_lens_params": null,
//                                          "interpolations": null,
//                                          "focal_length": null,
//                                          "crop_factor": null,
//                                          "global_shutter": false
//                                        }
//                                    """.trimIndent(),
//                                )

//                                val conv = ASCIIConverter(
//                                    100f * intensity,
//                                    mapper = Gradient.NORMAL.toMapper()
//                                )
//
//                                val b =
//                                    conv.convertToAsciiBitmap(bmp)

//                                val ascii = conv.convertToAscii(bmp)
//
//
//                                getSystemService<ClipboardManager>()!!.setPrimaryClip(
//                                    ClipData.newPlainText("", ascii)
//                                )

                                AutoStraighten.process(
                                    input = bmp,
                                    mode = if (intensity > 0.5f) Mode.Perspective
                                    else Mode.Manual(
                                        corners = Corners(
                                            topLeft = PointD(0.1, 0.0),
                                            topRight = PointD(1.0, 0.0),
                                            bottomRight = PointD(0.9, 1.0),
                                            bottomLeft = PointD(0.0, 1.0),
                                            isAbsolute = false
                                        )
                                    )
                                )
//                                ColorMap.apply(
//                                    bitmap = bmp,
//                                    map = ColorMap.Type.entries[(intensity * 21).roundToInt()].also {
//                                        Log.d(
//                                            "MAP",
//                                            it.name
//                                        )
//                                    }
//                                )
                            }
                        )
                    ).data(source).size(2000).build(),
                imageLoader = imageLoader,
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
//            AsyncImage(
//                model = model,
//                imageLoader = imageLoader,
//                modifier = Modifier.weight(1f),
//                contentDescription = null
//            )
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
//            AsyncImage(
//                model = target,
//                imageLoader = imageLoader,
//                modifier = Modifier.weight(1f),
//                contentDescription = null
//            )
        }

        Row {
            Button(onClick = pickImage) {
                Text("Source")
            }
            Button(onClick = pickImage2) {
                Text("Target")
            }
            Slider(value = intensity, onValueChange = { intensity = it }, valueRange = -1f..1f)
        }
        Slider(value = intensity2, onValueChange = { intensity2 = it }, valueRange = -1f..1f)
        Slider(value = intensity3, onValueChange = { intensity3 = it }, valueRange = -1f..1f)
        Slider(value = intensity4, onValueChange = { intensity4 = it }, valueRange = -1f..1f)
//        Row(
//            verticalAlignment = Alignment.CenterVertically,
//            modifier = Modifier.fillMaxWidth()
//        ) {
//            Button(
//                onClick = { pos = (pos - 1).coerceAtLeast(0) }
//            ) {
//                Text("-")
//            }
//
//            Text(
//                text = pos.toString() + " = ${filters[pos]}",
//                modifier = Modifier.weight(1f),
//                textAlign = TextAlign.Center,
//            )
//
//            Button(
//                onClick = { pos = (pos + 1).coerceAtMost(filters.lastIndex) }
//            ) {
//                Text("+")
//            }
//        }
    }

}

class GenericTransformation(
    val key: Any? = Random.nextInt(),
    val action: suspend (Bitmap, Size) -> Bitmap
) : Transformation() {

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