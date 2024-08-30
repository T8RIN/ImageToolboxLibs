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

include(":cropper")
include(":dynamic-theme")
include(":colordetector")
include(":gesture")
include(":beforeafter")
include(":image")
include(":screenshot")
include(":modalsheet")
include(":gpuimage")
include(":colorpicker")
include(":systemuicontroller")
include(":placeholder")
include(":logger")
include(":zoomable")
include(":extendedcolors")
include(":androidwm")
include(":gif-converter")
include(":apng")
include(":snowfall")
include(":svg")
include(":jp2decoder")
include(":tiffdecoder")
include(":qoi-coder")
include(":awebp")
include(":avif")
include(":psd")

include(":app")