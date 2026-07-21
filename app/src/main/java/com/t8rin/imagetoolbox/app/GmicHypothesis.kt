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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import com.t8rin.gmic.model.GmicExecutionOptions
import com.t8rin.gmic.filters.AlphaSharpen
import com.t8rin.gmic.filters.AnalogDamage
import com.t8rin.gmic.filters.BlurredFrame
import com.t8rin.gmic.filters.Breaks
import com.t8rin.gmic.filters.Brushify
import com.t8rin.gmic.filters.Charcoal
import com.t8rin.gmic.filters.ChessboardOverlay
import com.t8rin.gmic.filters.ChromaticAberrations
import com.t8rin.gmic.filters.CinematicDream
import com.t8rin.gmic.filters.CircleAbstraction
import com.t8rin.gmic.filters.CircleTransform
import com.t8rin.gmic.filters.ColorAbstraction
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
import com.t8rin.gmic.filters.DiceMosaic
import com.t8rin.gmic.filters.DiffusionTensors
import com.t8rin.gmic.filters.Dirty
import com.t8rin.gmic.filters.Doodle
import com.t8rin.gmic.filters.DreamPainting
import com.t8rin.gmic.filters.Droste
import com.t8rin.gmic.filters.Ellipsionism
import com.t8rin.gmic.filters.Engrave
import com.t8rin.gmic.filters.EquirectangularProjection
import com.t8rin.gmic.filters.FadedArray
import com.t8rin.gmic.filters.FeltPen
import com.t8rin.gmic.filters.Filaments
import com.t8rin.gmic.filters.FlipAndRotateBlocks
import com.t8rin.gmic.filters.Flower
import com.t8rin.gmic.filters.FuzzyFrame
import com.t8rin.gmic.filters.FreakyDetails
import com.t8rin.gmic.filters.Fractalize
import com.t8rin.gmic.filters.FrostedGlass
import com.t8rin.gmic.filters.Ghost
import com.t8rin.gmic.filters.GoldMeinelSharpen
import com.t8rin.gmic.filters.GraphicNovel
import com.t8rin.gmic.filters.HopePoster
import com.t8rin.gmic.filters.HuffmanGlitch
import com.t8rin.gmic.filters.InkWash
import com.t8rin.gmic.filters.Isophotes
import com.t8rin.gmic.filters.JpegArtifacts
import com.t8rin.gmic.filters.Linify
import com.t8rin.gmic.filters.LightPatch
import com.t8rin.gmic.filters.LocalOrientation
import com.t8rin.gmic.filters.Lomo
import com.t8rin.gmic.filters.LylejkPainting
import com.t8rin.gmic.filters.MakeSeamless
import com.t8rin.gmic.filters.MagicDetails
import com.t8rin.gmic.filters.MarkerDrawing
import com.t8rin.gmic.filters.MightyDetails
import com.t8rin.gmic.filters.Ministeck
import com.t8rin.gmic.filters.MineralMosaic
import com.t8rin.gmic.filters.MirrorFrame
import com.t8rin.gmic.filters.MirroredArray
import com.t8rin.gmic.filters.MultiscaleSharpen
import com.t8rin.gmic.filters.MessWithBits
import com.t8rin.gmic.filters.OffsetStripes
import com.t8rin.gmic.filters.OldMovieStripes
import com.t8rin.gmic.filters.OldSchool8Bit
import com.t8rin.gmic.filters.Painting
import com.t8rin.gmic.filters.PaintingFrame
import com.t8rin.gmic.filters.PencilPortrait
import com.t8rin.gmic.filters.PixelSort
import com.t8rin.gmic.filters.PoincareDisk
import com.t8rin.gmic.filters.PopShadows
import com.t8rin.gmic.filters.PosterEdges
import com.t8rin.gmic.filters.Puzzle
import com.t8rin.gmic.filters.Quadtree
import com.t8rin.gmic.filters.RainAndSnow
import com.t8rin.gmic.filters.RandomColorArray
import com.t8rin.gmic.filters.RandomPatchArray
import com.t8rin.gmic.filters.RandomShadeStripes
import com.t8rin.gmic.filters.RebuildFromSimilarBlocks
import com.t8rin.gmic.filters.ReliefLight
import com.t8rin.gmic.filters.RichardsonLucySharpen
import com.t8rin.gmic.filters.Retinex
import com.t8rin.gmic.filters.Rodilius
import com.t8rin.gmic.filters.RoundedFrame
import com.t8rin.gmic.filters.Shapeism
import com.t8rin.gmic.filters.SharpAbstract
import com.t8rin.gmic.filters.ShadowPatch
import com.t8rin.gmic.filters.ShufflePatches
import com.t8rin.gmic.filters.Skeleton
import com.t8rin.gmic.filters.SmoothAbstract
import com.t8rin.gmic.filters.SmoothFrame
import com.t8rin.gmic.filters.SmoothSkin
import com.t8rin.gmic.filters.SquareToCircle
import com.t8rin.gmic.filters.StainedGlass
import com.t8rin.gmic.filters.StereographicProjection
import com.t8rin.gmic.filters.Streak
import com.t8rin.gmic.filters.Stringify
import com.t8rin.gmic.filters.SuperPixels
import com.t8rin.gmic.filters.Taquin
import com.t8rin.gmic.filters.TileableRotation
import com.t8rin.gmic.filters.Tunnel
import com.t8rin.gmic.filters.VectorPainting
import com.t8rin.gmic.filters.WarpByIntensity
import com.t8rin.gmic.filters.WaterReflection
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.job
import java.util.concurrent.atomic.AtomicReference

