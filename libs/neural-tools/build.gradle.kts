plugins {
    alias(libs.plugins.image.toolbox.library)
    alias(libs.plugins.image.toolbox.maven)
    alias(libs.plugins.image.toolbox.compose)
}

android.namespace = "com.t8rin.neural_tools"

dependencies {
    implementation(libs.pytorch)
    implementation(libs.aire)
}