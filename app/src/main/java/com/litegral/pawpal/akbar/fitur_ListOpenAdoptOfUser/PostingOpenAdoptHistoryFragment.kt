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
        // Inflate layout untuk fragment ini
        return inflater.inflate(R.layout.fragment_posting_open_adopt_history, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inisialisasi Firebase
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Inisialisasi Views
        initViews(view)
        setupRecyclerView()
        setupClickListeners()

        // Muat data dari Firestore
        loadUserPosts()
    }

    private fun initViews(view: View) {
        recyclerViewHistory = view.findViewById(R.id.recyclerView_posting_history)
        fabAddFromHistory = view.findViewById(R.id.fab_add_from_history)
        buttonBackHistory = view.findViewById(R.id.button_back_history)
        progressBar = view.findViewById(R.id.progressBar_history) // Pastikan ID ini ada di XML
    }

    private fun setupRecyclerView() {
        // Inisialisasi adapter dengan list kosong, lalu berikan fungsi untuk menangani klik
        historyAdapter = PostingOpenAdoptHistoryAdapter(postList) { selectedPost ->
            Log.d("PostingHistory", "Item diklik: ${selectedPost.name} dengan ID: ${selectedPost.id}")
            if (selectedPost.id.isNotBlank()) {
                try {
                    // Navigasi ke halaman Update dengan mengirim petId
                    val action = PostingOpenAdoptHistoryFragmentDirections.actionPostingOpenAdoptHistoryFragmentToUpdatePostFragment(selectedPost.id)
                    findNavController().navigate(action)
                } catch (e: Exception) {
                    Log.e("PostingHistory", "Navigasi ke UpdatePostFragment gagal: ${e.message}")
                    Toast.makeText(context, "Gagal membuka halaman edit.", Toast.LENGTH_SHORT).show()
                }
            } else {
                Log.e("PostingHistory", "ID Hewan kosong, navigasi dibatalkan.")
            }
        }
        recyclerViewHistory.layoutManager = LinearLayoutManager(context)
        recyclerViewHistory.adapter = historyAdapter
    }

    private fun setupClickListeners() {
        buttonBackHistory.setOnClickListener {
            findNavController().popBackStack()
        }
        fabAddFromHistory.setOnClickListener {
            // Navigasi ke halaman form untuk membuat postingan baru
            findNavController().navigate(R.id.action_postingOpenAdoptHistoryFragment_to_createAdoptPostFragment)
        }
    }

    private fun loadUserPosts() {
        progressBar.isVisible = true // Tampilkan loading
        val currentUserId = auth.currentUser?.uid
        if (currentUserId == null) {
            Toast.makeText(context, "Anda harus login untuk melihat riwayat.", Toast.LENGTH_SHORT).show()
            progressBar.isVisible = false
            return
        }

        // Ambil semua dokumen dari koleksi 'pets' yang field 'uploaderUid'-nya sama dengan ID pengguna saat ini
        db.collection("pets")
            .whereEqualTo("uploaderUid", currentUserId)
            .orderBy("postedDate", Query.Direction.DESCENDING) // Urutkan dari yang terbaru
            .get()
            .addOnSuccessListener { documents ->
                postList.clear()
                for (document in documents) {
                    // Mengubah dokumen menjadi objek CatModel
                    val cat = document.toObject(CatModel::class.java)
                    // Set ID objek `cat` dari ID dokumen Firestore agar bisa dikirim saat navigasi
                    cat.id = document.id
                    postList.add(cat)
                }
                // Beri tahu adapter bahwa data telah berubah
                historyAdapter.notifyDataSetChanged()

                if (postList.isEmpty()) {
                    Toast.makeText(context, "Anda belum memiliki postingan.", Toast.LENGTH_SHORT).show()
                }
                progressBar.isVisible = false // Sembunyikan loading
            }
            .addOnFailureListener { exception ->
                progressBar.isVisible = false // Sembunyikan loading jika gagal
                Log.w("PostingHistory", "Error getting documents: ", exception)
                Toast.makeText(context, "Gagal memuat riwayat.", Toast.LENGTH_SHORT).show()
            }
    }
}