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
        cimg_verbosity=0
        cimg_date=""
        cimg_display=0
        cimg_time=""
        cimg_use_abort
        cimg_use_zlib
        gmic_is_parallel
        gmic_noexec
)

target_compile_options(
        gmic-android
        PRIVATE
        -O3
        -fopenmp
        -fdata-sections
        -ffunction-sections
        -fvisibility=hidden
        -fvisibility-inlines-hidden
)

target_link_options(
        gmic-android
        PRIVATE
        -fopenmp
        -Wl,--gc-sections
        -Wl,--exclude-libs,ALL
)

execute_process(
        COMMAND ${CMAKE_CXX_COMPILER} -print-resource-dir
        OUTPUT_VARIABLE CLANG_RESOURCE_DIR
        OUTPUT_STRIP_TRAILING_WHITESPACE
        COMMAND_ERROR_IS_FATAL ANY
)

if (ANDROID_ABI STREQUAL "armeabi-v7a")
    set(OPENMP_ARCH_DIR arm)
elseif (ANDROID_ABI STREQUAL "arm64-v8a")
    set(OPENMP_ARCH_DIR aarch64)
elseif (ANDROID_ABI STREQUAL "x86")
    set(OPENMP_ARCH_DIR i386)
elseif (ANDROID_ABI STREQUAL "x86_64")
    set(OPENMP_ARCH_DIR x86_64)
else ()
    message(FATAL_ERROR "Unsupported Android ABI for OpenMP: ${ANDROID_ABI}")
endif ()

set(OPENMP_RUNTIME "${CLANG_RESOURCE_DIR}/lib/linux/${OPENMP_ARCH_DIR}/libomp.so")
if (NOT EXISTS "${OPENMP_RUNTIME}")
    message(FATAL_ERROR "NDK OpenMP runtime not found: ${OPENMP_RUNTIME}")
endif ()

add_library(openmp-runtime SHARED IMPORTED)
set_target_properties(openmp-runtime PROPERTIES IMPORTED_LOCATION "${OPENMP_RUNTIME}")

find_library(JNIGRAPHICS_LIBRARY jnigraphics REQUIRED)
find_library(LOG_LIBRARY log REQUIRED)
find_library(Z_LIBRARY z REQUIRED)

target_link_libraries(
        gmic-android
        PRIVATE
        ${JNIGRAPHICS_LIBRARY}
        ${LOG_LIBRARY}
        ${Z_LIBRARY}
        openmp-runtime
)
