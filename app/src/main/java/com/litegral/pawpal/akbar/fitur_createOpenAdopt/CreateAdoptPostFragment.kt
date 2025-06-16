// Pastikan file ini berada di package: com.litegral.pawpal.akbar
package com.litegral.pawpal.akbar.fitur_createOpenAdopt

import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.litegral.pawpal.R
import com.litegral.pawpal.akbar.model.CatModel
import java.util.UUID

class CreateAdoptPostFragment : Fragment() {

    private val args: CreateAdoptPostFragmentArgs by navArgs()
    private var isEditMode = false
    private val MAX_DOCUMENTS = 2

    // Firebase instances
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var storage: FirebaseStorage

    // Views
    private lateinit var buttonBackForm: ImageButton
    private lateinit var textViewFormTitle: TextView
    private lateinit var editTextName: EditText
    private lateinit var editTextAge: EditText
    private lateinit var spinnerGender: Spinner
    private lateinit var editTextBreed: EditText
    private lateinit var editTextDescription: EditText
    private lateinit var buttonUploadDocument: Button
    private lateinit var layoutSelectedDocuments: LinearLayout
    private lateinit var imageViewProfilePhotoPreview: ImageView
    private lateinit var buttonSubmitAdoptPost: Button
    private lateinit var progressBar: ProgressBar

    // Data URI & URL
    private var selectedProfilePhotoUri: Uri? = null
    private var selectedDocumentUris: MutableList<Uri> = mutableListOf()
    private var existingImageUrls: MutableList<String> = mutableListOf()

