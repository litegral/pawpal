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
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.litegral.pawpal.R
import java.text.SimpleDateFormat
import java.util.*

class AddJournalFragment : Fragment() {

    // Firebase instances
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

    private var imageUri: Uri? = null

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

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

        // Initialize Views
        setupViews(view)
        setupClickListeners()
    }

    private fun setupViews(view: View) {
        imagePreview = view.findViewById(R.id.image_preview)
        btnSelectImage = view.findViewById(R.id.btnSelectImage)
        uploadButton = view.findViewById(R.id.uploadButton)
        titleEditText = view.findViewById(R.id.titleEditText)
        dateEditText = view.findViewById(R.id.dateEditText)
        descriptionEditText = view.findViewById(R.id.descriptionEditText)
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
            Toast.makeText(requireContext(), "Harap isi semua kolom", Toast.LENGTH_SHORT).show()
            return
        }

        if (imageUri == null) {
            Toast.makeText(requireContext(), "Harap pilih sebuah gambar", Toast.LENGTH_SHORT).show()
            return
        }

        uploadImageAndSaveJournal(date, title, desc)
    }

    private fun uploadImageAndSaveJournal(date: String, title: String, desc: String) {
        setLoading(true)
        val fileName = "journal_${auth.currentUser?.uid}_${System.currentTimeMillis()}.jpg"
        val storageRef = storage.reference.child("images/journal/$fileName")

        storageRef.putFile(imageUri!!)
            .addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { uri ->
                    saveJournalToFirestore(date, title, desc, uri.toString())
                }.addOnFailureListener {
                    handleFailure("Gagal mendapatkan URL unduhan.")
                }
            }
            .addOnFailureListener { e ->
                handleFailure("Gagal mengunggah gambar: ${e.message}")
            }
    }

    private fun saveJournalToFirestore(date: String, title: String, desc: String, imageUrl: String) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            handleFailure("Pengguna tidak login.")
            return
        }

        // Buat objek JournalEntry dengan userId yang sudah diisi
        val journalEntry = JournalEntry(
            userId = userId, // <-- Mengisi ID pengguna saat ini
            date = date,
            title = title,
            description = desc,
            imageUrl = imageUrl
        )

        // Simpan ke koleksi 'journals' di level atas
        db.collection("journals")
            .add(journalEntry)
            .addOnSuccessListener {
                setLoading(false)
                Toast.makeText(requireContext(), "Jurnal berhasil disimpan!", Toast.LENGTH_SHORT).show()
                findNavController().navigateUp()
            }
            .addOnFailureListener { e ->
                handleFailure("Gagal menyimpan jurnal: ${e.message}")
            }
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