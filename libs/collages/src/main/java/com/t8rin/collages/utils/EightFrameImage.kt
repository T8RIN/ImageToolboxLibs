@file:Suppress("FunctionName")

package com.t8rin.collages.utils

import android.graphics.PointF
import android.graphics.RectF
import com.t8rin.collages.model.TemplateItem
import com.t8rin.collages.view.PhotoItem

/**
 * Created by admin on 7/2/2016.
 */
internal object EightFrameImage {
    internal fun collage_8_16(): TemplateItem {
        val item = FrameImageUtils.collage("collage_8_16.png")
        val photoItemList = mutableListOf<PhotoItem>()
        //first frame
        var photoItem = PhotoItem()
        photoItem.index = 0
        photoItem.shrinkMethod = PhotoItem.SHRINK_METHOD_COMMON
        photoItem.bound.set(0f, 0f, 0.5f, 0.5f)
        photoItem.pointList.add(PointF(0f, 0f))
        photoItem.pointList.add(PointF(1f, 0f))
        photoItem.pointList.add(PointF(1f, 1f))
        //shrink map
        photoItem.shrinkMap = HashMap()
        photoItem.shrinkMap!![photoItem.pointList[0]] = PointF(1f, 2f)
        photoItem.shrinkMap!![photoItem.pointList[1]] = PointF(2f, 1f)
        photoItem.shrinkMap!![photoItem.pointList[2]] = PointF(1f, 1f)
        photoItemList.add(photoItem)
        //second frame
        photoItem = PhotoItem()
        photoItem.index = 1
        photoItem.shrinkMethod = PhotoItem.SHRINK_METHOD_COMMON
        photoItem.bound.set(0f, 0f, 0.5f, 0.5f)
        photoItem.pointList.add(PointF(0f, 0f))
        photoItem.pointList.add(PointF(1f, 1f))
        photoItem.pointList.add(PointF(0f, 1f))
        //shrink map
        photoItem.shrinkMap = HashMap()
        photoItem.shrinkMap!![photoItem.pointList[0]] = PointF(2f, 1f)
        photoItem.shrinkMap!![photoItem.pointList[1]] = PointF(1f, 1f)
        photoItem.shrinkMap!![photoItem.pointList[2]] = PointF(1f, 2f)
        photoItemList.add(photoItem)
        //third frame
        photoItem = PhotoItem()
        photoItem.index = 2
        photoItem.shrinkMethod = PhotoItem.SHRINK_METHOD_COMMON
        photoItem.bound.set(0.5f, 0f, 1f, 0.5f)
        photoItem.pointList.add(PointF(0f, 0f))
        photoItem.pointList.add(PointF(1f, 0f))
        photoItem.pointList.add(PointF(0f, 1f))
        //shrink map
        photoItem.shrinkMap = HashMap()
        photoItem.shrinkMap!![photoItem.pointList[0]] = PointF(1f, 2f)
        photoItem.shrinkMap!![photoItem.pointList[1]] = PointF(2f, 1f)
        photoItem.shrinkMap!![photoItem.pointList[2]] = PointF(1f, 1f)
        photoItemList.add(photoItem)
        //fourth frame
        photoItem = PhotoItem()
        photoItem.index = 3
        photoItem.shrinkMethod = PhotoItem.SHRINK_METHOD_COMMON
        photoItem.bound.set(0.5f, 0f, 1f, 0.5f)
        photoItem.pointList.add(PointF(1f, 0f))
        photoItem.pointList.add(PointF(1f, 1f))
        photoItem.pointList.add(PointF(0f, 1f))
        //shrink map
        photoItem.shrinkMap = HashMap()
        photoItem.shrinkMap!![photoItem.pointList[0]] = PointF(1f, 2f)
        photoItem.shrinkMap!![photoItem.pointList[1]] = PointF(2f, 1f)
        photoItem.shrinkMap!![photoItem.pointList[2]] = PointF(1f, 1f)
        photoItemList.add(photoItem)
        //fifth frame
        photoItem = PhotoItem()
        photoItem.index = 4
        photoItem.shrinkMethod = PhotoItem.SHRINK_METHOD_COMMON
        photoItem.bound.set(0f, 0.5f, 0.5f, 1f)
        photoItem.pointList.add(PointF(0f, 0f))
        photoItem.pointList.add(PointF(1f, 0f))
        photoItem.pointList.add(PointF(0f, 1f))
        //shrink map
        photoItem.shrinkMap = HashMap()
        photoItem.shrinkMap!![photoItem.pointList[0]] = PointF(2f, 1f)
        photoItem.shrinkMap!![photoItem.pointList[1]] = PointF(1f, 1f)
        photoItem.shrinkMap!![photoItem.pointList[2]] = PointF(1f, 2f)
        photoItemList.add(photoItem)
        //sixth frame
        photoItem = PhotoItem()
        photoItem.index = 5
        photoItem.shrinkMethod = PhotoItem.SHRINK_METHOD_COMMON
        photoItem.bound.set(0f, 0.5f, 0.5f, 1f)
        photoItem.pointList.add(PointF(1f, 0f))
        photoItem.pointList.add(PointF(1f, 1f))
        photoItem.pointList.add(PointF(0f, 1f))
        //shrink map
        photoItem.shrinkMap = HashMap()
        photoItem.shrinkMap!![photoItem.pointList[0]] = PointF(1f, 1f)
        photoItem.shrinkMap!![photoItem.pointList[1]] = PointF(1f, 2f)
        photoItem.shrinkMap!![photoItem.pointList[2]] = PointF(2f, 1f)
        photoItemList.add(photoItem)
        //seventh frame
        photoItem = PhotoItem()
        photoItem.index = 6
        photoItem.shrinkMethod = PhotoItem.SHRINK_METHOD_COMMON
        photoItem.bound.set(0.5f, 0.5f, 1f, 1f)
        photoItem.pointList.add(PointF(0f, 0f))
        photoItem.pointList.add(PointF(1f, 1f))
        photoItem.pointList.add(PointF(0f, 1f))
        photoItem.shrinkMap = HashMap()
        photoItem.shrinkMap!![photoItem.pointList[0]] = PointF(1f, 1f)
        photoItem.shrinkMap!![photoItem.pointList[1]] = PointF(1f, 2f)
        photoItem.shrinkMap!![photoItem.pointList[2]] = PointF(2f, 1f)
        photoItemList.add(photoItem)
        //eighth frame
        photoItem = PhotoItem()
        photoItem.index = 7
        photoItem.shrinkMethod = PhotoItem.SHRINK_METHOD_COMMON
        photoItem.bound.set(0.5f, 0.5f, 1f, 1f)
        photoItem.pointList.add(PointF(0f, 0f))
        photoItem.pointList.add(PointF(1f, 0f))
        photoItem.pointList.add(PointF(1f, 1f))
        photoItem.shrinkMap = HashMap()
        photoItem.shrinkMap!![photoItem.pointList[0]] = PointF(1f, 1f)
        photoItem.shrinkMap!![photoItem.pointList[1]] = PointF(1f, 2f)
        photoItem.shrinkMap!![photoItem.pointList[2]] = PointF(2f, 1f)
        photoItemList.add(photoItem)
        return item.copy(photoItemList = photoItemList)
    }

