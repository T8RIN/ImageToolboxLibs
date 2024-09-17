@file:Suppress("FunctionName")

package com.t8rin.collages.utils

import android.graphics.PointF
import com.t8rin.collages.model.TemplateItem
import com.t8rin.collages.view.PhotoItem

/**
 * Created by admin on 5/8/2016.
 */
internal object TwoFrameImage {
    fun collage_2_0(): TemplateItem {
        val item = FrameImageUtils.collage("collage_2_0.png")
        //first frame
        var photoItem = PhotoItem()
        photoItem.index = 0
        photoItem.bound.set(0f, 0f, 0.5f, 1f)
        photoItem.pointList.add(PointF(0f, 0f))
        photoItem.pointList.add(PointF(1f, 0f))
        photoItem.pointList.add(PointF(1f, 1f))
        photoItem.pointList.add(PointF(0f, 1f))
        item.photoItemList.add(photoItem)
        //second frame
        photoItem = PhotoItem()
        photoItem.index = 1
        photoItem.bound.set(0.5f, 0f, 1f, 1f)
        photoItem.pointList.add(PointF(0f, 0f))
        photoItem.pointList.add(PointF(1f, 0f))
        photoItem.pointList.add(PointF(1f, 1f))
        photoItem.pointList.add(PointF(0f, 1f))
        item.photoItemList.add(photoItem)
        return item
    }

    fun collage_2_1(): TemplateItem {
        val item = FrameImageUtils.collage("collage_2_1.png")
        //first frame
        var photoItem = PhotoItem()
        photoItem.index = 0
        photoItem.bound.set(0f, 0f, 1f, 0.5f)
        photoItem.pointList.add(PointF(0f, 0f))
        photoItem.pointList.add(PointF(1f, 0f))
        photoItem.pointList.add(PointF(1f, 1f))
        photoItem.pointList.add(PointF(0f, 1f))
        item.photoItemList.add(photoItem)
        //second frame
        photoItem = PhotoItem()
        photoItem.index = 1
        photoItem.bound.set(0f, 0.5f, 1f, 1f)
        photoItem.pointList.add(PointF(0f, 0f))
        photoItem.pointList.add(PointF(1f, 0f))
        photoItem.pointList.add(PointF(1f, 1f))
        photoItem.pointList.add(PointF(0f, 1f))
        item.photoItemList.add(photoItem)
        return item
    }

    fun collage_2_2(): TemplateItem {
        val item = FrameImageUtils.collage("collage_2_2.png")
        //first frame
        val photoItem1 = PhotoItem()
        photoItem1.index = 0
        photoItem1.bound.set(0f, 0f, 1f, 0.333f)
        photoItem1.pointList.add(PointF(0f, 0f))
        photoItem1.pointList.add(PointF(1f, 0f))
        photoItem1.pointList.add(PointF(1f, 1f))
        photoItem1.pointList.add(PointF(0f, 1f))
        item.photoItemList.add(photoItem1)
        //second frame
        val photoItem2 = PhotoItem()
        photoItem2.index = 1
        photoItem2.bound.set(0f, 0.333f, 1f, 1f)
        photoItem2.pointList.add(PointF(0f, 0f))
        photoItem2.pointList.add(PointF(1f, 0f))
        photoItem2.pointList.add(PointF(1f, 1f))
        photoItem2.pointList.add(PointF(0f, 1f))
        item.photoItemList.add(photoItem2)
        return item
    }

    fun collage_2_3(): TemplateItem {
        val item = FrameImageUtils.collage("collage_2_3.png")
        //first frame
        val photoItem1 = PhotoItem()
        photoItem1.index = 0
        photoItem1.bound.set(0f, 0f, 1f, 0.667f)
        photoItem1.pointList.add(PointF(0f, 0f))
        photoItem1.pointList.add(PointF(1f, 0f))
        photoItem1.pointList.add(PointF(1f, 0.5f))
        photoItem1.pointList.add(PointF(0f, 1f))
        item.photoItemList.add(photoItem1)
        //second frame
        val photoItem2 = PhotoItem()
        photoItem2.index = 1
        photoItem2.bound.set(0f, 0.333f, 1f, 1f)
        photoItem2.pointList.add(PointF(0f, 0.5f))
        photoItem2.pointList.add(PointF(1f, 0f))
        photoItem2.pointList.add(PointF(1f, 1f))
        photoItem2.pointList.add(PointF(0f, 1f))
        item.photoItemList.add(photoItem2)
        return item
    }

