package io.rektplorer64.pickerkt.content.datasource

import android.content.Context
import android.content.res.Resources
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.os.Looper
import io.rektplorer64.pickerkt.builder.PickerKt
import io.rektplorer64.pickerkt.builder.PickerKtConfiguration
import io.rektplorer64.pickerkt.builder.query.operand.ContentColumn
import io.rektplorer64.pickerkt.builder.query.operand.valueOf
import io.rektplorer64.pickerkt.common.data.Result
import io.rektplorer64.pickerkt.common.data.datasource.SingleItemSource
import io.rektplorer64.pickerkt.content.model.Content
import io.rektplorer64.pickerkt.contentresolver.ContentResolverColumn
import io.rektplorer64.pickerkt.contentresolver.MimeType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber


@Suppress("FunctionName")
fun ContentSingleSource(
    coroutineScope: CoroutineScope,
    context: Context,
    contentId: Long
): ContentSingleSource {
    return ContentSingleSource(
        coroutineScope = coroutineScope,
        context = context,
        config = PickerKt.picker {
            predicate {
                ContentColumn(ContentResolverColumn.ContentId) equal valueOf(contentId)
            }
        }
    )
}

class ContentSingleSource(
    coroutineScope: CoroutineScope,
    private val context: Context,
    private val uri: Uri,
    private val selection: String,
    private val selectionArguments: Array<String>?
) : SingleItemSource<Content>() {

    internal constructor(
        coroutineScope: CoroutineScope,
        context: Context,
        config: PickerKtConfiguration
    ) : this(
        coroutineScope = coroutineScope,
        context = context,
        uri = MimeType.Group.Unknown.toMediaStoreExternalUri(),
        selection = config.predicateString ?: "",
        selectionArguments = config.predicateArgumentString
    ) {
        Timber.d("Creating a SingleSource Flow: predicate=${config.predicateString}, predicateArgs=${config.predicateArgumentString?.toList()}")
    }

    init {
        coroutineScope.launch {
            context.contentResolver.registerContentObserver(
                uri,
                true,
                object : ContentObserver(Handler(Looper.myLooper()!!)) {
                    override fun onChange(selfChange: Boolean) {
                        refresh()
                    }
                }
            )
        }
    }

    override suspend fun fetchData(): Result<Content> {
        val queryCursor = withContext(Dispatchers.IO) {
            context.contentResolver.query(
                uri,
                CONTENT_LOADER_PROJECTION,
                selection,
                selectionArguments,
                null,
            )
        }

        if (queryCursor == null || queryCursor.count == 0) {
            return Result.Error(data = null, Resources.NotFoundException())
        }

        queryCursor.moveToFirst()
        val content = try {
            queryCursor.parseResolverRow()
        } catch (e: Exception) {
            Timber.e(e, "Failed to parse the content row.")
            return Result.Error(data = null, e)
        } finally {
            queryCursor.close()
        }

        return Result.Success(content)
    }
}