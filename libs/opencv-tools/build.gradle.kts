plugins {
    alias(libs.plugins.image.toolbox.library)
    alias(libs.plugins.image.toolbox.maven)
}

android.namespace = "com.t8rin.opencv_tools"

dependencies {
    implementation(libs.opencv)
}