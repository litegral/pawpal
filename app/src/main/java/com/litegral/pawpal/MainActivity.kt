package com.litegral.pawpal

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()

        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        if (currentUser == null) {
            // No user is signed in, redirect to SignInActivity
            val intent = Intent(this, SignInActivity::class.java)
            startActivity(intent)
            finish() // Finish MainActivity so the user can't go back to it without signing in
            return // Return early to prevent further execution in this activity
        }

        // Find the NavHostFragment and NavController
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        // Set up BottomNavigationView with NavController
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNav.setupWithNavController(navController)

        // Map fragment destinations to bottom navigation items
        val destinationToMenuItemMap = mapOf(
            R.id.homeFragment to R.id.homeFragment,
            R.id.catAdoptionHubFragment to R.id.catAdoptionHubFragment,
            R.id.journalFragment to R.id.journalFragment,
            R.id.matchFragment to R.id.matchFragment,
            R.id.swipeFragment to R.id.matchFragment // Map resultFragment to matchFragment
        )

        // Listen for navigation destination changes
        navController.addOnDestinationChangedListener { _, destination, _ ->
            val mappedDestinationId = destinationToMenuItemMap[destination.id]

            // Check and set the corresponding item as selected
            if (mappedDestinationId != null) {
                bottomNav.menu.findItem(mappedDestinationId)?.isChecked = true
                bottomNav.visibility = View.VISIBLE
            } else {
                bottomNav.visibility = View.GONE
            }
        }
    }
}
