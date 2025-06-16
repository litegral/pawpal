package com.litegral.pawpal.match.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class User(
    val userId: String = "",
    val name: String = "",
    val bio: String = "",
    val profileImageUrl: String = ""
) : Parcelable 