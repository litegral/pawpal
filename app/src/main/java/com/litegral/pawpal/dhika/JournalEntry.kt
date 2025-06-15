package com.litegral.pawpal.dhika

import android.os.Parcelable
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import kotlinx.parcelize.Parcelize
import java.util.Date

@Parcelize
data class JournalEntry(
    @DocumentId
    val id: String = "",

    val userId: String = "", // <-- TAMBAHKAN FIELD INI untuk menyimpan UID pemilik

    val date: String = "",
    val title: String = "",
    val description: String = "",
    val imageUrl: String = "",

    @ServerTimestamp
    val timestamp: Date? = null
) : Parcelable