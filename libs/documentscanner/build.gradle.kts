plugins {
    alias(libs.plugins.image.toolbox.library)
    alias(libs.plugins.image.toolbox.maven)
}

android.namespace = "com.websitebeaver.documentscanner"

dependencies {
    implementation(libs.opencv)
    implementation(libs.appCompat)

    implementation(projects.libs.opencvTools)
}