package com.litegral.pawpal

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.litegral.pawpal.match.model.Cat
import com.litegral.pawpal.match.model.User

class HistoryFragment : Fragment(R.layout.fragment_history) {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: HistoryAdapter
    private lateinit var emptyStateTextView: TextView
    private lateinit var backButton: ImageButton

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private var historyItems = mutableListOf<HistoryListItem>()

    private data class CatAndOwner(val cat: Cat, val owner: User)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.historyRecyclerView)
        emptyStateTextView = view.findViewById(R.id.emptyStateTextView)
        backButton = view.findViewById(R.id.backButton)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = HistoryAdapter(requireContext()) { item, _ ->
            handleRemoveItem(item)
        }
        recyclerView.adapter = adapter
        registerForContextMenu(recyclerView)

        backButton.setOnClickListener {
            findNavController().navigate(R.id.action_historyFragment_to_swipeFragment)
        }

        loadHistory()
    }

    private fun handleRemoveItem(item: HistoryListItem) {
        when (item) {
            is HistoryListItem.MatchItem -> removeMatch(item.cat)
            is HistoryListItem.LikedItem -> removeLike(item.cat)
            is HistoryListItem.Header -> { /* Headers are not removable */ }
        }
    }

    private fun removeMatch(cat: Cat) {
        val currentUserId = auth.currentUser?.uid ?: return
        val otherUserId = cat.userId

        db.collection("matches")
            .whereArrayContains("users", currentUserId)
            .get()
            .addOnSuccessListener { documents ->
                val batch = db.batch()
                documents.forEach { doc ->
                    val users = doc.get("users") as? List<*>
                    if (users != null && users.contains(otherUserId)) {
                        batch.delete(doc.reference)
                    }
                }
                batch.commit()
                    .addOnSuccessListener {
                        Toast.makeText(context, "Match removed", Toast.LENGTH_SHORT).show()
                        loadHistory()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(context, "Failed to remove match: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Failed to find match: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun removeLike(cat: Cat) {
        val currentUserId = auth.currentUser?.uid ?: return
        val likedUserId = cat.userId

        db.collection("likes")
            .whereEqualTo("likerId", currentUserId)
            .whereEqualTo("likedUserId", likedUserId)
            .get()
            .addOnSuccessListener { documents ->
                val batch = db.batch()
                documents.forEach { doc ->
                    batch.delete(doc.reference)
                }
                batch.commit()
                    .addOnSuccessListener {
                        Toast.makeText(context, "Like canceled", Toast.LENGTH_SHORT).show()
                        loadHistory()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(context, "Failed to cancel like: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Failed to find like: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onResume() {
        super.onResume()
        // Reload data when fragment becomes visible again
        loadHistory()
    }

    private fun loadHistory() {
        val currentUserId = auth.currentUser?.uid
        if (currentUserId == null) {
            emptyStateTextView.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
            return
        }

        val localHistoryItems = mutableListOf<HistoryListItem>()

        val matchesTask = db.collection("matches").whereArrayContains("users", currentUserId).get()
        val likesTask = db.collection("likes").whereEqualTo("likerId", currentUserId).get()

        Tasks.whenAllSuccess<QuerySnapshot>(matchesTask, likesTask).addOnSuccessListener { results ->
            localHistoryItems.clear()
            val matchesResult = results[0]
            val likesResult = results[1]

            // Process Matches
            val matchTasks = mutableListOf<Task<CatAndOwner?>>()
            if (!matchesResult.isEmpty) {
                for (document in matchesResult) {
                    val users = document.get("users") as? List<*>
                    val otherUserId = users?.firstOrNull { it != currentUserId } as? String
                    if (otherUserId != null) {
                        matchTasks.add(getCatAndOwner(otherUserId))
                    }
                }
            }

            // Process Likes
            val likedTasks = mutableListOf<Task<CatAndOwner?>>()
            if (!likesResult.isEmpty) {
                for (document in likesResult) {
                    val likedUserId = document.getString("likedUserId")
                    if (likedUserId != null) {
                        likedTasks.add(getCatAndOwner(likedUserId))
                    }
                }
            }

            Tasks.whenAllSuccess<CatAndOwner?>(matchTasks).addOnSuccessListener { matchedItems ->
                val validMatchedItems = matchedItems.filterNotNull()
                if (validMatchedItems.isNotEmpty()) {
                    localHistoryItems.add(HistoryListItem.Header("Matches"))
                    validMatchedItems.forEach { item ->
                        localHistoryItems.add(HistoryListItem.MatchItem(item.cat, item.owner.name))
                    }
                }

                Tasks.whenAllSuccess<CatAndOwner?>(likedTasks).addOnSuccessListener { likedItems ->
                    val validLikedItems = likedItems.filterNotNull()
                    if (validLikedItems.isNotEmpty()) {
                        localHistoryItems.add(HistoryListItem.Header("Likes"))
                        validLikedItems.forEach { item ->
                            localHistoryItems.add(HistoryListItem.LikedItem(item.cat, item.owner.name))
                        }
                    }

                    historyItems = localHistoryItems
                    updateUiWithHistory(historyItems)
                }
            }
        }.addOnFailureListener {
            Log.e("HistoryFragment", "Error loading history", it)
            emptyStateTextView.text = "Failed to load history."
            updateUiWithHistory(emptyList())
        }
    }

    private fun getCatAndOwner(userId: String): Task<CatAndOwner?> {
        val catTask = db.collection("cats").document(userId).get()
        val userTask = db.collection("users").document(userId).get()

        return Tasks.whenAll(catTask, userTask).continueWith {
            val catSnapshot = catTask.result
            val userSnapshot = userTask.result

            if (catSnapshot != null && catSnapshot.exists() && userSnapshot != null && userSnapshot.exists()) {
                val cat = catSnapshot.toObject(Cat::class.java)
                val user = userSnapshot.toObject(User::class.java)?.copy(userId = userSnapshot.id)
                if (cat != null && user != null) {
                    CatAndOwner(cat, user)
                } else {
                    null
                }
            } else {
                null
            }
        }
    }

    private fun updateUiWithHistory(historyItems: List<HistoryListItem>) {
        if (historyItems.isEmpty()) {
            emptyStateTextView.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        } else {
            emptyStateTextView.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
        }
        adapter.setItems(historyItems)
    }
}