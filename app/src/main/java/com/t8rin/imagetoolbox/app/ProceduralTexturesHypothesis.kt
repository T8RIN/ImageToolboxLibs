package com.t8rin.imagetoolbox.app

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.t8rin.fast_noise.texture.CamouflageTextureGenerator
import com.t8rin.fast_noise.texture.CamouflageTextureParameters
import com.t8rin.fast_noise.texture.CellTextureGenerator
import com.t8rin.fast_noise.texture.CellTextureParameters
import com.t8rin.fast_noise.texture.CloudTextureGenerator
import com.t8rin.fast_noise.texture.CloudTextureParameters
import com.t8rin.fast_noise.texture.CrackTextureGenerator
import com.t8rin.fast_noise.texture.CrackTextureParameters
import com.t8rin.fast_noise.texture.FabricTextureGenerator
import com.t8rin.fast_noise.texture.FabricTextureParameters
import com.t8rin.fast_noise.texture.LavaTextureGenerator
import com.t8rin.fast_noise.texture.LavaTextureParameters
import com.t8rin.fast_noise.texture.PaperTextureGenerator
import com.t8rin.fast_noise.texture.PaperTextureParameters
import com.t8rin.fast_noise.texture.RustTextureGenerator
import com.t8rin.fast_noise.texture.RustTextureParameters
import com.t8rin.fast_noise.texture.SmokeTextureGenerator
import com.t8rin.fast_noise.texture.SmokeTextureParameters
import com.t8rin.fast_noise.texture.StoneTextureGenerator
import com.t8rin.fast_noise.texture.StoneTextureParameters
import com.t8rin.fast_noise.texture.TopographyTextureGenerator
import com.t8rin.fast_noise.texture.TopographyTextureParameters
import com.t8rin.fast_noise.texture.WaterRippleTextureGenerator
import com.t8rin.fast_noise.texture.WaterRippleTextureParameters
import com.t8rin.fast_noise.texture.WoodTextureGenerator
import com.t8rin.fast_noise.texture.WoodTextureParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.random.Random

@Composable
fun MainActivity.ProceduralTexturesHypothesis(
    modifier: Modifier = Modifier
) {
    val pagerState = rememberPagerState(pageCount = textureDemos::size)

    Scaffold(modifier = modifier.fillMaxSize()) { contentPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
        ) {
            Text(
                text = "Procedural textures",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                beyondViewportPageCount = 1
            ) { page ->
                TextureDemoPage(
                    demo = textureDemos[page],
                    page = page,
                    pageCount = textureDemos.size
                )
            }
        }
    }
}

@Composable
private fun TextureDemoPage(
    demo: TextureDemo,
    page: Int,
    pageCount: Int
) {
    val values = remember(demo) {
        mutableStateListOf<Float>().apply {
            addAll(demo.controls.map(TextureControl::default))
        }
    }
    var seed by remember(demo) { mutableIntStateOf(1337) }
    var bitmap by remember(demo) { mutableStateOf<Bitmap?>(null) }
    var isGenerating by remember(demo) { mutableStateOf(false) }
    val parameterSnapshot = values.toList()

    LaunchedEffect(demo, seed, parameterSnapshot) {
        delay(120)
        isGenerating = true
        bitmap = withContext(Dispatchers.Default) {
            demo.generate(seed, parameterSnapshot)
        }
        isGenerating = false
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = demo.name,
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = "${page + 1} / $pageCount · seed $seed",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = {
                            demo.controls.forEachIndexed { index, control ->
                                values[index] = control.default
                            }
                            seed = 1337
                        }
                    ) {
                        Text("Reset")
                    }
                    Button(onClick = { seed = Random.nextInt() }) {
                        Text("Random")
                    }
                }
            }
        }

        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .heightIn(max = 520.dp)
                    .aspectRatio(1f),
                shape = MaterialTheme.shapes.large,
                tonalElevation = 3.dp
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    bitmap?.let { generatedBitmap ->
                        Image(
                            bitmap = generatedBitmap.asImageBitmap(),
                            contentDescription = demo.name,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }

                    if (isGenerating) {
                        CircularProgressIndicator()
                    }
                }
            }
        }

        item {
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
        }

        itemsIndexed(
            items = demo.controls,
            key = { _, control -> control.name }
        ) { index, control ->
            Column(
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = control.name,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = formatValue(values[index]),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Slider(
                    value = values[index],
                    onValueChange = { values[index] = it },
                    valueRange = control.range
                )
            }
        }
    }
}

