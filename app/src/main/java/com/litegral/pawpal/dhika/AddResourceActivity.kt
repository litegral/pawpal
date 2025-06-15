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
import com.google.firebase.storage.FirebaseStorage
import com.litegral.pawpal.R
import java.text.SimpleDateFormat
import java.util.*

// Nama kelas sudah benar (AddResourceFragment)
class AddResourceFragment : Fragment() {

    // --- Deklarasi View ---
    private lateinit var imagePreview: ImageView
    private lateinit var btnSelectImage: Button
    private lateinit var uploadButton: Button
    private lateinit var dateEditText: TextView // DIUBAH: dari EditText ke TextView
    private lateinit var titleEditText: EditText
    private lateinit var descriptionEditText: EditText
    private lateinit var tagSpinner: Spinner
    private lateinit var headerTextView: TextView

    // --- Variabel untuk data gambar ---
    private var imageUri: Uri? = null
    private var existingImageUrl: String? = null

    // --- Launcher untuk memilih gambar dari galeri ---
    private val selectImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            imageUri = it
            Glide.with(this)
                .load(it)
                .into(imagePreview)
        }
    }

    companion object {
        const val REQUEST_KEY = "add_resource_request"
        const val RESULT_KEY = "resource_result"
        const val EXTRA_IS_UPDATE = "extra_is_update_resource"
        const val EXTRA_RESOURCE_DATA = "extra_resource_data"
        const val EXTRA_POSITION = "extra_position_resource"

        fun newInstance(isUpdate: Boolean = false, resourceEntry: ResourceEntry? = null, position: Int = -1): AddResourceFragment {
            val fragment = AddResourceFragment()
            val args = bundleOf(
                EXTRA_IS_UPDATE to isUpdate,
                EXTRA_POSITION to position
            )
            resourceEntry?.let { args.putParcelable(EXTRA_RESOURCE_DATA, it) }
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.activity_add_resource, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // --- Inisialisasi View dari layout ---
        val backButton = view.findViewById<ImageView>(R.id.backButton)
        headerTextView = view.findViewById(R.id.headerTextView)
        dateEditText = view.findViewById(R.id.dateEditText)
        titleEditText = view.findViewById(R.id.titleEditText)
        descriptionEditText = view.findViewById(R.id.descriptionEditText)
        uploadButton = view.findViewById(R.id.uploadButton)
        tagSpinner = view.findViewById(R.id.tagSpinner)

        // Gunakan ID dari layout activity_add_resource.xml
        imagePreview = view.findViewById(R.id.imageView)
        btnSelectImage = view.findViewById(R.id.uploadPhotoButton)
        btnSelectImage.text = "Select Image" // Ubah teks tombol agar lebih jelas

        // --- Setup Spinner ---
        val hashtags = listOf("#Nutrition", "#Grooming", "#Health")
        val spinnerAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, hashtags).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        tagSpinner.adapter = spinnerAdapter

        // --- Logika untuk mode Update atau Add New ---
        val isUpdate = arguments?.getBoolean(EXTRA_IS_UPDATE, false) ?: false
        val position = arguments?.getInt(EXTRA_POSITION, -1) ?: -1

        if (isUpdate) {
            headerTextView.text = "UPDATE RESOURCE ENTRY"
            uploadButton.text = "Update"
            val entry = arguments?.getParcelable<ResourceEntry>(EXTRA_RESOURCE_DATA)
            entry?.let {
                val tagPosition = hashtags.indexOf(it.tag)
                if (tagPosition >= 0) {
                    tagSpinner.setSelection(tagPosition)
                }
                dateEditText.text = it.date // DIUBAH: dari setText ke text
                titleEditText.setText(it.title)
                descriptionEditText.setText(it.description)
                existingImageUrl = it.imageUrl // Simpan URL lama
                // Tampilkan gambar yang sudah ada
                Glide.with(this)
                    .load(it.imageUrl)
                    .placeholder(R.drawable.ic_cat_journey)
                    .into(imagePreview)
            }
        }

        // --- Event Listeners ---
        backButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        btnSelectImage.setOnClickListener {
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
            val tag = tagSpinner.selectedItem.toString()

            if (date.isBlank() || title.isBlank() || desc.isBlank() || tag.isBlank()) {
                Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Jika ada gambar baru, unggah. Jika tidak (mode update), gunakan URL lama.
            if (imageUri != null) {
                uploadImageToFirebase(tag, date, title, desc, isUpdate, position)
            } else if (isUpdate && existingImageUrl != null) {
                createResourceEntry(tag, date, title, desc, existingImageUrl!!, isUpdate, position)
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

    private fun uploadImageToFirebase(tag: String, date: String, title: String, desc: String, isUpdate: Boolean, position: Int) {
        setLoading(true)
        val fileName = "resource_${System.currentTimeMillis()}.jpg"
        val storageRef = FirebaseStorage.getInstance().getReference("/images/resource/$fileName")

        storageRef.putFile(imageUri!!)
            .addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { uri ->
                    val imageUrl = uri.toString()
                    createResourceEntry(tag, date, title, desc, imageUrl, isUpdate, position)
                    setLoading(false)
                }
            }
            .addOnFailureListener { e ->
                setLoading(false)
                Toast.makeText(requireContext(), "Upload failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun createResourceEntry(tag: String, date: String, title: String, desc: String, imageUrl: String, isUpdate: Boolean, position: Int) {
        val resultEntry = ResourceEntry(tag, title, date, desc, imageUrl)

        val resultBundle = bundleOf(
            RESULT_KEY to resultEntry,
            EXTRA_IS_UPDATE to isUpdate,
            EXTRA_POSITION to position
        )
        setFragmentResult(REQUEST_KEY, resultBundle)
        parentFragmentManager.popBackStack()
    }

    private fun setLoading(isLoading: Boolean) {
        uploadButton.isEnabled = !isLoading
        btnSelectImage.isEnabled = !isLoading
        // Anda bisa menambahkan ProgressBar jika diinginkan
    }
}