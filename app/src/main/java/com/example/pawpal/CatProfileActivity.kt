package com.example.pawpal

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar

class CatProfileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cat_profile) // Replace with your actual layout file name

        // Set up toolbar with back button
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(true)

        toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Initialize views
        val btnContact = findViewById<Button>(R.id.btnContact)
        val btnAdopt = findViewById<Button>(R.id.btnAdopt)

        // Set click listeners for buttons
        btnContact.setOnClickListener {
            // Handle contact button click
            // Example: open email intent or show contact dialog
            showContactDialog()
        }

        btnAdopt.setOnClickListener {
            // Handle adopt button click
            // Example: show adoption form or confirmation
            showAdoptionConfirmation()
        }

        // You can also load data here if you're passing it from another activity
        loadCatData()
    }

    private fun showContactDialog() {
        // Implement your contact logic here
        // Example: show a dialog with contact options
        AlertDialog.Builder(this)
            .setTitle("Contact Owner")
            .setMessage("Choose contact method:")
            .setPositiveButton("Email") { _, _ ->
                // Open email intent
                val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                    data = Uri.parse("mailto:owner@example.com")
                    putExtra(Intent.EXTRA_SUBJECT, "Regarding Kago the cat")
                }
                startActivity(emailIntent)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showAdoptionConfirmation() {
        // Implement your adoption logic here
        // Example: show confirmation dialog
        AlertDialog.Builder(this)
            .setTitle("Adopt Kago")
            .setMessage("Are you sure you want to adopt this cat?")
            .setPositiveButton("Yes") { _, _ ->
                // Handle adoption confirmation
                val intent = Intent(this@CatProfileActivity, CatForm::class.java)
                startActivity(intent)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun loadCatData() {
        // If you're passing data from another activity, you can load it here
        /*
        val catName = intent.getStringExtra("CAT_NAME") ?: "Kago"
        val catLocation = intent.getStringExtra("CAT_LOCATION") ?: "Jembatan Suhat (2.5km)"
        
        findViewById<TextView>(R.id.catName).text = catName
        findViewById<TextView>(R.id.catLocation).text = catLocation
        */
    }
}