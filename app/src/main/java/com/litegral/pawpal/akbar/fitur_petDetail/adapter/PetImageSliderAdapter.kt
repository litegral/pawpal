// Pastikan file ini ada di package: com.litegral.pawpal.akbar
package com.litegral.pawpal.akbar.fitur_petDetail.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.litegral.pawpal.R

class PetImageSliderAdapter(private val imageList: List<Any>) :
    RecyclerView.Adapter<PetImageSliderAdapter.ImageViewHolder>() {

    inner class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imageView_slider_item)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_pet_image_slider, parent, false)
        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        when (val imageSource = imageList[position]) {
            is Int -> holder.imageView.setImageResource(imageSource)
            is String -> Glide.with(holder.itemView.context).load(imageSource).into(holder.imageView)
        }
    }

    override fun getItemCount(): Int = imageList.size
}