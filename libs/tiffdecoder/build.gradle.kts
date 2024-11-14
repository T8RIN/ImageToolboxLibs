plugins {
    alias(libs.plugins.image.toolbox.library)
    alias(libs.plugins.image.toolbox.maven)
}

android {
    namespace = "com.t8rin.tiff"
}

dependencies {
    implementation(libs.coil)
    implementation(libs.coil.network)
    implementation(libs.ktor)
    api(libs.android.tiffbitmapfactory)
}