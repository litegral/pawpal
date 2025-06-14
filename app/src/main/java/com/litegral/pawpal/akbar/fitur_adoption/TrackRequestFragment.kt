package com.litegral.pawpal.akbar.fitur_adoption

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.litegral.pawpal.R
import com.litegral.pawpal.akbar.model.AdoptionRequest

class TrackRequestFragment : Fragment(R.layout.fragment_track_request) {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TrackRequestAdapter
    private lateinit var emptyStateTextView: TextView
    private lateinit var progressBar: ProgressBar

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Make sure to have a unique ID for this layout to avoid conflicts
        // e.g., R.layout.fragment_track_request
        recyclerView = view.findViewById(R.id.recyclerView_track_requests)
        emptyStateTextView = view.findViewById(R.id.emptyStateTextView_track)
        progressBar = view.findViewById(R.id.progressBar_track)
        view.findViewById<ImageButton>(R.id.button_back_track_requests).setOnClickListener {
            findNavController().popBackStack()
        }

        setupRecyclerView()
        loadRequests()
    }

    private fun setupRecyclerView() {
        adapter = TrackRequestAdapter(
            requireContext(),
            emptyList(),
            onEdit = { request ->
                // Navigate to Submit screen in "edit mode"
                val action = TrackRequestFragmentDirections.actionTrackRequestFragmentToSubmitRequestFragment(
                    petId = request.petId,
                    requestId = request.id // Pass request ID for editing
                )
                findNavController().navigate(action)
            },
            onDelete = { request ->
                showDeleteConfirmation(request)
            }
        )
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter
    }

    private fun loadRequests() {
        setLoading(true)
        val userId = auth.currentUser?.uid ?: return

        db.collection("adoptionRequests")
            .whereEqualTo("adopterId", userId)
            .orderBy("requestDate", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, e ->
                setLoading(false)
                if (e != null) {
                    Toast.makeText(context, "Error loading requests", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                if (snapshots == null || snapshots.isEmpty) {
                    emptyStateTextView.isVisible = true
                    recyclerView.isVisible = false
                } else {
                    val requests = snapshots.toObjects(AdoptionRequest::class.java)
                    adapter.updateData(requests)
                    emptyStateTextView.isVisible = false
                    recyclerView.isVisible = true
                }
            }
    }

    private fun showDeleteConfirmation(request: AdoptionRequest) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Request")
            .setMessage("Are you sure you want to delete this adoption request?")
            .setPositiveButton("Delete") { dialog, _ ->
                deleteRequest(request)
                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteRequest(request: AdoptionRequest) {
        setLoading(true)
        db.collection("adoptionRequests").document(request.id)
            .delete()
            .addOnSuccessListener {
                setLoading(false)
                Toast.makeText(context, "Request deleted", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                setLoading(false)
                Toast.makeText(context, "Error deleting request: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun setLoading(isLoading: Boolean) {
        progressBar.isVisible = isLoading
    }
}