    internal fun collage_8_15(): TemplateItem {
        return FrameImageUtils.buildParamsCollage("collage_8_15.png") {
            val x1 = param(0.3333f)
            val x2 = param(0.6666f)
            val y1 = param(0.3333f)
            val y2 = param(0.6666f)

            addBoxedItem(
                xParams = listOf(x1),
                yParams = listOf(y1),
                boxParams = { vs -> RectF(0f, 0f, vs[x1], vs[y1]) }
            )
            addBoxedItem(
                xParams = listOf(x1, x2),
                yParams = listOf(y1),
                boxParams = { vs -> RectF(vs[x1], 0f, vs[x2], vs[y1]) }
            )
            addBoxedItem(
                xParams = listOf(x2),
                yParams = listOf(y1),
                boxParams = { vs -> RectF(vs[x2], 0f, 1f, vs[y1]) }
            )
            addBoxedItem(
                xParams = listOf(x2),
                yParams = listOf(y1, y2),
                boxParams = { vs -> RectF(0f, vs[y1], vs[x2], vs[y2]) }
            )
            addBoxedItem(
                xParams = listOf(x2),
                yParams = listOf(y1, y2),
                boxParams = { vs -> RectF(vs[x2], vs[y1], 1f, vs[y2]) }
            )
            addBoxedItem(
                xParams = listOf(x1),
                yParams = listOf(y2),
                boxParams = { vs -> RectF(0f, vs[y2], vs[x1], 1f) }
            )
            addBoxedItem(
                xParams = listOf(x1, x2),
                yParams = listOf(y2),
                boxParams = { vs -> RectF(vs[x1], vs[y2], vs[x2], 1f) }
            )
            addBoxedItem(
                xParams = listOf(x2),
                yParams = listOf(y2),
                boxParams = { vs -> RectF(vs[x2], vs[y2], 1f, 1f) }
            )
        }
    }

