plugins {
    alias(libs.plugins.image.toolbox.library)
    alias(libs.plugins.image.toolbox.hilt)
    alias(libs.plugins.image.toolbox.compose)
    id("maven-publish")
}

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("mavenJava") {
                from(components["release"])
            }
        }
    }
}


android.namespace = "todo"

dependencies {

    implementation(projects.libs.pickerktBase)
    implementation(projects.libs.systemuicontroller)
    implementation(projects.libs.placeholder)

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.2")

    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.0-alpha02")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.0-alpha02")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.0-alpha02")

    implementation("androidx.compose.ui:ui-tooling-preview:1.6.3")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.activity:activity-compose:1.8.2")

    implementation("androidx.recyclerview:recyclerview-selection:1.1.0")

    implementation("androidx.navigation:navigation-fragment-ktx:2.7.7")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.7")
    implementation("androidx.navigation:navigation-compose:2.7.7")

    implementation(libs.coil)
    implementation(libs.coilCompose)
    implementation(libs.coilGif)
    implementation(libs.coilSvg)
    implementation(libs.coilVideo)

    implementation("androidx.window:window:1.2.0")

    implementation("com.jakewharton.threetenabp:threetenabp:1.4.6")

    implementation("androidx.paging:paging-compose:3.2.1")

}