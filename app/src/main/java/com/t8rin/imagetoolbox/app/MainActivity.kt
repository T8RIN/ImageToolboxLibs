package com.t8rin.imagetoolbox.app

import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import coil3.SingletonImageLoader
import coil3.annotation.DelicateCoilApi
import coil3.compose.AsyncImage
import coil3.imageLoader
import coil3.svg.SvgDecoder
import coil3.util.DebugLogger
import com.t8rin.collages.CollageType
import com.t8rin.curves.ImageCurvesEditorState
import com.t8rin.editbox.EditBoxState
import com.t8rin.imagetoolbox.app.ui.theme.ImageToolboxLibsTheme
import com.t8rin.opencv_tools.utils.OpenCV
import com.t8rin.palette.Palette
import com.t8rin.palette.PaletteColor
import com.t8rin.palette.PaletteFormat
import com.t8rin.palette.coders.PaintNETPaletteCoder
import com.t8rin.palette.coders.SVGPaletteCoder
import com.t8rin.palette.decode
import com.t8rin.palette.encode
import com.t8rin.palette.getCoder
import com.t8rin.palette.use
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    class MainViewModel : ViewModel() {
        private val imageModelState = mutableStateOf<Any?>(null)
        var imageModel: Any?
            get() = imageModelState.value
            set(value) {
                imageModelState.value = value
                curvesState = ImageCurvesEditorState.Default
            }
        var images by mutableStateOf(emptyList<Uri>())
        var collageImage by mutableStateOf<Bitmap?>(null)
        var trigger by mutableStateOf(false)
        var collageType by mutableStateOf(CollageType.Empty)
        var color by mutableStateOf(Color.White)
        var space by mutableFloatStateOf(0f)
        var curvesState by mutableStateOf(ImageCurvesEditorState.Default)

        val editBoxTextStates: List<EditBoxState> = List(4) {
            EditBoxState()
        }

        fun deactivateAllBoxes() {
            editBoxTextStates.forEach(EditBoxState::deactivate)
        }

        fun activateBox(state: EditBoxState) {
            deactivateAllBoxes()
            state.activate()
        }
    }

    val viewModel by viewModels<MainViewModel>()

    @OptIn(DelicateCoilApi::class, ExperimentalStdlibApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        OpenCV.init(application)
        enableEdgeToEdge()

        testPalette()

        SingletonImageLoader.setUnsafe(
            imageLoader.newBuilder()
                .logger(DebugLogger())
                .components {
                    add(SvgDecoder.Factory())
                }.build()
        )
        setContent {
            ImageToolboxLibsTheme {
                Surface {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                    ) {
                        AsyncImage(
                            model = svg,
                            contentDescription = null
                        )
                    }
                }
            }
        }
    }
}

private var svg: ByteArray = ByteArray(0)

@ExperimentalStdlibApi
private fun testPalette() {
    CoroutineScope(Dispatchers.IO).launch {
        val colors = mutableListOf(
            PaletteColor(
                color = Color.Red,
                name = "RedBLYA"
            ),
            PaletteColor(
                color = Color.Yellow,
                name = "YellowBLYA"
            ),
            PaletteColor(
                color = Color.Green,
                name = "GreenBLYA"
            )
        )

        val palette = Palette(
            name = "Cock",
            colors = colors
        )

        SVGPaletteCoder().use {
            encode(palette)
        }.onSuccess {
            svg = it
        }

        PaintNETPaletteCoder().use {
            decode(
                """
                    ;paint.net Palette File
                    ;Downloaded from Lospec.com/palette-list
                    ;Palette Name: Spectral II
                    ;Description: Introducing, Spectral II. This palette uses the same colours from "Spectral" [https://www.pixilart.com/palettes/spectral-26244], but adds 2 more colours. One with More contrast, for darker shading, and the other with more saturation, to help with stronger highlights. Thank you!<br>
                    ;Colors: 8
                    FF35143e
                    FF541646
                    FF5d2d75
                    FF644f9c
                    FF6d7ab7
                    FF89b7d0
                    FFa6e6e5
                    FFc7f0e6
                """.trimIndent().encodeToByteArray()
            )
        }.onSuccess {
            Log.w("TEST", it.colors.map { it.name to it.toArgb() }.toString())
        }

        Log.d("TEST", "-------Palette START------")

        Log.d("TEST", "Palette to test = ${colors.map { it.name to it.toArgb() }}")

        val s = mutableSetOf<PaletteFormat>()
        val f = mutableSetOf<PaletteFormat>()

        PaletteFormat.formatsWithDecodeAndEncode.forEach { format ->
            val coder = format.getCoder()

            val enc = coder.use {
                encode(palette)
            }
            var decFail: Throwable? = null
            var logfn: () -> Unit = {}
            val dec = enc.getOrNull()?.let {
                coder.use {
                    decode(it).colors.also {
                        logfn = {
                            Log.d(
                                "TEST",
                                "$format decoded = ${it.map { it.name to it.toArgb() }}"
                            )
                        }
                    }.takeIf { it.isNotEmpty() }?.all { dec ->
                        colors.any {
                            it.name == dec.name && it.toArgb() == dec.toArgb()
                        }
                    }
                }
            }?.onFailure { decFail = it }?.getOrNull() == true

            if (enc.isFailure || !dec) {
                logfn()
            }

            if (enc.isFailure) {
                Log.d("TEST", "Failure ENC on $format = ${enc.exceptionOrNull()}")
                f.add(format)
            }

            if (!dec) {
                Log.d("TEST", "Failure DEC on $format = $decFail")

                f.add(format)
            }

            if (enc.isSuccess && dec) {
                s.add(format)
            }
        }

        Log.d("TEST", "SUCESS result on $s")
        Log.d("TEST", "FAILURE result on $f")

        Log.d("TEST", "-------Palette END------")
    }
}