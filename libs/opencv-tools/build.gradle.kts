plugins {
    alias(libs.plugins.image.toolbox.library)
    alias(libs.plugins.image.toolbox.maven)
    alias(libs.plugins.image.toolbox.compose)
}

android.namespace = "com.t8rin.opencv_tools"

dependencies {
    implementation(libs.opencv)
    implementation(libs.coil.compose)

    implementation(projects.libs.image)
    implementation(projects.libs.zoomable)
    implementation(projects.libs.gesture)
}