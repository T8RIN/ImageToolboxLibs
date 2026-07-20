package com.t8rin.gmic.filters

enum class CubeFaceOrientation(override val value: Int) : GmicArgument {
    Normal(0),
    MirrorX(1),
    MirrorY(2),
    MirrorXY(3)
}

data class CubeFrame(
    val depth: Float = 3f,
    val centerX: Float = 50f,
    val centerY: Float = 50f,
    val leftOrientation: CubeFaceOrientation = CubeFaceOrientation.Normal,
    val rightOrientation: CubeFaceOrientation = CubeFaceOrientation.Normal,
    val upperOrientation: CubeFaceOrientation = CubeFaceOrientation.Normal,
    val lowerOrientation: CubeFaceOrientation = CubeFaceOrientation.Normal
) : RawGmicFilter(
    gmicCommand(
        "fx_frame_cube",
        depth.inRange("depth", 0f, 30f),
        centerX.inRange("centerX", 0f, 100f),
        centerY.inRange("centerY", 0f, 100f),
        leftOrientation,
        rightOrientation,
        upperOrientation,
        lowerOrientation
    ),
    gmicProcessAlpha
)