private fun formatValue(value: Float): String = when {
    value == 0f -> "0"
    kotlin.math.abs(value) < 0.1f -> String.format(Locale.US, "%.4f", value)
    kotlin.math.abs(value) < 10f -> String.format(Locale.US, "%.2f", value)
    else -> String.format(Locale.US, "%.1f", value)
}

private data class TextureControl(
    val name: String,
    val default: Float,
    val range: ClosedFloatingPointRange<Float>
)

private data class TextureDemo(
    val name: String,
    val controls: List<TextureControl>,
    val generate: (seed: Int, values: List<Float>) -> Bitmap?
)

private fun controls(
    scale: Float,
    vararg controls: TextureControl
): List<TextureControl> = listOf(
    TextureControl("Scale", scale, 0.001f..0.04f),
    *controls
)

private val textureDemos = listOf(
    TextureDemo(
        name = "Lava",
        controls = controls(
            0.008f,
            TextureControl("Distortion", 32f, 0f..80f),
            TextureControl("Flow", 1.35f, 0.2f..4f),
            TextureControl("Detail", 0.55f, 0f..1f),
            TextureControl("Crust", 0.52f, 0.1f..0.9f),
            TextureControl("Glow", 0.72f, 0f..1f)
        )
    ) { seed, v ->
        LavaTextureGenerator().generate(
            768,
            768,
            LavaTextureParameters(seed, v[0], v[1], v[2], v[3], v[4], v[5])
        )
    },
    TextureDemo(
        name = "Clouds",
        controls = controls(
            0.0045f,
            TextureControl("Coverage", 0.48f, 0f..1f),
            TextureControl("Softness", 0.22f, 0.01f..0.5f),
            TextureControl("Detail", 0.62f, 0f..1f),
            TextureControl("Distortion", 24f, 0f..80f),
            TextureControl("Density", 0.92f, 0f..1.5f)
        )
    ) { seed, v ->
        CloudTextureGenerator().generate(
            768,
            768,
            CloudTextureParameters(seed, v[0], v[1], v[2], v[3], v[4], v[5])
        )
    },
    TextureDemo(
        name = "Smoke",
        controls = controls(
            0.006f,
            TextureControl("Turbulence", 34f, 0f..80f),
            TextureControl("Density", 0.56f, 0f..1f),
            TextureControl("Wisps", 2.2f, 0f..8f),
            TextureControl("Contrast", 1.35f, 0f..3f),
            TextureControl("Detail", 0.5f, 0f..1f)
        )
    ) { seed, v ->
        SmokeTextureGenerator().generate(
            768,
            768,
            SmokeTextureParameters(seed, v[0], v[1], v[2], v[3], v[4], v[5])
        )
    },
    TextureDemo(
        name = "Stone",
        controls = controls(
            0.011f,
            TextureControl("Grain", 0.3f, 0f..1f),
            TextureControl("Veins", 0.58f, 0f..1f),
            TextureControl("Vein scale", 0.024f, 0.002f..0.08f),
            TextureControl("Distortion", 18f, 0f..60f),
            TextureControl("Contrast", 1.12f, 0f..3f)
        )
    ) { seed, v ->
        StoneTextureGenerator().generate(
            768,
            768,
            StoneTextureParameters(seed, v[0], v[1], v[2], v[3], v[4], v[5])
        )
    },
    TextureDemo(
        name = "Wood",
        controls = controls(
            0.008f,
            TextureControl("Rings", 18f, 2f..50f),
            TextureControl("Grain", 0.32f, 0f..1f),
            TextureControl("Distortion", 4.5f, 0f..15f),
            TextureControl("Stretch", 3.2f, 0.2f..10f),
            TextureControl("Contrast", 1.15f, 0f..3f)
        )
    ) { seed, v ->
        WoodTextureGenerator().generate(
            768,
            768,
            WoodTextureParameters(seed, v[0], v[1], v[2], v[3], v[4], v[5])
        )
    },
    TextureDemo(
        name = "Camouflage",
        controls = controls(
            0.007f,
            TextureControl("Threshold 1", 0.36f, 0f..1f),
            TextureControl("Threshold 2", 0.58f, 0f..1f),
            TextureControl("Threshold 3", 0.76f, 0f..1f),
            TextureControl("Distortion", 28f, 0f..80f),
            TextureControl("Edge softness", 0.035f, 0.001f..0.2f)
        )
    ) { seed, v ->
        CamouflageTextureGenerator().generate(
            768,
            768,
            CamouflageTextureParameters(seed, v[0], v[1], v[2], v[3], v[4], v[5])
        )
    },
    TextureDemo(
        name = "Paper",
        controls = controls(
            0.012f,
            TextureControl("Fiber density", 72f, 5f..160f),
            TextureControl("Fiber strength", 0.24f, 0f..1f),
            TextureControl("Grain", 0.16f, 0f..1f),
            TextureControl("Stains", 0.14f, 0f..1f),
            TextureControl("Roughness", 0.35f, 0f..1f)
        )
    ) { seed, v ->
        PaperTextureGenerator().generate(
            768,
            768,
            PaperTextureParameters(seed, v[0], v[1], v[2], v[3], v[4], v[5])
        )
    },
    TextureDemo(
        name = "Rust",
        controls = controls(
            0.009f,
            TextureControl("Corrosion", 0.55f, 0f..1f),
            TextureControl("Pitting", 0.34f, 0f..1f),
            TextureControl("Flakes", 0.42f, 0f..1f),
            TextureControl("Distortion", 16f, 0f..60f),
            TextureControl("Contrast", 1.25f, 0f..3f)
        )
    ) { seed, v ->
        RustTextureGenerator().generate(
            768,
            768,
            RustTextureParameters(seed, v[0], v[1], v[2], v[3], v[4], v[5])
        )
    },
    TextureDemo(
        name = "Fabric",
        controls = controls(
            0.018f,
            TextureControl("Horizontal threads", 28f, 2f..80f),
            TextureControl("Vertical threads", 28f, 2f..80f),
            TextureControl("Irregularity", 0.14f, 0f..1f),
            TextureControl("Depth", 0.48f, 0f..1f),
            TextureControl("Fuzz", 0.1f, 0f..1f)
        )
    ) { seed, v ->
        FabricTextureGenerator().generate(
            768,
            768,
            FabricTextureParameters(seed, v[0], v[1], v[2], v[3], v[4], v[5])
        )
    },
    TextureDemo(
        name = "Topography",
        controls = controls(
            0.0055f,
            TextureControl("Line count", 16f, 2f..50f),
            TextureControl("Line thickness", 0.11f, 0.005f..0.45f),
            TextureControl("Shading", 0.42f, 0f..1f),
            TextureControl("Distortion", 12f, 0f..60f),
            TextureControl("Contrast", 1.1f, 0f..3f)
        )
    ) { seed, v ->
        TopographyTextureGenerator().generate(
            768,
            768,
            TopographyTextureParameters(seed, v[0], v[1], v[2], v[3], v[4], v[5])
        )
    },
    TextureDemo(
        name = "Cells",
        controls = controls(
            0.018f,
            TextureControl("Jitter", 0.92f, 0f..2f),
            TextureControl("Border width", 0.12f, 0.005f..0.5f),
            TextureControl("Glow", 0.28f, 0f..1f),
            TextureControl("Distortion", 6f, 0f..40f),
            TextureControl("Variation", 0.42f, 0f..1f)
        )
    ) { seed, v ->
        CellTextureGenerator().generate(
            768,
            768,
            CellTextureParameters(seed, v[0], v[1], v[2], v[3], v[4], v[5])
        )
    },
    TextureDemo(
        name = "Cracks",
        controls = controls(
            0.019f,
            TextureControl("Width", 0.065f, 0.002f..0.3f),
            TextureControl("Density", 0.72f, 0f..1f),
            TextureControl("Distortion", 9f, 0f..50f),
            TextureControl("Depth", 0.72f, 0f..1f),
            TextureControl("Branching", 0.45f, 0f..1f)
        )
    ) { seed, v ->
        CrackTextureGenerator().generate(
            768,
            768,
            CrackTextureParameters(seed, v[0], v[1], v[2], v[3], v[4], v[5])
        )
    },
    TextureDemo(
        name = "Water ripples",
        controls = controls(
            0.0075f,
            TextureControl("Frequency", 22f, 1f..60f),
            TextureControl("Distortion", 22f, 0f..70f),
            TextureControl("Caustics", 0.58f, 0f..1f),
            TextureControl("Depth", 0.55f, 0f..1f),
            TextureControl("Highlights", 0.62f, 0f..1f)
        )
    ) { seed, v ->
        WaterRippleTextureGenerator().generate(
            768,
            768,
            WaterRippleTextureParameters(seed, v[0], v[1], v[2], v[3], v[4], v[5])
        )
    }
)
