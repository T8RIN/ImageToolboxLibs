# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.4.1)

# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds them for you.
# Gradle automatically packages shared libraries with your APK.

add_library( # Sets the name of the library.
        trickle

        # Sets the library as a shared library.
        SHARED

        # Provides a relative path to your source file(s).
        trickle.cpp
        # Provides a relative path to your source file(s).
        AverageSmoothFilter.cpp
        GaussianBlurFilter.cpp
        HDRFilter.cpp
        OilFilter.cpp
        SketchFilter.cpp
        TvFilter.cpp
        SharpenFilter.cpp
        GammaCorrectionFilter.cpp
        MotionBlurFilter.cpp
        BrightContrastFilter.cpp
        ColorTranslator.cpp
        HueSaturationFilter.cpp
        GothamFilter.cpp
        ColorTransfer.cpp
        ProcessingFilters.cpp
        ColorUtils.cpp
        lowpoly/lowpoly-lib.cpp
        lowpoly/sobel.c
        lowpoly/delaunay.c
        lowpoly/lowpoly.c
        lowpoly/dilution.c
        lowpoly/point.c
        lowpoly/dedup.c)

# Searches for a specified prebuilt library and stores the path as a
# variable. Because CMake includes system libraries in the search path by
# default, you only need to specify the name of the public NDK library
# you want to add. CMake verifies that the library exists before
# completing its build.

add_library(
        cairo
        STATIC
        IMPORTED
)

set_target_properties(
        cairo
        PROPERTIES
        IMPORTED_LOCATION
        ${CMAKE_HOME_DIRECTORY}/../libs/${ANDROID_ABI}/libcairo.a)

add_library(
        pixman
        STATIC
        IMPORTED
)

set_target_properties(
        pixman
        PROPERTIES
        IMPORTED_LOCATION
        ${CMAKE_HOME_DIRECTORY}/../libs/${ANDROID_ABI}/libpixman-1.a)

add_library(
        freetype
        STATIC
        IMPORTED
)

set_target_properties(
        freetype
        PROPERTIES
        IMPORTED_LOCATION
        ${CMAKE_HOME_DIRECTORY}/../libs/${ANDROID_ABI}/libfreetype.a)

add_library(
        png
        STATIC
        IMPORTED
)

set_target_properties(
        png
        PROPERTIES
        IMPORTED_LOCATION
        ${CMAKE_HOME_DIRECTORY}/../libs/${ANDROID_ABI}/libpng16.a)

include_directories(
        lowpoly/cairo/
)

# Searches for a specified prebuilt library and stores the path as a
# variable. Because system libraries are included in the search path by
# default, you only need to specify the name of the public NDK library
# you want to add. CMake verifies that the library exists before
# completing its build.

find_library( # Sets the name of the path variable.
        log-lib

        # Specifies the name of the NDK library that
        # you want CMake to locate.
        log)

# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in the
# build script, prebuilt third-party libraries, or system libraries.

find_package(ZLIB REQUIRED)


# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in this
# build script, prebuilt third-party libraries, or system libraries.

target_link_libraries( # Specifies the target library.
        trickle

        # Links the target library to the log library
        # included in the NDK.
        ${log-lib}
        jnigraphics
        cairo
        pixman
        freetype
        png
        ZLIB::ZLIB)

set_target_properties(
        trickle
        PROPERTIES LINK_FLAGS
        "-Wl,--build-id=none"
)