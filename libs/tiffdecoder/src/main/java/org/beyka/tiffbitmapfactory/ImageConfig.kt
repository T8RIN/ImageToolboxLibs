package org.beyka.tiffbitmapfactory

enum class ImageConfig(val value: Int) {
    /**
     * Each pixel is stored on 4 bytes. Each channel (RGB and alpha
     * for translucency) is stored with 8 bits of precision (256
     * possible values.)
     *
     *
     * This configuration is very flexible and offers the best
     * quality. It should be used whenever possible.
     */
    ARGB_8888(2),

    /**
     * Each pixel is stored on 2 bytes and only the RGB channels are
     * encoded: red is stored with 5 bits of precision (32 possible
     * values), green is stored with 6 bits of precision (64 possible
     * values) and blue is stored with 5 bits of precision.
     *
     *
     * This configuration can produce slight visual artifacts depending
     * on the configuration of the source. For instance, without
     * dithering, the result might show a greenish tint. To get better
     * results dithering should be applied.
     *
     *
     * This configuration may be useful when using opaque bitmaps
     * that do not require high color fidelity.
     */
    RGB_565(4),

    /**
     * Each pixel is stored as a single translucency (alpha) channel.
     * This is very useful to efficiently store masks for instance.
     * No color information is stored.
     * With this configuration, each pixel requires 1 byte of memory.
     */
    ALPHA_8(8)
}
