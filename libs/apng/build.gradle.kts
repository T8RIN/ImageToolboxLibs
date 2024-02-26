plugins {
    alias(libs.plugins.image.toolbox.library)
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

android {
    namespace = "oupson.apng"

    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    implementation(libs.coil)
}