    internal fun collage_8_14(): TemplateItem {
        return FrameImageUtils.buildParamsCollage("collage_8_14.png") {
            val x1 = param(0.3333f)
            val x2 = param(0.6666f)
            val y1 = param(0.3333f)
            val y2 = param(0.6666f)

            addBoxedItem(
                xParams = listOf(x1),
                yParams = listOf(y1),
                boxParams = { vs -> RectF(0f, 0f, vs[x1], vs[y1]) }
            )
            addBoxedItem(
                xParams = listOf(x1, x2),
                yParams = listOf(y1),
                boxParams = { vs -> RectF(vs[x1], 0f, vs[x2], vs[y1]) }
            )
            addBoxedItem(
                xParams = listOf(x2),
                yParams = listOf(y1),
                boxParams = { vs -> RectF(vs[x2], 0f, 1f, vs[y1]) }
            )
            addBoxedItem(
                xParams = listOf(x1),
                yParams = listOf(y1, y2),
                boxParams = { vs -> RectF(0f, vs[y1], vs[x1], vs[y2]) }
            )
            addBoxedItem(
                xParams = listOf(x1),
                yParams = listOf(y1, y2),
                boxParams = { vs -> RectF(vs[x1], vs[y1], 1f, vs[y2]) }
            )
            addBoxedItem(
                xParams = listOf(x1),
                yParams = listOf(y2),
                boxParams = { vs -> RectF(0f, vs[y2], vs[x1], 1f) }
            )
            addBoxedItem(
                xParams = listOf(x1, x2),
                yParams = listOf(y2),
                boxParams = { vs -> RectF(vs[x1], vs[y2], vs[x2], 1f) }
            )
            addBoxedItem(
                xParams = listOf(x2),
                yParams = listOf(y2),
                boxParams = { vs -> RectF(vs[x2], vs[y2], 1f, 1f) }
            )
        }
    }

    internal fun collage_8_13(): TemplateItem {
        return FrameImageUtils.buildParamsCollage("collage_8_13.png") {
            val x1 = param(0.3333f)
            val x2 = param(0.6666f)
            val y1 = param(0.25f)
            val y2 = param(0.5f)
            val y3 = param(0.75f)

            addBoxedItem(
                xParams = listOf(x1),
                yParams = listOf(y1),
                boxParams = { vs -> RectF(0f, 0f, vs[x1], vs[y1]) }
            )
            addBoxedItem(
                xParams = listOf(x1, x2),
                yParams = listOf(y1),
                boxParams = { vs -> RectF(vs[x1], 0f, vs[x2], vs[y1]) }
            )
            addBoxedItem(
                xParams = listOf(x2),
                yParams = listOf(y1),
                boxParams = { vs -> RectF(vs[x2], 0f, 1f, vs[y1]) }
            )
            addBoxedItem(
                yParams = listOf(y1, y2),
                boxParams = { vs -> RectF(0f, vs[y1], 1f, vs[y2]) }
            )
            addBoxedItem(
                yParams = listOf(y2, y3),
                boxParams = { vs -> RectF(0f, vs[y2], 1f, vs[y3]) }
            )
            addBoxedItem(
                xParams = listOf(x1),
                yParams = listOf(y3),
                boxParams = { vs -> RectF(0f, vs[y3], vs[x1], 1f) }
            )
            addBoxedItem(
                xParams = listOf(x1, x2),
                yParams = listOf(y3),
                boxParams = { vs -> RectF(vs[x1], vs[y3], vs[x2], 1f) }
            )
            addBoxedItem(
                xParams = listOf(x2),
                yParams = listOf(y3),
                boxParams = { vs -> RectF(vs[x2], vs[y3], 1f, 1f) }
            )
        }
    }

    internal fun collage_8_12(): TemplateItem {
        return FrameImageUtils.buildParamsCollage("collage_8_12.png") {
            val x1 = param(0.3333f)
            val x2 = param(0.6666f)
            val y1 = param(0.25f)
            val y2 = param(0.5f)
            val y3 = param(0.75f)

            addBoxedItem(
                xParams = listOf(x2),
                yParams = listOf(y1),
                boxParams = { vs -> RectF(0f, 0f, vs[x2], vs[y1]) }
            )
            addBoxedItem(
                xParams = listOf(x2),
                yParams = listOf(y1),
                boxParams = { vs -> RectF(vs[x2], 0f, 1f, vs[y1]) }
            )
            addBoxedItem(
                xParams = listOf(x1),
                yParams = listOf(y1, y2),
                boxParams = { vs -> RectF(0f, vs[y1], vs[x1], vs[y2]) }
            )
            addBoxedItem(
                xParams = listOf(x1),
                yParams = listOf(y1, y2),
                boxParams = { vs -> RectF(vs[x1], vs[y1], 1f, vs[y2]) }
            )
            addBoxedItem(
                xParams = listOf(x2),
                yParams = listOf(y2, y3),
                boxParams = { vs -> RectF(0f, vs[y2], vs[x2], vs[y3]) }
            )
            addBoxedItem(
                xParams = listOf(x2),
                yParams = listOf(y2, y3),
                boxParams = { vs -> RectF(vs[x2], vs[y2], 1f, vs[y3]) }
            )
            addBoxedItem(
                xParams = listOf(x1),
                yParams = listOf(y3),
                boxParams = { vs -> RectF(0f, vs[y3], vs[x1], 1f) }
            )
            addBoxedItem(
                xParams = listOf(x1),
                yParams = listOf(y3),
                boxParams = { vs -> RectF(vs[x1], vs[y3], 1f, 1f) }
            )
        }
    }

