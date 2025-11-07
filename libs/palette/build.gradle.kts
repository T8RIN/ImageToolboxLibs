plugins {
    alias(libs.plugins.image.toolbox.library)
    alias(libs.plugins.image.toolbox.compose)
    alias(libs.plugins.image.toolbox.maven)
    kotlin("plugin.serialization") version libs.versions.kotlin.get()
}

android.namespace = "com.t8rin.palette"

dependencies {
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.tooling)
    implementation(libs.compose.runtime)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material)
    implementation(libs.kotlinx.serialization.json)
}