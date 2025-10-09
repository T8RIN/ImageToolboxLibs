plugins {
    alias(libs.plugins.image.toolbox.library)
    alias(libs.plugins.image.toolbox.compose)
    alias(libs.plugins.image.toolbox.maven)
}

android.namespace = "io.github.alexzhirkevich"

dependencies {
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
}