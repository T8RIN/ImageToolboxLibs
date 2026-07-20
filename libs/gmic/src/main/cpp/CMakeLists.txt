cmake_minimum_required(VERSION 3.22.1)

project(gmic_android LANGUAGES CXX)

add_library(
        gmic-android
        SHARED
        gmic_jni.cpp
        third_party/gmic/gmic.cpp
)

target_include_directories(
        gmic-android
        PRIVATE
        third_party/gmic
)

target_compile_features(gmic-android PRIVATE cxx_std_17)

set_source_files_properties(
        third_party/gmic/gmic.cpp
        PROPERTIES
        COMPILE_DEFINITIONS gmic_core
)

target_compile_definitions(
        gmic-android
        PRIVATE
        cimg_date=""
        cimg_display=0
        cimg_time=""
        cimg_use_zlib
        gmic_noexec
)

target_compile_options(
        gmic-android
        PRIVATE
        -O3
        -fdata-sections
        -ffunction-sections
        -fvisibility=hidden
        -fvisibility-inlines-hidden
)

target_link_options(
        gmic-android
        PRIVATE
        -Wl,--build-id=none
        -Wl,--gc-sections
)

find_library(JNIGRAPHICS_LIBRARY jnigraphics REQUIRED)
find_library(LOG_LIBRARY log REQUIRED)
find_library(Z_LIBRARY z REQUIRED)

target_link_libraries(
        gmic-android
        PRIVATE
        ${JNIGRAPHICS_LIBRARY}
        ${LOG_LIBRARY}
        ${Z_LIBRARY}
)
