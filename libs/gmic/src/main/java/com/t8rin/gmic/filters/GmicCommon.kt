package com.t8rin.gmic.filters

import com.t8rin.gmic.model.GmicAlphaMode
import com.t8rin.gmic.model.GmicOptions

internal val gmicProcessAlpha = GmicOptions(alphaMode = GmicAlphaMode.Process)

enum class GmicBoundary(override val value: Int) : GmicArgument {
    Transparent(0),
    Nearest(1),
    Periodic(2),
    Mirror(3)
}

enum class GmicInterpolation(override val value: Int) : GmicArgument {
    NearestNeighbor(0),
    Linear(1)
}

enum class GmicValueAction(override val value: Int) : GmicArgument {
    None(0),
    Cut(1),
    Normalize(2)
}

/** Channel layouts supported by G'MIC's `apply_channels` helper. */
enum class GmicChannel(override val value: Int) : GmicArgument {
    All(0),
    Rgba(1),
    Rgb(2),
    Red(3),
    Green(4),
    Blue(5),
    Alpha(6),
    LinearRgb(7),
    LinearRed(8),
    LinearGreen(9),
    LinearBlue(10),
    YCbCr(11),
    Luminance(12),
    Chrominance(13),
    BlueChrominance(14),
    RedChrominance(15),
    GreenChrominance(16),
    Lab(17),
    LabLightness(18),
    LabChrominance(19),
    LabA(20),
    LabB(21),
    Lch(22),
    LchChrominance(23),
    LchChroma(24),
    LchHue(25),
    Hsv(26),
    HsvHue(27),
    HsvSaturation(28),
    HsvValue(29),
    Hsi(30),
    HsiIntensity(31),
    Hsl(32),
    HslLightness(33),
    Cmyk(34),
    Cmy(35),
    Cyan(36),
    Magenta(37),
    Yellow(38),
    Key(39),
    Yiq(40),
    YiqLuma(41),
    YiqChrominance(42),
    Ryb(43),
    RybRed(44),
    RybYellow(45),
    RybBlue(46)
}
