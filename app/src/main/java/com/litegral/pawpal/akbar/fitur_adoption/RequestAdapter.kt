package com.litegral.pawpal.akbar.fitur_adoption

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.litegral.pawpal.R
import com.litegral.pawpal.akbar.model.AdoptionRequest

class RequestAdapter(
    private val context: Context,
    private var requests: List<AdoptionRequest>,
    private val onStatusChangedClicked: (AdoptionRequest) -> Unit,
    private val onViewPhotoClicked: (String) -> Unit
) : RecyclerView.Adapter<RequestAdapter.RequestViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RequestViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_adoption_request, parent, false)
        return RequestViewHolder(view)
    }

    override fun onBindViewHolder(holder: RequestViewHolder, position: Int) {
        holder.bind(requests[position])
    }

    override fun getItemCount(): Int = requests.size

    fun updateData(newRequests: List<AdoptionRequest>) {
        requests = newRequests
        notifyDataSetChanged()
    }

    inner class RequestViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val adopterNameTextView: TextView = itemView.findViewById(R.id.adopterNameTextView_request)
        private val messageTextView: TextView = itemView.findViewById(R.id.messageTextView_request)
        private val homePhotoIcon: ImageView = itemView.findViewById(R.id.homePhoto_preview_icon)
        private val statusTextView: TextView = itemView.findViewById(R.id.statusTextView_request)
        private val changeStatusButton: Button = itemView.findViewById(R.id.button_change_status)

        fun bind(request: AdoptionRequest) {
            adopterNameTextView.text = "From: ${request.adopterName}"
            messageTextView.text = request.message
            statusTextView.text = "Status: ${request.status}"

            homePhotoIcon.isVisible = !request.homePhotoUrl.isNullOrEmpty()
            homePhotoIcon.setOnClickListener {
                request.homePhotoUrl?.let { url -> onViewPhotoClicked(url) }
            }

            changeStatusButton.setOnClickListener { onStatusChangedClicked(request) }
        }
    }
}