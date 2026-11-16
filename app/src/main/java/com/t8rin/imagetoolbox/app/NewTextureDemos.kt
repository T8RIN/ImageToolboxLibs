package com.t8rin.imagetoolbox.app

import android.graphics.Bitmap
import com.t8rin.fast_noise.texture.KnittedFabricTextureGenerator
import com.t8rin.fast_noise.texture.KnittedFabricTextureParameters
import com.t8rin.fast_noise.texture.WickerWeaveTextureGenerator
import com.t8rin.fast_noise.texture.WickerWeaveTextureParameters
import com.t8rin.fast_noise.texture.ChainmailTextureGenerator
import com.t8rin.fast_noise.texture.ChainmailTextureParameters
import com.t8rin.fast_noise.texture.HerringboneParquetTextureGenerator
import com.t8rin.fast_noise.texture.HerringboneParquetTextureParameters
import com.t8rin.fast_noise.texture.RoofTilesTextureGenerator
import com.t8rin.fast_noise.texture.RoofTilesTextureParameters
import com.t8rin.fast_noise.texture.HammeredMetalTextureGenerator
import com.t8rin.fast_noise.texture.HammeredMetalTextureParameters
import com.t8rin.fast_noise.texture.CorrugatedMetalTextureGenerator
import com.t8rin.fast_noise.texture.CorrugatedMetalTextureParameters
import com.t8rin.fast_noise.texture.BubbleWrapTextureGenerator
import com.t8rin.fast_noise.texture.BubbleWrapTextureParameters
import com.t8rin.fast_noise.texture.PenroseTilingTextureGenerator
import com.t8rin.fast_noise.texture.PenroseTilingTextureParameters
import com.t8rin.fast_noise.texture.CairoTilingTextureGenerator
import com.t8rin.fast_noise.texture.CairoTilingTextureParameters
import com.t8rin.fast_noise.texture.IslamicStarTilingTextureGenerator
import com.t8rin.fast_noise.texture.IslamicStarTilingTextureParameters
import com.t8rin.fast_noise.texture.CelticKnotworkTextureGenerator
import com.t8rin.fast_noise.texture.CelticKnotworkTextureParameters
import com.t8rin.fast_noise.texture.SeigaihaTextureGenerator
import com.t8rin.fast_noise.texture.SeigaihaTextureParameters
import com.t8rin.fast_noise.texture.AsanohaTextureGenerator
import com.t8rin.fast_noise.texture.AsanohaTextureParameters
import com.t8rin.fast_noise.texture.HoundstoothTextureGenerator
import com.t8rin.fast_noise.texture.HoundstoothTextureParameters
import com.t8rin.fast_noise.texture.JigsawTextureGenerator
import com.t8rin.fast_noise.texture.JigsawTextureParameters
import com.t8rin.fast_noise.texture.TreeBarkTextureGenerator
import com.t8rin.fast_noise.texture.TreeBarkTextureParameters
import com.t8rin.fast_noise.texture.PineConeTextureGenerator
import com.t8rin.fast_noise.texture.PineConeTextureParameters
import com.t8rin.fast_noise.texture.ZebraStripesTextureGenerator
import com.t8rin.fast_noise.texture.ZebraStripesTextureParameters
import com.t8rin.fast_noise.texture.LeopardSpotsTextureGenerator
import com.t8rin.fast_noise.texture.LeopardSpotsTextureParameters
import com.t8rin.fast_noise.texture.LissajousRibbonsTextureGenerator
import com.t8rin.fast_noise.texture.LissajousRibbonsTextureParameters
import com.t8rin.fast_noise.texture.BismuthCrystalTextureGenerator
import com.t8rin.fast_noise.texture.BismuthCrystalTextureParameters
import com.t8rin.fast_noise.texture.HilbertRibbonsTextureGenerator
import com.t8rin.fast_noise.texture.HilbertRibbonsTextureParameters
import com.t8rin.fast_noise.texture.maze.MazeAlgorithm
import com.t8rin.fast_noise.texture.maze.MazeTextureGenerator
import com.t8rin.fast_noise.texture.maze.MazeTextureParameters
import kotlin.math.roundToInt

