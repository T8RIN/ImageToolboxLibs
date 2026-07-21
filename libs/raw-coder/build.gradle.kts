@file:Suppress("UnstableApiUsage")

plugins {
    alias(libs.plugins.image.toolbox.library)
    alias(libs.plugins.image.toolbox.native)
    alias(libs.plugins.image.toolbox.maven)
}

android {
    namespace = "com.t8rin.raw_coder"
    defaultConfig {
        minSdk = 24
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        externalNativeBuild {
            cmake {
                arguments += "-DCMAKE_BUILD_TYPE=Release"
                cppFlags += listOf("-O3", "-fopenmp", "-flto", "-fvisibility=hidden")
            }
        }
    }
}

dependencies {
    api(libs.coil)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.runner)
}