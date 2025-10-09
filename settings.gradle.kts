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

include(":libs:cropper")
include(":libs:dynamic-theme")
include(":libs:colordetector")
include(":libs:gesture")
include(":libs:beforeafter")
include(":libs:image")
include(":libs:screenshot")
include(":libs:modalsheet")
include(":libs:gpuimage")
include(":libs:colorpicker")
include(":libs:systemuicontroller")
include(":libs:placeholder")
include(":libs:zoomable")
include(":libs:extendedcolors")
include(":libs:androidwm")
include(":libs:gif-converter")
include(":libs:apng")
include(":libs:snowfall")
include(":libs:svg")
include(":libs:jp2decoder")
include(":libs:tiffdecoder")
include(":libs:qoi-coder")
include(":libs:awebp")
include(":libs:avif")
include(":libs:psd")
include(":libs:djvu-coder")
include(":libs:fast-noise")
include(":libs:collages")
include(":libs:histogram")
include(":libs:ucrop")
include(":libs:opencv-tools")
include(":libs:editbox")
include(":libs:curves")
include(":libs:exif")
include(":libs:jhlabs")
include(":libs:ascii")
include(":libs:documentscanner")
include(":libs:qrose")

include(":app")