package com.litegral.pawpal.match.ui

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.litegral.pawpal.R
import com.litegral.pawpal.match.model.Cat
import java.util.UUID

class EditCatProfileFragment : Fragment(R.layout.fragment_edit_cat_profile) {

    private lateinit var catImageView: ImageView
    private lateinit var btnSelectImage: Button
    private lateinit var nameEditText: TextInputEditText
    private lateinit var breedEditText: TextInputEditText
    private lateinit var ageEditText: TextInputEditText
    private lateinit var genderEditText: TextInputEditText
    private lateinit var descriptionEditText: TextInputEditText
    private lateinit var btnSave: Button
    private lateinit var progressBar: ProgressBar

    private var imageUri: Uri? = null
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    private val selectImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            imageUri = it
            Glide.with(this).load(it).into(catImageView)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews(view)
        setupListeners()
        loadCatProfile()
    }

    private fun initViews(view: View) {
        catImageView = view.findViewById(R.id.catImageView)
        btnSelectImage = view.findViewById(R.id.btnSelectImage)
        nameEditText = view.findViewById(R.id.nameEditText)
        breedEditText = view.findViewById(R.id.breedEditText)
        ageEditText = view.findViewById(R.id.ageEditText)
        genderEditText = view.findViewById(R.id.genderEditText)
        descriptionEditText = view.findViewById(R.id.descriptionEditText)
        btnSave = view.findViewById(R.id.btnSave)
        progressBar = view.findViewById(R.id.progressBar)
    }

    private fun setupListeners() {
        btnSelectImage.setOnClickListener {
            selectImageLauncher.launch("image/*")
        }

        btnSave.setOnClickListener {
            saveCatProfile()
        }
    }

    private fun loadCatProfile() {
        val userId = auth.currentUser?.uid ?: return
        progressBar.visibility = View.VISIBLE

        db.collection("cats").document(userId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val cat = document.toObject(Cat::class.java)
                    cat?.let {
                        nameEditText.setText(it.name)
                        breedEditText.setText(it.breed)
                        ageEditText.setText(it.age)
                        genderEditText.setText(it.gender)
                        descriptionEditText.setText(it.description)
                        if (it.imageUrl.isNotEmpty()) {
                            Glide.with(this).load(it.imageUrl).into(catImageView)
                        }
                    }
                }
                progressBar.visibility = View.GONE
            }
            .addOnFailureListener {
                progressBar.visibility = View.GONE
                Toast.makeText(requireContext(), "Failed to load profile.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveCatProfile() {
        val name = nameEditText.text.toString().trim()
        val breed = breedEditText.text.toString().trim()
        val age = ageEditText.text.toString().trim()
        val gender = genderEditText.text.toString().trim()
        val description = descriptionEditText.text.toString().trim()

        if (name.isEmpty() || breed.isEmpty() || age.isEmpty() || gender.isEmpty() || description.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        progressBar.visibility = View.VISIBLE
        btnSave.isEnabled = false

        val userId = auth.currentUser?.uid
        if (userId == null) {
            Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show()
            progressBar.visibility = View.GONE
            btnSave.isEnabled = true
            return
        }

        if (imageUri != null) {
            uploadImageAndSaveProfile(userId, name, breed, age, gender, description)
        } else {
            // If no new image is selected, we need to preserve the old one.
            // Let's get the existing URL before saving.
            db.collection("cats").document(userId).get().addOnSuccessListener { document ->
                val existingImageUrl = document.toObject(Cat::class.java)?.imageUrl ?: ""
                saveProfileToFirestore(userId, name, breed, age, gender, description, existingImageUrl)
            }
        }
    }

    private fun uploadImageAndSaveProfile(userId: String, name: String, breed: String, age: String, gender: String, description: String) {
        val imageRef = storage.reference.child("cat_images/${UUID.randomUUID()}.jpg")
        imageUri?.let { uri ->
            imageRef.putFile(uri)
                .addOnSuccessListener {
                    imageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                        saveProfileToFirestore(userId, name, breed, age, gender, description, downloadUrl.toString())
                    }
                }
                .addOnFailureListener { e ->
                    progressBar.visibility = View.GONE
                    btnSave.isEnabled = true
                    Toast.makeText(requireContext(), "Image upload failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun saveProfileToFirestore(userId: String, name: String, breed: String, age: String, gender: String, description: String, imageUrl: String) {
        val catProfile = Cat(
            id = userId, // The document ID should be the user's ID
            userId = userId,
            name = name,
            breed = breed,
            age = age,
            gender = gender,
            description = description,
            imageUrl = imageUrl
        )

        db.collection("cats").document(userId).set(catProfile)
            .addOnSuccessListener {
                progressBar.visibility = View.GONE
                btnSave.isEnabled = true
                Toast.makeText(requireContext(), "Profile saved successfully", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            }
            .addOnFailureListener { e ->
                progressBar.visibility = View.GONE
                btnSave.isEnabled = true
                Toast.makeText(requireContext(), "Failed to save profile: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
} 