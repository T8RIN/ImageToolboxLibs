package com.photoeditor.photoeffect.model

import com.photoeditor.photoeffect.template.PhotoItem

/**
 * Created by vanhu_000 on 3/25/2016.
 */
class TemplateItem : ImageTemplate {
    var sectionManager: Int = 0
    var sectionFirstPosition: Int = 0
    var isHeader = false
    var header: String? = null

    var photoItemList: ArrayList<PhotoItem> = ArrayList()

    constructor()

    constructor(template: ImageTemplate) {
        languages = template.languages
        packageId = template.packageId
        preview = template.preview
        mtemplate = template.mtemplate
        child = template.child
        title = template.title
        thumbnail = template.thumbnail
        selectedThumbnail = template.selectedThumbnail
        isSelected = template.isSelected
        // To be used to display
        showingType = template.showingType
        // To be used in database
        lastModified = template.lastModified
        status = template.status
        id = template.id
        photoItemList = parseImageTemplate(template)
    }
}

private fun parseImageTemplate(template: ImageTemplate): ArrayList<PhotoItem> {
    val photoItems = ArrayList<PhotoItem>()
    try {
        val childTexts =
            template.child!!.split(";".toRegex()).dropLastWhile { it.isEmpty() }
                .toTypedArray()
        if (childTexts != null) {
            for (child in childTexts) {
                val properties =
                    child.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                if (properties != null) {
                    val item = PhotoItem()
                    item.index = Integer.parseInt(properties[0])
                    item.x = Integer.parseInt(properties[1]).toFloat()
                    item.y = Integer.parseInt(properties[2]).toFloat()
                    item.maskPath = properties[3]
                    photoItems.add(item)
                }
            }
            //Sort via index
            photoItems.sortWith(Comparator { lhs, rhs -> rhs.index - lhs.index })
        }
    } catch (ex: Exception) {
        ex.printStackTrace()
    }

    return photoItems
}
