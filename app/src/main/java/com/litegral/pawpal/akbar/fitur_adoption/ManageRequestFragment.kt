package com.litegral.pawpal.akbar.fitur_adoption

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.litegral.pawpal.R
import com.litegral.pawpal.akbar.model.AdoptionRequest
import com.litegral.pawpal.akbar.model.CatModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class ManageRequestFragment : Fragment(R.layout.fragment_manage_request) {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: OwnerPetListingAdapter
    private lateinit var progressBar: ProgressBar
    private lateinit var totalRequestsTextView: TextView
    private lateinit var pendingCountTextView: TextView

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.recyclerView_owner_pets)
        progressBar = view.findViewById(R.id.progressBar_manage)
        totalRequestsTextView = view.findViewById(R.id.textView_total_requests)
        pendingCountTextView = view.findViewById(R.id.textView_pending_count)

        view.findViewById<View>(R.id.button_back_manage).setOnClickListener {
            findNavController().popBackStack()
        }

        setupRecyclerView()
        loadPetsAndRequests()
    }

    private fun setupRecyclerView() {
        adapter = OwnerPetListingAdapter(emptyList()) { petWithRequests ->
            // Navigate to the list of requests for this pet
            val action = ManageRequestFragmentDirections.actionManageRequestFragmentToRequestListFragment(petWithRequests.pet.id)
            findNavController().navigate(action)
        }
        recyclerView.adapter = adapter
    }

    private fun loadPetsAndRequests() {
        progressBar.isVisible = true
        val userId = auth.currentUser?.uid ?: return

        // Using Coroutines to handle asynchronous calls cleanly
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // 1. Fetch all pets owned by the current user
                val petsQuery = db.collection("pets")
                    .whereEqualTo("uploaderUid", userId)
                    .orderBy("postedDate", Query.Direction.DESCENDING)
                    .get().await()
                val pets = petsQuery.toObjects(CatModel::class.java)

                // 2. Fetch all requests for those pets
                val petIds = pets.map { it.id }
                val allRequests = if (petIds.isNotEmpty()) {
                    db.collection("adoptionRequests")
                        .whereIn("petId", petIds)
                        .get().await().toObjects(AdoptionRequest::class.java)
                } else {
                    emptyList()
                }

                // 3. Group requests by petId
                val requestsByPetId = allRequests.groupBy { it.petId }

                // 4. Create the final list for the adapter
                val petsWithRequests = pets.map { pet ->
                    val requestsForPet = requestsByPetId[pet.id] ?: emptyList()
                    OwnerPetWithRequests(pet, requestsForPet)
                }

                // 5. Update UI on the main thread
                withContext(Dispatchers.Main) {
                    adapter.updateData(petsWithRequests)
                    updateSummary(allRequests)
                    progressBar.isVisible = false
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    progressBar.isVisible = false
                    Toast.makeText(context, "Failed to load data: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun updateSummary(allRequests: List<AdoptionRequest>) {
        val total = allRequests.size
        val pending = allRequests.count { it.status == "Pending" }
        totalRequestsTextView.text = "Total Request : $total"
        pendingCountTextView.text = "Pending Count : $pending"
    }
}

// Helper data class for the adapter
data class OwnerPetWithRequests(val pet: CatModel, val requests: List<AdoptionRequest>)