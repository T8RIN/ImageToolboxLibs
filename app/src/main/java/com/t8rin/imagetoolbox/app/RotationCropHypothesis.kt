package com.t8rin.imagetoolbox.app

import android.graphics.Bitmap
import android.graphics.BitmapFactory
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import com.smarttoolfactory.cropper.ImageCropper
import com.smarttoolfactory.cropper.model.AspectRatio
import com.smarttoolfactory.cropper.model.OutlineType
import com.smarttoolfactory.cropper.model.RectCropShape
import com.smarttoolfactory.cropper.settings.CropDefaults
import com.smarttoolfactory.cropper.settings.CropOutlineProperty
import kotlinx.coroutines.launch

@Composable
fun MainActivity.RotationCropHypothesis() {
    var bitmap by remember {
        mutableStateOf<Bitmap?>(null)
    }
    val scope = rememberCoroutineScope()
    val imagePicker =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.PickVisualMedia()) {
            scope.launch {
                bitmap = BitmapFactory.decodeStream(
                    it?.let { it1 ->
                        contentResolver.openInputStream(it1)
                    }
                )
            }
        }

    val pickImage: () -> Unit = {
        imagePicker.launch(PickVisualMediaRequest())
    }

    var crop by remember {
        mutableStateOf(false)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        val img = remember(bitmap) { bitmap?.asImageBitmap() }
        img?.let { imageBitmap ->
            ImageCropper(
                imageBitmap = imageBitmap,
                cropProperties = CropDefaults.properties(
                    cropOutlineProperty = CropOutlineProperty(
                        OutlineType.Rect,
                        RectCropShape(
                            id = 0,
                            title = OutlineType.Rect.name
                        )
                    ),
                    fling = true,
                    rotatable = true,
                    aspectRatio = AspectRatio(1 / 2f),
                    fixedAspectRatio = true
                ),
                crop = crop,
                onCropStart = { },
                onZoomChange = {},
                onCropSuccess = {
                    bitmap = it.asAndroidBitmap()
                    crop = false
                },
                modifier = Modifier.weight(1f)
            )
        }

        Row {
            Button(onClick = pickImage) {
                Text("Pick")
            }
            Button(
                onClick = {
                    crop = true
                }
            ) {
                Text("Crop")
            }
        }
    }
}