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
                srcDir("src/main/jni/libs")
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

    ndkVersion = "21.3.6528147"

    sourceSets.named("main") {
        jniLibs {
            srcDir("src/main/jni/libs")
        }
    }
}

dependencies {
    implementation(libs.coil)
}