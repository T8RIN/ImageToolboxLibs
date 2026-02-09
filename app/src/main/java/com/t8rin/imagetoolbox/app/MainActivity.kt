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
import androidx.compose.ui.keepScreenOn
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import coil3.SingletonImageLoader
import coil3.annotation.DelicateCoilApi
import coil3.imageLoader
import coil3.svg.SvgDecoder
import coil3.util.DebugLogger
import com.smarttoolfactory.colordetector.parser.ColorNameParser
import com.smarttoolfactory.colordetector.util.ColorUtil
import com.t8rin.collages.CollageType
import com.t8rin.curves.ImageCurvesEditorState
import com.t8rin.editbox.EditBoxState
import com.t8rin.imagetoolbox.app.ui.theme.ImageToolboxLibsTheme
import com.t8rin.neural_tools.NeuralTool
import com.t8rin.opencv_tools.utils.OpenCV
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
        NeuralTool.init(application)
        lifecycleScope.launch {
            ColorNameParser.init(application)

            Log.d("TEST_COLOR", ColorNameParser.parseColorName(Color(0xFFFFDDEE)))
            Log.d(
                "TEST_COLOR",
                ColorUtil.colorToHex(ColorNameParser.parseColorFromNameSingle("Transparent pink"))
            )
        }
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
                            .keepScreenOn()
                    ) {
                        Jp2Hypothesis()
                    }
                }
            }
        }
    }
}
