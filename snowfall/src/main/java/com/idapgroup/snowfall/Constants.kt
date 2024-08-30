package com.idapgroup.snowfall

internal object Constants {
    internal const val angleRange = 0.1f
    internal const val angleDivisor = 10000.0f
    internal const val angleSeed = 100.0f
    internal const val baseFrameDurationMillis = 16
    internal const val snowflakeDensity = 0.05
    internal const val defaultAlpha = 0.65f

    internal val incrementRange = 0.2f..0.8f
    internal val sizeRange = 20f..40f
    internal val angleSeedRange = -angleSeed..angleSeed
}