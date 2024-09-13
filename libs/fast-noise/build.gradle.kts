plugins {
    alias(libs.plugins.image.toolbox.library)
    alias(libs.plugins.image.toolbox.maven)
}

android {
    namespace = "com.t8rin.fast_noise"

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