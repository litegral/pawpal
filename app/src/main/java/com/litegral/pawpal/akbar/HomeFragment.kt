// Pastikan package ini sesuai dengan lokasi file Anda
package com.litegral.pawpal.akbar

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.litegral.pawpal.SignInActivity
import com.litegral.pawpal.R
class HomeFragment : Fragment() {

    // Views
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var btnLogout: MaterialButton
    private lateinit var fabAddNewPost: FloatingActionButton

    // Data & Adapter
    private lateinit var homepageAdapter: HomepageAdapter
    private var petList: MutableList<CatModel> = mutableListOf() // Hanya butuh satu list

    // Firebase
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Listener ini tetap berguna jika ada postingan baru, untuk me-refresh daftar
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

    private fun initViews(view: View) {
        recyclerView = view.findViewById(R.id.recyclerView)
        progressBar = view.findViewById(R.id.progressBar_home)
        btnLogout = view.findViewById(R.id.button_logout_home)
        fabAddNewPost = view.findViewById(R.id.fab_add_new_post)
    }

    private fun setupClickListeners() {
        btnLogout.setOnClickListener { performLogout() }
        fabAddNewPost.setOnClickListener { navigateToHistory() }
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = GridLayoutManager(context, 2)
        homepageAdapter = HomepageAdapter(petList) { selectedPet ->
            Log.d("HomeFragment", "Navigating with pet ID: ${selectedPet.id}")
            try {
                val action = HomeFragmentDirections.actionHomeFragmentToPetDetailFragment(selectedPet.id)
                findNavController().navigate(action)
            } catch (e: Exception) {
                Log.e("HomeFragment", "Navigasi ke detail gagal: ${e.message}")
            }
        }
        recyclerView.adapter = homepageAdapter
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
                // Langsung update adapter dengan data penuh
                homepageAdapter.updateData(petList)
                progressBar.isVisible = false
            }
            .addOnFailureListener { exception ->
                progressBar.isVisible = false
                Log.w("HomeFragment", "Error getting documents: ", exception)
                Toast.makeText(context, "Gagal memuat data.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun navigateToHistory() {
        try {
            findNavController().navigate(R.id.action_homeFragment_to_postingOpenAdoptHistoryFragment)
        } catch (e: Exception) {
            Log.e("HomeFragment", "Navigasi ke halaman riwayat gagal: ${e.message}")
        }
    }

    private fun performLogout() {
        auth.signOut()
        Toast.makeText(context, "Anda telah logout", Toast.LENGTH_SHORT).show()
        val intent = Intent(activity, SignInActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        activity?.finish()
    }
}