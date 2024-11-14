plugins {
    alias(libs.plugins.image.toolbox.library)
    alias(libs.plugins.image.toolbox.maven)
}

android.namespace = "com.t8rin.avif"

dependencies {
    implementation(libs.coil)
    implementation(libs.coil.network)
    implementation(libs.ktor)
    implementation(libs.coil.network)
    implementation(libs.ktor)
    implementation(libs.animation.avif)
    implementation(libs.androidx.vectordrawable.animated)
    implementation(libs.androidx.heifwriter)
}