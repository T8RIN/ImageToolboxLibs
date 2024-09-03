# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.4.1)

# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds them for you.
# Gradle automatically packages shared libraries with your APK.

add_library( # Sets the name of the library.
        djvu-coder

        # Sets the library as a shared library.
        SHARED

        # Provides a relative path to your source file(s).
        src/main/cpp/djvu-coder.cpp
        src/main/cpp/libdjvu/Arrays.cpp
        src/main/cpp/libdjvu/atomic.cpp
        src/main/cpp/libdjvu/BSByteStream.cpp
        src/main/cpp/libdjvu/BSEncodeByteStream.cpp
        src/main/cpp/libdjvu/ByteStream.cpp
        src/main/cpp/libdjvu/DataPool.cpp
        src/main/cpp/libdjvu/ddjvuapi.cpp
        src/main/cpp/libdjvu/debug.cpp
        src/main/cpp/libdjvu/DjVmDir.cpp
        src/main/cpp/libdjvu/DjVmDir0.cpp
        src/main/cpp/libdjvu/DjVmDoc.cpp
        src/main/cpp/libdjvu/DjVmNav.cpp
        src/main/cpp/libdjvu/DjVuAnno.cpp
        src/main/cpp/libdjvu/DjVuDocEditor.cpp
        src/main/cpp/libdjvu/DjVuDocument.cpp
        src/main/cpp/libdjvu/DjVuDumpHelper.cpp
        src/main/cpp/libdjvu/DjVuErrorList.cpp
        src/main/cpp/libdjvu/DjVuFile.cpp
        src/main/cpp/libdjvu/DjVuFileCache.cpp
        src/main/cpp/libdjvu/DjVuGlobal.cpp
        src/main/cpp/libdjvu/DjVuGlobalMemory.cpp
        src/main/cpp/libdjvu/DjVuImage.cpp
        src/main/cpp/libdjvu/DjVuInfo.cpp
        src/main/cpp/libdjvu/DjVuMessage.cpp
        src/main/cpp/libdjvu/DjVuMessageLite.cpp
        src/main/cpp/libdjvu/DjVuNavDir.cpp
        src/main/cpp/libdjvu/DjVuPalette.cpp
        src/main/cpp/libdjvu/DjVuPort.cpp
        src/main/cpp/libdjvu/DjVuText.cpp
        src/main/cpp/libdjvu/DjVuToPS.cpp
        src/main/cpp/libdjvu/GBitmap.cpp
        src/main/cpp/libdjvu/GContainer.cpp
        src/main/cpp/libdjvu/GException.cpp
        src/main/cpp/libdjvu/GIFFManager.cpp
        src/main/cpp/libdjvu/GMapAreas.cpp
        src/main/cpp/libdjvu/GOS.cpp
        src/main/cpp/libdjvu/GPixmap.cpp
        src/main/cpp/libdjvu/GRect.cpp
        src/main/cpp/libdjvu/GScaler.cpp
        src/main/cpp/libdjvu/GSmartPointer.cpp
        src/main/cpp/libdjvu/GString.cpp
        src/main/cpp/libdjvu/GThreads.cpp
        src/main/cpp/libdjvu/GUnicode.cpp
        src/main/cpp/libdjvu/GURL.cpp
        src/main/cpp/libdjvu/IFFByteStream.cpp
        src/main/cpp/libdjvu/IW44EncodeCodec.cpp
        src/main/cpp/libdjvu/IW44Image.cpp
        src/main/cpp/libdjvu/JB2EncodeCodec.cpp
        src/main/cpp/libdjvu/JB2Image.cpp
        src/main/cpp/libdjvu/JPEGDecoder.cpp
        src/main/cpp/libdjvu/miniexp.cpp
        src/main/cpp/libdjvu/MMRDecoder.cpp
        src/main/cpp/libdjvu/MMX.cpp
        src/main/cpp/libdjvu/UnicodeByteStream.cpp
        src/main/cpp/libdjvu/XMLParser.cpp
        src/main/cpp/libdjvu/XMLTags.cpp
        src/main/cpp/libdjvu/ZPCodec.cpp
)

add_definitions(-DHAVE_PTHREAD)
target_compile_definitions(djvu-coder PRIVATE HAVE_PTHREAD=42)

add_definitions(-DUNIX)
target_compile_definitions(djvu-coder PRIVATE UNIX=1)

add_definitions(-DHAVE_WCHAR_H)
target_compile_definitions(djvu-coder PRIVATE HAVE_WCHAR_H=1)

add_definitions(-DHAVE_WCHAR_T)
target_compile_definitions(djvu-coder PRIVATE HAVE_WCHAR_T=0)

add_definitions(-DHAVE_DIRENT_H)
target_compile_definitions(djvu-coder PRIVATE HAVE_DIRENT_H=1)

add_definitions(-DHAVE_WCTYPE_H)
target_compile_definitions(djvu-coder PRIVATE HAVE_WCTYPE_H=1)


add_definitions(-DHAVE_MBSTATE_T)
target_compile_definitions(djvu-coder PRIVATE HAVE_MBSTATE_T=1)

# Searches for a specified prebuilt library and stores the path as a
# variable. Because CMake includes system libraries in the search path by
# default, you only need to specify the name of the public NDK library
# you want to add. CMake verifies that the library exists before
# completing its build.

find_library( # Sets the name of the path variable.
        log-lib

        # Specifies the name of the NDK library that
        # you want CMake to locate.
        log)

# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in this
# build script, prebuilt third-party libraries, or system libraries.

target_link_libraries( # Specifies the target library.
        djvu-coder

        # Links the target library to the log library
        # included in the NDK.
        ${log-lib}
        jnigraphics)

set(CMAKE_CXX_STANDARD 20)
set(CMAKE_CXX_STANDARD_REQUIRED ON)