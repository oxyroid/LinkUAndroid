package com.linku.im.ui.components

import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.LayoutManager

@Composable
fun <T : Any> NativeLazyList(
    list: List<T> = emptyList(),
    item: @Composable (T) -> Unit,
    layoutManager: LayoutManager = LinearLayoutManager(
        LocalContext.current,
        LinearLayoutManager.VERTICAL,
        false
    ),
    isItemsTheSame: (T, T) -> Boolean = { old, new -> old == new },
    isContentsTheSame: (T, T) -> Boolean = { old, new -> old == new },
    modifier: Modifier = Modifier
) {
    val callback = remember(isItemsTheSame, isContentsTheSame) {
        object : DiffUtil.ItemCallback<T>() {
            override fun areItemsTheSame(oldItem: T, newItem: T): Boolean {
                return isItemsTheSame(oldItem, newItem)
            }

            override fun areContentsTheSame(oldItem: T, newItem: T): Boolean {
                return isContentsTheSame(oldItem, newItem)
            }
        }
    }
    val adapter = remember {
        NativeLazyListAdapter(callback, item)
    }
    AndroidView(
        factory = { context ->
            RecyclerView(context).apply {
                this.layoutManager = layoutManager
                this.adapter = adapter
            }
        },
        modifier = modifier
    )
    adapter.submitList(list)
}

internal class NativeLazyListAdapter<T>(
    callback: DiffUtil.ItemCallback<T>,
    private val item: @Composable (T) -> Unit
) : ListAdapter<T, NativeLazyListViewHolder>(callback) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NativeLazyListViewHolder {
        return ComposeView(parent.context)
            .let(::NativeLazyListViewHolder)
    }

    override fun onBindViewHolder(holder: NativeLazyListViewHolder, position: Int) {
        val composeView = holder.itemView as ComposeView
        composeView.setContent {
            item(getItem(position))
        }
    }
}

internal class NativeLazyListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

@Composable
@Preview
internal fun NativeLazyListTest() {
    NativeLazyList(
        list = listOf("1", "2", "3"),
        item = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(text = it)
            }
        }
    )
}