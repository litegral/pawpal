package com.litegral.pawpal

import android.content.Context
import android.view.ContextMenu
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.litegral.pawpal.match.model.Cat

class HistoryAdapter(
    private val context: Context,
    private val onRemoveListener: (HistoryListItem, Int) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var items: List<HistoryListItem> = listOf()
    private var longPressedPosition: Int = -1

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_MATCH = 1
        private const val TYPE_LIKED = 2
    }

    fun setItems(items: List<HistoryListItem>) {
        this.items = items
        notifyDataSetChanged()
    }

    fun getItem(position: Int): HistoryListItem? {
        return if (position in items.indices) items[position] else null
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is HistoryListItem.Header -> TYPE_HEADER
            is HistoryListItem.MatchItem -> TYPE_MATCH
            is HistoryListItem.LikedItem -> TYPE_LIKED
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_HEADER -> {
                val view = inflater.inflate(R.layout.item_history_header, parent, false)
                HeaderViewHolder(view)
            }
            TYPE_MATCH -> {
                val view = inflater.inflate(R.layout.item_history_match, parent, false)
                MatchViewHolder(view)
            }
            TYPE_LIKED -> {
                val view = inflater.inflate(R.layout.item_history_liked, parent, false)
                LikedViewHolder(view)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = items[position]
        when (holder) {
            is HeaderViewHolder -> holder.bind(item as HistoryListItem.Header)
            is MatchViewHolder -> holder.bind(item as HistoryListItem.MatchItem)
            is LikedViewHolder -> holder.bind(item as HistoryListItem.LikedItem)
        }
        holder.itemView.setOnLongClickListener {
            longPressedPosition = holder.adapterPosition
            false
        }
    }

    override fun getItemCount(): Int = items.size

    inner class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val headerTextView: TextView = itemView.findViewById(R.id.headerTextView)
        fun bind(item: HistoryListItem.Header) {
            headerTextView.text = item.title
        }
    }

    inner class MatchViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnCreateContextMenuListener {
        private val nameTextView: TextView = itemView.findViewById(R.id.nameTextView)
        private val descriptionTextView: TextView = itemView.findViewById(R.id.descriptionTextView)
        private val catImageView: ImageView = itemView.findViewById(R.id.catImageView)
        init {
            itemView.setOnCreateContextMenuListener(this)
        }
        fun bind(item: HistoryListItem.MatchItem) {
            val cat = item.cat
            nameTextView.text = cat.name
            descriptionTextView.text = "You matched with ${cat.name}!"
            Glide.with(context).load(cat.imageUrl).into(catImageView)
        }
        override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenu.ContextMenuInfo?) {
            menu.add(Menu.NONE, R.id.action_remove, Menu.NONE, "Remove Match")
                .setOnMenuItemClickListener {
                    if (longPressedPosition != -1) {
                        onRemoveListener(items[longPressedPosition], longPressedPosition)
                    }
                    true
                }
        }
    }

    inner class LikedViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnCreateContextMenuListener {
        private val nameTextView: TextView = itemView.findViewById(R.id.nameTextView)
        private val descriptionTextView: TextView = itemView.findViewById(R.id.descriptionTextView)
        private val catImageView: ImageView = itemView.findViewById(R.id.catImageView)
        init {
            itemView.setOnCreateContextMenuListener(this)
        }
        fun bind(item: HistoryListItem.LikedItem) {
            val cat = item.cat
            nameTextView.text = cat.name
            descriptionTextView.text = "You liked ${cat.name}"
            Glide.with(context).load(cat.imageUrl).into(catImageView)
        }
        override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenu.ContextMenuInfo?) {
            menu.add(Menu.NONE, R.id.action_remove, Menu.NONE, "Cancel Like")
                .setOnMenuItemClickListener {
                     if (longPressedPosition != -1) {
                        onRemoveListener(items[longPressedPosition], longPressedPosition)
                    }
                    true
                }
        }
    }
}