plugins {
    alias(libs.plugins.image.toolbox.library)
    alias(libs.plugins.image.toolbox.maven)
}

android {
    namespace = "com.watermark.androidwm"
    defaultConfig {
        ndk.abiFilters.addAll(listOf("armeabi-v7a", "arm64-v8a", "x86", "x86_64"))
    }
    externalNativeBuild {
        cmake {
            path("src/main/cpp/CMakeLists.txt")
            ndkVersion = "28.1.13356709"
        }
    }
}

dependencies {
    implementation(libs.appCompat)
}