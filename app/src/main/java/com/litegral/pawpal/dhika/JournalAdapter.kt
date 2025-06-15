package com.litegral.pawpal.dhika

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.litegral.pawpal.R

class JournalAdapter(
    private val journalEntries: MutableList<JournalEntry>,
    private val onItemClicked: (JournalEntry, Int) -> Unit
) : RecyclerView.Adapter<JournalAdapter.JournalViewHolder>() {

    class JournalViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val journalImage: ImageView = itemView.findViewById(R.id.journal_image)
        val journalDate: TextView = itemView.findViewById(R.id.journal_date)
        val journalTitle: TextView = itemView.findViewById(R.id.journal_title)
        val journalDescription: TextView = itemView.findViewById(R.id.journal_description)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JournalViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_journal_entry, parent, false)
        return JournalViewHolder(view)
    }

    override fun onBindViewHolder(holder: JournalViewHolder, position: Int) {
        val entry = journalEntries[position]
        holder.journalDate.text = entry.date
        holder.journalTitle.text = entry.title
        holder.journalDescription.text = entry.description

        // Gunakan Glide untuk memuat gambar dari URL
        Glide.with(holder.itemView.context)
            .load(entry.imageUrl)
            .placeholder(R.drawable.sample_cat) // Gambar sementara saat memuat
            .error(R.drawable.ic_cat_walk) // Gambar jika terjadi error
            .into(holder.journalImage)

        holder.itemView.setOnClickListener {
            onItemClicked(entry, position)
        }
    }
    // ... sisa kode adapter tidak berubah ...
    override fun getItemCount() = journalEntries.size

    fun addEntry(entry: JournalEntry) {
        journalEntries.add(0, entry)
        notifyItemInserted(0)
    }

    fun updateEntry(position: Int, entry: JournalEntry) {
        if (position >= 0 && position < journalEntries.size) {
            journalEntries[position] = entry
            notifyItemChanged(position)
        }
    }

    fun removeEntry(position: Int) {
        if (position >= 0 && position < journalEntries.size) {
            journalEntries.removeAt(position)
            notifyItemRemoved(position)
        }
    }
}