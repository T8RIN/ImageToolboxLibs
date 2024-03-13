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

    implementation("androidx.core:core-ktx:1.12.0")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.2")

    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.activity:activity-compose:1.8.2")

    implementation("com.squareup.moshi:moshi-kotlin:1.13.0")
    kapt("com.squareup.moshi:moshi-kotlin-codegen:1.13.0")

    implementation("androidx.loader:loader:1.1.0")
    implementation("androidx.compose.ui:ui-tooling-preview:1.6.3")

    implementation("com.jakewharton.timber:timber:5.0.1")

    implementation("com.jakewharton.threetenabp:threetenabp:1.4.6")

    implementation("androidx.paging:paging-runtime:3.2.1")
}