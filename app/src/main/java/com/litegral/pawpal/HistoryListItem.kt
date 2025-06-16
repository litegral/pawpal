package com.litegral.pawpal

import com.litegral.pawpal.match.model.Cat

sealed class HistoryListItem {
    data class Header(val title: String) : HistoryListItem()
    data class MatchItem(val cat: Cat, val ownerName: String) : HistoryListItem()
    data class LikedItem(val cat: Cat, val ownerName: String) : HistoryListItem()
} 