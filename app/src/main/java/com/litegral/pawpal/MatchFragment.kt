package com.litegral.pawpal

import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController

class MatchFragment : Fragment(R.layout.fragment_match) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnSave = view.findViewById<Button>(R.id.btnSave)
        btnSave.setOnClickListener {
            findNavController().navigate(R.id.action_matchFragment_to_swipeFragment)
        }
    }
}