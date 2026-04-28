cmake_minimum_required(VERSION 3.4.1)

project(Advancedcrop)

add_library(
        Advancedcrop
        SHARED
        Advancedcrop.cpp
)

set(OPENJPEG_SOURCE_DIR
        ${CMAKE_CURRENT_SOURCE_DIR}/../../../../jp2decoder/src/main/cpp/openjpeg
)

set(OPENJPEG_BINARY_DIR
        ${CMAKE_BINARY_DIR}/openjpeg
)

add_compile_definitions(PNG_ARM_NEON_OPT=0)
add_compile_definitions(PNG_ARM_NEON=0)

target_include_directories(
        Advancedcrop
        PRIVATE
        ${CMAKE_CURRENT_SOURCE_DIR}
        ${OPENJPEG_SOURCE_DIR}/thirdparty/include
        ${OPENJPEG_SOURCE_DIR}/thirdparty/libpng
)

add_subdirectory(
        ${OPENJPEG_SOURCE_DIR}/thirdparty/libz
        ${CMAKE_BINARY_DIR}/libz
)

add_subdirectory(
        ${OPENJPEG_SOURCE_DIR}/thirdparty/libpng
        ${CMAKE_BINARY_DIR}/libpng
)

find_library(log-lib log)

target_link_libraries(
        Advancedcrop
        ${log-lib}
        png
        z
)

set_target_properties(
        Advancedcrop
        PROPERTIES LINK_FLAGS
        "-Wl,--build-id=none"
)