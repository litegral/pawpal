package com.litegral.pawpal

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class HistoryFragment : Fragment(R.layout.fragment_history) {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: HistoryAdapter
    private lateinit var petRepository: PetRepository
    private lateinit var emptyStateTextView: TextView
    private lateinit var backButton: ImageButton

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        petRepository = PetRepository.getInstance(requireContext())

        recyclerView = view.findViewById(R.id.historyRecyclerView)
        emptyStateTextView = view.findViewById(R.id.emptyStateTextView)
        backButton = view.findViewById(R.id.backButton)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        adapter = HistoryAdapter(requireContext())
        recyclerView.adapter = adapter

        backButton.setOnClickListener {
            findNavController().navigate(R.id.action_historyFragment_to_swipeFragment)
        }

        loadLikedPets()
    }

    override fun onResume() {
        super.onResume()
        // Reload data when fragment becomes visible again
        loadLikedPets()
    }

    private fun loadLikedPets() {
        val likedPets = petRepository.getLikedPets()
        adapter.setItems(likedPets)

        // Show empty state message if no pets are liked
        if (likedPets.isEmpty()) {
            emptyStateTextView.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        } else {
            emptyStateTextView.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
        }
    }
}