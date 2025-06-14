package com.litegral.pawpal.akbar.fitur_ListOpenAdoptOfUser.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.litegral.pawpal.R
import com.litegral.pawpal.akbar.model.CatModel

class PostingOpenAdoptHistoryAdapter(
    private val postList: List<CatModel>,
    private val onItemClicked: (CatModel) -> Unit
) : RecyclerView.Adapter<PostingOpenAdoptHistoryAdapter.HistoryViewHolder>() {

    class HistoryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val petName: TextView = view.findViewById(R.id.textView_history_pet_name)
        private val petSubtitle: TextView = view.findViewById(R.id.textView_history_pet_subtitle)

        fun bind(cat: CatModel, onItemClicked: (CatModel) -> Unit) {
            petName.text = cat.name
            petSubtitle.text = itemView.context.getString(R.string.click_to_edit)
            itemView.setOnClickListener { onItemClicked(cat) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_posting_open_adopt_history, parent, false)
        return HistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        holder.bind(postList[position], onItemClicked)
    }

    override fun getItemCount(): Int = postList.size
}