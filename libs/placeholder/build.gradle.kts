plugins {
    alias(libs.plugins.image.toolbox.library)
    alias(libs.plugins.image.toolbox.compose)
    alias(libs.plugins.image.toolbox.maven)
}

android.namespace = "com.google.accompanist.placeholder"

dependencies {
    implementation(libs.compose.foundation)
    implementation(libs.compose.ui.util)
    implementation(libs.coroutinesAndroid)
}
