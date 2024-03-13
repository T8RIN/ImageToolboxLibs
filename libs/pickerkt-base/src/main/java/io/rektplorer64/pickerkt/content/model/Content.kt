package io.rektplorer64.pickerkt.content.model

import android.content.ContentUris
import android.net.Uri
import android.os.Parcelable
import android.text.format.DateUtils
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.squareup.moshi.JsonClass
import io.rektplorer64.pickerkt.common.property.Identifiable
import io.rektplorer64.pickerkt.common.serializer.InstantSerializer
import io.rektplorer64.pickerkt.common.unit.Byte
import io.rektplorer64.pickerkt.contentresolver.MimeType
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable
import org.threeten.bp.Instant
import org.threeten.bp.ZoneId
import org.threeten.bp.temporal.ChronoUnit

@Parcelize
@Serializable
data class Content(
    override val id: Long,
    override val name: String,
    @Serializable(with = InstantSerializer::class) val dateAdded: Instant,
    val mimeType: MimeType,
    val size: Byte,
    val collectionId: String
) : Identifiable<Long>, Comparable<Content>, Parcelable {

    @IgnoredOnParcel
    val type: MimeType.Group
        get() = mimeType.group

    @IgnoredOnParcel
    val uri: Uri
        get() = ContentUris.withAppendedId(type.toMediaStoreExternalUri(), id)

    @IgnoredOnParcel
    val relativeTimeString: String
        @Composable get() {
            return DateUtils.getRelativeTimeSpanString(LocalContext.current, dateAdded.toEpochMilli(), true).toString()
        }

    override fun compareTo(other: Content): Int {
        return id.compareTo(other.id)
    }
}

fun List<Content>.groupByCollectionAndCount(): Map<String, Int> =
    groupBy { it.collectionId }.mapValues { it.value.size }

fun List<Content>.groupByMimeTypeAndCount(): Map<MimeType, Int> =
    groupBy { it.mimeType }.mapValues { it.value.size }

fun List<Content>.groupByMimeTypeGroupAndCount(): Map<MimeType.Group, Int> =
    groupBy { it.mimeType.group }.mapValues { it.value.size }

fun Map<Long, Pair<Instant, Content>>.timeSortedValues(): List<Content> = values
    .sortedBy { it.first }
    .map { it.second }

fun List<Pair<Instant, Content>>.timeSortedValues(): List<Content> = this
    .sortedBy { it.first }
    .map { it.second }

infix fun Content.shouldSeparateApartFrom(content: Content): Boolean {
    val localDate1 = dateAdded.atZone(ZoneId.systemDefault())
    val localDate2 = content.dateAdded.atZone(ZoneId.systemDefault())
    return localDate1.truncatedTo(ChronoUnit.DAYS) != localDate2.truncatedTo(ChronoUnit.DAYS)
}