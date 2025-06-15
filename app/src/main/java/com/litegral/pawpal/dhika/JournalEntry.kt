package com.litegral.pawpal.dhika

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class JournalEntry(
    val date: String,
    val title: String,
    val description: String,
    val imageUrl: String
) : Parcelable