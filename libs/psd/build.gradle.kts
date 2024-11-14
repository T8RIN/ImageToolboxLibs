plugins {
    alias(libs.plugins.image.toolbox.library)
    alias(libs.plugins.image.toolbox.maven)
}

android.namespace = "com.t8rin.psd"

dependencies {
    implementation(libs.coil)
    implementation(libs.coil.network)
    implementation(libs.ktor)
}