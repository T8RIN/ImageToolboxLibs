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
import com.t8rin.fast_noise.texture.AncientRunesTextureGenerator
import com.t8rin.fast_noise.texture.AncientRunesTextureParameters
import com.t8rin.fast_noise.texture.ApollonianGasketTextureGenerator
import com.t8rin.fast_noise.texture.ApollonianGasketTextureParameters
import com.t8rin.fast_noise.texture.AsphaltTextureGenerator
import com.t8rin.fast_noise.texture.AsphaltTextureParameters
import com.t8rin.fast_noise.texture.AuroraTextureGenerator
import com.t8rin.fast_noise.texture.AuroraTextureParameters
import com.t8rin.fast_noise.texture.BacterialCultureTextureGenerator
import com.t8rin.fast_noise.texture.BacterialCultureTextureParameters
import com.t8rin.fast_noise.texture.BioluminescenceTextureGenerator
import com.t8rin.fast_noise.texture.BioluminescenceTextureParameters
import com.t8rin.fast_noise.texture.BiomechanicalTissueTextureGenerator
import com.t8rin.fast_noise.texture.BiomechanicalTissueTextureParameters
import com.t8rin.fast_noise.texture.BrickTextureGenerator
import com.t8rin.fast_noise.texture.BrickTextureParameters
import com.t8rin.fast_noise.texture.BurningShipTextureGenerator
import com.t8rin.fast_noise.texture.BurningShipTextureParameters
import com.t8rin.fast_noise.texture.CamouflageTextureGenerator
import com.t8rin.fast_noise.texture.CamouflageTextureParameters
import com.t8rin.fast_noise.texture.CarbonFiberTextureGenerator
import com.t8rin.fast_noise.texture.CarbonFiberTextureParameters
import com.t8rin.fast_noise.texture.CellTextureGenerator
import com.t8rin.fast_noise.texture.CellTextureParameters
import com.t8rin.fast_noise.texture.CellularEmbryoTextureGenerator
import com.t8rin.fast_noise.texture.CellularEmbryoTextureParameters
import com.t8rin.fast_noise.texture.ChladniPlateTextureGenerator
import com.t8rin.fast_noise.texture.ChladniPlateTextureParameters
import com.t8rin.fast_noise.texture.ChromaticTunnelTextureGenerator
import com.t8rin.fast_noise.texture.ChromaticTunnelTextureParameters
import com.t8rin.fast_noise.texture.ChromatophoreTextureGenerator
import com.t8rin.fast_noise.texture.ChromatophoreTextureParameters
import com.t8rin.fast_noise.texture.CircuitBoardTextureGenerator
import com.t8rin.fast_noise.texture.CircuitBoardTextureParameters
import com.t8rin.fast_noise.texture.CloudChamberTextureGenerator
import com.t8rin.fast_noise.texture.CloudChamberTextureParameters
import com.t8rin.fast_noise.texture.CloudTextureGenerator
import com.t8rin.fast_noise.texture.CloudTextureParameters
import com.t8rin.fast_noise.texture.ConcreteTextureGenerator
import com.t8rin.fast_noise.texture.ConcreteTextureParameters
import com.t8rin.fast_noise.texture.CoralGrowthTextureGenerator
import com.t8rin.fast_noise.texture.CoralGrowthTextureParameters
import com.t8rin.fast_noise.texture.CosmicVortexTextureGenerator
import com.t8rin.fast_noise.texture.CosmicVortexTextureParameters
import com.t8rin.fast_noise.texture.CrackTextureGenerator
import com.t8rin.fast_noise.texture.CrackTextureParameters
import com.t8rin.fast_noise.texture.CrystalGrowthTextureGenerator
import com.t8rin.fast_noise.texture.CrystalGrowthTextureParameters
import com.t8rin.fast_noise.texture.CymaticRosetteTextureGenerator
import com.t8rin.fast_noise.texture.CymaticRosetteTextureParameters
import com.t8rin.fast_noise.texture.DamascusTextureGenerator
import com.t8rin.fast_noise.texture.DamascusTextureParameters
import com.t8rin.fast_noise.texture.DendriticCrystalTextureGenerator
import com.t8rin.fast_noise.texture.DendriticCrystalTextureParameters
import com.t8rin.fast_noise.texture.DirtTextureGenerator
import com.t8rin.fast_noise.texture.DirtTextureParameters
import com.t8rin.fast_noise.texture.DragonScalesTextureGenerator
import com.t8rin.fast_noise.texture.DragonScalesTextureParameters
import com.t8rin.fast_noise.texture.EclipseCoronaTextureGenerator
import com.t8rin.fast_noise.texture.EclipseCoronaTextureParameters
import com.t8rin.fast_noise.texture.ElectricArcFieldTextureGenerator
import com.t8rin.fast_noise.texture.ElectricArcFieldTextureParameters
import com.t8rin.fast_noise.texture.EmberFieldTextureGenerator
import com.t8rin.fast_noise.texture.EmberFieldTextureParameters
import com.t8rin.fast_noise.texture.EventHorizonTextureGenerator
import com.t8rin.fast_noise.texture.EventHorizonTextureParameters
import com.t8rin.fast_noise.texture.FabricTextureGenerator
import com.t8rin.fast_noise.texture.FabricTextureParameters
import com.t8rin.fast_noise.texture.FerrofluidCrownTextureGenerator
import com.t8rin.fast_noise.texture.FerrofluidCrownTextureParameters
import com.t8rin.fast_noise.texture.FiberOpticBundleTextureGenerator
import com.t8rin.fast_noise.texture.FiberOpticBundleTextureParameters
import com.t8rin.fast_noise.texture.FireTextureGenerator
import com.t8rin.fast_noise.texture.FireTextureParameters
import com.t8rin.fast_noise.texture.FireflySwarmTextureGenerator
import com.t8rin.fast_noise.texture.FireflySwarmTextureParameters
import com.t8rin.fast_noise.texture.FlowTextureGenerator
import com.t8rin.fast_noise.texture.FlowTextureParameters
import com.t8rin.fast_noise.texture.FluidVorticityTextureGenerator
import com.t8rin.fast_noise.texture.FluidVorticityTextureParameters
import com.t8rin.fast_noise.texture.FoliageTextureGenerator
import com.t8rin.fast_noise.texture.FoliageTextureParameters
import com.t8rin.fast_noise.texture.FractalBloomTextureGenerator
import com.t8rin.fast_noise.texture.FractalBloomTextureParameters
import com.t8rin.fast_noise.texture.FrostFernTextureGenerator
import com.t8rin.fast_noise.texture.FrostFernTextureParameters
import com.t8rin.fast_noise.texture.GalacticWebTextureGenerator
import com.t8rin.fast_noise.texture.GalacticWebTextureParameters
import com.t8rin.fast_noise.texture.GalaxyFilamentsTextureGenerator
import com.t8rin.fast_noise.texture.GalaxyFilamentsTextureParameters
import com.t8rin.fast_noise.texture.GeodeTextureGenerator
import com.t8rin.fast_noise.texture.GeodeTextureParameters
import com.t8rin.fast_noise.texture.GildedFiligreeTextureGenerator
import com.t8rin.fast_noise.texture.GildedFiligreeTextureParameters
import com.t8rin.fast_noise.texture.GrassTextureGenerator
import com.t8rin.fast_noise.texture.GrassTextureParameters
import com.t8rin.fast_noise.texture.HolographicTextureGenerator
import com.t8rin.fast_noise.texture.HolographicTextureParameters
import com.t8rin.fast_noise.texture.HoneycombTextureGenerator
import com.t8rin.fast_noise.texture.HoneycombTextureParameters
import com.t8rin.fast_noise.texture.HyperbolicTilingTextureGenerator
import com.t8rin.fast_noise.texture.HyperbolicTilingTextureParameters
import com.t8rin.fast_noise.texture.IceTextureGenerator
import com.t8rin.fast_noise.texture.IceTextureParameters
import com.t8rin.fast_noise.texture.InkMarblingTextureGenerator
import com.t8rin.fast_noise.texture.InkMarblingTextureParameters
import com.t8rin.fast_noise.texture.InkWashMountainsTextureGenerator
import com.t8rin.fast_noise.texture.InkWashMountainsTextureParameters
import com.t8rin.fast_noise.texture.IrisTextureGenerator
import com.t8rin.fast_noise.texture.IrisTextureParameters
import com.t8rin.fast_noise.texture.JuliaSetTextureGenerator
import com.t8rin.fast_noise.texture.JuliaSetTextureParameters
import com.t8rin.fast_noise.texture.KaleidoscopeCrystalTextureGenerator
import com.t8rin.fast_noise.texture.KaleidoscopeCrystalTextureParameters
import com.t8rin.fast_noise.texture.KelpForestTextureGenerator
import com.t8rin.fast_noise.texture.KelpForestTextureParameters
import com.t8rin.fast_noise.texture.KintsugiTextureGenerator
import com.t8rin.fast_noise.texture.KintsugiTextureParameters
import com.t8rin.fast_noise.texture.LavaLampTextureGenerator
import com.t8rin.fast_noise.texture.LavaLampTextureParameters
import com.t8rin.fast_noise.texture.LavaTextureGenerator
import com.t8rin.fast_noise.texture.LavaTextureParameters
import com.t8rin.fast_noise.texture.LeatherTextureGenerator
import com.t8rin.fast_noise.texture.LeatherTextureParameters
import com.t8rin.fast_noise.texture.LichenColonyTextureGenerator
import com.t8rin.fast_noise.texture.LichenColonyTextureParameters
import com.t8rin.fast_noise.texture.LichtenbergFigureTextureGenerator
import com.t8rin.fast_noise.texture.LichtenbergFigureTextureParameters
import com.t8rin.fast_noise.texture.LightningTextureGenerator
import com.t8rin.fast_noise.texture.LightningTextureParameters
import com.t8rin.fast_noise.texture.LiquidCrystalTextureGenerator
import com.t8rin.fast_noise.texture.LiquidCrystalTextureParameters
import com.t8rin.fast_noise.texture.LunarEjectaTextureGenerator
import com.t8rin.fast_noise.texture.LunarEjectaTextureParameters
import com.t8rin.fast_noise.texture.MagneticFieldTextureGenerator
import com.t8rin.fast_noise.texture.MagneticFieldTextureParameters
import com.t8rin.fast_noise.texture.MandelbrotTextureGenerator
import com.t8rin.fast_noise.texture.MandelbrotTextureParameters
import com.t8rin.fast_noise.texture.MicroscopicDiatomsTextureGenerator
import com.t8rin.fast_noise.texture.MicroscopicDiatomsTextureParameters
import com.t8rin.fast_noise.texture.MoebiusWeaveTextureGenerator
import com.t8rin.fast_noise.texture.MoebiusWeaveTextureParameters
import com.t8rin.fast_noise.texture.MoireGuillocheTextureGenerator
import com.t8rin.fast_noise.texture.MoireGuillocheTextureParameters
import com.t8rin.fast_noise.texture.MossTextureGenerator
import com.t8rin.fast_noise.texture.MossTextureParameters
import com.t8rin.fast_noise.texture.MotherboardHeatmapTextureGenerator
import com.t8rin.fast_noise.texture.MotherboardHeatmapTextureParameters
import com.t8rin.fast_noise.texture.MyceliumTextureGenerator
import com.t8rin.fast_noise.texture.MyceliumTextureParameters
import com.t8rin.fast_noise.texture.NautilusShellTextureGenerator
import com.t8rin.fast_noise.texture.NautilusShellTextureParameters
import com.t8rin.fast_noise.texture.NebulaTextureGenerator
import com.t8rin.fast_noise.texture.NebulaTextureParameters
import com.t8rin.fast_noise.texture.NeonCityTextureGenerator
import com.t8rin.fast_noise.texture.NeonCityTextureParameters
import com.t8rin.fast_noise.texture.NeuralGardenTextureGenerator
import com.t8rin.fast_noise.texture.NeuralGardenTextureParameters
import com.t8rin.fast_noise.texture.OceanCurrentsTextureGenerator
import com.t8rin.fast_noise.texture.OceanCurrentsTextureParameters
import com.t8rin.fast_noise.texture.OilSlickTextureGenerator
import com.t8rin.fast_noise.texture.OilSlickTextureParameters
import com.t8rin.fast_noise.texture.OpalTextureGenerator
import com.t8rin.fast_noise.texture.OpalTextureParameters
import com.t8rin.fast_noise.texture.OrigamiFacetsTextureGenerator
import com.t8rin.fast_noise.texture.OrigamiFacetsTextureParameters
import com.t8rin.fast_noise.texture.PaperTextureGenerator
import com.t8rin.fast_noise.texture.PaperTextureParameters
import com.t8rin.fast_noise.texture.PeacockFeatherTextureGenerator
import com.t8rin.fast_noise.texture.PeacockFeatherTextureParameters
import com.t8rin.fast_noise.texture.PhyllotaxisBloomTextureGenerator
import com.t8rin.fast_noise.texture.PhyllotaxisBloomTextureParameters
import com.t8rin.fast_noise.texture.PorousSpongeTextureGenerator
import com.t8rin.fast_noise.texture.PorousSpongeTextureParameters
import com.t8rin.fast_noise.texture.PrismaticLightTextureGenerator
import com.t8rin.fast_noise.texture.PrismaticLightTextureParameters
import com.t8rin.fast_noise.texture.QuantumFoamTextureGenerator
import com.t8rin.fast_noise.texture.QuantumFoamTextureParameters
import com.t8rin.fast_noise.texture.QuasicrystalTextureGenerator
import com.t8rin.fast_noise.texture.QuasicrystalTextureParameters
import com.t8rin.fast_noise.texture.RainOnGlassTextureGenerator
import com.t8rin.fast_noise.texture.RainOnGlassTextureParameters
import com.t8rin.fast_noise.texture.RayleighBenardTextureGenerator
import com.t8rin.fast_noise.texture.RayleighBenardTextureParameters
import com.t8rin.fast_noise.texture.ReactionDiffusionTextureGenerator
import com.t8rin.fast_noise.texture.ReactionDiffusionTextureParameters
import com.t8rin.fast_noise.texture.RingedPlanetTextureGenerator
import com.t8rin.fast_noise.texture.RingedPlanetTextureParameters
import com.t8rin.fast_noise.texture.RiverDeltaTextureGenerator
import com.t8rin.fast_noise.texture.RiverDeltaTextureParameters
import com.t8rin.fast_noise.texture.RorschachInkblotTextureGenerator
import com.t8rin.fast_noise.texture.RorschachInkblotTextureParameters
import com.t8rin.fast_noise.texture.RustTextureGenerator
import com.t8rin.fast_noise.texture.RustTextureParameters
import com.t8rin.fast_noise.texture.SandTextureGenerator
import com.t8rin.fast_noise.texture.SandTextureParameters
import com.t8rin.fast_noise.texture.SeismicInterferenceTextureGenerator
import com.t8rin.fast_noise.texture.SeismicInterferenceTextureParameters
import com.t8rin.fast_noise.texture.SierpinskiTriangleTextureGenerator
import com.t8rin.fast_noise.texture.SierpinskiTriangleTextureParameters
import com.t8rin.fast_noise.texture.SlimeMoldTextureGenerator
import com.t8rin.fast_noise.texture.SlimeMoldTextureParameters
import com.t8rin.fast_noise.texture.SmokeTextureGenerator
import com.t8rin.fast_noise.texture.SmokeTextureParameters
import com.t8rin.fast_noise.texture.SnakeSkinTextureGenerator
import com.t8rin.fast_noise.texture.SnakeSkinTextureParameters
import com.t8rin.fast_noise.texture.SoapFilmTextureGenerator
import com.t8rin.fast_noise.texture.SoapFilmTextureParameters
import com.t8rin.fast_noise.texture.SolarGranulationTextureGenerator
import com.t8rin.fast_noise.texture.SolarGranulationTextureParameters
import com.t8rin.fast_noise.texture.SpectralPrismTextureGenerator
import com.t8rin.fast_noise.texture.SpectralPrismTextureParameters
import com.t8rin.fast_noise.texture.StainedGlassTextureGenerator
import com.t8rin.fast_noise.texture.StainedGlassTextureParameters
import com.t8rin.fast_noise.texture.StoneTextureGenerator
import com.t8rin.fast_noise.texture.StoneTextureParameters
import com.t8rin.fast_noise.texture.StrangeAttractorTextureGenerator
import com.t8rin.fast_noise.texture.StrangeAttractorTextureParameters
import com.t8rin.fast_noise.texture.SupernovaTextureGenerator
import com.t8rin.fast_noise.texture.SupernovaTextureParameters
import com.t8rin.fast_noise.texture.TerrainTextureGenerator
import com.t8rin.fast_noise.texture.TerrainTextureParameters
import com.t8rin.fast_noise.texture.TerrazzoTextureGenerator
import com.t8rin.fast_noise.texture.TerrazzoTextureParameters
import com.t8rin.fast_noise.texture.TopographyTextureGenerator
import com.t8rin.fast_noise.texture.TopographyTextureParameters
import com.t8rin.fast_noise.texture.TopologicalKnotTextureGenerator
import com.t8rin.fast_noise.texture.TopologicalKnotTextureParameters
import com.t8rin.fast_noise.texture.TurbulentInkTextureGenerator
import com.t8rin.fast_noise.texture.TurbulentInkTextureParameters
import com.t8rin.fast_noise.texture.VeinedLeafTextureGenerator
import com.t8rin.fast_noise.texture.VeinedLeafTextureParameters
import com.t8rin.fast_noise.texture.VelvetTextureGenerator
import com.t8rin.fast_noise.texture.VelvetTextureParameters
import com.t8rin.fast_noise.texture.VolcanicObsidianTextureGenerator
import com.t8rin.fast_noise.texture.VolcanicObsidianTextureParameters
import com.t8rin.fast_noise.texture.WaterRippleTextureGenerator
import com.t8rin.fast_noise.texture.WaterRippleTextureParameters
import com.t8rin.fast_noise.texture.WatercolorTextureGenerator
import com.t8rin.fast_noise.texture.WatercolorTextureParameters
import com.t8rin.fast_noise.texture.WoodTextureGenerator
import com.t8rin.fast_noise.texture.WoodTextureParameters
import com.t8rin.fast_noise.texture.XRayBotanicalTextureGenerator
import com.t8rin.fast_noise.texture.XRayBotanicalTextureParameters
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

