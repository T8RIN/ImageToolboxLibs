package com.t8rin.gmic

import android.graphics.Bitmap
import com.t8rin.gmic.model.GmicOptions

interface GmicFilter {
    val command: String

    val auxiliaryInputs: List<Bitmap>
        get() = emptyList()

    val options: GmicOptions
        get() = GmicOptions()

    companion object {
        operator fun invoke(
            command: String,
            options: GmicOptions = GmicOptions()
        ): GmicFilter = object : GmicFilter {
            override val command: String = command
            override val options: GmicOptions = options
        }
    }
}
