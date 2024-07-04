package com.t8rin.imagetoolbox.app

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.imageLoader
import coil.util.DebugLogger
import com.gemalto.jp2.coil.Jpeg2000Decoder
import org.beyka.tiffbitmapfactory.TiffDecoder

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
        AsyncImage(
            model = model,
            imageLoader = imageLoader,
            modifier = Modifier.weight(1f),
            contentDescription = null
        )

        Row {
            Button(onClick = pickImage) {
                Text("Pick")
            }
        }
    }
}