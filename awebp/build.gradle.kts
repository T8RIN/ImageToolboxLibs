plugins {
    alias(libs.plugins.image.toolbox.library)
    alias(libs.plugins.image.toolbox.maven)
}

android.namespace = "com.t8rin.awebp"

dependencies {
    implementation(libs.coil)
    implementation(libs.animation.awebp)
    implementation(libs.animation.awebp.encoder)
    implementation(libs.androidx.vectordrawable.animated)
}