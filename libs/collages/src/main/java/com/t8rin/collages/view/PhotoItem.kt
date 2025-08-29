package com.t8rin.collages.view

import android.graphics.Path
import android.graphics.PointF
import android.graphics.RectF
import android.net.Uri

internal data class PhotoItem(
    //Primary info
    var index: Int = 0,
    var imagePath: Uri? = null,
    //Using point list to construct view. All points and width, height are in [0, 1] range.
    var pointList: ArrayList<PointF> = ArrayList(),
    var bound: RectF = RectF(),
    //Using path to create
    var path: Path? = null,
    var pathRatioBound: RectF? = null,
    var pathInCenterHorizontal: Boolean = false,
    var pathInCenterVertical: Boolean = false,
    var pathAlignParentRight: Boolean = false,
    var pathScaleRatio: Float = 1f,
    var fitBound: Boolean = false,
    //other info
    var hasBackground: Boolean = false,
    var shrinkMethod: Int = SHRINK_METHOD_DEFAULT,
    var cornerMethod: Int = CORNER_METHOD_DEFAULT,
    var disableShrink: Boolean = false,
    var shrinkMap: HashMap<PointF, PointF>? = null,
    //Clear polygon or arc area
    var clearAreaPoints: ArrayList<PointF>? = null,
    //Clear an area using path
    var clearPath: Path? = null,
    var clearPathRatioBound: RectF? = null,
    var clearPathInCenterHorizontal: Boolean = false,
    var clearPathInCenterVertical: Boolean = false,
    var clearPathScaleRatio: Float = 1f,
    var centerInClearBound: Boolean = false,
) {

    companion object {
        const val SHRINK_METHOD_DEFAULT = 0
        const val SHRINK_METHOD_3_3 = 1
        const val SHRINK_METHOD_USING_MAP = 2
        const val SHRINK_METHOD_3_6 = 3
        const val SHRINK_METHOD_3_8 = 4
        const val SHRINK_METHOD_COMMON = 5
        const val CORNER_METHOD_DEFAULT = 0
        const val CORNER_METHOD_3_6 = 1
        const val CORNER_METHOD_3_13 = 2
    }
}
