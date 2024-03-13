package io.rektplorer64.pickerkt.collection.datasource

import android.database.Cursor
import android.provider.MediaStore
import io.rektplorer64.pickerkt.collection.model.Collection
import io.rektplorer64.pickerkt.common.unit.Byte
import io.rektplorer64.pickerkt.common.unit.asByteUnit
import io.rektplorer64.pickerkt.content.model.Content
import io.rektplorer64.pickerkt.contentresolver.BUCKET_DISPLAY_NAME
import io.rektplorer64.pickerkt.contentresolver.BUCKET_ID
import io.rektplorer64.pickerkt.contentresolver.MimeType
import org.threeten.bp.Instant
import timber.log.Timber
import java.util.*

internal val COLLECTION_LOADER_PROJECTION = arrayOf(
    MediaStore.MediaColumns._ID,
    MediaStore.MediaColumns.DISPLAY_NAME,
    MediaStore.MediaColumns.DATE_ADDED,
    MediaStore.MediaColumns.MIME_TYPE,
    MediaStore.MediaColumns.SIZE,
    BUCKET_ID,
    BUCKET_DISPLAY_NAME
)

internal fun Cursor.parseResolverRow(): Pair<Content, Collection> {
    val id = getLong(getColumnIndexOrThrow(MediaStore.MediaColumns._ID))
    val displayName = getString(getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME))
    val dateAdded = getLong(getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_ADDED))
    val mimeType = MimeType.of(getString(getColumnIndexOrThrow(MediaStore.MediaColumns.MIME_TYPE)) ?: MimeType.Unknown.name)
    val byteSize = getLong(getColumnIndexOrThrow(MediaStore.MediaColumns.SIZE))
    val bucketId = getString(getColumnIndexOrThrow(BUCKET_ID)) ?: "1"
    val bucketDisplayName = getString(getColumnIndexOrThrow(BUCKET_DISPLAY_NAME))

    val contentRow = Content(
        id = id,
        name = displayName ?: "Unknown",
        dateAdded = Instant.ofEpochSecond(dateAdded),
        mimeType = mimeType,
        size = Byte(byteSize),
        collectionId = bucketId
    )
    return contentRow to Collection(
        id = bucketId,
        name = (bucketDisplayName ?: "unknown"),
        size = 0.asByteUnit(),
        contentCount = 0,
        lastContentItem = contentRow,
        timeAdded = contentRow.dateAdded,
        contentGroupCounts = EnumMap(mapOf(mimeType to 0))
    )
}