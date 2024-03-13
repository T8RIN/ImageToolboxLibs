package io.rektplorer64.pickerkt.contentresolver

import android.provider.MediaStore

enum class ContentResolverColumn(val columnName: String) {
    Name(columnName = MediaStore.MediaColumns.DISPLAY_NAME),
    ContentMimeType(columnName = MediaStore.MediaColumns.MIME_TYPE),
    ByteSize(columnName = MediaStore.MediaColumns.SIZE),
    DateAdded(columnName = MediaStore.MediaColumns.DATE_ADDED),
    DateModified(columnName = MediaStore.MediaColumns.DATE_MODIFIED),
    CollectionId(columnName = BUCKET_ID),
    ContentId(columnName = CONTENT_ID),
    CollectionName(columnName = BUCKET_DISPLAY_NAME)
}