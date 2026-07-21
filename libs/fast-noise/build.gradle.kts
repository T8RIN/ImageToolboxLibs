plugins {
    alias(libs.plugins.image.toolbox.library)
    alias(libs.plugins.image.toolbox.native)
    alias(libs.plugins.image.toolbox.maven)
}

android {
    namespace = "com.t8rin.fast_noise"

    buildFeatures {
        buildConfig = true
    }
}