package com.t8rin.gmic.filters

enum class RetinexColorSpace(override val value: Int) : GmicArgument {
    Hsi(0),
    Hsv(1),
    Lab(2),
    LinearRgb(3),
    Rgb(4),
    YCbCr(5)
}

data class Retinex(
    val strength: Float = 75f,
    val valueOffset: Float = 16f,
    val colorSpace: RetinexColorSpace = RetinexColorSpace.Hsv,
    val minCut: Float = 1f,
    val maxCut: Float = 1f,
    val regularization: Float = 5f,
    val lowScale: Float = 15f,
    val middleScale: Float = 80f,
    val highScale: Float = 250f
) : RawGmicFilter(
    gmicCommand(
        "fx_retinex",
        strength.inRange("strength", 0f, 100f),
        valueOffset.inRange("valueOffset", 1f, 256f),
        colorSpace,
        minCut.inRange("minCut", 0f, 100f),
        maxCut.inRange("maxCut", 0f, 100f),
        regularization.inRange("regularization", 0f, 32f),
        lowScale.inRange("lowScale", 1f, 512f),
        middleScale.inRange("middleScale", 1f, 512f),
        highScale.inRange("highScale", 1f, 512f)
    )
)

enum class DetailSmoothing(override val value: Int) : GmicArgument {
    Gaussian(0),
    Bilateral(1),
    Diffusion(2)
}

data class DetailBand(
    val threshold: Float = 0f,
    val smoothness: Float = 0f,
    val smoothing: DetailSmoothing = DetailSmoothing.Diffusion,
    val gain: Float = 0f
)

enum class GmicParallelism(override val value: Int) : GmicArgument {
    Auto(0),
    OneThread(1),
    TwoThreads(2),
    FourThreads(3),
    EightThreads(4),
    SixteenThreads(5)
}

data class DetailsEqualizer(
    val baseScale: Float = 5f,
    val detailScale: Float = 0.5f,
    val coarse: DetailBand = DetailBand(),
    val medium: DetailBand = DetailBand(),
    val small: DetailBand = DetailBand(),
    val fine: DetailBand = DetailBand(),
    val channel: GmicChannel = GmicChannel.All,
    val valueAction: GmicValueAction = GmicValueAction.None,
    val parallelism: GmicParallelism = GmicParallelism.Auto,
    val spatialOverlap: Int = 32
) : RawGmicFilter(
    gmicCommand(
        "fx_equalize_details",
        baseScale.inRange("baseScale", 0f, 15f),
        detailScale.inRange("detailScale", 0f, 5f),
        coarse.threshold.inRange("coarse.threshold", 0f, 10f),
        coarse.smoothness.inRange("coarse.smoothness", 0f, 10f),
        coarse.smoothing,
        coarse.gain.inRange("coarse.gain", -4f, 4f),
        medium.threshold.inRange("medium.threshold", 0f, 10f),
        medium.smoothness.inRange("medium.smoothness", 0f, 10f),
        medium.smoothing,
        medium.gain.inRange("medium.gain", -4f, 4f),
        small.threshold.inRange("small.threshold", 0f, 10f),
        small.smoothness.inRange("small.smoothness", 0f, 10f),
        small.smoothing,
        small.gain.inRange("small.gain", -4f, 4f),
        fine.threshold.inRange("fine.threshold", 0f, 10f),
        fine.smoothness.inRange("fine.smoothness", 0f, 10f),
        fine.smoothing,
        fine.gain.inRange("fine.gain", -4f, 4f),
        channel,
        valueAction,
        parallelism,
        spatialOverlap.inRange("spatialOverlap", 0..256)
    )
)

data class FreakyDetails(
    val amplitude: Int = 2,
    val scale: Float = 10f,
    val iterations: Int = 1,
    val channel: GmicChannel = GmicChannel.LinearRgb
) : RawGmicFilter(
    gmicCommand(
        "fx_freaky_details",
        amplitude.inRange("amplitude", 1..5),
        scale.inRange("scale", 0f, 100f),
        iterations.inRange("iterations", 1..4),
        channel
    )
)

data class MagicDetails(
    val amplitude: Float = 6f,
    val spatialScale: Float = 3f,
    val valueScale: Float = 15f,
    val edges: Float = -0.5f,
    val smoothness: Float = 2f,
    val channel: GmicChannel = GmicChannel.Luminance
) : RawGmicFilter(
    gmicCommand(
        "fx_magic_details",
        amplitude.inRange("amplitude", 0f, 30f),
        spatialScale.inRange("spatialScale", 0f, 10f),
        valueScale.inRange("valueScale", 0f, 20f),
        edges.inRange("edges", -3f, 3f),
        smoothness.inRange("smoothness", 0f, 20f),
        channel
    )
)

data class MightyDetails(
    val amplitude: Float = 25f,
    val amount: Float = 1f,
    val scale: Float = 25f,
    val smoothness: Int = 1,
    val channel: GmicChannel = GmicChannel.Luminance
) : RawGmicFilter(
    gmicCommand(
        "fx_mighty_details",
        amplitude.inRange("amplitude", 0f, 100f),
        amount.inRange("amount", 0f, 2f),
        scale.inRange("scale", 1f, 100f),
        smoothness.inRange("smoothness", 0..10),
        channel
    )
)

data class PopShadows(
    val strength: Float = 0.75f,
    val scale: Float = 5f,
    val postNormalize: Boolean = true
) : RawGmicFilter(
    gmicCommand(
        "fx_pop_shadows",
        strength.inRange("strength", 0f, 1f),
        scale.inRange("scale", 0f, 20f),
        postNormalize
    )
)
