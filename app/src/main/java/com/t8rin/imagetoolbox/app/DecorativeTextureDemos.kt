package com.t8rin.imagetoolbox.app

import com.t8rin.fast_noise.texture.ChromaticPlaidTextureGenerator
import com.t8rin.fast_noise.texture.ChromaticPlaidTextureParameters
import com.t8rin.fast_noise.texture.MalachiteTextureGenerator
import com.t8rin.fast_noise.texture.MalachiteTextureParameters
import com.t8rin.fast_noise.texture.LabradoriteTextureGenerator
import com.t8rin.fast_noise.texture.LabradoriteTextureParameters
import com.t8rin.fast_noise.texture.SeraphiniteTextureGenerator
import com.t8rin.fast_noise.texture.SeraphiniteTextureParameters
import com.t8rin.fast_noise.texture.PorcelainCrackleTextureGenerator
import com.t8rin.fast_noise.texture.PorcelainCrackleTextureParameters
import com.t8rin.fast_noise.texture.RakuGlazeTextureGenerator
import com.t8rin.fast_noise.texture.RakuGlazeTextureParameters
import com.t8rin.fast_noise.texture.MotherOfPearlTextureGenerator
import com.t8rin.fast_noise.texture.MotherOfPearlTextureParameters
import com.t8rin.fast_noise.texture.TemperedTitaniumTextureGenerator
import com.t8rin.fast_noise.texture.TemperedTitaniumTextureParameters
import com.t8rin.fast_noise.texture.EndGrainMosaicTextureGenerator
import com.t8rin.fast_noise.texture.EndGrainMosaicTextureParameters
import com.t8rin.fast_noise.texture.CorkTextureGenerator
import com.t8rin.fast_noise.texture.CorkTextureParameters
import com.t8rin.fast_noise.texture.CutCrystalGlassTextureGenerator
import com.t8rin.fast_noise.texture.CutCrystalGlassTextureParameters
import com.t8rin.fast_noise.texture.SashikoTextureGenerator
import com.t8rin.fast_noise.texture.SashikoTextureParameters
import com.t8rin.fast_noise.texture.BargelloTextureGenerator
import com.t8rin.fast_noise.texture.BargelloTextureParameters
import com.t8rin.fast_noise.texture.KilimTextureGenerator
import com.t8rin.fast_noise.texture.KilimTextureParameters
import com.t8rin.fast_noise.texture.BrocadeTextureGenerator
import com.t8rin.fast_noise.texture.BrocadeTextureParameters
import com.t8rin.fast_noise.texture.BatikTextureGenerator
import com.t8rin.fast_noise.texture.BatikTextureParameters
import com.t8rin.fast_noise.texture.IkatTextureGenerator
import com.t8rin.fast_noise.texture.IkatTextureParameters
import com.t8rin.fast_noise.texture.EmbossedPaisleyTextureGenerator
import com.t8rin.fast_noise.texture.EmbossedPaisleyTextureParameters
import com.t8rin.fast_noise.texture.ArtDecoFansTextureGenerator
import com.t8rin.fast_noise.texture.ArtDecoFansTextureParameters
import com.t8rin.fast_noise.texture.CloverLatticeTextureGenerator
import com.t8rin.fast_noise.texture.CloverLatticeTextureParameters
import com.t8rin.fast_noise.texture.GuillocheRosettesTextureGenerator
import com.t8rin.fast_noise.texture.GuillocheRosettesTextureParameters
import com.t8rin.fast_noise.texture.GinkgoLeavesTextureGenerator
import com.t8rin.fast_noise.texture.GinkgoLeavesTextureParameters
import com.t8rin.fast_noise.texture.MonsteraCanopyTextureGenerator
import com.t8rin.fast_noise.texture.MonsteraCanopyTextureParameters
import com.t8rin.fast_noise.texture.LotusPondTextureGenerator
import com.t8rin.fast_noise.texture.LotusPondTextureParameters
import com.t8rin.fast_noise.texture.SeaAnemoneTextureGenerator
import com.t8rin.fast_noise.texture.SeaAnemoneTextureParameters
import com.t8rin.fast_noise.texture.JellyfishTextureGenerator
import com.t8rin.fast_noise.texture.JellyfishTextureParameters
import com.t8rin.fast_noise.texture.ButterflyWingsTextureGenerator
import com.t8rin.fast_noise.texture.ButterflyWingsTextureParameters
import com.t8rin.fast_noise.texture.FungalGillsTextureGenerator
import com.t8rin.fast_noise.texture.FungalGillsTextureParameters
import com.t8rin.fast_noise.texture.RadiolariaTextureGenerator
import com.t8rin.fast_noise.texture.RadiolariaTextureParameters
import com.t8rin.fast_noise.texture.SlotCanyonTextureGenerator
import com.t8rin.fast_noise.texture.SlotCanyonTextureParameters
import com.t8rin.fast_noise.texture.GlacialCrevassesTextureGenerator
import com.t8rin.fast_noise.texture.GlacialCrevassesTextureParameters
import com.t8rin.fast_noise.texture.SpiralPearlsTextureGenerator
import com.t8rin.fast_noise.texture.SpiralPearlsTextureParameters
import com.t8rin.fast_noise.texture.HopfFibrationTextureGenerator
import com.t8rin.fast_noise.texture.HopfFibrationTextureParameters
import com.t8rin.fast_noise.texture.SpirographTextureGenerator
import com.t8rin.fast_noise.texture.SpirographTextureParameters
import com.t8rin.fast_noise.texture.ChiralPinwheelsTextureGenerator
import com.t8rin.fast_noise.texture.ChiralPinwheelsTextureParameters
import com.t8rin.fast_noise.texture.TruchetBlocksTextureGenerator
import com.t8rin.fast_noise.texture.TruchetBlocksTextureParameters
import com.t8rin.fast_noise.texture.ImpossibleStairsTextureGenerator
import com.t8rin.fast_noise.texture.ImpossibleStairsTextureParameters
import com.t8rin.fast_noise.texture.WovenHexagonsTextureGenerator
import com.t8rin.fast_noise.texture.WovenHexagonsTextureParameters
import com.t8rin.fast_noise.texture.GyroidTextureGenerator
import com.t8rin.fast_noise.texture.GyroidTextureParameters
import com.t8rin.fast_noise.texture.HypercubeTextureGenerator
import com.t8rin.fast_noise.texture.HypercubeTextureParameters
import com.t8rin.fast_noise.texture.LenticularWavesTextureGenerator
import com.t8rin.fast_noise.texture.LenticularWavesTextureParameters
import com.t8rin.fast_noise.texture.StellarNurseryTextureGenerator
import com.t8rin.fast_noise.texture.StellarNurseryTextureParameters
import com.t8rin.fast_noise.texture.PulsarJetsTextureGenerator
import com.t8rin.fast_noise.texture.PulsarJetsTextureParameters
import com.t8rin.fast_noise.texture.GravitationalLensingTextureGenerator
import com.t8rin.fast_noise.texture.GravitationalLensingTextureParameters
import com.t8rin.fast_noise.texture.NacreCloudsTextureGenerator
import com.t8rin.fast_noise.texture.NacreCloudsTextureParameters
import com.t8rin.fast_noise.texture.MercuryDropletsTextureGenerator
import com.t8rin.fast_noise.texture.MercuryDropletsTextureParameters
import com.t8rin.fast_noise.texture.SonicBoomTextureGenerator
import com.t8rin.fast_noise.texture.SonicBoomTextureParameters
import com.t8rin.fast_noise.texture.RibbonVortexTextureGenerator
import com.t8rin.fast_noise.texture.RibbonVortexTextureParameters
import com.t8rin.fast_noise.texture.IridescentFoamTextureGenerator
import com.t8rin.fast_noise.texture.IridescentFoamTextureParameters
import com.t8rin.fast_noise.texture.LavaCrustTextureGenerator
import com.t8rin.fast_noise.texture.LavaCrustTextureParameters
import com.t8rin.fast_noise.texture.AbyssalVentTextureGenerator
import com.t8rin.fast_noise.texture.AbyssalVentTextureParameters

