plugins {
    alias(libs.plugins.image.toolbox.library)
    alias(libs.plugins.image.toolbox.maven)
}

android {
    namespace = "oupson.apng"

    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    implementation(libs.coil)
}