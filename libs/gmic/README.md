# G'MIC Android

Android/JNI wrapper for G'MIC 4.0.2. Kotlin code passes an Android `Bitmap` to native code and receives a `Bitmap`; pixel arrays are never exposed by the public API.

The bundled G'MIC source is used under the CeCILL-C license. Shell execution from G'MIC commands is disabled in the Android build.
