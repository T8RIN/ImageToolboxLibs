plugins {
    alias(libs.plugins.image.toolbox.library)
    alias(libs.plugins.image.toolbox.maven)
    alias(libs.plugins.image.toolbox.compose)
}

android.namespace = "com.t8rin.curves"

dependencies {
    implementation(libs.coil.compose)
    implementation(projects.libs.gpuimage)
    implementation(projects.libs.histogram)
}