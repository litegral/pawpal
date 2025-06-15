package com.litegral.pawpal.dhika

import android.app.DatePickerDialog
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import com.bumptech.glide.Glide
import com.litegral.pawpal.R
import com.google.firebase.storage.FirebaseStorage
import java.text.SimpleDateFormat
import java.util.*

// Ganti nama file dan kelas dari AddJournalActivity menjadi AddJournalFragment
class AddJournalFragment : Fragment() {

    private lateinit var imagePreview: ImageView
    private lateinit var btnSelectImage: Button
    private lateinit var uploadButton: Button
    private lateinit var titleEditText: EditText
    private lateinit var dateEditText: TextView // DIUBAH: dari EditText ke TextView
    private lateinit var descriptionEditText: EditText
    private lateinit var headerTextView: TextView

    private var imageUri: Uri? = null
    private var existingImageUrl: String? = null

    // ActivityResultLauncher untuk memilih gambar dari galeri
    private val selectImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            imageUri = it
            Glide.with(this)
                .load(it)
                .into(imagePreview)
        }
    }

    companion object {
        const val REQUEST_KEY = "add_journal_request"
        const val RESULT_KEY = "journal_result"
        const val EXTRA_IS_UPDATE = "extra_is_update"
        const val EXTRA_JOURNAL_DATA = "extra_journal_data"
        const val EXTRA_POSITION = "extra_position"

        fun newInstance(isUpdate: Boolean = false, journalEntry: JournalEntry? = null, position: Int = -1): AddJournalFragment {
            val fragment = AddJournalFragment()
            val args = bundleOf(
                EXTRA_IS_UPDATE to isUpdate,
                EXTRA_POSITION to position
            )
            // Hanya tambahkan data jika tidak null
            journalEntry?.let { args.putParcelable(EXTRA_JOURNAL_DATA, it) }
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Ganti R.layout.activity_add_journal jika nama file berbeda
        return inflater.inflate(R.layout.activity_add_journal, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inisialisasi Views
        val backButton = view.findViewById<ImageView>(R.id.backButton)
        headerTextView = view.findViewById(R.id.headerTextView)
        dateEditText = view.findViewById(R.id.dateEditText)
        titleEditText = view.findViewById(R.id.titleEditText)
        descriptionEditText = view.findViewById(R.id.descriptionEditText)
        uploadButton = view.findViewById(R.id.uploadButton)
        btnSelectImage = view.findViewById(R.id.btnSelectImage) // Ganti ID jika berbeda
        imagePreview = view.findViewById(R.id.image_preview)   // Ganti ID jika berbeda
        // Tambahkan ProgressBar ke layout Anda, misalnya di tengah layar
        // progressBar = view.findViewById(R.id.progressBar)

        val isUpdate = arguments?.getBoolean(EXTRA_IS_UPDATE, false) ?: false
        val position = arguments?.getInt(EXTRA_POSITION, -1) ?: -1

        if (isUpdate) {
            headerTextView.text = "UPDATE JOURNAL ENTRY"
            uploadButton.text = "Update"
            val entry = arguments?.getParcelable<JournalEntry>(EXTRA_JOURNAL_DATA)
            entry?.let {
                dateEditText.text = it.date // DIUBAH: dari setText ke text
                titleEditText.setText(it.title)
                descriptionEditText.setText(it.description)
                existingImageUrl = it.imageUrl
                Glide.with(this)
                    .load(it.imageUrl)
                    .placeholder(R.drawable.ic_cat_journey)
                    .into(imagePreview)
            }
        }

        backButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        btnSelectImage.setOnClickListener {
            // Buka galeri untuk memilih gambar
            selectImageLauncher.launch("image/*")
        }

        // DITAMBAHKAN: OnClickListener untuk menampilkan DatePickerDialog
        dateEditText.setOnClickListener {
            showDatePickerDialog()
        }

        uploadButton.setOnClickListener {
            val date = dateEditText.text.toString().trim()
            val title = titleEditText.text.toString().trim()
            val desc = descriptionEditText.text.toString().trim()

            if (title.isBlank() || date.isBlank() || desc.isBlank()) {
                Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Jika ada gambar baru yang dipilih, unggah. Jika tidak, gunakan URL lama (untuk mode update)
            if (imageUri != null) {
                uploadImageToFirebase(date, title, desc, isUpdate, position)
            } else if (isUpdate && existingImageUrl != null) {
                // Tidak ada gambar baru yang dipilih, langsung simpan dengan URL lama
                createJournalEntry(date, title, desc, existingImageUrl!!, isUpdate, position)
            } else {
                Toast.makeText(requireContext(), "Please select an image", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // DITAMBAHKAN: Fungsi untuk menampilkan kalender
    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, selectedDay ->
                val selectedDate = Calendar.getInstance()
                selectedDate.set(selectedYear, selectedMonth, selectedDay)
                val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID"))
                dateEditText.text = dateFormat.format(selectedDate.time)
            },
            year,
            month,
            day
        )
        datePickerDialog.show()
    }

    private fun uploadImageToFirebase(date: String, title: String, desc: String, isUpdate: Boolean, position: Int) {
        setLoading(true)
        val fileName = "journal_${System.currentTimeMillis()}.jpg"
        val storageRef = FirebaseStorage.getInstance().getReference("/images/journal/$fileName")

        storageRef.putFile(imageUri!!)
            .addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { uri ->
                    val imageUrl = uri.toString()
                    createJournalEntry(date, title, desc, imageUrl, isUpdate, position)
                    setLoading(false)
                }
            }
            .addOnFailureListener { e ->
                setLoading(false)
                Toast.makeText(requireContext(), "Upload failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun createJournalEntry(date: String, title: String, desc: String, imageUrl: String, isUpdate: Boolean, position: Int) {
        val resultEntry = JournalEntry(date, title, desc, imageUrl)

        val resultBundle = bundleOf(
            RESULT_KEY to resultEntry,
            EXTRA_IS_UPDATE to isUpdate,
            EXTRA_POSITION to position
        )
        setFragmentResult(REQUEST_KEY, resultBundle)
        parentFragmentManager.popBackStack()
    }

    private fun setLoading(isLoading: Boolean) {
        // Tampilkan/sembunyikan ProgressBar
        // progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        uploadButton.isEnabled = !isLoading
        btnSelectImage.isEnabled = !isLoading
    }
}