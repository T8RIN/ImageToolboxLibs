cmake_minimum_required(VERSION 3.22.1)

project(archive_brotli LANGUAGES C CXX)

file(GLOB BROTLI_COMMON_SOURCES CONFIGURE_DEPENDS "brotli/c/common/*.c")
file(GLOB BROTLI_DECODER_SOURCES CONFIGURE_DEPENDS "brotli/c/dec/*.c")
file(GLOB BROTLI_ENCODER_SOURCES CONFIGURE_DEPENDS "brotli/c/enc/*.c")

add_library(
        archive-brotli
        SHARED
        archive_brotli_jni.cpp
        ${BROTLI_COMMON_SOURCES}
        ${BROTLI_DECODER_SOURCES}
        ${BROTLI_ENCODER_SOURCES}
)

target_include_directories(
        archive-brotli
        PRIVATE
        brotli/c/include
)

target_compile_options(
        archive-brotli
        PRIVATE
        -Oz
        -ffunction-sections
        -fdata-sections
        -fvisibility=hidden
)
target_link_options(archive-brotli PRIVATE -Wl,--gc-sections)
set_target_properties(archive-brotli PROPERTIES CXX_STANDARD 20 CXX_STANDARD_REQUIRED ON)
