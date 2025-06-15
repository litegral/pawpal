package com.litegral.pawpal.dhika

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.litegral.pawpal.JournalFragmentDirections
import com.litegral.pawpal.R
import java.text.SimpleDateFormat
import java.util.*

class JournalContentFragment : Fragment() {

    // Firebase
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private var firestoreListener: ListenerRegistration? = null

    // Views & Adapter
    private lateinit var adapter: JournalAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyState: View
    private lateinit var greetingText: TextView
    private lateinit var statTotal: TextView
    private lateinit var statMonthly: TextView
    private lateinit var statPhotos: TextView
    private lateinit var memoryContainer: LinearLayout
    private lateinit var memoryTitle: TextView
    private lateinit var memoryDivider: View

    private val journalList = mutableListOf<JournalEntry>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_journal_content, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        setupViews(view)
        setupRecyclerView()
        setupClickListeners(view)

        listenForJournalUpdates()
    }

    private fun setupViews(view: View) {
        recyclerView = view.findViewById(R.id.recycler_journal)
        emptyState = view.findViewById(R.id.empty_state_container)
        greetingText = view.findViewById(R.id.dashboard_greeting)
        statTotal = view.findViewById(R.id.stat_total)
        statMonthly = view.findViewById(R.id.stat_monthly)
        statPhotos = view.findViewById(R.id.stat_photos)
        memoryContainer = view.findViewById(R.id.memory_container)
        memoryTitle = view.findViewById(R.id.memory_title)
        memoryDivider = view.findViewById(R.id.divider_memory)
    }

    private fun setupRecyclerView() {
        // PERUBAHAN DI SINI: Implementasi navigasi saat item diklik
        adapter = JournalAdapter(journalList) { entry, _ ->
            // Membuat aksi navigasi dengan membawa data 'entry'
            val action = JournalFragmentDirections.actionJournalFragmentToDetailJournalFragment(entry)
            // Menjalankan navigasi. Menggunakan findNavController dari parent (JournalFragment)
            findNavController().navigate(action)
        }
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter
    }

    private fun setupClickListeners(view: View) {
        view.findViewById<Button>(R.id.btn_add_journal).setOnClickListener {
            val action = JournalFragmentDirections.actionJournalFragmentToAddJournalFragment()
            findNavController().navigate(action)
        }
    }

    private fun listenForJournalUpdates() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Log.w("JournalContent", "Pengguna tidak login.")
            updateUIForNoData()
            return
        }

        val query = db.collection("journals")
            .whereEqualTo("userId", userId)
            .orderBy("timestamp", Query.Direction.DESCENDING)

        firestoreListener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e("JournalContent", "Gagal mendengarkan data Firestore.", error)
                return@addSnapshotListener
            }

            if (snapshot != null) {
                val newEntries = snapshot.toObjects(JournalEntry::class.java)
                journalList.clear()
                journalList.addAll(newEntries)
                adapter.notifyDataSetChanged()

                checkEmptyState()
                updateJournalDashboard()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        firestoreListener?.remove()
    }

    private fun updateUIForNoData() {
        journalList.clear()
        adapter.notifyDataSetChanged()
        checkEmptyState()
        updateJournalDashboard()
    }

    private fun checkEmptyState() {
        emptyState.visibility = if (journalList.isEmpty()) View.VISIBLE else View.GONE
        recyclerView.visibility = if (journalList.isEmpty()) View.GONE else View.VISIBLE
    }

    private fun updateJournalDashboard() {
        val calendar = Calendar.getInstance()
        greetingText.text = when (calendar.get(Calendar.HOUR_OF_DAY)) {
            in 4..10 -> "Selamat Pagi!"
            in 11..14 -> "Selamat Siang!"
            in 15..17 -> "Selamat Sore!"
            else -> "Selamat Malam!"
        }

        statTotal.text = journalList.size.toString()
        statMonthly.text = journalList.count { entry ->
            try {
                val entryDate = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID")).parse(entry.date)
                val entryCal = Calendar.getInstance().apply { time = entryDate!! }
                entryCal.get(Calendar.MONTH) == calendar.get(Calendar.MONTH) &&
                        entryCal.get(Calendar.YEAR) == calendar.get(Calendar.YEAR)
            } catch (e: Exception) { false }
        }.toString()
        statPhotos.text = journalList.count { it.imageUrl.isNotEmpty() }.toString()

        val currentMonthDay = SimpleDateFormat("dd-MM", Locale("id", "ID")).format(calendar.time)
        val memoryEntry = journalList.find { entry ->
            try {
                val entryDate = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID")).parse(entry.date)
                val entryCal = Calendar.getInstance().apply { time = entryDate!! }
                SimpleDateFormat("dd-MM", Locale("id", "ID")).format(entryCal.time) == currentMonthDay &&
                        entryCal.get(Calendar.YEAR) < calendar.get(Calendar.YEAR)
            } catch (e: Exception) { false }
        }

        if (memoryEntry != null) {
            memoryTitle.text = "“${memoryEntry.title}”"
            memoryContainer.visibility = View.VISIBLE
            memoryDivider.visibility = View.VISIBLE
        } else {
            memoryContainer.visibility = View.GONE
            memoryDivider.visibility = View.GONE
        }
    }
}