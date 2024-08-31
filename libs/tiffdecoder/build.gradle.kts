plugins {
    alias(libs.plugins.image.toolbox.library)
    alias(libs.plugins.image.toolbox.maven)
}

android {
    namespace = "com.t8rin.tiff"
}

dependencies {
    implementation(libs.coil)
    api(libs.android.tiffbitmapfactory)
}