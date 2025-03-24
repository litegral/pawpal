package com.example.pawpal

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class CatForm : AppCompatActivity() {

    private lateinit var etProposal: EditText
    private lateinit var btnProposal: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cat_form)

        etProposal = findViewById(R.id.etProposal)
        btnProposal = findViewById(R.id.btnProposal)

        btnProposal.setOnClickListener {
            val proposalText = etProposal.text.toString().trim()

            if (proposalText.isEmpty()) {
                Toast.makeText(this, "Proposal tidak boleh kosong!", Toast.LENGTH_SHORT).show()
            } else {
                saveProposalToPrefs(proposalText)
                Toast.makeText(this, "Proposal disimpan!", Toast.LENGTH_SHORT).show()
                etProposal.setText("")
            }
        }
    }

    private fun saveProposalToPrefs(text: String) {
        val prefs = getSharedPreferences("MyProposals", MODE_PRIVATE)
        prefs.edit().putString("last_proposal", text).apply()
    }
}
