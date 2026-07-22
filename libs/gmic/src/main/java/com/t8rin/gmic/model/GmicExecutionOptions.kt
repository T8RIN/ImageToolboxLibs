package com.t8rin.gmic.model

data class GmicExecutionOptions(
    /** Optional G'MIC command definitions made available to this interpreter run. */
    val customCommands: String? = null,
    /** Maximum worker count for this run. Null uses all logical CPU threads reported by Android. */
    val maxThreads: Int? = null,
    /** Emits detailed per-stage native timing and memory diagnostics to `GmicProfile`. */
    val profilingEnabled: Boolean = false,
    /** Skips G'MIC's built-in `smooth` command, including calls made by bundled filters. */
    val disableSmooth: Boolean = true
)
