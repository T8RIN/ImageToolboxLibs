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
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.t8rin.collages.Collage
import com.t8rin.collages.CollageType
import com.t8rin.collages.CollageTypeSelection
import com.t8rin.imagetoolbox.app.ui.theme.ImageToolboxLibsTheme
import kotlin.random.Random

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalFoundationApi::class)
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
                        var images by remember {
                            mutableStateOf(emptyList<Uri>())
                        }
                        val imagePicker =
                            rememberLauncherForActivityResult(contract = ActivityResultContracts.PickMultipleVisualMedia()) {
                                images = it
                            }
                        Column {
                            var collageImage by remember {
                                mutableStateOf<Bitmap?>(null)
                            }
                            var trigger by remember {
                                mutableStateOf(false)
                            }
                            var collageType by remember {
                                mutableStateOf(CollageType())
                            }
                            var color by remember {
                                mutableStateOf(Color.White)
                            }
                            Collage(
                                modifier = Modifier.weight(1f),
                                images = images,
                                collageCreationTrigger = trigger,
                                onCollageCreated = {
                                    trigger = false
                                    collageImage = it
                                },
                                backgroundColor = color,
                                collageType = collageType
                            )
                            Row(
                                modifier = Modifier.weight(1f)
                            ) {
                                AsyncImage(
                                    model = collageImage,
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .weight(1f)
                                        .background(MaterialTheme.colorScheme.secondaryContainer)
                                        .combinedClickable(
                                            onDoubleClick = {
                                                color = Color(Random.nextInt())
                                            },
                                            onLongClick = {
                                                imagePicker.launch(
                                                    PickVisualMediaRequest(
                                                        ActivityResultContracts.PickVisualMedia.ImageOnly
                                                    )
                                                )
                                            }
                                        ) {
                                            trigger = true
                                        },
                                    contentDescription = null
                                )
                            }
                            CollageTypeSelection(
                                imagesCount = images.size,
                                value = collageType,
                                onValueChange = {
                                    collageType = it
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(100.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}