    fun collage_2_4(): TemplateItem {
        val item = FrameImageUtils.collage("collage_2_4.png")
        //first frame
        //first frame
        var photoItem = PhotoItem()
        photoItem.index = 0
        photoItem.bound.set(0f, 0f, 1f, 0.5714f)
        photoItem.pointList.add(PointF(0f, 0f))
        photoItem.pointList.add(PointF(1f, 0f))
        photoItem.pointList.add(PointF(1f, 1f))
        photoItem.pointList.add(PointF(0.8333f, 0.75f))
        photoItem.pointList.add(PointF(0.6666f, 1f))
        photoItem.pointList.add(PointF(0.5f, 0.75f))
        photoItem.pointList.add(PointF(0.3333f, 1f))
        photoItem.pointList.add(PointF(0.1666f, 0.75f))
        photoItem.pointList.add(PointF(0f, 1f))
        item.photoItemList.add(photoItem)
        //second frame
        photoItem = PhotoItem()
        photoItem.index = 1
        photoItem.bound.set(0f, 0.4286f, 1f, 1f)
        photoItem.pointList.add(PointF(0f, 0.25f))
        photoItem.pointList.add(PointF(0.1666f, 0f))
        photoItem.pointList.add(PointF(0.3333f, 0.25f))
        photoItem.pointList.add(PointF(0.5f, 0f))
        photoItem.pointList.add(PointF(0.6666f, 0.25f))
        photoItem.pointList.add(PointF(0.8333f, 0f))
        photoItem.pointList.add(PointF(1f, 0.25f))
        photoItem.pointList.add(PointF(1f, 1f))
        photoItem.pointList.add(PointF(0f, 1f))
        item.photoItemList.add(photoItem)
        return item
    }

    fun collage_2_5(): TemplateItem {
        val item = FrameImageUtils.collage("collage_2_5.png")
        //first frame
        var photoItem = PhotoItem()
        photoItem.index = 0
        photoItem.bound.set(0f, 0f, 1f, 0.6667f)
        photoItem.pointList.add(PointF(0f, 0f))
        photoItem.pointList.add(PointF(1f, 0f))
        photoItem.pointList.add(PointF(1f, 1f))
        photoItem.pointList.add(PointF(0f, 1f))
        item.photoItemList.add(photoItem)
        //second frame
        photoItem = PhotoItem()
        photoItem.index = 1
        photoItem.bound.set(0f, 0.6667f, 1f, 1f)
        photoItem.pointList.add(PointF(0f, 0f))
        photoItem.pointList.add(PointF(1f, 0f))
        photoItem.pointList.add(PointF(1f, 1f))
        photoItem.pointList.add(PointF(0f, 1f))
        item.photoItemList.add(photoItem)
        return item
    }

    fun collage_2_6(): TemplateItem {
        val item = FrameImageUtils.collage("collage_2_6.png")
        //first frame
        val photoItem1 = PhotoItem()
        photoItem1.index = 0
        photoItem1.bound.set(0f, 0f, 1f, 0.667f)
        photoItem1.pointList.add(PointF(0f, 0f))
        photoItem1.pointList.add(PointF(1f, 0f))
        photoItem1.pointList.add(PointF(1f, 1f))
        photoItem1.pointList.add(PointF(0f, 0.5f))
        item.photoItemList.add(photoItem1)
        //second frame
        val photoItem2 = PhotoItem()
        photoItem2.index = 1
        photoItem2.bound.set(0f, 0.333f, 1f, 1f)
        photoItem2.pointList.add(PointF(0f, 0f))
        photoItem2.pointList.add(PointF(1f, 0.5f))
        photoItem2.pointList.add(PointF(1f, 1f))
        photoItem2.pointList.add(PointF(0f, 1f))
        item.photoItemList.add(photoItem2)
        return item
    }

    fun collage_2_7(): TemplateItem {
        val item = FrameImageUtils.collage("collage_2_7.png")
        //first frame
        val photoItem1 = PhotoItem()
        photoItem1.index = 0
        photoItem1.bound.set(0f, 0f, 1f, 1f)
        photoItem1.pointList.add(PointF(0f, 0f))
        photoItem1.pointList.add(PointF(1f, 0f))
        photoItem1.pointList.add(PointF(1f, 1f))
        photoItem1.pointList.add(PointF(0f, 1f))
        //clear area
        photoItem1.clearAreaPoints = ArrayList()
        photoItem1.clearAreaPoints!!.add(PointF(0.6f, 0.6f))
        photoItem1.clearAreaPoints!!.add(PointF(0.9f, 0.6f))
        photoItem1.clearAreaPoints!!.add(PointF(0.9f, 0.9f))
        photoItem1.clearAreaPoints!!.add(PointF(0.6f, 0.9f))
        item.photoItemList.add(photoItem1)
        //second frame
        val photoItem2 = PhotoItem()
        photoItem2.index = 1
        //photoItem2.hasBackground = true;
        photoItem2.bound.set(0.6f, 0.6f, 0.9f, 0.9f)
        photoItem2.pointList.add(PointF(0f, 0f))
        photoItem2.pointList.add(PointF(1f, 0f))
        photoItem2.pointList.add(PointF(1f, 1f))
        photoItem2.pointList.add(PointF(0f, 1f))
        item.photoItemList.add(photoItem2)
        return item
    }

