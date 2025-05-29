plugins {
    alias(libs.plugins.image.toolbox.library)
    alias(libs.plugins.image.toolbox.compose)
    alias(libs.plugins.image.toolbox.maven)
}

android.namespace = "com.smarttoolfactory.gesture"

dependencies {
    implementation(libs.compose.foundation)
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.util)
    implementation(libs.compose.runtime)
    implementation(libs.androidx.material3)
    implementation(libs.icons.extended)
}