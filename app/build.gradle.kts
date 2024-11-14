import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.t8rin.imagetoolbox.app"
    compileSdk = libs.versions.androidCompileSdk.get().toIntOrNull()

    defaultConfig {
        applicationId = "com.t8rin.imagetoolbox.app"
        minSdk = libs.versions.androidMinSdk.get().toIntOrNull()
        targetSdk = libs.versions.androidTargetSdk.get().toIntOrNull()
        versionCode = 1
        versionName = libs.versions.libVersion.get()
    }
    buildFeatures {
        compose = true
    }

    compileOptions {
        sourceCompatibility = javaVersion
        targetCompatibility = javaVersion
        isCoreLibraryDesugaringEnabled = true
    }
    kotlin {
        compilerOptions {
            jvmTarget = JvmTarget.fromTarget(libs.versions.jvmTarget.get())
        }
    }
}

dependencies {
    implementation(libs.androidxCore)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(projects.libs.dynamicTheme)
    implementation(projects.libs.androidwm)
    implementation(projects.libs.awebp)
    implementation(projects.libs.qoiCoder)
    implementation(projects.libs.tiffdecoder)
    implementation(projects.libs.systemuicontroller)
    implementation(projects.libs.jp2decoder)
    implementation(projects.libs.psd)
    implementation(projects.libs.djvuCoder)
    implementation(projects.libs.fastNoise)
    implementation(projects.libs.collages)
    implementation(projects.libs.histogram)
    implementation(projects.libs.ucrop)
    implementation(projects.libs.opencvTools)

    implementation(projects.libs.cropper)
    implementation(libs.androidx.palette.ktx)
    implementation(projects.libs.curves)
    implementation(projects.libs.avif)
    coreLibraryDesugaring(libs.desugaring)
    implementation(libs.coil)
    implementation(libs.coil.network)
    implementation(libs.ktor)
    implementation(libs.coil.compose)
}

val Project.javaVersion: JavaVersion
    get() = JavaVersion.toVersion(
        libs.versions.jvmTarget.get()
    )