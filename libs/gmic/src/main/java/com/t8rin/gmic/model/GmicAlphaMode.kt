package com.t8rin.gmic.model

/** Defines how the alpha channel is passed through a G'MIC pipeline. */
enum class GmicAlphaMode {
    /** Keeps source alpha unchanged. Intended for filters that do not change image geometry. */
    Preserve,

    /** Passes alpha to G'MIC as the fourth channel. Use for geometric transformations. */
    Process
}