    internal fun collage_8_11(): TemplateItem {
        return FrameImageUtils.buildParamsCollage("collage_8_11.png") {
            val xTop = param(0.5f)
            val xMid2 = param(0.6666f)
            val xBot = param(0.3333f)
            val y1 = param(0.25f)
            val y2 = param(0.5f)
            val y3 = param(0.75f)

            addBoxedItem(
                xParams = listOf(xTop),
                yParams = listOf(y1),
                boxParams = { vs -> RectF(0f, 0f, vs[xTop], vs[y1]) }
            )
            addBoxedItem(
                xParams = listOf(xTop),
                yParams = listOf(y1),
                boxParams = { vs -> RectF(vs[xTop], 0f, 1f, vs[y1]) }
            )
            addBoxedItem(
                xParams = listOf(xTop),
                yParams = listOf(y1, y2),
                boxParams = { vs -> RectF(0f, vs[y1], vs[xTop], vs[y2]) }
            )
            addBoxedItem(
                xParams = listOf(xTop),
                yParams = listOf(y1, y2),
                boxParams = { vs -> RectF(vs[xTop], vs[y1], 1f, vs[y2]) }
            )
            addBoxedItem(
                xParams = listOf(xMid2),
                yParams = listOf(y2, y3),
                boxParams = { vs -> RectF(0f, vs[y2], vs[xMid2], vs[y3]) }
            )
            addBoxedItem(
                xParams = listOf(xMid2),
                yParams = listOf(y2, y3),
                boxParams = { vs -> RectF(vs[xMid2], vs[y2], 1f, vs[y3]) }
            )
            addBoxedItem(
                xParams = listOf(xBot),
                yParams = listOf(y3),
                boxParams = { vs -> RectF(0f, vs[y3], vs[xBot], 1f) }
            )
            addBoxedItem(
                xParams = listOf(xBot),
                yParams = listOf(y3),
                boxParams = { vs -> RectF(vs[xBot], vs[y3], 1f, 1f) }
            )
        }
    }

    internal fun collage_8_10(): TemplateItem {
        return FrameImageUtils.buildParamsCollage("collage_8_10.png") {
            val x = param(0.5f)
            val y1 = param(0.25f)
            val y2 = param(0.5f)
            val y3 = param(0.75f)

            addBoxedItem(
                xParams = listOf(x),
                yParams = listOf(y1),
                boxParams = { vs -> RectF(0f, 0f, vs[x], vs[y1]) }
            )
            addBoxedItem(
                xParams = listOf(x),
                yParams = listOf(y1),
                boxParams = { vs -> RectF(vs[x], 0f, 1f, vs[y1]) }
            )
            addBoxedItem(
                xParams = listOf(x),
                yParams = listOf(y1, y2),
                boxParams = { vs -> RectF(0f, vs[y1], vs[x], vs[y2]) }
            )
            addBoxedItem(
                xParams = listOf(x),
                yParams = listOf(y1, y2),
                boxParams = { vs -> RectF(vs[x], vs[y1], 1f, vs[y2]) }
            )
            addBoxedItem(
                xParams = listOf(x),
                yParams = listOf(y2, y3),
                boxParams = { vs -> RectF(0f, vs[y2], vs[x], vs[y3]) }
            )
            addBoxedItem(
                xParams = listOf(x),
                yParams = listOf(y2, y3),
                boxParams = { vs -> RectF(vs[x], vs[y2], 1f, vs[y3]) }
            )
            addBoxedItem(
                xParams = listOf(x),
                yParams = listOf(y3),
                boxParams = { vs -> RectF(0f, vs[y3], vs[x], 1f) }
            )
            addBoxedItem(
                xParams = listOf(x),
                yParams = listOf(y3),
                boxParams = { vs -> RectF(vs[x], vs[y3], 1f, 1f) }
            )
        }
    }

