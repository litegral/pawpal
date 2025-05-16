package com.litegral.pawpal

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.floatingactionbutton.FloatingActionButton
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
    private lateinit var petRepository: PetRepository
    private var currentItems: List<CardItem> = emptyList()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        petRepository = PetRepository.getInstance(requireContext())
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

    }

    private fun loadCardData() {
        currentItems = listOf(
            CardItem(
                "1",
                "Max",
                "Persian, black, 3 years old",
                "https://picsum.photos/400"
            ),
            CardItem("2", "Bella", "Persian, black, 2 years old", "https://picsum.photos/400"),
            CardItem("3", "Charlie", "Energetic Beagle, 1 year old", "https://picsum.photos/400"),
            CardItem("4", "Luna", "Calm German Shepherd, 4 years old", "https://picsum.photos/400"),
            CardItem("5", "Cooper", "Friendly Bulldog, 2 years old", "https://picsum.photos/400")
        )

        adapter.setItems(currentItems)
    }

    /**
     * Save the current card as liked
     */
    private fun saveLikedPet(position: Int) {
        if (position < currentItems.size) {
            val likedPet = currentItems[position]
            petRepository.likePet(likedPet)
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
                saveLikedPet(position)
                Toast.makeText(context, "Liked and saved to history!", Toast.LENGTH_SHORT).show()
            }

            Direction.Left -> {
                // Handle left (skip) swipe
                Toast.makeText(context, "Skipped", Toast.LENGTH_SHORT).show()
            }

            else -> { /* Handle other directions if needed */
            }
        }

        // Check if we've reached the end of the deck
        if (manager.topPosition == adapter.itemCount) {
            Toast.makeText(context, "No more pets available", Toast.LENGTH_SHORT).show()
            // Add code to handle end of cards, maybe reload or show a message
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
