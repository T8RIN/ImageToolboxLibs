plugins {
    alias(libs.plugins.image.toolbox.library)
    alias(libs.plugins.image.toolbox.maven)
    alias(libs.plugins.image.toolbox.compose)
}

android {
    namespace = "com.t8rin.histogram"

    buildFeatures {
        buildConfig = true
    }
    defaultConfig {
        ndk.abiFilters.addAll(listOf("armeabi-v7a", "arm64-v8a", "x86", "x86_64"))
    }
    externalNativeBuild {
        cmake {
            path("CMakeLists.txt")
            ndkVersion = "26.1.10909125"
        }
    }
}

dependencies {
    implementation(libs.coil)
    implementation(libs.compose.charts)
}