    internal fun collage_8_9(): TemplateItem {
        return FrameImageUtils.buildParamsCollage("collage_8_9.png") {
            val x1 = param(0.3333f)
            val x2 = param(0.6666f)
            val y1 = param(0.25f)
            val y2 = param(0.5f)

            addBoxedItem(
                xParams = listOf(x1),
                yParams = listOf(y1),
                boxParams = { vs -> RectF(0f, 0f, vs[x1], vs[y1]) }
            )
            addBoxedItem(
                xParams = listOf(x1, x2),
                yParams = listOf(y1),
                boxParams = { vs -> RectF(vs[x1], 0f, vs[x2], vs[y1]) }
            )
            addBoxedItem(
                xParams = listOf(x2),
                yParams = listOf(y1),
                boxParams = { vs -> RectF(vs[x2], 0f, 1f, vs[y1]) }
            )
            addBoxedItem(
                xParams = listOf(x1),
                yParams = listOf(y1, y2),
                boxParams = { vs -> RectF(0f, vs[y1], vs[x1], vs[y2]) }
            )
            addBoxedItem(
                xParams = listOf(x1, x2),
                yParams = listOf(y1, y2),
                boxParams = { vs -> RectF(vs[x1], vs[y1], vs[x2], vs[y2]) }
            )
            addBoxedItem(
                xParams = listOf(x2),
                yParams = listOf(y1, y2),
                boxParams = { vs -> RectF(vs[x2], vs[y1], 1f, vs[y2]) }
            )
            addBoxedItem(
                xParams = listOf(x2),
                yParams = listOf(y2),
                boxParams = { vs -> RectF(0f, vs[y2], vs[x2], 1f) }
            )
            addBoxedItem(
                xParams = listOf(x2),
                yParams = listOf(y2),
                boxParams = { vs -> RectF(vs[x2], vs[y2], 1f, 1f) }
            )
        }
    }

    internal fun collage_8_8(): TemplateItem {
        return FrameImageUtils.buildParamsCollage("collage_8_8.png") {
            val xR = param(0.6666f)
            val xM = param(0.3333f)
            val y1 = param(0.25f)
            val y2 = param(0.5f)
            val y3 = param(0.75f)
            val yMid = param(0.7f)

            addBoxedItem(
                xParams = listOf(xM),
                yParams = listOf(y1),
                boxParams = { vs -> RectF(0f, 0f, vs[xM], vs[y1]) }
            )
            addBoxedItem(
                xParams = listOf(xM, xR),
                yParams = listOf(y1),
                boxParams = { vs -> RectF(vs[xM], 0f, vs[xR], vs[y1]) }
            )
            addBoxedItem(
                xParams = listOf(xR),
                yParams = listOf(y1, yMid),
                boxParams = { vs -> RectF(0f, vs[y1], vs[xR], vs[yMid]) }
            )
            addBoxedItem(
                xParams = listOf(xR),
                yParams = listOf(yMid),
                boxParams = { vs -> RectF(0f, vs[yMid], vs[xR], 1f) }
            )
            addBoxedItem(
                xParams = listOf(xR),
                yParams = listOf(y1),
                boxParams = { vs -> RectF(vs[xR], 0f, 1f, vs[y1]) }
            )
            addBoxedItem(
                xParams = listOf(xR),
                yParams = listOf(y1, y2),
                boxParams = { vs -> RectF(vs[xR], vs[y1], 1f, vs[y2]) }
            )
            addBoxedItem(
                xParams = listOf(xR),
                yParams = listOf(y2, y3),
                boxParams = { vs -> RectF(vs[xR], vs[y2], 1f, vs[y3]) }
            )
            addBoxedItem(
                xParams = listOf(xR),
                yParams = listOf(y3),
                boxParams = { vs -> RectF(vs[xR], vs[y3], 1f, 1f) }
            )
        }
    }

    internal fun collage_8_7(): TemplateItem {
        return FrameImageUtils.buildParamsCollage("collage_8_7.png") {
            val x1 = param(0.3f)
            val x2 = param(0.6f)
            val y1 = param(0.2f)
            val y2 = param(0.6f)
            val yR1 = param(0.3333f)
            val yR2 = param(0.6666f)

            addBoxedItem(
                xParams = listOf(x1),
                yParams = listOf(y1),
                boxParams = { vs -> RectF(0f, 0f, vs[x1], vs[y1]) }
            )
            addBoxedItem(
                xParams = listOf(x1, x2),
                yParams = listOf(y1),
                boxParams = { vs -> RectF(vs[x1], 0f, vs[x2], vs[y1]) }
            )
            addBoxedItem(
                xParams = listOf(x1),
                yParams = listOf(y1, y2),
                boxParams = { vs -> RectF(0f, vs[y1], vs[x1], vs[y2]) }
            )
            addBoxedItem(
                xParams = listOf(x1, x2),
                yParams = listOf(y1, y2),
                boxParams = { vs -> RectF(vs[x1], vs[y1], vs[x2], vs[y2]) }
            )
            addBoxedItem(
                xParams = listOf(x2),
                yParams = listOf(y2),
                boxParams = { vs -> RectF(0f, vs[y2], vs[x2], 1f) }
            )
            addBoxedItem(
                xParams = listOf(x2),
                yParams = listOf(yR1),
                boxParams = { vs -> RectF(vs[x2], 0f, 1f, vs[yR1]) }
            )
            addBoxedItem(
                xParams = listOf(x2),
                yParams = listOf(yR1, yR2),
                boxParams = { vs -> RectF(vs[x2], vs[yR1], 1f, vs[yR2]) }
            )
            addBoxedItem(
                xParams = listOf(x2),
                yParams = listOf(yR2),
                boxParams = { vs -> RectF(vs[x2], vs[yR2], 1f, 1f) }
            )
        }
    }

