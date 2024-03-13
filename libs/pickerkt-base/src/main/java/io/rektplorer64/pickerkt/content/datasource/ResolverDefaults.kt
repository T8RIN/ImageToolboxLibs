package io.rektplorer64.pickerkt.content.datasource

import android.database.Cursor
import android.provider.MediaStore
import io.rektplorer64.pickerkt.common.unit.Byte
import io.rektplorer64.pickerkt.content.model.Content
import io.rektplorer64.pickerkt.contentresolver.BUCKET_ID
import io.rektplorer64.pickerkt.contentresolver.MimeType
import org.threeten.bp.Instant

internal val CONTENT_LOADER_PROJECTION = arrayOf(
    MediaStore.MediaColumns._ID,
    MediaStore.MediaColumns.DISPLAY_NAME,
    MediaStore.MediaColumns.DATE_ADDED,
    MediaStore.MediaColumns.MIME_TYPE,
    MediaStore.MediaColumns.SIZE,
    BUCKET_ID
)

internal fun Cursor.parseResolverRow(): Content {
    val id = getLong(getColumnIndexOrThrow(MediaStore.MediaColumns._ID))
    val displayName = getString(getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME))
    val dateAdded = getLong(getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_ADDED))
    val mimeType = getString(getColumnIndexOrThrow(MediaStore.MediaColumns.MIME_TYPE))
    val byteSize = getLong(getColumnIndexOrThrow(MediaStore.MediaColumns.SIZE))
    val bucketId = getString(getColumnIndexOrThrow(BUCKET_ID))

    return Content(
        id = id,
        name = displayName,
        dateAdded = Instant.ofEpochSecond(dateAdded),
        mimeType = MimeType.of(mimeType),
        size = Byte(byteSize),
        collectionId = bucketId
    )
}