package io.rektplorer64.pickerkt.collection.datasource

import android.content.Context
import android.content.res.Resources
import android.database.ContentObserver
import android.database.Cursor
import android.net.Uri
import android.os.Handler
import android.os.Looper
import io.rektplorer64.pickerkt.builder.PickerKtConfiguration
import io.rektplorer64.pickerkt.builder.query.operand.ContentColumn
import io.rektplorer64.pickerkt.builder.query.operand.valueOf
import io.rektplorer64.pickerkt.collection.model.Collection
import io.rektplorer64.pickerkt.common.data.Result
import io.rektplorer64.pickerkt.common.data.datasource.ListingSource
import io.rektplorer64.pickerkt.common.data.transform
import io.rektplorer64.pickerkt.common.unit.Byte
import io.rektplorer64.pickerkt.content.model.Content
import io.rektplorer64.pickerkt.contentresolver.*
import io.rektplorer64.pickerkt.util.forEachIndexed
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.*

class CollectionListingSource(
    coroutineScope: CoroutineScope,
    private val context: Context,
    private val uri: Uri,
    private val selection: String,
    private val selectionArguments: Array<String>?,
    private val sortOrder: String?
) : ListingSource<Collection>() {

    constructor(
        coroutineScope: CoroutineScope,
        context: Context,
        config: PickerKtConfiguration
    ) : this(
        coroutineScope = coroutineScope,
        context = context,
        uri = MimeType.Group.Unknown.toMediaStoreExternalUri(),
//        selection = "",
        selection = config.predicateString ?: "",
//        selectionArguments = null,
        selectionArguments = config.predicateArgumentString,
//        sortOrder = null
        sortOrder = config.orderByString
    ) {
        Timber.d("Creating a New List Flow: predicate=${config.predicateString}, predicateArgs=${config.predicateArgumentString?.toList()}, orderBy=${config.orderByString}")
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

    override suspend fun fetchData(): Result<List<Collection>> {
        val queryCursor = context.contentResolver.query(
            uri,
            COLLECTION_LOADER_PROJECTION,
            selection,
            selectionArguments,
            sortOrder,
        ) ?: return Result.Error(throwable = IllegalStateException("Empty result"), data = null)

        if (queryCursor.count == 0) {
            return Result.Success(data = listOf())
        }

        Timber.d("Query Cursor Columns: ${queryCursor.columnNames.joinToString(separator = ", ")}")

        return try {
            val listOfCollections = queryCursor.parseQueryCursor().toMutableList()

            // If no BUCKET_ID specified, display contents from any folder.
            if (!selection.contains(BUCKET_ID)) {
                listOfCollections.formulateTheAllFoldersCollection()?.let {
                    listOfCollections.add(0, it)
                }
            }

            Result.Success(data = listOfCollections)
        } catch (e: Exception) {
            Timber.e(e, "An error occurred while getting a new page.")
            Result.Error(throwable = e, data = null)
        } finally {
            queryCursor.close()
        }
    }

    private fun Cursor.parseQueryCursor(): List<Collection> {
        val map = mutableMapOf<String, Pair<Int, Collection>>()

        forEachIndexed {
            val (contentRow, tempCollection) = parseResolverRow()
            val collectionId = tempCollection.id

            map.putIfAbsent(collectionId, (map.size + 1) to tempCollection)
            map[collectionId] = map[collectionId]!!.copy(
                second = map[collectionId]!!.second.let { accumulatorCollection ->
                    accumulatorCollection.copy(
                        size = accumulatorCollection.size + contentRow.size,
                        contentCount = accumulatorCollection.contentCount + 1
                    ).apply {
                        contentGroupCounts as EnumMap<MimeType, Int>
                        contentGroupCounts.also {
                            tempCollection.contentGroupCounts?.forEach { (t, _) ->
                                it[t] = (it[t] ?: 0) + 1
                            }
                        }
                    }
                }
            )
        }

        return map.values.sortedBy { it.first }.map { it.second }
    }

    private fun List<Collection>.formulateTheAllFoldersCollection(): Collection? {
        var totalByteSize = Byte(0L)
        var totalContentCount = 0
        var theLastContentItem: Content? = null
        val mimeGroupMap = mutableMapOf<MimeType, Int>()

        forEach { collection ->
            totalByteSize += collection.size
            totalContentCount += collection.contentCount

            collection.contentGroupCounts?.forEach { (mimeGroup, count) ->
                mimeGroupMap[mimeGroup] = (mimeGroupMap[mimeGroup] ?: 0) + count
            }

            if (theLastContentItem == null || theLastContentItem!!.dateAdded.toEpochMilli() < collection.lastContentItem?.dateAdded?.toEpochMilli() ?: 0) {
                theLastContentItem = collection.lastContentItem
            }
        }

        val allTimeLastContentItem = theLastContentItem ?: return null

        return Collection(
            id = AllFoldersCollection.id,
            name = context.getString(AllFoldersCollection.nameStringRes),
            timeAdded = allTimeLastContentItem.dateAdded,
            size = totalByteSize,
            contentGroupCounts = mimeGroupMap,
            contentCount = totalContentCount,
            lastContentItem = allTimeLastContentItem
        )
    }
}

@Suppress("FunctionName")
fun CollectionFlow(
    coroutineScope: CoroutineScope,
    context: Context,
    collectionId: Long?,
    config: PickerKtConfiguration
): Flow<Result<Collection>> {
    val wildcardOnly = collectionId == null
    val modifiedConfig = config.asBuilder()
        .apply {
            if (!wildcardOnly) {
                predicate {
                    ContentColumn(ContentResolverColumn.CollectionId) equal valueOf(collectionId!!)
                }
            }
        }
        .build()
        .also {
            Timber.d("[X] Config: $it")
        }

    return CollectionListingSource(
        coroutineScope = coroutineScope,
        context = context,
        config = modifiedConfig
    ).flow
        .onEach { Timber.d("[X] New Instance: $it") }
        .map { result ->
            result.transform {
                if (it == null) {
                    throw Resources.NotFoundException()
                } else {
                    when {
                        it.size == 1 -> it.lastOrNull()
                            ?: throw Resources.NotFoundException()
                        wildcardOnly -> it.firstOrNull()
                            ?: throw Resources.NotFoundException()
                        else -> throw Resources.NotFoundException()
                    }
                }
            }
        }
        .catch {
            Timber.e(it, "Failed to retrieve a collection with an ID of $collectionId")
            emit(Result.Error(throwable = it, data = null))
        }
        .onEach { Timber.d("[X] Mapped: $it") }
}