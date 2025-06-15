package com.litegral.pawpal.dhika

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs // Import navArgs
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth // Import FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore // Import FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage // Import FirebaseStorage
import com.litegral.pawpal.JournalFragmentDirections
import com.litegral.pawpal.R

// Nama kelas sudah benar (DetailResourceFragment)
class DetailResourceFragment : Fragment() {

    // Menggunakan Safe Args untuk mendapatkan argumen
    private val args: DetailResourceFragmentArgs by navArgs()

    // Firebase instances
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var storage: FirebaseStorage

    // Views
    private lateinit var tagText: TextView
    private lateinit var titleText: TextView
    private lateinit var dateText: TextView
    private lateinit var descriptionText: TextView
    private lateinit var imageView: ImageView
    private lateinit var updateButton: Button
    private lateinit var deleteButton: Button
    // Tambahkan ProgressBar jika ada di layout activity_detail_resource.xml
    // private lateinit var progressBar: ProgressBar

    // Objek resource yang sedang ditampilkan
    private lateinit var currentResourceEntry: ResourceEntry

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.activity_detail_resource, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inisialisasi Firebase
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

        // Inisialisasi Views
        tagText = view.findViewById(R.id.tagText)
        titleText = view.findViewById(R.id.titleText)
        dateText = view.findViewById(R.id.dateText)
        descriptionText = view.findViewById(R.id.descriptionText)
        imageView = view.findViewById(R.id.img_resource_detail)
        updateButton = view.findViewById(R.id.btn_update)
        deleteButton = view.findViewById(R.id.btn_delete)
        // Inisialisasi ProgressBar jika ada
        // progressBar = view.findViewById(R.id.progressBar_detail) // Anda perlu menambahkan ID ini di XML jika belum ada

        // Dapatkan data resource dari Safe Args
        currentResourceEntry = args.resourceEntry

        displayResourceDetails()
        setupClickListeners()
        checkPermissionAndSetupButtons()
    }

    private fun displayResourceDetails() {
        tagText.text = currentResourceEntry.tag
        titleText.text = currentResourceEntry.title
        dateText.text = currentResourceEntry.date
        descriptionText.text = currentResourceEntry.description

        Glide.with(this)
            .load(currentResourceEntry.imageUrl)
            .placeholder(R.drawable.sample_cat)
            .error(R.drawable.ic_cat_walk) // Ganti dengan gambar error yang sesuai
            .into(imageView)
    }

    private fun setupClickListeners() {
        deleteButton.setOnClickListener {
            showDeleteConfirmationDialog()
        }

        updateButton.setOnClickListener {
            navigateToUpdateForm()
        }
    }

    private fun checkPermissionAndSetupButtons() {
        val currentUserUid = auth.currentUser?.uid
        val isOwner = currentUserUid == currentResourceEntry.userId

        // Sembunyikan/tampilkan tombol berdasarkan kepemilikan
        updateButton.isVisible = isOwner
        deleteButton.isVisible = isOwner
    }

    private fun showDeleteConfirmationDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Resource")
            .setMessage("Are you sure you want to delete this resource?")
            .setPositiveButton("Delete") { dialog, which ->
                performDelete()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun performDelete() {
        setLoading(true)

        // 1. Hapus gambar dari Firebase Storage
        val imageRef = storage.getReferenceFromUrl(currentResourceEntry.imageUrl)
        imageRef.delete()
            .addOnSuccessListener {
                Log.d("DetailResource", "Image deleted from Storage successfully.")
                // 2. Jika gambar berhasil dihapus, hapus dokumen dari Firestore
                deleteDocumentFromFirestore()
            }
            .addOnFailureListener { e ->
                setLoading(false)
                Toast.makeText(requireContext(), "Failed to delete image: ${e.message}", Toast.LENGTH_LONG).show()
                Log.e("DetailResource", "Error deleting image from Storage", e)
            }
    }

    private fun deleteDocumentFromFirestore() {
        db.collection("resources").document(currentResourceEntry.id)
            .delete()
            .addOnSuccessListener {
                setLoading(false)
                Toast.makeText(requireContext(), "Resource deleted successfully!", Toast.LENGTH_SHORT).show()
                // Kembali ke halaman sebelumnya setelah berhasil dihapus
                findNavController().popBackStack()
            }
            .addOnFailureListener { e ->
                setLoading(false)
                Toast.makeText(requireContext(), "Failed to delete resource: ${e.message}", Toast.LENGTH_LONG).show()
                Log.e("DetailResource", "Error deleting document from Firestore", e)
            }
    }

    private fun navigateToUpdateForm() {
        try {
            // Aksi navigasi ke AddResourceFragment dalam mode update
            val action = JournalFragmentDirections.actionJournalFragmentToAddResourceFragment(
                resourceEntry = currentResourceEntry, // Kirim objek resource yang ada
                isUpdate = true, // Set flag isUpdate ke true
                position = args.position // Kirim posisi (jika diperlukan untuk manajemen UI adapter)
            )
            findNavController().navigate(action)
        } catch (e: Exception) {
            Log.e("DetailResource", "Navigasi ke update form gagal: ${e.message}", e)
            Toast.makeText(requireContext(), "Failed to open update form.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setLoading(isLoading: Boolean) {
        // Tampilkan/sembunyikan ProgressBar jika ada
        // progressBar.isVisible = isLoading
        updateButton.isEnabled = !isLoading
        deleteButton.isEnabled = !isLoading
    }
}