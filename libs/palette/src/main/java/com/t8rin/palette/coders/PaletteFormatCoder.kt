package com.t8rin.palette.coders

import com.t8rin.palette.PaletteCoder
import com.t8rin.palette.PaletteFormat
import com.t8rin.palette.getCoder

class PaletteFormatCoder(
    val paletteFormat: PaletteFormat
) : PaletteCoder by paletteFormat.getCoder()