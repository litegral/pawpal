// Pastikan file ini ada di package: com.litegral.pawpal.akbar
package com.litegral.pawpal.akbar.fitur_petDetail

import android.graphics.Color
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
import com.litegral.pawpal.R
import com.litegral.pawpal.akbar.model.CatModel
import com.litegral.pawpal.akbar.fitur_petDetail.adapter.PetImageSliderAdapter

class PetDetailFragment : Fragment() {

    // Menerima petId dari fragment sebelumnya
    private val args: PetDetailFragmentArgs by navArgs()

    // Firebase
    private lateinit var db: FirebaseFirestore

    // Deklarasi Views
    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout
    private lateinit var buttonBack: ImageButton
    private lateinit var textPetName: TextView
    private lateinit var textGender: TextView
    private lateinit var textPetBreed: TextView
    private lateinit var textPetAge: TextView
    private lateinit var textPetDescription: TextView
    private lateinit var buttonAdoptMe: Button
    private lateinit var progressBar: ProgressBar

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_pet_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = FirebaseFirestore.getInstance()

        initViews(view)
        setupClickListeners()

        // Ambil data dari Firestore berdasarkan petId yang diterima
        loadPetDetailsFromFirestore(args.petId)
    }

    private fun initViews(view: View) {
        viewPager = view.findViewById(R.id.viewPager_pet_images)
        tabLayout = view.findViewById(R.id.tabLayout_image_indicator)
        buttonBack = view.findViewById(R.id.button_back_detail)
        textPetName = view.findViewById(R.id.textView_pet_name_detail)
        textGender = view.findViewById(R.id.textView_gender_detail)
        textPetBreed = view.findViewById(R.id.textView_pet_breed_detail)
        textPetAge = view.findViewById(R.id.textView_pet_age_detail)
        textPetDescription = view.findViewById(R.id.textView_pet_description_detail)
        buttonAdoptMe = view.findViewById(R.id.button_adopt_me)
        progressBar = view.findViewById(R.id.progressBar_detail)
    }

    private fun setupClickListeners() {
        buttonBack.setOnClickListener {
            findNavController().popBackStack()
        }
        buttonAdoptMe.setOnClickListener {
            try {
                val action = PetDetailFragmentDirections.actionPetDetailFragmentToSubmitRequestFragment(args.petId)
                findNavController().navigate(action)
            } catch (e: Exception) {
                Log.e("PetDetailFragment", "Navigasi ke AdoptionFormFragment gagal: ${e.message}")
            }
        }
    }

    private fun loadPetDetailsFromFirestore(petId: String) {
        setLoading(true) // Tampilkan loading

        // Mengambil satu dokumen dari koleksi 'pets' berdasarkan ID-nya
        db.collection("pets").document(petId).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    // Konversi dokumen Firestore ke objek CatModel
                    val pet = document.toObject(CatModel::class.java)
                    if (pet != null) {
                        // Jika berhasil, panggil fungsi untuk menampilkan data ke UI
                        displayPetData(pet)
                    } else {
                        handleDataNotFound()
                    }
                } else {
                    handleDataNotFound()
                }
                setLoading(false) // Sembunyikan loading setelah selesai
            }
            .addOnFailureListener { exception ->
                setLoading(false)
                Log.e("PetDetailFragment", "Gagal mengambil data: ", exception)
                Toast.makeText(context, "Gagal memuat detail hewan.", Toast.LENGTH_SHORT).show()
            }
    }

    // Fungsi baru untuk mengisi UI dengan data dari Firestore
    private fun displayPetData(pet: CatModel) {
        // Isi semua TextView dengan data
        textPetName.text = pet.name
        textPetBreed.text = pet.breed
        textPetAge.text = pet.age
        textPetDescription.text = pet.description

        // Atur gender dan warnanya
        if (pet.isFemale) {
            textGender.text = "♀"
            textGender.setTextColor(Color.parseColor("#F200FF"))
        } else {
            textGender.text = "♂"
            textGender.setTextColor(Color.parseColor("#FCA93F"))
        }
        textGender.visibility = View.VISIBLE

        // Setup slider gambar dengan daftar URL dari Firestore
        if (pet.imageUrls.isNotEmpty()) {
            val imageSliderAdapter = PetImageSliderAdapter(pet.imageUrls)
            viewPager.adapter = imageSliderAdapter
            viewPager.visibility = View.VISIBLE

            // Tampilkan indikator titik jika gambar lebih dari 1
            if (pet.imageUrls.size > 1) {
                tabLayout.visibility = View.VISIBLE
                TabLayoutMediator(tabLayout, viewPager) { _, _ -> }.attach()
            } else {
                tabLayout.visibility = View.GONE
            }
        } else {
            // Handle jika tidak ada gambar sama sekali
            viewPager.visibility = View.GONE
            tabLayout.visibility = View.GONE
        }
    }

    private fun handleDataNotFound() {
        Log.e("PetDetailFragment", "Dokumen hewan dengan ID '${args.petId}' tidak ditemukan.")
        Toast.makeText(context, "Data hewan tidak ditemukan.", Toast.LENGTH_SHORT).show()
        textPetName.text = "Tidak Ditemukan"
        // Sembunyikan view lain jika perlu
        textGender.visibility = View.GONE
        viewPager.visibility = View.GONE
    }

    private fun setLoading(isLoading: Boolean) {
        progressBar.isVisible = isLoading
    }




}