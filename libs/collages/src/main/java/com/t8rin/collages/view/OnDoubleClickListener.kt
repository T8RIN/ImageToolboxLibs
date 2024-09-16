package com.photoeditor.photoeffect.multitouch

internal interface OnDoubleClickListener {
    fun onPhotoViewDoubleClick(view: PhotoView, entity: MultiTouchEntity)
    fun onBackgroundDoubleClick()
}
