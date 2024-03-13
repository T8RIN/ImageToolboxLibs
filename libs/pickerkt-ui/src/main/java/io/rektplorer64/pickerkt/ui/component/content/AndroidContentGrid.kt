package io.rektplorer64.pickerkt.ui.component.content

import android.annotation.SuppressLint
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.viewinterop.AndroidView
import androidx.paging.compose.LazyPagingItems
import androidx.recyclerview.selection.*
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import io.rektplorer64.pickerkt.content.model.Content
import io.rektplorer64.pickerkt.ui.common.recyclerview.ComposeRecyclerViewAdapter

private val ContentDiffCallback = object : DiffUtil.ItemCallback<Content>() {
    override fun areItemsTheSame(oldItem: Content, newItem: Content): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Content, newItem: Content): Boolean {
        return oldItem == newItem
    }
}

private class ContentItemAdapter() :
    ComposeRecyclerViewAdapter<Content, ContentItemAdapter.ViewHolder>(ContentDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ComposeView(parent.context))
    }

    class ViewHolder(composeView: View) : ComposeViewHolder<Content>(composeView) {

        override val data: MutableState<Content?> = mutableStateOf(null)

        @Composable
        @SuppressLint("ComposableNaming")
        override fun item(data: Content?) {
            Text(text = data?.id?.toString() ?: "Unknown")
        }

        fun getItemDetails(): ItemDetailsLookup.ItemDetails<Long> =
            object : ItemDetailsLookup.ItemDetails<Long>() {
                override fun getPosition(): Int = absoluteAdapterPosition
                override fun getSelectionKey(): Long = data.value!!.id
            }
    }
}

class MyItemDetailsLookup(private val recyclerView: RecyclerView) :
    ItemDetailsLookup<Long>() {
    override fun getItemDetails(event: MotionEvent): ItemDetails<Long>? {
        val view = recyclerView.findChildViewUnder(event.x, event.y)
        if (view != null) {
            return (recyclerView.getChildViewHolder(view) as ContentItemAdapter.ViewHolder).getItemDetails()
        }
        return null
    }
}

private fun RecyclerView.bindSelectionTracker(): SelectionTracker<Long> {
    return SelectionTracker
        .Builder(
            "",
            this,
            StableIdKeyProvider(this),
            MyItemDetailsLookup(this),
            StorageStrategy.createLongStorage()
        )
        .withSelectionPredicate(
            SelectionPredicates.createSelectAnything()
        )
        .build()
}

@Composable
fun ContentGrid(
    modifier: Modifier = Modifier,
    scrollConnection: NestedScrollConnection? = null,
    content: LazyPagingItems<Content>,
    selectionVisible: Boolean = false,
    selection: List<Long>,
    onContentItemClick: (Content) -> Unit,
) {
    AndroidView(
        factory = {
            RecyclerView(it).apply {
                adapter = ContentItemAdapter()
                bindSelectionTracker()
            }
        },
        update = {
            (it.adapter as ComposeRecyclerViewAdapter<Content, ContentItemAdapter.ViewHolder>).submitList(content.itemSnapshotList.items)
        }
    )
}