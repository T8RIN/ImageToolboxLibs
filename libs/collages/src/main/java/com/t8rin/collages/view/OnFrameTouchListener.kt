package com.t8rin.collages.view

import android.view.MotionEvent

internal interface OnFrameTouchListener {
    fun onFrameTouch(event: MotionEvent)
    fun onFrameDoubleClick(event: MotionEvent)
}
