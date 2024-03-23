plugins {
    alias(libs.plugins.image.toolbox.library)
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