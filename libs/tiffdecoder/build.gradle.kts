plugins {
    alias(libs.plugins.image.toolbox.library)
    alias(libs.plugins.image.toolbox.maven)
}

android {
    namespace = "org.beyka.tiffbitmapfactory"

    buildFeatures {
        buildConfig = true
    }
    defaultConfig {
        ndk.abiFilters.addAll(listOf("armeabi-v7a", "arm64-v8a", "x86", "x86_64"))
    }
    ndkVersion = "21.3.6528147"
}

dependencies {
    implementation(libs.coil)
}