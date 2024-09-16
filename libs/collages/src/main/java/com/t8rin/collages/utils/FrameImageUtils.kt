package com.photoeditor.photoeffect.utils

import android.content.Context
import android.graphics.Matrix
import android.graphics.Path
import android.graphics.PointF
import androidx.core.net.toUri
import com.photoeditor.photoeffect.model.TemplateItem
import com.photoeditor.photoeffect.template.PhotoItem
import java.io.IOException

/**
 * Created by admin on 5/6/2016.
 */
object FrameImageUtils {
    val FRAME_FOLDER = "frame"

    internal fun collage(frameName: String): TemplateItem {
        val item = TemplateItem()
        item.preview = ("file:///android_asset/$FRAME_FOLDER/$frameName").toUri()
        item.title = frameName
        return item
    }

    private fun collage_1_0(): TemplateItem {
        val item = collage("collage_1_0.png")
        val photoItem = PhotoItem()
        photoItem.bound.set(0f, 0f, 1f, 1f)
        photoItem.index = 0
        photoItem.pointList.add(PointF(0f, 0f))
        photoItem.pointList.add(PointF(1f, 0f))
        photoItem.pointList.add(PointF(1f, 1f))
        photoItem.pointList.add(PointF(0f, 1f))
        item.photoItemList.add(photoItem)
        return item
    }

    fun createTwoHeartItem(): Array<Path?> {
        val result = arrayOfNulls<Path>(2)
        var t = Path()
        t.moveTo(297.3f, 550.87f)
        t.cubicTo(283.52f, 535.43f, 249.13f, 505.34f, 220.86f, 483.99f)
        t.cubicTo(137.12f, 420.75f, 125.72f, 411.6f, 91.72f, 380.29f)
        t.cubicTo(29.03f, 322.57f, 2.41f, 264.58f, 2.5f, 185.95f)
        t.cubicTo(2.55f, 147.57f, 5.17f, 132.78f, 15.91f, 110.15f)
        t.cubicTo(34.15f, 71.77f, 61.01f, 43.24f, 95.36f, 25.8f)
        t.cubicTo(119.69f, 13.44f, 131.68f, 7.95f, 172.3f, 7.73f)
        t.cubicTo(214.8f, 7.49f, 223.74f, 12.45f, 248.74f, 26.18f)
        t.cubicTo(279.16f, 42.9f, 310.48f, 78.62f, 316.95f, 103.99f)
        t.lineTo(320.95f, 119.66f)
        result[0] = t
        t = Path()
        t.moveTo(320.95f, 119.66f)
        t.lineTo(330.81f, 98.08f)
        t.cubicTo(386.53f, -23.89f, 564.41f, -22.07f, 626.31f, 101.11f)
        t.cubicTo(645.95f, 140.19f, 648.11f, 223.62f, 630.69f, 270.62f)
        t.cubicTo(607.98f, 331.93f, 565.31f, 378.67f, 466.69f, 450.3f)
        t.cubicTo(402.01f, 497.27f, 328.8f, 568.35f, 323.71f, 578.33f)
        t.cubicTo(317.79f, 589.92f, 323.42f, 580.14f, 297.3f, 550.87f)
        result[1] = t
        return result
    }

    fun createHeartItem(top: Float, size: Float): Path {
        val path = Path()
        path.moveTo(top, top + size / 4)
        path.quadTo(top, top, top + size / 4, top)
        path.quadTo(top + size / 2, top, top + size / 2, top + size / 4)
        path.quadTo(top + size / 2, top, top + size * 3 / 4, top)
        path.quadTo(top + size, top, top + size, top + size / 4)
        path.quadTo(top + size, top + size / 2, top + size * 3 / 4, top + size * 3 / 4)
        path.lineTo(top + size / 2, top + size)
        path.lineTo(top + size / 4, top + size * 3 / 4)
        path.quadTo(top, top + size / 2, top, top + size / 4)
        return path
    }