    internal fun collage_8_6(): TemplateItem {
        return FrameImageUtils.buildParamsCollage("collage_8_6.png") {
            val xMain = param(0.6f)
            val xSmall = param(0.3f)
            val yTop = param(0.5f)
            val yR1 = param(0.3333f)
            val yR2 = param(0.6666f)
            val ySmall = param(0.75f)

            addBoxedItem(
                xParams = listOf(xMain),
                yParams = listOf(yTop),
                boxParams = { vs -> RectF(0f, 0f, vs[xMain], vs[yTop]) }
            )
            addBoxedItem(
                xParams = listOf(xMain),
                yParams = listOf(yR1),
                boxParams = { vs -> RectF(vs[xMain], 0f, 1f, vs[yR1]) }
            )
            addBoxedItem(
                xParams = listOf(xMain),
                yParams = listOf(yR1, yR2),
                boxParams = { vs -> RectF(vs[xMain], vs[yR1], 1f, vs[yR2]) }
            )
            addBoxedItem(
                xParams = listOf(xMain),
                yParams = listOf(yR2),
                boxParams = { vs -> RectF(vs[xMain], vs[yR2], 1f, 1f) }
            )
            addBoxedItem(
                xParams = listOf(xSmall),
                yParams = listOf(yTop, ySmall),
                boxParams = { vs -> RectF(0f, vs[yTop], vs[xSmall], vs[ySmall]) }
            )
            addBoxedItem(
                xParams = listOf(xSmall, xMain),
                yParams = listOf(yTop, ySmall),
                boxParams = { vs -> RectF(vs[xSmall], vs[yTop], vs[xMain], vs[ySmall]) }
            )
            addBoxedItem(
                xParams = listOf(xSmall),
                yParams = listOf(ySmall),
                boxParams = { vs -> RectF(0f, vs[ySmall], vs[xSmall], 1f) }
            )
            addBoxedItem(
                xParams = listOf(xSmall, xMain),
                yParams = listOf(ySmall),
                boxParams = { vs -> RectF(vs[xSmall], vs[ySmall], vs[xMain], 1f) }
            )
        }
    }

    internal fun collage_8_5(): TemplateItem {
        return FrameImageUtils.buildParamsCollage("collage_8_5.png") {
            val x0 = param(0.25f)
            val x1 = param(0.5f)
            val x2 = param(0.75f)
            val y = param(0.5f)

            addBoxedItem(
                xParams = listOf(x0),
                yParams = listOf(y),
                boxParams = { vs -> RectF(0f, 0f, vs[x0], vs[y]) }
            )
            addBoxedItem(
                xParams = listOf(x0, x1),
                yParams = listOf(y),
                boxParams = { vs -> RectF(vs[x0], 0f, vs[x1], vs[y]) }
            )
            addBoxedItem(
                xParams = listOf(x1, x2),
                yParams = listOf(y),
                boxParams = { vs -> RectF(vs[x1], 0f, vs[x2], vs[y]) }
            )
            addBoxedItem(
                xParams = listOf(x2),
                yParams = listOf(y),
                boxParams = { vs -> RectF(vs[x2], 0f, 1f, vs[y]) }
            )
            addBoxedItem(
                xParams = listOf(x0),
                yParams = listOf(y),
                boxParams = { vs -> RectF(0f, vs[y], vs[x0], 1f) }
            )
            addBoxedItem(
                xParams = listOf(x0, x1),
                yParams = listOf(y),
                boxParams = { vs -> RectF(vs[x0], vs[y], vs[x1], 1f) }
            )
            addBoxedItem(
                xParams = listOf(x1, x2),
                yParams = listOf(y),
                boxParams = { vs -> RectF(vs[x1], vs[y], vs[x2], 1f) }
            )
            addBoxedItem(
                xParams = listOf(x2),
                yParams = listOf(y),
                boxParams = { vs -> RectF(vs[x2], vs[y], 1f, 1f) }
            )
        }
    }

