package com.litegral.pawpal.akbar.fitur_ListOpenAdoptOfUser

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.litegral.pawpal.R
import com.litegral.pawpal.akbar.fitur_ListOpenAdoptOfUser.adapter.PostingOpenAdoptHistoryAdapter
import com.litegral.pawpal.akbar.model.CatModel

class PostingOpenAdoptHistoryFragment : Fragment() {

    private lateinit var recyclerViewHistory: RecyclerView
    private lateinit var historyAdapter: PostingOpenAdoptHistoryAdapter
    private lateinit var fabAddFromHistory: FloatingActionButton
    private lateinit var buttonBackHistory: ImageButton
    private lateinit var progressBar: ProgressBar

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private val postList = mutableListOf<CatModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.fragment_posting_open_adopt_history, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()


        initViews(view)
        setupRecyclerView()
        setupClickListeners()

        // LOAD DATA FIREBASE
        loadUserPosts()
    }

    private fun initViews(view: View) {
        recyclerViewHistory = view.findViewById(R.id.recyclerView_posting_history)
        fabAddFromHistory = view.findViewById(R.id.fab_add_from_history)
        buttonBackHistory = view.findViewById(R.id.button_back_history)
        progressBar = view.findViewById(R.id.progressBar_history)
    }

    private fun setupRecyclerView() {
        historyAdapter = PostingOpenAdoptHistoryAdapter(
            postList,
            onEditClicked = { selectedPost ->
                // EDIT LOGIC
                if (selectedPost.id.isNotBlank()) {
                    val action = PostingOpenAdoptHistoryFragmentDirections.actionPostingOpenAdoptHistoryFragmentToUpdatePostFragment(selectedPost.id)
                    findNavController().navigate(action)
                } else {
                    Log.e("PostingHistory", "ID Hewan kosong, navigasi edit dibatalkan.")
                }
            },
            onViewRequestsClicked = { selectedPost ->
                if (selectedPost.id.isNotBlank()) {
                    val action = PostingOpenAdoptHistoryFragmentDirections.actionPostingOpenAdoptHistoryFragmentToRequestListFragment(selectedPost.id)
                    findNavController().navigate(action)
                } else {
                    Log.e("PostingHistory", "ID Hewan kosong, navigasi view requests dibatalkan.")
                }
            }
        )
        recyclerViewHistory.layoutManager = LinearLayoutManager(context)
        recyclerViewHistory.adapter = historyAdapter
    }

    // HANDLE UI
    private fun setupClickListeners() {
        buttonBackHistory.setOnClickListener {
            findNavController().popBackStack()
        }

        //LOGIC UNTUK NAVIGASI KE HALAMAN CREATE ADOPT POST UNTUK TOMBOL HIJAU
        fabAddFromHistory.setOnClickListener {
            findNavController().navigate(R.id.action_postingOpenAdoptHistoryFragment_to_createAdoptPostFragment)
        }
    }

    private fun loadUserPosts() {
        progressBar.isVisible = true
        val currentUserId = auth.currentUser?.uid
        if (currentUserId == null) {
            Toast.makeText(context, "Anda harus login untuk melihat riwayat.", Toast.LENGTH_SHORT).show()
            progressBar.isVisible = false
            return
        }

        // Ambil semua dokumen dari koleksi 'pets' yang field 'uploaderUid'-nya sama dengan ID pengguna saat ini
        db.collection("pets")
            .whereEqualTo("uploaderUid", currentUserId)
            .orderBy("postedDate", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                postList.clear()
                for (document in documents) {
                    // Mengubah dokumen menjadi objek CatModel
                    val cat = document.toObject(CatModel::class.java)
                    cat.id = document.id
                    postList.add(cat)
                }
                historyAdapter.notifyDataSetChanged()

                if (postList.isEmpty()) {
                    Toast.makeText(context, "Anda belum memiliki postingan.", Toast.LENGTH_SHORT).show()
                }
                progressBar.isVisible = false
            }
            .addOnFailureListener { exception ->
                progressBar.isVisible = false
                Log.w("PostingHistory", "Error getting documents: ", exception)
                Toast.makeText(context, "Gagal memuat riwayat.", Toast.LENGTH_SHORT).show()
            }
    }
}