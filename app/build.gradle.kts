plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
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

    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.compose.compiler.get().toString()
    }

    compileOptions {
        sourceCompatibility = javaVersion
        targetCompatibility = javaVersion
        isCoreLibraryDesugaringEnabled = true
    }
    kotlinOptions {
        jvmTarget = javaVersion.toString()
    }
}

dependencies {
    implementation(libs.androidxCore)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.compose.bom))
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

    implementation(projects.libs.cropper)
    implementation(libs.androidx.palette.ktx)
    coreLibraryDesugaring(libs.desugaring)
    implementation(libs.coil)
    implementation(libs.coil.compose)
}

val Project.javaVersion: JavaVersion
    get() = JavaVersion.toVersion(
        libs.versions.jvmTarget.get().toString()
    )