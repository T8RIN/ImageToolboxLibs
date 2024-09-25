package com.t8rin.imagetoolbox.app

import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.t8rin.opencv_tools.free_corners_crop.FreeCornersCropper

@Composable
fun MainActivity.FreeCornersCropHypothesis() {
    var uri by rememberSaveable {
        mutableStateOf<Uri?>(null)
    }
    val imagePicker =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.PickVisualMedia()) {
            it?.let {
                uri = it
            }
        }

    val pickImage: () -> Unit = {
        imagePicker.launch(PickVisualMediaRequest())
    }

    var croppingTrigger by remember {
        mutableStateOf(false)
    }
    var cropped by remember {
        mutableStateOf<Bitmap?>(null)
    }

    Box {
        Column {
            FreeCornersCropper(
                imageModel = uri,
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Yellow),
                containerModifier = Modifier.weight(1f),
                croppingTrigger = croppingTrigger,
                onCropped = {
                    cropped = it
                    croppingTrigger = false
                }
            )

            Spacer(Modifier.height(24.dp))
            Row {
                Button(pickImage) {
                    Text("PICK")
                }
                Button(
                    onClick = {
                        croppingTrigger = true
                    }
                ) {
                    Text("CROPP")
                }
            }
        }
        AsyncImage(
            model = cropped,
            contentDescription = null,
            modifier = Modifier.size(120.dp)
        )
    }
}