internal val decorativeTextureDemos = listOf(
    coloredTextureDemo(
        name = "Chromatic plaid",
        controls = listOf(
            TextureControl("Scale", 0.008f, 0.001f..0.04f),
            TextureControl("Stripe width", 0.45f, 0f..1f),
            TextureControl("Thread density", 0.5f, 0f..1f),
            TextureControl("Weave depth", 0.6f, 0f..1f),
            TextureControl("Rotation", 0.5f, 0f..1f),
            TextureControl("Contrast", 1.0f, 0.2f..2.5f)
        ),
        colors = listOf(
            0xFFFFFF00.toInt(),
            0xFFFF0000.toInt(),
            0xFFFF00FF.toInt(),
            0xFF00FFFF.toInt()
        )
    ) { seed, v, colors ->
        ChromaticPlaidTextureGenerator().generate(
            768, 768,
            ChromaticPlaidTextureParameters(
                seed, v[0], v[1], v[2], v[3], v[4], v[5],
                colors[0], colors[1], colors[2], colors[3]
            )
        )
    },
    coloredTextureDemo(
        name = "Malachite",
        controls = listOf(
            TextureControl("Scale", 0.008f, 0.001f..0.04f),
            TextureControl("Band density", 0.55f, 0f..1f),
            TextureControl("Warping", 0.48f, 0f..1f),
            TextureControl("Relief", 0.55f, 0f..1f),
            TextureControl("Polish", 0.65f, 0f..1f),
            TextureControl("Contrast", 1.05f, 0.2f..2.5f)
        ),
        colors = listOf(
            0xFF071F18.toInt(),
            0xFF17613E.toInt(),
            0xFF56AE70.toInt(),
            0xFFD5F0B4.toInt()
        )
    ) { seed, v, colors ->
        MalachiteTextureGenerator().generate(
            768, 768,
            MalachiteTextureParameters(
                seed, v[0], v[1], v[2], v[3], v[4], v[5],
                colors[0], colors[1], colors[2], colors[3]
            )
        )
    },
    coloredTextureDemo(
        name = "Labradorite",
        controls = listOf(
            TextureControl("Scale", 0.008f, 0.001f..0.04f),
            TextureControl("Facet density", 0.55f, 0f..1f),
            TextureControl("Cleavage", 0.48f, 0f..1f),
            TextureControl("Relief", 0.55f, 0f..1f),
            TextureControl("Flash", 0.65f, 0f..1f),
            TextureControl("Contrast", 1.05f, 0.2f..2.5f)
        ),
        colors = listOf(
            0xFF101B27.toInt(),
            0xFF273E54.toInt(),
            0xFF338BBA.toInt(),
            0xFFEDD188.toInt()
        )
    ) { seed, v, colors ->
        LabradoriteTextureGenerator().generate(
            768, 768,
            LabradoriteTextureParameters(
                seed, v[0], v[1], v[2], v[3], v[4], v[5],
                colors[0], colors[1], colors[2], colors[3]
            )
        )
    },
    coloredTextureDemo(
        name = "Seraphinite",
        controls = listOf(
            TextureControl("Scale", 0.008f, 0.001f..0.04f),
            TextureControl("Plume density", 0.55f, 0f..1f),
            TextureControl("Curvature", 0.48f, 0f..1f),
            TextureControl("Fibers", 0.55f, 0f..1f),
            TextureControl("Sheen", 0.65f, 0f..1f),
            TextureControl("Contrast", 1.05f, 0.2f..2.5f)
        ),
        colors = listOf(
            0xFF071E1B.toInt(),
            0xFF245541.toInt(),
            0xFF9EBEAB.toInt(),
            0xFFE7F2D4.toInt()
        )
    ) { seed, v, colors ->
        SeraphiniteTextureGenerator().generate(
            768, 768,
            SeraphiniteTextureParameters(
                seed, v[0], v[1], v[2], v[3], v[4], v[5],
                colors[0], colors[1], colors[2], colors[3]
            )
        )
    },
    coloredTextureDemo(
        name = "Porcelain crackle",
        controls = listOf(
            TextureControl("Scale", 0.008f, 0.001f..0.04f),
            TextureControl("Crack density", 0.55f, 0f..1f),
            TextureControl("Irregularity", 0.48f, 0f..1f),
            TextureControl("Crack width", 0.55f, 0f..1f),
            TextureControl("Glaze", 0.65f, 0f..1f),
            TextureControl("Contrast", 1.05f, 0.2f..2.5f)
        ),
        colors = listOf(
            0xFF1D4157.toInt(),
            0xFFD8E4DE.toInt(),
            0xFF9FBFCA.toInt(),
            0xFFFFF6DD.toInt()
        )
    ) { seed, v, colors ->
        PorcelainCrackleTextureGenerator().generate(
            768, 768,
            PorcelainCrackleTextureParameters(
                seed, v[0], v[1], v[2], v[3], v[4], v[5],
                colors[0], colors[1], colors[2], colors[3]
            )
        )
    },
    coloredTextureDemo(
        name = "Raku glaze",
        controls = listOf(
            TextureControl("Scale", 0.008f, 0.001f..0.04f),
            TextureControl("Island density", 0.55f, 0f..1f),
            TextureControl("Distortion", 0.48f, 0f..1f),
            TextureControl("Crackle", 0.55f, 0f..1f),
            TextureControl("Iridescence", 0.65f, 0f..1f),
            TextureControl("Contrast", 1.05f, 0.2f..2.5f)
        ),
        colors = listOf(
            0xFF171916.toInt(),
            0xFF92553E.toInt(),
            0xFF508D8A.toInt(),
            0xFFEAD19C.toInt()
        )
    ) { seed, v, colors ->
        RakuGlazeTextureGenerator().generate(
            768, 768,
            RakuGlazeTextureParameters(
                seed, v[0], v[1], v[2], v[3], v[4], v[5],
                colors[0], colors[1], colors[2], colors[3]
            )
        )
    },
    coloredTextureDemo(
        name = "Mother of pearl",
        controls = listOf(
            TextureControl("Scale", 0.008f, 0.001f..0.04f),
            TextureControl("Plate density", 0.55f, 0f..1f),
            TextureControl("Distortion", 0.48f, 0f..1f),
            TextureControl("Growth lines", 0.55f, 0f..1f),
            TextureControl("Iridescence", 0.65f, 0f..1f),
            TextureControl("Contrast", 1.05f, 0.2f..2.5f)
        ),
        colors = listOf(
            0xFF172F3B.toInt(),
            0xFF95CBCB.toInt(),
            0xFFD7AECC.toInt(),
            0xFFFFF4D8.toInt()
        )
    ) { seed, v, colors ->
        MotherOfPearlTextureGenerator().generate(
            768, 768,
            MotherOfPearlTextureParameters(
                seed, v[0], v[1], v[2], v[3], v[4], v[5],
                colors[0], colors[1], colors[2], colors[3]
            )
        )
    },
    coloredTextureDemo(
        name = "Tempered titanium",
        controls = listOf(
            TextureControl("Scale", 0.008f, 0.001f..0.04f),
            TextureControl("Patch density", 0.55f, 0f..1f),
            TextureControl("Warping", 0.48f, 0f..1f),
            TextureControl("Brushing", 0.55f, 0f..1f),
            TextureControl("Polish", 0.65f, 0f..1f),
            TextureControl("Contrast", 1.05f, 0.2f..2.5f)
        ),
        colors = listOf(
            0xFF10202D.toInt(),
            0xFF6C66AD.toInt(),
            0xFFB98839.toInt(),
            0xFFD5F5EF.toInt()
        )
    ) { seed, v, colors ->
        TemperedTitaniumTextureGenerator().generate(
            768, 768,
            TemperedTitaniumTextureParameters(
                seed, v[0], v[1], v[2], v[3], v[4], v[5],
                colors[0], colors[1], colors[2], colors[3]
            )
        )
    },
    coloredTextureDemo(
        name = "End grain mosaic",
        controls = listOf(
            TextureControl("Scale", 0.008f, 0.001f..0.04f),
            TextureControl("Block density", 0.55f, 0f..1f),
            TextureControl("Ring density", 0.48f, 0f..1f),
            TextureControl("Irregularity", 0.55f, 0f..1f),
            TextureControl("Polish", 0.65f, 0f..1f),
            TextureControl("Contrast", 1.05f, 0.2f..2.5f)
        ),
        colors = listOf(
            0xFF25160F.toInt(),
            0xFF81512A.toInt(),
            0xFFB88A51.toInt(),
            0xFFEAD7A1.toInt()
        )
    ) { seed, v, colors ->
        EndGrainMosaicTextureGenerator().generate(
            768, 768,
            EndGrainMosaicTextureParameters(
                seed, v[0], v[1], v[2], v[3], v[4], v[5],
                colors[0], colors[1], colors[2], colors[3]
            )
        )
    },
    coloredTextureDemo(
        name = "Cork",
        controls = listOf(
            TextureControl("Scale", 0.008f, 0.001f..0.04f),
            TextureControl("Chip density", 0.55f, 0f..1f),
            TextureControl("Irregularity", 0.48f, 0f..1f),
            TextureControl("Pores", 0.55f, 0f..1f),
            TextureControl("Relief", 0.65f, 0f..1f),
            TextureControl("Contrast", 1.05f, 0.2f..2.5f)
        ),
        colors = listOf(
            0xFF322114.toInt(),
            0xFF967448.toInt(),
            0xFFC6A274.toInt(),
            0xFFEDD4AA.toInt()
        )
    ) { seed, v, colors ->
        CorkTextureGenerator().generate(
            768, 768,
            CorkTextureParameters(
                seed, v[0], v[1], v[2], v[3], v[4], v[5],
                colors[0], colors[1], colors[2], colors[3]
            )
        )
    },
    coloredTextureDemo(
        name = "Cut crystal glass",
        controls = listOf(
            TextureControl("Scale", 0.008f, 0.001f..0.04f),
            TextureControl("Cut density", 0.55f, 0f..1f),
            TextureControl("Facets", 0.48f, 0f..1f),
            TextureControl("Relief", 0.55f, 0f..1f),
            TextureControl("Sparkle", 0.65f, 0f..1f),
            TextureControl("Contrast", 1.05f, 0.2f..2.5f)
        ),
        colors = listOf(
            0xFF0A1D2D.toInt(),
            0xFF3D7792.toInt(),
            0xFFA6CED6.toInt(),
            0xFFF7F5DA.toInt()
        )
    ) { seed, v, colors ->
        CutCrystalGlassTextureGenerator().generate(
            768, 768,
            CutCrystalGlassTextureParameters(
                seed, v[0], v[1], v[2], v[3], v[4], v[5],
                colors[0], colors[1], colors[2], colors[3]
            )
        )
    },
    coloredTextureDemo(
        name = "Sashiko",
        controls = listOf(
            TextureControl("Scale", 0.008f, 0.001f..0.04f),
            TextureControl("Stitch density", 0.55f, 0f..1f),
            TextureControl("Stitch length", 0.48f, 0f..1f),
            TextureControl("Thread width", 0.55f, 0f..1f),
            TextureControl("Relief", 0.65f, 0f..1f),
            TextureControl("Contrast", 1.05f, 0.2f..2.5f)
        ),
        colors = listOf(
            0xFF0B1C37.toInt(),
            0xFF244768.toInt(),
            0xFFC6D5D7.toInt(),
            0xFFF2DBB0.toInt()
        )
    ) { seed, v, colors ->
        SashikoTextureGenerator().generate(
            768, 768,
            SashikoTextureParameters(
                seed, v[0], v[1], v[2], v[3], v[4], v[5],
                colors[0], colors[1], colors[2], colors[3]
            )
        )
    },
    coloredTextureDemo(
        name = "Bargello",
        controls = listOf(
            TextureControl("Scale", 0.008f, 0.001f..0.04f),
            TextureControl("Flame count", 0.55f, 0f..1f),
            TextureControl("Flame height", 0.48f, 0f..1f),
            TextureControl("Stitch width", 0.55f, 0f..1f),
            TextureControl("Relief", 0.65f, 0f..1f),
            TextureControl("Contrast", 1.05f, 0.2f..2.5f)
        ),
        colors = listOf(
            0xFF25213A.toInt(),
            0xFFCE6473.toInt(),
            0xFF3A8C9A.toInt(),
            0xFFF2D998.toInt()
        )
    ) { seed, v, colors ->
        BargelloTextureGenerator().generate(
            768, 768,
            BargelloTextureParameters(
                seed, v[0], v[1], v[2], v[3], v[4], v[5],
                colors[0], colors[1], colors[2], colors[3]
            )
        )
    },
    coloredTextureDemo(
        name = "Kilim",
        controls = listOf(
            TextureControl("Scale", 0.008f, 0.001f..0.04f),
            TextureControl("Diamond density", 0.55f, 0f..1f),
            TextureControl("Stepping", 0.48f, 0f..1f),
            TextureControl("Border width", 0.55f, 0f..1f),
            TextureControl("Weave", 0.65f, 0f..1f),
            TextureControl("Contrast", 1.05f, 0.2f..2.5f)
        ),
        colors = listOf(
            0xFF281D26.toInt(),
            0xFFB94B39.toInt(),
            0xFF2C7477.toInt(),
            0xFFEDD3A2.toInt()
        )
    ) { seed, v, colors ->
        KilimTextureGenerator().generate(
            768, 768,
            KilimTextureParameters(
                seed, v[0], v[1], v[2], v[3], v[4], v[5],
                colors[0], colors[1], colors[2], colors[3]
            )
        )
    },
    coloredTextureDemo(
        name = "Brocade",
        controls = listOf(
            TextureControl("Scale", 0.008f, 0.001f..0.04f),
            TextureControl("Ornament density", 0.55f, 0f..1f),
            TextureControl("Curvature", 0.48f, 0f..1f),
            TextureControl("Thread width", 0.55f, 0f..1f),
            TextureControl("Metallic", 0.65f, 0f..1f),
            TextureControl("Contrast", 1.05f, 0.2f..2.5f)
        ),
        colors = listOf(
            0xFF271425.toInt(),
            0xFF74404C.toInt(),
            0xFFCAA45F.toInt(),
            0xFFF8DE99.toInt()
        )
    ) { seed, v, colors ->
        BrocadeTextureGenerator().generate(
            768, 768,
            BrocadeTextureParameters(
                seed, v[0], v[1], v[2], v[3], v[4], v[5],
                colors[0], colors[1], colors[2], colors[3]
            )
        )
    },
    coloredTextureDemo(
        name = "Batik",
        controls = listOf(
            TextureControl("Scale", 0.008f, 0.001f..0.04f),
            TextureControl("Flower density", 0.55f, 0f..1f),
            TextureControl("Petals", 0.48f, 0f..1f),
            TextureControl("Wax width", 0.55f, 0f..1f),
            TextureControl("Distortion", 0.65f, 0f..1f),
            TextureControl("Contrast", 1.05f, 0.2f..2.5f)
        ),
        colors = listOf(
            0xFF102A3B.toInt(),
            0xFF397D85.toInt(),
            0xFF9EB68B.toInt(),
            0xFFF4D69E.toInt()
        )
    ) { seed, v, colors ->
        BatikTextureGenerator().generate(
            768, 768,
            BatikTextureParameters(
                seed, v[0], v[1], v[2], v[3], v[4], v[5],
                colors[0], colors[1], colors[2], colors[3]
            )
        )
    },
    coloredTextureDemo(
        name = "Ikat",
        controls = listOf(
            TextureControl("Scale", 0.008f, 0.001f..0.04f),
            TextureControl("Motif density", 0.55f, 0f..1f),
            TextureControl("Chevron height", 0.48f, 0f..1f),
            TextureControl("Feathering", 0.55f, 0f..1f),
            TextureControl("Weave", 0.65f, 0f..1f),
            TextureControl("Contrast", 1.05f, 0.2f..2.5f)
        ),
        colors = listOf(
            0xFF1C2641.toInt(),
            0xFF546A9E.toInt(),
            0xFFCC7D68.toInt(),
            0xFFF1DAB7.toInt()
        )
    ) { seed, v, colors ->
        IkatTextureGenerator().generate(
            768, 768,
            IkatTextureParameters(
                seed, v[0], v[1], v[2], v[3], v[4], v[5],
                colors[0], colors[1], colors[2], colors[3]
            )
        )
    },
    coloredTextureDemo(
        name = "Embossed paisley",
        controls = listOf(
            TextureControl("Scale", 0.008f, 0.001f..0.04f),
            TextureControl("Motif density", 0.55f, 0f..1f),
            TextureControl("Curl", 0.48f, 0f..1f),
            TextureControl("Line width", 0.55f, 0f..1f),
            TextureControl("Relief", 0.65f, 0f..1f),
            TextureControl("Contrast", 1.05f, 0.2f..2.5f)
        ),
        colors = listOf(
            0xFF201A30.toInt(),
            0xFF73506F.toInt(),
            0xFFC29863.toInt(),
            0xFFF2D5A1.toInt()
        )
    ) { seed, v, colors ->
        EmbossedPaisleyTextureGenerator().generate(
            768, 768,
            EmbossedPaisleyTextureParameters(
                seed, v[0], v[1], v[2], v[3], v[4], v[5],
                colors[0], colors[1], colors[2], colors[3]
            )
        )
    },
    coloredTextureDemo(
        name = "Art deco fans",
        controls = listOf(
            TextureControl("Scale", 0.008f, 0.001f..0.04f),
            TextureControl("Fan density", 0.55f, 0f..1f),
            TextureControl("Ribs", 0.48f, 0f..1f),
            TextureControl("Line width", 0.55f, 0f..1f),
            TextureControl("Metallic", 0.65f, 0f..1f),
            TextureControl("Contrast", 1.05f, 0.2f..2.5f)
        ),
        colors = listOf(
            0xFF151F2A.toInt(),
            0xFF37515A.toInt(),
            0xFFAE8551.toInt(),
            0xFFF1D7A1.toInt()
        )
    ) { seed, v, colors ->
        ArtDecoFansTextureGenerator().generate(
            768, 768,
            ArtDecoFansTextureParameters(
                seed, v[0], v[1], v[2], v[3], v[4], v[5],
                colors[0], colors[1], colors[2], colors[3]
            )
        )
    },
    coloredTextureDemo(
        name = "Clover lattice",
        controls = listOf(
            TextureControl("Scale", 0.008f, 0.001f..0.04f),
            TextureControl("Tile density", 0.55f, 0f..1f),
            TextureControl("Curvature", 0.48f, 0f..1f),
            TextureControl("Grout width", 0.55f, 0f..1f),
            TextureControl("Glaze", 0.65f, 0f..1f),
            TextureControl("Contrast", 1.05f, 0.2f..2.5f)
        ),
        colors = listOf(
            0xFFE8DDC9.toInt(),
            0xFF386C79.toInt(),
            0xFF9E656A.toInt(),
            0xFFE7B96B.toInt()
        )
    ) { seed, v, colors ->
        CloverLatticeTextureGenerator().generate(
            768, 768,
            CloverLatticeTextureParameters(
                seed, v[0], v[1], v[2], v[3], v[4], v[5],
                colors[0], colors[1], colors[2], colors[3]
            )
        )
    },
    coloredTextureDemo(
        name = "Guilloche rosettes",
        controls = listOf(
            TextureControl("Scale", 0.008f, 0.001f..0.04f),
            TextureControl("Rosette density", 0.55f, 0f..1f),
            TextureControl("Petal count", 0.48f, 0f..1f),
            TextureControl("Line width", 0.55f, 0f..1f),
            TextureControl("Layers", 0.65f, 0f..1f),
            TextureControl("Contrast", 1.05f, 0.2f..2.5f)
        ),
        colors = listOf(
            0xFF101D38.toInt(),
            0xFF258E9B.toInt(),
            0xFFCC739F.toInt(),
            0xFFF2DAA5.toInt()
        )
    ) { seed, v, colors ->
        GuillocheRosettesTextureGenerator().generate(
            768, 768,
            GuillocheRosettesTextureParameters(
                seed, v[0], v[1], v[2], v[3], v[4], v[5],
                colors[0], colors[1], colors[2], colors[3]
            )
        )
    },
    coloredTextureDemo(
        name = "Ginkgo leaves",
        controls = listOf(
            TextureControl("Scale", 0.008f, 0.001f..0.04f),
            TextureControl("Leaf density", 0.55f, 0f..1f),
            TextureControl("Fan width", 0.48f, 0f..1f),
            TextureControl("Veins", 0.55f, 0f..1f),
            TextureControl("Light", 0.65f, 0f..1f),
            TextureControl("Contrast", 1.05f, 0.2f..2.5f)
        ),
        colors = listOf(
            0xFF17292D.toInt(),
            0xFF6B7350.toInt(),
            0xFFCAA44D.toInt(),
            0xFFF4DD91.toInt()
        )
    ) { seed, v, colors ->
        GinkgoLeavesTextureGenerator().generate(
            768, 768,
            GinkgoLeavesTextureParameters(
                seed, v[0], v[1], v[2], v[3], v[4], v[5],
                colors[0], colors[1], colors[2], colors[3]
            )
        )
    },
    coloredTextureDemo(
        name = "Monstera canopy",
        controls = listOf(
            TextureControl("Scale", 0.008f, 0.001f..0.04f),
            TextureControl("Leaf density", 0.55f, 0f..1f),
            TextureControl("Lobes", 0.48f, 0f..1f),
            TextureControl("Fenestration", 0.55f, 0f..1f),
            TextureControl("Light", 0.65f, 0f..1f),
            TextureControl("Contrast", 1.05f, 0.2f..2.5f)
        ),
        colors = listOf(
            0xFF0A241F.toInt(),
            0xFF1C5741.toInt(),
            0xFF6C9B61.toInt(),
            0xFFB8CE8F.toInt()
        )
    ) { seed, v, colors ->
        MonsteraCanopyTextureGenerator().generate(
            768, 768,
            MonsteraCanopyTextureParameters(
                seed, v[0], v[1], v[2], v[3], v[4], v[5],
                colors[0], colors[1], colors[2], colors[3]
            )
        )
    },
    coloredTextureDemo(
        name = "Lotus pond",
        controls = listOf(
            TextureControl("Scale", 0.008f, 0.001f..0.04f),
            TextureControl("Pad density", 0.55f, 0f..1f),
            TextureControl("Pad size", 0.48f, 0f..1f),
            TextureControl("Ripples", 0.55f, 0f..1f),
            TextureControl("Blossoms", 0.65f, 0f..1f),
            TextureControl("Contrast", 1.05f, 0.2f..2.5f)
        ),
        colors = listOf(
            0xFF082B36.toInt(),
            0xFF337765.toInt(),
            0xFFC18C9D.toInt(),
            0xFFE7DDAC.toInt()
        )
    ) { seed, v, colors ->
        LotusPondTextureGenerator().generate(
            768, 768,
            LotusPondTextureParameters(
                seed, v[0], v[1], v[2], v[3], v[4], v[5],
                colors[0], colors[1], colors[2], colors[3]
            )
        )
    },
    coloredTextureDemo(
        name = "Sea anemone",
        controls = listOf(
            TextureControl("Scale", 0.008f, 0.001f..0.04f),
            TextureControl("Tentacles", 0.55f, 0f..1f),
            TextureControl("Curl", 0.48f, 0f..1f),
            TextureControl("Thickness", 0.55f, 0f..1f),
            TextureControl("Glow", 0.65f, 0f..1f),
            TextureControl("Contrast", 1.05f, 0.2f..2.5f)
        ),
        colors = listOf(
            0xFF071B30.toInt(),
            0xFF286E83.toInt(),
            0xFFCE7DAD.toInt(),
            0xFFF2DAB6.toInt()
        )
    ) { seed, v, colors ->
        SeaAnemoneTextureGenerator().generate(
            768, 768,
            SeaAnemoneTextureParameters(
                seed, v[0], v[1], v[2], v[3], v[4], v[5],
                colors[0], colors[1], colors[2], colors[3]
            )
        )
    },
    coloredTextureDemo(
        name = "Jellyfish",
        controls = listOf(
            TextureControl("Scale", 0.008f, 0.001f..0.04f),
            TextureControl("Tentacles", 0.55f, 0f..1f),
            TextureControl("Bell shape", 0.48f, 0f..1f),
            TextureControl("Thickness", 0.55f, 0f..1f),
            TextureControl("Glow", 0.65f, 0f..1f),
            TextureControl("Contrast", 1.05f, 0.2f..2.5f)
        ),
        colors = listOf(
            0xFF051C32.toInt(),
            0xFF2D7390.toInt(),
            0xFFA98AD6.toInt(),
            0xFFDDFAEC.toInt()
        )
    ) { seed, v, colors ->
        JellyfishTextureGenerator().generate(
            768, 768,
            JellyfishTextureParameters(
                seed, v[0], v[1], v[2], v[3], v[4], v[5],
                colors[0], colors[1], colors[2], colors[3]
            )
        )
    },
    coloredTextureDemo(
        name = "Butterfly wings",
        controls = listOf(
            TextureControl("Scale", 0.008f, 0.001f..0.04f),
            TextureControl("Vein density", 0.55f, 0f..1f),
            TextureControl("Wing shape", 0.48f, 0f..1f),
            TextureControl("Vein width", 0.55f, 0f..1f),
            TextureControl("Iridescence", 0.65f, 0f..1f),
            TextureControl("Contrast", 1.05f, 0.2f..2.5f)
        ),
        colors = listOf(
            0xFF1B1B27.toInt(),
            0xFFBB622C.toInt(),
            0xFF477E9B.toInt(),
            0xFFF3D69B.toInt()
        )
    ) { seed, v, colors ->
        ButterflyWingsTextureGenerator().generate(
            768, 768,
            ButterflyWingsTextureParameters(
                seed, v[0], v[1], v[2], v[3], v[4], v[5],
                colors[0], colors[1], colors[2], colors[3]
            )
        )
    },
    coloredTextureDemo(
        name = "Fungal gills",
        controls = listOf(
            TextureControl("Scale", 0.008f, 0.001f..0.04f),
            TextureControl("Gill density", 0.55f, 0f..1f),
            TextureControl("Undulation", 0.48f, 0f..1f),
            TextureControl("Relief", 0.55f, 0f..1f),
            TextureControl("Light", 0.65f, 0f..1f),
            TextureControl("Contrast", 1.05f, 0.2f..2.5f)
        ),
        colors = listOf(
            0xFF241B29.toInt(),
            0xFF785C72.toInt(),
            0xFFBD9A8B.toInt(),
            0xFFEFDABD.toInt()
        )
    ) { seed, v, colors ->
        FungalGillsTextureGenerator().generate(
            768, 768,
            FungalGillsTextureParameters(
                seed, v[0], v[1], v[2], v[3], v[4], v[5],
                colors[0], colors[1], colors[2], colors[3]
            )
        )
    },
    coloredTextureDemo(
        name = "Radiolaria",
        controls = listOf(
            TextureControl("Scale", 0.008f, 0.001f..0.04f),
            TextureControl("Pore density", 0.55f, 0f..1f),
            TextureControl("Spines", 0.48f, 0f..1f),
            TextureControl("Shell width", 0.55f, 0f..1f),
            TextureControl("Glow", 0.65f, 0f..1f),
            TextureControl("Contrast", 1.05f, 0.2f..2.5f)
        ),
        colors = listOf(
            0xFF092634.toInt(),
            0xFF28778B.toInt(),
            0xFF8FCAC3.toInt(),
            0xFFE7F1C9.toInt()
        )
    ) { seed, v, colors ->
        RadiolariaTextureGenerator().generate(
            768, 768,
            RadiolariaTextureParameters(
                seed, v[0], v[1], v[2], v[3], v[4], v[5],
                colors[0], colors[1], colors[2], colors[3]
            )
        )
    },
    coloredTextureDemo(
        name = "Slot canyon",
        controls = listOf(
            TextureControl("Scale", 0.008f, 0.001f..0.04f),
            TextureControl("Strata", 0.55f, 0f..1f),
            TextureControl("Meander", 0.48f, 0f..1f),
            TextureControl("Relief", 0.55f, 0f..1f),
            TextureControl("Light", 0.65f, 0f..1f),
            TextureControl("Contrast", 1.05f, 0.2f..2.5f)
        ),
        colors = listOf(
            0xFF261B2A.toInt(),
            0xFF864332.toInt(),
            0xFFCE8B5A.toInt(),
            0xFFF0D5A1.toInt()
        )
    ) { seed, v, colors ->
        SlotCanyonTextureGenerator().generate(
            768, 768,
            SlotCanyonTextureParameters(
                seed, v[0], v[1], v[2], v[3], v[4], v[5],
                colors[0], colors[1], colors[2], colors[3]
            )
        )
    },
    coloredTextureDemo(
        name = "Glacial crevasses",
        controls = listOf(
            TextureControl("Scale", 0.008f, 0.001f..0.04f),
            TextureControl("Fracture density", 0.55f, 0f..1f),
            TextureControl("Warping", 0.48f, 0f..1f),
            TextureControl("Depth", 0.55f, 0f..1f),
            TextureControl("Frost", 0.65f, 0f..1f),
            TextureControl("Contrast", 1.05f, 0.2f..2.5f)
        ),
        colors = listOf(
            0xFF0A2942.toInt(),
            0xFF2A718F.toInt(),
            0xFF83BDD0.toInt(),
            0xFFE4F0DF.toInt()
        )
    ) { seed, v, colors ->
        GlacialCrevassesTextureGenerator().generate(
            768, 768,
            GlacialCrevassesTextureParameters(
                seed, v[0], v[1], v[2], v[3], v[4], v[5],
                colors[0], colors[1], colors[2], colors[3]
            )
        )
    },
    coloredTextureDemo(
        name = "Spiral pearls",
        controls = listOf(
            TextureControl("Scale", 0.008f, 0.001f..0.04f),
            TextureControl("Spiral arms", 0.55f, 0f..1f),
            TextureControl("Growth", 0.48f, 0f..1f),
            TextureControl("Bead size", 0.55f, 0f..1f),
            TextureControl("Polish", 0.65f, 0f..1f),
            TextureControl("Contrast", 1.05f, 0.2f..2.5f)
        ),
        colors = listOf(
            0xFF101C31.toInt(),
            0xFF41879E.toInt(),
            0xFFC279A0.toInt(),
            0xFFF3DBAD.toInt()
        )
    ) { seed, v, colors ->
        SpiralPearlsTextureGenerator().generate(
            768, 768,
            SpiralPearlsTextureParameters(
                seed, v[0], v[1], v[2], v[3], v[4], v[5],
                colors[0], colors[1], colors[2], colors[3]
            )
        )
    },
    coloredTextureDemo(
        name = "Hopf fibration",
        controls = listOf(
            TextureControl("Scale", 0.008f, 0.001f..0.04f),
            TextureControl("Fiber count", 0.55f, 0f..1f),
            TextureControl("Latitude", 0.48f, 0f..1f),
            TextureControl("Thickness", 0.55f, 0f..1f),
            TextureControl("Glow", 0.65f, 0f..1f),
            TextureControl("Contrast", 1.05f, 0.2f..2.5f)
        ),
        colors = listOf(
            0xFF0A1930.toInt(),
            0xFF338DA5.toInt(),
            0xFFD77B9D.toInt(),
            0xFFEED293.toInt()
        )
    ) { seed, v, colors ->
        HopfFibrationTextureGenerator().generate(
            768, 768,
            HopfFibrationTextureParameters(
                seed, v[0], v[1], v[2], v[3], v[4], v[5],
                colors[0], colors[1], colors[2], colors[3]
            )
        )
    },
    coloredTextureDemo(
        name = "Spirograph",
        controls = listOf(
            TextureControl("Scale", 0.008f, 0.001f..0.04f),
            TextureControl("Lobes", 0.55f, 0f..1f),
            TextureControl("Ratio", 0.48f, 0f..1f),
            TextureControl("Line width", 0.55f, 0f..1f),
            TextureControl("Glow", 0.65f, 0f..1f),
            TextureControl("Contrast", 1.05f, 0.2f..2.5f)
        ),
        colors = listOf(
            0xFF111A30.toInt(),
            0xFF339DAD.toInt(),
            0xFFDA7FA9.toInt(),
            0xFFF4D48D.toInt()
        )
    ) { seed, v, colors ->
        SpirographTextureGenerator().generate(
            768, 768,
            SpirographTextureParameters(
                seed, v[0], v[1], v[2], v[3], v[4], v[5],
                colors[0], colors[1], colors[2], colors[3]
            )
        )
    },
    coloredTextureDemo(
        name = "Chiral pinwheels",
        controls = listOf(
            TextureControl("Scale", 0.008f, 0.001f..0.04f),
            TextureControl("Tile density", 0.55f, 0f..1f),
            TextureControl("Chirality", 0.48f, 0f..1f),
            TextureControl("Curvature", 0.55f, 0f..1f),
            TextureControl("Relief", 0.65f, 0f..1f),
            TextureControl("Contrast", 1.05f, 0.2f..2.5f)
        ),
        colors = listOf(
            0xFF132F3D.toInt(),
            0xFF519399.toInt(),
            0xFFC88370.toInt(),
            0xFFF2D49E.toInt()
        )
    ) { seed, v, colors ->
        ChiralPinwheelsTextureGenerator().generate(
            768, 768,
            ChiralPinwheelsTextureParameters(
                seed, v[0], v[1], v[2], v[3], v[4], v[5],
                colors[0], colors[1], colors[2], colors[3]
            )
        )
    },
    coloredTextureDemo(
        name = "Truchet blocks",
        controls = listOf(
            TextureControl("Scale", 0.008f, 0.001f..0.04f),
            TextureControl("Tile density", 0.55f, 0f..1f),
            TextureControl("Routing", 0.48f, 0f..1f),
            TextureControl("Bevel", 0.55f, 0f..1f),
            TextureControl("Light", 0.65f, 0f..1f),
            TextureControl("Contrast", 1.05f, 0.2f..2.5f)
        ),
        colors = listOf(
            0xFF112330.toInt(),
            0xFF2F6679.toInt(),
            0xFFCA9764.toInt(),
            0xFFE6D8B4.toInt()
        )
    ) { seed, v, colors ->
        TruchetBlocksTextureGenerator().generate(
            768, 768,
            TruchetBlocksTextureParameters(
                seed, v[0], v[1], v[2], v[3], v[4], v[5],
                colors[0], colors[1], colors[2], colors[3]
            )
        )
    },
    coloredTextureDemo(
        name = "Impossible stairs",
        controls = listOf(
            TextureControl("Scale", 0.008f, 0.001f..0.04f),
            TextureControl("Stair count", 0.55f, 0f..1f),
            TextureControl("Rotation", 0.48f, 0f..1f),
            TextureControl("Step height", 0.55f, 0f..1f),
            TextureControl("Light", 0.65f, 0f..1f),
            TextureControl("Contrast", 1.05f, 0.2f..2.5f)
        ),
        colors = listOf(
            0xFF152B3B.toInt(),
            0xFF507E91.toInt(),
            0xFFC08C79.toInt(),
            0xFFEADAB6.toInt()
        )
    ) { seed, v, colors ->
        ImpossibleStairsTextureGenerator().generate(
            768, 768,
            ImpossibleStairsTextureParameters(
                seed, v[0], v[1], v[2], v[3], v[4], v[5],
                colors[0], colors[1], colors[2], colors[3]
            )
        )
    },
    coloredTextureDemo(
        name = "Woven hexagons",
        controls = listOf(
            TextureControl("Scale", 0.008f, 0.001f..0.04f),
            TextureControl("Weave density", 0.55f, 0f..1f),
            TextureControl("Ribbon width", 0.48f, 0f..1f),
            TextureControl("Twist", 0.55f, 0f..1f),
            TextureControl("Relief", 0.65f, 0f..1f),
            TextureControl("Contrast", 1.05f, 0.2f..2.5f)
        ),
        colors = listOf(
            0xFF132E35.toInt(),
            0xFF2D7580.toInt(),
            0xFFD19B61.toInt(),
            0xFFF3DBA7.toInt()
        )
    ) { seed, v, colors ->
        WovenHexagonsTextureGenerator().generate(
            768, 768,
            WovenHexagonsTextureParameters(
                seed, v[0], v[1], v[2], v[3], v[4], v[5],
                colors[0], colors[1], colors[2], colors[3]
            )
        )
    },
    coloredTextureDemo(
        name = "Gyroid",
        controls = listOf(
            TextureControl("Scale", 0.008f, 0.001f..0.04f),
            TextureControl("Periods", 0.55f, 0f..1f),
            TextureControl("Rotation", 0.48f, 0f..1f),
            TextureControl("Thickness", 0.55f, 0f..1f),
            TextureControl("Polish", 0.65f, 0f..1f),
            TextureControl("Contrast", 1.05f, 0.2f..2.5f)
        ),
        colors = listOf(
            0xFF102336.toInt(),
            0xFF498C9F.toInt(),
            0xFFB7839D.toInt(),
            0xFFEDDCB0.toInt()
        )
    ) { seed, v, colors ->
        GyroidTextureGenerator().generate(
            768, 768,
            GyroidTextureParameters(
                seed, v[0], v[1], v[2], v[3], v[4], v[5],
                colors[0], colors[1], colors[2], colors[3]
            )
        )
    },
    coloredTextureDemo(
        name = "Hypercube",
        controls = listOf(
            TextureControl("Scale", 0.008f, 0.001f..0.04f),
            TextureControl("Projection", 0.55f, 0f..1f),
            TextureControl("Rotation", 0.48f, 0f..1f),
            TextureControl("Line width", 0.55f, 0f..1f),
            TextureControl("Glow", 0.65f, 0f..1f),
            TextureControl("Contrast", 1.05f, 0.2f..2.5f)
        ),
        colors = listOf(
            0xFF081B30.toInt(),
            0xFF2C8AA4.toInt(),
            0xFFD08BAB.toInt(),
            0xFFF1D797.toInt()
        )
    ) { seed, v, colors ->
        HypercubeTextureGenerator().generate(
            768, 768,
            HypercubeTextureParameters(
                seed, v[0], v[1], v[2], v[3], v[4], v[5],
                colors[0], colors[1], colors[2], colors[3]
            )
        )
    },
    coloredTextureDemo(
        name = "Lenticular waves",
        controls = listOf(
            TextureControl("Scale", 0.008f, 0.001f..0.04f),
            TextureControl("Lens density", 0.55f, 0f..1f),
            TextureControl("Phase", 0.48f, 0f..1f),
            TextureControl("Curvature", 0.55f, 0f..1f),
            TextureControl("Relief", 0.65f, 0f..1f),
            TextureControl("Contrast", 1.05f, 0.2f..2.5f)
        ),
        colors = listOf(
            0xFF162F49.toInt(),
            0xFF4A96A8.toInt(),
            0xFFD78E8D.toInt(),
            0xFFF2DBAA.toInt()
        )
    ) { seed, v, colors ->
        LenticularWavesTextureGenerator().generate(
            768, 768,
            LenticularWavesTextureParameters(
                seed, v[0], v[1], v[2], v[3], v[4], v[5],
                colors[0], colors[1], colors[2], colors[3]
            )
        )
    },
    coloredTextureDemo(
        name = "Stellar nursery",
        controls = listOf(
            TextureControl("Scale", 0.008f, 0.001f..0.04f),
            TextureControl("Pillar density", 0.55f, 0f..1f),
            TextureControl("Turbulence", 0.48f, 0f..1f),
            TextureControl("Dust", 0.55f, 0f..1f),
            TextureControl("Stars", 0.65f, 0f..1f),
            TextureControl("Contrast", 1.05f, 0.2f..2.5f)
        ),
        colors = listOf(
            0xFF081627.toInt(),
            0xFF34536A.toInt(),
            0xFFC0836B.toInt(),
            0xFFF6DBA9.toInt()
        )
    ) { seed, v, colors ->
        StellarNurseryTextureGenerator().generate(
            768, 768,
            StellarNurseryTextureParameters(
                seed, v[0], v[1], v[2], v[3], v[4], v[5],
                colors[0], colors[1], colors[2], colors[3]
            )
        )
    },
    coloredTextureDemo(
        name = "Pulsar jets",
        controls = listOf(
            TextureControl("Scale", 0.008f, 0.001f..0.04f),
            TextureControl("Jet width", 0.55f, 0f..1f),
            TextureControl("Precession", 0.48f, 0f..1f),
            TextureControl("Shock rings", 0.55f, 0f..1f),
            TextureControl("Glow", 0.65f, 0f..1f),
            TextureControl("Contrast", 1.05f, 0.2f..2.5f)
        ),
        colors = listOf(
            0xFF07142B.toInt(),
            0xFF336CB0.toInt(),
            0xFFC078AD.toInt(),
            0xFFF2E5B4.toInt()
        )
    ) { seed, v, colors ->
        PulsarJetsTextureGenerator().generate(
            768, 768,
            PulsarJetsTextureParameters(
                seed, v[0], v[1], v[2], v[3], v[4], v[5],
                colors[0], colors[1], colors[2], colors[3]
            )
        )
    },
    coloredTextureDemo(
        name = "Gravitational lensing",
        controls = listOf(
            TextureControl("Scale", 0.008f, 0.001f..0.04f),
            TextureControl("Galaxy density", 0.55f, 0f..1f),
            TextureControl("Lens strength", 0.48f, 0f..1f),
            TextureControl("Arc width", 0.55f, 0f..1f),
            TextureControl("Stars", 0.65f, 0f..1f),
            TextureControl("Contrast", 1.05f, 0.2f..2.5f)
        ),
        colors = listOf(
            0xFF081528.toInt(),
            0xFF3A7092.toInt(),
            0xFFC7A374.toInt(),
            0xFFEDF3D2.toInt()
        )
    ) { seed, v, colors ->
        GravitationalLensingTextureGenerator().generate(
            768, 768,
            GravitationalLensingTextureParameters(
                seed, v[0], v[1], v[2], v[3], v[4], v[5],
                colors[0], colors[1], colors[2], colors[3]
            )
        )
    },
    coloredTextureDemo(
        name = "Nacre clouds",
        controls = listOf(
            TextureControl("Scale", 0.008f, 0.001f..0.04f),
            TextureControl("Wave density", 0.55f, 0f..1f),
            TextureControl("Turbulence", 0.48f, 0f..1f),
            TextureControl("Iridescence", 0.55f, 0f..1f),
            TextureControl("Light", 0.65f, 0f..1f),
            TextureControl("Contrast", 1.05f, 0.2f..2.5f)
        ),
        colors = listOf(
            0xFF263650.toInt(),
            0xFF789AB0.toInt(),
            0xFFCA9FBB.toInt(),
            0xFFF2D6AC.toInt()
        )
    ) { seed, v, colors ->
        NacreCloudsTextureGenerator().generate(
            768, 768,
            NacreCloudsTextureParameters(
                seed, v[0], v[1], v[2], v[3], v[4], v[5],
                colors[0], colors[1], colors[2], colors[3]
            )
        )
    },
    coloredTextureDemo(
        name = "Mercury droplets",
        controls = listOf(
            TextureControl("Scale", 0.008f, 0.001f..0.04f),
            TextureControl("Drop density", 0.55f, 0f..1f),
            TextureControl("Size variation", 0.48f, 0f..1f),
            TextureControl("Roundness", 0.55f, 0f..1f),
            TextureControl("Polish", 0.65f, 0f..1f),
            TextureControl("Contrast", 1.05f, 0.2f..2.5f)
        ),
        colors = listOf(
            0xFF0C2536.toInt(),
            0xFF587887.toInt(),
            0xFFCFAE7E.toInt(),
            0xFFF0F0D5.toInt()
        )
    ) { seed, v, colors ->
        MercuryDropletsTextureGenerator().generate(
            768, 768,
            MercuryDropletsTextureParameters(
                seed, v[0], v[1], v[2], v[3], v[4], v[5],
                colors[0], colors[1], colors[2], colors[3]
            )
        )
    },
    coloredTextureDemo(
        name = "Sonic boom",
        controls = listOf(
            TextureControl("Scale", 0.008f, 0.001f..0.04f),
            TextureControl("Wave count", 0.55f, 0f..1f),
            TextureControl("Mach angle", 0.48f, 0f..1f),
            TextureControl("Thickness", 0.55f, 0f..1f),
            TextureControl("Glow", 0.65f, 0f..1f),
            TextureControl("Contrast", 1.05f, 0.2f..2.5f)
        ),
        colors = listOf(
            0xFF0B2340.toInt(),
            0xFF307C9C.toInt(),
            0xFFB67CAD.toInt(),
            0xFFF1E0AA.toInt()
        )
    ) { seed, v, colors ->
        SonicBoomTextureGenerator().generate(
            768, 768,
            SonicBoomTextureParameters(
                seed, v[0], v[1], v[2], v[3], v[4], v[5],
                colors[0], colors[1], colors[2], colors[3]
            )
        )
    },
    coloredTextureDemo(
        name = "Ribbon vortex",
        controls = listOf(
            TextureControl("Scale", 0.008f, 0.001f..0.04f),
            TextureControl("Ribbon count", 0.55f, 0f..1f),
            TextureControl("Twist", 0.48f, 0f..1f),
            TextureControl("Thickness", 0.55f, 0f..1f),
            TextureControl("Light", 0.65f, 0f..1f),
            TextureControl("Contrast", 1.05f, 0.2f..2.5f)
        ),
        colors = listOf(
            0xFF0C1C34.toInt(),
            0xFF368EA4.toInt(),
            0xFFD47D9F.toInt(),
            0xFFEFD397.toInt()
        )
    ) { seed, v, colors ->
        RibbonVortexTextureGenerator().generate(
            768, 768,
            RibbonVortexTextureParameters(
                seed, v[0], v[1], v[2], v[3], v[4], v[5],
                colors[0], colors[1], colors[2], colors[3]
            )
        )
    },
    coloredTextureDemo(
        name = "Iridescent foam",
        controls = listOf(
            TextureControl("Scale", 0.008f, 0.001f..0.04f),
            TextureControl("Bubble density", 0.55f, 0f..1f),
            TextureControl("Size variation", 0.48f, 0f..1f),
            TextureControl("Film width", 0.55f, 0f..1f),
            TextureControl("Iridescence", 0.65f, 0f..1f),
            TextureControl("Contrast", 1.05f, 0.2f..2.5f)
        ),
        colors = listOf(
            0xFF0B263B.toInt(),
            0xFF4C869E.toInt(),
            0xFFC08DB5.toInt(),
            0xFFECE4BD.toInt()
        )
    ) { seed, v, colors ->
        IridescentFoamTextureGenerator().generate(
            768, 768,
            IridescentFoamTextureParameters(
                seed, v[0], v[1], v[2], v[3], v[4], v[5],
                colors[0], colors[1], colors[2], colors[3]
            )
        )
    },
    coloredTextureDemo(
        name = "Lava crust",
        controls = listOf(
            TextureControl("Scale", 0.008f, 0.001f..0.04f),
            TextureControl("Plate density", 0.55f, 0f..1f),
            TextureControl("Irregularity", 0.48f, 0f..1f),
            TextureControl("Crust depth", 0.55f, 0f..1f),
            TextureControl("Heat", 0.65f, 0f..1f),
            TextureControl("Contrast", 1.05f, 0.2f..2.5f)
        ),
        colors = listOf(
            0xFF141B24.toInt(),
            0xFF493C42.toInt(),
            0xFFDB692D.toInt(),
            0xFFFFE0A0.toInt()
        )
    ) { seed, v, colors ->
        LavaCrustTextureGenerator().generate(
            768, 768,
            LavaCrustTextureParameters(
                seed, v[0], v[1], v[2], v[3], v[4], v[5],
                colors[0], colors[1], colors[2], colors[3]
            )
        )
    },
    coloredTextureDemo(
        name = "Abyssal vent",
        controls = listOf(
            TextureControl("Scale", 0.008f, 0.001f..0.04f),
            TextureControl("Chimney count", 0.55f, 0f..1f),
            TextureControl("Current", 0.48f, 0f..1f),
            TextureControl("Smoke density", 0.55f, 0f..1f),
            TextureControl("Glow", 0.65f, 0f..1f),
            TextureControl("Contrast", 1.05f, 0.2f..2.5f)
        ),
        colors = listOf(
            0xFF061D2C.toInt(),
            0xFF254D5E.toInt(),
            0xFF9C8070.toInt(),
            0xFFC6D6B7.toInt()
        )
    ) { seed, v, colors ->
        AbyssalVentTextureGenerator().generate(
            768, 768,
            AbyssalVentTextureParameters(
                seed, v[0], v[1], v[2], v[3], v[4], v[5],
                colors[0], colors[1], colors[2], colors[3]
            )
        )
    }
)
