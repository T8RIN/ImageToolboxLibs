cmake_minimum_required(VERSION 3.22.1)
project(raw_coder LANGUAGES C CXX)

file(GLOB_RECURSE LIBRAW_SOURCES CONFIGURE_DEPENDS
        "libraw/src/*.cpp"
)

add_library(libraw STATIC ${LIBRAW_SOURCES})
target_include_directories(libraw PUBLIC "libraw")
target_compile_definitions(libraw PRIVATE
        USE_ZLIB
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
target_link_libraries(raw_coder PRIVATE libraw z log jnigraphics)