package com.litegral.pawpal

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

// Nama kelas mengikuti konvensi, di dalam package utama
class ProfileFragment : Fragment(R.layout.fragment_profile) {

    // Firebase instances
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var storage: FirebaseStorage

    // Views
    private lateinit var backButton: ImageButton
    private lateinit var profileImageView: ImageView
    private lateinit var changePhotoButton: MaterialButton
    private lateinit var nameEditText: TextInputEditText
    private lateinit var saveProfileButton: MaterialButton
    private lateinit var logoutButton: MaterialButton // Tombol Logout
    private lateinit var editCatProfileButton: MaterialButton
    private lateinit var progressBar: ProgressBar

    // Variabel untuk menyimpan URI gambar yang baru dipilih
    private var selectedImageUri: Uri? = null

    // Launcher untuk memilih gambar dari galeri
    private val imagePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                selectedImageUri = uri
                // Tampilkan gambar baru yang dipilih di ImageView sebagai preview
                profileImageView.setImageURI(uri)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inisialisasi Firebase
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

        // Menghubungkan variabel dengan View di layout
        initViews(view)
        setupListeners()

        // Muat data profil dari Firestore saat fragment dibuka
        loadProfileFromFirestore()
    }

    private fun initViews(view: View) {
        backButton = view.findViewById(R.id.backButton)
        profileImageView = view.findViewById(R.id.profileImageView)
        changePhotoButton = view.findViewById(R.id.changePhotoButton)
        nameEditText = view.findViewById(R.id.nameEditText)
        saveProfileButton = view.findViewById(R.id.saveProfileButton)
        logoutButton = view.findViewById(R.id.button_logout_profile) // Pastikan ID ini ada di XML
        editCatProfileButton = view.findViewById(R.id.editCatProfileButton)
        progressBar = view.findViewById(R.id.progressBar_profile) // Pastikan ID ini ada di XML
    }

    private fun setupListeners() {
        backButton.setOnClickListener {
            // Kembali ke halaman sebelumnya
            findNavController().navigateUp()
        }

        changePhotoButton.setOnClickListener {
            // Membuka galeri untuk memilih gambar
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            imagePickerLauncher.launch(intent)
        }

        saveProfileButton.setOnClickListener {
            // Menyimpan perubahan profil ke Firebase
            saveProfileChanges()
        }

        editCatProfileButton.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_editCatProfileFragment)
        }

        logoutButton.setOnClickListener {
            // Melakukan logout
            performLogout()
        }
    }

    private fun loadProfileFromFirestore() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Toast.makeText(context, "Pengguna tidak ditemukan, silakan login ulang.", Toast.LENGTH_SHORT).show()
            return
        }

        setLoading(true) // Tampilkan loading

        // Mengambil dokumen pengguna dari koleksi 'users'
        db.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val name = document.getString("displayName")
                    val imageUrl = document.getString("profilePhotoUrl")

                    nameEditText.setText(name)

                    // Jika ada URL gambar, gunakan Glide untuk menampilkannya
                    if (!imageUrl.isNullOrEmpty()) {
                        Glide.with(this)
                            .load(imageUrl)
                            .placeholder(R.drawable.ic_profile_placeholder)
                            .error(R.drawable.ic_profile_placeholder) // Gambar jika URL error
                            .into(profileImageView)
                    }
                } else {
                    Log.w("ProfileFragment", "Dokumen profil tidak ada untuk user: $userId")
                }
                setLoading(false) // Sembunyikan loading
            }
            .addOnFailureListener { e ->
                setLoading(false)
                Toast.makeText(context, "Gagal mengambil data profil.", Toast.LENGTH_SHORT).show()
                Log.e("ProfileFragment", "Gagal memuat profil", e)
            }
    }

    private fun saveProfileChanges() {
        val userId = auth.currentUser?.uid ?: return
        val name = nameEditText.text.toString().trim()

        if (name.isEmpty()) {
            Toast.makeText(context, "Nama tidak boleh kosong", Toast.LENGTH_SHORT).show()
            return
        }

        setLoading(true)

        // Cek apakah pengguna memilih gambar baru atau tidak
        if (selectedImageUri != null) {
            // Jika ya, upload dulu gambar baru ke Firebase Storage
            // Menggunakan UID pengguna sebagai nama file untuk memastikan unik dan mudah diganti
            val storageRef = storage.reference.child("profile_images/$userId")
            storageRef.putFile(selectedImageUri!!)
                .addOnSuccessListener {
                    // Setelah upload berhasil, dapatkan URL download-nya
                    storageRef.downloadUrl.addOnSuccessListener { uri ->
                        // Simpan nama DAN URL gambar baru ke Firestore
                        updateUserDocument(userId, name, uri.toString())
                    }
                }
                .addOnFailureListener { e ->
                    setLoading(false)
                    Toast.makeText(context, "Gagal mengupload gambar: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            // Jika tidak ada gambar baru, langsung update nama saja di Firestore
            updateUserDocument(userId, name, null)
        }
    }

    private fun updateUserDocument(userId: String, name: String, imageUrl: String?) {
        // Buat map untuk menyimpan data yang akan di-update
        val userProfileMap = mutableMapOf<String, Any>(
            "displayName" to name
        )

        // Hanya tambahkan `profilePhotoUrl` ke map jika ada URL baru
        if (imageUrl != null) {
            userProfileMap["profilePhotoUrl"] = imageUrl
        }

        // Gunakan .update() untuk memperbarui field yang ada tanpa menghapus field lain
        db.collection("users").document(userId)
            .update(userProfileMap)
            .addOnSuccessListener {
                setLoading(false)
                Toast.makeText(context, "Profil berhasil disimpan!", Toast.LENGTH_SHORT).show()
                findNavController().navigateUp() // Kembali ke halaman sebelumnya (HomeFragment)
            }
            .addOnFailureListener { e ->
                setLoading(false)
                Toast.makeText(context, "Gagal menyimpan profil: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun performLogout() {
        auth.signOut()
        Toast.makeText(context, "Anda telah logout", Toast.LENGTH_SHORT).show()

        // Arahkan ke SignInActivity dan bersihkan semua activity sebelumnya
        val intent = Intent(activity, SignInActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        activity?.finish() // Tutup MainActivity
    }

    private fun setLoading(isLoading: Boolean) {
        progressBar.isVisible = isLoading
        saveProfileButton.isEnabled = !isLoading
        logoutButton.isEnabled = !isLoading
        editCatProfileButton.isEnabled = !isLoading
    }
}