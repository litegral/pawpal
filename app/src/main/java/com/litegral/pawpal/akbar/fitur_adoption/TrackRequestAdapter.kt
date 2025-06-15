// app/src/main/java/com/litegral/pawpal/akbar/fitur_adoption/TrackRequestAdapter.kt
package com.litegral.pawpal.akbar.fitur_adoption

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.litegral.pawpal.R
import com.litegral.pawpal.akbar.model.AdoptionRequest

class TrackRequestAdapter(
    private val context: Context,
    private var requests: List<AdoptionRequest>,
    private val onEdit: (AdoptionRequest) -> Unit,
    private val onDelete: (AdoptionRequest) -> Unit
) : RecyclerView.Adapter<TrackRequestAdapter.TrackViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrackViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_track_request, parent, false)
        return TrackViewHolder(view)
    }

    override fun onBindViewHolder(holder: TrackViewHolder, position: Int) {
        holder.bind(requests[position])
    }

    override fun getItemCount(): Int = requests.size

    fun updateData(newRequests: List<AdoptionRequest>) {
        requests = newRequests
        notifyDataSetChanged()
    }

    inner class TrackViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val petImageView: ImageView = itemView.findViewById(R.id.petImageView_track)
        private val petNameTextView: TextView = itemView.findViewById(R.id.petNameTextView_track)
        private val statusTextView: TextView = itemView.findViewById(R.id.statusTextView_track)
        private val editButton: ImageButton = itemView.findViewById(R.id.editButton_track)
        private val deleteButton: ImageButton = itemView.findViewById(R.id.deleteButton_track)

        fun bind(request: AdoptionRequest) {
            petNameTextView.text = request.petName
            statusTextView.text = request.status

            petImageView.contentDescription = "Request for pet: ${request.petName}"
            editButton.contentDescription = "Edit your adoption request for ${request.petName}"
            deleteButton.contentDescription = "Delete your adoption request for ${request.petName}"

            when (request.status) {
                "Accepted" -> statusTextView.setTextColor(Color.GREEN)
                "Declined" -> statusTextView.setTextColor(Color.RED)
                else -> statusTextView.setTextColor(Color.GRAY)
            }

            Glide.with(context).load(request.petImageUrl).into(petImageView)

            editButton.setOnClickListener { onEdit(request) }
            deleteButton.setOnClickListener { onDelete(request) }
        }
    }
}