package com.t8rin.raw_coder

enum class RawDecodeMode {
    EmbeddedPreview,
    Developed
}

enum class RawOutputColorSpace(internal val nativeValue: Int) {
    SRgb(1),
    AdobeRgb(2),
    WideGamutRgb(3),
    ProPhotoRgb(4),
    Xyz(5),
    Aces(6)
}

enum class RawDemosaicQuality(internal val nativeValue: Int) {
    Linear(0),
    Vng(1),
    Ppg(2),
    Ahd(3),
    Dcb(4),
    Dht(11),
    Aahd(12)
}

sealed interface RawWhiteBalance {
    data object Camera : RawWhiteBalance
    data object Auto : RawWhiteBalance
    data object Daylight : RawWhiteBalance

    data class Custom(
        val redMultiplier: Float,
        val greenMultiplier: Float,
        val blueMultiplier: Float,
        val secondGreenMultiplier: Float = greenMultiplier
    ) : RawWhiteBalance {
        init {
            require(
                redMultiplier.isFinite() && redMultiplier > 0f &&
                        greenMultiplier.isFinite() && greenMultiplier > 0f &&
                        blueMultiplier.isFinite() && blueMultiplier > 0f &&
                        secondGreenMultiplier.isFinite() && secondGreenMultiplier > 0f
            ) { "White balance multipliers must be finite and greater than zero" }
        }
    }
}

sealed interface RawHighlightRecovery {
    data object Clip : RawHighlightRecovery
    data object Unclip : RawHighlightRecovery
    data object Blend : RawHighlightRecovery

    data class Reconstruct(val level: Int = 5) : RawHighlightRecovery {
        init {
            require(level in 3..9) { "Highlight reconstruction level must be in 3..9" }
        }
    }
}

internal val RawWhiteBalance.nativeValue: Int
    get() = when (this) {
        RawWhiteBalance.Camera -> 0
        RawWhiteBalance.Auto -> 1
        RawWhiteBalance.Daylight -> 2
        is RawWhiteBalance.Custom -> 3
    }

internal val RawHighlightRecovery.nativeValue: Int
    get() = when (this) {
        RawHighlightRecovery.Clip -> 0
        RawHighlightRecovery.Unclip -> 1
        RawHighlightRecovery.Blend -> 2
        is RawHighlightRecovery.Reconstruct -> level
    }

data class RawDevelopSettings(
    val whiteBalance: RawWhiteBalance = RawWhiteBalance.Camera,
    val outputColorSpace: RawOutputColorSpace = RawOutputColorSpace.SRgb,
    val highlightRecovery: RawHighlightRecovery = RawHighlightRecovery.Clip,
    val exposureCompensationEv: Float = 0f,
    val highlightPreservation: Float = 0f,
    val autoBrightness: Boolean = true,
    val brightness: Float = 1f,
    val quality: RawDemosaicQuality = RawDemosaicQuality.Ahd,
    val halfSize: Boolean = false,
    val applyOrientation: Boolean = true
) {
    init {
        require(exposureCompensationEv.isFinite() && exposureCompensationEv in -2f..3f) {
            "exposureCompensationEv must be finite and in -2..3"
        }
        require(highlightPreservation.isFinite() && highlightPreservation in 0f..1f) {
            "highlightPreservation must be finite and in 0..1"
        }
        require(brightness.isFinite() && brightness > 0f) {
            "brightness must be finite and greater than zero"
        }
    }
}

data class RawDecodeOptions(
    val mode: RawDecodeMode = RawDecodeMode.Developed,
    val developSettings: RawDevelopSettings = RawDevelopSettings()
)

data class RawInfo(
    val width: Int,
    val height: Int,
    val orientation: Int,
    val embeddedPreviewCount: Int,
    val isDng: Boolean
) {
    val orientedWidth: Int
        get() = if (orientation in ROTATED_ORIENTATIONS) height else width

    val orientedHeight: Int
        get() = if (orientation in ROTATED_ORIENTATIONS) width else height
}

internal val ROTATED_ORIENTATIONS = setOf(4, 5, 6, 7)
