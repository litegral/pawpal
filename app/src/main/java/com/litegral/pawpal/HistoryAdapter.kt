package com.litegral.pawpal

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class HistoryAdapter(private val context: Context) : RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {

    private var items: List<CardItem> = emptyList()

    fun setItems(newItems: List<CardItem>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_history_pet, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int = items.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.petNameTextView)
        private val descriptionTextView: TextView = itemView.findViewById(R.id.petDescriptionTextView)
        private val petImageView: ImageView = itemView.findViewById(R.id.petImageView)

        fun bind(item: CardItem) {
            nameTextView.text = item.name
            descriptionTextView.text = item.description

            // Load image using Glide
            Glide.with(context)
                .load(item.imageUrl)
                .centerCrop()
                .into(petImageView)
        }
    }
}