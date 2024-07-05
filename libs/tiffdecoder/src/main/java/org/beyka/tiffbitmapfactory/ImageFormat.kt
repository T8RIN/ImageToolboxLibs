package org.beyka.tiffbitmapfactory

/**
 * Supported image formats
 */
enum class ImageFormat(val value: Int) {
    UNKNOWN(0),
    JPEG(1),
    PNG(2),
    TIFF(4),
    BMP(5)
}
