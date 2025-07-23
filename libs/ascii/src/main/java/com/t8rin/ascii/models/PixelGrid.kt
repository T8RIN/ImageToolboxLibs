package com.t8rin.ascii.models

class PixelGrid(width: Int, height: Int, length: Int) {

    var width: Int = width
        private set

    var height: Int = height
        private set

    var pixelCount: Int = length
        private set

    var blocks: Array<Array<Int?>> = Array(width) { arrayOfNulls(height) }

    fun add(pixel: Int, row: Int, column: Int) {
        blocks[row][column] = pixel
    }

    init {
        require(width > 0 && height > 0) { "Width and height must be positive values." }
        blocks = Array(width) { arrayOfNulls(height) }
        pixelCount = length
    }
}