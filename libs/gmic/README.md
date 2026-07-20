# G'MIC Android

Android/JNI wrapper for G'MIC 4.0.2. Kotlin code passes an Android `Bitmap` to native code and receives a `Bitmap`; pixel arrays are never exposed by the public API.

```kotlin
val blurred = Gmic.apply(bitmap, GmicFilters.Blur(radius = 4f))
val custom = Gmic.run(bitmap, "sepia blur 0.4 sharpen 80")
```

Add filters by implementing `GmicFilter`. Use `GmicAlphaMode.Process` for commands that transform image geometry. `GmicAlphaMode.Preserve`, the default, is intended for same-size color and filtering pipelines.

The bundled G'MIC source is used under the CeCILL-C license. Shell execution from G'MIC commands is disabled in the Android build.
