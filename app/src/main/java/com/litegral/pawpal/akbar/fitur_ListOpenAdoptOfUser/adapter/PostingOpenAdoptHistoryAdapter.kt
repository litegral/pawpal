package com.litegral.pawpal.akbar.fitur_ListOpenAdoptOfUser.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.litegral.pawpal.R
import com.litegral.pawpal.akbar.model.CatModel

class PostingOpenAdoptHistoryAdapter(
    private val postList: List<CatModel>,
    private val onEditClicked: (CatModel) -> Unit,
    private val onViewRequestsClicked: (CatModel) -> Unit
) : RecyclerView.Adapter<PostingOpenAdoptHistoryAdapter.HistoryViewHolder>() {

    // FUNCTION UNTUK UPDATE VIEWHOLDER DENGAN DATA BARU
    class HistoryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val petName: TextView = view.findViewById(R.id.textView_history_pet_name)
        private val editClickArea: View = view.findViewById(R.id.edit_click_area)
        private val viewRequestsButton: MaterialButton = view.findViewById(R.id.button_view_requests)

        fun bind(
            cat: CatModel,
            onEditClicked: (CatModel) -> Unit,
            onViewRequestsClicked: (CatModel) -> Unit
        ) {
            petName.text = cat.name

            editClickArea.setOnClickListener { onEditClicked(cat) }

            viewRequestsButton.setOnClickListener { onViewRequestsClicked(cat) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_posting_open_adopt_history, parent, false)
        return HistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        holder.bind(postList[position], onEditClicked, onViewRequestsClicked)
    }

    override fun getItemCount(): Int = postList.size
}