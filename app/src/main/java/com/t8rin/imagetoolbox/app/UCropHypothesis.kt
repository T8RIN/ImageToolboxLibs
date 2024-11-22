package com.t8rin.imagetoolbox.app

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.yalantis.ucrop.compose.UCropper

@Composable
fun MainActivity.UCropHypothesis() {
    Box(Modifier.fillMaxSize()) {
        var imageUri by rememberSaveable {
            mutableStateOf<Uri?>(null)
        }
        var croppedUri by rememberSaveable {
            mutableStateOf<Uri?>(null)
        }
        var croppingTrigger by remember {
            mutableStateOf(false)
        }
        var rotationAngle by rememberSaveable {
            mutableFloatStateOf(0f)
        }
        val launcher =
            rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) {
                it?.let {
                    imageUri = it
                }
            }
        var isLoading by remember {
            mutableStateOf(true)
        }

        Box(modifier = Modifier.size(450.dp)) {
            AnimatedContent(imageUri) { r ->
                if (r != Uri.EMPTY) {
                    UCropper(
                        imageModel = r,
                        croppingTrigger = croppingTrigger,
                        aspectRatio = null,
                        onCropped = {
                            croppingTrigger = false
                            imageUri = it
                            croppedUri = it
                            rotationAngle = 0f
                        },
                        containerModifier = Modifier.fillMaxSize(),
                        modifier = Modifier.background(Color.Red),
                        onLoadingStateChange = {
                            isLoading = it
                        },
                        contentPadding = WindowInsets.systemBars.union(WindowInsets.displayCutout)
                            .asPaddingValues()
                    )
                }
            }
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(200.dp)
                )
            }

            AsyncImage(
                model = croppedUri,
                contentDescription = null,
                modifier = Modifier.size(50.dp)
            )

            BackHandler {
                imageUri = Uri.EMPTY
            }
        }
        Column(
            Modifier
                .align(Alignment.BottomEnd)
                .padding(top = 40.dp)
        ) {
            Button(onClick = {
                launcher.launch(
                    PickVisualMediaRequest(
                        ActivityResultContracts.PickVisualMedia.ImageOnly
                    )
                )
            }) {
                Text("COCKCKCKCKOCOC")
            }
            Button(onClick = { croppingTrigger = true }) {
                Text("CRROOOOOOOOOOP")
            }
        }
    }
}