plugins {
    alias(libs.plugins.image.toolbox.library)
    alias(libs.plugins.image.toolbox.native)
    alias(libs.plugins.image.toolbox.maven)
}

android {
    namespace = "com.gemalto.jp2"

    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    implementation(libs.coil)
}