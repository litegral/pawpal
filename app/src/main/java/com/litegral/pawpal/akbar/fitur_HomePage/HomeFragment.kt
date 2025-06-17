
package com.litegral.pawpal.akbar.fitur_HomePage

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.litegral.pawpal.R
import com.litegral.pawpal.SignInActivity
import com.litegral.pawpal.akbar.fitur_HomePage.adapter.HomepageAdapter
import com.litegral.pawpal.akbar.model.CatModel
import de.hdodenhof.circleimageview.CircleImageView

class HomeFragment : Fragment(), View.OnClickListener {

    // Views
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var fabAddNewPost: FloatingActionButton
    private lateinit var profileNameTextView: TextView
    private lateinit var profileImageView: CircleImageView

    // Data & Adapter
    private lateinit var homepageAdapter: HomepageAdapter
    private var petList: MutableList<CatModel> = mutableListOf()

    // Firebase
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Listener untuk menerima data dari CreateAdoptPostFragment
        setFragmentResultListener("newPetPostRequest") { _, _ ->
            Toast.makeText(context, "Memperbarui daftar...", Toast.LENGTH_SHORT).show()
            loadPetsFromFirestore()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        initViews(view)
        setupClickListeners()
        setupRecyclerView()


        loadPetsFromFirestore()
    }

    // Panggil `loadUserProfile` di onResume agar data selalu terbaru saat kembali ke halaman ini
    override fun onResume() {
        super.onResume()
        loadUserProfile()
    }

    private fun initViews(view: View) {
        recyclerView = view.findViewById(R.id.recyclerView)
        progressBar = view.findViewById(R.id.progressBar_home)
        fabAddNewPost = view.findViewById(R.id.fab_add_new_post)
        profileNameTextView = view.findViewById(R.id.profile_name_home_top)
        profileImageView = view.findViewById(R.id.profile_image_home_top)
    }

    private fun setupClickListeners() {

        fabAddNewPost.setOnClickListener(this)
        profileImageView.setOnClickListener(this)
        profileNameTextView.setOnClickListener(this)
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = GridLayoutManager(context, 2)
        homepageAdapter = HomepageAdapter(petList) { selectedPet ->
            Log.d("HomeFragment", "Navigating with pet ID: ${selectedPet.id}")
            try {
                // Navigasi ke detail hewan dengan mengirim ID
                val action = HomeFragmentDirections.actionHomeFragmentToPetDetailFragment(selectedPet.id)
                findNavController().navigate(action)
            } catch (e: Exception) {
                Log.e("HomeFragment", "Navigasi ke detail gagal: ${e.message}")
            }
        }
        recyclerView.adapter = homepageAdapter
    }

    // FUNCTION UNTUK MENGAMBIL DAN MENAMPILKAN PROFIL PENGGUNA ---
    private fun loadUserProfile() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Log.w("HomeFragment", "Tidak ada pengguna yang login, tidak bisa memuat profil.")
            return
        }

        // Ambil dokumen pengguna dari koleksi 'users' berdasarkan UID
        db.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val name = document.getString("displayName")
                    val imageUrl = document.getString("profilePhotoUrl")

                    // Tampilkan nama USER
                    profileNameTextView.text = name ?: "Pengguna"

                    // Cek url
                    if (!imageUrl.isNullOrEmpty()) {
                        Glide.with(this)
                            .load(imageUrl)
                            .placeholder(R.drawable.ic_profile_placeholder)
                            .error(R.drawable.ic_profile_placeholder)
                            .into(profileImageView)
                    } else {
                        profileImageView.setImageResource(R.drawable.ic_profile_placeholder)
                    }
                } else {

                    Log.w("HomeFragment", "Dokumen profil tidak ditemukan untuk user: $userId")
                    profileNameTextView.text = auth.currentUser?.displayName ?: "Pengguna"
                }
            }
            .addOnFailureListener { exception ->
                Log.e("HomeFragment", "Gagal mengambil data profil dari Firestore", exception)
                profileNameTextView.text = auth.currentUser?.displayName ?: "Pengguna"
            }
    }

    private fun loadPetsFromFirestore() {
        progressBar.isVisible = true

        db.collection("pets")
            .orderBy("postedDate", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                petList.clear()
                for (document in documents) {
                    val pet = document.toObject(CatModel::class.java).apply {
                        id = document.id
                    }
                    petList.add(pet)
                }
                homepageAdapter.updateData(petList)
                progressBar.isVisible = false
            }
            .addOnFailureListener { exception ->
                progressBar.isVisible = false
                Log.w("HomeFragment", "Error getting documents: ", exception)
            }
    }

    override fun onClick(v: View?) {
        when (v?.id) {

            R.id.fab_add_new_post -> navigateToHistory()
            R.id.profile_image_home_top, R.id.profile_name_home_top -> navigateToProfile()
        }
    }

    private fun navigateToProfile() {
        try {
            findNavController().navigate(R.id.action_homeFragment_to_profileFragment)
        } catch (e: Exception) {
            Log.e("HomeFragment", "Navigasi ke ProfileFragment gagal: ${e.message}")
        }
    }

    private fun navigateToHistory() {
        try {
            findNavController().navigate(R.id.action_homeFragment_to_postingOpenAdoptHistoryFragment)
        } catch (e: Exception) {
            Log.e("HomeFragment", "Navigasi ke halaman riwayat gagal: ${e.message}")
        }
    }


}