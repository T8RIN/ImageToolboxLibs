package io.rektplorer64.pickerkt.collection.model

import android.text.format.DateUtils
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import io.rektplorer64.pickerkt.R
import io.rektplorer64.pickerkt.common.data.Nameable
import io.rektplorer64.pickerkt.common.property.Identifiable
import io.rektplorer64.pickerkt.common.unit.Byte
import io.rektplorer64.pickerkt.content.model.Content
import io.rektplorer64.pickerkt.contentresolver.MimeType
import org.threeten.bp.Instant


interface CollectionBase : Identifiable<String>, Nameable

data class Collection(
    override val id: String,
    override val name: String,
    @StringRes override val nameStringRes: Int? = null,
    val contentCount: Int,
    val timeAdded: Instant,
    val size: Byte,
    val lastContentItem: Content?,
    val contentGroupCounts: Map<MimeType, Int>?
) : CollectionBase {
    val relativeTimeString: String
        @Composable get() {
            return DateUtils.getRelativeTimeSpanString(LocalContext.current, timeAdded.toEpochMilli(), true).toString()
        }
}

val Collection.finalName: String
    @Composable get() {
        return nameStringRes?.let { stringResource(it) }
            ?: name.takeIf { it.isNotEmpty() }
            ?: stringResource(R.string.common_unknown)
    }

