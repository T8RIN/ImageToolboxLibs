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
import com.t8rin.gmic.filters.AlphaSharpen
import com.t8rin.gmic.filters.AnalogDamage
import com.t8rin.gmic.filters.Breaks
import com.t8rin.gmic.filters.Brushify
import com.t8rin.gmic.filters.ChromaticAberrations
import com.t8rin.gmic.filters.CinematicDream
import com.t8rin.gmic.filters.CircleAbstraction
import com.t8rin.gmic.filters.CircleTransform
import com.t8rin.gmic.filters.ConformalMap
import com.t8rin.gmic.filters.Cracks
import com.t8rin.gmic.filters.Crease
import com.t8rin.gmic.filters.CrtPhosphors
import com.t8rin.gmic.filters.CrtScanlines
import com.t8rin.gmic.filters.Cubism
import com.t8rin.gmic.filters.CubeFrame
import com.t8rin.gmic.filters.Cutout
import com.t8rin.gmic.filters.Deblur
import com.t8rin.gmic.filters.DetailsEqualizer
import com.t8rin.gmic.filters.Dirty
import com.t8rin.gmic.filters.DreamPainting
import com.t8rin.gmic.filters.Droste
import com.t8rin.gmic.filters.Ellipsionism
import com.t8rin.gmic.filters.Engrave
import com.t8rin.gmic.filters.FeltPen
import com.t8rin.gmic.filters.Filaments
import com.t8rin.gmic.filters.FlipAndRotateBlocks
import com.t8rin.gmic.filters.Flower
import com.t8rin.gmic.filters.FreakyDetails
import com.t8rin.gmic.filters.Fractalize
import com.t8rin.gmic.filters.FrostedGlass
import com.t8rin.gmic.filters.Ghost
import com.t8rin.gmic.filters.GoldMeinelSharpen
import com.t8rin.gmic.filters.GraphicNovel
import com.t8rin.gmic.filters.HopePoster
import com.t8rin.gmic.filters.HuffmanGlitch
import com.t8rin.gmic.filters.InkWash
import com.t8rin.gmic.filters.JpegArtifacts
import com.t8rin.gmic.filters.Linify
import com.t8rin.gmic.filters.LylejkPainting
import com.t8rin.gmic.filters.MagicDetails
import com.t8rin.gmic.filters.MarkerDrawing
import com.t8rin.gmic.filters.MightyDetails
import com.t8rin.gmic.filters.MineralMosaic
import com.t8rin.gmic.filters.MultiscaleSharpen
import com.t8rin.gmic.filters.Painting
import com.t8rin.gmic.filters.PixelSort
import com.t8rin.gmic.filters.PoincareDisk
import com.t8rin.gmic.filters.PopShadows
import com.t8rin.gmic.filters.PosterEdges
import com.t8rin.gmic.filters.Puzzle
import com.t8rin.gmic.filters.Quadtree
import com.t8rin.gmic.filters.RainAndSnow
import com.t8rin.gmic.filters.RichardsonLucySharpen
import com.t8rin.gmic.filters.Retinex
import com.t8rin.gmic.filters.Rodilius
import com.t8rin.gmic.filters.Shapeism
import com.t8rin.gmic.filters.SharpAbstract
import com.t8rin.gmic.filters.SmoothAbstract
import com.t8rin.gmic.filters.SquareToCircle
import com.t8rin.gmic.filters.StainedGlass
import com.t8rin.gmic.filters.StereographicProjection
import com.t8rin.gmic.filters.Streak
import com.t8rin.gmic.filters.Stringify
import com.t8rin.gmic.filters.Tunnel
import com.t8rin.gmic.filters.VectorPainting
import com.t8rin.gmic.filters.WarpByIntensity

private data class GmicPreset(
    val title: String,
    val filter: GmicFilter
)

