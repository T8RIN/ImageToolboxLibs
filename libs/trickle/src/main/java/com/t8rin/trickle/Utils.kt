package com.t8rin.trickle

import android.graphics.Color

object TrickleUtils {

    fun generateShades(color: Int, shadeStep: Int = 5, from: Int = 2, to: Int = 98): List<Int> {
        val shades = mutableListOf<Int>()

        val alpha = Color.alpha(color)

        val hsv = FloatArray(3)
        Color.colorToHSV(color, hsv)

        for (i in from..to step shadeStep) {
            shades.add(
                Color.HSVToColor(
                    alpha,
                    shade(hsv, i)
                )
            )
        }

        return shades
    }

    fun shade(hsv: FloatArray, tone: Int): FloatArray {
        val valueFactor = tone / 100.0f
        val shadedHsv = hsv.copyOf()
        shadedHsv[2] = valueFactor

        return shadedHsv
    }

}