private fun showcaseDemo(
    name: String,
    scale: Float,
    parameters: List<Pair<String, Float>>,
    generate: (seed: Int, values: List<Float>) -> Bitmap?
) = TextureDemo(
    name = name,
    controls = listOf(
        TextureControl("Scale", scale, 0.001f..0.04f),
        *parameters.map { (controlName, default) ->
            TextureControl(
                name = controlName,
                default = default,
                range = if (controlName == "Contrast") 0.2f..2.5f else 0f..1f
            )
        }.toTypedArray()
    ),
    generate = generate
)

private val showcaseDemos = listOf(
    showcaseDemo(
        "Geode / agate",
        0.008f,
        listOf(
            "Bands" to 0.68f,
            "Distortion" to 0.55f,
            "Crystal sharpness" to 0.72f,
            "Sparkle" to 0.70f,
            "Contrast" to 1.35f
        )
    ) { seed, v ->
        GeodeTextureGenerator().generate(
            768,
            768,
            GeodeTextureParameters(seed, v[0], v[1], v[2], v[3], v[4], v[5])
        )
    },
    showcaseDemo(
        "Prismatic light",
        0.008f,
        listOf(
            "Facets" to 0.66f,
            "Dispersion" to 0.72f,
            "Beam sharpness" to 0.76f,
            "Bloom" to 0.78f,
            "Contrast" to 1.35f
        )
    ) { seed, v ->
        PrismaticLightTextureGenerator().generate(
            768,
            768,
            PrismaticLightTextureParameters(seed, v[0], v[1], v[2], v[3], v[4], v[5])
        )
    },
    showcaseDemo(
        "Stained glass",
        0.008f,
        listOf(
            "Cells" to 0.62f,
            "Irregularity" to 0.38f,
            "Lead sharpness" to 0.75f,
            "Glow" to 0.52f,
            "Contrast" to 1.25f
        )
    ) { seed, v ->
        StainedGlassTextureGenerator().generate(
            768,
            768,
            StainedGlassTextureParameters(seed, v[0], v[1], v[2], v[3], v[4], v[5])
        )
    },
    showcaseDemo(
        "Kelp forest",
        0.006f,
        listOf(
            "Density" to 0.64f,
            "Current" to 0.68f,
            "Depth sharpness" to 0.62f,
            "Light rays" to 0.76f,
            "Contrast" to 1.35f
        )
    ) { seed, v ->
        KelpForestTextureGenerator().generate(
            768,
            768,
            KelpForestTextureParameters(seed, v[0], v[1], v[2], v[3], v[4], v[5])
        )
    },
    showcaseDemo(
        "Frost fern",
        0.008f,
        listOf(
            "Branches" to 0.72f,
            "Branching" to 0.48f,
            "Crystal sharpness" to 0.70f,
            "Frost glow" to 0.78f,
            "Contrast" to 1.30f
        )
    ) { seed, v ->
        FrostFernTextureGenerator().generate(
            768,
            768,
            FrostFernTextureParameters(seed, v[0], v[1], v[2], v[3], v[4], v[5])
        )
    },
    showcaseDemo(
        "Liquid crystal",
        0.008f,
        listOf(
            "Domains" to 0.66f,
            "Dispersion" to 0.62f,
            "Boundary sharpness" to 0.58f,
            "Shine" to 0.72f,
            "Contrast" to 1.30f
        )
    ) { seed, v ->
        LiquidCrystalTextureGenerator().generate(
            768,
            768,
            LiquidCrystalTextureParameters(seed, v[0], v[1], v[2], v[3], v[4], v[5])
        )
    },
    showcaseDemo(
        "Dragon scales",
        0.007f,
        listOf(
            "Scale density" to 0.58f,
            "Curvature" to 0.35f,
            "Rim sharpness" to 0.72f,
            "Iridescence" to 0.60f,
            "Contrast" to 1.35f
        )
    ) { seed, v ->
        DragonScalesTextureGenerator().generate(
            768,
            768,
            DragonScalesTextureParameters(seed, v[0], v[1], v[2], v[3], v[4], v[5])
        )
    },
    showcaseDemo(
        "Firefly swarm",
        0.007f,
        listOf(
            "Density" to 0.66f,
            "Drift" to 0.72f,
            "Trail sharpness" to 0.82f,
            "Glow" to 0.90f,
            "Contrast" to 1.45f
        )
    ) { seed, v ->
        FireflySwarmTextureGenerator().generate(
            768,
            768,
            FireflySwarmTextureParameters(seed, v[0], v[1], v[2], v[3], v[4], v[5])
        )
    },
    showcaseDemo(
        "Mycelium",
        0.008f,
        listOf(
            "Network density" to 0.70f,
            "Branching" to 0.62f,
            "Fiber sharpness" to 0.72f,
            "Glow" to 0.74f,
            "Contrast" to 1.45f
        )
    ) { seed, v ->
        MyceliumTextureGenerator().generate(
            768,
            768,
            MyceliumTextureParameters(seed, v[0], v[1], v[2], v[3], v[4], v[5])
        )
    },
    showcaseDemo(
        "Kintsugi",
        0.008f,
        listOf(
            "Crack density" to 0.60f,
            "Irregularity" to 0.48f,
            "Gold sharpness" to 0.82f,
            "Metallic" to 0.70f,
            "Contrast" to 1.35f
        )
    ) { seed, v ->
        KintsugiTextureGenerator().generate(
            768,
            768,
            KintsugiTextureParameters(seed, v[0], v[1], v[2], v[3], v[4], v[5])
        )
    },
    showcaseDemo(
        "Carbon fiber",
        0.010f,
        listOf(
            "Weave density" to 0.58f,
            "Twill" to 0.22f,
            "Fiber sharpness" to 0.74f,
            "Sheen" to 0.42f,
            "Contrast" to 1.50f
        )
    ) { seed, v ->
        CarbonFiberTextureGenerator().generate(
            768,
            768,
            CarbonFiberTextureParameters(seed, v[0], v[1], v[2], v[3], v[4], v[5])
        )
    },
    showcaseDemo(
        "Circuit board",
        0.008f,
        listOf(
            "Trace density" to 0.64f,
            "Routing" to 0.25f,
            "Trace sharpness" to 0.80f,
            "Emission" to 0.72f,
            "Contrast" to 1.45f
        )
    ) { seed, v ->
        CircuitBoardTextureGenerator().generate(
            768,
            768,
            CircuitBoardTextureParameters(seed, v[0], v[1], v[2], v[3], v[4], v[5])
        )
    },
    showcaseDemo(
        "Soap film",
        0.007f,
        listOf(
            "Bands" to 0.68f,
            "Flow" to 0.72f,
            "Film sharpness" to 0.55f,
            "Shine" to 0.72f,
            "Contrast" to 1.25f
        )
    ) { seed, v ->
        SoapFilmTextureGenerator().generate(
            768,
            768,
            SoapFilmTextureParameters(seed, v[0], v[1], v[2], v[3], v[4], v[5])
        )
    },
    showcaseDemo(
        "Moiré guilloché",
        0.008f,
        listOf(
            "Frequency" to 0.55f,
            "Offset" to 0.30f,
            "Line sharpness" to 0.72f,
            "Glow" to 0.48f,
            "Contrast" to 1.50f
        )
    ) { seed, v ->
        MoireGuillocheTextureGenerator().generate(
            768,
            768,
            MoireGuillocheTextureParameters(seed, v[0], v[1], v[2], v[3], v[4], v[5])
        )
    },
    showcaseDemo(
        "Snake skin",
        0.009f,
        listOf(
            "Scale density" to 0.62f,
            "Irregularity" to 0.40f,
            "Rim sharpness" to 0.72f,
            "Sheen" to 0.40f,
            "Contrast" to 1.35f
        )
    ) { seed, v ->
        SnakeSkinTextureGenerator().generate(
            768,
            768,
            SnakeSkinTextureParameters(seed, v[0], v[1], v[2], v[3], v[4], v[5])
        )
    },
    showcaseDemo(
        "Terrazzo",
        0.008f,
        listOf(
            "Chip density" to 0.58f,
            "Variation" to 0.32f,
            "Chip sharpness" to 0.68f,
            "Polish" to 0.28f,
            "Contrast" to 1.25f
        )
    ) { seed, v ->
        TerrazzoTextureGenerator().generate(
            768,
            768,
            TerrazzoTextureParameters(seed, v[0], v[1], v[2], v[3], v[4], v[5])
        )
    },
    showcaseDemo(
        "Galaxy filaments",
        0.007f,
        listOf(
            "Filament density" to 0.70f,
            "Turbulence" to 0.62f,
            "Filament sharpness" to 0.78f,
            "Star glow" to 0.84f,
            "Contrast" to 1.50f
        )
    ) { seed, v ->
        GalaxyFilamentsTextureGenerator().generate(
            768,
            768,
            GalaxyFilamentsTextureParameters(seed, v[0], v[1], v[2], v[3], v[4], v[5])
        )
    },
    showcaseDemo(
        "Volcanic obsidian",
        0.008f,
        listOf(
            "Facets" to 0.64f,
            "Fracturing" to 0.48f,
            "Edge sharpness" to 0.75f,
            "Lava glow" to 0.64f,
            "Contrast" to 1.55f
        )
    ) { seed, v ->
        VolcanicObsidianTextureGenerator().generate(
            768,
            768,
            VolcanicObsidianTextureParameters(seed, v[0], v[1], v[2], v[3], v[4], v[5])
        )
    },
    showcaseDemo(
        "Motherboard heatmap",
        0.008f,
        listOf(
            "Trace density" to 0.62f,
            "Heat spread" to 0.38f,
            "Trace sharpness" to 0.76f,
            "Heat glow" to 0.68f,
            "Contrast" to 1.45f
        )
    ) { seed, v ->
        MotherboardHeatmapTextureGenerator().generate(
            768,
            768,
            MotherboardHeatmapTextureParameters(seed, v[0], v[1], v[2], v[3], v[4], v[5])
        )
    },
    showcaseDemo(
        "Microscopic diatoms",
        0.007f,
        listOf(
            "Colony density" to 0.62f,
            "Shell variation" to 0.30f,
            "Pore sharpness" to 0.74f,
            "Luminescence" to 0.58f,
            "Contrast" to 1.35f
        )
    ) { seed, v ->
        MicroscopicDiatomsTextureGenerator().generate(
            768,
            768,
            MicroscopicDiatomsTextureParameters(seed, v[0], v[1], v[2], v[3], v[4], v[5])
        )
    },
    showcaseDemo(
        "Reaction diffusion · 2-pass",
        0.009f,
        listOf(
            "Pattern scale" to 0.72f,
            "Feedback" to 0.68f,
            "Edge sharpness" to 0.72f,
            "Glow" to 0.68f,
            "Contrast" to 1.45f
        )
    ) { seed, v ->
        ReactionDiffusionTextureGenerator().generate(
            768,
            768,
            ReactionDiffusionTextureParameters(seed, v[0], v[1], v[2], v[3], v[4], v[5])
        )
    },
    showcaseDemo(
        "Coral growth · 2-pass",
        0.008f,
        listOf(
            "Branch density" to 0.68f,
            "Growth bias" to 0.62f,
            "Edge sharpness" to 0.70f,
            "Glow" to 0.72f,
            "Contrast" to 1.40f
        )
    ) { seed, v ->
        CoralGrowthTextureGenerator().generate(
            768,
            768,
            CoralGrowthTextureParameters(seed, v[0], v[1], v[2], v[3], v[4], v[5])
        )
    },
    showcaseDemo(
        "Slime mold · 2-pass",
        0.008f,
        listOf(
            "Trail density" to 0.72f,
            "Chemotaxis" to 0.72f,
            "Trail sharpness" to 0.76f,
            "Glow" to 0.74f,
            "Contrast" to 1.50f
        )
    ) { seed, v ->
        SlimeMoldTextureGenerator().generate(
            768,
            768,
            SlimeMoldTextureParameters(seed, v[0], v[1], v[2], v[3], v[4], v[5])
        )
    },
    showcaseDemo(
        "Dendritic crystal · 2-pass",
        0.008f,
        listOf(
            "Branch count" to 0.68f,
            "Branching" to 0.52f,
            "Crystal sharpness" to 0.80f,
            "Glow" to 0.82f,
            "Contrast" to 1.50f
        )
    ) { seed, v ->
        DendriticCrystalTextureGenerator().generate(
            768,
            768,
            DendriticCrystalTextureParameters(seed, v[0], v[1], v[2], v[3], v[4], v[5])
        )
    },
    showcaseDemo(
        "Electric arc field · 2-pass",
        0.008f,
        listOf(
            "Arc count" to 0.72f,
            "Turbulence" to 0.72f,
            "Arc sharpness" to 0.82f,
            "Glow" to 0.90f,
            "Contrast" to 1.55f
        )
    ) { seed, v ->
        ElectricArcFieldTextureGenerator().generate(
            768,
            768,
            ElectricArcFieldTextureParameters(seed, v[0], v[1], v[2], v[3], v[4], v[5])
        )
    },
    showcaseDemo(
        "Cloud chamber · 2-pass",
        0.008f,
        listOf(
            "Track density" to 0.68f,
            "Deflection" to 0.58f,
            "Track sharpness" to 0.75f,
            "Glow" to 0.82f,
            "Contrast" to 1.45f
        )
    ) { seed, v ->
        CloudChamberTextureGenerator().generate(
            768,
            768,
            CloudChamberTextureParameters(seed, v[0], v[1], v[2], v[3], v[4], v[5])
        )
    },
    showcaseDemo(
        "Turbulent ink · 2-pass",
        0.006f,
        listOf(
            "Ribbon density" to 0.70f,
            "Turbulence" to 0.78f,
            "Edge sharpness" to 0.68f,
            "Bleed" to 0.62f,
            "Contrast" to 1.40f
        )
    ) { seed, v ->
        TurbulentInkTextureGenerator().generate(
            768,
            768,
            TurbulentInkTextureParameters(seed, v[0], v[1], v[2], v[3], v[4], v[5])
        )
    },
    showcaseDemo(
        "Cellular embryo · 2-pass",
        0.008f,
        listOf(
            "Embryo density" to 0.62f,
            "Morphology" to 0.48f,
            "Membrane sharpness" to 0.72f,
            "Glow" to 0.58f,
            "Contrast" to 1.35f
        )
    ) { seed, v ->
        CellularEmbryoTextureGenerator().generate(
            768,
            768,
            CellularEmbryoTextureParameters(seed, v[0], v[1], v[2], v[3], v[4], v[5])
        )
    },
    showcaseDemo(
        "Neural garden · 2-pass",
        0.008f,
        listOf(
            "Neuron density" to 0.70f,
            "Branching" to 0.72f,
            "Fiber sharpness" to 0.78f,
            "Glow" to 0.78f,
            "Contrast" to 1.50f
        )
    ) { seed, v ->
        NeuralGardenTextureGenerator().generate(
            768,
            768,
            NeuralGardenTextureParameters(seed, v[0], v[1], v[2], v[3], v[4], v[5])
        )
    },
    showcaseDemo(
        "Magnetic field · 2-pass",
        0.008f,
        listOf(
            "Line density" to 0.68f,
            "Pole distortion" to 0.42f,
            "Line sharpness" to 0.74f,
            "Glow" to 0.72f,
            "Contrast" to 1.40f
        )
    ) { seed, v ->
        MagneticFieldTextureGenerator().generate(
            768,
            768,
            MagneticFieldTextureParameters(seed, v[0], v[1], v[2], v[3], v[4], v[5])
        )
    },
    showcaseDemo(
        "River delta · 2-pass",
        0.006f,
        listOf(
            "Channel density" to 0.68f,
            "Erosion" to 0.78f,
            "Channel sharpness" to 0.76f,
            "Water glow" to 0.62f,
            "Contrast" to 1.50f
        )
    ) { seed, v ->
        RiverDeltaTextureGenerator().generate(
            768,
            768,
            RiverDeltaTextureParameters(seed, v[0], v[1], v[2], v[3], v[4], v[5])
        )
    },
    showcaseDemo(
        "Lichen colony · 2-pass",
        0.009f,
        listOf(
            "Colony density" to 0.64f,
            "Spread" to 0.58f,
            "Edge sharpness" to 0.70f,
            "Moisture" to 0.48f,
            "Contrast" to 1.35f
        )
    ) { seed, v ->
        LichenColonyTextureGenerator().generate(
            768,
            768,
            LichenColonyTextureParameters(seed, v[0], v[1], v[2], v[3], v[4], v[5])
        )
    },
    showcaseDemo(
        "Bacterial culture · 2-pass",
        0.009f,
        listOf(
            "Colony density" to 0.68f,
            "Growth variation" to 0.62f,
            "Membrane sharpness" to 0.68f,
            "Glow" to 0.58f,
            "Contrast" to 1.35f
        )
    ) { seed, v ->
        BacterialCultureTextureGenerator().generate(
            768,
            768,
            BacterialCultureTextureParameters(seed, v[0], v[1], v[2], v[3], v[4], v[5])
        )
    },
    showcaseDemo(
        "Fluid vorticity · 2-pass",
        0.007f,
        listOf(
            "Vortex density" to 0.72f,
            "Turbulence" to 0.78f,
            "Ridge sharpness" to 0.70f,
            "Glow" to 0.72f,
            "Contrast" to 1.40f
        )
    ) { seed, v ->
        FluidVorticityTextureGenerator().generate(
            768,
            768,
            FluidVorticityTextureParameters(seed, v[0], v[1], v[2], v[3], v[4], v[5])
        )
    },
    showcaseDemo(
        "Crystal growth · 2-pass",
        0.008f,
        listOf(
            "Crystal density" to 0.64f,
            "Anisotropy" to 0.48f,
            "Facet sharpness" to 0.80f,
            "Glow" to 0.78f,
            "Contrast" to 1.50f
        )
    ) { seed, v ->
        CrystalGrowthTextureGenerator().generate(
            768,
            768,
            CrystalGrowthTextureParameters(seed, v[0], v[1], v[2], v[3], v[4], v[5])
        )
    },
    showcaseDemo(
        "Galactic web · 2-pass",
        0.007f,
        listOf(
            "Node density" to 0.72f,
            "Gravity warp" to 0.68f,
            "Filament sharpness" to 0.80f,
            "Glow" to 0.86f,
            "Contrast" to 1.55f
        )
    ) { seed, v ->
        GalacticWebTextureGenerator().generate(
            768,
            768,
            GalacticWebTextureParameters(seed, v[0], v[1], v[2], v[3], v[4], v[5])
        )
    },
    showcaseDemo(
        "Veined leaf · 2-pass",
        0.008f,
        listOf(
            "Vein density" to 0.68f,
            "Curvature" to 0.52f,
            "Vein sharpness" to 0.78f,
            "Translucency" to 0.62f,
            "Contrast" to 1.40f
        )
    ) { seed, v ->
        VeinedLeafTextureGenerator().generate(
            768,
            768,
            VeinedLeafTextureParameters(seed, v[0], v[1], v[2], v[3], v[4], v[5])
        )
    },
    showcaseDemo(
        "Porous sponge · 2-pass",
        0.009f,
        listOf(
            "Pore density" to 0.64f,
            "Irregularity" to 0.42f,
            "Pore sharpness" to 0.72f,
            "Moisture" to 0.42f,
            "Contrast" to 1.35f
        )
    ) { seed, v ->
        PorousSpongeTextureGenerator().generate(
            768,
            768,
            PorousSpongeTextureParameters(seed, v[0], v[1], v[2], v[3], v[4], v[5])
        )
    },
    showcaseDemo(
        "Rain on glass · 2-pass",
        0.008f,
        listOf(
            "Drop density" to 0.68f,
            "Streaking" to 0.58f,
            "Drop sharpness" to 0.74f,
            "Refraction" to 0.78f,
            "Contrast" to 1.40f
        )
    ) { seed, v ->
        RainOnGlassTextureGenerator().generate(
            768,
            768,
            RainOnGlassTextureParameters(seed, v[0], v[1], v[2], v[3], v[4], v[5])
        )
    },
    showcaseDemo(
        "Ember field · 2-pass",
        0.008f,
        listOf(
            "Ember density" to 0.72f,
            "Updraft" to 0.62f,
            "Ember sharpness" to 0.76f,
            "Glow" to 0.88f,
            "Contrast" to 1.55f
        )
    ) { seed, v ->
        EmberFieldTextureGenerator().generate(
            768,
            768,
            EmberFieldTextureParameters(seed, v[0], v[1], v[2], v[3], v[4], v[5])
        )
    },
    showcaseDemo(
        "Quantum foam · 2-pass",
        0.008f,
        listOf(
            "Bubble density" to 0.72f,
            "Uncertainty" to 0.78f,
            "Boundary sharpness" to 0.72f,
            "Glow" to 0.78f,
            "Contrast" to 1.50f
        )
    ) { seed, v ->
        QuantumFoamTextureGenerator().generate(
            768,
            768,
            QuantumFoamTextureParameters(seed, v[0], v[1], v[2], v[3], v[4], v[5])
        )
    },
    showcaseDemo(
        "Chladni plate · 2-pass",
        0.008f,
        listOf(
            "Mode count" to 0.62f,
            "Asymmetry" to 0.38f,
            "Line sharpness" to 0.82f,
            "Metallic" to 0.54f,
            "Contrast" to 1.55f
        )
    ) { seed, v ->
        ChladniPlateTextureGenerator().generate(
            768,
            768,
            ChladniPlateTextureParameters(seed, v[0], v[1], v[2], v[3], v[4], v[5])
        )
    },
    showcaseDemo(
        "Cymatic rosette · 2-pass",
        0.008f,
        listOf(
            "Petal count" to 0.68f,
            "Resonance" to 0.44f,
            "Ridge sharpness" to 0.78f,
            "Glow" to 0.76f,
            "Contrast" to 1.45f
        )
    ) { seed, v ->
        CymaticRosetteTextureGenerator().generate(
            768,
            768,
            CymaticRosetteTextureParameters(seed, v[0], v[1], v[2], v[3], v[4], v[5])
        )
    },
    showcaseDemo(
        "Lichtenberg figure · 2-pass",
        0.008f,
        listOf(
            "Branch count" to 0.72f,
            "Branching" to 0.66f,
            "Arc sharpness" to 0.84f,
            "Glow" to 0.90f,
            "Contrast" to 1.60f
        )
    ) { seed, v ->
        LichtenbergFigureTextureGenerator().generate(
            768,
            768,
            LichtenbergFigureTextureParameters(seed, v[0], v[1], v[2], v[3], v[4], v[5])
        )
    },
    showcaseDemo(
        "Quasicrystal · 2-pass",
        0.008f,
        listOf(
            "Symmetry" to 0.70f,
            "Phase warp" to 0.36f,
            "Ridge sharpness" to 0.76f,
            "Glow" to 0.62f,
            "Contrast" to 1.45f
        )
    ) { seed, v ->
        QuasicrystalTextureGenerator().generate(
            768,
            768,
            QuasicrystalTextureParameters(seed, v[0], v[1], v[2], v[3], v[4], v[5])
        )
    },
    showcaseDemo(
        "Mandelbrot · 2-pass",
        0.004f,
        listOf(
            "Iteration detail" to 0.68f,
            "Center offset" to 0.42f,
            "Boundary sharpness" to 0.78f,
            "Glow" to 0.72f,
            "Contrast" to 1.50f
        )
    ) { seed, v ->
        MandelbrotTextureGenerator().generate(
            768,
            768,
            MandelbrotTextureParameters(seed, v[0], v[1], v[2], v[3], v[4], v[5])
        )
    },
    showcaseDemo(
        "Burning ship · 2-pass",
        0.0035f,
        listOf(
            "Iteration detail" to 0.72f,
            "Center offset" to 0.48f,
            "Boundary sharpness" to 0.80f,
            "Glow" to 0.78f,
            "Contrast" to 1.55f
        )
    ) { seed, v ->
        BurningShipTextureGenerator().generate(
            768,
            768,
            BurningShipTextureParameters(seed, v[0], v[1], v[2], v[3], v[4], v[5])
        )
    },
    showcaseDemo(
        "Julia set · 2-pass",
        0.008f,
        listOf(
            "Iterations" to 0.68f,
            "Constant phase" to 0.52f,
            "Filament sharpness" to 0.78f,
            "Glow" to 0.76f,
            "Contrast" to 1.50f
        )
    ) { seed, v ->
        JuliaSetTextureGenerator().generate(
            768,
            768,
            JuliaSetTextureParameters(seed, v[0], v[1], v[2], v[3], v[4], v[5])
        )
    },
    showcaseDemo(
        "Kaleidoscope crystal · 2-pass",
        0.008f,
        listOf(
            "Segments" to 0.72f,
            "Fold warp" to 0.46f,
            "Facet sharpness" to 0.76f,
            "Glow" to 0.72f,
            "Contrast" to 1.45f
        )
    ) { seed, v ->
        KaleidoscopeCrystalTextureGenerator().generate(
            768,
            768,
            KaleidoscopeCrystalTextureParameters(seed, v[0], v[1], v[2], v[3], v[4], v[5])
        )
    },
    showcaseDemo(
        "Spectral prism · 2-pass",
        0.008f,
        listOf(
            "Facet count" to 0.64f,
            "Refraction" to 0.58f,
            "Edge sharpness" to 0.80f,
            "Bloom" to 0.80f,
            "Contrast" to 1.45f
        )
    ) { seed, v ->
        SpectralPrismTextureGenerator().generate(
            768,
            768,
            SpectralPrismTextureParameters(seed, v[0], v[1], v[2], v[3], v[4], v[5])
        )
    },
    showcaseDemo(
        "Topological knot · 2-pass",
        0.008f,
        listOf(
            "Turns" to 0.62f,
            "Twist" to 0.54f,
            "Tube sharpness" to 0.82f,
            "Glow" to 0.76f,
            "Contrast" to 1.55f
        )
    ) { seed, v ->
        TopologicalKnotTextureGenerator().generate(
            768,
            768,
            TopologicalKnotTextureParameters(seed, v[0], v[1], v[2], v[3], v[4], v[5])
        )
    },
    showcaseDemo(
        "X-ray botanical · 2-pass",
        0.008f,
        listOf(
            "Leaf density" to 0.66f,
            "Curvature" to 0.48f,
            "Vein sharpness" to 0.76f,
            "Luminescence" to 0.74f,
            "Contrast" to 1.45f
        )
    ) { seed, v ->
        XRayBotanicalTextureGenerator().generate(
            768,
            768,
            XRayBotanicalTextureParameters(seed, v[0], v[1], v[2], v[3], v[4], v[5])
        )
    },
    showcaseDemo(
        "Chromatophore · 2-pass",
        0.008f,
        listOf(
            "Cell density" to 0.68f,
            "Pulse phase" to 0.52f,
            "Cell sharpness" to 0.72f,
            "Iridescence" to 0.70f,
            "Contrast" to 1.40f
        )
    ) { seed, v ->
        ChromatophoreTextureGenerator().generate(
            768,
            768,
            ChromatophoreTextureParameters(seed, v[0], v[1], v[2], v[3], v[4], v[5])
        )
    },
    showcaseDemo(
        "Biomechanical tissue · 2-pass",
        0.008f,
        listOf(
            "Rib density" to 0.66f,
            "Tension" to 0.58f,
            "Ridge sharpness" to 0.78f,
            "Wet sheen" to 0.62f,
            "Contrast" to 1.50f
        )
    ) { seed, v ->
        BiomechanicalTissueTextureGenerator().generate(
            768,
            768,
            BiomechanicalTissueTextureParameters(seed, v[0], v[1], v[2], v[3], v[4], v[5])
        )
    },
    showcaseDemo(
        "Gilded filigree · 2-pass",
        0.008f,
        listOf(
            "Curl density" to 0.68f,
            "Ornament" to 0.46f,
            "Line sharpness" to 0.82f,
            "Metallic" to 0.74f,
            "Contrast" to 1.55f
        )
    ) { seed, v ->
        GildedFiligreeTextureGenerator().generate(
            768,
            768,
            GildedFiligreeTextureParameters(seed, v[0], v[1], v[2], v[3], v[4], v[5])
        )
    },
    showcaseDemo(
        "Ancient runes · 2-pass",
        0.008f,
        listOf(
            "Glyph density" to 0.62f,
            "Erosion" to 0.54f,
            "Stroke sharpness" to 0.78f,
            "Glow" to 0.72f,
            "Contrast" to 1.50f
        )
    ) { seed, v ->
        AncientRunesTextureGenerator().generate(
            768,
            768,
            AncientRunesTextureParameters(seed, v[0], v[1], v[2], v[3], v[4], v[5])
        )
    },
    showcaseDemo(
        "Solar granulation · 2-pass",
        0.008f,
        listOf(
            "Granule density" to 0.72f,
            "Convection" to 0.58f,
            "Edge sharpness" to 0.74f,
            "Heat glow" to 0.86f,
            "Contrast" to 1.50f
        )
    ) { seed, v ->
        SolarGranulationTextureGenerator().generate(
            768,
            768,
            SolarGranulationTextureParameters(seed, v[0], v[1], v[2], v[3], v[4], v[5])
        )
    },
    showcaseDemo(
        "Lunar ejecta · 2-pass",
        0.008f,
        listOf(
            "Crater density" to 0.64f,
            "Ray length" to 0.48f,
            "Rim sharpness" to 0.80f,
            "Albedo" to 0.50f,
            "Contrast" to 1.50f
        )
    ) { seed, v ->
        LunarEjectaTextureGenerator().generate(
            768,
            768,
            LunarEjectaTextureParameters(seed, v[0], v[1], v[2], v[3], v[4], v[5])
        )
    },
    showcaseDemo(
        "Ocean currents · 2-pass",
        0.007f,
        listOf(
            "Current density" to 0.70f,
            "Curl" to 0.72f,
            "Stream sharpness" to 0.76f,
            "Glow" to 0.68f,
            "Contrast" to 1.45f
        )
    ) { seed, v ->
        OceanCurrentsTextureGenerator().generate(
            768,
            768,
            OceanCurrentsTextureParameters(seed, v[0], v[1], v[2], v[3], v[4], v[5])
        )
    },
    showcaseDemo(
        "Ink wash mountains · 2-pass",
        0.006f,
        listOf(
            "Ridge count" to 0.62f,
            "Mist" to 0.58f,
            "Ridge sharpness" to 0.72f,
            "Paper glow" to 0.42f,
            "Contrast" to 1.40f
        )
    ) { seed, v ->
        InkWashMountainsTextureGenerator().generate(
            768,
            768,
            InkWashMountainsTextureParameters(seed, v[0], v[1], v[2], v[3], v[4], v[5])
        )
    },
    showcaseDemo(
        "Neon city · 2-pass",
        0.008f,
        listOf(
            "Building density" to 0.68f,
            "Perspective" to 0.52f,
            "Edge sharpness" to 0.80f,
            "Neon glow" to 0.86f,
            "Contrast" to 1.55f
        )
    ) { seed, v ->
        NeonCityTextureGenerator().generate(
            768,
            768,
            NeonCityTextureParameters(seed, v[0], v[1], v[2], v[3], v[4], v[5])
        )
    },
    showcaseDemo(
        "Phyllotaxis bloom · 2-pass",
        0.008f,
        listOf(
            "Seed density" to 0.68f,
            "Spiral angle" to 0.62f,
            "Seed sharpness" to 0.82f,
            "Glow" to 0.72f,
            "Contrast" to 1.50f
        )
    ) { seed, v ->
        PhyllotaxisBloomTextureGenerator().generate(
            768,
            768,
            PhyllotaxisBloomTextureParameters(seed, v[0], v[1], v[2], v[3], v[4], v[5])
        )
    },
    showcaseDemo(
        "Sierpiński triangle · 2-pass",
        0.008f,
        listOf(
            "Depth" to 0.68f,
            "Rotation" to 0.50f,
            "Edge sharpness" to 0.82f,
            "Glow" to 0.66f,
            "Contrast" to 1.55f
        )
    ) { seed, v ->
        SierpinskiTriangleTextureGenerator().generate(
            768,
            768,
            SierpinskiTriangleTextureParameters(seed, v[0], v[1], v[2], v[3], v[4], v[5])
        )
    },
    showcaseDemo(
        "Apollonian gasket · 2-pass",
        0.008f,
        listOf(
            "Recursion" to 0.66f,
            "Curvature" to 0.52f,
            "Boundary sharpness" to 0.80f,
            "Glow" to 0.72f,
            "Contrast" to 1.50f
        )
    ) { seed, v ->
        ApollonianGasketTextureGenerator().generate(
            768,
            768,
            ApollonianGasketTextureParameters(seed, v[0], v[1], v[2], v[3], v[4], v[5])
        )
    },
    showcaseDemo(
        "Hyperbolic tiling · 2-pass",
        0.008f,
        listOf(
            "Polygon sides" to 0.62f,
            "Curvature" to 0.72f,
            "Edge sharpness" to 0.80f,
            "Glow" to 0.74f,
            "Contrast" to 1.50f
        )
    ) { seed, v ->
        HyperbolicTilingTextureGenerator().generate(
            768,
            768,
            HyperbolicTilingTextureParameters(seed, v[0], v[1], v[2], v[3], v[4], v[5])
        )
    },
    showcaseDemo(
        "Möbius weave · 2-pass",
        0.008f,
        listOf(
            "Band count" to 0.66f,
            "Twist" to 0.62f,
            "Edge sharpness" to 0.76f,
            "Sheen" to 0.68f,
            "Contrast" to 1.45f
        )
    ) { seed, v ->
        MoebiusWeaveTextureGenerator().generate(
            768,
            768,
            MoebiusWeaveTextureParameters(seed, v[0], v[1], v[2], v[3], v[4], v[5])
        )
    },
    showcaseDemo(
        "Rorschach inkblot · 2-pass",
        0.006f,
        listOf(
            "Lobe count" to 0.70f,
            "Symmetry" to 0.82f,
            "Edge sharpness" to 0.68f,
            "Bleed" to 0.52f,
            "Contrast" to 1.45f
        )
    ) { seed, v ->
        RorschachInkblotTextureGenerator().generate(
            768,
            768,
            RorschachInkblotTextureParameters(seed, v[0], v[1], v[2], v[3], v[4], v[5])
        )
    },
    showcaseDemo(
        "Seismic interference · 2-pass",
        0.008f,
        listOf(
            "Source count" to 0.64f,
            "Phase" to 0.52f,
            "Ridge sharpness" to 0.78f,
            "Glow" to 0.72f,
            "Contrast" to 1.50f
        )
    ) { seed, v ->
        SeismicInterferenceTextureGenerator().generate(
            768,
            768,
            SeismicInterferenceTextureParameters(seed, v[0], v[1], v[2], v[3], v[4], v[5])
        )
    },
    showcaseDemo(
        "Rayleigh–Bénard convection · 2-pass",
        0.008f,
        listOf(
            "Cell density" to 0.70f,
            "Convection" to 0.62f,
            "Edge sharpness" to 0.72f,
            "Heat glow" to 0.82f,
            "Contrast" to 1.50f
        )
    ) { seed, v ->
        RayleighBenardTextureGenerator().generate(
            768,
            768,
            RayleighBenardTextureParameters(seed, v[0], v[1], v[2], v[3], v[4], v[5])
        )
    },
    showcaseDemo(
        "Origami facets · 2-pass",
        0.008f,
        listOf(
            "Fold density" to 0.66f,
            "Asymmetry" to 0.52f,
            "Crease sharpness" to 0.82f,
            "Sheen" to 0.62f,
            "Contrast" to 1.45f
        )
    ) { seed, v ->
        OrigamiFacetsTextureGenerator().generate(
            768,
            768,
            OrigamiFacetsTextureParameters(seed, v[0], v[1], v[2], v[3], v[4], v[5])
        )
    },
    showcaseDemo(
        "Fiber optic bundle · 2-pass",
        0.008f,
        listOf(
            "Fiber density" to 0.72f,
            "Bend" to 0.62f,
            "Core sharpness" to 0.82f,
            "Glow" to 0.88f,
            "Contrast" to 1.55f
        )
    ) { seed, v ->
        FiberOpticBundleTextureGenerator().generate(
            768,
            768,
            FiberOpticBundleTextureParameters(seed, v[0], v[1], v[2], v[3], v[4], v[5])
        )
    }
)

