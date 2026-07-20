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

data class RawDevelopSettings(
    val useCameraWhiteBalance: Boolean = true,
    val useAutoWhiteBalance: Boolean = false,
    val outputColorSpace: RawOutputColorSpace = RawOutputColorSpace.SRgb,
    val highlightRecovery: Int = 0,
    val quality: RawDemosaicQuality = RawDemosaicQuality.Ahd,
    val halfSize: Boolean = false,
    val applyOrientation: Boolean = true
) {
    init {
        require(highlightRecovery in 0..9) { "highlightRecovery must be in 0..9" }
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
)
