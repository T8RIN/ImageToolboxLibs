package com.t8rin.gmic.filters

enum class OrganicFibersPalette(override val value: Int) : GmicArgument {
    Default(0),
    Hsv(1),
    Lines(2),
    Hot(3),
    Cool(4),
    Jet(5),
    Flag(6),
    Cube(7),
    Rainbow(8),
    Parula(9),
    Spring(10),
    Summer(11),
    Autumn(12),
    Winter(13),
    Bone(14),
    Copper(15),
    Pink(16),
    Vga(17),
    Algae(18),
    Amp(19),
    Balance(20),
    Curl(21),
    Deep(22),
    Delta(23),
    Dense(24),
    Diff(25),
    Gray(26),
    Haline(27),
    Ice(28),
    Matter(29),
    Oxy(30),
    Phase(31),
    Rain(32),
    Solar(33),
    Speed(34),
    Tarn(35),
    Tempo(36),
    Thermal(37),
    Topo(38),
    Turbid(39),
    Aurora(40),
    Hocuspocus(41),
    Srb2(42),
    Uzebox(43),
    Amiga7800(44),
    Amiga7800Mess(45),
    FornaxVoid1(46)
}

data class OrganicFibers(
    val agentDensity: Float = 20f,
    val iterations: Int = 20,
    val orientations: Int = 3,
    val sensorDistance: Int = 15,
    val sensorAngle: Float = 15f,
    val motionDistance: Int = 3,
    val motionAngle: Float = 15f,
    val motionMoment: Float = 30f,
    val trailBlur: Float = 1f,
    val particleSize: Int = 19,
    val particleThickness: Float = 10f,
    val quantizedOrientations: Int = 24,
    val opacity: Float = 50f,
    val sharpening: Float = 0f,
    val palette: OrganicFibersPalette = OrganicFibersPalette.Solar
) : RawGmicFilter(
    gmicCommand(
        "fx_organic_fibers",
        agentDensity.inRange("agentDensity", 0f, 50f),
        iterations.inRange("iterations", 1..256),
        orientations.inRange("orientations", 2..16),
        sensorDistance.inRange("sensorDistance", 0..400),
        sensorAngle.inRange("sensorAngle", 0f, 180f),
        motionDistance.inRange("motionDistance", 0..400),
        motionAngle.inRange("motionAngle", 0f, 180f),
        motionMoment.inRange("motionMoment", 0f, 100f),
        trailBlur.inRange("trailBlur", 0f, 20f),
        particleSize.inRange("particleSize", 1..128),
        particleThickness.inRange("particleThickness", 0f, 100f),
        quantizedOrientations.inRange("quantizedOrientations", 1..32),
        opacity.inRange("opacity", 0f, 100f),
        sharpening.inRange("sharpening", 0f, 100f),
        palette
    )
)

enum class ReactionDiffusionMode(override val value: Int) : GmicArgument {
    Monochrome(0),
    MonochromeWithAlpha(1),
    Rgb(2),
    RgbWithAlpha(3)
}

data class ReactionDiffusion(
    val iterations: Int = 5,
    val size: Float = 5f,
    val mode: ReactionDiffusionMode = ReactionDiffusionMode.Monochrome
) : RawGmicFilter(
    gmicCommand(
        "fx_reaction_diffusion",
        iterations.inRange("iterations", 1..10),
        size.inRange("size", 0f, 20f),
        mode
    ),
    gmicProcessAlpha
)

enum class RorschachMirror(override val value: Int) : GmicArgument {
    None(0),
    XAxis(1),
    YAxis(2),
    BothAxes(3)
}

enum class RorschachStencil(override val value: Int) : GmicArgument {
    BlackAndWhite(0),
    Rgb(1),
    Color(2)
}

data class Rorschach(
    val scale: Float = 3f,
    val mirror: RorschachMirror = RorschachMirror.XAxis,
    val stencil: RorschachStencil = RorschachStencil.Color
) : RawGmicFilter(
    gmicCommand(
        "fx_rorschach",
        scale.inRange("scale", 0f, 10f),
        mirror,
        stencil
    )
)

enum class TruchetType(override val value: Int) : GmicArgument {
    Straight(0),
    Curved(1)
}

enum class TruchetColorMode(override val value: Int) : GmicArgument {
    WhiteOnBlack(0),
    BlackOnWhite(1),
    WhiteOnTransparent(2),
    BlackOnTransparent(3),
    TransparentOnWhite(4),
    TransparentOnBlack(5),
    Random(6)
}

data class Truchet(
    val scale: Int = 32,
    val radius: Int = 5,
    val smoothness: Float = 1f,
    val type: TruchetType = TruchetType.Curved,
    val colorMode: TruchetColorMode = TruchetColorMode.WhiteOnBlack
) : RawGmicFilter(
    gmicCommand(
        "fx_truchet",
        scale.inRange("scale", 1..256),
        radius.inRange("radius", 1..64),
        smoothness.inRange("smoothness", 0f, 10f),
        type,
        colorMode
    ),
    gmicProcessAlpha
)
