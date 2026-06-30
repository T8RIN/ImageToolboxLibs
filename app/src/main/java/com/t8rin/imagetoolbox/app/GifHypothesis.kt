package com.t8rin.imagetoolbox.app

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.graphics.applyCanvas
import androidx.core.net.toUri
import coil3.compose.AsyncImage
import coil3.gif.GifDecoder
import coil3.imageLoader
import com.t8rin.gif_converter.GifEncoder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import kotlin.math.max

@Composable
fun MainActivity.GifHypothesis() {
    var source by remember {
        mutableStateOf<Uri?>(null)
    }
    var result by remember {
        mutableStateOf<Uri?>(null)
    }
    var repeatCount by remember {
        mutableIntStateOf(0)
    }
    var quality by remember {
        mutableIntStateOf(10)
    }
    var fps by remember {
        mutableFloatStateOf(12f)
    }
    var framesCount by remember {
        mutableIntStateOf(8)
    }
    var offset by remember {
        mutableIntStateOf(0)
    }

    val picker =
        rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            source = uri
        }

    var isLoading by remember {
        mutableStateOf(false)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
            .systemBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Button(onClick = {
            picker.launch(
                PickVisualMediaRequest(
                    ActivityResultContracts.PickVisualMedia.ImageOnly
                )
            )
        }) {
            Text("Pick source")
        }

        AsyncImage(
            model = source,
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .background(Color.Black)
        )

        Text("Frames: $framesCount")
        Slider(
            value = framesCount.toFloat(),
            onValueChange = { framesCount = it.toInt().coerceIn(2, 20) },
            valueRange = 2f..20f
        )

        Text("FPS: $fps")
        Slider(value = fps, onValueChange = { fps = it }, valueRange = 1f..30f)

        Text("Quality: $quality")
        Slider(
            value = quality.toFloat(),
            onValueChange = { quality = it.toInt().coerceIn(1, 20) },
            valueRange = 1f..20f
        )

        Text("Repeat: $repeatCount")
        Slider(
            value = repeatCount.toFloat(),
            onValueChange = { repeatCount = it.toInt().coerceIn(0, 10) },
            valueRange = 0f..10f
        )

        Text("Offset: $offset")
        Slider(
            value = offset.toFloat(),
            onValueChange = { offset = it.toInt().coerceIn(0, 100) },
            valueRange = 0f..100f
        )

        Button(
            onClick = {
                result = null
                CoroutineScope(Dispatchers.IO).launch {
                    val uri = source ?: return@launch
                    val base = contentResolver.openInputStream(uri)?.use {
                        BitmapFactory.decodeStream(it)
                    } ?: return@launch
                    val outFile = File(cacheDir, "gif_hypothesis.gif")
                    FileOutputStream(outFile).use { stream ->
                        val encoder = GifEncoder()
                            .setRepeat(repeatCount)
                            .setQuality(quality)
                            .setFrameRate(fps)
                        if (!encoder.start(stream)) return@launch
                        isLoading = true
                        val totalFrames = max(2, framesCount)
                        repeat(totalFrames) { index ->
                            val frame = base.copy(Bitmap.Config.ARGB_8888, true)
                            val dx = (offset + index * 12) % max(1, frame.width)
                            frame.applyCanvas {
                                translate(dx.toFloat(), 0f)
                                drawBitmap(base, 0f, 0f, null)
                            }
                            encoder.addFrame(frame)
                        }
                        encoder.finish()
                    }
                    result = outFile.toUri()
                    isLoading = false
                }
            }
        ) {
            Row {
                Text("Encode GIF")
                if (isLoading) {
                    CircularProgressIndicator(
                        trackColor = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        AsyncImage(
            model = result,
            imageLoader = remember {
                imageLoader.newBuilder()
                    .components {
                        add(GifDecoder.Factory())
                    }
                    .build()
            },
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .background(Color.DarkGray)
        )
    }
}
