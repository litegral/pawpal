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
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.litegral.pawpal.R
import com.litegral.pawpal.akbar.model.AdoptionRequest

class RequestListFragment : Fragment(R.layout.fragment_request_list) {

    private val args: RequestListFragmentArgs by navArgs()
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: RequestAdapter
    private lateinit var emptyStateTextView: TextView
    private lateinit var progressBar: ProgressBar

    private val db = FirebaseFirestore.getInstance()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.recyclerView_requests)
        emptyStateTextView = view.findViewById(R.id.emptyStateTextView_requests)
        progressBar = view.findViewById(R.id.progressBar_requests)
        view.findViewById<ImageButton>(R.id.button_back_requests).setOnClickListener {
            findNavController().popBackStack()
        }

        setupRecyclerView()
        loadRequests()
    }

    private fun setupRecyclerView() {
        adapter = RequestAdapter(
            requireContext(),
            emptyList(),
            onStatusChangedClicked = { request ->
                showStatusChangeDialog(request)
            },
            onViewPhotoClicked = { imageUrl ->
                // You can implement a dialog to show the full image here
                Toast.makeText(context, "Show full image: $imageUrl", Toast.LENGTH_SHORT).show()
            }
        )
        recyclerView.adapter = adapter
    }

    private fun loadRequests() {
        setLoading(true)
        db.collection("adoptionRequests")
            .whereEqualTo("petId", args.petId)
            .orderBy("requestDate", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, e ->
                setLoading(false)
                if (e != null) {
                    Toast.makeText(context, "Error loading requests", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                val requests = snapshots?.toObjects(AdoptionRequest::class.java) ?: emptyList()
                adapter.updateData(requests)

                emptyStateTextView.isVisible = requests.isEmpty()
                recyclerView.isVisible = requests.isNotEmpty()
            }
    }

    private fun showStatusChangeDialog(request: AdoptionRequest) {
        val statuses = arrayOf("Pending", "Accepted", "Declined")
        AlertDialog.Builder(requireContext())
            .setTitle("Change Request Status")
            .setItems(statuses) { dialog, which ->
                val newStatus = statuses[which]
                updateRequestStatus(request, newStatus)
                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun updateRequestStatus(request: AdoptionRequest, newStatus: String) {
        setLoading(true)
        db.collection("adoptionRequests").document(request.id)
            .update("status", newStatus)
            .addOnSuccessListener {
                setLoading(false)
                Toast.makeText(context, "Status updated to $newStatus", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                setLoading(false)
                Toast.makeText(context, "Failed to update status", Toast.LENGTH_SHORT).show()
            }
    }

    private fun setLoading(isLoading: Boolean) {
        progressBar.isVisible = isLoading
    }
}