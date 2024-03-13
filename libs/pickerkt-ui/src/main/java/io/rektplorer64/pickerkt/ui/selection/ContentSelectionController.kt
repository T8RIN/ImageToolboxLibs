package io.rektplorer64.pickerkt.ui.selection

import android.widget.Toast
import androidx.annotation.Keep
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState.Companion.Saver
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.mapSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.*
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.rektplorer64.pickerkt.common.property.groupById
import io.rektplorer64.pickerkt.common.serializer.InstantSerializer
import io.rektplorer64.pickerkt.common.unit.Byte
import io.rektplorer64.pickerkt.content.model.Content
import io.rektplorer64.pickerkt.content.model.timeSortedValues
import io.rektplorer64.pickerkt.contentresolver.MimeType
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.threeten.bp.Instant

@Composable
fun rememberContentSelectionController(
    initialSelections: List<Content> = emptyList(),
    maxSelection: Int = 100,
    onSelectionChanged: ((List<Content>) -> Unit)? = null
): ContentSelectionController {
    val controller = rememberSaveable(initialSelections, saver = ContentSelectionController.Saver) {
        ContentSelectionController(initialSelections, maxSelection = maxSelection)
    }

    LaunchedEffect(controller.selection.keys.joinToString(separator = ",")) {

        val timeSortedValues = controller.selection.timeSortedValues().take(maxSelection)
        controller.canonicalSelectionList.clear()
        controller.canonicalSelectionList.addAll(timeSortedValues)

        controller.canonicalSelectionOrderMap.clear()
        controller.canonicalSelectionOrderMap.putAll(timeSortedValues.mapIndexed { i, it -> it.id to i })

        onSelectionChanged?.invoke(controller.canonicalSelectionList)
    }

    return controller
}

class ContentSelectionController internal constructor(
    initialSelections: List<Content>,
    val maxSelection: Int = Int.MAX_VALUE,
) :
    SelectionController<Content, Long> {

    // TODO: We could use StateObject or something
    internal val selection = mutableStateMapOf(
        *initialSelections
            .take(maxSelection)
            .map { it.id to (Instant.now() to it) }
            .toTypedArray()
    )

    private fun addNewSelection(item: Content, timestamp: Instant = Instant.now()): Boolean {
        val beforeCount = selection.size

        if (beforeCount >= maxSelection) {
            return false
        }

        selection.putIfAbsent(
            item.id,
            (selection[item.id]?.first ?: timestamp) to item
        )

        return selection.size > beforeCount
    }

    private fun removeNewSelection(item: Content): Boolean {
        val beforeCount = selection.size

        if (item in this) {
            selection.remove(item.id)
        }

        return selection.size < beforeCount
    }

    override val size: Int
        get() = selection.size

    override operator fun contains(itemId: Long) = itemId in selection

    override operator fun contains(item: Content) = item.id in selection

    override fun select(item: Content): Boolean {
        return addNewSelection(item)
    }

    override fun select(item: List<Content>) {
        var timestamp = Instant.now().toEpochMilli()
        item.forEach {
            addNewSelection(it, timestamp = Instant.ofEpochMilli(timestamp))
            timestamp++
        }
    }

    override fun unselect(item: Content): Boolean {
        return removeNewSelection(item)
    }

    override fun toggleSelection(item: Content) {
        if (item.id !in this) select(item) else unselect(item)
    }

    override fun clear() = selection.clear()

    override fun invert(items: Collection<Content>) {
        val selectedItem = selection.toMap()

        selection.clear()
        items.forEach {
            if (it.id !in selectedItem) {
                selection.putIfAbsent(it.id, Instant.now() to it)
            }
        }
    }

    override fun toString(): String {
        return "CollectionSelectionController(selection=$selection)"
    }

    override val canonicalSelectionList = mutableStateListOf<Content>()

    override val canonicalSelectionOrderMap = mutableStateMapOf<Long, Int>()

    override fun replaceAllWith(newSelection: List<Content>) {
        val selectionSet = selection.map { x -> x.key }.toHashSet()
        val tempSelectionMap = newSelection.groupById()
        val tempSelectionSet = tempSelectionMap.keys

        val idToBeRemoved = selectionSet - tempSelectionSet
        val idToBeAdded = tempSelectionSet - selectionSet

        idToBeRemoved.forEach { id -> selection.remove(id) }
        idToBeAdded.forEach { id ->
            selection.putIfAbsent(
                id,
                Instant.now() to tempSelectionMap[id]!!.first()
            )
        }
    }

    override fun removeIf(predicate: (Content) -> Boolean) {
        selection.forEach { (t, u) ->
            if (predicate(u.second)) {
                selection.remove(t)
            }
        }
    }

    @Keep
    @Serializable
    private class ContentSelectionSaverEntry(
        @Serializable(with = InstantSerializer::class) val timeSelected: Instant,
        val content: Content
    ) {
        constructor(pair: Pair<Instant, Content>) : this(pair.first, pair.second)

        fun toPair(): Pair<Instant, Content> = timeSelected to content
    }

    companion object {
        /**
         * The default [Saver] implementation for [ContentSelectionController].
         */
        val Saver = mapSaver(
            save = {
                it.selection
                    .mapKeys { it.key.toString() }
                    .mapValues { Json.encodeToString(ContentSelectionSaverEntry(it.value)) }
            },
            restore = {
                ContentSelectionController(
                    initialSelections = it.values.toList()
                        .map {
                            Json.decodeFromString<ContentSelectionSaverEntry>(it as String).toPair()
                        }
                        .timeSortedValues()
                )
            }
        )
    }
}


@Composable
@Preview
private fun Test() {
    val contents = remember {
        (0 until 10)
            .map {
                Content(
                    id = it.toLong(),
                    name = "Hello word $it",
                    dateAdded = Instant.now(),
                    mimeType = MimeType.Jpeg,
                    size = Byte(100L),
                    collectionId = "Collection $it",
                )
            }
    }
    val context = LocalContext.current
    val controller = rememberContentSelectionController(
        maxSelection = 10
    ) {
        Toast.makeText(context, "Selection: ${it.size} items", Toast.LENGTH_SHORT).show()
    }

    Column {
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = { controller.invert(contents) },
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            Text("INVERT SELECTION")
        }

        contents.forEach {
            Row(
                modifier = Modifier.padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(it.id.toString() + " (${it.id in controller})")
                Button(onClick = {
                    if (it !in controller) {
                        controller.select(it)
                    } else {
                        controller.unselect(it)
                    }
                }) {
                    Text(text = "SELECT")
                }
            }
        }
    }
}