    fun createFatHeartItem(): Path {
        val path = Path()
        path.moveTo(75f, 40f)
        path.cubicTo(75f, 37f, 70f, 25f, 50f, 25f)
        path.cubicTo(20f, 25f, 20f, 62.5f, 20f, 62.5f)
        path.cubicTo(20f, 80f, 40f, 102f, 75f, 120f)
        path.cubicTo(110f, 102f, 130f, 80f, 130f, 62.5f)
        path.cubicTo(130f, 62.5f, 130f, 25f, 100f, 25f)
        path.cubicTo(85f, 25f, 75f, 37f, 75f, 40f)
        val m = Matrix()
        m.postTranslate(-20f, -25f)
        path.transform(m)
        return path
    }

    fun createHeartItem(): Path {
        val path = Path()
        path.moveTo(256.0f, -7.47f)
        path.lineTo(225.07f, 20.69f)
        path.cubicTo(115.2f, 120.32f, 42.67f, 186.24f, 42.67f, 266.67f)
        path.cubicTo(42.67f, 332.59f, 94.29f, 384.0f, 160.0f, 384.0f)
        path.cubicTo(197.12f, 384.0f, 232.75f, 366.72f, 256.0f, 339.63f)
        path.cubicTo(279.25f, 366.72f, 314.88f, 384.0f, 352.0f, 384.0f)
        path.cubicTo(417.71f, 384.0f, 469.33f, 332.59f, 469.33f, 266.67f)
        path.cubicTo(469.33f, 186.24f, 396.8f, 120.32f, 286.93f, 20.69f)
        path.lineTo(256.0f, -7.47f)
        val m = Matrix()
        m.preScale(1f, -1f)
        m.postTranslate(-42f, 384f)
        path.transform(m)
        return path
    }

