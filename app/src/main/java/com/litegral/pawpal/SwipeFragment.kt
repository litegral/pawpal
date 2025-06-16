package com.litegral.pawpal

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.gms.tasks.Tasks
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.DocumentSnapshot
import com.litegral.pawpal.match.model.Cat
import com.litegral.pawpal.match.model.User
import com.yuyakaido.android.cardstackview.CardStackLayoutManager
import com.yuyakaido.android.cardstackview.CardStackListener
import com.yuyakaido.android.cardstackview.CardStackView
import com.yuyakaido.android.cardstackview.Direction
import com.yuyakaido.android.cardstackview.Duration
import com.yuyakaido.android.cardstackview.StackFrom
import com.yuyakaido.android.cardstackview.SwipeAnimationSetting

class SwipeFragment : Fragment(R.layout.fragment_swipe), CardStackListener {

    private lateinit var cardStackView: CardStackView
    private lateinit var manager: CardStackLayoutManager
    private lateinit var adapter: CardStackAdapter
    private lateinit var likeButton: FloatingActionButton
    private lateinit var skipButton: FloatingActionButton
    private lateinit var historyButton: ImageButton
    private lateinit var profileButton: ImageButton
    private lateinit var noCatsLayout: LinearLayout
    private lateinit var refreshButton: Button
    private var currentItems: List<CardItem> = emptyList()
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeViews(view)
        setupCardStackView()
        setupButtons()
        loadCardData()
    }

    private fun initializeViews(view: View) {
        cardStackView = view.findViewById(R.id.cardStackView)
        likeButton = view.findViewById(R.id.likeButton)
        skipButton = view.findViewById(R.id.skipButton)
        historyButton = view.findViewById(R.id.historyButton)
        profileButton = view.findViewById(R.id.profileButton)
        noCatsLayout = view.findViewById(R.id.noCatsLayout)
        refreshButton = view.findViewById(R.id.refreshButton)
    }

    private fun setupCardStackView() {
        manager = CardStackLayoutManager(requireContext(), this)
        manager.apply {
            setStackFrom(StackFrom.Top)
            setVisibleCount(3)
            setTranslationInterval(8.0f)
            setScaleInterval(0.95f)
            setSwipeThreshold(0.3f)
            setMaxDegree(20.0f)
            setDirections(Direction.HORIZONTAL)
            setCanScrollHorizontal(true)
            setCanScrollVertical(false)
        }

        adapter = CardStackAdapter(requireContext())
        cardStackView.layoutManager = manager
        cardStackView.adapter = adapter
    }

    private fun setupButtons() {
        likeButton.setOnClickListener {
            // Swipe right with animation
            val setting = SwipeAnimationSetting.Builder()
                .setDirection(Direction.Right)
                .setDuration(Duration.Normal.duration)
                .build()
            manager.setSwipeAnimationSetting(setting)
            cardStackView.swipe()
        }

        skipButton.setOnClickListener {
            // Swipe left with animation
            val setting = SwipeAnimationSetting.Builder()
                .setDirection(Direction.Left)
                .setDuration(Duration.Normal.duration)
                .build()
            manager.setSwipeAnimationSetting(setting)
            cardStackView.swipe()
        }

        // Set up history button click to navigate to history fragment
        historyButton.setOnClickListener {
            findNavController().navigate(R.id.action_swipeFragment_to_historyFragment)
        }

        profileButton.setOnClickListener {
            findNavController().navigate(R.id.action_swipeFragment_to_profileFragment)
        }

        refreshButton.setOnClickListener {
            loadCardData()
        }
    }

    private fun loadCardData() {
        noCatsLayout.visibility = View.GONE
        cardStackView.visibility = View.VISIBLE
        val currentUserId = auth.currentUser?.uid
        if (currentUserId == null) {
            Toast.makeText(requireContext(), "User not logged in.", Toast.LENGTH_SHORT).show()
            return
        }

        val matchesTask = db.collection("matches").whereArrayContains("users", currentUserId).get()
        val likesTask = db.collection("likes").whereEqualTo("likerId", currentUserId).get()
        val skipsTask = db.collection("skips").whereEqualTo("skipperId", currentUserId).get()

        Tasks.whenAllSuccess<QuerySnapshot>(matchesTask, likesTask, skipsTask).addOnSuccessListener { results ->
            val matchesResult = results[0]
            val likesResult = results[1]
            val skipsResult = results[2]

            val matchedUserIds = matchesResult.documents.mapNotNull {
                (it.get("users") as? List<*>)?.firstOrNull { id -> id != currentUserId } as? String
            }

            val likedUserIds = likesResult.documents.mapNotNull {
                it.getString("likedUserId")
            }
            
            val skippedUserIds = skipsResult.documents.mapNotNull {
                it.getString("skippedUserId")
            }

            // We exclude matched, liked, and skipped users
            val excludedUserIds = (matchedUserIds + likedUserIds + skippedUserIds).toSet()

            db.collection("cats")
                .whereNotEqualTo("userId", currentUserId)
                .get()
                .addOnSuccessListener { documents ->
                    if (documents != null && !documents.isEmpty) {
                        val catList = documents.toObjects(Cat::class.java)
                            .filter { it.userId !in excludedUserIds }

                        if (catList.isNotEmpty()) {
                            val ownerIds = catList.map { it.userId }.distinct()
                            val ownerTasks = ownerIds.map { db.collection("users").document(it).get() }
                            Tasks.whenAllSuccess<DocumentSnapshot>(ownerTasks).addOnSuccessListener { ownerSnapshots ->
                                val owners = ownerSnapshots.mapNotNull { doc ->
                                    doc.toObject(User::class.java)?.copy(userId = doc.id)
                                }.associateBy { it.userId }

                                currentItems = catList.map { cat ->
                                    val owner = owners[cat.userId]
                                    CardItem(
                                        id = cat.id,
                                        ownerId = cat.userId,
                                        ownerName = owner?.name ?: "Unknown",
                                        name = cat.name,
                                        age = cat.age,
                                        breed = cat.breed,
                                        gender = cat.gender,
                                        description = cat.description,
                                        imageUrl = cat.imageUrl
                                    )
                                }
                                adapter.setItems(currentItems)
                                noCatsLayout.visibility = View.GONE
                                cardStackView.visibility = View.VISIBLE

                            }.addOnFailureListener { exception ->
                                Toast.makeText(context, "Failed to load owner details: ${exception.message}", Toast.LENGTH_SHORT).show()
                                noCatsLayout.visibility = View.VISIBLE
                                cardStackView.visibility = View.GONE
                            }
                        } else {
                            noCatsLayout.visibility = View.VISIBLE
                            cardStackView.visibility = View.GONE
                        }
                    } else {
                        noCatsLayout.visibility = View.VISIBLE
                        cardStackView.visibility = View.GONE
                    }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(context, "Failed to load pets: ${exception.message}", Toast.LENGTH_SHORT).show()
                    noCatsLayout.visibility = View.VISIBLE
                    cardStackView.visibility = View.GONE
                }
        }.addOnFailureListener { exception ->
            Toast.makeText(context, "Failed to load interaction data: ${exception.message}", Toast.LENGTH_SHORT).show()
            noCatsLayout.visibility = View.VISIBLE
            cardStackView.visibility = View.GONE
        }
    }

    private fun handleLike(position: Int) {
        if (position >= currentItems.size) return

        val likedCard = currentItems[position]
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(requireContext(), "You must be logged in to like cats.", Toast.LENGTH_SHORT).show()
            return
        }
        val currentUserId = currentUser.uid
        val otherUserId = likedCard.ownerId

        // 1. Save the like
        val like = hashMapOf(
            "likerId" to currentUserId,
            "likedUserId" to otherUserId,
            "timestamp" to FieldValue.serverTimestamp()
        )

        db.collection("likes").add(like)
            .addOnSuccessListener {
                Toast.makeText(context, "Liked ${likedCard.name}", Toast.LENGTH_SHORT).show()
                // 2. Check for a match
                checkForMatch(currentUserId, otherUserId)
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Failed to like: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun handleSkip(position: Int) {
        if (position >= currentItems.size) return

        val skippedCard = currentItems[position]
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(requireContext(), "You must be logged in to skip cats.", Toast.LENGTH_SHORT).show()
            return
        }
        val currentUserId = currentUser.uid
        val otherUserId = skippedCard.ownerId

        // Save the skip
        val skip = hashMapOf(
            "skipperId" to currentUserId,
            "skippedUserId" to otherUserId,
            "timestamp" to FieldValue.serverTimestamp()
        )

        db.collection("skips").add(skip)
            .addOnSuccessListener {
                Toast.makeText(context, "Skipped ${skippedCard.name}", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Failed to record skip: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun checkForMatch(currentUserId: String, otherUserId: String) {
        // Check if the other user has liked the current user
        db.collection("likes")
            .whereEqualTo("likerId", otherUserId)
            .whereEqualTo("likedUserId", currentUserId)
            .limit(1)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    // It's a match!
                    createMatch(currentUserId, otherUserId)
                }
            }
    }

    private fun createMatch(userId1: String, userId2: String) {
        val match = hashMapOf(
            "users" to listOf(userId1, userId2),
            "timestamp" to FieldValue.serverTimestamp()
        )

        db.collection("matches").add(match)
            .addOnSuccessListener {
                Toast.makeText(context, "It's a Match!", Toast.LENGTH_LONG).show()
            }
    }

    // CardStackListener implementation
    override fun onCardDragging(direction: Direction, ratio: Float) {
        // Optional: Handle card dragging
    }

    override fun onCardSwiped(direction: Direction) {
        val position = manager.topPosition - 1 // Get the position of the swiped card

        when (direction) {
            Direction.Right -> {
                // Handle right (like) swipe
                handleLike(position)
            }

            Direction.Left -> {
                // Handle left (skip) swipe
                handleSkip(position)
            }

            else -> { /* Handle other directions if needed */
            }
        }

        // Check if we've reached the end of the deck
        if (manager.topPosition == adapter.itemCount) {
            noCatsLayout.visibility = View.VISIBLE
            cardStackView.visibility = View.GONE
        }
    }

    override fun onCardRewound() {
        // Optional: Handle card rewind (if you implement undo functionality)
    }

    override fun onCardCanceled() {
        // Optional: Handle card cancellation
    }

    override fun onCardAppeared(view: View, position: Int) {
        // Optional: Handle card appearance
    }

    override fun onCardDisappeared(view: View, position: Int) {
        // Optional: Handle card disappearance
    }
}