plugins {
    alias(libs.plugins.image.toolbox.library)
    alias(libs.plugins.image.toolbox.native)
    alias(libs.plugins.image.toolbox.compose)
    alias(libs.plugins.image.toolbox.maven)
}

android {
    namespace = "com.t8rin.crop.advanced"
}

dependencies {
    implementation(projects.libs.exif)
    implementation(libs.coil)
}