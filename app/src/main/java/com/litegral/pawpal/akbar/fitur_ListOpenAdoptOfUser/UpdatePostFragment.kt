package com.litegral.pawpal.akbar.fitur_ListOpenAdoptOfUser

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.litegral.pawpal.R
import com.litegral.pawpal.akbar.model.CatModel
import com.litegral.pawpal.akbar.fitur_petDetail.adapter.PetImageSliderAdapter

class UpdatePostFragment : Fragment() {

    private val args: UpdatePostFragmentArgs by navArgs()
    private var currentPet: CatModel? = null

    // Views
    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout
    private lateinit var buttonBack: ImageButton
    private lateinit var textPetName: TextView
    private lateinit var textPetDescription: TextView
    private lateinit var buttonUpdate: Button
    private lateinit var buttonDelete: Button
    private lateinit var progressBar: ProgressBar

    // Firebase
    private lateinit var storage: FirebaseStorage
    private lateinit var db: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_update_post, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        storage = FirebaseStorage.getInstance()
        db = FirebaseFirestore.getInstance()

        initViews(view)
        setupClickListeners()
        loadPetDetailsFromFirestore(args.petId)
    }

    private fun initViews(view: View){
        viewPager = view.findViewById(R.id.viewPager_pet_images_update)
        tabLayout = view.findViewById(R.id.tabLayout_image_indicator_update)
        buttonBack = view.findViewById(R.id.button_back_update)
        textPetName = view.findViewById(R.id.textView_pet_name_update)
        textPetDescription = view.findViewById(R.id.textView_pet_description_update)
        buttonUpdate = view.findViewById(R.id.button_update)
        buttonDelete = view.findViewById(R.id.button_delete)
        progressBar = view.findViewById(R.id.progressBar_update) // Pastikan ID ini ada di XML
    }

    private fun setupClickListeners(){
        buttonBack.setOnClickListener { findNavController().popBackStack() }
        buttonUpdate.setOnClickListener { navigateToEditForm() }
        buttonDelete.setOnClickListener { showDeleteConfirmationDialog() }
    }

    // LOAD DATA SEBELUMNYA UNTUK UPDATE
    private fun loadPetDetailsFromFirestore(petId: String) {
        setLoading(true)
        db.collection("pets").document(petId).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    currentPet = document.toObject(CatModel::class.java)
                    currentPet?.let { displayPetData(it) }
                } else {
                    Toast.makeText(context, "Data hewan tidak ditemukan.", Toast.LENGTH_SHORT).show()
                }
                setLoading(false)
            }
            .addOnFailureListener { exception ->
                setLoading(false)
                Toast.makeText(context, "Gagal memuat detail.", Toast.LENGTH_SHORT).show()
                Log.w("UpdatePostFragment", "Error getting document", exception)
            }
    }

    // FUNCTION UNTUK MENAMPILKAN GAMBAR DALAM BENTUK SLIDER
    private fun displayPetData(pet: CatModel) {
        textPetName.text = pet.name
        textPetDescription.text = pet.description

        if (pet.imageUrls.isNotEmpty()) {
            val imageSliderAdapter = PetImageSliderAdapter(pet.imageUrls)
            viewPager.adapter = imageSliderAdapter

            if (pet.imageUrls.size > 1) {
                tabLayout.visibility = View.VISIBLE
                TabLayoutMediator(tabLayout, viewPager) { _, _ -> }.attach()
            } else {
                tabLayout.visibility = View.GONE
            }
        }
    }

    // FUCNTION UNTUK NAVIGASI KE HALAMAN UPDATE FORM
    private fun navigateToEditForm() {
        val petId = args.petId
        try {
            val action = UpdatePostFragmentDirections.actionUpdatePostFragmentToCreateAdoptPostFragment(petId)
            findNavController().navigate(action)
        } catch (e: Exception) {
            Log.e("UpdatePostFragment", "Navigasi ke form edit gagal: ${e.message}")
        }
    }

    // DIALOG
    private fun showDeleteConfirmationDialog() {
        if (context == null || currentPet == null) return

        AlertDialog.Builder(requireContext())
            .setTitle("Hapus Postingan")
            .setMessage("Anda yakin ingin menghapus postingan untuk ${currentPet?.name}?")
            .setPositiveButton("Ya, Hapus") { _, _ -> performDelete() }
            .setNegativeButton("Tidak", null)
            .setIcon(R.drawable.cat_dislike)
            .show()
    }

    // LOGIC DELETE
    private fun performDelete() {
        val petToDelete = currentPet ?: return
        setLoading(true)

        // HAPUS GAMBAR YANG ADA DAN HAPUS DOCUMENT
        deleteImagesFromStorage(petToDelete.imageUrls) { allImagesDeleted ->
            if (allImagesDeleted) {
                deletePostFromFirestore(petToDelete.id)
            } else {
                setLoading(false)
                Toast.makeText(context, "Gagal menghapus beberapa file gambar.", Toast.LENGTH_LONG).show()
            }
        }
    }

    // FUNCTION YANG DI PANGGIKL DI PERFORM DELETE UNTUK DELETE GAMBAR PADA DOCUMENT BERDASARKAN ID
    private fun deleteImagesFromStorage(imageUrls: List<String>, onComplete: (Boolean) -> Unit) {
        if (imageUrls.isEmpty()) {
            onComplete(true); return
        }
        var processedCount = 0
        var success = true
        imageUrls.forEach { url ->
            if (url.isNotBlank()) {
                storage.getReferenceFromUrl(url).delete().addOnCompleteListener { task ->
                    if (!task.isSuccessful) {
                        success = false
                        Log.w("UpdatePostFragment", "Gagal hapus gambar: $url", task.exception)
                    }
                    processedCount++
                    if (processedCount == imageUrls.size) onComplete(success)
                }
            } else {
                processedCount++
                if (processedCount == imageUrls.size) onComplete(success)
            }
        }
    }

    // FUNCTION YANG DI PANGGIKL DI PERFORM DELETE UNTUK DELETE POSTINGAN PADA DOCUMENT BERDASARKAN ID
    private fun deletePostFromFirestore(petId: String) {
        db.collection("pets").document(petId).delete()
            .addOnSuccessListener {
                Toast.makeText(context, "Postingan berhasil dihapus.", Toast.LENGTH_LONG).show()
                findNavController().popBackStack() // Kembali ke halaman riwayat
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Gagal menghapus data: ${e.message}", Toast.LENGTH_LONG).show()
            }
            .addOnCompleteListener { setLoading(false) }
    }

    private fun setLoading(isLoading: Boolean) {
        progressBar.isVisible = isLoading
        buttonUpdate.isEnabled = !isLoading
        buttonDelete.isEnabled = !isLoading
    }
}