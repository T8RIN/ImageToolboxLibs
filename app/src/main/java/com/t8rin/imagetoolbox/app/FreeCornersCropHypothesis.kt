package com.t8rin.imagetoolbox.app

import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.material3.Button
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.t8rin.opencv_tools.free_corners_crop.FreeCornersCropper

@Composable
fun MainActivity.FreeCornersCropHypothesis() {
    var uri by rememberSaveable {
        mutableStateOf<Any?>(null)
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
            var coerce by rememberSaveable {
                mutableStateOf(true)
            }
            FreeCornersCropper(
                imageModel = uri,
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Yellow),
                containerModifier = Modifier.weight(1f),
                croppingTrigger = croppingTrigger,
                onCropped = {
                    uri = it
                    cropped = it
                    croppingTrigger = false
                },
                contentPadding = WindowInsets.systemBars.union(WindowInsets.displayCutout).let {
                    if (LocalConfiguration.current.orientation != ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) it.only(
                        WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom
                    )
                    else it.only(WindowInsetsSides.Horizontal)
                }.asPaddingValues() + PaddingValues(24.dp),
                coercePointsToImageArea = coerce
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
                Switch(coerce, { coerce = it })
            }
        }
        AsyncImage(
            model = cropped,
            contentDescription = null,
            modifier = Modifier
                .alpha(0.25f)
                .size(150.dp)
                .background(Color.Red),
            contentScale = if (cropped == null) {
                ContentScale.Fit
            } else if (cropped!!.run { width > height }) {
                ContentScale.FillWidth
            } else ContentScale.FillHeight
        )
    }
}

@Composable
operator fun PaddingValues.plus(paddingValues: PaddingValues): PaddingValues {
    val ld = LocalLayoutDirection.current
    return PaddingValues(
        start = calculateStartPadding(ld) + paddingValues.calculateStartPadding(ld),
        top = calculateTopPadding() + paddingValues.calculateTopPadding(),
        end = calculateEndPadding(ld) + paddingValues.calculateEndPadding(ld),
        bottom = calculateBottomPadding() + paddingValues.calculateBottomPadding(),
    )
}