package com.litegral.pawpal.akbar.fitur_adoption

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.litegral.pawpal.R

class OwnerPetListingAdapter(
    private var items: List<OwnerPetWithRequests>,
    private val onItemClicked: (OwnerPetWithRequests) -> Unit
) : RecyclerView.Adapter<OwnerPetListingAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_owner_pet_listing, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position], onItemClicked)
    }

    override fun getItemCount(): Int = items.size

    fun updateData(newItems: List<OwnerPetWithRequests>) {
        items = newItems
        notifyDataSetChanged()
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val petImageView: ImageView = view.findViewById(R.id.petImageView_owner)
        private val petNameTextView: TextView = view.findViewById(R.id.petNameTextView_owner)
        private val petBreedTextView: TextView = view.findViewById(R.id.petBreedTextView_owner)
        private val requestCountTextView: TextView = view.findViewById(R.id.requestCountTextView_owner)
        private val pendingIndicatorTextView: TextView = view.findViewById(R.id.pendingIndicatorTextView_owner)
        private val clickableArea: View = view.findViewById(R.id.clickable_area)

        fun bind(item: OwnerPetWithRequests, onItemClicked: (OwnerPetWithRequests) -> Unit) {
            val pet = item.pet
            val requests = item.requests

            petNameTextView.text = pet.name
            petBreedTextView.text = pet.breed

            if (pet.imageUrls.isNotEmpty()) {
                Glide.with(itemView.context).load(pet.imageUrls[0]).into(petImageView)
            } else {
                petImageView.setImageResource(R.drawable.ic_profile_placeholder)
            }

            requestCountTextView.text = "${requests.size} Request(s)"

            val pendingCount = requests.count { it.status == "Pending" }
            if (pendingCount > 0) {
                pendingIndicatorTextView.text = "$pendingCount Pending !!!"
                pendingIndicatorTextView.isVisible = true
            } else {
                pendingIndicatorTextView.isVisible = false
            }

            clickableArea.setOnClickListener { onItemClicked(item) }
        }
    }
}