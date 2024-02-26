package oupson.apng.exceptions

class BadCRCException : Exception()

// TODO BETTER MESSAGES
class BadApngException(override val message: String? = null) : Exception()

class InvalidFrameSizeException(animationWidth : Int, animationHeight : Int, frameWidth : Int, frameHeight : Int, isFirstFrame : Boolean) : Exception() {
    override val message: String = when {
        animationWidth != frameWidth && isFirstFrame -> {
            "Width of first frame must be equal to width of APNG ($animationWidth != $frameWidth)."
        }
        frameHeight != frameHeight && isFirstFrame -> {
            "Height of first frame must be equal to height of APNG ($animationHeight != $frameHeight)."
        }
        frameWidth > animationWidth -> {
            "Frame width must be inferior or equal at the animation width ($animationWidth < $frameWidth)."
        }
        frameHeight > animationHeight -> {
            "Frame height must be inferior or equal at the animation height ($animationHeight < $frameHeight)."
        }
        else -> {
            "Unknown problem"
        }
    }
}

class BadBitmapsDiffSize(firstBitmapWidth : Int, firstBitmapHeight : Int, secondBitmapWidth : Int, secondBitmapHeight : Int) : Exception() {
    override val message: String = "${firstBitmapWidth}x${firstBitmapHeight} must be bigger than or equal to ${secondBitmapWidth}x${secondBitmapHeight}"
}

class BadParameterException(message : String) : Exception(message)