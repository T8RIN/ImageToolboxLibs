plugins {
    alias(libs.plugins.image.toolbox.library)
    alias(libs.plugins.image.toolbox.maven)
}

android {
    namespace = "org.beyka.tiffbitmapfactory"

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