package com.t8rin.palette

/**
 * Common errors for ColorPaletteCodable
 */
sealed class CommonError(message: String) : Throwable(message) {
    class UnsupportedPaletteType : CommonError("Unsupported palette type")
    class InvalidFormat : CommonError("Invalid format")
    class InvalidASEHeader : CommonError("Invalid ASE header")
    class InvalidColorComponentCountForModelType :
        CommonError("Invalid color component count for model type")

    class UnknownBlockType : CommonError("Unknown block type")
    class GroupAlreadyOpen : CommonError("Group already open")
    class GroupNotOpen : CommonError("Group not open")
    class UnsupportedColorSpace : CommonError("Unsupported color space")
    class InvalidVersion : CommonError("Invalid version")
    class InvalidBOM : CommonError("Invalid BOM")
    class NotImplemented : CommonError("Not implemented")
    class CannotCreateColor : CommonError("Cannot create color")
    class TooFewColors : CommonError("Too few colors")
    class IndexOutOfRange : CommonError("Index out of range")

    data class UnknownColorMode(val mode: String) : CommonError("Unknown color mode: $mode")
    data class UnknownColorType(val type: Int) : CommonError("Unknown color type: $type")
    data class InvalidRGBHexString(val string: String) :
        CommonError("Invalid RGB hex string: $string")

    data class InvalidRGBAHexString(val string: String) :
        CommonError("Invalid RGBA hex string: $string")

}