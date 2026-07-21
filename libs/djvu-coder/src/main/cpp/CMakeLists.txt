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
        djvu-coder.cpp
        libdjvu/Arrays.cpp
        libdjvu/atomic.cpp
        libdjvu/BSByteStream.cpp
        libdjvu/BSEncodeByteStream.cpp
        libdjvu/ByteStream.cpp
        libdjvu/DataPool.cpp
        libdjvu/ddjvuapi.cpp
        libdjvu/debug.cpp
        libdjvu/DjVmDir.cpp
        libdjvu/DjVmDir0.cpp
        libdjvu/DjVmDoc.cpp
        libdjvu/DjVmNav.cpp
        libdjvu/DjVuAnno.cpp
        libdjvu/DjVuDocEditor.cpp
        libdjvu/DjVuDocument.cpp
        libdjvu/DjVuDumpHelper.cpp
        libdjvu/DjVuErrorList.cpp
        libdjvu/DjVuFile.cpp
        libdjvu/DjVuFileCache.cpp
        libdjvu/DjVuGlobal.cpp
        libdjvu/DjVuGlobalMemory.cpp
        libdjvu/DjVuImage.cpp
        libdjvu/DjVuInfo.cpp
        libdjvu/DjVuMessage.cpp
        libdjvu/DjVuMessageLite.cpp
        libdjvu/DjVuNavDir.cpp
        libdjvu/DjVuPalette.cpp
        libdjvu/DjVuPort.cpp
        libdjvu/DjVuText.cpp
        libdjvu/DjVuToPS.cpp
        libdjvu/GBitmap.cpp
        libdjvu/GContainer.cpp
        libdjvu/GException.cpp
        libdjvu/GIFFManager.cpp
        libdjvu/GMapAreas.cpp
        libdjvu/GOS.cpp
        libdjvu/GPixmap.cpp
        libdjvu/GRect.cpp
        libdjvu/GScaler.cpp
        libdjvu/GSmartPointer.cpp
        libdjvu/GString.cpp
        libdjvu/GThreads.cpp
        libdjvu/GUnicode.cpp
        libdjvu/GURL.cpp
        libdjvu/IFFByteStream.cpp
        libdjvu/IW44EncodeCodec.cpp
        libdjvu/IW44Image.cpp
        libdjvu/JB2EncodeCodec.cpp
        libdjvu/JB2Image.cpp
        libdjvu/JPEGDecoder.cpp
        libdjvu/miniexp.cpp
        libdjvu/MMRDecoder.cpp
        libdjvu/MMX.cpp
        libdjvu/UnicodeByteStream.cpp
        libdjvu/XMLParser.cpp
        libdjvu/XMLTags.cpp
        libdjvu/ZPCodec.cpp
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