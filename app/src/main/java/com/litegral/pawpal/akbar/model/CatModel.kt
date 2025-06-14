package com.litegral.pawpal.akbar.model

import android.os.Parcelable
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp
import kotlinx.parcelize.Parcelize
import java.util.Date

@IgnoreExtraProperties
@Parcelize
data class CatModel(
    var id: String = "",
    var uploaderUid: String = "",
    var uploaderName: String = "",
    var name: String = "",
    var age: String = "",
    var isFemale: Boolean = false,
    var breed: String = "",
    var description: String = "",
    var petPosition: String = "",
    var imageUrls: List<String> = listOf(),
    @ServerTimestamp
    var postedDate: Date? = null
) : Parcelable