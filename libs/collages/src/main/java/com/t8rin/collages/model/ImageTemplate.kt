package com.photoeditor.photoeffect.model

import android.net.Uri
import android.os.Parcel
import android.os.Parcelable
import androidx.core.net.toUri

/**
 * Created by vanhu_000 on 3/17/2016.
 */
open class ImageTemplate : ItemInfo {
    var languages: Array<Language?> = emptyArray()
    var packageId: Long = 0
    var preview: Uri? = null
    var mtemplate: String? = null
    var child: String? = null

    constructor()

    override fun writeToParcel(dest: Parcel, flags: Int) {
        super.writeToParcel(dest, flags)
        var len = 0
        if (languages != null && languages.size > 0) {
            len = languages.size
        }
        dest.writeInt(len)
        if (languages != null && len > 0) {
            dest.writeTypedArray(languages, flags)
        }

        dest.writeLong(packageId)
        dest.writeString(preview?.toString())
        dest.writeString(mtemplate)
        dest.writeString(child)
    }

    protected constructor(`in`: Parcel) : super(`in`) {
        val len = `in`.readInt()
        if (len > 0) {
            languages = arrayOfNulls(len)
            `in`.readTypedArray(languages, Language.CREATOR)
        }
        packageId = `in`.readLong()
        preview = `in`.readString()?.toUri()
        mtemplate = `in`.readString()
        child = `in`.readString()
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ImageTemplate> {
        override fun createFromParcel(parcel: Parcel): ImageTemplate {
            return ImageTemplate(parcel)
        }

        override fun newArray(size: Int): Array<ImageTemplate?> {
            return arrayOfNulls(size)
        }
    }
}