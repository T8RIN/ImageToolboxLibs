package com.t8rin.imagetoolbox.app

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.transformations
import com.t8rin.gmic.Gmic
import com.t8rin.gmic.GmicFilter

private data class GmicPreset(
    val title: String,
    val filter: GmicFilter
)

private val gmicPresets = listOf(
    GmicPreset("Blur", GmicFilters.Blur(3f)),
    GmicPreset("Sharpen", GmicFilters.Sharpen(180f)),
    GmicPreset("Grayscale", GmicFilters.Grayscale),
    GmicPreset("Sepia", GmicFilters.Sepia),
    GmicPreset("Solarize", GmicFilters.Solarize),
    GmicPreset("Negative", GmicFilters.Negative),
    GmicPreset("Equalize", GmicFilters.Equalize)
)

@Composable
fun MainActivity.GmicHypothesis(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var imageModel by remember { mutableStateOf<Any>(R.drawable.test) }
    var selectedPreset by remember { mutableStateOf(gmicPresets.first()) }
    var command by remember { mutableStateOf(selectedPreset.filter.command) }
    var activeFilter by remember { mutableStateOf<GmicFilter>(selectedPreset.filter) }
    var error by remember { mutableStateOf<String?>(null) }

    val picker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) imageModel = uri
    }

    val transformation = remember(activeFilter) {
        GenericTransformation(
            key = "gmic:${activeFilter.command}:${activeFilter.options}"
        ) { bitmap ->
            Gmic.run(bitmap, activeFilter)
        }
    }
    val request = remember(imageModel, transformation) {
        ImageRequest.Builder(context)
            .data(imageModel)
            .transformations(transformation)
            .build()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = {
                    picker.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                }
            ) {
                Text("Choose image")
            }
            Text(
                text = "G'MIC ${Gmic.VERSION}",
                style = MaterialTheme.typography.titleMedium
            )
        }

        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(gmicPresets) { preset ->
                FilterChip(
                    selected = selectedPreset == preset,
                    onClick = {
                        selectedPreset = preset
                        command = preset.filter.command
                        activeFilter = preset.filter
                    },
                    label = { Text(preset.title) }
                )
            }
        }

        OutlinedTextField(
            value = command,
            onValueChange = { command = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("G'MIC command") },
            singleLine = true
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Process alpha",
                modifier = Modifier.weight(1f)
            )
            Button(
                enabled = command.isNotBlank(),
                onClick = {
                    selectedPreset = GmicPreset("Custom", GmicFilter(command))
                    activeFilter = GmicFilter(
                        command = command
                    )
                    error = null
                }
            ) {
                Text("Run")
            }
        }

        error?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = request,
                contentDescription = "G'MIC filter result",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit,
                onSuccess = { error = null },
                onError = { error = it.result.throwable.message ?: "G'MIC processing failed" }
            )
        }
    }
}

private object GmicFilters {
    data class Blur(val radius: Float) : GmicFilter {
        init {
            require(radius.isFinite() && radius >= 0f) { "Blur radius must be finite and non-negative" }
        }

        override val command: String = "blur $radius"
    }

    data class Sharpen(val amount: Float) : GmicFilter {
        init {
            require(amount.isFinite() && amount >= 0f) { "Sharpen amount must be finite and non-negative" }
        }

        override val command: String = "sharpen $amount"
    }

    data object Grayscale : GmicFilter {
        override val command: String = "to_gray"
    }

    data object Sepia : GmicFilter {
        override val command: String = "sepia"
    }

    data object Solarize : GmicFilter {
        override val command: String = "solarize"
    }

    data object Negative : GmicFilter {
        override val command: String = "negate"
    }

    data object Equalize : GmicFilter {
        override val command: String = "equalize"
    }
}
