plugins {
    alias(libs.plugins.image.toolbox.library)
    alias(libs.plugins.image.toolbox.compose)
    alias(libs.plugins.image.toolbox.maven)
}

android.namespace = "com.google.accompanist.systemuicontroller"

dependencies {
    implementation(libs.compose.ui)
    implementation(libs.coroutinesAndroid)
}