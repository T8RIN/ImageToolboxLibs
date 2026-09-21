@file:Suppress("UnstableApiUsage")

import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.tasks.Sync

plugins {
    alias(libs.plugins.image.toolbox.library)
    alias(libs.plugins.image.toolbox.native)
    alias(libs.plugins.image.toolbox.maven)
}

val jxlNative = configurations.create("jxlNative")

val extractedJxlNative = layout.buildDirectory.dir("jxl-native")
val extractJxlNative = tasks.register<Sync>("extractJxlNative") {
    from({ jxlNative.files.map { zipTree(it) } }) {
        include("jni/**")
    }
    into(extractedJxlNative)
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

tasks.configureEach {
    if (name.startsWith("configureCMake") || name.startsWith("buildCMake")) {
        dependsOn(extractJxlNative)
    }
}

android {
    namespace = "com.t8rin.raw_coder"
    defaultConfig {
        minSdk = 24
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        externalNativeBuild {
            cmake {
                arguments += "-DCMAKE_BUILD_TYPE=Release"
                arguments += "-DJXL_LIB_DIR=${extractedJxlNative.get().asFile.resolve("jni").absolutePath}"
                cppFlags += listOf("-O3", "-fopenmp", "-flto", "-fvisibility=hidden")
            }
        }
    }
}

dependencies {
    api(libs.coil)
    api(libs.jxl.coder)
    jxlNative("io.github.awxkee:jxl-coder-libjxl:${libs.versions.jxlCoder.get()}:release@aar")
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.runner)
}