private val gmicPresets = listOf(
    GmicPreset("Frosted Glass", FrostedGlass()),
    GmicPreset("Circle Abstraction", CircleAbstraction()),
    GmicPreset("Cubism", Cubism()),
    GmicPreset("Felt Pen", FeltPen()),
    GmicPreset("Ghost", Ghost()),
    GmicPreset("Linify", Linify()),
    GmicPreset("Marker Drawing", MarkerDrawing()),
    GmicPreset("Rodilius", Rodilius()),
    GmicPreset("Shapeism", Shapeism()),
    GmicPreset("Sharp Abstract", SharpAbstract()),
    GmicPreset("Smooth Abstract", SmoothAbstract()),
    GmicPreset("Stringify", Stringify()),
    GmicPreset("Engrave", Engrave()),
    GmicPreset("Filaments", Filaments()),
    GmicPreset("Ink Wash", InkWash()),
    GmicPreset("Brushify", Brushify()),
    GmicPreset("Cutout", Cutout()),
    GmicPreset("Ellipsionism", Ellipsionism()),
    GmicPreset("Fractalize", Fractalize()),
    GmicPreset("Hope Poster", HopePoster()),
    GmicPreset("Lylejk Painting", LylejkPainting()),
    GmicPreset("Painting", Painting()),
    GmicPreset("Poster Edges", PosterEdges()),
    GmicPreset("Quadtree", Quadtree()),
    GmicPreset("Vector Painting", VectorPainting()),
    GmicPreset("Breaks", Breaks()),
    GmicPreset("Circle Transform", CircleTransform()),
    GmicPreset("Crease", Crease()),
    GmicPreset("Flower", Flower()),
    GmicPreset("Square to Circle", SquareToCircle()),
    GmicPreset("Warp by Intensity", WarpByIntensity()),
    GmicPreset("Droste", Droste()),
    GmicPreset("Conformal Map", ConformalMap()),
    GmicPreset("Poincare Disk", PoincareDisk()),
    GmicPreset("Little Planet", StereographicProjection()),
    GmicPreset("Tunnel", Tunnel()),
    GmicPreset("Puzzle", Puzzle()),
    GmicPreset("Cube Frame", CubeFrame()),
    GmicPreset("Chromatic Aberrations", ChromaticAberrations()),
    GmicPreset("CRT Phosphors", CrtPhosphors()),
    GmicPreset("CRT Scanlines", CrtScanlines()),
    GmicPreset("Dirty", Dirty()),
    GmicPreset("Flip Blocks", FlipAndRotateBlocks()),
    GmicPreset("Huffman Glitch", HuffmanGlitch()),
    GmicPreset("JPEG Artifacts", JpegArtifacts()),
    GmicPreset("Pixel Sort", PixelSort()),
    GmicPreset("Rain & Snow", RainAndSnow()),
    GmicPreset("Streak", Streak()),
    GmicPreset("Retinex", Retinex()),
    GmicPreset("Details Equalizer", DetailsEqualizer()),
    GmicPreset("Freaky Details", FreakyDetails()),
    GmicPreset("Magic Details", MagicDetails()),
    GmicPreset("Mighty Details", MightyDetails()),
    GmicPreset("Pop Shadows", PopShadows()),
    GmicPreset("Deblur", Deblur()),
    GmicPreset("Alpha Sharpen", AlphaSharpen()),
    GmicPreset("Gold-Meinel", GoldMeinelSharpen()),
    GmicPreset("Richardson-Lucy", RichardsonLucySharpen()),
    GmicPreset("Multiscale Sharpen", MultiscaleSharpen()),
    GmicPreset("Cinematic Dream", CinematicDream()),
    GmicPreset("Graphic Novel", GraphicNovel()),
    GmicPreset("Dream Painting", DreamPainting()),
    GmicPreset("Analog Damage", AnalogDamage()),
    GmicPreset("Cracks", Cracks()),
    GmicPreset("Mineral Mosaic", MineralMosaic()),
    GmicPreset("Stained Glass", StainedGlass())
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
                text = "Typed filter or raw G'MIC command",
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
