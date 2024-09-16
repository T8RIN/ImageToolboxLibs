package com.photoeditor.photoeffect.frame

import android.view.MotionEvent

internal interface OnFrameTouchListener {
    fun onFrameTouch(event: MotionEvent)
    fun onFrameDoubleClick(event: MotionEvent)
}