internal val newTextureDemos: List<TextureDemo> = listOf(
    coloredTextureDemo(
        name = "Maze",
        controls = listOf(
            TextureControl("Algorithm", 0f, 0f..2f, labels = listOf("Backtracking", "Prim", "Kruskal")),
            TextureControl("Columns", 32f, 1f..256f, steps = 254),
            TextureControl("Rows", 32f, 1f..256f, steps = 254),
            TextureControl("Wall width", 0.12f, 0.02f..0.8f),
            TextureControl("Padding", 0.04f, 0.01f..0.2f),
            TextureControl("Openings", 1f, 0f..1f, labels = listOf("Closed", "Open"))
        ),
        colors = listOf(0xFF172638.toInt(), 0xFFFFF3D8.toInt()),
        colorNames = listOf("Walls", "Background")
    ) { seed, v, colors ->
        MazeTextureGenerator().generate(
            768, 768,
            MazeTextureParameters(
                seed = seed,
                columns = v[1].roundToInt(),
                rows = v[2].roundToInt(),
                algorithm = MazeAlgorithm.entries[v[0].roundToInt()],
                wallWidth = v[3],
                padding = v[4],
                openings = v[5] >= 0.5f,
                wallColor = colors[0],
                backgroundColor = colors[1]
            )
        )
    },
    coloredTextureDemo(
        name = "Knitted fabric",
        controls = listOf(
            TextureControl("Scale", 0.014f, 0.001f..0.04f),
            TextureControl("Loop width", 0.65f, 0f..1f),
            TextureControl("Yarn twist", 0.65f, 0f..1f),
            TextureControl("Fuzz", 0.4f, 0f..1f),
            TextureControl("Direction", 0.5f, 0f..1f),
            TextureControl("Variation", 0.4f, 0f..1f)
        ),
        colors = listOf(
            0xFF21151D.toInt(),
            0xFFA04E67.toInt(),
            0xFFDC8B8A.toInt(),
            0xFFFFE8D2.toInt()
        )
    ) { seed, v, colors ->
        KnittedFabricTextureGenerator().generate(
            768, 768,
            KnittedFabricTextureParameters(
                seed, v[0], v[1], v[2], v[3], v[4], v[5],
                colors[0], colors[1], colors[2], colors[3]
            )
        )
    },
    coloredTextureDemo(
        name = "Wicker weave",
        controls = listOf(
            TextureControl("Scale", 0.014f, 0.001f..0.04f),
            TextureControl("Strip width", 0.8f, 0f..1f),
            TextureControl("Fibers", 0.6f, 0f..1f),
            TextureControl("Roughness", 0.3f, 0f..1f),
            TextureControl("Direction", 0.5f, 0f..1f),
            TextureControl("Variation", 0.5f, 0f..1f)
        ),
        colors = listOf(
            0xFF271B13.toInt(),
            0xFF936435.toInt(),
            0xFFC79954.toInt(),
            0xFFF3DFAB.toInt()
        )
    ) { seed, v, colors ->
        WickerWeaveTextureGenerator().generate(
            768, 768,
            WickerWeaveTextureParameters(
                seed, v[0], v[1], v[2], v[3], v[4], v[5],
                colors[0], colors[1], colors[2], colors[3]
            )
        )
    },
    coloredTextureDemo(
        name = "Chainmail",
        controls = listOf(
            TextureControl("Scale", 0.014f, 0.001f..0.04f),
            TextureControl("Ring radius", 0.65f, 0f..1f),
            TextureControl("Wire width", 0.45f, 0f..1f),
            TextureControl("Flattening", 0.35f, 0f..1f),
            TextureControl("Weave depth", 0.7f, 0f..1f),
            TextureControl("Polish", 0.65f, 0f..1f)
        ),
        colors = listOf(
            0xFF080E18.toInt(),
            0xFF4A6079.toInt(),
            0xFFC89145.toInt(),
            0xFFE9F1FC.toInt()
        )
    ) { seed, v, colors ->
        ChainmailTextureGenerator().generate(
            768, 768,
            ChainmailTextureParameters(
                seed, v[0], v[1], v[2], v[3], v[4], v[5],
                colors[0], colors[1], colors[2], colors[3]
            )
        )
    },
    coloredTextureDemo(
        name = "Herringbone parquet",
        controls = listOf(
            TextureControl("Scale", 0.014f, 0.001f..0.04f),
            TextureControl("Plank length", 0.4f, 0f..1f),
            TextureControl("Joint width", 0.3f, 0f..1f),
            TextureControl("Wood grain", 0.6f, 0f..1f),
            TextureControl("Direction", 0.5f, 0f..1f),
            TextureControl("Polish", 0.55f, 0f..1f)
        ),
        colors = listOf(
            0xFF21160F.toInt(),
            0xFF794020.toInt(),
            0xFFC58A45.toInt(),
            0xFFF5DC9C.toInt()
        )
    ) { seed, v, colors ->
        HerringboneParquetTextureGenerator().generate(
            768, 768,
            HerringboneParquetTextureParameters(
                seed, v[0], v[1], v[2], v[3], v[4], v[5],
                colors[0], colors[1], colors[2], colors[3]
            )
        )
    },
    coloredTextureDemo(
        name = "Roof tiles",
        controls = listOf(
            TextureControl("Scale", 0.02f, 0.001f..0.04f),
            TextureControl("Curvature", 0.6f, 0f..1f),
            TextureControl("Roughness", 0.25f, 0f..1f),
            TextureControl("Relief", 0.8f, 0f..1f),
            TextureControl("Shadows", 0.5f, 0f..1f),
            TextureControl("Variation", 0.6f, 0f..1f)
        ),
        colors = listOf(
            0xFF32150F.toInt(),
            0xFFA34125.toInt(),
            0xFFD57A43.toInt(),
            0xFFF5BD87.toInt()
        )
    ) { seed, v, colors ->
        RoofTilesTextureGenerator().generate(
            768, 768,
            RoofTilesTextureParameters(
                seed, v[0], v[1], v[2], v[3], v[4], v[5],
                colors[0], colors[1], colors[2], colors[3]
            )
        )
    },
    coloredTextureDemo(
        name = "Hammered metal",
        controls = listOf(
            TextureControl("Scale", 0.028f, 0.001f..0.04f),
            TextureControl("Irregularity", 0.7f, 0f..1f),
            TextureControl("Dent depth", 0.8f, 0f..1f),
            TextureControl("Polish", 0.8f, 0f..1f),
            TextureControl("Roughness", 0.25f, 0f..1f),
            TextureControl("Patina", 0.3f, 0f..1f)
        ),
        colors = listOf(
            0xFF28140E.toInt(),
            0xFFB66634.toInt(),
            0xFF367E78.toInt(),
            0xFFFFE2A6.toInt()
        )
    ) { seed, v, colors ->
        HammeredMetalTextureGenerator().generate(
            768, 768,
            HammeredMetalTextureParameters(
                seed, v[0], v[1], v[2], v[3], v[4], v[5],
                colors[0], colors[1], colors[2], colors[3]
            )
        )
    },
    coloredTextureDemo(
        name = "Corrugated metal",
        controls = listOf(
            TextureControl("Scale", 0.018f, 0.001f..0.04f),
            TextureControl("Direction", 0.5f, 0f..1f),
            TextureControl("Sharpness", 0.65f, 0f..1f),
            TextureControl("Polish", 0.8f, 0f..1f),
            TextureControl("Roughness", 0.25f, 0f..1f),
            TextureControl("Patina", 0.2f, 0f..1f)
        ),
        colors = listOf(
            0xFF121D29.toInt(),
            0xFF6B8297.toInt(),
            0xFFA76032.toInt(),
            0xFFECF6FC.toInt()
        )
    ) { seed, v, colors ->
        CorrugatedMetalTextureGenerator().generate(
            768, 768,
            CorrugatedMetalTextureParameters(
                seed, v[0], v[1], v[2], v[3], v[4], v[5],
                colors[0], colors[1], colors[2], colors[3]
            )
        )
    },
    coloredTextureDemo(
        name = "Bubble wrap",
        controls = listOf(
            TextureControl("Scale", 0.02f, 0.001f..0.04f),
            TextureControl("Bubble radius", 0.7f, 0f..1f),
            TextureControl("Relief", 0.7f, 0f..1f),
            TextureControl("Shine", 0.75f, 0f..1f),
            TextureControl("Rim width", 0.45f, 0f..1f),
            TextureControl("Highlights", 0.8f, 0f..1f)
        ),
        colors = listOf(
            0xFF15364F.toInt(),
            0xFF398AA6.toInt(),
            0xFFA3DBE3.toInt(),
            0xFFF0FFFF.toInt()
        )
    ) { seed, v, colors ->
        BubbleWrapTextureGenerator().generate(
            768, 768,
            BubbleWrapTextureParameters(
                seed, v[0], v[1], v[2], v[3], v[4], v[5],
                colors[0], colors[1], colors[2], colors[3]
            )
        )
    },
    coloredTextureDemo(
        name = "Penrose tiling",
        controls = listOf(
            TextureControl("Scale", 0.018f, 0.001f..0.04f),
            TextureControl("Rotation", 0f, 0f..1f),
            TextureControl("Joint width", 0.22f, 0f..1f),
            TextureControl("Roughness", 0.3f, 0f..1f),
            TextureControl("Inset", 0.4f, 0f..1f),
            TextureControl("Inlay width", 0.4f, 0f..1f)
        ),
        colors = listOf(
            0xFF101E2C.toInt(),
            0xFF247F91.toInt(),
            0xFFD7A448.toInt(),
            0xFFFFF0C8.toInt()
        )
    ) { seed, v, colors ->
        PenroseTilingTextureGenerator().generate(
            768, 768,
            PenroseTilingTextureParameters(
                seed, v[0], v[1], v[2], v[3], v[4], v[5],
                colors[0], colors[1], colors[2], colors[3]
            )
        )
    },
    coloredTextureDemo(
        name = "Cairo tiling",
        controls = listOf(
            TextureControl("Scale", 0.026f, 0.001f..0.04f),
            TextureControl("Rotation", 0f, 0f..1f),
            TextureControl("Joint width", 0.25f, 0f..1f),
            TextureControl("Variation", 0.75f, 0f..1f),
            TextureControl("Roughness", 0.3f, 0f..1f),
            TextureControl("Inlay width", 0.25f, 0f..1f)
        ),
        colors = listOf(
            0xFF172637.toInt(),
            0xFF45778A.toInt(),
            0xFFE3AE73.toInt(),
            0xFFFFF1D6.toInt()
        )
    ) { seed, v, colors ->
        CairoTilingTextureGenerator().generate(
            768, 768,
            CairoTilingTextureParameters(
                seed, v[0], v[1], v[2], v[3], v[4], v[5],
                colors[0], colors[1], colors[2], colors[3]
            )
        )
    },
    coloredTextureDemo(
        name = "Star mosaic",
        controls = listOf(
            TextureControl("Scale", 0.012f, 0.001f..0.04f),
            TextureControl("Tile radius", 0.6f, 0f..1f),
            TextureControl("Line width", 0.5f, 0f..1f),
            TextureControl("Star radius", 0.45f, 0f..1f),
            TextureControl("Roughness", 0.3f, 0f..1f),
            TextureControl("Brightness", 0.35f, 0f..1f)
        ),
        colors = listOf(
            0xFF0C2336.toInt(),
            0xFF146F88.toInt(),
            0xFFC0964A.toInt(),
            0xFFFFF0C8.toInt()
        )
    ) { seed, v, colors ->
        IslamicStarTilingTextureGenerator().generate(
            768, 768,
            IslamicStarTilingTextureParameters(
                seed, v[0], v[1], v[2], v[3], v[4], v[5],
                colors[0], colors[1], colors[2], colors[3]
            )
        )
    },
    coloredTextureDemo(
        name = "Celtic knotwork",
        controls = listOf(
            TextureControl("Scale", 0.012f, 0.001f..0.04f),
            TextureControl("Rotation", 0.5f, 0f..1f),
            TextureControl("Ribbon width", 0.5f, 0f..1f),
            TextureControl("Inlay width", 0.4f, 0f..1f),
            TextureControl("Weave depth", 0.55f, 0f..1f),
            TextureControl("Highlight width", 0.4f, 0f..1f)
        ),
        colors = listOf(
            0xFF0B2526.toInt(),
            0xFF287666.toInt(),
            0xFFC39140.toInt(),
            0xFFFFE4A1.toInt()
        )
    ) { seed, v, colors ->
        CelticKnotworkTextureGenerator().generate(
            768, 768,
            CelticKnotworkTextureParameters(
                seed, v[0], v[1], v[2], v[3], v[4], v[5],
                colors[0], colors[1], colors[2], colors[3]
            )
        )
    },
    coloredTextureDemo(
        name = "Seigaiha",
        controls = listOf(
            TextureControl("Scale", 0.014f, 0.001f..0.04f),
            TextureControl("Rotation", 0.5f, 0f..1f),
            TextureControl("Line width", 0.3f, 0f..1f),
            TextureControl("Rings", 0.5f, 0f..1f),
            TextureControl("Variation", 0.55f, 0f..1f),
            TextureControl("Roughness", 0.3f, 0f..1f)
        ),
        colors = listOf(
            0xFF092340.toInt(),
            0xFF1A658B.toInt(),
            0xFF42A6BA.toInt(),
            0xFFE6F3DA.toInt()
        )
    ) { seed, v, colors ->
        SeigaihaTextureGenerator().generate(
            768, 768,
            SeigaihaTextureParameters(
                seed, v[0], v[1], v[2], v[3], v[4], v[5],
                colors[0], colors[1], colors[2], colors[3]
            )
        )
    },
    coloredTextureDemo(
        name = "Asanoha",
        controls = listOf(
            TextureControl("Scale", 0.012f, 0.001f..0.04f),
            TextureControl("Rotation", 0f, 0f..1f),
            TextureControl("Line width", 0.3f, 0f..1f),
            TextureControl("Background", 0.4f, 0f..1f),
            TextureControl("Roughness", 0.25f, 0f..1f),
            TextureControl("Brightness", 0.2f, 0f..1f)
        ),
        colors = listOf(
            0xFF23182D.toInt(),
            0xFF5F3969.toInt(),
            0xFFCB8F55.toInt(),
            0xFFFFE7BD.toInt()
        )
    ) { seed, v, colors ->
        AsanohaTextureGenerator().generate(
            768, 768,
            AsanohaTextureParameters(
                seed, v[0], v[1], v[2], v[3], v[4], v[5],
                colors[0], colors[1], colors[2], colors[3]
            )
        )
    },
    coloredTextureDemo(
        name = "Houndstooth",
        controls = listOf(
            TextureControl("Scale", 0.008f, 0.001f..0.04f),
            TextureControl("Rotation", 0.5f, 0f..1f),
            TextureControl("Thread density", 0.5f, 0f..1f),
            TextureControl("Weave", 0.55f, 0f..1f),
            TextureControl("Fuzz", 0.3f, 0f..1f),
            TextureControl("Brightness", 0.25f, 0f..1f)
        ),
        colors = listOf(
            0xFF111923.toInt(),
            0xFF435467.toInt(),
            0xFFD9D3BF.toInt(),
            0xFFFFF7DC.toInt()
        )
    ) { seed, v, colors ->
        HoundstoothTextureGenerator().generate(
            768, 768,
            HoundstoothTextureParameters(
                seed, v[0], v[1], v[2], v[3], v[4], v[5],
                colors[0], colors[1], colors[2], colors[3]
            )
        )
    },
    coloredTextureDemo(
        name = "Jigsaw",
        controls = listOf(
            TextureControl("Scale", 0.01f, 0.001f..0.04f),
            TextureControl("Tab radius", 0.6f, 0f..1f),
            TextureControl("Irregularity", 0.5f, 0f..1f),
            TextureControl("Color variation", 0.6f, 0f..1f),
            TextureControl("Roughness", 0.4f, 0f..1f),
            TextureControl("Bevel", 0.5f, 0f..1f)
        ),
        colors = listOf(
            0xFF18243A.toInt(),
            0xFF4589A4.toInt(),
            0xFFE6AD72.toInt(),
            0xFFFFF0CD.toInt()
        )
    ) { seed, v, colors ->
        JigsawTextureGenerator().generate(
            768, 768,
            JigsawTextureParameters(
                seed, v[0], v[1], v[2], v[3], v[4], v[5],
                colors[0], colors[1], colors[2], colors[3]
            )
        )
    },
    coloredTextureDemo(
        name = "Tree bark",
        controls = listOf(
            TextureControl("Scale", 0.016f, 0.001f..0.04f),
            TextureControl("Distortion", 0.7f, 0f..1f),
            TextureControl("Furrow depth", 0.4f, 0f..1f),
            TextureControl("Detail", 0.65f, 0f..1f),
            TextureControl("Cracks", 0.5f, 0f..1f),
            TextureControl("Lichen", 0.35f, 0f..1f)
        ),
        colors = listOf(
            0xFF221B16.toInt(),
            0xFF796043.toInt(),
            0xFF647647.toInt(),
            0xFFC9B98B.toInt()
        )
    ) { seed, v, colors ->
        TreeBarkTextureGenerator().generate(
            768, 768,
            TreeBarkTextureParameters(
                seed, v[0], v[1], v[2], v[3], v[4], v[5],
                colors[0], colors[1], colors[2], colors[3]
            )
        )
    },
    coloredTextureDemo(
        name = "Pine cone",
        controls = listOf(
            TextureControl("Scale", 0.018f, 0.001f..0.04f),
            TextureControl("Scale length", 0.7f, 0f..1f),
            TextureControl("Relief", 0.7f, 0f..1f),
            TextureControl("Creases", 0.35f, 0f..1f),
            TextureControl("Variation", 0.45f, 0f..1f),
            TextureControl("Edge width", 0.4f, 0f..1f)
        ),
        colors = listOf(
            0xFF24150E.toInt(),
            0xFF865130.toInt(),
            0xFFB78547.toInt(),
            0xFFE8C994.toInt()
        )
    ) { seed, v, colors ->
        PineConeTextureGenerator().generate(
            768, 768,
            PineConeTextureParameters(
                seed, v[0], v[1], v[2], v[3], v[4], v[5],
                colors[0], colors[1], colors[2], colors[3]
            )
        )
    },
    coloredTextureDemo(
        name = "Zebra stripes",
        controls = listOf(
            TextureControl("Scale", 0.01f, 0.001f..0.04f),
            TextureControl("Direction", 0.5f, 0f..1f),
            TextureControl("Distortion", 0.55f, 0f..1f),
            TextureControl("Frequency", 0.4f, 0f..1f),
            TextureControl("Branching", 0.45f, 0f..1f),
            TextureControl("Balance", 0.5f, 0f..1f)
        ),
        colors = listOf(
            0xFF111721.toInt(),
            0xFF3F4851.toInt(),
            0xFFDBDCCB.toInt(),
            0xFFFFF9E7.toInt()
        )
    ) { seed, v, colors ->
        ZebraStripesTextureGenerator().generate(
            768, 768,
            ZebraStripesTextureParameters(
                seed, v[0], v[1], v[2], v[3], v[4], v[5],
                colors[0], colors[1], colors[2], colors[3]
            )
        )
    },
    coloredTextureDemo(
        name = "Leopard spots",
        controls = listOf(
            TextureControl("Scale", 0.013f, 0.001f..0.04f),
            TextureControl("Jitter", 0.7f, 0f..1f),
            TextureControl("Spot radius", 0.65f, 0f..1f),
            TextureControl("Irregularity", 0.4f, 0f..1f),
            TextureControl("Broken rings", 0.45f, 0f..1f),
            TextureControl("Fur", 0.5f, 0f..1f)
        ),
        colors = listOf(
            0xFF211A16.toInt(),
            0xFFC99344.toInt(),
            0xFF8E5528.toInt(),
            0xFFF4DB93.toInt()
        )
    ) { seed, v, colors ->
        LeopardSpotsTextureGenerator().generate(
            768, 768,
            LeopardSpotsTextureParameters(
                seed, v[0], v[1], v[2], v[3], v[4], v[5],
                colors[0], colors[1], colors[2], colors[3]
            )
        )
    },
    coloredTextureDemo(
        name = "Lissajous ribbons",
        controls = listOf(
            TextureControl("Scale", 0.012f, 0.001f..0.04f),
            TextureControl("Horizontal frequency", 3f, 1f..9f, steps = 7),
            TextureControl("Vertical frequency", 4f, 1f..9f, steps = 7),
            TextureControl("Phase", 0.12f, 0f..1f),
            TextureControl("Ribbon width", 0.45f, 0f..1f),
            TextureControl("Glow", 0.65f, 0f..1f)
        ),
        colors = listOf(
            0xFF080E21.toInt(),
            0xFF1CC6D0.toInt(),
            0xFFEC589B.toInt(),
            0xFFFFE594.toInt()
        )
    ) { seed, v, colors ->
        LissajousRibbonsTextureGenerator().generate(
            768, 768,
            LissajousRibbonsTextureParameters(
                seed, v[0], v[1], v[2], v[3], v[4], v[5],
                colors[0], colors[1], colors[2], colors[3]
            )
        )
    },
    coloredTextureDemo(
        name = "Bismuth crystal",
        controls = listOf(
            TextureControl("Scale", 0.011f, 0.001f..0.04f),
            TextureControl("Direction", 0.5f, 0f..1f),
            TextureControl("Irregularity", 0.55f, 0f..1f),
            TextureControl("Steps", 0.6f, 0f..1f),
            TextureControl("Iridescence", 0.75f, 0f..1f),
            TextureControl("Relief", 0.65f, 0f..1f)
        ),
        colors = listOf(
            0xFF211329.toInt(),
            0xFF5B54B7.toInt(),
            0xFF23B8B8.toInt(),
            0xFFFFCD70.toInt()
        )
    ) { seed, v, colors ->
        BismuthCrystalTextureGenerator().generate(
            768, 768,
            BismuthCrystalTextureParameters(
                seed, v[0], v[1], v[2], v[3], v[4], v[5],
                colors[0], colors[1], colors[2], colors[3]
            )
        )
    },
    coloredTextureDemo(
        name = "Hilbert ribbons",
        controls = listOf(
            TextureControl("Scale", 0.01f, 0.001f..0.04f),
            TextureControl("Order", 4f, 2f..7f, steps = 4),
            TextureControl("Ribbon width", 0.65f, 0f..1f),
            TextureControl("Glow", 0.6f, 0f..1f),
            TextureControl("Quarter turns", 0f, 0f..3f, steps = 2),
            TextureControl("Highlight", 0.7f, 0f..1f)
        ),
        colors = listOf(
            0xFF0C1429.toInt(),
            0xFF278BD2.toInt(),
            0xFFD747AB.toInt(),
            0xFFFFD878.toInt()
        )
    ) { seed, v, colors ->
        HilbertRibbonsTextureGenerator().generate(
            768, 768,
            HilbertRibbonsTextureParameters(
                seed, v[0], v[1], v[2], v[3], v[4], v[5],
                colors[0], colors[1], colors[2], colors[3]
            )
        )
    }
)

private fun coloredTextureDemo(
    name: String,
    controls: List<TextureControl>,
    colors: List<Int>,
    colorNames: List<String> = listOf("Background", "Primary", "Secondary", "Highlight"),
    generate: (Int, List<Float>, List<Int>) -> Bitmap?
) = TextureDemo(
    name = name,
    controls = controls,
    colors = colors,
    colorNames = colorNames,
    generateColored = generate,
    generate = { seed, values -> generate(seed, values, colors) }
)
