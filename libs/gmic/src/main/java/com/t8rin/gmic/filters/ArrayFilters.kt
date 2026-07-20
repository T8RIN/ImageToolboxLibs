package com.t8rin.gmic.filters

import android.graphics.Color
import androidx.annotation.ColorInt

enum class ArrayMirror(override val value: Int) : GmicArgument {
    None(0),
    XAxis(1),
    YAxis(2),
    BothAxes(3)
}

enum class ArraySizing(override val value: Int) : GmicArgument {
    Shrink(0),
    Expand(1),
    Repeat(2)
}

data class FadedArray(
    val xTiles: Int = 2,
    val yTiles: Int = 2,
    val xOffset: Float = 0f,
    val yOffset: Float = 0f,
    val fadeStart: Float = 80f,
    val fadeEnd: Float = 90f,
    val mirror: ArrayMirror = ArrayMirror.None,
    val sizing: ArraySizing = ArraySizing.Shrink
) : RawGmicFilter(
    gmicCommand(
        "fx_array_fade",
        xTiles.inRange("xTiles", 1..10),
        yTiles.inRange("yTiles", 1..10),
        xOffset.inRange("xOffset", 0f, 100f),
        yOffset.inRange("yOffset", 0f, 100f),
        fadeStart.inRange("fadeStart", 1f, 100f),
        fadeEnd.inRange("fadeEnd", 1f, 100f),
        mirror,
        sizing
    )
)

enum class MirroredArrayMode(override val value: Int) : GmicArgument {
    XAxis(0),
    YAxis(1),
    BothAxes(2),
    DoubleBothAxes(3)
}

enum class ArrayInitialization(override val value: Int) : GmicArgument {
    Original(0),
    MirrorX(1),
    MirrorY(2),
    Rotate90(3),
    Rotate180(4),
    Rotate270(5)
}

data class MirroredArray(
    val iterations: Int = 1,
    val xOffset: Float = 0f,
    val yOffset: Float = 0f,
    val mode: MirroredArrayMode = MirroredArrayMode.BothAxes,
    val initialization: ArrayInitialization = ArrayInitialization.Original,
    val expandSize: Boolean = false,
    val crop: Int = 0
) : RawGmicFilter(
    gmicCommand(
        "fx_array_mirror",
        iterations.inRange("iterations", 1..10),
        xOffset.inRange("xOffset", 0f, 100f),
        yOffset.inRange("yOffset", 0f, 100f),
        mode,
        initialization,
        expandSize,
        crop.inRange("crop", 0..100)
    )
)

data class RandomPatchArray(
    val sourceXTiles: Int = 5,
    val sourceYTiles: Int = 5,
    val destinationXTiles: Int = 7,
    val destinationYTiles: Int = 7,
    val randomSeed: Int = 0
) : RawGmicFilter(
    gmicCommand(
        "fx_array_random",
        sourceXTiles.inRange("sourceXTiles", 1..128),
        sourceYTiles.inRange("sourceYTiles", 1..128),
        destinationXTiles.inRange("destinationXTiles", 1..128),
        destinationYTiles.inRange("destinationYTiles", 1..128),
        randomSeed.inRange("randomSeed", 0..65535)
    )
)

data class RandomColorArray(
    val xTiles: Int = 5,
    val yTiles: Int = 5,
    val opacity: Float = 0.5f
) : RawGmicFilter(
    gmicCommand(
        "fx_array_color",
        xTiles.inRange("xTiles", 1..20),
        yTiles.inRange("yTiles", 1..20),
        opacity.inRange("opacity", 0f, 1f)
    )
)

data class ChessboardOverlay(
    val firstSize: Int = 64,
    val secondSize: Int = 64,
    val firstOffset: Int = 0,
    val secondOffset: Int = 0,
    val angle: Float = 0f,
    val opacity: Float = 0.25f,
    @param:ColorInt val firstColor: Int = Color.BLACK,
    @param:ColorInt val secondColor: Int = Color.WHITE
) : RawGmicFilter(
    gmicCommand(
        "fx_chessboard",
        firstSize.inRange("firstSize", 1..512),
        secondSize.inRange("secondSize", 1..512),
        firstOffset.inRange("firstOffset", 0..512),
        secondOffset.inRange("secondOffset", 0..512),
        angle.inRange("angle", 0f, 180f),
        opacity.inRange("opacity", 0f, 1f),
        firstColor.gmicRed(),
        firstColor.gmicGreen(),
        firstColor.gmicBlue(),
        firstColor.gmicAlpha(),
        secondColor.gmicRed(),
        secondColor.gmicGreen(),
        secondColor.gmicBlue(),
        secondColor.gmicAlpha()
    ),
    gmicProcessAlpha
)

