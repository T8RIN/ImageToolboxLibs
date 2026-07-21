# G'MIC for Android

Android/JNI wrapper for G'MIC 4.0.2. The public API accepts an Android `Bitmap`, executes the complete command string in one native processing call, and returns a `Bitmap`. It does not expose Kotlin pixel arrays or resize the source image.

Use the blocking API only from an existing worker thread:

```kotlin
val result = Gmic.run(bitmap, "polygonize 100,10 rotate 20")
```

UI and preview code should use the cancellation-aware API. Cancelling its coroutine sets G'MIC's native abort flag:

```kotlin
val result = Gmic.runCancellable(bitmap, filter)
```

Both APIs use the same pipeline:

```text
Bitmap -> one RGBA-to-G'MIC conversion -> complete command -> one direct G'MIC-to-Bitmap conversion
```

There is no wrapper limit on the number of sequential commands. Downscaling, if desired for an interactive preview, belongs in the preview image loader; export and normal library calls always process the supplied resolution.

The native layer parses the bundled standard library once into an immutable prototype. Isolated interpreters are reset from that prototype and reused; custom command prototypes are cached separately without reparsing the standard library.

Custom command definitions can be supplied for one run or configured as the default:

```kotlin
val result = Gmic.runCancellable(
    input = bitmap,
    command = "my_filter",
    options = GmicOptions(),
    executionOptions = GmicExecutionOptions(customCommands = "my_filter : blur 2")
)

Gmic.setCustomCommands("my_filter : blur 2")
```

The NDK OpenMP runtime is packaged as one shared `libomp.so` for every supported ABI, and G'MIC's pthread-backed `parallel`/`apc` commands are enabled. Keeping OpenMP shared avoids the fatal duplicate-runtime initialization that occurs when multiple native components load statically embedded copies in one app process. By default a single run may use all logical CPU threads reported by Android. `GmicExecutionOptions.maxThreads` sets a hard per-run cap, including nested `_cpus` assignments and parallel branches; a process-wide admission budget prevents independent runs from oversubscribing the device. Android joins the deferred `parallel 0` form at its command boundary so multiple asynchronous groups cannot bypass that hard cap; the commands inside each group still run in parallel.

Two default all-core runs therefore queue instead of competing for the same CPUs and full-resolution working memory. Set an explicit lower `maxThreads` on independent background runs when concurrent execution is preferred; cancellation remains active while a run waits for admission.

Set `GmicExecutionOptions(profilingEnabled = true)` to emit structured `GmicProfile` logcat entries. They include the full command, dimensions, pixel/channel and top-level operation counts, every bridge/runtime/parse/execute/copy/cleanup stage, known bridge copy/allocation counts, sampled process-native heap, G'MIC parallel-branch activity, runtime reuse counters, stdlib/custom parse counters, and the resolved thread count. Cancelled and failed runs emit the completed stage timings plus the interrupted stage and elapsed time. G'MIC's internal temporary allocations are reflected in the heap samples but are not counted as bridge allocations.

With preserved alpha, the native bridge needs a 3-channel float input and an ARGB output, or 16 bytes per pixel in known bridge allocations. The caller-owned input Bitmap adds another 4 bytes per pixel. An 8000x6000 run therefore has a roughly 960 MB integration floor before filter-specific G'MIC scratch images; complex full-resolution filters can require several additional float images and may exceed the memory available on low-RAM devices. The wrapper does not silently downscale or tile such commands because that would change their result.

The bundled G'MIC source is used under the CeCILL-C license. Shell execution from G'MIC commands is disabled in the Android build.
