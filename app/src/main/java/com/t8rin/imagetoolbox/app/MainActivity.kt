package com.t8rin.imagetoolbox.app

import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.ViewModel
import coil.compose.AsyncImage
import coil.imageLoader
import coil.request.ImageRequest
import com.t8rin.collages.CollageType
import com.t8rin.curves.ImageCurvesEditor
import com.t8rin.curves.ImageCurvesEditorState
import com.t8rin.imagetoolbox.app.ui.theme.ImageToolboxLibsTheme

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
    }

    val viewModel by viewModels<MainViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ImageToolboxLibsTheme {
                Surface {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                    ) {
                        val imagePicker =
                            rememberLauncherForActivityResult(contract = ActivityResultContracts.PickVisualMedia()) {
                                viewModel.imageModel = it?.toString() ?: ""
                            }

                        val pickImage: () -> Unit = {
                            imagePicker.launch(PickVisualMediaRequest())
                        }

                        var bitmap by remember {
                            mutableStateOf<Bitmap?>(null)
                        }
                        val context = LocalContext.current

                        LaunchedEffect(viewModel.imageModel) {
                            bitmap =
                                if (viewModel.imageModel is Bitmap?) viewModel.imageModel as Bitmap?
                                else {
                                    context.imageLoader.execute(
                                        ImageRequest.Builder(context).data(viewModel.imageModel)
                                            .allowHardware(false).build()
                                    ).drawable?.toBitmap()
                                }
                        }
                        var trigger by remember {
                            mutableStateOf(false)
                        }

                        var showOrig by remember {
                            mutableStateOf(false)
                        }
                        ImageCurvesEditor(
                            bitmap = bitmap,
                            state = viewModel.curvesState,
                            curvesSelectionText = {
                                Text(
                                    when (it) {
                                        0 -> "luma"
                                        1 -> "red"
                                        2 -> "green"
                                        3 -> "blue"
                                        else -> "ERROR"
                                    }
                                )
                            },
                            placeControlsAtTheEnd = LocalConfiguration.current.orientation != ActivityInfo.SCREEN_ORIENTATION_PORTRAIT,
                            imageObtainingTrigger = trigger,
                            onImageObtained = {
                                trigger = false
                                viewModel.collageImage = it
                            },
                            contentPadding = WindowInsets.systemBars.union(WindowInsets.displayCutout)
                                .asPaddingValues() + PaddingValues(16.dp),
                            containerModifier = Modifier.align(Alignment.Center),
                            showOriginal = showOrig
                        )
                        Row(modifier = Modifier.align(Alignment.BottomCenter)) {
                            Button(pickImage) {
                                Text("PICK")
                            }
                            Button(
                                onClick = { trigger = true }
                            ) {
                                Text("GET")
                            }
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(Color.Green)
                                    .pointerInput(Unit) {
                                        detectTapGestures(
                                            onPress = {
                                                showOrig = true
                                                awaitRelease()
                                                showOrig = false
                                            }
                                        )
                                    }
                            )
                        }
                        AsyncImage(
                            model = viewModel.collageImage,
                            contentDescription = null,
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .alpha(0.25f)
                                .size(150.dp)
                                .background(Color.Red)
                        )
                    }
                }
            }
        }
    }
}