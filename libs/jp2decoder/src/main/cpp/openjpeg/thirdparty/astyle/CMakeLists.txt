SET(ASTYLE_SRCS
        astyle_main.cpp
        ASBeautifier.cpp
        ASEnhancer.cpp
        ASFormatter.cpp
        ASLocalizer.cpp
        ASResource.cpp
)

if (CMAKE_VERSION VERSION_LESS "3.1")
    if (CMAKE_CXX_COMPILER_ID MATCHES "GNU" OR CMAKE_CXX_COMPILER_ID MATCHES "Clang")
        set(CMAKE_CXX_FLAGS "--std=c++11 ${CMAKE_CXX_FLAGS}")
    endif ()
else ()
    set(CMAKE_CXX_STANDARD 11)
endif ()

# No warnings for astyle build
IF (NOT MSVC)
    SET_SOURCE_FILES_PROPERTIES(${ASTYLE_SRCS} PROPERTIES COMPILE_FLAGS -w)
ENDIF (NOT MSVC)
ADD_EXECUTABLE(opjstyle ${ASTYLE_SRCS})
SET_TARGET_PROPERTIES(opjstyle PROPERTIES LINKER_LANGUAGE CXX)
SET_TARGET_PROPERTIES(opjstyle PROPERTIES RUNTIME_OUTPUT_DIRECTORY ${CMAKE_SOURCE_DIR}/scripts)

IF (CMAKE_CXX_COMPILER_ID MATCHES "Clang")
    SET(CMAKE_EXE_LINKER_FLAGS "${CMAKE_EXE_LINKER_FLAGS} -lstdc++")
ENDIF (CMAKE_CXX_COMPILER_ID MATCHES "Clang")
