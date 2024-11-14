plugins {
    alias(libs.plugins.image.toolbox.library)
    alias(libs.plugins.image.toolbox.compose)
    alias(libs.plugins.image.toolbox.maven)
}

android {
    namespace = "com.yalantis.ucrop"
    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    implementation(libs.appCompat)
    implementation(libs.exifinterface)
    implementation(libs.transition)
    implementation(libs.coil.compose)
    implementation(libs.okhttp)
}
