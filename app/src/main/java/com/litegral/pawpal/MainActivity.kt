package com.litegral.pawpal

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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
            R.id.adoptionFragment to R.id.adoptionFragment,
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
