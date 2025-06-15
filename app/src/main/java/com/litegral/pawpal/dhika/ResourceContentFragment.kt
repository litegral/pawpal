package com.litegral.pawpal.dhika

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.litegral.pawpal.R

class ResourceContentFragment : Fragment() {

    private lateinit var adapter: ResourceAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyState: View

    private lateinit var btnAll: Button
    private lateinit var btnNutrition: Button
    private lateinit var btnGrooming: Button
    private lateinit var btnHealth: Button
    private lateinit var filterButtons: List<Button>

    private val masterResourceEntries = mutableListOf<ResourceEntry>()
    private val displayedResourceEntries = mutableListOf<ResourceEntry>()

    private var currentFilterTag: String = "All"

    companion object {
        fun newInstance() = ResourceContentFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_resource_content, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.recycler_resource)
        emptyState = view.findViewById(R.id.empty_state_container)

        adapter = ResourceAdapter(displayedResourceEntries) { entry, _ ->
            // Navigasi ke detail
        }
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        view.findViewById<Button>(R.id.btn_add_resource).setOnClickListener {
            // Logika untuk navigasi ke halaman tambah resource
        }

        setupFilterButtons(view)
        applyFilter()
    }

    private fun setupFilterButtons(view: View) {
        btnAll = view.findViewById(R.id.btn_filter_all)
        btnNutrition = view.findViewById(R.id.btn_filter_nutrition)
        btnGrooming = view.findViewById(R.id.btn_filter_grooming)
        btnHealth = view.findViewById(R.id.btn_filter_health)
        filterButtons = listOf(btnAll, btnNutrition, btnGrooming, btnHealth)

        btnAll.setOnClickListener {
            currentFilterTag = "All"
            applyFilter()
        }
        btnNutrition.setOnClickListener {
            currentFilterTag = "#Nutrition"
            applyFilter()
        }
        btnGrooming.setOnClickListener {
            currentFilterTag = "#Grooming"
            applyFilter()
        }
        btnHealth.setOnClickListener {
            currentFilterTag = "#Health"
            applyFilter()
        }
    }

    private fun applyFilter() {
        val filteredList = if (currentFilterTag == "All") {
            masterResourceEntries
        } else {
            masterResourceEntries.filter { it.tag.equals(currentFilterTag, ignoreCase = true) }
        }

        displayedResourceEntries.clear()
        displayedResourceEntries.addAll(filteredList)
        adapter.notifyDataSetChanged()
        updateButtonUI()
        checkEmptyState()
    }

    private fun updateButtonUI() {
        val selectedButton = when (currentFilterTag) {
            "#Nutrition" -> btnNutrition
            "#Grooming" -> btnGrooming
            "#Health" -> btnHealth
            else -> btnAll
        }

        filterButtons.forEach { button ->
            if (button == selectedButton) {
                button.setBackgroundResource(R.drawable.tab_selected)
                button.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white))
            } else {
                button.setBackgroundResource(R.drawable.tab_unselected)
                // Pastikan Anda memiliki warna hitam atau warna default di colors.xml
                button.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.black))
            }
        }
    }

    private fun checkEmptyState() {
        if (displayedResourceEntries.isEmpty()) {
            recyclerView.visibility = View.GONE
            emptyState.visibility = View.VISIBLE
        } else {
            recyclerView.visibility = View.VISIBLE
            emptyState.visibility = View.GONE
        }
    }
}