package com.litegral.pawpal.akbar.model

import android.os.Parcelable
import com.google.firebase.firestore.ServerTimestamp
import kotlinx.parcelize.Parcelize
import java.util.Date

@Parcelize
data class AdoptionRequest(
    var id: String = "",
    var petId: String = "",
    var petName: String = "",
    var petImageUrl: String = "",
    var ownerId: String = "",
    var adopterId: String = "",
    var adopterName: String = "",
    var adopterContact: String = "",
    var message: String = "",
    var homePhotoUrl: String? = null,
    var status: String = "Pending", // Pending, Accepted, Declined
    @ServerTimestamp
    var requestDate: Date? = null
) : Parcelable