    internal fun collage_8_4(): TemplateItem {
        return FrameImageUtils.buildParamsCollage("collage_8_4.png") {
            val x1 = param(0.25f)
            val x2 = param(0.5f)
            val x3 = param(0.75f)
            val yTop = param(0.25f)
            val yMid = param(0.75f)

            addBoxedItem(
                xParams = listOf(x1),
                yParams = listOf(yTop),
                boxParams = { vs -> RectF(0f, 0f, vs[x1], vs[yTop]) }
            )
            addBoxedItem(
                xParams = listOf(x1),
                yParams = listOf(yTop),
                boxParams = { vs -> RectF(0f, vs[yTop], vs[x1], 1f) }
            )
            addBoxedItem(
                xParams = listOf(x1, x2),
                yParams = listOf(yMid),
                boxParams = { vs -> RectF(vs[x1], 0f, vs[x2], vs[yMid]) }
            )
            addBoxedItem(
                xParams = listOf(x1, x2),
                yParams = listOf(yMid),
                boxParams = { vs -> RectF(vs[x1], vs[yMid], vs[x2], 1f) }
            )
            addBoxedItem(
                xParams = listOf(x2, x3),
                yParams = listOf(yTop),
                boxParams = { vs -> RectF(vs[x2], 0f, vs[x3], vs[yTop]) }
            )
            addBoxedItem(
                xParams = listOf(x2, x3),
                yParams = listOf(yTop),
                boxParams = { vs -> RectF(vs[x2], vs[yTop], vs[x3], 1f) }
            )
            addBoxedItem(
                xParams = listOf(x3),
                yParams = listOf(yMid),
                boxParams = { vs -> RectF(vs[x3], 0f, 1f, vs[yMid]) }
            )
            addBoxedItem(
                xParams = listOf(x3),
                yParams = listOf(yMid),
                boxParams = { vs -> RectF(vs[x3], vs[yMid], 1f, 1f) }
            )
        }
    }

    internal fun collage_8_3(): TemplateItem {
        return FrameImageUtils.buildParamsCollage("collage_8_3.png") {
            val x1 = param(0.25f)
            val x2 = param(0.5f)
            val x3 = param(0.75f)
            val y1 = param(0.25f)
            val y2 = param(0.75f)

            addBoxedItem(
                xParams = listOf(x1),
                yParams = listOf(y1),
                boxParams = { vs -> RectF(0f, 0f, vs[x1], vs[y1]) }
            )
            addBoxedItem(
                xParams = listOf(x1, x2),
                yParams = listOf(y1),
                boxParams = { vs -> RectF(vs[x1], 0f, vs[x2], vs[y1]) }
            )
            addBoxedItem(
                xParams = listOf(x2),
                yParams = listOf(y1),
                boxParams = { vs -> RectF(vs[x2], 0f, 1f, vs[y1]) }
            )
            addBoxedItem(
                yParams = listOf(y1, y2),
                boxParams = { vs -> RectF(0f, vs[y1], vs[x2], vs[y2]) }
            )
            addBoxedItem(
                yParams = listOf(y1, y2),
                boxParams = { vs -> RectF(vs[x2], vs[y1], 1f, vs[y2]) }
            )
            addBoxedItem(
                xParams = listOf(x2),
                yParams = listOf(y2),
                boxParams = { vs -> RectF(0f, vs[y2], vs[x2], 1f) }
            )
            addBoxedItem(
                xParams = listOf(x2, x3),
                yParams = listOf(y2),
                boxParams = { vs -> RectF(vs[x2], vs[y2], vs[x3], 1f) }
            )
            addBoxedItem(
                xParams = listOf(x3),
                yParams = listOf(y2),
                boxParams = { vs -> RectF(vs[x3], vs[y2], 1f, 1f) }
            )
        }
    }

    internal fun collage_8_2(): TemplateItem {
        return FrameImageUtils.buildParamsCollage("collage_8_2.png") {
            val x1 = param(0.3333f)
            val x2 = param(0.6666f)
            val y1 = param(0.3333f)
            val y2 = param(0.6666f)
            val yMid = param(0.5f)

            addBoxedItem(
                xParams = listOf(x1),
                yParams = listOf(y1),
                boxParams = { vs -> RectF(0f, 0f, vs[x1], vs[y1]) }
            )
            addBoxedItem(
                xParams = listOf(x1),
                yParams = listOf(y1, y2),
                boxParams = { vs -> RectF(0f, vs[y1], vs[x1], vs[y2]) }
            )
            addBoxedItem(
                xParams = listOf(x1),
                yParams = listOf(y2),
                boxParams = { vs -> RectF(0f, vs[y2], vs[x1], 1f) }
            )
            addBoxedItem(
                xParams = listOf(x1, x2),
                yParams = listOf(yMid),
                boxParams = { vs -> RectF(vs[x1], 0f, vs[x2], vs[yMid]) }
            )
            addBoxedItem(
                xParams = listOf(x1, x2),
                yParams = listOf(yMid),
                boxParams = { vs -> RectF(vs[x1], vs[yMid], vs[x2], 1f) }
            )
            addBoxedItem(
                xParams = listOf(x2),
                yParams = listOf(y1),
                boxParams = { vs -> RectF(vs[x2], 0f, 1f, vs[y1]) }
            )
            addBoxedItem(
                xParams = listOf(x2),
                yParams = listOf(y1, y2),
                boxParams = { vs -> RectF(vs[x2], vs[y1], 1f, vs[y2]) }
            )
            addBoxedItem(
                xParams = listOf(x2),
                yParams = listOf(y2),
                boxParams = { vs -> RectF(vs[x2], vs[y2], 1f, 1f) }
            )
        }
    }

