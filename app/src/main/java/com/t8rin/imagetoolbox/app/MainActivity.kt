package com.t8rin.imagetoolbox.app

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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.ViewModel
import coil.imageLoader
import coil.request.ImageRequest
import com.t8rin.collages.CollageType
import com.t8rin.curves.ImageCurvesEditor
import com.t8rin.imagetoolbox.app.ui.theme.ImageToolboxLibsTheme

class MainActivity : ComponentActivity() {

    class MainViewModel : ViewModel() {
        var images by mutableStateOf(emptyList<Uri>())
        var collageImage by mutableStateOf<Bitmap?>(null)
        var trigger by mutableStateOf(false)
        var collageType by mutableStateOf(CollageType.Empty)
        var color by mutableStateOf(Color.White)
        var space by mutableFloatStateOf(0f)
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
                            .systemBarsPadding()
                    ) {
                        var imageModel by remember {
                            mutableStateOf<Any?>(null)
                        }

                        val imagePicker =
                            rememberLauncherForActivityResult(contract = ActivityResultContracts.PickVisualMedia()) {
                                imageModel = it?.toString() ?: ""
                            }

                        val pickImage: () -> Unit = {
                            imagePicker.launch(PickVisualMediaRequest())
                        }

                        var bitmap by remember {
                            mutableStateOf<Bitmap?>(null)
                        }
                        val context = LocalContext.current

                        LaunchedEffect(imageModel) {
                            bitmap = if (imageModel is Bitmap?) imageModel as Bitmap?
                            else {
                                context.imageLoader.execute(
                                    ImageRequest.Builder(context).data(imageModel)
                                        .allowHardware(false).build()
                                ).drawable?.toBitmap()
                            }
                        }

                        if (bitmap != null) {
                            ImageCurvesEditor(bitmap!!)
                        }
                        Button(pickImage, modifier = Modifier.align(Alignment.BottomCenter)) {
                            Text(
                                "PICK"
                            )
                        }
                    }
                }
            }
        }
    }
}