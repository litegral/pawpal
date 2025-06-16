package com.litegral.pawpal.match.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Cat(
    val id: String = "",
    val userId: String = "",
    val name: String = "",
    val breed: String = "",
    val age: String = "",
    val gender: String = "",
    val description: String = "",
    val imageUrl: String = ""
) : Parcelable 