enum class DiceColorModel(override val value: Int) : GmicArgument {
    BlackDice(0),
    WhiteDice(1),
    ColoredNumbers(2),
    ColoredSides(3)
}

data class DiceMosaic(
    val resolution: Float = 2f,
    val size: Int = 24,
    val colorModel: DiceColorModel = DiceColorModel.WhiteDice
) : RawGmicFilter(
    gmicCommand(
        "fx_dices",
        resolution.inRange("resolution", 1f, 10f),
        size.inRange("size", 8..64),
        colorModel
    )
)

data class Ministeck(
    val colors: Int = 8,
    val resolution: Int = 64,
    val pieceSize: Int = 8,
    val pieceComplexity: Int = 2,
    val reliefAmplitude: Float = 100f,
    val reliefSize: Float = 0.3f,
    val outline: Boolean = false
) : RawGmicFilter(
    gmicCommand(
        "fx_ministeck",
        colors.inRange("colors", 2..24),
        resolution.inRange("resolution", 16..256),
        pieceSize.inRange("pieceSize", 1..64),
        pieceComplexity.inRange("pieceComplexity", 1..10),
        reliefAmplitude.inRange("reliefAmplitude", 0f, 256f),
        reliefSize.inRange("reliefSize", 0f, 1f),
        outline
    )
)

enum class PatchShuffleMode(override val value: Int) : GmicArgument {
    Shuffle(0),
    Rotate(1),
    ShuffleAndRotate(2)
}

enum class PatchReconstruction(override val value: Int) : GmicArgument {
    Colors(0),
    Gradients(1),
    Laplacians(2)
}

data class ShufflePatches(
    val mode: PatchShuffleMode = PatchShuffleMode.Shuffle,
    val reconstruction: PatchReconstruction = PatchReconstruction.Colors,
    val patchSize: Int = 64,
    val overlap: Float = 0f,
    val overlapDeviation: Float = 20f,
    val randomSeed: Int = 0
) : RawGmicFilter(
    gmicCommand(
        "fx_shuffle_patches",
        mode,
        reconstruction,
        patchSize.inRange("patchSize", 4..512),
        overlap.inRange("overlap", 0f, 50f),
        overlapDeviation.inRange("overlapDeviation", 0f, 100f),
        randomSeed.inRange("randomSeed", 0..65535)
    )
)

enum class RemovedTaquinTile(override val value: Int) : GmicArgument {
    None(0),
    First(1),
    Last(2),
    Random(3)
}

data class Taquin(
    val xTiles: Int = 7,
    val yTiles: Int = 7,
    val removedTile: RemovedTaquinTile = RemovedTaquinTile.None,
    val relief: Float = 50f,
    val borderThickness: Float = 5f,
    val borderOutline: Int = 0,
    @param:ColorInt val outlineColor: Int = Color.BLACK,
    val randomSeed: Int = 0
) : RawGmicFilter(
    gmicCommand(
        "fx_taquin",
        xTiles.inRange("xTiles", 1..20),
        yTiles.inRange("yTiles", 1..20),
        removedTile,
        relief.inRange("relief", 0f, 255f),
        borderThickness.inRange("borderThickness", 0f, 100f),
        borderOutline.inRange("borderOutline", 0..16),
        outlineColor.gmicRed(),
        outlineColor.gmicGreen(),
        outlineColor.gmicBlue(),
        outlineColor.gmicAlpha(),
        randomSeed.inRange("randomSeed", 0..65535)
    ),
    gmicProcessAlpha
)

enum class TileableArrayMode(override val value: Int) : GmicArgument {
    None(0),
    XAxis(1),
    YAxis(2),
    BothAxes(3),
    DoubleBothAxes(4)
}

data class TileableRotation(
    val angle: Float = 45f,
    val maximumSizeFactor: Int = 8,
    val arrayMode: TileableArrayMode = TileableArrayMode.None
) : RawGmicFilter(
    gmicCommand(
        "fx_rotate_tileable",
        angle.inRange("angle", 0f, 360f),
        maximumSizeFactor.inRange("maximumSizeFactor", 0..20),
        arrayMode
    )
)

data class MakeSeamless(
    val equalizeLight: Float = 0f
) : RawGmicFilter(
    gmicCommand("fx_make_seamless", equalizeLight.inRange("equalizeLight", 0f, 100f))
)