    fun collage_2_8(): TemplateItem {
        val item = FrameImageUtils.collage("collage_2_8.png")
        //first frame
        var photoItem = PhotoItem()
        photoItem.index = 0
        photoItem.bound.set(0f, 0f, 0.3333f, 1f)
        photoItem.pointList.add(PointF(0f, 0f))
        photoItem.pointList.add(PointF(1f, 0f))
        photoItem.pointList.add(PointF(1f, 1f))
        photoItem.pointList.add(PointF(0f, 1f))
        item.photoItemList.add(photoItem)
        //second frame
        photoItem = PhotoItem()
        photoItem.index = 1
        photoItem.bound.set(0.3333f, 0f, 1f, 1f)
        photoItem.pointList.add(PointF(0f, 0f))
        photoItem.pointList.add(PointF(1f, 0f))
        photoItem.pointList.add(PointF(1f, 1f))
        photoItem.pointList.add(PointF(0f, 1f))
        item.photoItemList.add(photoItem)
        return item
    }

    fun collage_2_9(): TemplateItem {
        val item = FrameImageUtils.collage("collage_2_9.png")
        //first frame
        val photoItem1 = PhotoItem()
        photoItem1.index = 0
        photoItem1.bound.set(0f, 0f, 0.6667f, 1f)
        photoItem1.pointList.add(PointF(0f, 0f))
        photoItem1.pointList.add(PointF(0.5f, 0f))
        photoItem1.pointList.add(PointF(1f, 1f))
        photoItem1.pointList.add(PointF(0f, 1f))
        item.photoItemList.add(photoItem1)
        //second frame
        val photoItem2 = PhotoItem()
        photoItem2.index = 1
        photoItem2.bound.set(0.3333f, 0f, 1f, 1f)
        photoItem2.pointList.add(PointF(0f, 0f))
        photoItem2.pointList.add(PointF(1f, 0f))
        photoItem2.pointList.add(PointF(1f, 1f))
        photoItem2.pointList.add(PointF(0.5f, 1f))
        item.photoItemList.add(photoItem2)
        return item
    }

    fun collage_2_10(): TemplateItem {
        val item = FrameImageUtils.collage("collage_2_10.png")
        //first frame
        val photoItem1 = PhotoItem()
        photoItem1.index = 0
        photoItem1.bound.set(0f, 0f, 0.667f, 1f)
        photoItem1.pointList.add(PointF(0f, 0f))
        photoItem1.pointList.add(PointF(1f, 0f))
        photoItem1.pointList.add(PointF(1f, 1f))
        photoItem1.pointList.add(PointF(0f, 1f))
        item.photoItemList.add(photoItem1)
        //second frame
        val photoItem2 = PhotoItem()
        photoItem2.index = 1
        photoItem2.bound.set(0.667f, 0f, 1f, 1f)
        photoItem2.pointList.add(PointF(0f, 0f))
        photoItem2.pointList.add(PointF(1f, 0f))
        photoItem2.pointList.add(PointF(1f, 1f))
        photoItem2.pointList.add(PointF(0f, 1f))
        item.photoItemList.add(photoItem2)
        return item
    }

    fun collage_2_11(): TemplateItem {
        val item = FrameImageUtils.collage("collage_2_11.png")
        //first frame
        val photoItem1 = PhotoItem()
        photoItem1.index = 0
        photoItem1.bound.set(0f, 0f, 0.667f, 1f)
        photoItem1.pointList.add(PointF(0f, 0f))
        photoItem1.pointList.add(PointF(1f, 0f))
        photoItem1.pointList.add(PointF(0.5f, 1f))
        photoItem1.pointList.add(PointF(0f, 1f))
        item.photoItemList.add(photoItem1)
        //second frame
        val photoItem2 = PhotoItem()
        photoItem1.index = 1
        photoItem2.bound.set(0.333f, 0f, 1f, 1f)
        photoItem2.pointList.add(PointF(0.5f, 0f))
        photoItem2.pointList.add(PointF(1f, 0f))
        photoItem2.pointList.add(PointF(1f, 1f))
        photoItem2.pointList.add(PointF(0f, 1f))
        item.photoItemList.add(photoItem2)
        return item
    }
}