private data class GmicPreset(
    val title: String,
    val filter: GmicFilter
)

private data class GmicPreviewSpec(
    val filter: GmicFilter,
    val configuration: Gmic.Configuration
)

private class GmicPreviewJobs {
    private val current = AtomicReference<Job?>(null)

    fun cancelCurrent() {
        current.getAndSet(null)?.cancel()
    }

    suspend fun <T> runLatest(block: suspend () -> T): T {
        val job = currentCoroutineContext().job
        val previous = current.getAndSet(job)
        if (previous !== job) previous?.cancel()
        return try {
            block()
        } finally {
            current.compareAndSet(job, null)
        }
    }
}

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
    GmicPreset("Diffusion Tensors", DiffusionTensors()),
    GmicPreset("Doodle", Doodle()),
    GmicPreset("Charcoal", Charcoal()),
    GmicPreset("Pencil Portrait", PencilPortrait()),
    GmicPreset("Color Abstraction", ColorAbstraction()),
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
    GmicPreset("Nadir / Zenith", EquirectangularProjection()),
    GmicPreset("Water Reflection", WaterReflection()),
    GmicPreset("Puzzle", Puzzle()),
    GmicPreset("Cube Frame", CubeFrame()),
    GmicPreset("Blurred Frame", BlurredFrame()),
    GmicPreset("Fuzzy Frame", FuzzyFrame()),
    GmicPreset("Mirror Frame", MirrorFrame()),
    GmicPreset("Painting Frame", PaintingFrame()),
    GmicPreset("Rounded Frame", RoundedFrame()),
    GmicPreset("Smooth Frame", SmoothFrame()),
    GmicPreset("Chromatic Aberrations", ChromaticAberrations()),
    GmicPreset("CRT Phosphors", CrtPhosphors()),
    GmicPreset("CRT Scanlines", CrtScanlines()),
    GmicPreset("Dirty", Dirty()),
    GmicPreset("Flip Blocks", FlipAndRotateBlocks()),
    GmicPreset("Lomo", Lomo()),
    GmicPreset("Mess with Bits", MessWithBits()),
    GmicPreset("Offset Stripes", OffsetStripes()),
    GmicPreset("Old Movie Stripes", OldMovieStripes()),
    GmicPreset("Old School 8-Bit", OldSchool8Bit()),
    GmicPreset("Random Shade Stripes", RandomShadeStripes()),
    GmicPreset("Rebuild Similar Blocks", RebuildFromSimilarBlocks()),
    GmicPreset("Huffman Glitch", HuffmanGlitch()),
    GmicPreset("JPEG Artifacts", JpegArtifacts()),
    GmicPreset("Pixel Sort", PixelSort()),
    GmicPreset("Rain & Snow", RainAndSnow()),
    GmicPreset("Streak", Streak()),
    GmicPreset("Retinex", Retinex()),
    GmicPreset("Isophotes", Isophotes()),
    GmicPreset("Local Orientation", LocalOrientation()),
    GmicPreset("Skeleton", Skeleton()),
    GmicPreset("Super Pixels", SuperPixels()),
    GmicPreset("Details Equalizer", DetailsEqualizer()),
    GmicPreset("Freaky Details", FreakyDetails()),
    GmicPreset("Magic Details", MagicDetails()),
    GmicPreset("Mighty Details", MightyDetails()),
    GmicPreset("Pop Shadows", PopShadows()),
    GmicPreset("Light Patch", LightPatch()),
    GmicPreset("Relief Light", ReliefLight()),
    GmicPreset("Shadow Patch", ShadowPatch()),
    GmicPreset("Deblur", Deblur()),
    GmicPreset("Alpha Sharpen", AlphaSharpen()),
    GmicPreset("Gold-Meinel", GoldMeinelSharpen()),
    GmicPreset("Richardson-Lucy", RichardsonLucySharpen()),
    GmicPreset("Multiscale Sharpen", MultiscaleSharpen()),
    GmicPreset("Smooth Skin", SmoothSkin()),
    GmicPreset("Cinematic Dream", CinematicDream()),
    GmicPreset("Graphic Novel", GraphicNovel()),
    GmicPreset("Dream Painting", DreamPainting()),
    GmicPreset("Analog Damage", AnalogDamage()),
    GmicPreset("Faded Array", FadedArray()),
    GmicPreset("Mirrored Array", MirroredArray()),
    GmicPreset("Random Patch Array", RandomPatchArray()),
    GmicPreset("Random Color Array", RandomColorArray()),
    GmicPreset("Chessboard Overlay", ChessboardOverlay()),
    GmicPreset("Dice Mosaic", DiceMosaic()),
    GmicPreset("Ministeck", Ministeck()),
    GmicPreset("Shuffle Patches", ShufflePatches()),
    GmicPreset("Taquin", Taquin()),
    GmicPreset("Tileable Rotation", TileableRotation()),
    GmicPreset("Make Seamless", MakeSeamless()),
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
    val gmicConfiguration by Gmic.configuration.collectAsState()
    var previewSpec by remember {
        mutableStateOf(GmicPreviewSpec(activeFilter, gmicConfiguration))
    }
    val previewJobs = remember { GmicPreviewJobs() }
    var error by remember { mutableStateOf<String?>(null) }

    val picker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) imageModel = uri
    }

    LaunchedEffect(activeFilter, gmicConfiguration) {
        previewJobs.cancelCurrent()
        delay(200)
        previewSpec = GmicPreviewSpec(activeFilter, gmicConfiguration)
    }

    val transformation = remember(previewSpec) {
        val filter = previewSpec.filter
        val configuration = previewSpec.configuration
        val executionOptions = GmicExecutionOptions(
            customCommands = configuration.customCommands
        )
        GenericTransformation(
            key = "gmic:${Gmic.VERSION}:${filter.command}:${filter.options}:" +
                "$executionOptions:${configuration.version}"
        ) { bitmap ->
            previewJobs.runLatest {
                Gmic.runCancellable(
                    bitmap,
                    filter.command,
                    filter.options,
                    executionOptions
                )
            }
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
                    if (command != activeFilter.command) {
                        val customFilter = GmicFilter(command)
                        selectedPreset = GmicPreset("Custom", customFilter)
                        activeFilter = customFilter
                    }
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
