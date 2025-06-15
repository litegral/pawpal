package com.litegral.pawpal.dhika

import android.os.Parcelable
import com.google.firebase.firestore.DocumentId // Import untuk @DocumentId
import com.google.firebase.firestore.ServerTimestamp // Import untuk @ServerTimestamp
import kotlinx.parcelize.Parcelize
import java.util.Date

@Parcelize
data class ResourceEntry(
    @DocumentId // Mengisi field 'id' dengan ID dokumen Firestore saat membaca data
    val id: String = "",
    val userId: String = "", // Menyimpan UID pemilik resource
    val tag: String = "",
    val title: String = "",
    val date: String = "",
    val description: String = "",
    val imageUrl: String = "",
    @ServerTimestamp // Mengisi field 'timestamp' secara otomatis oleh server Firestore saat menulis data
    val timestamp: Date? = null
) : Parcelable