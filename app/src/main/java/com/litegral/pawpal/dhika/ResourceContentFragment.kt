package com.litegral.pawpal.dhika

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.litegral.pawpal.JournalFragmentDirections
import com.litegral.pawpal.R

class ResourceContentFragment : Fragment() {

    // Firebase
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private var firestoreListener: ListenerRegistration? = null

    private lateinit var adapter: ResourceAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyState: View

    private lateinit var btnAll: Button
    private lateinit var btnNutrition: Button
    private lateinit var btnGrooming: Button
    private lateinit var btnHealth: Button
    private lateinit var filterButtons: List<Button>

    private val masterResourceEntries = mutableListOf<ResourceEntry>()
    private val displayedResourceEntries = mutableListOf<ResourceEntry>()

    private var currentFilterTag: String = "All"

    companion object {
        fun newInstance() = ResourceContentFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_resource_content, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        recyclerView = view.findViewById(R.id.recycler_resource)
        emptyState = view.findViewById(R.id.empty_state_container)

        // MODIFIKASI: Implementasi klik item untuk navigasi ke detail
        adapter = ResourceAdapter(displayedResourceEntries) { entry, position ->
            try {
                // Buat aksi navigasi dan kirim objek ResourceEntry serta posisinya
                val action = JournalFragmentDirections.actionJournalFragmentToDetailResourceFragment(entry, position)
                findNavController().navigate(action)
            } catch (e: Exception) {
                Log.e("ResourceContent", "Navigasi ke DetailResourceFragment gagal: ${e.message}")
            }
        }
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        view.findViewById<Button>(R.id.btn_add_resource).setOnClickListener {
            try {
                findNavController().navigate(R.id.action_journalFragment_to_addResourceFragment)
            } catch (e: Exception) {
                Log.e("ResourceContent", "Navigasi ke AddResourceFragment gagal: ${e.message}")
            }
        }

        setupFilterButtons(view)
        listenForResourceUpdates()
    }

    private fun setupFilterButtons(view: View) {
        btnAll = view.findViewById(R.id.btn_filter_all)
        btnNutrition = view.findViewById(R.id.btn_filter_nutrition)
        btnGrooming = view.findViewById(R.id.btn_filter_grooming)
        btnHealth = view.findViewById(R.id.btn_filter_health)
        filterButtons = listOf(btnAll, btnNutrition, btnGrooming, btnHealth)

        btnAll.setOnClickListener {
            currentFilterTag = "All"
            applyFilter()
        }
        btnNutrition.setOnClickListener {
            currentFilterTag = "#Nutrition"
            applyFilter()
        }
        btnGrooming.setOnClickListener {
            currentFilterTag = "#Grooming"
            applyFilter()
        }
        btnHealth.setOnClickListener {
            currentFilterTag = "#Health"
            applyFilter()
        }
    }

    private fun listenForResourceUpdates() {
        // HAPUS filter .whereEqualTo("userId", userId) untuk menampilkan semua resource.
        val query = db.collection("resources")
            .orderBy("timestamp", Query.Direction.DESCENDING) // Urutkan dari yang terbaru

        firestoreListener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e("ResourceContent", "Gagal mendengarkan data Firestore.", error)
                return@addSnapshotListener
            }

            if (snapshot != null) {
                val newEntries = snapshot.toObjects(ResourceEntry::class.java)
                masterResourceEntries.clear()
                masterResourceEntries.addAll(newEntries)
                applyFilter() // Terapkan filter setelah data dimuat
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        firestoreListener?.remove()
    }

    private fun applyFilter() {
        val filteredList = if (currentFilterTag == "All") {
            masterResourceEntries
        } else {
            masterResourceEntries.filter { it.tag.equals(currentFilterTag, ignoreCase = true) }
        }

        displayedResourceEntries.clear()
        displayedResourceEntries.addAll(filteredList)
        adapter.notifyDataSetChanged()
        updateButtonUI()
        checkEmptyState()
    }

    private fun updateButtonUI() {
        val selectedButton = when (currentFilterTag) {
            "#Nutrition" -> btnNutrition
            "#Grooming" -> btnGrooming
            "#Health" -> btnHealth
            else -> btnAll
        }

        filterButtons.forEach { button ->
            if (button == selectedButton) {
                button.setBackgroundResource(R.drawable.tab_selected)
                button.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white))
            } else {
                button.setBackgroundResource(R.drawable.tab_unselected)
                button.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.black))
            }
        }
    }

    private fun checkEmptyState() {
        emptyState.visibility = if (displayedResourceEntries.isEmpty()) View.VISIBLE else View.GONE
        recyclerView.visibility = if (displayedResourceEntries.isEmpty()) View.GONE else View.VISIBLE
    }

    private fun updateUIForNoData() {
        masterResourceEntries.clear()
        displayedResourceEntries.clear()
        adapter.notifyDataSetChanged()
        checkEmptyState()
    }
}