package com.t8rin.gmic.filters

enum class SkinEstimation(override val value: Int) : GmicArgument {
    None(0),
    Manual(1),
    Automatic(2)
}

enum class SkinSmoothnessType(override val value: Int) : GmicArgument {
    Gaussian(0),
    Bilateral(1)
}

data class SmoothSkin(
    val skinEstimation: SkinEstimation = SkinEstimation.Automatic,
    val tolerance: Float = 0.5f,
    val maskSmoothness: Float = 1f,
    val threshold: Float = 1f,
    val preNormalize: Boolean = true,
    val manualX: Float = 50f,
    val manualY: Float = 50f,
    val manualRadius: Float = 5f,
    val baseScale: Float = 2f,
    val fineScale: Float = 0.2f,
    val detailSmoothness: Float = 3f,
    val smoothnessType: SkinSmoothnessType = SkinSmoothnessType.Bilateral,
    val gain: Float = 0.05f
) : RawGmicFilter(
    gmicCommand(
        "fx_smooth_skin",
        skinEstimation,
        tolerance.inRange("tolerance", 0f, 1f),
        maskSmoothness.inRange("maskSmoothness", 0f, 5f),
        threshold.inRange("threshold", 0f, 10f),
        preNormalize,
        manualX.inRange("manualX", 0f, 100f),
        manualY.inRange("manualY", 0f, 100f),
        manualRadius.inRange("manualRadius", 0f, 25f),
        baseScale.inRange("baseScale", 0f, 10f),
        fineScale.inRange("fineScale", 0f, 0.8f),
        detailSmoothness.inRange("detailSmoothness", 0f, 10f),
        smoothnessType,
        gain.inRange("gain", 0f, 0.5f)
    )
)
