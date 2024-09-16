package com.photoeditor.photoeffect.utils

import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import com.photoeditor.photoeffect.frame.FrameEntity
import com.photoeditor.photoeffect.multitouch.MultiTouchEntity

class ResultContainer private constructor() {

    var imageEntities: ArrayList<MultiTouchEntity>? = ArrayList()
        private set
    var photoBackgroundImage: Uri? = null

    // frame
    private var mFrameStickerImages: ArrayList<MultiTouchEntity>? = ArrayList()
    var frameBackgroundImage: Uri? = null
    private var mFrameImages: ArrayList<FrameEntity>? = ArrayList()
    private val mDecodedImageMap = HashMap<Uri, Bitmap>()

    fun removeImageEntity(entity: MultiTouchEntity) {
        imageEntities!!.remove(entity)
    }

    fun putImageEntities(images: ArrayList<MultiTouchEntity>) {
        imageEntities!!.clear()
        for (entity in images) {
            imageEntities!!.add(entity)
        }
    }

    fun putImage(key: Uri, bitmap: Bitmap) {
        mDecodedImageMap[key] = bitmap
    }

    fun getImage(key: Uri): Bitmap? {
        return mDecodedImageMap[key]
    }

    fun copyImageEntities(): ArrayList<MultiTouchEntity> {
        val result = ArrayList<MultiTouchEntity>()
        for (entity in imageEntities!!) {
            result.add(entity)
        }

        return result
    }

    fun putFrameImage(entity: FrameEntity) {
        mFrameImages!!.add(entity)
    }

    fun copyFrameImages(): ArrayList<FrameEntity> {
        val result = ArrayList<FrameEntity>()
        for (uri in mFrameImages!!) {
            result.add(uri)
        }

        return result
    }

    fun putFrameSticker(entity: MultiTouchEntity) {
        mFrameStickerImages!!.add(entity)
    }

    fun putFrameStickerImages(images: ArrayList<MultiTouchEntity>) {
        mFrameStickerImages!!.clear()
        for (entity in images) {
            mFrameStickerImages!!.add(entity)
        }
    }

    fun copyFrameStickerImages(): ArrayList<MultiTouchEntity> {
        val result = ArrayList<MultiTouchEntity>()
        for (entity in mFrameStickerImages!!) {
            result.add(entity)
        }

        return result
    }

    fun removeFrameSticker(entity: MultiTouchEntity) {
        mFrameStickerImages!!.remove(entity)
    }

    /**
     * Clear all frame image uri
     */
    fun clearFrameImages() {
        mFrameImages!!.clear()
    }

    fun clearAll() {
        imageEntities!!.clear()
        photoBackgroundImage = null
        mFrameStickerImages!!.clear()
        mFrameImages!!.clear()
        frameBackgroundImage = null
        mDecodedImageMap.clear()
    }

    fun clearAllImageInFrameCreator() {
        mFrameStickerImages!!.clear()
        mFrameImages!!.clear()
        frameBackgroundImage = null
    }

    fun saveToBundle(bundle: Bundle) {
        bundle.putParcelableArrayList(IMAGES_KEY, imageEntities)
        bundle.putParcelable(PHOTO_BACKGROUND_IMAGE_KEY, photoBackgroundImage)
        bundle.putParcelableArrayList(
            FRAME_STICKER_IMAGES_KEY,
            mFrameStickerImages
        )
        bundle.putParcelable(FRAME_BACKGROUND_IMAGE_KEY, frameBackgroundImage)
        bundle.putParcelableArrayList(FRAME_IMAGES_KEY, mFrameImages)
    }

    fun restoreFromBundle(bundle: Bundle) {
        imageEntities = bundle.getParcelableArrayList(IMAGES_KEY)
        if (imageEntities == null) {
            imageEntities = ArrayList()
        }
        photoBackgroundImage = bundle
            .getParcelable(PHOTO_BACKGROUND_IMAGE_KEY)
        mFrameStickerImages = bundle
            .getParcelableArrayList(FRAME_STICKER_IMAGES_KEY)
        if (mFrameStickerImages == null) {
            mFrameStickerImages = ArrayList()
        }
        frameBackgroundImage = bundle
            .getParcelable(FRAME_BACKGROUND_IMAGE_KEY)
        mFrameImages = bundle.getParcelableArrayList(FRAME_IMAGES_KEY)
        if (mFrameImages == null) {
            mFrameImages = ArrayList()
        }
    }

    companion object {
        val IMAGES_KEY = "imagesKey"
        val PHOTO_BACKGROUND_IMAGE_KEY = "photoBgKey"
        val FRAME_STICKER_IMAGES_KEY = "frameStickerKey"
        val FRAME_BACKGROUND_IMAGE_KEY = "frameBackgroundKey"
        val FRAME_IMAGES_KEY = "frameImageKey"

        private var instance: ResultContainer? = null

        fun getInstance(): ResultContainer {
            if (instance == null) {
                instance = ResultContainer()
            }

            return instance as ResultContainer
        }
    }
}
