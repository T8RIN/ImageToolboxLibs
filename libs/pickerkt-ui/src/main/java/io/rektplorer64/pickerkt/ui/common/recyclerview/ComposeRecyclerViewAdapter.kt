package io.rektplorer64.pickerkt.ui.common.recyclerview

import android.annotation.SuppressLint
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import io.rektplorer64.pickerkt.ui.ui.theme.setThemedContent

abstract class ComposeRecyclerViewAdapter<T, VH : ComposeRecyclerViewAdapter.ComposeViewHolder<T>>(diffCallback: DiffUtil.ItemCallback<T>) :
    ListAdapter<T, VH>(diffCallback) {

    override fun onBindViewHolder(holder: VH, position: Int) {
        (holder.itemView as ComposeView).apply {
            holder.data.value = getItem(position)
            setThemedContent {
                val data by holder.data
                holder.item(data)
            }
        }
    }

    abstract class ComposeViewHolder<T>(composeView: View) : RecyclerView.ViewHolder(composeView) {

        abstract val data: MutableState<T?>

        @Composable
        @SuppressLint("ComposableNaming")
        abstract fun item(data: T?)
    }
}