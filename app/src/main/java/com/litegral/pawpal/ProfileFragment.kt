package com.litegral.pawpal

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class ProfileFragment : Fragment(R.layout.fragment_profile) {

    private lateinit var backButton: ImageButton
    private lateinit var profileImageView: ImageView
    private lateinit var changePhotoButton: MaterialButton
    private lateinit var nameEditText: TextInputEditText
    private lateinit var bioEditText: TextInputEditText
    private lateinit var saveProfileButton: MaterialButton
    private var selectedImageUri: Uri? = null
    private val userPreferences by lazy { UserPreferences.getInstance(requireContext()) }

    // Register activity result launcher for image selection
    private val getContent = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                selectedImageUri = uri
                profileImageView.setImageURI(uri)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeViews(view)
        loadProfileData()
        setupListeners()
    }

    private fun initializeViews(view: View) {
        backButton = view.findViewById(R.id.backButton)
        profileImageView = view.findViewById(R.id.profileImageView)
        changePhotoButton = view.findViewById(R.id.changePhotoButton)
        nameEditText = view.findViewById(R.id.nameEditText)
        bioEditText = view.findViewById(R.id.bioEditText)
        saveProfileButton = view.findViewById(R.id.saveProfileButton)
    }

    private fun loadProfileData() {
        // Load saved profile data from preferences
        nameEditText.setText(userPreferences.getUserName())
        bioEditText.setText(userPreferences.getUserBio())

        // Load profile image if available
        val savedImageUri = userPreferences.getProfileImageUri()
        if (savedImageUri != null) {
            try {
                profileImageView.setImageURI(Uri.parse(savedImageUri))
                selectedImageUri = Uri.parse(savedImageUri)
            } catch (e: Exception) {
                // Fallback to default image if there's an error
            }
        }
    }

    private fun setupListeners() {
        backButton.setOnClickListener {
            findNavController().navigateUp()
        }

        changePhotoButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            getContent.launch(intent)
        }

        saveProfileButton.setOnClickListener {
            saveProfileData()
            Toast.makeText(context, "Profile saved successfully!", Toast.LENGTH_SHORT).show()
            findNavController().navigateUp()
        }
    }

    private fun saveProfileData() {
        val name = nameEditText.text.toString().trim()
        val bio = bioEditText.text.toString().trim()

        // Save to preferences
        userPreferences.setUserName(name)
        userPreferences.setUserBio(bio)
        selectedImageUri?.let {
            userPreferences.setProfileImageUri(it.toString())
        }
    }
}
