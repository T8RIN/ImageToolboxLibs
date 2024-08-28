plugins {
    alias(libs.plugins.image.toolbox.library)
    alias(libs.plugins.image.toolbox.maven)
}

android.namespace = "com.t8rin.awebp"

dependencies {
    implementation(libs.coil)
    implementation(libs.awebp)
    implementation(libs.awebp.encoder)
    implementation(libs.androidx.vectordrawable.animated)
}