package com.litegral.pawpal.dhika

import android.app.DatePickerDialog
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.litegral.pawpal.R
import java.text.SimpleDateFormat
import java.util.*

class AddResourceFragment : Fragment() {

    private val args: AddResourceFragmentArgs by navArgs()

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var storage: FirebaseStorage

    private lateinit var imagePreview: ImageView
    private lateinit var btnSelectImage: Button
    private lateinit var uploadButton: Button
    private lateinit var dateEditText: TextView
    private lateinit var titleEditText: EditText
    private lateinit var descriptionEditText: EditText
    private lateinit var tagSpinner: Spinner
    private lateinit var headerTextView: TextView

    private var imageUri: Uri? = null
    private var existingImageUrl: String? = null

    private var isUpdateMode: Boolean = false
    private var resourceToUpdate: ResourceEntry? = null

    private val selectImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            imageUri = it
            Glide.with(this)
                .load(it)
                .into(imagePreview)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.activity_add_resource, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

        val backButton = view.findViewById<ImageView>(R.id.backButton)
        headerTextView = view.findViewById(R.id.headerTextView)
        dateEditText = view.findViewById(R.id.dateEditText)
        titleEditText = view.findViewById(R.id.titleEditText)
        descriptionEditText = view.findViewById(R.id.descriptionEditText)
        uploadButton = view.findViewById(R.id.uploadButton)
        tagSpinner = view.findViewById(R.id.tagSpinner)

        imagePreview = view.findViewById(R.id.imageView)
        btnSelectImage = view.findViewById(R.id.uploadPhotoButton)
        btnSelectImage.text = "Select Image"

