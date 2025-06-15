package com.litegral.pawpal

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.litegral.pawpal.dhika.JournalContentFragment
import com.litegral.pawpal.dhika.ResourceContentFragment

class JournalFragment : Fragment(), FragmentManager.OnBackStackChangedListener {

    // Hapus inisialisasi lazy untuk mengelola instance secara manual
    private var journalFragment: Fragment? = null
    private var resourceFragment: Fragment? = null
    private var activeFragment: Fragment? = null

    private lateinit var btnJournal: Button
    private lateinit var btnResource: Button
    private lateinit var tabContainer: View

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Menggunakan layout utama yang berisi FrameLayout sebagai container
        return inflater.inflate(R.layout.fragment_journal, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btnJournal = view.findViewById(R.id.btn_journal)
        btnResource = view.findViewById(R.id.btn_resource)
        tabContainer = view.findViewById(R.id.tab_container)

        childFragmentManager.addOnBackStackChangedListener(this)

        // Cari fragmen yang sudah ada atau buat yang baru
        // Ini mencegah duplikasi saat fragment di-recreate
        journalFragment = childFragmentManager.findFragmentByTag("journal_content")
        resourceFragment = childFragmentManager.findFragmentByTag("resource_content")

        val transaction = childFragmentManager.beginTransaction()

        if (journalFragment == null) {
            journalFragment = JournalContentFragment()
            transaction.add(R.id.fragment_container, journalFragment!!, "journal_content")
        }

        if (resourceFragment == null) {
            resourceFragment = ResourceContentFragment()
            transaction.add(R.id.fragment_container, resourceFragment!!, "resource_content")
        }

        // Tentukan fragmen aktif dan sembunyikan yang lain
        // Cek fragmen mana yang saat ini terlihat, atau default ke journalFragment
        activeFragment = when {
            resourceFragment?.isResumed == true -> resourceFragment
            else -> journalFragment
        }

        if (activeFragment == journalFragment) {
            resourceFragment?.let { transaction.hide(it) }
            journalFragment?.let { transaction.show(it) }
            updateButtonUI(isJournalSelected = true)
        } else {
            journalFragment?.let { transaction.hide(it) }
            resourceFragment?.let { transaction.show(it) }
            updateButtonUI(isJournalSelected = false)
        }
        transaction.commit()

        btnJournal.setOnClickListener {
            if (activeFragment != journalFragment) {
                childFragmentManager.beginTransaction().hide(activeFragment!!).show(journalFragment!!).commit()
                activeFragment = journalFragment
                updateButtonUI(isJournalSelected = true)
            }
        }

        btnResource.setOnClickListener {
            if (activeFragment != resourceFragment) {
                childFragmentManager.beginTransaction().hide(activeFragment!!).show(resourceFragment!!).commit()
                activeFragment = resourceFragment
                updateButtonUI(isJournalSelected = false)
            }
        }
    }

    override fun onBackStackChanged() {
        val hasBackStack = childFragmentManager.backStackEntryCount > 0
        tabContainer.visibility = if (hasBackStack) View.GONE else View.VISIBLE
    }

    fun navigateTo(fragment: Fragment, addToBackStack: Boolean = true) {
        val transaction = parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)

        if (addToBackStack) {
            transaction.addToBackStack(null)
        }
        transaction.commit()
    }

    override fun onResume() {
        super.onResume()
        onBackStackChanged()
    }

    private fun updateButtonUI(isJournalSelected: Boolean) {
        val whiteColor = ContextCompat.getColor(requireContext(), android.R.color.white)
        val blackColor = ContextCompat.getColor(requireContext(), android.R.color.black)

        if (isJournalSelected) {
            btnJournal.setBackgroundResource(R.drawable.tab_selected)
            btnJournal.setTextColor(whiteColor)
            btnResource.setBackgroundResource(R.drawable.tab_unselected)
            btnResource.setTextColor(blackColor)
        } else {
            btnResource.setBackgroundResource(R.drawable.tab_selected)
            btnResource.setTextColor(whiteColor)
            btnJournal.setBackgroundResource(R.drawable.tab_unselected)
            btnJournal.setTextColor(blackColor)
        }
    }
}