    fun loadFrameImages(context: Context): ArrayList<TemplateItem> {
        val templateItemList = ArrayList<TemplateItem>()
        val am = context.assets
        try {
            val frameNames = am.list(FRAME_FOLDER)
            templateItemList.clear()
            if (!frameNames.isNullOrEmpty()) {
                for (str in frameNames) {
                    val item = createTemplateItems(str)
                    if (item != null)
                        templateItemList.add(item)
                }

                templateItemList.sortWith(Comparator { lhs, rhs -> lhs.photoItemList.size - rhs.photoItemList.size })
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return templateItemList
    }

    private fun createTemplateItems(frameName: String): TemplateItem? {
        return when (frameName) {
            "collage_1_0.png" -> collage_1_0()
            "collage_2_0.png" -> TwoFrameImage.collage_2_0()
            "collage_2_1.png" -> TwoFrameImage.collage_2_1()
            "collage_2_2.png" -> TwoFrameImage.collage_2_2()
            "collage_2_3.png" -> TwoFrameImage.collage_2_3()
            "collage_2_4.png" -> TwoFrameImage.collage_2_4()
            "collage_2_5.png" -> TwoFrameImage.collage_2_5()
            "collage_2_6.png" -> TwoFrameImage.collage_2_6()
            "collage_2_7.png" -> TwoFrameImage.collage_2_7()
            "collage_2_8.png" -> TwoFrameImage.collage_2_8()
            "collage_2_9.png" -> TwoFrameImage.collage_2_9()
            "collage_2_10.png" -> TwoFrameImage.collage_2_10()
            "collage_2_11.png" -> TwoFrameImage.collage_2_11()
            "collage_3_0.png" -> ThreeFrameImage.collage_3_0()
            "collage_3_1.png" -> ThreeFrameImage.collage_3_1()
            "collage_3_2.png" -> ThreeFrameImage.collage_3_2()
            "collage_3_3.png" -> ThreeFrameImage.collage_3_3()
            "collage_3_4.png" -> ThreeFrameImage.collage_3_4()
            "collage_3_5.png" -> ThreeFrameImage.collage_3_5()
            "collage_3_6.png" -> ThreeFrameImage.collage_3_6()
            "collage_3_7.png" -> ThreeFrameImage.collage_3_7()
            "collage_3_8.png" -> ThreeFrameImage.collage_3_8()
            "collage_3_9.png" -> ThreeFrameImage.collage_3_9()
            "collage_3_10.png" -> ThreeFrameImage.collage_3_10()
            "collage_3_11.png" -> ThreeFrameImage.collage_3_11()
            "collage_3_12.png" -> ThreeFrameImage.collage_3_12()
            "collage_3_13.png" -> ThreeFrameImage.collage_3_13()
            "collage_3_14.png" -> ThreeFrameImage.collage_3_14()
            "collage_3_15.png" -> ThreeFrameImage.collage_3_15()
            "collage_3_16.png" -> ThreeFrameImage.collage_3_16()
            "collage_3_17.png" -> ThreeFrameImage.collage_3_17()
            "collage_3_18.png" -> ThreeFrameImage.collage_3_18()
            "collage_3_19.png" -> ThreeFrameImage.collage_3_19()
            "collage_3_20.png" -> ThreeFrameImage.collage_3_20()
            "collage_3_21.png" -> ThreeFrameImage.collage_3_21()
            "collage_3_22.png" -> ThreeFrameImage.collage_3_22()
            "collage_3_23.png" -> ThreeFrameImage.collage_3_23()
            "collage_3_24.png" -> ThreeFrameImage.collage_3_24()
            "collage_3_25.png" -> ThreeFrameImage.collage_3_25()
            "collage_3_26.png" -> ThreeFrameImage.collage_3_26()
            "collage_3_27.png" -> ThreeFrameImage.collage_3_27()
            "collage_3_28.png" -> ThreeFrameImage.collage_3_28()
            "collage_3_29.png" -> ThreeFrameImage.collage_3_29()
            "collage_3_30.png" -> ThreeFrameImage.collage_3_30()
            "collage_3_31.png" -> ThreeFrameImage.collage_3_31()
            "collage_3_32.png" -> ThreeFrameImage.collage_3_32()
            "collage_3_33.png" -> ThreeFrameImage.collage_3_33()
            "collage_3_34.png" -> ThreeFrameImage.collage_3_34()
            "collage_3_35.png" -> ThreeFrameImage.collage_3_35()
            "collage_3_36.png" -> ThreeFrameImage.collage_3_36()
            "collage_3_37.png" -> ThreeFrameImage.collage_3_37()
            "collage_3_38.png" -> ThreeFrameImage.collage_3_38()
            "collage_3_39.png" -> ThreeFrameImage.collage_3_39()
            "collage_3_40.png" -> ThreeFrameImage.collage_3_40()
            "collage_3_41.png" -> ThreeFrameImage.collage_3_41()
            "collage_3_42.png" -> ThreeFrameImage.collage_3_42()
            "collage_3_43.png" -> ThreeFrameImage.collage_3_43()
            "collage_3_44.png" -> ThreeFrameImage.collage_3_44()
            "collage_3_45.png" -> ThreeFrameImage.collage_3_45()
            "collage_3_46.png" -> ThreeFrameImage.collage_3_46()
            "collage_3_47.png" -> ThreeFrameImage.collage_3_47()
            "collage_4_0.png" -> FourFrameImage.collage_4_0()
            "collage_4_1.png" -> FourFrameImage.collage_4_1()
            "collage_4_2.png" -> FourFrameImage.collage_4_2()
            "collage_4_4.png" -> FourFrameImage.collage_4_4()
            "collage_4_5.png" -> FourFrameImage.collage_4_5()
            "collage_4_6.png" -> FourFrameImage.collage_4_6()
            "collage_4_7.png" -> FourFrameImage.collage_4_7()
            "collage_4_8.png" -> FourFrameImage.collage_4_8()
            "collage_4_9.png" -> FourFrameImage.collage_4_9()
            "collage_4_10.png" -> FourFrameImage.collage_4_10()
            "collage_4_11.png" -> FourFrameImage.collage_4_11()
            "collage_4_12.png" -> FourFrameImage.collage_4_12()
            "collage_4_13.png" -> FourFrameImage.collage_4_13()
            "collage_4_14.png" -> FourFrameImage.collage_4_14()
            "collage_4_15.png" -> FourFrameImage.collage_4_15()
            "collage_4_16.png" -> FourFrameImage.collage_4_16()
            "collage_4_17.png" -> FourFrameImage.collage_4_17()
            "collage_4_18.png" -> FourFrameImage.collage_4_18()
            "collage_4_19.png" -> FourFrameImage.collage_4_19()
            "collage_4_20.png" -> FourFrameImage.collage_4_20()
            "collage_4_21.png" -> FourFrameImage.collage_4_21()
            "collage_4_22.png" -> FourFrameImage.collage_4_22()
            "collage_4_23.png" -> FourFrameImage.collage_4_23()
            "collage_4_24.png" -> FourFrameImage.collage_4_24()
            "collage_4_25.png" -> FourFrameImage.collage_4_25()
            "collage_5_0.png" -> FiveFrameImage.collage_5_0()
            "collage_5_1.png" -> FiveFrameImage.collage_5_1()
            "collage_5_2.png" -> FiveFrameImage.collage_5_2()
            "collage_5_3.png" -> FiveFrameImage.collage_5_3()
            "collage_5_4.png" -> FiveFrameImage.collage_5_4()
            "collage_5_5.png" -> FiveFrameImage.collage_5_5()
            "collage_5_6.png" -> FiveFrameImage.collage_5_6()
            "collage_5_7.png" -> FiveFrameImage.collage_5_7()
            "collage_5_8.png" -> FiveFrameImage.collage_5_8()
            "collage_5_9.png" -> FiveFrameImage.collage_5_9()
            "collage_5_10.png" -> FiveFrameImage.collage_5_10()
            "collage_5_11.png" -> FiveFrameImage.collage_5_11()
            "collage_5_12.png" -> FiveFrameImage.collage_5_12()
            "collage_5_13.png" -> FiveFrameImage.collage_5_13()
            "collage_5_14.png" -> FiveFrameImage.collage_5_14()
            "collage_5_15.png" -> FiveFrameImage.collage_5_15()
            "collage_5_16.png" -> FiveFrameImage.collage_5_16()
            "collage_5_17.png" -> FiveFrameImage.collage_5_17()
            "collage_5_18.png" -> FiveFrameImage.collage_5_18()
            "collage_5_19.png" -> FiveFrameImage.collage_5_19()
            "collage_5_20.png" -> FiveFrameImage.collage_5_20()
            "collage_5_21.png" -> FiveFrameImage.collage_5_21()
            "collage_5_22.png" -> FiveFrameImage.collage_5_22()
            "collage_5_23.png" -> FiveFrameImage.collage_5_23()
            "collage_5_24.png" -> FiveFrameImage.collage_5_24()
            "collage_5_25.png" -> FiveFrameImage.collage_5_25()
            "collage_5_26.png" -> FiveFrameImage.collage_5_26()
            "collage_5_27.png" -> FiveFrameImage.collage_5_27()
            "collage_5_28.png" -> FiveFrameImage.collage_5_28()
            "collage_5_29.png" -> FiveFrameImage.collage_5_29()
            "collage_5_30.png" -> FiveFrameImage.collage_5_30()
            "collage_5_31.png" -> FiveFrameImage.collage_5_31()
            "collage_6_0.png" -> SixFrameImage.collage_6_0()
            "collage_6_1.png" -> SixFrameImage.collage_6_1()
            "collage_6_2.png" -> SixFrameImage.collage_6_2()
            "collage_6_3.png" -> SixFrameImage.collage_6_3()
            "collage_6_4.png" -> SixFrameImage.collage_6_4()
            "collage_6_5.png" -> SixFrameImage.collage_6_5()
            "collage_6_6.png" -> SixFrameImage.collage_6_6()
            "collage_6_7.png" -> SixFrameImage.collage_6_7()
            "collage_6_8.png" -> SixFrameImage.collage_6_8()
            "collage_6_9.png" -> SixFrameImage.collage_6_9()
            "collage_6_10.png" -> SixFrameImage.collage_6_10()
            "collage_6_11.png" -> SixFrameImage.collage_6_11()
            "collage_6_12.png" -> SixFrameImage.collage_6_12()
            "collage_6_13.png" -> SixFrameImage.collage_6_13()
            "collage_6_14.png" -> SixFrameImage.collage_6_14()
            "collage_7_0.png" -> SevenFrameImage.collage_7_0()
            "collage_7_1.png" -> SevenFrameImage.collage_7_1()
            "collage_7_2.png" -> SevenFrameImage.collage_7_2()
            "collage_7_3.png" -> SevenFrameImage.collage_7_3()
            "collage_7_4.png" -> SevenFrameImage.collage_7_4()
            "collage_7_5.png" -> SevenFrameImage.collage_7_5()
            "collage_7_6.png" -> SevenFrameImage.collage_7_6()
            "collage_7_7.png" -> SevenFrameImage.collage_7_7()
            "collage_7_8.png" -> SevenFrameImage.collage_7_8()
            "collage_7_9.png" -> SevenFrameImage.collage_7_9()
            "collage_7_10.png" -> SevenFrameImage.collage_7_10()
            "collage_8_0.png" -> EightFrameImage.collage_8_0()
            "collage_8_1.png" -> EightFrameImage.collage_8_1()
            "collage_8_2.png" -> EightFrameImage.collage_8_2()
            "collage_8_3.png" -> EightFrameImage.collage_8_3()
            "collage_8_4.png" -> EightFrameImage.collage_8_4()
            "collage_8_5.png" -> EightFrameImage.collage_8_5()
            "collage_8_6.png" -> EightFrameImage.collage_8_6()
            "collage_8_7.png" -> EightFrameImage.collage_8_7()
            "collage_8_8.png" -> EightFrameImage.collage_8_8()
            "collage_8_9.png" -> EightFrameImage.collage_8_9()
            "collage_8_10.png" -> EightFrameImage.collage_8_10()
            "collage_8_11.png" -> EightFrameImage.collage_8_11()
            "collage_8_12.png" -> EightFrameImage.collage_8_12()
            "collage_8_13.png" -> EightFrameImage.collage_8_13()
            "collage_8_14.png" -> EightFrameImage.collage_8_14()
            "collage_8_15.png" -> EightFrameImage.collage_8_15()
            "collage_8_16.png" -> EightFrameImage.collage_8_16()
            "collage_9_0.png" -> NineFrameImage.collage_9_0()
            "collage_9_1.png" -> NineFrameImage.collage_9_1()
            "collage_9_2.png" -> NineFrameImage.collage_9_2()
            "collage_9_3.png" -> NineFrameImage.collage_9_3()
            "collage_9_4.png" -> NineFrameImage.collage_9_4()
            "collage_9_5.png" -> NineFrameImage.collage_9_5()
            "collage_9_6.png" -> NineFrameImage.collage_9_6()
            "collage_9_7.png" -> NineFrameImage.collage_9_7()
            "collage_9_8.png" -> NineFrameImage.collage_9_8()
            "collage_9_9.png" -> NineFrameImage.collage_9_9()
            "collage_9_10.png" -> NineFrameImage.collage_9_10()
            "collage_9_11.png" -> NineFrameImage.collage_9_11()
            "collage_10_0.png" -> TenFrameImage.collage_10_0()
            "collage_10_1.png" -> TenFrameImage.collage_10_1()
            "collage_10_2.png" -> TenFrameImage.collage_10_2()
            "collage_10_3.png" -> TenFrameImage.collage_10_3()
            "collage_10_4.png" -> TenFrameImage.collage_10_4()
            "collage_10_5.png" -> TenFrameImage.collage_10_5()
            "collage_10_6.png" -> TenFrameImage.collage_10_6()
            "collage_10_7.png" -> TenFrameImage.collage_10_7()
            "collage_10_8.png" -> TenFrameImage.collage_10_8()
            else -> null
        }
    }
}
