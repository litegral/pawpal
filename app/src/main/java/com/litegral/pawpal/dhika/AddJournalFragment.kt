package com.litegral.pawpal.dhika

import android.app.DatePickerDialog
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.litegral.pawpal.R
import java.text.SimpleDateFormat
import java.util.*

class AddJournalFragment : Fragment() {

    // Safe Args untuk mendapatkan argumen dari navigasi
    private val args: AddJournalFragmentArgs by navArgs()

    // Firebase
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var storage: FirebaseStorage

    // Views
    private lateinit var imagePreview: ImageView
    private lateinit var btnSelectImage: Button
    private lateinit var uploadButton: Button
    private lateinit var titleEditText: EditText
    private lateinit var dateEditText: TextView
    private lateinit var descriptionEditText: EditText
    private lateinit var headerTextView: TextView

    // Variabel untuk menyimpan state
    private var imageUri: Uri? = null
    private var existingImageUrl: String? = null
    private var isUpdateMode: Boolean = false
    private var journalToUpdate: JournalEntry? = null

    private val selectImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            imageUri = it
            Glide.with(this).load(it).into(imagePreview)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.activity_add_journal, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeFirebase()
        setupViews(view)
        checkModeAndPopulateData()
        setupClickListeners()
    }

    private fun initializeFirebase() {
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()
    }

    private fun setupViews(view: View) {
        headerTextView = view.findViewById(R.id.headerTextView)
        imagePreview = view.findViewById(R.id.image_preview)
        btnSelectImage = view.findViewById(R.id.btnSelectImage)
        uploadButton = view.findViewById(R.id.uploadButton)
        titleEditText = view.findViewById(R.id.titleEditText)
        dateEditText = view.findViewById(R.id.dateEditText)
        descriptionEditText = view.findViewById(R.id.descriptionEditText)
    }

    private fun checkModeAndPopulateData() {
        isUpdateMode = args.isUpdate
        journalToUpdate = args.journalEntry

        if (isUpdateMode && journalToUpdate != null) {
            // Mode Update: isi form dengan data yang ada
            headerTextView.text = "Update Jurnal"
            uploadButton.text = "Update"

            journalToUpdate?.let { entry ->
                titleEditText.setText(entry.title)
                dateEditText.text = entry.date
                descriptionEditText.setText(entry.description)
                existingImageUrl = entry.imageUrl
                Glide.with(this)
                    .load(entry.imageUrl)
                    .placeholder(R.drawable.ic_cat_journey)
                    .into(imagePreview)
            }
        } else {
            // Mode Create: biarkan form kosong
            headerTextView.text = "Jurnal Baru"
            uploadButton.text = "Upload"
        }
    }

    private fun setupClickListeners() {
        view?.findViewById<ImageView>(R.id.backButton)?.setOnClickListener { findNavController().navigateUp() }
        btnSelectImage.setOnClickListener { selectImageLauncher.launch("image/*") }
        dateEditText.setOnClickListener { showDatePickerDialog() }
        uploadButton.setOnClickListener { handleUploadButtonClick() }
    }

    private fun handleUploadButtonClick() {
        val date = dateEditText.text.toString().trim()
        val title = titleEditText.text.toString().trim()
        val desc = descriptionEditText.text.toString().trim()

        if (title.isBlank() || date.isBlank() || desc.isBlank()) {
            Toast.makeText(requireContext(), "Harap isi semua kolom.", Toast.LENGTH_SHORT).show()
            return
        }

        if (!isUpdateMode && imageUri == null) {
            Toast.makeText(requireContext(), "Harap pilih sebuah gambar.", Toast.LENGTH_SHORT).show()
            return
        }

        setLoading(true)

        if (isUpdateMode) {
            if (imageUri != null) {
                // Jika gambar diubah: upload gambar baru, lalu update dokumen
                uploadImageAndUpdateJournal(date, title, desc)
            } else {
                // Jika gambar tidak diubah: langsung update dokumen
                updateJournalDocument(date, title, desc, existingImageUrl ?: "")
            }
        } else {
            // Membuat jurnal baru
            uploadImageAndCreateJournal(date, title, desc)
        }
    }

    private fun uploadImageAndCreateJournal(date: String, title: String, desc: String) {
        val fileName = "journal_${auth.currentUser?.uid}_${System.currentTimeMillis()}.jpg"
        val storageRef = storage.reference.child("images/journal/$fileName")

        storageRef.putFile(imageUri!!)
            .addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { uri ->
                    createJournalDocument(date, title, desc, uri.toString())
                }
            }
            .addOnFailureListener { e -> handleFailure("Gagal mengunggah gambar: ${e.message}") }
    }

    private fun createJournalDocument(date: String, title: String, desc: String, imageUrl: String) {
        val userId = auth.currentUser?.uid ?: ""
        val journalEntry = JournalEntry(userId = userId, date = date, title = title, description = desc, imageUrl = imageUrl)
        db.collection("journals").add(journalEntry)
            .addOnSuccessListener {
                setLoading(false)
                Toast.makeText(requireContext(), "Jurnal berhasil disimpan!", Toast.LENGTH_SHORT).show()
                findNavController().navigateUp()
            }
            .addOnFailureListener { e -> handleFailure("Gagal menyimpan jurnal: ${e.message}") }
    }

    private fun uploadImageAndUpdateJournal(date: String, title: String, desc: String) {
        // Hapus gambar lama jika ada
        existingImageUrl?.takeIf { it.isNotEmpty() }?.let { url ->
            storage.getReferenceFromUrl(url).delete()
                .addOnSuccessListener { Log.d("AddJournalFragment", "Gambar lama dihapus.") }
                .addOnFailureListener { e -> Log.e("AddJournalFragment", "Gagal hapus gambar lama.", e) }
        }

        // Upload gambar baru
        val fileName = "journal_${auth.currentUser?.uid}_${System.currentTimeMillis()}.jpg"
        val newStorageRef = storage.reference.child("images/journal/$fileName")
        newStorageRef.putFile(imageUri!!)
            .addOnSuccessListener {
                newStorageRef.downloadUrl.addOnSuccessListener { newUri ->
                    updateJournalDocument(date, title, desc, newUri.toString())
                }
            }
            .addOnFailureListener { e -> handleFailure("Gagal unggah gambar baru: ${e.message}") }
    }

    private fun updateJournalDocument(date: String, title: String, desc: String, imageUrl: String) {
        val documentId = journalToUpdate?.id
        if (documentId.isNullOrBlank()) {
            handleFailure("Error: ID Jurnal tidak ditemukan untuk update.")
            return
        }

        val updatedData = mapOf("date" to date, "title" to title, "description" to desc, "imageUrl" to imageUrl)
        db.collection("journals").document(documentId).update(updatedData)
            .addOnSuccessListener {
                setLoading(false)
                Toast.makeText(requireContext(), "Jurnal berhasil diperbarui!", Toast.LENGTH_SHORT).show()
                // Kembali ke halaman daftar jurnal (melewati halaman detail)
                findNavController().popBackStack(R.id.journalFragment, false)
            }
            .addOnFailureListener { e -> handleFailure("Gagal memperbarui jurnal: ${e.message}") }
    }

    private fun handleFailure(message: String) {
        setLoading(false)
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
        Log.e("AddJournalFragment", message)
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                val selectedDate = Calendar.getInstance().apply { set(year, month, dayOfMonth) }
                val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID"))
                dateEditText.text = dateFormat.format(selectedDate.time)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun setLoading(isLoading: Boolean) {
        uploadButton.isEnabled = !isLoading
        btnSelectImage.isEnabled = !isLoading
    }
}