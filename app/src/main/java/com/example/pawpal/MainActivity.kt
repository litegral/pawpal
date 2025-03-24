package com.example.pawpal

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Setup window insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Setup RecyclerView
        setupCatsList()
    }

    private fun setupCatsList() {
        val recyclerView = findViewById<RecyclerView>(R.id.cats_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Create sample data
        val catsList = createSampleCats()

        // Set adapter
        val adapter = CatAdapter(catsList)
        recyclerView.adapter = adapter
    }

    private fun createSampleCats(): List<Cat> {
        // Create some sample cats for testing
        // Replace R.drawable.cat_placeholder with your actual drawable resources
        return listOf(
            Cat(
                name = "Milo",
                age = "1 tahun",
                gender = "Jantan",
                location = "Jakarta Selatan",
                status = "Lihat",
                imageResId = R.drawable.cat_placeholder
            ),
            Cat(
                name = "Luna",
                age = "8 bulan",
                gender = "Betina",
                location = "Jakarta Pusat",
                status = "Lihat",
                imageResId = R.drawable.cat_placeholder
            ),
            Cat(
                name = "Oliver",
                age = "2 tahun",
                gender = "Jantan",
                location = "Bandung",
                status = "Lihat",
                imageResId = R.drawable.cat_placeholder
            ),
            Cat(
                name = "Bella",
                age = "1.5 tahun",
                gender = "Betina",
                location = "Surabaya",
                status = "Lihat",
                imageResId = R.drawable.cat_placeholder
            ),
            Cat(
                name = "Max",
                age = "3 tahun",
                gender = "Jantan",
                location = "Jakarta Barat",
                status = "Lihat",
                imageResId = R.drawable.cat_placeholder
            )
        )
    }
}
