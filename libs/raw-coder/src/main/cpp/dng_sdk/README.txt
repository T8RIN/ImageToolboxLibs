Welcome to the Digital Negative Software Development Kit (DNG SDK)

This package comes with C++ source code for the DNG SDK (see
dng_sdk/source). This code has been tested using C++14 and C++17
language versions with various compilers on x64, arm32, and arm64
architectures.

This package also includes some project files for building a
command-line utility named "dng_validate" on macOS and Windows (see
dng_sdk/projects). These project files have been tested using the
following compilers:

  macOS: Xcode 12.4 thru Xcode 14.2

  Windows: Visual Studio 2022 (17.3.5) + clang

    The clang tools are available as an optional component in the
    Visual Studio installer.

Instructions to build dng_validate:

  macOS:

    - Open dng_sdk/projects/mac/dng_validate.xcodeproj

    - Select either "dng_validate debug" or "dng_validate release"
      from the Scheme dropdown menu

    - Build

  Windows:

    - Open dng_sdk/projects/win/dng_validate.sln

    - Select "Validate Debug" or "Validate Release" from the
      Configuration dropdown menu

    - Build

Dependencies:

This distribution of the SDK includes the following libraries and
additional dependencies:

  libjpeg for JPEG support
    - see JPEG_ReadMe.txt for details

  libjxl for JPEG XL support
    - see JXL_ReadMe.txt for details
    - includes brotli and highway dependencies

  XMP SDK for XMP support
    - see XMP_ReadMe.txt for details
    - includes boost, expat, and zlib dependencies
