@file:Suppress("UnstableApiUsage")

plugins {
    alias(libs.plugins.image.toolbox.library)
    alias(libs.plugins.image.toolbox.native)
    alias(libs.plugins.image.toolbox.maven)
    alias(libs.plugins.image.toolbox.compose)
}

android {
    namespace = "com.t8rin.histogram"

    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    implementation(libs.coil)
}