private val textureDemos = showcaseDemos + listOf(
    TextureDemo(
        "Ferrofluid crown",
        controls(
            0.012f,
            TextureControl("Spikes", 19f, 3f..48f),
            TextureControl("Spike length", 0.14f, 0f..0.3f),
            TextureControl("Body size", 0.22f, 0.05f..0.4f),
            TextureControl("Metallic", 0.86f, 0f..1f),
            TextureControl("Distortion", 0.42f, 0f..1f)
        )
    ) { seed, v ->
        FerrofluidCrownTextureGenerator().generate(
            768,
            768,
            FerrofluidCrownTextureParameters(seed, v[0], v[1], v[2], v[3], v[4], v[5])
        )
    },
    TextureDemo(
        "Supernova",
        controls(
            0.01f,
            TextureControl("Shock radius", 0.27f, 0.08f..0.45f),
            TextureControl("Shell width", 0.075f, 0.008f..0.18f),
            TextureControl("Ejecta", 0.72f, 0f..1f),
            TextureControl("Turbulence", 0.68f, 0f..1f),
            TextureControl("Stars", 0.24f, 0f..1f)
        )
    ) { seed, v ->
        SupernovaTextureGenerator().generate(
            768,
            768,
            SupernovaTextureParameters(seed, v[0], v[1], v[2], v[3], v[4], v[5])
        )
    },
    TextureDemo(
        "Iris",
        controls(
            0.014f,
            TextureControl("Pupil size", 0.12f, 0.03f..0.28f),
            TextureControl("Iris size", 0.38f, 0.12f..0.48f),
            TextureControl("Fibers", 46f, 6f..100f),
            TextureControl("Color variation", 0.72f, 0f..1f),
            TextureControl("Catchlight", 0.82f, 0f..1f)
        )
    ) { seed, v ->
        IrisTextureGenerator().generate(
            768,
            768,
            IrisTextureParameters(seed, v[0], v[1], v[2], v[3], v[4], v[5])
        )
    },
    TextureDemo(
        "Peacock feather",
        controls(
            0.012f,
            TextureControl("Eye size", 0.24f, 0.08f..0.4f),
            TextureControl("Barb density", 54f, 8f..100f),
            TextureControl("Curvature", 0.58f, 0f..1f),
            TextureControl("Iridescence", 0.8f, 0f..1f),
            TextureControl("Softness", 0.42f, 0f..1f)
        )
    ) { seed, v ->
        PeacockFeatherTextureGenerator().generate(
            768,
            768,
            PeacockFeatherTextureParameters(seed, v[0], v[1], v[2], v[3], v[4], v[5])
        )
    },
    TextureDemo(
        "Nautilus shell",
        controls(
            0.011f,
            TextureControl("Turns", 3.4f, 0.5f..8f),
            TextureControl("Chambers", 19f, 3f..48f),
            TextureControl("Opening", 0.13f, 0.03f..0.3f),
            TextureControl("Ridges", 0.68f, 0f..1f),
            TextureControl("Pearlescence", 0.46f, 0f..1f)
        )
    ) { seed, v ->
        NautilusShellTextureGenerator().generate(
            768,
            768,
            NautilusShellTextureParameters(seed, v[0], v[1], v[2], v[3], v[4], v[5])
        )
    },
    TextureDemo(
        "Ringed planet",
        controls(
            0.009f,
            TextureControl("Planet size", 0.24f, 0.08f..0.38f),
            TextureControl("Ring tilt", 0.72f, 0f..1f),
            TextureControl("Ring width", 0.16f, 0.03f..0.3f),
            TextureControl("Atmosphere", 0.62f, 0f..1f),
            TextureControl("Stars", 0.25f, 0f..1f)
        )
    ) { seed, v ->
        RingedPlanetTextureGenerator().generate(
            768,
            768,
            RingedPlanetTextureParameters(seed, v[0], v[1], v[2], v[3], v[4], v[5])
        )
    },
    TextureDemo(
        "Event horizon",
        controls(
            0.008f,
            TextureControl("Disk tilt", 0.72f, 0f..1f),
            TextureControl("Horizon size", 0.15f, 0.04f..0.35f),
            TextureControl("Disk width", 0.065f, 0.005f..0.16f),
            TextureControl("Lensing", 0.82f, 0f..1f),
            TextureControl("Stars", 0.22f, 0f..1f)
        )
    ) { seed, v ->
        EventHorizonTextureGenerator().generate(
            768,
            768,
            EventHorizonTextureParameters(seed, v[0], v[1], v[2], v[3], v[4], v[5])
        )
    },
    TextureDemo(
        "Fractal bloom",
        controls(
            0.01f,
            TextureControl("Petals", 7f, 2f..16f),
            TextureControl("Layers", 5f, 1f..12f),
            TextureControl("Curl", 4.2f, -10f..10f),
            TextureControl("Filigree", 0.7f, 0f..1f),
            TextureControl("Glow", 0.72f, 0f..1f)
        )
    ) { seed, v ->
        FractalBloomTextureGenerator().generate(
            768,
            768,
            FractalBloomTextureParameters(seed, v[0], v[1], v[2], v[3], v[4], v[5])
        )
    },
    TextureDemo(
        "Chromatic tunnel",
        controls(
            0.009f,
            TextureControl("Depth", 18f, 2f..40f),
            TextureControl("Twist", 5.5f, -14f..14f),
            TextureControl("Facets", 7f, 2f..18f),
            TextureControl("Curvature", 0.48f, 0f..1f),
            TextureControl("Glow", 0.78f, 0f..1f)
        )
    ) { seed, v ->
        ChromaticTunnelTextureGenerator().generate(
            768,
            768,
            ChromaticTunnelTextureParameters(seed, v[0], v[1], v[2], v[3], v[4], v[5])
        )
    },
    TextureDemo(
        "Eclipse corona",
        controls(
            0.008f,
            TextureControl("Moon size", 0.23f, 0.06f..0.4f),
            TextureControl("Corona size", 0.2f, 0.02f..0.4f),
            TextureControl("Rays", 34f, 2f..80f),
            TextureControl("Turbulence", 0.62f, 0f..1f),
            TextureControl("Diamond ring", 0.8f, 0f..1f)
        )
    ) { seed, v ->
        EclipseCoronaTextureGenerator().generate(
            768,
            768,
            EclipseCoronaTextureParameters(seed, v[0], v[1], v[2], v[3], v[4], v[5])
        )
    },
    TextureDemo(
        "Strange attractor",
        controls(
            0.01f,
            TextureControl("Lobes", 3f, 1f..9f),
            TextureControl("Orbit density", 18f, 4f..40f),
            TextureControl("Curvature", 6f, 1f..14f),
            TextureControl("Thickness", 0.035f, 0.002f..0.1f),
            TextureControl("Glow", 0.8f, 0f..1f)
        )
    ) { seed, v ->
        StrangeAttractorTextureGenerator().generate(
            768,
            768,
            StrangeAttractorTextureParameters(seed, v[0], v[1], v[2], v[3], v[4], v[5])
        )
    },
    TextureDemo("Opal", controls(0.007f, TextureControl("Color play", 0.82f, 0f..1f), TextureControl("Milkiness", 0.48f, 0f..1f), TextureControl("Bands", 8f, 1f..30f), TextureControl("Distortion", 28f, 0f..100f), TextureControl("Glow", 0.62f, 0f..1f))) { seed, v -> OpalTextureGenerator().generate(768, 768, OpalTextureParameters(seed, v[0], v[1], v[2], v[3], v[4], v[5])) },
    TextureDemo("Damascus steel", controls(0.006f, TextureControl("Layers", 22f, 2f..60f), TextureControl("Folding", 0.72f, 0f..1f), TextureControl("Distortion", 34f, 0f..100f), TextureControl("Polish", 0.58f, 0f..1f), TextureControl("Contrast", 1.4f, 0f..3f))) { seed, v -> DamascusTextureGenerator().generate(768, 768, DamascusTextureParameters(seed, v[0], v[1], v[2], v[3], v[4], v[5])) },
    TextureDemo("Lightning", controls(0.006f, TextureControl("Branches", 7f, 1f..25f), TextureControl("Turbulence", 42f, 0f..120f), TextureControl("Width", 0.055f, 0.005f..0.3f), TextureControl("Glow", 0.82f, 0f..1f), TextureControl("Intensity", 0.9f, 0f..1f))) { seed, v -> LightningTextureGenerator().generate(768, 768, LightningTextureParameters(seed, v[0], v[1], v[2], v[3], v[4], v[5])) },
    TextureDemo("Velvet", controls(0.014f, TextureControl("Fibers", 0.78f, 0f..1f), TextureControl("Direction", 0.18f, 0f..1f), TextureControl("Softness", 0.7f, 0f..1f), TextureControl("Sheen", 0.62f, 0f..1f), TextureControl("Folds", 0.34f, 0f..1f))) { seed, v -> VelvetTextureGenerator().generate(768, 768, VelvetTextureParameters(seed, v[0], v[1], v[2], v[3], v[4], v[5])) },
    TextureDemo("Ink marbling", controls(0.006f, TextureControl("Ribbons", 14f, 2f..40f), TextureControl("Turbulence", 48f, 0f..120f), TextureControl("Feathering", 0.52f, 0f..1f), TextureControl("Ink balance", 0.5f, 0f..1f), TextureControl("Contrast", 1.25f, 0f..3f))) { seed, v -> InkMarblingTextureGenerator().generate(768, 768, InkMarblingTextureParameters(seed, v[0], v[1], v[2], v[3], v[4], v[5])) },
    TextureDemo("Holographic foil", controls(0.008f, TextureControl("Spectrum", 12f, 1f..30f), TextureControl("Crinkles", 0.7f, 0f..1f), TextureControl("Diffraction", 0.82f, 0f..1f), TextureControl("Angle", 0.3f, 0f..1f), TextureControl("Shine", 0.72f, 0f..1f))) { seed, v -> HolographicTextureGenerator().generate(768, 768, HolographicTextureParameters(seed, v[0], v[1], v[2], v[3], v[4], v[5])) },
    TextureDemo("Bioluminescence", controls(0.009f, TextureControl("Veins", 0.72f, 0f..1f), TextureControl("Branching", 0.58f, 0f..1f), TextureControl("Turbulence", 30f, 0f..100f), TextureControl("Glow", 0.86f, 0f..1f), TextureControl("Depth", 0.62f, 0f..1f))) { seed, v -> BioluminescenceTextureGenerator().generate(768, 768, BioluminescenceTextureParameters(seed, v[0], v[1], v[2], v[3], v[4], v[5])) },
    TextureDemo("Cosmic vortex", controls(0.008f, TextureControl("Arms", 5f, 1f..12f), TextureControl("Twist", 12f, 1f..25f), TextureControl("Turbulence", 0.52f, 0f..1f), TextureControl("Stars", 0.28f, 0f..1f), TextureControl("Core glow", 0.82f, 0f..1f))) { seed, v -> CosmicVortexTextureGenerator().generate(768, 768, CosmicVortexTextureParameters(seed, v[0], v[1], v[2], v[3], v[4], v[5])) },
    TextureDemo("Lava lamp", controls(0.007f, TextureControl("Blobs", 6f, 1f..20f), TextureControl("Softness", 0.24f, 0f..1f), TextureControl("Distortion", 32f, 0f..100f), TextureControl("Glow", 0.58f, 0f..1f), TextureControl("Contrast", 1.3f, 0f..3f))) { seed, v -> LavaLampTextureGenerator().generate(768, 768, LavaLampTextureParameters(seed, v[0], v[1], v[2], v[3], v[4], v[5])) },
    TextureDemo(
        name = "Grass",
        controls = controls(
            0.012f,
            TextureControl("Blade density", 1f, 0f..1f),
            TextureControl("Blade length", 50f, 2f..50f),
            TextureControl("Wind", 1f, 0f..1f),
            TextureControl("Patchiness", 0.35f, 0f..1f),
            TextureControl("Highlights", 0.45f, 0f..1f)
        )
    ) { seed, v ->
        GrassTextureGenerator().generate(
            768,
            768,
            GrassTextureParameters(seed, v[0], v[1], v[2], v[3], v[4], v[5])
        )
    },
    TextureDemo(
        name = "Dirt",
        controls = controls(
            0.009f,
            TextureControl("Clumps", 0.62f, 0f..1f),
            TextureControl("Moisture", 0.35f, 0f..1f),
            TextureControl("Pebbles", 0.22f, 0f..1f),
            TextureControl("Roughness", 0.68f, 0f..1f),
            TextureControl("Variation", 0.52f, 0f..1f)
        )
    ) { seed, v ->
        DirtTextureGenerator().generate(
            768,
            768,
            DirtTextureParameters(seed, v[0], v[1], v[2], v[3], v[4], v[5])
        )
    },
    TextureDemo(
        name = "Leather",
        controls = controls(
            0.011f,
            TextureControl("Wrinkles", 0.58f, 0f..1f),
            TextureControl("Pores", 0.42f, 0f..1f),
            TextureControl("Grain", 0.5f, 0f..1f),
            TextureControl("Softness", 0.38f, 0f..1f),
            TextureControl("Shine", 0.035f, 0f..1f)
        )
    ) { seed, v ->
        LeatherTextureGenerator().generate(
            768,
            768,
            LeatherTextureParameters(seed, v[0], v[1], v[2], v[3], v[4], v[5])
        )
    },
    TextureDemo(
        name = "Concrete",
        controls = controls(
            0.014f,
            TextureControl("Aggregate", 0.45f, 0f..1f),
            TextureControl("Stains", 0.28f, 0f..1f),
            TextureControl("Roughness", 0.65f, 0f..1f),
            TextureControl("Cracks", 0.14f, 0f..1f),
            TextureControl("Contrast", 1.08f, 0f..3f)
        )
    ) { seed, v ->
        ConcreteTextureGenerator().generate(
            768,
            768,
            ConcreteTextureParameters(seed, v[0], v[1], v[2], v[3], v[4], v[5])
        )
    },
    TextureDemo(
        name = "Asphalt",
        controls = controls(
            0.02f,
            TextureControl("Aggregate", 0.72f, 0f..1f),
            TextureControl("Tar", 0.44f, 0f..1f),
            TextureControl("Wear", 0.28f, 0f..1f),
            TextureControl("Speckles", 0.48f, 0f..1f),
            TextureControl("Contrast", 1.3f, 0f..3f)
        )
    ) { seed, v ->
        AsphaltTextureGenerator().generate(
            768,
            768,
            AsphaltTextureParameters(seed, v[0], v[1], v[2], v[3], v[4], v[5])
        )
    },
    TextureDemo(
        name = "Moss",
        controls = controls(
            0.018f,
            TextureControl("Density", 0.74f, 0f..1f),
            TextureControl("Fibers", 0.58f, 0f..1f),
            TextureControl("Moisture", 0.38f, 0f..1f),
            TextureControl("Variation", 0.62f, 0f..1f),
            TextureControl("Clumps", 0.46f, 0f..1f)
        )
    ) { seed, v ->
        MossTextureGenerator().generate(
            768,
            768,
            MossTextureParameters(seed, v[0], v[1], v[2], v[3], v[4], v[5])
        )
    },
    TextureDemo(
        name = "Fire",
        controls = controls(
            0.006f,
            TextureControl("Flame frequency", 8f, 1f..30f),
            TextureControl("Turbulence", 36f, 0f..100f),
            TextureControl("Intensity", 0.78f, 0f..1f),
            TextureControl("Smoke", 0.18f, 0f..1f),
            TextureControl("Detail", 0.62f, 0f..1f)
        )
    ) { seed, v ->
        FireTextureGenerator().generate(
            768,
            768,
            FireTextureParameters(seed, v[0], v[1], v[2], v[3], v[4], v[5])
        )
    },
    TextureDemo(
        name = "Aurora",
        controls = controls(
            0.004f,
            TextureControl("Ribbons", 7f, 1f..25f),
            TextureControl("Distortion", 34f, 0f..100f),
            TextureControl("Glow", 0.72f, 0f..1f),
            TextureControl("Stars", 0.2f, 0f..1f),
            TextureControl("Contrast", 1.32f, 0f..3f)
        )
    ) { seed, v ->
        AuroraTextureGenerator().generate(
            768,
            768,
            AuroraTextureParameters(seed, v[0], v[1], v[2], v[3], v[4], v[5])
        )
    },
    TextureDemo(
        name = "Oil slick",
        controls = controls(
            0.008f,
            TextureControl("Bands", 13f, 1f..30f),
            TextureControl("Distortion", 32f, 0f..100f),
            TextureControl("Iridescence", 0.82f, 0f..1f),
            TextureControl("Darkness", 0.3f, 0f..1f),
            TextureControl("Contrast", 1.2f, 0f..3f)
        )
    ) { seed, v ->
        OilSlickTextureGenerator().generate(
            768,
            768,
            OilSlickTextureParameters(seed, v[0], v[1], v[2], v[3], v[4], v[5])
        )
    },
    TextureDemo(
        name = "Watercolor",
        controls = controls(
            0.006f,
            TextureControl("Blooms", 0.72f, 0f..1f),
            TextureControl("Pigment", 0.65f, 0f..1f),
            TextureControl("Edges", 0.38f, 0f..1f),
            TextureControl("Paper", 0.22f, 0f..1f),
            TextureControl("Diffusion", 0.62f, 0f..1f)
        )
    ) { seed, v ->
        WatercolorTextureGenerator().generate(
            768,
            768,
            WatercolorTextureParameters(seed, v[0], v[1], v[2], v[3], v[4], v[5])
        )
    },
    TextureDemo(
        name = "Abstract flow",
        controls = controls(
            0.006f,
            TextureControl("Frequency", 12f, 1f..40f),
            TextureControl("Distortion", 46f, 0f..120f),
            TextureControl("Symmetry", 0.3f, 0f..1f),
            TextureControl("Sharpness", 1.25f, 0.1f..5f),
            TextureControl("Glow", 0.62f, 0f..1f)
        )
    ) { seed, v ->
        FlowTextureGenerator().generate(
            768,
            768,
            FlowTextureParameters(seed, v[0], v[1], v[2], v[3], v[4], v[5])
        )
    },
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
            0.0039f,
            TextureControl("Rings", 11.4f, 2f..50f),
            TextureControl("Grain", 0.75f, 0f..1f),
            TextureControl("Distortion", 15f, 0f..30f),
            TextureControl("Stretch", 7.42f, 0.2f..12f),
            TextureControl("Contrast", 0.17f, 0f..3f)
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
    },
    TextureDemo(
        name = "Foliage",
        controls = controls(
            0.022f,
            TextureControl("Density", 0.72f, 0f..1f),
            TextureControl("Edge softness", 0.16f, 0.01f..0.6f),
            TextureControl("Veins", 0.38f, 0f..1f),
            TextureControl("Lighting", 0.62f, 0f..1f),
            TextureControl("Variation", 0.58f, 0f..1f)
        )
    ) { seed, v ->
        FoliageTextureGenerator().generate(
            768,
            768,
            FoliageTextureParameters(seed, v[0], v[1], v[2], v[3], v[4], v[5])
        )
    },
    TextureDemo(
        name = "Bricks",
        controls = controls(
            0.012f,
            TextureControl("Aspect ratio", 2.15f, 0.5f..5f),
            TextureControl("Mortar width", 0.09f, 0.005f..0.4f),
            TextureControl("Irregularity", 0.18f, 0f..1f),
            TextureControl("Roughness", 0.42f, 0f..1f),
            TextureControl("Bevel", 0.55f, 0f..1f)
        )
    ) { seed, v ->
        BrickTextureGenerator().generate(
            768,
            768,
            BrickTextureParameters(seed, v[0], v[1], v[2], v[3], v[4], v[5])
        )
    },
    TextureDemo(
        name = "Terrain",
        controls = controls(
            0.0045f,
            TextureControl("Water level", 0.34f, 0f..1f),
            TextureControl("Mountain level", 0.7f, 0f..1f),
            TextureControl("Erosion", 0.46f, 0f..1f),
            TextureControl("Detail", 0.58f, 0f..1f),
            TextureControl("Snow level", 0.86f, 0f..1f)
        )
    ) { seed, v ->
        TerrainTextureGenerator().generate(
            768,
            768,
            TerrainTextureParameters(seed, v[0], v[1], v[2], v[3], v[4], v[5])
        )
    },
    TextureDemo(
        name = "Ice",
        controls = controls(
            0.014f,
            TextureControl("Crack width", 0.075f, 0.002f..0.4f),
            TextureControl("Frost", 0.48f, 0f..1f),
            TextureControl("Depth", 0.64f, 0f..1f),
            TextureControl("Distortion", 8f, 0f..50f),
            TextureControl("Sparkle", 0.32f, 0f..1f)
        )
    ) { seed, v ->
        IceTextureGenerator().generate(
            768,
            768,
            IceTextureParameters(seed, v[0], v[1], v[2], v[3], v[4], v[5])
        )
    },
    TextureDemo(
        name = "Sand dunes",
        controls = controls(
            0.005f,
            TextureControl("Dune frequency", 12f, 1f..40f),
            TextureControl("Wind angle", 0.32f, 0f..1f),
            TextureControl("Ripples", 0.62f, 0f..1f),
            TextureControl("Grain", 0.22f, 0f..1f),
            TextureControl("Contrast", 1.18f, 0f..3f)
        )
    ) { seed, v ->
        SandTextureGenerator().generate(
            768,
            768,
            SandTextureParameters(seed, v[0], v[1], v[2], v[3], v[4], v[5])
        )
    },
    TextureDemo(
        name = "Nebula",
        controls = controls(
            0.004f,
            TextureControl("Turbulence", 44f, 0f..100f),
            TextureControl("Cloud density", 0.64f, 0f..1f),
            TextureControl("Stars", 0.38f, 0f..1f),
            TextureControl("Glow", 0.72f, 0f..1f),
            TextureControl("Contrast", 1.45f, 0f..3f)
        )
    ) { seed, v ->
        NebulaTextureGenerator().generate(
            768,
            768,
            NebulaTextureParameters(seed, v[0], v[1], v[2], v[3], v[4], v[5])
        )
    },
    TextureDemo(
        name = "Honeycomb",
        controls = controls(
            0.018f,
            TextureControl("Border width", 0.095f, 0.005f..0.4f),
            TextureControl("Bevel", 0.5f, 0f..1f),
            TextureControl("Irregularity", 0.12f, 0f..1f),
            TextureControl("Fill", 0.72f, 0f..1f),
            TextureControl("Glow", 0.35f, 0f..1f)
        )
    ) { seed, v ->
        HoneycombTextureGenerator().generate(
            768,
            768,
            HoneycombTextureParameters(seed, v[0], v[1], v[2], v[3], v[4], v[5])
        )
    }
)
