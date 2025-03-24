package com.example.pawpal

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip

class CatAdapter(private val cats: List<Cat>) : RecyclerView.Adapter<CatAdapter.CatViewHolder>() {

    class CatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val catImage: ImageView = itemView.findViewById(R.id.cat_image)
        val catName: TextView = itemView.findViewById(R.id.cat_name)
        val catAge: TextView = itemView.findViewById(R.id.cat_age)
        val catGender: TextView = itemView.findViewById(R.id.cat_gender)
        val catLocation: TextView = itemView.findViewById(R.id.cat_location)
        val catStatusChip: Chip = itemView.findViewById(R.id.cat_status_chip)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CatViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_cat, parent, false)
        return CatViewHolder(view)
    }

    override fun onBindViewHolder(holder: CatViewHolder, position: Int) {
        val cat = cats[position]

        // Set cat image (you'll need to use a library like Glide/Picasso for real images)
        holder.catImage.setImageResource(cat.imageResId)

        // Set text fields
        holder.catName.text = cat.name
        holder.catAge.text = "Umur: ${cat.age}"
        holder.catGender.text = "Jenis Kelamin: ${cat.gender}"
        holder.catLocation.text = "Lokasi: ${cat.location}"

        // Set status chip
        holder.catStatusChip.text = cat.status
    }

    override fun getItemCount() = cats.size
}