    internal fun collage_8_1(): TemplateItem {
        return FrameImageUtils.buildParamsCollage("collage_8_1.png") {
            val x1 = param(0.3333f)
            val x2 = param(0.5f)
            val x3 = param(0.6666f)
            val y1 = param(0.3333f)
            val y2 = param(0.6666f)

            addBoxedItem(
                xParams = listOf(x1),
                yParams = listOf(y1),
                boxParams = { vs -> RectF(0f, 0f, vs[x1], vs[y1]) }
            )
            addBoxedItem(
                xParams = listOf(x1, x3),
                yParams = listOf(y1),
                boxParams = { vs -> RectF(vs[x1], 0f, vs[x3], vs[y1]) }
            )
            addBoxedItem(
                xParams = listOf(x3),
                yParams = listOf(y1),
                boxParams = { vs -> RectF(vs[x3], 0f, 1f, vs[y1]) }
            )
            addBoxedItem(
                xParams = listOf(x2),
                yParams = listOf(y1, y2),
                boxParams = { vs -> RectF(0f, vs[y1], vs[x2], vs[y2]) }
            )
            addBoxedItem(
                xParams = listOf(x2),
                yParams = listOf(y1, y2),
                boxParams = { vs -> RectF(vs[x2], vs[y1], 1f, vs[y2]) }
            )
            addBoxedItem(
                xParams = listOf(x1),
                yParams = listOf(y2),
                boxParams = { vs -> RectF(0f, vs[y2], vs[x1], 1f) }
            )
            addBoxedItem(
                xParams = listOf(x1, x3),
                yParams = listOf(y2),
                boxParams = { vs -> RectF(vs[x1], vs[y2], vs[x3], 1f) }
            )
            addBoxedItem(
                xParams = listOf(x3),
                yParams = listOf(y2),
                boxParams = { vs -> RectF(vs[x3], vs[y2], 1f, 1f) }
            )
        }
    }

    internal fun collage_8_0(): TemplateItem {
        return FrameImageUtils.buildParamsCollage("collage_8_0.png") {
            val x1 = param(0.25f)
            val x2 = param(0.5f)
            val x3 = param(0.75f)
            val y1 = param(0.25f)
            val y2 = param(0.5f)
            val y3 = param(0.75f)

            addBoxedItem(
                xParams = listOf(x1),
                yParams = listOf(y2),
                boxParams = { vs -> RectF(0f, 0f, vs[x1], vs[y2]) }
            )
            addBoxedItem(
                xParams = listOf(x1, x2),
                yParams = listOf(y2),
                boxParams = { vs -> RectF(vs[x1], 0f, vs[x2], vs[y2]) }
            )
            addBoxedItem(
                xParams = listOf(x2),
                yParams = listOf(y1),
                boxParams = { vs -> RectF(vs[x2], 0f, 1f, vs[y1]) }
            )
            addBoxedItem(
                xParams = listOf(x2),
                yParams = listOf(y1, y2),
                boxParams = { vs -> RectF(vs[x2], vs[y1], 1f, vs[y2]) }
            )
            addBoxedItem(
                xParams = listOf(x2),
                yParams = listOf(y2, y3),
                boxParams = { vs -> RectF(0f, vs[y2], vs[x2], vs[y3]) }
            )
            addBoxedItem(
                xParams = listOf(x2),
                yParams = listOf(y3),
                boxParams = { vs -> RectF(0f, vs[y3], vs[x2], 1f) }
            )
            addBoxedItem(
                xParams = listOf(x2, x3),
                yParams = listOf(y2),
                boxParams = { vs -> RectF(vs[x2], vs[y2], vs[x3], 1f) }
            )
            addBoxedItem(
                xParams = listOf(x3),
                yParams = listOf(y2),
                boxParams = { vs -> RectF(vs[x3], vs[y2], 1f, 1f) }
            )
        }
    }
}