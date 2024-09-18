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
import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import coil.compose.AsyncImage
import com.t8rin.collages.Collage
import com.t8rin.collages.CollageType
import com.t8rin.collages.CollageTypeSelection
import com.t8rin.histogram.HistogramRGB
import com.t8rin.imagetoolbox.app.ui.theme.ImageToolboxLibsTheme
import kotlin.random.Random

class MainActivity : ComponentActivity() {

    class MainViewModel : ViewModel() {
        var images by mutableStateOf(emptyList<Uri>())
        var collageImage by mutableStateOf<Bitmap?>(null)
        var trigger by mutableStateOf(false)
        var collageType by mutableStateOf(CollageType.Empty)
        var color by mutableStateOf(Color.White)
        var space by mutableFloatStateOf(0f)
    }

    private val viewModel by viewModels<MainViewModel>()

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

                        val imagePicker =
                            rememberLauncherForActivityResult(contract = ActivityResultContracts.PickMultipleVisualMedia()) {
                                viewModel.images = it
                            }
                        Column {

                            Collage(
                                modifier = Modifier.size(400.dp),
                                images = viewModel.images,
                                collageCreationTrigger = viewModel.trigger,
                                onCollageCreated = {
                                    viewModel.trigger = false
                                    viewModel.collageImage = it
                                },
                                backgroundColor = viewModel.color,
                                collageType = viewModel.collageType,
                                spacing = viewModel.space,
                                cornerRadius = viewModel.space
                            )
                            Row(
                                modifier = Modifier.weight(1f)
                            ) {
                                HistogramRGB(
                                    imageUri = viewModel.images.firstOrNull() ?: Uri.EMPTY,
                                    modifier = Modifier
                                        .width(90.dp)
                                        .height(50.dp)
                                )
                                AsyncImage(
                                    model = viewModel.collageImage,
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .weight(1f)
                                        .background(MaterialTheme.colorScheme.secondaryContainer)
                                        .combinedClickable(
                                            onDoubleClick = {
                                                viewModel.space = Random
                                                    .nextInt(0, 100)
                                                    .toFloat()
                                                viewModel.color = Color(Random.nextInt())
                                            },
                                            onLongClick = {
                                                imagePicker.launch(
                                                    PickVisualMediaRequest(
                                                        ActivityResultContracts.PickVisualMedia.ImageOnly
                                                    )
                                                )
                                            }
                                        ) {
                                            viewModel.trigger = true
                                        },
                                    contentDescription = null
                                )
                            }
                            CollageTypeSelection(
                                imagesCount = viewModel.images.size,
                                value = viewModel.collageType,
                                onValueChange = {
                                    viewModel.collageType = it
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(100.dp),
                                shape = RoundedCornerShape(12.dp),
                                itemModifierFactory = { isSelected ->
                                    Modifier
                                        .background(
                                            animateColorAsState(
                                                targetValue = if (isSelected) {
                                                    MaterialTheme.colorScheme.primaryContainer
                                                } else MaterialTheme.colorScheme.surfaceContainerLowest,
                                            ).value,
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .padding(8.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}