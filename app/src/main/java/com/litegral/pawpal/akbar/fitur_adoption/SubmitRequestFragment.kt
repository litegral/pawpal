package com.litegral.pawpal.akbar.fitur_adoption

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.litegral.pawpal.R
import com.litegral.pawpal.akbar.model.AdoptionRequest
import com.litegral.pawpal.akbar.model.CatModel
import java.util.*
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import com.bumptech.glide.Glide

class SubmitRequestFragment : Fragment(R.layout.fragment_submit_request) {

    private val args: SubmitRequestFragmentArgs by navArgs()
    private var isEditMode = false
    private var existingRequest: AdoptionRequest? = null
    private var petToAdopt: CatModel? = null
    private var selectedImageUri: Uri? = null

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var storage: FirebaseStorage

    private lateinit var nameEditText: EditText
    private lateinit var contactEditText: EditText
    private lateinit var messageEditText: EditText
    private lateinit var photoPreview: ImageView
    private lateinit var sendButton: Button
    private lateinit var backButton: ImageButton
    private lateinit var progressBar: ProgressBar

    private val imagePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            selectedImageUri = uri
            photoPreview.setImageURI(uri)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

        initViews(view)
        setupListeners()

        if (args.requestId != null) {
            isEditMode = true
            loadExistingRequestForEdit(args.requestId!!)
        } else {
            isEditMode = false
            loadPetData()
        }
        loadPetData()
    }

    private fun loadExistingRequestForEdit(requestId: String) {
        setLoading(true)
        db.collection("adoptionRequests").document(requestId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    existingRequest = document.toObject(AdoptionRequest::class.java)
                    populateFormWithExistingData()
                } else {
                    Toast.makeText(context, "Request data not found.", Toast.LENGTH_SHORT).show()
                }
                setLoading(false)
            }
            .addOnFailureListener {
                setLoading(false)
                Toast.makeText(context, "Failed to load request data for editing.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun populateFormWithExistingData() {
        existingRequest?.let { request ->
            nameEditText.setText(request.adopterName)
            contactEditText.setText(request.adopterContact)
            messageEditText.setText(request.message)
            // Load the home photo if it exists
            if (!request.homePhotoUrl.isNullOrEmpty()) {
                Glide.with(this).load(request.homePhotoUrl).into(photoPreview)
            }
        }
    }

    private fun initViews(view: View) {
        nameEditText = view.findViewById(R.id.editText_name_submit)
        contactEditText = view.findViewById(R.id.editText_contact_submit)
        messageEditText = view.findViewById(R.id.editText_message_submit)
        photoPreview = view.findViewById(R.id.imageView_photo_preview)
        sendButton = view.findViewById(R.id.button_send_request)
        backButton = view.findViewById(R.id.button_back_submit)
        progressBar = view.findViewById(R.id.progressBar_submit)
    }

    private fun setupListeners() {
        backButton.setOnClickListener { findNavController().popBackStack() }
        photoPreview.setOnClickListener { imagePickerLauncher.launch("image/*") }
        sendButton.setOnClickListener { sendAdoptionRequest() }
    }

    private fun loadPetData() {
        setLoading(true)
        db.collection("pets").document(args.petId).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    petToAdopt = document.toObject(CatModel::class.java)
                } else {
                    Toast.makeText(context, "Pet data not found.", Toast.LENGTH_SHORT).show()
                    findNavController().popBackStack()
                }
                setLoading(false)
            }
            .addOnFailureListener {
                Toast.makeText(context, "Failed to load pet data.", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
                setLoading(false)
            }
    }

    private fun sendAdoptionRequest() {
        val name = nameEditText.text.toString().trim()
        val contact = contactEditText.text.toString().trim()
        val message = messageEditText.text.toString().trim()

        if (name.isEmpty() || contact.isEmpty() || message.isEmpty()) {
            Toast.makeText(context, "Please fill all fields.", Toast.LENGTH_SHORT).show()
            return
        }

        setLoading(true)

        // If in edit mode, just update and stop.
        if (isEditMode) {
            updateAdoptionRequest()
            return // <-- Add a return to stop the function here.
        }

        // --- Create Mode Logic ---
        val currentUser = auth.currentUser
        val pet = petToAdopt

        if (currentUser == null || pet == null) {
            Toast.makeText(context, "User or pet data is missing.", Toast.LENGTH_SHORT).show()
            setLoading(false)
            return
        }

        if (selectedImageUri != null) {
            uploadImageAndCreateRequest(name, contact, message, currentUser.uid, pet)
        } else {
            createRequest(name, contact, message, currentUser.uid, pet, null)
        }
    }

    private fun updateAdoptionRequest() {
        val requestId = args.requestId ?: return
        setLoading(true)

        val updatedData = mapOf(
            "adopterName" to nameEditText.text.toString().trim(),
            "adopterContact" to contactEditText.text.toString().trim(),
            "message" to messageEditText.text.toString().trim()
            // Note: Updating the photo is more complex. For now, we'll just update text.
        )

        db.collection("adoptionRequests").document(requestId)
            .update(updatedData)
            .addOnSuccessListener {
                setLoading(false)
                Toast.makeText(context, "Request updated successfully!", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            }
            .addOnFailureListener { e ->
                setLoading(false)
                Toast.makeText(context, "Failed to update request: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun uploadImageAndCreateRequest(name: String, contact: String, message: String, adopterId: String, pet: CatModel) {
        val fileName = "adoption_requests/${UUID.randomUUID()}.jpg"
        val imageRef = storage.reference.child(fileName)
        imageRef.putFile(selectedImageUri!!)
            .addOnSuccessListener {
                imageRef.downloadUrl.addOnSuccessListener { uri ->
                    createRequest(name, contact, message, adopterId, pet, uri.toString())
                }
            }
            .addOnFailureListener {
                setLoading(false)
                Toast.makeText(context, "Image upload failed.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun createRequest(name: String, contact: String, message: String, adopterId: String, pet: CatModel, imageUrl: String?) {
        val requestId = db.collection("adoptionRequests").document().id
        val request = AdoptionRequest(
            id = requestId,
            petId = pet.id,
            petName = pet.name,
            petImageUrl = pet.imageUrls.firstOrNull() ?: "",
            ownerId = pet.uploaderUid,
            adopterId = adopterId,
            adopterName = name,
            adopterContact = contact,
            message = message,
            homePhotoUrl = imageUrl,
            status = "Pending"
        )

        db.collection("adoptionRequests").document(requestId).set(request)
            .addOnSuccessListener {
                setLoading(false)
                Toast.makeText(context, "Request sent successfully!", Toast.LENGTH_SHORT).show()
                // You can navigate to a dedicated success screen here if you want
                findNavController().popBackStack()
            }
            .addOnFailureListener { e ->
                setLoading(false)
                Toast.makeText(context, "Failed to send request: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun setLoading(isLoading: Boolean) {
        progressBar.isVisible = isLoading
        sendButton.isEnabled = !isLoading
    }
}