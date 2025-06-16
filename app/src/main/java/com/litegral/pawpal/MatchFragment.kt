package com.litegral.pawpal

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MatchFragment : Fragment(R.layout.fragment_match) {

    private lateinit var progressBar: ProgressBar
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // The original button is removed from fragment_match.xml,
        // but if it were there, its logic is replaced by the check below.
        // We can add a progress bar to fragment_match.xml for better UX
        // progressBar = view.findViewById(R.id.progressBar)

        checkCatProfile()
    }

    private fun checkCatProfile() {
        // progressBar.visibility = View.VISIBLE
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Toast.makeText(requireContext(), "You need to be logged in.", Toast.LENGTH_SHORT).show()
            // Handle not logged in case, maybe navigate to login screen
            // progressBar.visibility = View.GONE
            return
        }

        db.collection("cats").document(userId).get()
            .addOnSuccessListener { document ->
                // progressBar.visibility = View.GONE
                if (document != null && document.exists()) {
                    // Profile exists, navigate to SwipeFragment
                    findNavController().navigate(R.id.action_matchFragment_to_swipeFragment)
                } else {
                    // Profile does not exist, navigate to EditCatProfileFragment
                    findNavController().navigate(R.id.action_matchFragment_to_editCatProfileFragment)
                }
            }
            .addOnFailureListener { exception ->
                // progressBar.visibility = View.GONE
                Toast.makeText(requireContext(), "Failed to check profile: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }
}