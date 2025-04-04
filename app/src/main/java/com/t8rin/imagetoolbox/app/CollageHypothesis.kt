package com.t8rin.imagetoolbox.app

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.t8rin.collages.Collage
import com.t8rin.collages.CollageTypeSelection
import com.t8rin.histogram.HistogramType
import com.t8rin.histogram.ImageHistogram
import kotlin.random.Random

@Composable
fun MainActivity.CollageHypothesis() {
    val imagePicker =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.PickMultipleVisualMedia()) {
            viewModel.images = it
        }
    var aspect by remember {
        mutableFloatStateOf(0.5f)
    }

    Column {
        Collage(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 400.dp),
            images = viewModel.images,
            collageCreationTrigger = viewModel.trigger,
            onCollageCreated = {
                viewModel.trigger = false
                viewModel.collageImage = it
            },
            backgroundColor = viewModel.color,
            collageType = viewModel.collageType,
            spacing = viewModel.space,
            cornerRadius = viewModel.space,
            aspectRatio = aspect
        )
        Row {
            ImageHistogram(
                imageUri = viewModel.images.firstOrNull() ?: Uri.EMPTY,
                modifier = Modifier
                    .padding(8.dp)
                    .width(120.dp)
                    .height(80.dp)
                    .background(
                        color = MaterialTheme.colorScheme.background,
                        shape = RoundedCornerShape(2.dp)
                    )
                    .padding(8.dp),
                onSwapType = { type ->
                    when (type) {
                        HistogramType.RGB -> HistogramType.Brightness
                        HistogramType.Brightness -> HistogramType.Camera
                        HistogramType.Camera -> HistogramType.RGB
                    }
                }
            )
            AsyncImage(
                model = viewModel.collageImage,
                modifier = Modifier
                    .height(300.dp)
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
                .weight(1f, false)
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