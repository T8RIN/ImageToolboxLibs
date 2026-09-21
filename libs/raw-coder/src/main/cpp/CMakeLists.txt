cmake_minimum_required(VERSION 3.22.1)
project(raw_coder LANGUAGES C CXX)

if (NOT DEFINED JXL_LIB_DIR)
    message(FATAL_ERROR "JXL_LIB_DIR must point to the extracted jxl-coder native libraries")
endif()

file(GLOB DNG_SDK_SOURCES CONFIGURE_DEPENDS
        "dng_sdk/source/*.cpp"
)
list(FILTER DNG_SDK_SOURCES EXCLUDE REGEX "/dng_validate\\.cpp$")
list(FILTER DNG_SDK_SOURCES EXCLUDE REGEX "/dng_update_meta\\.cpp$")
list(FILTER DNG_SDK_SOURCES EXCLUDE REGEX "/dng_xmp_sdk\\.cpp$")

add_library(dng_sdk STATIC ${DNG_SDK_SOURCES})
target_compile_features(dng_sdk PUBLIC cxx_std_17)
target_include_directories(dng_sdk PUBLIC
        "dng_sdk/source"
        "libjxl/include"
)
target_compile_definitions(dng_sdk PRIVATE
        qAndroid=1
        qDNGUseXMP=0
)
target_compile_options(dng_sdk PRIVATE
        -O3
        -flto
        -ffunction-sections
        -fdata-sections
        -fvisibility=hidden
        -Wno-deprecated-declarations
)
file(GLOB_RECURSE LIBRAW_SOURCES CONFIGURE_DEPENDS
        "libraw/src/*.cpp"
)

add_library(libraw STATIC ${LIBRAW_SOURCES})
target_include_directories(libraw
        PUBLIC "libraw"
        PRIVATE "dng_sdk/source" "libjxl/include"
)
target_compile_definitions(libraw PRIVATE
        USE_DNGSDK
        USE_ZLIB
        qAndroid=1
        qDNGUseXMP=0
        LIBRAW_CALLOC_RAWSTORE
        LIBRAW_MAX_PROFILE_SIZE_MB=64
        LIBRAW_MAX_THUMBNAIL_MB=128
        LIBRAW_X3F_ALLOC_LIMIT_MB=256
)
target_compile_options(libraw PRIVATE
        -O3
        -fopenmp
        -flto
        -ffunction-sections
        -fdata-sections
        -fvisibility=hidden
)

add_library(raw_coder SHARED raw_coder.cpp)
target_compile_features(raw_coder PRIVATE cxx_std_20)
target_compile_definitions(raw_coder PRIVATE
        qAndroid=1
        qDNGUseXMP=0
)
target_compile_options(raw_coder PRIVATE
        -O3
        -fopenmp
        -flto
        -ffunction-sections
        -fdata-sections
        -fvisibility=hidden
)
target_link_options(raw_coder PRIVATE
        -flto
        -fopenmp
        -static-openmp
        -Wl,--gc-sections
        -Wl,--exclude-libs,ALL
)
target_link_directories(raw_coder PRIVATE "${JXL_LIB_DIR}/${ANDROID_ABI}")
target_link_libraries(raw_coder PRIVATE
        libraw
        dng_sdk
        jxl
        jxl_threads
        z
        log
        jnigraphics
)
