plugins {
    alias(libs.plugins.image.toolbox.library)
    alias(libs.plugins.image.toolbox.compose)
    alias(libs.plugins.image.toolbox.maven)
}

android.namespace = "com.smarttoolfactory.colorpicker"

dependencies {
    implementation(projects.libs.gesture)
    implementation(projects.libs.screenshot)
    implementation(projects.libs.extendedcolors)
    implementation(projects.libs.colordetector)

    implementation(libs.compose.colorful.sliders)

    implementation(libs.compose.foundation)
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.tooling)
    implementation(libs.compose.runtime)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material)
    implementation(libs.androidx.material.icons.extended)
}