package com.litegral.pawpal.dhika

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.litegral.pawpal.JournalFragment
import com.litegral.pawpal.R
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class JournalContentFragment : Fragment() {

    private lateinit var adapter: JournalAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyState: View

    // Deklarasi View untuk Dasbor
    private lateinit var greetingText: TextView
    private lateinit var statTotal: TextView
    private lateinit var statMonthly: TextView
    private lateinit var statPhotos: TextView
    private lateinit var memoryContainer: LinearLayout
    private lateinit var memoryTitle: TextView
    private lateinit var memoryDivider: View

    private val journalEntries = mutableListOf<JournalEntry>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupFragmentResultListeners()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // ==================================================================
        // INILAH BAGIAN YANG DIPERBAIKI
        // Menggunakan layout spesifik untuk konten jurnal, bukan layout utama.
        return inflater.inflate(R.layout.fragment_journal_content, container, false)
        // ==================================================================
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inisialisasi View
        recyclerView = view.findViewById(R.id.recycler_journal)
        emptyState = view.findViewById(R.id.empty_state_container)
        greetingText = view.findViewById(R.id.dashboard_greeting)
        statTotal = view.findViewById(R.id.stat_total)
        statMonthly = view.findViewById(R.id.stat_monthly)
        statPhotos = view.findViewById(R.id.stat_photos)
        memoryContainer = view.findViewById(R.id.memory_container)
        memoryTitle = view.findViewById(R.id.memory_title)
        memoryDivider = view.findViewById(R.id.divider_memory)

        adapter = JournalAdapter(journalEntries) { entry, position ->
            (parentFragment as? JournalFragment)?.navigateTo(DetailJournalFragment.newInstance(entry, position))
        }
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        updateJournalDashboard()
        checkEmptyState()

        view.findViewById<Button>(R.id.btn_add_journal).setOnClickListener {
            (parentFragment as? JournalFragment)?.navigateTo(AddJournalFragment.newInstance())
        }
    }

    private fun setupFragmentResultListeners() {
        // Menggunakan childFragmentManager dari parentFragment karena Add/Detail Journal
        // berada dalam scope yang sama dengan JournalContentFragment.
        parentFragmentManager.setFragmentResultListener(AddJournalFragment.REQUEST_KEY, this) { _, bundle ->
            val newEntry = bundle.getParcelable<JournalEntry>(AddJournalFragment.RESULT_KEY)
            val isUpdate = bundle.getBoolean(AddJournalFragment.EXTRA_IS_UPDATE, false)
            val position = bundle.getInt(AddJournalFragment.EXTRA_POSITION, -1)

            if (newEntry != null) {
                if (isUpdate && position != -1) {
                    adapter.updateEntry(position, newEntry)
                    Snackbar.make(requireView(), "Journal updated successfully!", Snackbar.LENGTH_SHORT).show()
                } else {
                    adapter.addEntry(newEntry)
                }
                checkEmptyState()
                updateJournalDashboard()
            }
        }

        parentFragmentManager.setFragmentResultListener(DetailJournalFragment.REQUEST_KEY, this) { _, bundle ->
            val position = bundle.getInt(DetailJournalFragment.EXTRA_POSITION, -1)
            when (bundle.getString("action")) {
                DetailJournalFragment.ACTION_DELETE -> {
                    if (position != -1) {
                        adapter.removeEntry(position)
                        checkEmptyState()
                        Snackbar.make(recyclerView, "Entri berhasil dihapus", Snackbar.LENGTH_SHORT).show()
                        updateJournalDashboard()
                    }
                }
                DetailJournalFragment.ACTION_UPDATE -> {
                    val entryToUpdate = bundle.getParcelable<JournalEntry>(DetailJournalFragment.EXTRA_JOURNAL_ENTRY)
                    if (position != -1 && entryToUpdate != null) {
                        (parentFragment as? JournalFragment)?.navigateTo(
                            AddJournalFragment.newInstance(isUpdate = true, journalEntry = entryToUpdate, position = position)
                        )
                    }
                }
            }
        }
    }

    private fun updateJournalDashboard() {
        // 1. Atur Sapaan Dinamis
        val calendar = Calendar.getInstance()
        val greeting = when (calendar.get(Calendar.HOUR_OF_DAY)) {
            in 4..10 -> "Selamat Pagi!"
            in 11..14 -> "Selamat Siang!"
            in 15..17 -> "Selamat Sore!"
            else -> "Selamat Malam!"
        }
        greetingText.text = greeting

        // 2. Hitung dan Tampilkan Statistik
        val totalCount = journalEntries.size
        val monthlyCount = journalEntries.count { entry ->
            try {
                val entryDate = SimpleDateFormat("dd MMMM yyyy", Locale.forLanguageTag("id")).parse(entry.date)
                val entryCal = Calendar.getInstance().apply { time = entryDate!! }
                entryCal.get(Calendar.MONTH) == calendar.get(Calendar.MONTH) &&
                        entryCal.get(Calendar.YEAR) == calendar.get(Calendar.YEAR)
            } catch (e: Exception) { false }
        }
        val photoCount = journalEntries.count { it.imageUrl.isNotEmpty() }

        statTotal.text = totalCount.toString()
        statMonthly.text = monthlyCount.toString()
        statPhotos.text = photoCount.toString()

        // 3. Cari dan Tampilkan Kenangan "On This Day"
        val currentMonthDay = SimpleDateFormat("dd-MM", Locale.forLanguageTag("id")).format(calendar.time)
        val memoryEntry = journalEntries.find { entry ->
            try {
                val entryDate = SimpleDateFormat("dd MMMM yyyy", Locale.forLanguageTag("id")).parse(entry.date)
                val entryCal = Calendar.getInstance().apply { time = entryDate!! }
                val entryMonthDay = SimpleDateFormat("dd-MM", Locale.forLanguageTag("id")).format(entryCal.time)

                entryMonthDay == currentMonthDay && entryCal.get(Calendar.YEAR) < calendar.get(Calendar.YEAR)
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

    private fun checkEmptyState() {
        if (adapter.itemCount == 0) {
            recyclerView.visibility = View.GONE
            emptyState.visibility = View.VISIBLE
        } else {
            recyclerView.visibility = View.VISIBLE
            emptyState.visibility = View.GONE
        }
    }
}