    // Launcher untuk gambar
    private val pickProfilePhotoLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) {
                selectedProfilePhotoUri = uri
                imageViewProfilePhotoPreview.setImageURI(uri)
                imageViewProfilePhotoPreview.setPadding(0, 0, 0, 0)
            }
        }

    private val pickDocumentsLauncher =
        registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
            if (uris.isNotEmpty()) {
                val currentDocCount = existingImageUrls.drop(1).size + selectedDocumentUris.size
                val slotsAvailable = MAX_DOCUMENTS - currentDocCount

                if (uris.size > slotsAvailable) {
                    Toast.makeText(context, "Batas maksimal $MAX_DOCUMENTS dokumen. Hanya $slotsAvailable file pertama yang ditambahkan.", Toast.LENGTH_LONG).show()
                }

                // Hanya ambil file sebanyak slot yang tersedia
                val urisToAdd = uris.take(slotsAvailable)
                selectedDocumentUris.addAll(urisToAdd)

                updateSelectedDocumentsUI()
                updateUploadButtonState() // Perbarui status tombol setelah memilih
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_create_adopt_post, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

        initViews(view)
        setupSpinner()
        setupClickListeners()

        val petIdFromArgs = args.petId
        if (petIdFromArgs != null) {
            isEditMode = true
            loadPetDataForEdit(petIdFromArgs)
        } else {
            isEditMode = false
            setupCreateMode()
        }
    }

    private fun initViews(view: View) {
        buttonBackForm = view.findViewById(R.id.button_back_form)
        textViewFormTitle = view.findViewById(R.id.textView_form_title)
        editTextName = view.findViewById(R.id.editText_name_form_post)
        editTextAge = view.findViewById(R.id.editText_age_form_post)
        spinnerGender = view.findViewById(R.id.spinner_gender_form_post)
        editTextBreed = view.findViewById(R.id.editText_breed_form_post)
        editTextDescription = view.findViewById(R.id.editText_description_form_post)
        buttonUploadDocument = view.findViewById(R.id.button_upload_document)
        layoutSelectedDocuments = view.findViewById(R.id.layout_selected_documents)
        imageViewProfilePhotoPreview = view.findViewById(R.id.imageView_profile_photo_preview)
        buttonSubmitAdoptPost = view.findViewById(R.id.button_submit_adopt_post)
        progressBar = view.findViewById(R.id.progressBar_form)
    }

    // SETUP SPINNER
    private fun setupSpinner() {
        ArrayAdapter.createFromResource(
            requireContext(), R.array.gender_array, android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerGender.adapter = adapter
        }
    }

    //
    private fun setupClickListeners() {
        buttonBackForm.setOnClickListener { findNavController().popBackStack() }
        imageViewProfilePhotoPreview.setOnClickListener { pickProfilePhotoLauncher.launch("image/*") }
        // Listener tombol dokumen sekarang memanggil launcher yang sudah "pintar"
        buttonUploadDocument.setOnClickListener { pickDocumentsLauncher.launch("image/*") }
        buttonSubmitAdoptPost.setOnClickListener { submitForm() }
    }

    // --- FUNGSI INI SEKARANG MENGAMBIL DATA DARI FIREBASE DAN MENGISI FORM ---
    private fun loadPetDataForEdit(petId: String) {
        setLoading(true)
        textViewFormTitle.text = "Update Post"
        buttonSubmitAdoptPost.text = "Save Changes"

        db.collection("pets").document(petId).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val petToEdit = document.toObject(CatModel::class.java)
                    if (petToEdit != null) {
                        // Isi semua form dengan data yang sudah ada
                        editTextName.setText(petToEdit.name)
                        editTextAge.setText(petToEdit.age)
                        editTextBreed.setText(petToEdit.breed)
                        editTextDescription.setText(petToEdit.description)
                        spinnerGender.setSelection(if (petToEdit.isFemale) 1 else 0)
                        existingImageUrls.addAll(petToEdit.imageUrls)
                        if (existingImageUrls.isNotEmpty()) {
                            Glide.with(this).load(existingImageUrls[0]).into(imageViewProfilePhotoPreview)
                        }
                        updateSelectedDocumentsUI()
                        updateUploadButtonState()
                    }
                }
                setLoading(false)
            }
            .addOnFailureListener {
                setLoading(false)
                Toast.makeText(context, "Gagal memuat data untuk diedit.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun setupCreateMode() {
        textViewFormTitle.text = "Open Adopt Form"
        buttonSubmitAdoptPost.text = "Upload Postingan"
        updateUploadButtonState()
    }

    private fun submitForm() {
        setLoading(true)
        val name = editTextName.text.toString().trim()
        val age = editTextAge.text.toString().trim()
        val breed = editTextBreed.text.toString().trim()
        val description = editTextDescription.text.toString().trim()
        val isFemale = spinnerGender.selectedItem.toString().equals("Female", true)

        if (name.isBlank() || age.isBlank()) {
            Toast.makeText(context, "Nama dan Umur wajib diisi.", Toast.LENGTH_SHORT).show()
            setLoading(false)
            return
        }

        if (isEditMode) {
            // Memanggil fungsi untuk handle logika update
            handleUpdatePost(args.petId!!, name, age, isFemale, breed, description)
        } else {
            // Memanggil fungsi untuk handle logika create
            if (selectedProfilePhotoUri == null) {
                Toast.makeText(context, "Foto Profil wajib diisi.", Toast.LENGTH_SHORT).show()
                setLoading(false)
                return
            }
            uploadNewImagesAndSaveToFirestore(true, name, age, isFemale, breed, description, null)
        }
    }

    private fun handleUpdatePost(petId: String, name: String, age: String, isFemale: Boolean, breed: String, description: String) {
        val newImagesToUpload = mutableListOf<Uri>()
        selectedProfilePhotoUri?.let { newImagesToUpload.add(it) }
        newImagesToUpload.addAll(selectedDocumentUris)

        if (newImagesToUpload.isNotEmpty()) {
            // Jika ada gambar baru yang dipilih, upload dulu
            uploadNewImagesAndSaveToFirestore(false, name, age, isFemale, breed, description, petId)
        } else {
            // Jika tidak ada gambar baru, langsung update data teks
            updateFirestoreDocument(petId, name, age, isFemale, breed, description, existingImageUrls)
        }
    }

    private fun uploadNewImagesAndSaveToFirestore(isCreate: Boolean, name: String, age: String, isFemale: Boolean, breed: String, description: String, petId: String?) {
        val uploaderId = auth.currentUser?.uid ?: return setLoading(false)

        val allUrisToUpload = mutableListOf<Uri>()
        selectedProfilePhotoUri?.let { allUrisToUpload.add(it) }
        allUrisToUpload.addAll(selectedDocumentUris)

        val newUploadedUrls = mutableMapOf<Uri, String>()
        var uploadCounter = 0

        allUrisToUpload.forEach { uri ->
            val fileName = "${UUID.randomUUID()}.jpg"
            val imageRef = storage.reference.child("images/$uploaderId/$fileName")

            imageRef.putFile(uri)
                .addOnSuccessListener {
                    imageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                        newUploadedUrls[uri] = downloadUrl.toString()
                        uploadCounter++
                        if (uploadCounter == allUrisToUpload.size) {
                            if (isCreate) {
                                // Proses CREATE: urutkan URL dan simpan dokumen baru
                                val finalUrls = sortUrls(newUploadedUrls)
                                createFirestoreDocument(name, age, isFemale, breed, description, finalUrls)
                            } else {
                                // Proses UPDATE: gabungkan URL lama dan baru, lalu update dokumen
                                val finalUrls = mergeUrls(existingImageUrls, newUploadedUrls)
                                updateFirestoreDocument(petId!!, name, age, isFemale, breed, description, finalUrls)
                            }
                        }
                    }
                }
                .addOnFailureListener { e -> setLoading(false); Toast.makeText(context, "Gagal unggah: ${e.message}", Toast.LENGTH_SHORT).show() }
        }
    }

    private fun sortUrls(newUrls: Map<Uri, String>): List<String> {
        val sortedList = mutableListOf<String>()
        selectedProfilePhotoUri?.let {
            sortedList.add(newUrls[it]!!)
        }
        selectedDocumentUris.forEach {
            sortedList.add(newUrls[it]!!)
        }
        return sortedList
    }

    private fun mergeUrls(oldUrls: List<String>, newUrls: Map<Uri, String>): List<String> {
        val finalList = oldUrls.toMutableList()
        // Ganti foto profil jika ada yang baru
        selectedProfilePhotoUri?.let {
            // Hapus foto profil lama dari storage
            if (oldUrls.isNotEmpty() && oldUrls[0].isNotBlank()) {
                storage.getReferenceFromUrl(oldUrls[0]).delete()
            }
            finalList[0] = newUrls[it]!!
        }
        // Tambahkan URL dokumen baru
        selectedDocumentUris.forEach {
            finalList.add(newUrls[it]!!)
        }
        return finalList
    }
    // --- FUNGSI BARU UNTUK MENGONTROL TOMBOL UPLOAD DOKUMEN ---
    private fun updateUploadButtonState() {
        // Hitung total dokumen (yang sudah ada + yang baru dipilih)
        val totalDocumentCount = existingImageUrls.drop(1).size + selectedDocumentUris.size

        if (totalDocumentCount >= MAX_DOCUMENTS) {
            buttonUploadDocument.isEnabled = false // Nonaktifkan tombol
            buttonUploadDocument.text = "Batas Dokumen Tercapai"
        } else {
            buttonUploadDocument.isEnabled = true // Aktifkan tombol
            buttonUploadDocument.text = "Pilih Dokumen"
        }
    }

    private fun createFirestoreDocument(name: String, age: String, isFemale: Boolean, breed: String, description: String, imageUrls: List<String>) {
        val uploaderId = auth.currentUser?.uid ?: return
        val newPetId = db.collection("pets").document().id

        val newPet = CatModel(
            id = newPetId, uploaderUid = uploaderId, uploaderName = auth.currentUser?.displayName ?: "",
            name = name, age = age, isFemale = isFemale, breed = breed, description = description,
            imageUrls = imageUrls, petPosition = "Bogor"
        )
        db.collection("pets").document(newPetId).set(newPet)
            .addOnSuccessListener {
                Toast.makeText(context, "Postingan berhasil diunggah!", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            }
            .addOnFailureListener { e -> Toast.makeText(context, "Gagal menyimpan: ${e.message}", Toast.LENGTH_SHORT).show() }
            .addOnCompleteListener { setLoading(false) }
    }

    private fun updateFirestoreDocument(petId: String, name: String, age: String, isFemale: Boolean, breed: String, description: String, imageUrls: List<String>) {
        val updatedData = mapOf(
            "name" to name, "age" to age, "isFemale" to isFemale,
            "breed" to breed, "description" to description, "imageUrls" to imageUrls
        )
        db.collection("pets").document(petId).update(updatedData)
            .addOnSuccessListener {
                Toast.makeText(context, "Postingan berhasil diperbarui!", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            }
            .addOnFailureListener { e -> Toast.makeText(context, "Gagal memperbarui: ${e.message}", Toast.LENGTH_SHORT).show() }
            .addOnCompleteListener { setLoading(false) }
    }

    private fun setLoading(isLoading: Boolean) {
        progressBar.isVisible = isLoading
        buttonSubmitAdoptPost.isEnabled = !isLoading
    }

    // Fungsi-fungsi helper untuk UI dokumen
    private fun updateSelectedDocumentsUI() {
        layoutSelectedDocuments.removeAllViews()
        existingImageUrls.drop(1).forEach { url ->
            val fileName = url.substringAfterLast('%', "Dokumen").substringAfter('F').substringBefore('?').take(15) + "..."
            layoutSelectedDocuments.addView(createDocumentTextView(fileName))
        }
        selectedDocumentUris.forEach { uri ->
            layoutSelectedDocuments.addView(createDocumentTextView(getFileName(uri) ?: "Dokumen Baru"))
        }
    }
    private fun createDocumentTextView(fileName: String): TextView {
        return TextView(context).apply {
            text = fileName; textSize = 14f; setPadding(0, 8, 0, 8)
            setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_document_upload, 0, 0, 0)
            compoundDrawablePadding = 16
        }
    }
    private fun getFileName(uri: Uri): String? {
        context?.contentResolver?.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1) return cursor.getString(nameIndex)
            }
        }
        return null
    }
}