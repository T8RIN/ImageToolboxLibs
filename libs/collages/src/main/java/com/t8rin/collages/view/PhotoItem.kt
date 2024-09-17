package com.t8rin.collages.view

import android.graphics.Path
import android.graphics.PointF
import android.graphics.RectF
import android.net.Uri


internal class PhotoItem {
    //Primary info
    var index = 0
    var imagePath: Uri? = null

    //Using point list to construct view. All points and width, height are in [0, 1] range.
    var pointList = ArrayList<PointF>()
    var bound = RectF()

    //Using path to create
    var path: Path? = null
    var pathRatioBound: RectF? = null
    var pathInCenterHorizontal = false
    var pathInCenterVertical = false
    var pathAlignParentRight = false
    var pathScaleRatio = 1f
    var fitBound = false

    //other info
    var hasBackground = false
    var shrinkMethod = SHRINK_METHOD_DEFAULT
    var cornerMethod = CORNER_METHOD_DEFAULT
    var disableShrink = false
    var shrinkMap: HashMap<PointF, PointF>? = null

    //Clear polygon or arc area
    var clearAreaPoints: ArrayList<PointF>? = null

    //Clear an area using path
    var clearPath: Path? = null
    var clearPathRatioBound: RectF? = null
    var clearPathInCenterHorizontal = false
    var clearPathInCenterVertical = false
    var clearPathScaleRatio = 1f
    var centerInClearBound = false

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
