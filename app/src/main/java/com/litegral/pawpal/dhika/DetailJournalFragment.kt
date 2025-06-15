package com.litegral.pawpal.dhika

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.litegral.pawpal.R

class DetailJournalFragment : Fragment() {

    private val args: DetailJournalFragmentArgs by navArgs()
    private var journalEntry: JournalEntry? = null

    // Firebase
    private lateinit var db: FirebaseFirestore
    private lateinit var storage: FirebaseStorage

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.activity_detail_journal, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

        val titleText: TextView = view.findViewById(R.id.titleText)
        val dateText: TextView = view.findViewById(R.id.dateText)
        val descriptionText: TextView = view.findViewById(R.id.descriptionText)
        val imageView: ImageView = view.findViewById(R.id.img_journal_detail)
        val updateButton: Button = view.findViewById(R.id.btn_update)
        val deleteButton: Button = view.findViewById(R.id.btn_delete)

        journalEntry = args.journalEntry

        journalEntry?.let { entry ->
            titleText.text = entry.title
            dateText.text = entry.date
            descriptionText.text = entry.description

            Glide.with(this)
                .load(entry.imageUrl)
                .placeholder(R.drawable.sample_cat)
                .error(R.drawable.ic_cat_walk)
                .into(imageView)
        }

        deleteButton.setOnClickListener {
            showDeleteConfirmationDialog()
        }

        updateButton.setOnClickListener {
            // Mengirim data jurnal ke halaman AddJournalFragment untuk di-edit
            journalEntry?.let { entry ->
                val action = DetailJournalFragmentDirections.actionDetailJournalFragmentToAddJournalFragment(entry, true)
                findNavController().navigate(action)
            }
        }
    }

    private fun showDeleteConfirmationDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Hapus Jurnal")
            .setMessage("Apakah Anda yakin ingin menghapus jurnal ini secara permanen?")
            .setIcon(R.drawable.cat_dislike) // Pastikan drawable ini ada
            .setPositiveButton("Ya, Hapus") { _, _ ->
                deleteJournal()
            }
            .setNegativeButton("Tidak", null)
            .show()
    }

    private fun deleteJournal() {
        val entryToDelete = journalEntry ?: return
        if (entryToDelete.id.isBlank()) {
            Toast.makeText(context, "Error: ID Jurnal tidak ditemukan.", Toast.LENGTH_SHORT).show()
            return
        }

        // Hapus gambar dari Firebase Storage
        val imageRef = storage.getReferenceFromUrl(entryToDelete.imageUrl)
        imageRef.delete().addOnSuccessListener {
            Log.d("DetailJournalFragment", "Gambar berhasil dihapus dari Storage.")
            // Setelah gambar terhapus, hapus dokumen dari Firestore
            deleteFirestoreDocument(entryToDelete.id)
        }.addOnFailureListener { exception ->
            Log.e("DetailJournalFragment", "Gagal menghapus gambar.", exception)
            // Tetap coba hapus dokumen Firestore meskipun gambar gagal dihapus
            deleteFirestoreDocument(entryToDelete.id)
        }
    }

    private fun deleteFirestoreDocument(documentId: String) {
        db.collection("journals").document(documentId).delete()
            .addOnSuccessListener {
                Toast.makeText(context, "Jurnal berhasil dihapus.", Toast.LENGTH_SHORT).show()
                findNavController().navigateUp() // Kembali ke daftar jurnal
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Gagal menghapus jurnal: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}