        val hashtags = listOf("#Nutrition", "#Grooming", "#Health")
        val spinnerAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, hashtags).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        tagSpinner.adapter = spinnerAdapter

        isUpdateMode = args.isUpdate
        resourceToUpdate = args.resourceEntry

        if (isUpdateMode && resourceToUpdate != null) {
            headerTextView.text = "UPDATE RESOURCE ENTRY"
            uploadButton.text = "Update"
            resourceToUpdate?.let { entry ->
                val tagPosition = hashtags.indexOf(entry.tag)
                if (tagPosition >= 0) {
                    tagSpinner.setSelection(tagPosition)
                }
                dateEditText.text = entry.date
                titleEditText.setText(entry.title)
                descriptionEditText.setText(entry.description)
                existingImageUrl = entry.imageUrl

                Glide.with(this)
                    .load(entry.imageUrl)
                    .placeholder(R.drawable.ic_cat_journey)
                    .into(imagePreview)
            }
        } else {
            headerTextView.text = "NEW RESOURCE ENTRY"
            uploadButton.text = "Upload"
        }

        backButton.setOnClickListener {
            findNavController().popBackStack()
        }

        btnSelectImage.setOnClickListener {
            selectImageLauncher.launch("image/*")
        }

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

            if (!isUpdateMode && imageUri == null) {
                Toast.makeText(requireContext(), "Please select an image", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            setLoading(true)

            if (isUpdateMode) {
                if (imageUri != null) {
                    uploadImageForUpdate(tag, date, title, desc, existingImageUrl)
                } else {
                    updateResourceInFirestore(tag, date, title, desc, existingImageUrl!!)
                }
            } else {
                if (imageUri != null) {
                    uploadImageForNewEntry(tag, date, title, desc)
                } else {
                    Toast.makeText(requireContext(), "Please select an image", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

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
                val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID")) // Corrected format to include year
                dateEditText.text = dateFormat.format(selectedDate.time)
            },
            year,
            month,
            day
        )
        datePickerDialog.show()
    }

    private fun uploadImageForNewEntry(tag: String, date: String, title: String, desc: String) {
        setLoading(true)
        val fileName = "resource_${System.currentTimeMillis()}.jpg"
        val storageRef = storage.getReference("/images/resource/$fileName")

        storageRef.putFile(imageUri!!)
            .addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { uri ->
                    val imageUrl = uri.toString()
                    saveResourceToFirestore(tag, date, title, desc, imageUrl)
                }
                    .addOnFailureListener { e ->
                        setLoading(false)
                        Toast.makeText(requireContext(), "Failed to get download URL: ${e.message}", Toast.LENGTH_LONG).show()
                        Log.e("AddResourceFragment", "Failed to get download URL", e)
                    }
            }
            .addOnFailureListener { e ->
                setLoading(false)
                Toast.makeText(requireContext(), "Upload image failed: ${e.message}", Toast.LENGTH_LONG).show()
                Log.e("AddResourceFragment", "Upload image failed", e)
            }
    }

    private fun uploadImageForUpdate(tag: String, date: String, title: String, desc: String, oldImageUrl: String?) {
        setLoading(true)
        val fileName = "resource_${System.currentTimeMillis()}.jpg"
        val storageRef = storage.getReference("/images/resource/$fileName")

        storageRef.putFile(imageUri!!)
            .addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { uri ->
                    val newImageUrl = uri.toString()
                    if (oldImageUrl != null && oldImageUrl.isNotBlank() && oldImageUrl != newImageUrl) {
                        storage.getReferenceFromUrl(oldImageUrl).delete()
                            .addOnSuccessListener {
                                Log.d("AddResourceFragment", "Old image deleted from Storage.")
                                updateResourceInFirestore(tag, date, title, desc, newImageUrl)
                            }
                            .addOnFailureListener { e ->
                                Log.e("AddResourceFragment", "Failed to delete old image: ${e.message}", e)
                                updateResourceInFirestore(tag, date, title, desc, newImageUrl)
                            }
                    } else {
                        updateResourceInFirestore(tag, date, title, desc, newImageUrl)
                    }
                }
            }
            .addOnFailureListener { e ->
                setLoading(false)
                Toast.makeText(requireContext(), "Upload new image failed: ${e.message}", Toast.LENGTH_LONG).show()
                Log.e("AddResourceFragment", "Upload image for update failed", e)
            }
    }

    private fun saveResourceToFirestore(tag: String, date: String, title: String, desc: String, imageUrl: String) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            setLoading(false)
            Toast.makeText(requireContext(), "User not logged in. Please sign in again.", Toast.LENGTH_SHORT).show()
            return
        }

        val newResourceEntry = ResourceEntry(
            userId = userId,
            tag = tag,
            title = title,
            date = date,
            description = desc,
            imageUrl = imageUrl,
            timestamp = null
        )

        db.collection("resources")
            .add(newResourceEntry)
            .addOnSuccessListener { documentReference ->
                setLoading(false)
                Toast.makeText(requireContext(), "Resource added successfully!", Toast.LENGTH_SHORT).show()
                Log.d("AddResourceFragment", "DocumentSnapshot added with ID: ${documentReference.id}")
                findNavController().popBackStack()
            }
            .addOnFailureListener { e ->
                setLoading(false)
                Toast.makeText(requireContext(), "Failed to add resource: ${e.message}", Toast.LENGTH_LONG).show()
                Log.e("AddResourceFragment", "Error adding document", e)
            }
    }

    private fun updateResourceInFirestore(tag: String, date: String, title: String, desc: String, imageUrl: String) {
        val resourceId = resourceToUpdate?.id
        if (resourceId == null) {
            setLoading(false)
            Toast.makeText(requireContext(), "Error: Resource ID missing for update.", Toast.LENGTH_SHORT).show()
            return
        }

        val updatedData = mapOf(
            "tag" to tag,
            "title" to title,
            "date" to date,
            "description" to desc,
            "imageUrl" to imageUrl
            // userId dan timestamp tidak diupdate karena itu field yang dibuat otomatis/statis
        )

        db.collection("resources").document(resourceId)
            .update(updatedData)
            .addOnSuccessListener {
                setLoading(false)
                Toast.makeText(requireContext(), "Resource updated successfully!", Toast.LENGTH_SHORT).show()
                // BARIS PERUBAHAN DI SINI:
                // Pop back to journalFragment (parent dari ResourceContentFragment)
                // dan pastikan ResourceContentFragment juga muncul di atasnya
                findNavController().popBackStack(R.id.journalFragment, false) // popUpTo journalFragment
                // Kemudian navigasi ke ResourceContentFragment, yang akan ditampilkan di dalam JournalFragment
                // Perhatikan: ini akan membuat ResourceContentFragment menjadi fragment aktif di dalam JournalFragment
                // tanpa memulai JournalFragment dari awal jika sudah ada di back stack.
                // findNavController().navigate(R.id.resourceContentFragment) // TIDAK BISA LANGSUNG KE FRAGMENT CHILD
            }
            .addOnFailureListener { e ->
                setLoading(false)
                Toast.makeText(requireContext(), "Failed to update resource: ${e.message}", Toast.LENGTH_LONG).show()
                Log.e("AddResourceFragment", "Error updating document", e)
            }
    }

    private fun setLoading(isLoading: Boolean) {
        uploadButton.isEnabled = !isLoading
        btnSelectImage.isEnabled = !isLoading
    }
}