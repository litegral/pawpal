package com.litegral.pawpal.akbar.fitur_adoption

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import com.litegral.pawpal.R

class CatAdoptionHubFragment : Fragment(R.layout.fragment_adoption) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Navigate to the adopter's request tracking screen
        view.findViewById<MaterialButton>(R.id.button_cat_adopter).setOnClickListener {
            findNavController().navigate(R.id.action_catAdoptionHubFragment_to_trackRequestFragment)
        }

        // Navigate to the owner's pet list to see requests
        view.findViewById<MaterialButton>(R.id.button_cat_owner).setOnClickListener {
            // As per your design, this should go to the list of the owner's pets
            // PostingOpenAdoptHistoryFragment seems to be that screen
            findNavController().navigate(R.id.action_catAdoptionHubFragment_to_manageRequestFragment)
        }
    }
}