package com.t8rin.gmic.filters

/** Soft highlight reconstruction followed by dense, cinematic shadows. */
data class CinematicDream(
    val detailsStrength: Float = 75f,
    val detailsScale: Float = 45f,
    val bloomSmoothness: Float = 55f,
    val highlight: Int = 28,
    val localContrast: Float = 18f,
    val shadowStrength: Float = 0.55f,
    val shadowScale: Float = 6f,
    val normalize: Boolean = true
) : RawGmicFilter(
    gmicPipeline(
        gmicCommand(
            "fx_highlight_bloom",
            detailsStrength.inRange("detailsStrength", 0f, 400f),
            detailsScale.inRange("detailsScale", 0f, 255f),
            bloomSmoothness.inRange("bloomSmoothness", 0f, 255f),
            highlight.inRange("highlight", 0..100),
            localContrast.inRange("localContrast", 0f, 100f)
        ),
        gmicCommand(
            "fx_pop_shadows",
            shadowStrength.inRange("shadowStrength", 0f, 1f),
            shadowScale.inRange("shadowScale", 0f, 20f),
            normalize
        )
    )
)

/** Poster edges enriched with Retinex micro-contrast for a graphic-novel finish. */
data class GraphicNovel(
    val smoothness: Float = 18f,
    val edgeThreshold: Float = 55f,
    val edgeShade: Float = 7f,
    val edgeThickness: Float = 1f,
    val posterization: Int = 7,
    val retinexStrength: Float = 35f,
    val retinexRegularization: Float = 4f
) : RawGmicFilter(
    gmicPipeline(
        gmicCommand(
            "fx_poster_edges",
            smoothness.inRange("smoothness", 0f, 100f),
            edgeThreshold.inRange("edgeThreshold", 0f, 100f),
            edgeShade.inRange("edgeShade", 0f, 30f),
            edgeThickness.inRange("edgeThickness", 0f, 5f),
            12,
            posterization.inRange("posterization", 0..15),
            20
        ),
        gmicCommand(
            "fx_retinex",
            retinexStrength.inRange("retinexStrength", 0f, 100f),
            16,
            RetinexColorSpace.Lab,
            1,
            1,
            retinexRegularization.inRange("retinexRegularization", 0f, 32f),
            15,
            80,
            250
        )
    )
)

/** Painterly abstraction with a controlled highlight bloom pass. */
data class DreamPainting(
    val abstraction: Int = 4,
    val detailsScale: Float = 2f,
    val colorStrength: Float = 1.7f,
    val paintSmoothness: Float = 40f,
    val sharpenShades: Boolean = true,
    val bloomStrength: Float = 45f,
    val bloomScale: Float = 35f,
    val bloomSmoothness: Float = 45f,
    val highlight: Int = 35,
    val contrast: Float = 12f
) : RawGmicFilter(
    gmicPipeline(
        gmicCommand(
            "fx_painting",
            abstraction.inRange("abstraction", 1..10),
            detailsScale.inRange("detailsScale", 0f, 5f),
            colorStrength.inRange("colorStrength", 0f, 4f),
            paintSmoothness.inRange("paintSmoothness", 0f, 1000f),
            sharpenShades
        ),
        gmicCommand(
            "fx_highlight_bloom",
            bloomStrength.inRange("bloomStrength", 0f, 400f),
            bloomScale.inRange("bloomScale", 0f, 255f),
            bloomSmoothness.inRange("bloomSmoothness", 0f, 255f),
            highlight.inRange("highlight", 0..100),
            contrast.inRange("contrast", 0f, 100f)
        )
    )
)

/** Dirt, lens color separation and JPEG damage combined into one analog-decay effect. */
data class AnalogDamage(
    val dirt: Float = 18f,
    val monochromeDirt: Boolean = true,
    val aberrationX: Float = 3f,
    val aberrationY: Float = 1f,
    val aberrationSmoothness: Float = 0.6f,
    val jpegQuality: Int = 38
) : RawGmicFilter(
    gmicPipeline(
        gmicCommand(
            "fx_dirty",
            dirt.inRange("dirt", 0f, 100f),
            monochromeDirt,
            GmicChannel.All,
            GmicValueAction.Cut
        ),
        gmicCommand(
            "fx_chromatic_aberrations",
            255,
            0,
            0,
            AberrationType.Shift,
            aberrationX.inRange("aberrationX", -32f, 32f),
            aberrationY.inRange("aberrationY", -32f, 32f),
            aberrationSmoothness.inRange("aberrationSmoothness", 0f, 10f),
            20,
            1,
            0,
            255,
            255,
            AberrationType.Shift,
            -aberrationX,
            -aberrationY,
            aberrationSmoothness,
            0,
            1
        ),
        gmicCommand(
            "fx_jpeg_artefacts",
            jpegQuality.inRange("jpegQuality", 1..100)
        )
    )
)
