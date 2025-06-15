package com.litegral.pawpal.dhika

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ResourceEntry(
    val tag: String,
    val title: String,
    val date: String,
    val description: String,
    val imageUrl: String
) : Parcelable