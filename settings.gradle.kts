@file:Suppress("UnstableApiUsage")

pluginManagement {
    repositories {
        includeBuild("build-logic")
        gradlePluginPortal()
        google()
        mavenCentral()
        maven { setUrl("https://jitpack.io") }
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { setUrl("https://jitpack.io") }
    }
}
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

rootProject.name = "ImageToolboxLibs"

include(":libs:gpuimage")
include(":libs:androidwm")
include(":libs:gif-converter")
include(":libs:apng")
include(":libs:jp2decoder")
include(":libs:qoi-coder")
include(":libs:awebp")
include(":libs:psd")
include(":libs:djvu-coder")
include(":libs:fast-noise")
include(":libs:histogram")
include(":libs:advanced-crop")
include(":libs:exif")
include(":libs:jhlabs")

include(":app")
