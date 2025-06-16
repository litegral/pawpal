package com.litegral.pawpal

import android.content.Context
import android.view.View
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class CardStackAdapter(
    private val context: Context,
    private var items: List<CardItem> = emptyList()
) : RecyclerView.Adapter<CardStackAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return ViewHolder(inflater.inflate(R.layout.item_card, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.name.text = item.name
        holder.age.text = item.age
        holder.breed.text = item.breed
        holder.gender.text = item.gender
        holder.description.text = item.description

        // Load image using Glide
        Glide.with(context)
            .load(item.imageUrl)
            .into(holder.image)
    }

    override fun getItemCount(): Int = items.size

    fun setItems(items: List<CardItem>) {
        this.items = items
        notifyDataSetChanged()
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val image: ImageView = view.findViewById(R.id.itemImage)
        val name: TextView = view.findViewById(R.id.itemName)
        val age: TextView = view.findViewById(R.id.itemAge)
        val breed: TextView = view.findViewById(R.id.itemBreed)
        val gender: TextView = view.findViewById(R.id.itemGender)
        val description: TextView = view.findViewById(R.id.itemDescription)
    }
}
