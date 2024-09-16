package com.photoeditor.photoeffect.frame

import android.graphics.Matrix
import android.net.Uri
import android.os.Parcel
import android.os.Parcelable

class FrameEntity : Parcelable {

    var image: Uri? = null
    private val mMatrix = Matrix()

    var matrix: Matrix
        get() = Matrix(mMatrix)
        set(matrix) = mMatrix.set(matrix)

    constructor()

    private constructor(`in`: Parcel) {
        val values = FloatArray(9)
        `in`.readFloatArray(values)
        mMatrix.setValues(values)
        image = `in`.readParcelable(Uri::class.java.classLoader)
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        val values = FloatArray(9)
        mMatrix.getValues(values)
        dest.writeFloatArray(values)
        dest.writeParcelable(image, flags)

    }

    companion object CREATOR : Parcelable.Creator<FrameEntity> {
        override fun createFromParcel(parcel: Parcel): FrameEntity {
            return FrameEntity(parcel)
        }

        override fun newArray(size: Int): Array<FrameEntity?> {
            return arrayOfNulls(size)
        }
    }
}
