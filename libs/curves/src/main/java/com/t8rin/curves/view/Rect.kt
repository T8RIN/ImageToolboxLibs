package com.t8rin.curves.view

internal class Rect {
    @JvmField
    var x: Float = 0f

    @JvmField
    var y: Float = 0f

    @JvmField
    var width: Float = 0f

    @JvmField
    var height: Float = 0f

    constructor()

    constructor(x: Float, y: Float, width: Float, height: Float) {
        this.x = x
        this.y = y
        this.width = width
        this.height = height
    }
}