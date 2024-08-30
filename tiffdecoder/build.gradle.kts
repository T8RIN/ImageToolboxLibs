plugins {
    alias(libs.plugins.image.toolbox.library)
    alias(libs.plugins.image.toolbox.maven)
}

android {
    namespace = "org.beyka.tiffbitmapfactory"

    val softwareName = "\"" + project.name + "-" + libs.versions.libVersion.get() + "\""

    buildFeatures {
        buildConfig = true
    }
    defaultConfig {
        sourceSets.named("main") {
            jniLibs {
                srcDir("src/main/libs")
            }
        }
    }

    buildTypes {
        release {
            buildConfigField("String", "softwarename", softwareName)
        }
        debug {
            buildConfigField("String", "softwarename", softwareName)
        }
    }

    ndkVersion = "26.1.10909125"

    sourceSets.named("main") {
        jniLibs {
            srcDir("src/main/libs")
        }
    }
}

dependencies {
    implementation(libs.coil)
}