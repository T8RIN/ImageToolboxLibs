package com.t8rin.collages.model

import android.os.Parcel
import android.os.Parcelable

/**
 * Field names are used in Gson. So Don't rename if file 'package.json' doesn't
 * change field names.
 *
 * @author vanhu_000
 */
internal open class ItemInfo : Parcelable {

    var title: String? = null
    var thumbnail: String? = null
    var selectedThumbnail: String? = null
    var isSelected = false

    // To be used to display
    var showingType = NORMAL_ITEM_TYPE

    // To be used in database
    var lastModified: String? = null
    var status: String? = null
    var id: Long = 0

    constructor()

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(title)
        dest.writeString(thumbnail)
        dest.writeString(selectedThumbnail)
        dest.writeBooleanArray(booleanArrayOf(isSelected))
        // To be used to display
        dest.writeInt(showingType)
        // To be used in database
        dest.writeString(lastModified)
        dest.writeString(status)
        dest.writeLong(id)
    }

    protected constructor(`in`: Parcel) {
        title = `in`.readString()
        thumbnail = `in`.readString()
        selectedThumbnail = `in`.readString()
        val b = BooleanArray(1)
        `in`.readBooleanArray(b)
        isSelected = b[0]
        showingType = `in`.readInt()
        lastModified = `in`.readString()
        status = `in`.readString()
        id = `in`.readLong()
    }

    companion object CREATOR : Parcelable.Creator<ItemInfo> {

        val STATUS_ACTIVE = "active"
        val STATUS_DELETED = "deleted"

        val NORMAL_ITEM_TYPE = 0
        val PACKAGE_ITEM_TYPE = 1
        val ADD_ITEM_TYPE = 2
        val SQUARE_CROP_TYPE = 3
        val CUSTOM_CROP_TYPE = 4
        val DRAW_CROP_TYPE = 5

        override fun createFromParcel(parcel: Parcel): ItemInfo {
            return ItemInfo(parcel)
        }

        override fun newArray(size: Int): Array<ItemInfo?> {
            return arrayOfNulls(size)
        }
    }
}
