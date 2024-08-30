plugins {
    alias(libs.plugins.image.toolbox.library)
    alias(libs.plugins.image.toolbox.compose)
    alias(libs.plugins.image.toolbox.maven)
}

android.namespace = "com.smarttoolfactory.colordetector"

dependencies {
    implementation(libs.androidx.palette.ktx)

    implementation(projects.gesture)
    implementation(projects.screenshot)
    implementation(projects.image)
    implementation(projects.zoomable)

    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.tooling)
    implementation(libs.compose.runtime)
    implementation(libs.compose.material3)
    implementation(libs.compose.material)
}