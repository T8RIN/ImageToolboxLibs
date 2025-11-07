package com.t8rin.palette

/**
 * Common errors for ColorPaletteCodable
 */
sealed class CommonError(message: String) : Throwable(message) {
    class UnsupportedPaletteType : CommonError("Unsupported palette type")
    class UnableToLoadFile : CommonError("Unable to load file")
    class InvalidFormat : CommonError("Invalid format")
    class InvalidASEHeader : CommonError("Invalid ASE header")
    class InvalidColorComponentCountForModelType :
        CommonError("Invalid color component count for model type")

    class InvalidEndOfFile : CommonError("Invalid end of file")
    class InvalidString : CommonError("Invalid string")
    class InvalidIntegerValue : CommonError("Invalid integer value")
    class UnknownBlockType : CommonError("Unknown block type")
    class GroupAlreadyOpen : CommonError("Group already open")
    class GroupNotOpen : CommonError("Group not open")
    class UnsupportedCGColorType : CommonError("Unsupported CGColor type")
    class UnsupportedColorSpace : CommonError("Unsupported color space")
    class InvalidVersion : CommonError("Invalid version")
    class InvalidBOM : CommonError("Invalid BOM")
    class InvalidUnicodeFormatString : CommonError("Invalid unicode format string")
    class UnsupportedCoderType : CommonError("Unsupported coder type")
    class CannotConvertColorSpace : CommonError("Cannot convert color space")
    class MismatchedColorspace : CommonError("Mismatched colorspace")
    class CannotCreateImage : CommonError("Cannot create image")
    class NotImplemented : CommonError("Not implemented")
    class NotEnoughColorsToGenerateGradient : CommonError("Not enough colors to generate gradient")
    class CannotGenerateGradient : CommonError("Cannot generate gradient")
    class CannotCreateColor : CommonError("Cannot create color")
    class TooFewColors : CommonError("Too few colors")
    class IndexOutOfRange : CommonError("Index out of range")
    class CannotGenerateOutputZip : CommonError("Cannot generate output zip")

    data class UnknownColorMode(val mode: String) : CommonError("Unknown color mode: $mode")
    data class UnknownColorType(val type: Int) : CommonError("Unknown color type: $type")
    data class InvalidRGBHexString(val string: String) :
        CommonError("Invalid RGB hex string: $string")

    data class InvalidRGBAHexString(val string: String) :
        CommonError("Invalid RGBA hex string: $string")

    data class InvalidARGBHexString(val string: String) :
        CommonError("Invalid ARGB hex string: $string")

    data class UnsupportedColorspace(val colorspace: ColorSpace) :
        CommonError("Unsupported colorspace: $colorspace")
}

/**
 * Gradient errors
 */
sealed class GradientError(message: String) : Exception(message) {
    class CannotNormalize : GradientError("Cannot normalize gradient")
    class MismatchColorCount : GradientError("Mismatch color count")
    class UnsupportedColorFormat : GradientError("Unsupported color format")
    class NoGradients : GradientError("No gradients")
    class InvalidStringData : GradientError("Invalid string data")
    class NotEnoughStops : GradientError("Not enough stops")
    class ExpectedUnitRange : GradientError("Expected unit range")
    class InternalError : GradientError("Internal error")
}


