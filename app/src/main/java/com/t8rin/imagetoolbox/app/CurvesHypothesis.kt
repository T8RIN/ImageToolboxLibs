package com.t8rin.imagetoolbox.app

import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import coil3.compose.AsyncImage
import coil3.imageLoader
import coil3.request.ImageRequest
import coil3.request.allowHardware
import coil3.toBitmap
import com.t8rin.curves.ImageCurvesEditor
import com.t8rin.curves.ImageCurvesEditorState

@Composable
fun MainActivity.CurvesHypothesis() {
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
                    ).image?.toBitmap()
                }
        }
        var trigger by remember {
            mutableStateOf(false)
        }

        var showOrig by remember {
            mutableStateOf(false)
        }
        LazyColumn {
            item {
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
                    showOriginal = showOrig,
                    onStateChange = {
                        this@CurvesHypothesis.viewModel.curvesState = it
                    }
                )
            }
            items(100) {
                Text("DKDDKDKDD")
            }
        }
        Row(modifier = Modifier.align(Alignment.BottomCenter)) {
            Button(pickImage) {
                Text("PICK")
            }
            Button(
                onClick = { trigger = true }
            ) {
                Text("GET")
            }
            Button(
                onClick = {
                    viewModel.curvesState = ImageCurvesEditorState(
                        buildList {
                            repeat(4) {
                                add(
                                    buildList {
                                        repeat(5) {
                                            add(0.5f)
                                        }
                                    }
                                )
                            }
                        }
                    )
                }
            ) {
                Text("RESET")
            }
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(Color.Green)
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onPress = {
                                viewModel.curvesState
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